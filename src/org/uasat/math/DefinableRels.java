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
	private final SatSolver<?> solver;

	public DefinableRels(Structure<Boolean> structure) {
		assert structure != null;

		this.structure = structure;
		solver = new Sat4J();
	}

	public DefinableRels(Structure<Boolean> structure, SatSolver<?> solver) {
		assert structure != null && solver != null;

		this.structure = structure;
		this.solver = solver;
	}

	public Structure<Boolean> getStructure() {
		return structure;
	}

	public SatSolver<?> getSolver() {
		return solver;
	}

	public void addIntersections(final Set<Relation<Boolean>> set, int arity) {
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

				BOOL b = alg.not(s0.isMemberOf(set));
				b = alg.and(b, s1.isMemberOf(set));
				b = alg.and(b, s2.isMemberOf(set));
				b = alg.and(b, s0.isEqualTo(s1.intersect(s2)));

				return b;
			}
		};

		Tensor<Boolean> tensor = prob.solveAll(solver).get(0);
		for (Tensor<Boolean> t : Tensor.unstack(tensor)) {
			assert !Relation.wrap(t).isMemberOf(set);
			set.add(Relation.wrap(t));
		}
	}
}
