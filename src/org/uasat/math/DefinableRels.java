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
import org.uasat.solvers.*;

public class DefinableRels {
	private final Structure<Boolean> structure;
	private final int arity;
	private final SatSolver<?> solver;
	private final TreeSet<Relation<Boolean>> relations;

	public DefinableRels(Structure<Boolean> structure, int arity) {
		this(structure, arity, new Sat4J());
	}

	public DefinableRels(Structure<Boolean> structure, int arity,
			SatSolver<?> solver) {
		assert structure != null && arity >= 1 && solver != null;

		this.structure = structure;
		this.arity = arity;
		this.solver = solver;
		this.relations = new TreeSet<Relation<Boolean>>(Relation.COMPARATOR);
	}

	public Structure<Boolean> getStructure() {
		return structure;
	}

	public SatSolver<?> getSolver() {
		return solver;
	}

	public int getArity() {
		return arity;
	}

	public Set<Relation<Boolean>> getRelations() {
		return relations;
	}

	public void addRelation(Relation<Boolean> rel) {
		assert rel.getArity() == arity && rel.getSize() == structure.getSize();
		relations.add(rel);
	}

	public void addFull() {
		relations.add(Relation.full(structure.getSize(), arity));
	}

	public void addDiagonals() {
		int size = structure.getSize();
		for (int i = 0; i < size; i++) {
			Relation<Boolean> s = Relation.singleton(size, i);
			relations.add(s.diagonal(arity));
		}
	}

	public void removeEmpty() {
		relations.remove(Relation.empty(structure.getSize(), arity));
	}

	public void addIntersections() {
		int size = structure.getSize();
		Tensor<Boolean> full = Relation.full(size, arity).getTensor();
		Tensor<Boolean> empty = Relation.empty(size, arity).getTensor();

		BoolProblem prob = new BoolProblem(full, empty, empty) {
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

		Tensor<Boolean> tensor = prob.solveAll(solver).get(0);

		int a = relations.size();
		for (Tensor<Boolean> t : Tensor.unstack(tensor))
			relations.add(Relation.wrap(t));

		assert a + tensor.getLastDim() == relations.size();
	}

	public void addEdgeDefinables() {
		int size = structure.getSize();

		for (Relation<Boolean> rel : structure.getRelations()) {
			for (int cord = 0; cord < rel.getArity(); cord++) {
				Tensor<Boolean> full = Relation.full(size, arity).getTensor();
				Tensor<Boolean> empty = Relation.empty(size, arity).getTensor();

				List<Tensor<Boolean>> sig = new ArrayList<Tensor<Boolean>>();
				for (int i = 0; i < rel.getSize(); i++)
					sig.add(i == cord ? full : empty);

				BoolProblem problem = new BoolProblem(sig) {
					@Override
					public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
							List<Tensor<BOOL>> tensors) {
						// TODO Auto-generated method stub
						return null;
					}

				};
			}
		}
	}
}
