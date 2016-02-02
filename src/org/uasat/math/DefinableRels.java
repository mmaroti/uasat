/**
 *	Copyright (C) Miklos Maroti, 2015-2016
 *
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the 
 * Free Software Foundation; either version 2 of the License, or (at your 
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General 
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along 
 * with this program; if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.uasat.math;

import java.util.*;

import org.uasat.core.*;

public class DefinableRels {
	private final int size;
	private final int arity;
	private final TreeSet<Relation<Boolean>> relations;
	private final SatSolver<?> solver;

	public DefinableRels(int size, int arity) {
		this(size, arity, SatSolver.getDefault());
	}

	public DefinableRels(int size, int arity, SatSolver<?> solver) {
		assert size >= 1 && arity >= 1 && solver != null;

		this.size = size;
		this.arity = arity;
		this.relations = new TreeSet<Relation<Boolean>>(Relation.COMPARATOR);
		this.solver = solver;
	}

	public SatSolver<?> getSolver() {
		return solver;
	}

	public int getSize() {
		return size;
	}

	public int getArity() {
		return arity;
	}

	public List<Relation<Boolean>> getRelations() {
		return new ArrayList<Relation<Boolean>>(relations);
	}

	public int getCount() {
		return relations.size();
	}

	public void clear() {
		relations.clear();
	}

	public void add(Relation<Boolean> rel) {
		assert rel.getArity() == arity && rel.getSize() == size;
		relations.add(rel);
	}

	public void addFull() {
		relations.add(Relation.full(size, arity));
	}

	public void addSingletons() {
		assert arity == 1;
		for (int i = 0; i < size; i++)
			relations.add(Relation.singleton(size, i));
	}

	public void addIntersections() {
		Tensor<Boolean> full = Relation.full(size, arity).getTensor();
		Tensor<Boolean> empty = Relation.empty(size, arity).getTensor();

		SatProblem problem = new SatProblem(full, empty, empty) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> s0 = new Relation<BOOL>(alg, tensors.get(0));
				Relation<BOOL> s1 = new Relation<BOOL>(alg, tensors.get(1));
				Relation<BOOL> s2 = new Relation<BOOL>(alg, tensors.get(2));

				BOOL b = alg.not(s0.isMemberOf(relations));
				b = alg.and(b, s1.isMemberOf(relations));
				b = alg.and(b, s2.isMemberOf(relations));
				b = alg.and(b, s0.isEqualTo(s1.intersect(s2)));

				return b;
			}
		};

		for (;;) {
			Tensor<Boolean> tensor = problem.solveAll(solver).get(0);
			if (tensor.getLastDim() == 0)
				break;

			int a = relations.size();
			for (Tensor<Boolean> t : Tensor.unstack(tensor))
				relations.add(Relation.wrap(t));

			assert a + tensor.getLastDim() == relations.size();
		}
	}

	public void addInverses() {
		assert arity == 2;

		List<Relation<Boolean>> rels = new ArrayList<Relation<Boolean>>(
				relations);
		for (Relation<Boolean> rel : rels)
			relations.add(rel.rotate(1));
	}

	public void addCompositions() {
		assert arity == 2;

		Tensor<Boolean> full = Relation.full(size, arity).getTensor();
		Tensor<Boolean> empty = Relation.empty(size, arity).getTensor();

		SatProblem problem = new SatProblem(full, empty, empty) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> s0 = new Relation<BOOL>(alg, tensors.get(0));
				Relation<BOOL> s1 = new Relation<BOOL>(alg, tensors.get(1));
				Relation<BOOL> s2 = new Relation<BOOL>(alg, tensors.get(2));

				BOOL b = alg.not(s0.isMemberOf(relations));
				b = alg.and(b, s1.isMemberOf(relations));
				b = alg.and(b, s2.isMemberOf(relations));
				b = alg.and(b, s0.isEqualTo(s1.compose(s2)));

				return b;
			}
		};

		for (;;) {
			Tensor<Boolean> tensor = problem.solveAll(solver).get(0);
			if (tensor.getLastDim() == 0)
				break;

			int a = relations.size();
			for (Tensor<Boolean> t : Tensor.unstack(tensor))
				relations.add(Relation.wrap(t));

			assert a + tensor.getLastDim() == relations.size();
		}
	}

	public void addEdgeDefinableSubalgs(final Relation<Boolean> relation) {
		assert arity == 1;

		for (;;) {
			int s = relations.size();

			for (int c = 0; c < relation.getArity(); c++) {
				final int coord = c;
				Tensor<Boolean> full = Relation.full(size, 1).getTensor();
				Tensor<Boolean> empty = Relation.empty(size, 1).getTensor();

				List<Tensor<Boolean>> sig = new ArrayList<Tensor<Boolean>>();
				for (int i = 0; i < relation.getArity(); i++)
					sig.add(i == coord ? full : empty);

				SatProblem problem = new SatProblem(sig) {
					@Override
					public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
							List<Tensor<BOOL>> tensors) {
						Relation<BOOL> rel = Relation.lift(alg, relation);

						List<Relation<BOOL>> proj = new ArrayList<Relation<BOOL>>();
						for (Tensor<BOOL> t : tensors)
							proj.add(new Relation<BOOL>(alg, t));

						BOOL b = alg.TRUE;
						for (int i = 0; i < relation.getArity(); i++) {
							BOOL c = proj.get(i).isMemberOf(relations);
							if (i == coord)
								c = alg.not(c);
							b = alg.and(b, c);
						}

						for (int i = 0; i < coord; i++)
							rel = proj.get(i).compose(rel);
						for (int i = relation.getArity() - 1; i > coord; i--)
							rel = rel.compose(proj.get(i));

						b = alg.and(b, rel.isEqualTo(proj.get(coord)));

						return b;
					}
				};

				Tensor<Boolean> tensor = problem.solveAll(solver).get(coord);

				int a = relations.size();
				for (Tensor<Boolean> t : Tensor.unstack(tensor))
					relations.add(Relation.wrap(t));

				assert a + tensor.getLastDim() == relations.size();
			}

			if (relations.size() == s)
				break;
		}
	}

	public void keepNonEmpty() {
		relations.remove(Relation.empty(size, arity));
	}

	public void keepMeetIrreducibles() {
		List<Relation<Boolean>> rels = new ArrayList<Relation<Boolean>>(
				relations);
		relations.clear();

		while (!rels.isEmpty()) {
			Relation<Boolean> rel = rels.remove(rels.size() - 1);

			Relation<Boolean> m = Relation.full(size, arity);
			for (Relation<Boolean> r : rels) {
				if (rel.isSubsetOf(r))
					m = m.intersect(r);
			}

			for (Relation<Boolean> r : relations) {
				if (rel.isSubsetOf(r))
					m = m.intersect(r);
			}

			if (!m.isEqualTo(rel))
				relations.add(rel);
		}
	}

	public void addTreeDefinableSubalgs(Structure<Boolean> structure) {
		assert arity == 1;

		addSingletons();
		addFull();
		for (;;) {
			int c = getCount();

			addIntersections();
			for (Relation<Boolean> rel : structure.getRelations())
				addEdgeDefinableSubalgs(rel);

			if (c == getCount())
				break;
		}
		keepNonEmpty();
	}

	public void printRelations(String msg) {
		System.out.println(msg + ": " + relations.size());

		int i = 0;
		for (Relation<Boolean> rel : relations)
			System.out.println(Util.formatIndex(i++) + ": "
					+ Relation.formatMembers(rel));

		System.out.println();
	}
}
