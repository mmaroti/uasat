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

public class LocalCond {
	private static DecimalFormat TIME_FORMAT = new DecimalFormat("0.00");

	public void findCommImpliesWeakNU4(final int size) {
		SatProblem prob = new SatProblem(new int[] { size, size, size },
				new int[] { size, size, size, size }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Operation<BOOL> op = new Operation<BOOL>(alg, tensors.get(0));
				Relation<BOOL> rel = new Relation<BOOL>(alg, tensors.get(1));

				BOOL b = op.isOperation();
				b = alg.and(b, op.isIdempotent());
				b = alg.and(b, op.preserves(rel));

				for (int i = 0; i < size; i++)
					b = alg.and(b,
							alg.equ(op.hasValue(i, 0, 1), op.hasValue(i, 1, 0)));

				b = alg.and(b, rel.getValue(1, 0, 0, 0));
				b = alg.and(b, rel.getValue(0, 1, 0, 0));
				b = alg.and(b, rel.getValue(0, 0, 1, 0));
				b = alg.and(b, rel.getValue(0, 0, 0, 1));

				for (int i = 0; i < size; i++)
					b = alg.and(b, alg.not(rel.getValue(i, i, i, i)));

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
		long time = System.currentTimeMillis();
		LocalCond test = new LocalCond();

		test.findCommImpliesWeakNU4(6);

		time = System.currentTimeMillis() - time;
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time)
				+ " seconds.");
	}
}
