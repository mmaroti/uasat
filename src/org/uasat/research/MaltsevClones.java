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

	public void analyze(Algebra<Boolean> alg) {
		Algebra.print(alg);

		GeneratedOps gen = new GeneratedOps(alg.getSize(), 1);
		gen.addProjections();
		gen.addCompositions(alg);
		gen.print();

		// gen = new GeneratedOps(alg.getSize(), 2);
		// gen.addProjections();
		// gen.addCompositions(alg);
		// gen.print();

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

	public static void main2(String[] args) {
		SatSolver.setDefault("jni-cominisatps");
		long time = System.currentTimeMillis();
		MaltsevClones test = new MaltsevClones();

		// test.findMaltsevAlgebras(3);
		// test.analyze(new Algebra<Boolean>(BoolAlgebra.INSTANCE, Operation
		// .parse(3, 3, "012 202 120 111 012 001 212 222 012")));

		// test.analyze(LOOP_5E);
		// test.analyze(BULIN_LOOP);
		// test.analyze(new Algebra<Boolean>(BoolAlgebra.INSTANCE,
		// Operation.parse(6, 2,
		// "012345 154203 230514 321450 405132 543021")));
		test.analyze(MALTSEV33E);

		time = System.currentTimeMillis() - time;
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time) + " seconds.");
	}

	static final Algebra<Boolean> MALTSEV32A = Algebra.wrap(Operation
		.parse(3, 3, "012 102 220 102 012 221 220 221 012"));

	static final Algebra<Boolean> MALTSEV32B = Algebra.wrap(Operation
		.parse(3, 3, "012 100 200 100 012 021 200 021 012"));

	static final Algebra<Boolean> MALTSEV32C = Algebra.wrap(Operation
		.parse(3, 3, "012 101 210 101 012 121 210 121 012"));

	static final Algebra<Boolean> MALTSEV32D = Algebra.wrap(Operation
		.parse(3, 3, "012 001 020 110 012 211 202 122 012"));

	static final Algebra<Boolean> MALTSEV33A = MALTSEV32A;

	static final Algebra<Boolean> MALTSEV33B = MALTSEV32B;

	static final Algebra<Boolean> MALTSEV33C = MALTSEV32C;

	static final Algebra<Boolean> MALTSEV33D = Algebra.wrap(Operation
		.parse(3, 3, "012 201 120 120 012 201 201 120 012"));

	static final Algebra<Boolean> MALTSEV33E = Algebra.wrap(Operation
		.parse(3, 3, "012 101 220 100 012 221 200 121 012"));

	static final Algebra<Boolean> MALTSEV43A = Algebra.wrap(Operation.parse(4, 3,
		"0123 1010 2103 3030 1010 0123 1212 0321 2103 1212 0123 3232 3030 0321 3232 0123"));

	static final Algebra<Boolean> MALTSEV43B = Algebra.wrap(Operation.parse(4, 3,
		"0123 1032 2301 3210 1032 0123 3210 2301 2301 3210 0123 1032 3210 2301 1032 0123"));

	static final Algebra<Boolean> MALTSEV43C = Algebra.wrap(Operation.parse(4, 3,
		"0123 1010 2202 3030 1000 0123 2213 0331 2000 1213 0123 3332 3000 0331 2332 0123"));

	static final Algebra<Boolean> MALTSEV43D = Algebra.wrap(Operation.parse(4, 3,
		"0123 1011 2100 3100 1011 0123 1211 1311 2100 1211 0123 0132 3100 1311 0132 0123"));

	static final Algebra<Boolean> MALTSEV43E = Algebra.wrap(Operation.parse(4, 3,
		"0123 1023 2200 3300 1023 0123 2213 3331 2200 2213 0123 0332 3300 3331 0332 0123"));

	static final Algebra<Boolean> MALTSEV43F = Algebra.wrap(Operation.parse(4, 3,
		"0123 1000 2002 3020 1000 0123 0212 0321 2002 0212 0123 2232 3020 0321 2232 0123"));

	static final Algebra<Boolean> MALTSEV43G = Algebra.wrap(Operation.parse(4, 3,
		"0123 1003 2003 3330 1003 0123 0213 3331 2003 0213 0123 3332 3330 3331 3332 0123"));

	static final Algebra<Boolean> MALTSEV43H = Algebra.wrap(Operation.parse(4, 3,
		"0123 1001 2000 3100 1001 0123 0211 1311 2000 0211 0123 0132 3100 1311 0132 0123"));

	static final Algebra<Boolean> MALTSEV43I = Algebra.wrap(Operation.parse(4, 3,
		"0123 1020 2202 3020 1020 0123 2212 0321 2202 2212 0123 2232 3020 0321 2232 0123"));

	static final Algebra<Boolean> MALTSEV43J = Algebra.wrap(Operation.parse(4, 3,
		"0123 1021 2202 3120 1021 0123 2211 1311 2202 2211 0123 2132 3120 1311 2132 0123"));

	static final Algebra<Boolean> MALTSEV43K = Algebra.wrap(Operation.parse(4, 3,
		"0123 1023 2200 3300 1023 0123 2211 3311 2200 2211 0123 0132 3300 3311 0132 0123"));

	static final Algebra<Boolean> MALTSEV43L = Algebra.wrap(Operation.parse(4, 3,
		"0123 2011 1200 1200 1200 0123 2011 2011 2011 1200 0123 0132 3011 1300 0132 0123"));

	static final Algebra<Boolean> MALTSEV43M = Algebra.wrap(Operation.parse(4, 3,
		"0123 1023 2202 3320 1023 0123 2213 3331 2202 2213 0123 2332 3320 3331 2332 0123"));

	static final Algebra<Boolean> MALTSEV43N = Algebra.wrap(Operation.parse(4, 3,
		"0123 1001 2002 3120 1001 0123 0213 1331 2002 0213 0123 2332 3120 1331 2332 0123"));

	static final Algebra<Boolean> MALTSEV43O = Algebra.wrap(Operation.parse(4, 3,
		"0123 1000 2000 3000 1000 0123 0212 0321 2000 0212 0123 0232 3000 0321 0232 0123"));

	static final Algebra<Boolean> MALTSEV43P = Algebra.wrap(Operation.parse(4, 3,
		"0123 1020 2203 3030 1020 0123 2213 0331 2203 2213 0123 3332 3030 0331 3332 0123"));

	static final Algebra<Boolean> MALTSEV43Q = Algebra.wrap(Operation.parse(4, 3,
		"0123 1023 3302 2230 1023 0123 3312 2231 2231 2231 0123 3312 3312 3312 2231 0123"));

	static final Algebra<Boolean> MALTSEV43R = Algebra.wrap(Operation.parse(4, 3,
		"0123 1000 2000 3000 1000 0123 0213 0331 2000 0213 0123 0332 3000 0331 0332 0123"));

	static final Algebra<Boolean> MALTSEV43S = Algebra.wrap(Operation.parse(4, 3,
		"0123 2012 1201 3120 1201 0123 2012 1231 2012 1201 0123 2312 3120 2312 1231 0123"));

	static final Algebra<Boolean> MALTSEV43T = Algebra.wrap(Operation.parse(4, 3,
		"0123 1023 2202 3320 1023 0123 2212 3321 2202 2212 0123 2232 3320 3321 2232 0123"));

	static final Algebra<Boolean> MALTSEV43U = Algebra.wrap(Operation.parse(4, 3,
		"0123 3001 3001 1330 1330 0123 0213 3001 2330 0213 0123 3002 3001 1330 1330 0123"));

	static final Algebra<Boolean> MALTSEV43V = Algebra.wrap(Operation.parse(4, 3,
		"0123 3031 2103 1310 1310 0123 1310 3031 2103 3231 0123 1312 3031 1310 3031 0123"));

	static final Algebra<Boolean> MALTSEV43W = Algebra.wrap(Operation.parse(4, 3,
		"0123 1011 2102 3120 1011 0123 1211 1311 2102 1211 0123 2132 3120 1311 2132 0123"));

	static final Algebra<Boolean> MALTSEV43X = Algebra.wrap(Operation.parse(4, 3,
		"0123 1010 2100 3000 1010 0123 1212 0321 2100 1212 0123 0232 3000 0321 0232 0123"));

	static final Algebra<Boolean> MALTSEV43Y = Algebra.wrap(Operation.parse(4, 3,
		"0123 1011 2103 3130 1011 0123 1211 1311 2103 1211 0123 3132 3130 1311 3132 0123"));

	public static void main3(String[] args) {
		// SatSolver.setDefault("jni-cominisatps");
		long time = System.currentTimeMillis();

		MinimalClones clones = new MinimalClones("maltsev", 4);
		clones.trace = true;
		// clones.addUpperLimit(Relation.parse(3, "20 01 11"));
		// clones.addUpperLimit(Relation.parse(3, "20 01 02"));
		// clones.addUpperLimit(Relation.parse(3, "20 01 22"));
		// clones.addUpperLimit(Relation.parse(3, "20 01 12"));

		// clones.addUpperLimit(Relation.parse(3, "010 001"));
		// clones.addUpperLimit(Relation.parse(3,
		// "200 110 020 101 011 221 002 212 122"));

		clones.findAll(3);
		clones.print();
		clones.printWitnesses(1, 3);

		time = System.currentTimeMillis() - time;
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time) + " seconds.");
	}

	public static void main(String[] args) {
		XmlWriter.writeAlgebra(MALTSEV43Y, "maltsev43y", "m");
	}
}
