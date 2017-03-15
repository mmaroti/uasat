/**
 * Copyright (C) Miklos Maroti, 2015-2017
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

package org.uasat.clone;

import java.util.*;
import org.uasat.core.*;
import org.uasat.math.*;

public class Split {
	protected final SatSolver<?> solver;
	protected final FinitelyGen clone1;
	protected final FinitelyRel clone2;

	protected Relation<Boolean> rel = null;
	protected Operation<Boolean> op = null;
	public boolean trace = false;

	public Split(FinitelyGen clone1, FinitelyRel clone2) {
		this.solver = SatSolver.getDefault();
		this.clone1 = clone1;
		this.clone2 = clone2;

		assert clone1.getSize() == clone2.getSize();
	}

	public int getSize() {
		return clone1.getSize();
	}

	public Relation<Boolean> getRelation() {
		return rel;
	}

	public Operation<Boolean> getOperation() {
		return op;
	}

	public void print() {
		if (rel == null)
			System.out.println("no split found");
		else {
			System.out.println("split found");
			System.out.println(Relation.format(rel));
			System.out.println(Operation.format(op));
		}
	}

	private boolean find(int relArity, int opArity,
			final Relation<Boolean> above) {
		do {
			SatProblem problem = new SatProblem(Util.createShape(getSize(),
					relArity), Util.createShape(getSize(), opArity + 1)) {
				@Override
				public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
						List<Tensor<BOOL>> tensors) {
					Relation<BOOL> rel = new Relation<BOOL>(alg, tensors.get(0));
					Operation<BOOL> op = new Operation<BOOL>(alg,
							tensors.get(1));

					BOOL b = op.isOperation();
					b = alg.and(b, alg.not(op.preserves(rel)));
					b = alg.and(b, clone1.isPossibleMember(alg, rel));
					b = alg.and(b, clone2.isPossibleMember(alg, op));

					if (above != null) {
						Relation<BOOL> above2 = Relation.lift(alg, above);
						b = alg.and(b, above2.isProperSubsetOf(rel));
					}

					return b;
				}
			};

			List<Tensor<Boolean>> sol = problem.solveOne(solver);
			if (sol == null) {
				rel = null;
				op = null;

				if (trace)
					System.out.println("trace: not found");

				return false;
			}

			rel = Relation.wrap(sol.get(0));
			op = Operation.wrap(sol.get(1));

			if (trace)
				System.out.println("trace: " + Relation.format(rel));
		} while (!clone1.isMember(rel) || !clone2.isMember(op));

		if (trace)
			System.out.println("trace: found one");

		return true;
	}

	/**
	 * Returns <code>true</code> if the finitely generated <code>clone1</code>
	 * is not greater than or equal to the finitely related <code>clone2</code>
	 * as witnessed by a pair of relation and operation of the specified
	 * arities. If such pair is found, then the relation is compatible with
	 * <code>clone1</code>, the operation is a member of <code>clone2</code> but
	 * the relation is not compatible with the operation. You can query the
	 * found relation and operation using <code>getRelation()</code> and
	 * <code>getOperation()</code>.
	 */
	public boolean findOne(int relArity, int opArity) {
		return find(relArity, opArity, null);
	}

	public boolean findMaxRel(int relArity, int opArity) {
		Relation<Boolean> relx = null;
		Operation<Boolean> opx = null;

		while (find(relArity, opArity, relx)) {
			relx = rel;
			opx = op;
		}

		rel = relx;
		op = opx;

		return rel != null;
	}
}
