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

public class MeetSD {
	private static DecimalFormat TIME_FORMAT = new DecimalFormat("0.00");

	public void findBigDaddy(final int size) {
		System.out.println("finding big daddy " + size);

		SatProblem prob = new SatProblem(new int[] { size, size, size, size }, new int[] { size, size, size, size },
			new int[] { size, size, size }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg, List<Tensor<BOOL>> tensors) {
				Operation<BOOL> op1 = new Operation<BOOL>(alg, tensors.get(0));
				Operation<BOOL> op2 = new Operation<BOOL>(alg, tensors.get(1));
				Relation<BOOL> rel = new Relation<BOOL>(alg, tensors.get(2));

				BOOL b = op1.isOperation();
				b = alg.and(b, op2.isOperation());
				b = alg.and(b, Operation.areJovanovicTerms2(op1, op2));
				b = alg.and(b, op1.preserves(rel));
				b = alg.and(b, op2.preserves(rel));

				b = alg.and(b, rel.getValue(0, 1, 0));
				b = alg.and(b, rel.getValue(0, 2, 2));
				b = alg.and(b, rel.getValue(1, 1, 2));
				b = alg.and(b, rel.getValue(2, 0, 1));

				b = alg.and(b, rel.diagonal().isEmpty());

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

	static final Algebra<Boolean> BIG_DADDY_6 = new Algebra<Boolean>(BoolAlgebra.INSTANCE, Operation.parse(6, 3,
		"000000 043345 043345 003303 040044 043345 " + "044044 415545 355355 355355 444444 555555 "
			+ "033303 455545 352355 333333 455545 555555 " + "033303 053345 353355 333333 053345 353355 "
			+ "044044 445545 045345 045345 444444 445545 " + "055345 455545 355355 355355 455545 555555"),
		Operation.parse(6, 3, "043345 045345 053345 053345 045345 055345 "
			+ "043345 015345 055345 053345 045345 055345 " + "043345 055345 052345 053345 045345 055345 "
			+ "043345 055345 053345 053345 045345 055345 " + "043345 045345 055345 053345 045345 055345 "
			+ "043345 055345 055345 053345 045345 055345"));

	public void findLittleSis(final int size) {
		System.out.println("finding little sister " + size);

		SatProblem prob = new SatProblem(new int[] { size, size, size, size }, new int[] { size, size, size, size },
			new int[] { size, size, size }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg, List<Tensor<BOOL>> tensors) {
				Operation<BOOL> op1 = new Operation<BOOL>(alg, tensors.get(0));
				Operation<BOOL> op2 = new Operation<BOOL>(alg, tensors.get(1));
				Relation<BOOL> rel = new Relation<BOOL>(alg, tensors.get(2));

				BOOL b = op1.isOperation();
				b = alg.and(b, op2.isOperation());
				b = alg.and(b, Operation.areJovanovicTerms2(op1, op2));
				b = alg.and(b, op1.preserves(rel));
				b = alg.and(b, op2.preserves(rel));

				b = alg.and(b, rel.getValue(0, 1, 1));
				b = alg.and(b, rel.getValue(0, 0, 2));
				b = alg.and(b, rel.getValue(1, 2, 0));
				b = alg.and(b, rel.getValue(2, 0, 1));

				b = alg.and(b, rel.diagonal().isEmpty());

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

	public static void main(String[] args) {
		SatSolver.setDefault("jni-cominisatps");
		long time = System.currentTimeMillis();
		MeetSD test = new MeetSD();

		test.findBigDaddy(6);
		test.findLittleSis(6);

		time = System.currentTimeMillis() - time;
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time) + " seconds.");
	}
}
