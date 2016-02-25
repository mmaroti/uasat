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

package org.uasat.research;

import java.text.*;
import java.util.*;

import org.uasat.core.*;
import org.uasat.math.*;

public class TestSubpowers {
	private static DecimalFormat TIME_FORMAT = new DecimalFormat("0.00");

	public static Operation<Boolean> ZERO = Operation.parse(2, 0, "0");
	public static Operation<Boolean> ONE = Operation.parse(2, 0, "1");
	public static Operation<Boolean> NOT = Operation.parse(2, 1, "10");
	public static Operation<Boolean> MEET = Operation.parse(2, 2, "00 01");
	public static Operation<Boolean> JOIN = Operation.parse(2, 2, "01 11");
	public static Operation<Boolean> ADD = Operation.parse(2, 2, "01 10");
	public static Operation<Boolean> EQU = Operation.parse(2, 2, "10 01");
	public static Operation<Boolean> IMPL = Operation.parse(2, 2, "11 01");
	public static Operation<Boolean> MAJOR = Operation.parse(2, 3,
			"00 01 01 11");
	public static Operation<Boolean> MINOR = Operation.parse(2, 3,
			"01 10 10 01");

	public static void main(String[] args) {
		long time = System.currentTimeMillis();

		Algebra<Boolean> alg = Algebra.wrap(2);
		CompatibleRels subs = new CompatibleRels(alg);

		subs.printUniqueCriticalRelsCompement(1);
		subs.printUniqueCriticalRelsCompement(2);
		subs.printUniqueCriticalRelsCompement(3);
		subs.printUniqueCriticalRelsCompement(4);
		// subs.printCriticalRels(5);

		time = System.currentTimeMillis() - time;
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time)
				+ " seconds.");
	}

	public void findStrangeRelation(int size, int arity) {
		final Relation<Boolean> res = Relation.singleton(size, 0).cartesian(
				Relation.full(size, arity - 1));

		System.out.println(Relation.format(res));

		SatProblem prob = new SatProblem(new int[] { size, size, size, size },
				new int[] { size, size, size, size }, Util.createShape(size,
						arity)) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {

				Operation<BOOL> op1 = new Operation<BOOL>(alg, tensors.get(0));
				Operation<BOOL> op2 = new Operation<BOOL>(alg, tensors.get(1));
				Relation<BOOL> rel = new Relation<BOOL>(alg, tensors.get(2));

				BOOL b = op1.isOperation();
				b = alg.and(b, op2.isOperation());
				b = alg.and(b, op1.preserves(rel));
				b = alg.and(b, op2.preserves(rel));
				b = alg.and(b, alg.not(rel.isEssential()));

				Relation<BOOL> rel2 = rel.intersect(Relation.lift(alg, res));
				b = alg.and(b, rel2.isEssential());

				return b;
			}
		};

		List<Tensor<Boolean>> tensors = prob.solveOne(SatSolver.getDefault());
		if (tensors == null) {
			System.out.println("no solution");
			return;
		}

		Operation<Boolean> op1 = Operation.wrap(tensors.get(0));
		Operation<Boolean> op2 = Operation.wrap(tensors.get(1));
		Relation<Boolean> rel = Relation.wrap(tensors.get(2));

		System.out.println(Operation.format(op1));
		System.out.println(Operation.format(op2));
		System.out.println(Relation.format(rel));
	}

	public static void main2(String[] args) {
		long time = System.currentTimeMillis();
		TestSubpowers test = new TestSubpowers();

		test.findStrangeRelation(2, 3);

		time = System.currentTimeMillis() - time;
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time)
				+ " seconds.");
	}
}
