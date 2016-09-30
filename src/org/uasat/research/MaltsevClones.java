/**
 * Copyright (C) Miklos Maroti, 2015-2016
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
import java.util.List;

import org.uasat.core.*;
import org.uasat.math.*;

public class MaltsevClones {
	private static DecimalFormat TIME_FORMAT = new DecimalFormat("0.00");

	static final Algebra<Boolean> BULIN_LOOP = new Algebra<Boolean>(BoolAlgebra.INSTANCE, Operation.parse(6, 2,
		"012345 103254 234501 325410 451023 540132"));

	public void findCriticalRels(Algebra<Boolean> alg) {
		Algebra.print(alg);

		CompatibleRels comp = new CompatibleRels(alg);
		comp.printAllRels(1);
		comp.printCriticalRels(1);
		comp.printEquivalences();
		comp.printCriticalRels(2);
		comp.printUniCriticalRels(2);
		comp.printUniCriticalRels(3);
		comp.printUniCriticalRels(4);
		// comp.printUniCriticalRels(5);
	}

	public void findMaltsevAlgebras(int size) {
		final List<Permutation<Boolean>> perms = Permutation.nontrivialPerms(size);

		SatProblem problem = new SatProblem(new int[] { size, size, size, size }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg, List<Tensor<BOOL>> tensors) {
				Operation<BOOL> op = new Operation<BOOL>(alg, tensors.get(0));

				BOOL b = op.isOperation();
				b = alg.and(b, op.isMaltsev());

				for (Permutation<Boolean> p : perms) {
					Permutation<BOOL> perm = Permutation.lift(alg, p);
					b = alg.and(b, op.isLexLeq(op.conjugate(perm)));
				}

				return b;
			}
		};

		List<Tensor<Boolean>> list = Tensor.unstack(problem.solveAll(SatSolver.getDefault()).get(0));
		for (Tensor<Boolean> tensor : list) {
			Operation<Boolean> op = Operation.wrap(tensor);
			System.out.println(Operation.format(op));
		}
	}

	static final Algebra<Boolean> LOOP_2 = new Algebra<Boolean>(BoolAlgebra.INSTANCE, Operation.parse(2, 2, "01 10"));
	static final Algebra<Boolean> LOOP_3 = new Algebra<Boolean>(BoolAlgebra.INSTANCE, Operation.parse(3, 2,
		"012 120 201"));
	static final Algebra<Boolean> LOOP_4A = new Algebra<Boolean>(BoolAlgebra.INSTANCE, Operation.parse(4, 2,
		"0123 1302 2031 3210"));
	static final Algebra<Boolean> LOOP_4B = new Algebra<Boolean>(BoolAlgebra.INSTANCE, Operation.parse(4, 2,
		"0123 1032 2301 3210"));
	static final Algebra<Boolean> LOOP_5A = new Algebra<Boolean>(BoolAlgebra.INSTANCE, Operation.parse(5, 2,
		"01234 14302 20413 32041 43120"));
	static final Algebra<Boolean> LOOP_5B = new Algebra<Boolean>(BoolAlgebra.INSTANCE, Operation.parse(5, 2,
		"01234 14302 23041 32410 40123"));
	static final Algebra<Boolean> LOOP_5C = new Algebra<Boolean>(BoolAlgebra.INSTANCE, Operation.parse(5, 2,
		"01234 14302 23140 30421 42013"));
	static final Algebra<Boolean> LOOP_5D = new Algebra<Boolean>(BoolAlgebra.INSTANCE, Operation.parse(5, 2,
		"01234 14320 23401 30142 42013"));
	static final Algebra<Boolean> LOOP_5E = new Algebra<Boolean>(BoolAlgebra.INSTANCE, Operation.parse(5, 2,
		"01234 14320 23041 30412 42103"));
	static final Algebra<Boolean> LOOP_5F = new Algebra<Boolean>(BoolAlgebra.INSTANCE, Operation.parse(5, 2,
		"01234 10423 23041 34102 42310"));

	public void findLoops(final int size) {
		final List<Permutation<Boolean>> perms = Permutation.nontrivialPerms(size);
		for (int i = perms.size() - 1; i >= 0; i--) {
			if (!perms.get(i).hasValue(0, 0)) {
				perms.remove(i);
			}
		}

		SatProblem problem = new SatProblem(new int[] { size, size, size }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg, List<Tensor<BOOL>> tensors) {
				Operation<BOOL> op = new Operation<BOOL>(alg, tensors.get(0));

				BOOL b = op.isOperation();
				b = alg.and(b, op.asRelation().rotate(1).isOperation());
				b = alg.and(b, op.asRelation().rotate(2).isOperation());

				for (int i = 0; i < size; i++) {
					b = alg.and(b, op.hasValue(i, i, 0));
					b = alg.and(b, op.hasValue(i, 0, i));
				}

				for (Permutation<Boolean> p : perms) {
					Permutation<BOOL> perm = Permutation.lift(alg, p);
					b = alg.and(b, op.isLexLeq(op.conjugate(perm)));
				}

				return b;
			}
		};

		List<Tensor<Boolean>> list = Tensor.unstack(problem.solveAll(SatSolver.getDefault()).get(0));
		for (Tensor<Boolean> tensor : list) {
			Operation<Boolean> op = Operation.wrap(tensor);
			System.out.println(Operation.format(op));
		}
	}

	public static void main(String[] args) {
		SatSolver.setDefault("jni-cominisatps");
		long time = System.currentTimeMillis();
		MaltsevClones test = new MaltsevClones();

		// test.findLoops(6);
		// test.findCriticalRels(LOOP_5E);
		test.findCriticalRels(BULIN_LOOP);
//		test.findCriticalRels(new Algebra<Boolean>(BoolAlgebra.INSTANCE, Operation.parse(6, 2,
//			"012345 154203 230514 321450 405132 543021")));

		time = System.currentTimeMillis() - time;
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time) + " seconds.");
	}
}
