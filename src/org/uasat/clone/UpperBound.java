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

public class UpperBound extends Meetable {
	protected final SatSolver<?> solver;
	protected final Joinable clone;
	protected int relArity;
	protected final List<Relation<Boolean>> relations = new ArrayList<Relation<Boolean>>();

	public UpperBound(Joinable clone, int relArity) {
		super(clone.getSize());
		assert relArity >= 1;

		this.clone = clone;
		this.relArity = relArity;
		this.solver = SatSolver.getDefault();
	}

	public void add(Relation<Boolean> rel) {
		assert clone.verify(rel);
		relations.add(rel);
	}

	public <BOOL> BOOL member(BoolAlgebra<BOOL> alg, Operation<BOOL> op) {
		BOOL b = alg.TRUE;

		for (Relation<Boolean> rel : relations)
			b = alg.and(b, op.preserves(Relation.lift(alg, rel)));

		return b;
	}

	public boolean verify(final Operation<Boolean> op) {
		Relation<Boolean> rel;
		do {
			SatProblem problem = new SatProblem(Util.createShape(size, relArity)) {
				@Override
				public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg, List<Tensor<BOOL>> tensors) {
					Relation<BOOL> rel = new Relation<BOOL>(alg, tensors.get(0));

					BOOL b = alg.not(Operation.lift(alg, op).preserves(rel));
					return alg.and(b, clone.member(alg, rel));
				}
			};

			List<Tensor<Boolean>> sol = problem.solveOne(solver);
			if (sol == null)
				return true;

			rel = Relation.wrap(sol.get(0));
		} while (!clone.verify(rel));

		relations.add(rel);
		return false;
	}
}
