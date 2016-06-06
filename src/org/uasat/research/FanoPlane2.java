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

import java.text.DecimalFormat;
import java.util.*;

import org.uasat.core.*;

public class FanoPlane2 {
	public static final SatSolver<?> SOLVER = SatSolver.getDefault();

	private static final Tensor<Boolean> GEN2 = Tensor.matrix(2, 3,
			Arrays.asList(true, false, false, true, true, true));

	public static <BOOL> Tensor<BOOL> generate2(BoolAlgebra<BOOL> alg,
			Tensor<BOOL> tensor) {
		assert tensor.getOrder() == 2 && tensor.getDim(0) == 7
				&& tensor.getDim(1) == 2;

		Contract<BOOL> c = Contract.linear(alg);
		c.add(tensor, "ij");
		c.add(alg.lift(GEN2), "jk");
		return c.get("ik");
	}

	public static <BOOL> BOOL isMinimal2(BoolAlgebra<BOOL> alg,
			Tensor<BOOL> tensor) {
		assert tensor.getOrder() == 2 && tensor.getDim(0) == 7
				&& tensor.getDim(1) == 3;

		List<Tensor<BOOL>> ts = Tensor.unstack(tensor);
		BOOL b = Tensor.fold(alg.ANY, 1, ts.get(0)).get();
		b = alg.and(b, alg.lexLess(ts.get(0), ts.get(1)));
		b = alg.and(b, alg.lexLess(ts.get(1), ts.get(2)));

		return b;
	}

	public static Tensor<Boolean> subspaces2() {
		SatProblem problem = new SatProblem(new int[] { 7, 2 }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Tensor<BOOL> tensor = tensors.get(0);
				tensor = generate2(alg, tensor);
				return isMinimal2(alg, tensor);
			}
		};

		Tensor<Boolean> ss = problem.solveAll(SOLVER).get(0);
		System.out.println("subspaces of dim 2: " + ss.getLastDim());

		return ss;
	}

	private static final Tensor<Boolean> GEN3 = Tensor.matrix(3, 7, Arrays
			.asList(true, false, false, false, true, false, false, false, true,
					true, true, false, true, false, true, false, true, true,
					true, true, true));

	public static <BOOL> Tensor<BOOL> generate3(BoolAlgebra<BOOL> alg,
			Tensor<BOOL> tensor) {
		assert tensor.getOrder() == 2 && tensor.getDim(0) == 7
				&& tensor.getDim(1) == 3;

		Contract<BOOL> c = Contract.linear(alg);
		c.add(tensor, "ij");
		c.add(alg.lift(GEN3), "jk");
		return c.get("ik");
	}

	public static <BOOL> BOOL isMinimal3(BoolAlgebra<BOOL> alg,
			Tensor<BOOL> tensor) {
		assert tensor.getOrder() == 2 && tensor.getDim(0) == 7
				&& tensor.getDim(1) == 7;

		List<Tensor<BOOL>> ts = Tensor.unstack(tensor);
		BOOL b = Tensor.fold(alg.ANY, 1, ts.get(0)).get();
		b = alg.and(b, alg.lexLess(ts.get(0), ts.get(1)));
		b = alg.and(b, alg.lexLess(ts.get(1), ts.get(2)));
		b = alg.and(b, alg.lexLess(ts.get(1), ts.get(3)));
		b = alg.and(b, alg.lexLess(ts.get(2), ts.get(4)));
		b = alg.and(b, alg.lexLess(ts.get(2), ts.get(5)));
		b = alg.and(b, alg.lexLess(ts.get(2), ts.get(6)));

		return b;
	}

	public static Tensor<Boolean> subspaces3() {
		SatProblem problem = new SatProblem(new int[] { 7, 3 }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Tensor<BOOL> tensor = tensors.get(0);
				tensor = generate3(alg, tensor);
				return isMinimal3(alg, tensor);
			}
		};

		Tensor<Boolean> ss = problem.solveAll(SOLVER).get(0);
		System.out.println("subspaces of dim 3: " + ss.getLastDim());

		return ss;
	}

	private static DecimalFormat TIME_FORMAT = new DecimalFormat("0.00");

	public static void main(String[] args) {
		long time = System.currentTimeMillis();

		subspaces2();
		subspaces3();

		time = System.currentTimeMillis() - time;
		System.out.println("Total variables: " + SOLVER.getTotalVariables()
				+ ", clauses: " + SOLVER.getTotalClauses());
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time)
				+ " seconds.");
	}
}
