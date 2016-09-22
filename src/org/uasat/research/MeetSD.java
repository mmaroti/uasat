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
		
		SatProblem prob = new SatProblem(new int[] { size, size, size, size },
				new int[] { size, size, size, size }, new int[] { size, size,
						size }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
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

				for (int i = 0; i < size; i++)
					b = alg.and(b, alg.not(rel.getValue(i, i, i)));

				return b;
			}
		};

		List<Tensor<Boolean>> tensors = prob.solveOne(SatSolver.getDefault());
		if (tensors == null) {
			System.out.println("no solution");
			return;
		}

		Operation<Boolean> op = Operation.wrap(tensors.get(0));
		Relation<Boolean> rel = Relation.wrap(tensors.get(1));

		System.out.println(Operation.format(op));
		System.out.println(Relation.format(rel));
	}

	public void findLittleSis(final int size) {
		System.out.println("finding little sister " + size);
		
		SatProblem prob = new SatProblem(new int[] { size, size, size, size },
				new int[] { size, size, size, size }, new int[] { size, size,
						size }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
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

				for (int i = 0; i < size; i++)
					b = alg.and(b, alg.not(rel.getValue(i, i, i)));

				return b;
			}
		};

		List<Tensor<Boolean>> tensors = prob.solveOne(SatSolver.getDefault());
		if (tensors == null) {
			System.out.println("no solution");
			return;
		}

		Operation<Boolean> op = Operation.wrap(tensors.get(0));
		Relation<Boolean> rel = Relation.wrap(tensors.get(1));

		System.out.println(Operation.format(op));
		System.out.println(Relation.format(rel));
	}

	public static void main(String[] args) {
		SatSolver.setDefault("jni-cominisatps");
		long time = System.currentTimeMillis();
		MeetSD test = new MeetSD();

		test.findBigDaddy(5);
		test.findLittleSis(5);

		time = System.currentTimeMillis() - time;
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time)
				+ " seconds.");
	}
}
