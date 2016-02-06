/**
 *	Copyright (C) Miklos Maroti, 2015
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

package org.uasat.research;

import java.text.*;
import java.util.*;

import org.uasat.core.*;
import org.uasat.math.*;

public class Quasigroup {
	SatSolver<?> solver = SatSolver.getDefault();

	Operation<Boolean> generateLatinSquare(int size) {
		SatProblem problem = new SatProblem(new int[] { size, size, size }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> rel = new Relation<BOOL>(alg, tensors.get(0));
				BOOL b = rel.isOperation();
				b = alg.and(b, rel.rotate(1).isOperation());
				b = alg.and(b, rel.rotate(2).isOperation());

				Operation<BOOL> op = rel.asOperation();
				b = alg.and(b, op.isUnitElement(0));

				return b;
			}
		};

		List<Tensor<Boolean>> sol = problem.solveOne(solver);
		assert sol != null;

		return Operation.wrap(sol.get(0));
	}

	void checkEquivalence(final Operation<Boolean> q1,
			final Operation<Boolean> q2) {
		int size = q1.getSize();
		assert size == q2.getSize() && q1.getArity() == 2 && q2.getArity() == 2;

		SatProblem problem = new SatProblem(new int[] { size, size },
				new int[] { size, size }, new int[] { size, size }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Operation<BOOL> s1 = Operation.lift(alg, q1);
				Operation<BOOL> s2 = Operation.lift(alg, q2);
				Operation<BOOL> p1 = new Operation<BOOL>(alg, tensors.get(0));
				Operation<BOOL> p2 = new Operation<BOOL>(alg, tensors.get(1));
				Operation<BOOL> p3 = new Operation<BOOL>(alg, tensors.get(2));

				BOOL b = p1.isOperation();
				b = alg.and(b, p2.isOperation());
				b = alg.and(b, p3.isOperation());
				b = alg.and(b, p1.isSurjective());
				b = alg.and(b, p2.isSurjective());
				b = alg.and(b, p3.isSurjective());

				Relation<BOOL> r = s1.asRelation().compose(p1.asRelation());
				r = r.rotate(1).compose(p2.asRelation());
				r = r.rotate(1).compose(p3.asRelation()).rotate(1);

				b = alg.and(b, r.isEqualTo(s2.asRelation()));

				return b;
			}
		};

		List<Tensor<Boolean>> sol = problem.solveOne(solver);
		if (sol == null)
			System.out.println("no solution");
		else {
			Operation<Boolean> p1 = Operation.wrap(sol.get(0));
			Operation<Boolean> p2 = Operation.wrap(sol.get(1));
			Operation<Boolean> p3 = Operation.wrap(sol.get(2));

			System.out.println("has solution:");
			System.out.println(Operation.formatTable(p1));
			System.out.println(Operation.formatTable(p2));
			System.out.println(Operation.formatTable(p3));
		}
	}

	private static DecimalFormat TIME_FORMAT = new DecimalFormat("0.00");

	public static void main(String[] args) {
		long time = System.currentTimeMillis();
		Quasigroup test = new Quasigroup();

		int size = 15;
		Operation<Boolean> q1 = test.generateLatinSquare(size);
		System.out.println(Operation.formatTable(q1));
		Operation<Boolean> q2 = Operation.moduloAdd(size);
		System.out.println(Operation.formatTable(q2));
		test.checkEquivalence(q1, q2);

		time = System.currentTimeMillis() - time;
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time)
				+ " seconds.");
	}
}
