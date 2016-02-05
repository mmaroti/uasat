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

public class GeneratedRels implements Iterable<Relation<Boolean>> {
	private final int size;
	private final int arity;
	private final TreeSet<Relation<Boolean>> relations;
	private final SatSolver<?> solver;

	public GeneratedRels(int size, int arity) {
		this(size, arity, SatSolver.getDefault());
	}

	public GeneratedRels(int size, int arity, SatSolver<?> solver) {
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
		for (;;) {
			int a = relations.size();

			List<Relation<Boolean>> rels = new ArrayList<Relation<Boolean>>(
					relations);

			for (int i = 0; i < rels.size(); i++) {
				Relation<Boolean> r1 = rels.get(i);
				for (int j = 0; j < i; j++) {
					Relation<Boolean> r2 = rels.get(j);

					Relation<Boolean> r3 = r1.intersect(r2);
					if (relations.add(r3))
						rels.add(r3);
				}
			}

			if (a == relations.size())
				break;
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

		for (;;) {
			int a = relations.size();

			List<Relation<Boolean>> rels = new ArrayList<Relation<Boolean>>(
					relations);

			for (int i = 0; i < rels.size(); i++) {
				Relation<Boolean> r1 = rels.get(i);
				for (int j = 0; j <= i; j++) {
					Relation<Boolean> r2 = rels.get(j);

					Relation<Boolean> r3 = r1.compose(r2);
					if (relations.add(r3))
						rels.add(r3);

					if (i == j)
						continue;

					r3 = r2.compose(r1);
					if (relations.add(r3))
						rels.add(r3);
				}
			}

			if (a == relations.size())
				break;
		}
	}

	public void addTreeDefUnary(final Relation<Boolean> relation) {
		assert arity == 1;

		List<Tensor<Boolean>> sig = new ArrayList<Tensor<Boolean>>();
		sig.add(Relation.full(size, 1).getTensor());

		Tensor<Boolean> empty = Relation.empty(size, 1).getTensor();
		for (int i = 1; i < relation.getArity(); i++)
			sig.add(empty);

		for (;;) {
			int s = relations.size();

			for (int x = 0; x < relation.getArity(); x++) {
				final int xf = x;

				SatProblem problem = new SatProblem(sig) {
					@Override
					public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
							List<Tensor<BOOL>> tensors) {
						Relation<BOOL> r0 = new Relation<BOOL>(alg,
								tensors.get(0));
						BOOL b = alg.not(r0.isMemberOf(relations));

						Relation<BOOL> rel = Relation.lift(alg,
								relation.permute(xf));

						for (int i = 1; i < tensors.size(); i++) {
							Relation<BOOL> r = new Relation<BOOL>(alg,
									tensors.get(i));
							b = alg.and(b, r.isMemberOf(relations));
							rel = rel.compose(r);
						}

						b = alg.and(b, rel.isEqualTo(r0));
						return b;
					}
				};

				Tensor<Boolean> tensor = problem.solveAll(solver).get(0);

				int a = relations.size();
				for (Tensor<Boolean> t : Tensor.unstack(tensor))
					relations.add(Relation.wrap(t));

				assert a + tensor.getLastDim() == relations.size();
			}

			if (relations.size() == s)
				break;
		}
	}

	public void addPathDefBinary(final List<Relation<Boolean>> subalgs,
			final Relation<Boolean> relation) {
		assert arity == 2;

		if (relation.getArity() <= 1)
			return;

		List<Tensor<Boolean>> sig = new ArrayList<Tensor<Boolean>>();
		sig.add(Relation.full(size, 2).getTensor());

		Tensor<Boolean> empty = Relation.empty(size, 1).getTensor();
		for (int i = 0; i < relation.getArity(); i++)
			sig.add(empty);

		for (;;) {
			int s = relations.size();

			for (int x = 0; x < relation.getArity(); x++) {
				for (int y = 0; y < relation.getArity(); y++) {
					if (x == y)
						continue;
					final int xf = x, yf = y;

					SatProblem problem = new SatProblem(sig) {
						@Override
						public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
								List<Tensor<BOOL>> tensors) {

							Relation<BOOL> r0 = new Relation<BOOL>(alg,
									tensors.get(0));
							BOOL b = alg.not(r0.isMemberOf(relations));

							Relation<BOOL> r1 = new Relation<BOOL>(alg,
									tensors.get(1));
							b = alg.and(b, r1.isMemberOf(subalgs));

							Relation<BOOL> r2 = new Relation<BOOL>(alg,
									tensors.get(2));
							b = alg.and(b, r2.isMemberOf(subalgs));

							Relation<BOOL> rel = Relation.lift(alg,
									relation.permute(xf, yf));

							for (int i = 3; i < tensors.size(); i++) {
								Relation<BOOL> r = new Relation<BOOL>(alg,
										tensors.get(i));
								b = alg.and(b, r.isMemberOf(subalgs));
								rel = rel.compose(r);
							}

							rel = rel.intersect(r1.cartesian(r2));
							b = alg.and(b, rel.isEqualTo(r0));

							return b;
						}
					};

					Tensor<Boolean> tensor = problem.solveAll(solver).get(0);

					int a = relations.size();
					for (Tensor<Boolean> t : Tensor.unstack(tensor)) {
						Relation<Boolean> r = Relation.wrap(t);
						assert r.project(0).isMemberOf(subalgs);
						assert r.project(1).isMemberOf(subalgs);
						relations.add(r);
					}
					assert a + tensor.getLastDim() == relations.size();
				}
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

	public static GeneratedRels getTreeDefUnary(Structure<Boolean> structure) {
		GeneratedRels gen = new GeneratedRels(structure.getSize(), 1);

		gen.addSingletons();
		gen.addFull();
		for (;;) {
			int c = gen.getCount();

			gen.addIntersections();
			for (Relation<Boolean> rel : structure.getRelations())
				gen.addTreeDefUnary(rel);

			if (c == gen.getCount())
				break;
		}

		return gen;
	}

	public void addPathDefBinary(List<Relation<Boolean>> subalgs,
			Structure<Boolean> structure) {
		assert arity == 2;

		for (Relation<Boolean> s : subalgs)
			add(s.diagonal(2));

		for (Relation<Boolean> rel : structure.getRelations())
			addPathDefBinary(subalgs, rel);

		addCompositions();
	}

	public void print() {
		System.out.println("generated rels of size " + size + " arity " + arity
				+ " count " + relations.size());

		int c = 0;
		for (Relation<Boolean> rel : relations)
			System.out.println((c++) + ":\t" + Relation.formatMembers(rel));

		System.out.println();
	}

	@Override
	public Iterator<Relation<Boolean>> iterator() {
		return relations.iterator();
	}
}
