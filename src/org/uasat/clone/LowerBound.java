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

public class LowerBound extends FinitelyGen {
	protected final SatSolver<?> solver;
	protected final FinitelyRel clone;
	protected int opArity;
	protected final List<Operation<Boolean>> operations = new ArrayList<Operation<Boolean>>();

	public LowerBound(FinitelyRel clone, int opArity) {
		super(clone.getSize());
		assert opArity >= 1;

		this.clone = clone;
		this.opArity = opArity;
		this.solver = SatSolver.getDefault();
	}

	public void add(Operation<Boolean> op) {
		assert op.isOperation() && clone.isMember(op);
		operations.add(op);
	}

	public <BOOL> BOOL isPossibleMember(BoolAlgebra<BOOL> alg,
			Relation<BOOL> rel) {
		BOOL b = alg.TRUE;

		for (Operation<Boolean> op : operations)
			b = alg.and(b, Operation.lift(alg, op).preserves(rel));

		return b;
	}

	public boolean isMember(final Relation<Boolean> rel) {
		Operation<Boolean> op;
		do {
			SatProblem problem = new SatProblem(Util.createShape(size,
					opArity + 1)) {
				@Override
				public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
						List<Tensor<BOOL>> tensors) {
					Operation<BOOL> op = new Operation<BOOL>(alg,
							tensors.get(0));

					BOOL b = alg.not(op.preserves(Relation.lift(alg, rel)));
					b = alg.and(b, op.isOperation());
					return alg.and(b, clone.isPossibleMember(alg, op));
				}
			};

			List<Tensor<Boolean>> sol = problem.solveOne(solver);
			if (sol == null)
				return true;

			op = Operation.wrap(sol.get(0));
		} while (!clone.isMember(op));

		operations.add(op);
		return false;
	}
}
