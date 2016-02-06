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

public class MonoidalInt {
	public static GeneratedOps parseMonoid(int size, String monoid) {
		GeneratedOps ops = new GeneratedOps(size, 1);

		for (String m : monoid.split(" ")) {
			if (m.isEmpty())
				continue;

			ops.add(Operation.parseTable(size, 1, m));
		}

		assert ops.isSelfClosed();
		return ops;
	}

	public static <ELEM> Tensor<ELEM> getCompatibility(
			final BoolAlgebra<ELEM> alg, final List<Operation<ELEM>> ops,
			final List<Relation<ELEM>> rels) {
		return Tensor.generate(ops.size(), rels.size(),
				new Func2<ELEM, Integer, Integer>() {
					@Override
					public ELEM call(Integer a, Integer b) {
						return ops.get(a).preserves(rels.get(b));
					}
				});
	}

	public static void printMatrix(String what, Tensor<Boolean> rel) {
		assert rel.getOrder() == 2;

		System.out.println(what + ":");
		for (int j = 0; j < rel.getDim(1); j++) {
			for (int i = 0; i < rel.getDim(0); i++)
				System.out.print(rel.getElem(i, j) ? "1" : "0");
			System.out.println();
		}
	}

	public static <ELEM> ELEM isClosedSubset(BoolAlgebra<ELEM> alg,
			Tensor<ELEM> subset, Tensor<ELEM> galois) {
		Tensor<ELEM> t;

		t = Tensor.reduce(alg.ALL, "y", alg.LEQ, subset.named("x"),
				galois.named("xy"));
		t = Tensor.reduce(alg.ALL, "x", alg.LEQ, t.named("y"),
				galois.named("xy"));
		t = Tensor.map2(alg.EQU, subset, t);
		t = Tensor.fold(alg.ALL, 1, t);

		return t.get();
	}

	public static <ELEM> Tensor<Boolean> getClosedSubsets(
			SatSolver<ELEM> solver, final Tensor<Boolean> galois) {
		SatProblem prob = new SatProblem(new int[] { galois.getDim(0) }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Tensor<BOOL> sub = tensors.get(0);
				Tensor<BOOL> rel = Tensor.map(alg.LIFT, galois);
				return isClosedSubset(alg, sub, rel);
			}
		};

		return prob.solveAll(solver, LIMIT).get(0);
	}

	public static <ELEM> Tensor<ELEM> transpose(Tensor<ELEM> matrix) {
		assert matrix.getOrder() == 2;
		return Tensor.reshape(matrix,
				new int[] { matrix.getDim(1), matrix.getDim(0) }, new int[] {
						1, 0 });
	}

	public static Tensor<Boolean> sort(Tensor<Boolean> tensor) {
		List<Tensor<Boolean>> list = Tensor.unstack(tensor);

		Collections.sort(list, new Comparator<Tensor<Boolean>>() {
			@Override
			public int compare(Tensor<Boolean> arg0, Tensor<Boolean> arg1) {
				Iterator<Boolean> iter0 = arg0.iterator();
				Iterator<Boolean> iter1 = arg1.iterator();

				while (iter0.hasNext()) {
					if (!iter1.hasNext())
						return 1;

					boolean b0 = iter0.next();
					boolean b1 = iter1.next();
					if (b0 && !b1)
						return 1;
					else if (!b0 && b1)
						return -1;
				}

				return iter1.hasNext() ? -1 : 0;
			}
		});

		int[] shape = new int[tensor.getOrder() - 1];
		System.arraycopy(tensor.getShape(), 0, shape, 0, shape.length);
		return Tensor.stack(shape, list);
	}

	protected static DecimalFormat TIME_FORMAT = new DecimalFormat("0.00");

	public static void printClones(String what, List<Operation<Boolean>> ops,
			List<Relation<Boolean>> rels) {
		if (ops.size() < LIMIT && rels.size() < LIMIT) {
			Tensor<Boolean> compat = getCompatibility(BoolAlgebra.INSTANCE,
					ops, rels);
			Tensor<Boolean> closed = getClosedSubsets(SatSolver.getDefault(),
					compat);

			System.out.println("clones (" + what + "): " + closed.getDim(1));
			if (closed.getDim(0) <= PRINT_LIMIT
					&& closed.getDim(1) <= PRINT_LIMIT)
				printMatrix("closed op subsets", sort(closed));
		}
	}

	public static void printRels(String what, Collection<Relation<Boolean>> rels) {
		System.out.println(what + (rels.size() < LIMIT ? ": " : ": >= ")
				+ rels.size());

		if (rels.size() <= PRINT_LIMIT) {
			int c = 0;
			for (Relation<Boolean> rel : rels)
				System.out.println((c++) + ":\t" + Relation.formatMembers(rel));
		}
	}

	public static void printOps(String what, Collection<Operation<Boolean>> ops) {
		System.out.println(what + (ops.size() < LIMIT ? ": " : ": >= ")
				+ ops.size());

		if (ops.size() <= PRINT_LIMIT) {
			int c = 0;
			for (Operation<Boolean> op : ops)
				System.out.println((c++) + ":\t" + Operation.formatTable(op));
		}
	}

	public static void printStatistics(int size, String monoid) {
		SatSolver<?> solver = SatSolver.getDefault();
		solver.debugging = false;

		GeneratedOps ops = parseMonoid(size, monoid);
		CompatibleRels crel = new CompatibleRels(Algebra.wrap(ops
				.getOperations()));

		System.out.println("monoid: " + monoid);

		List<Relation<Boolean>> unaryRels = crel.findUniqueRels(1, LIMIT);
		printRels("unique unary rels", unaryRels);
		printRels("unique critical unary rels", crel.findUniqueCriticalRels(1));

		List<Relation<Boolean>> binaryRels = crel.findUniqueRels(2, LIMIT);
		printRels("unique binary rels", binaryRels);
		printRels("unique critical binary rels", crel.findUniqueCriticalRels(2));

		List<Relation<Boolean>> ternaryRels = crel.findUniqueRels(3, LIMIT);
		printRels("unique ternary rels", ternaryRels);
		printRels("unique critical ternary rels",
				crel.findUniqueCriticalRels(3));

		// printRels("unique critical quaternary rels",
		// crel.findUniqueCriticalRels(4));

		CompatibleOps cops = new CompatibleOps(
				Structure.trivial(ops.getSize()), ops);

		List<Operation<Boolean>> binaryOps = cops
				.findBinaryOps("unique", LIMIT);
		printOps("unique binary ops", binaryOps);

		List<Operation<Boolean>> ternaryOps = cops.findTernaryOps("unique",
				LIMIT);
		printOps("unique ternary ops", ternaryOps);

		long time = System.currentTimeMillis();

		printClones("op 2 rel 2", binaryOps, binaryRels);
		printClones("op 2 rel 3", binaryOps, ternaryRels);
		printClones("op 3 rel 2", ternaryOps, binaryRels);
		printClones("op 3 rel 3", ternaryOps, ternaryRels);

		time = System.currentTimeMillis() - time;
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time)
				+ " seconds.");
		System.out.println();
	}

	public static String[] TWO_MONOIDS = new String[] { "01", "01 00", "01 10",
			"01 00 11", "01 10 00 11" };

	public static String[] INFINITE_MONOIDS = new String[] { "012", "000 012",
			"002 012", "000 001 012", "000 002 012", "000 011 012",
			"000 012 021", "002 012 022", "000 001 002 012", "000 001 010 012",
			"000 001 011 012", "000 001 012 111", "000 001 002 010 012",
			"000 001 002 011 012", "000 001 002 012 111",
			"000 001 010 011 012", "000 001 010 012 111",
			"000 001 011 012 111", "000 001 012 110 111",
			"000 001 012 111 112", "000 001 012 111 222",
			"000 001 002 010 011 012", "000 001 002 010 012 111",
			"000 001 002 011 012 022", "000 001 002 011 012 111",
			"000 001 002 012 110 111", "000 001 002 012 111 112",
			"000 001 002 012 111 222", "000 001 010 011 012 111",
			"000 001 010 012 110 111", "000 001 010 012 111 222",
			"000 001 011 012 111 112", "000 001 011 012 111 222",
			"000 001 012 102 110 111", "000 001 012 110 111 222",
			"000 001 012 111 112 222", "000 002 010 012 101 111",
			"000 001 002 010 011 012 111", "000 001 002 010 012 110 111",
			"000 001 002 010 012 111 222", "000 001 002 011 012 110 111",
			"000 001 002 011 012 111 112", "000 001 002 011 012 111 222",
			"000 001 002 012 110 111 112", "000 001 002 012 110 111 222",
			"000 001 002 012 111 112 222", "000 001 010 011 012 110 111",
			"000 001 010 011 012 111 222", "000 001 010 012 101 110 111",
			"000 001 010 012 110 111 222", "000 001 011 012 111 112 222",
			"000 001 012 102 110 111 222", "000 001 002 010 011 012 110 111",
			"000 001 002 010 011 012 111 222",
			"000 001 002 010 012 101 110 111",
			"000 001 002 010 012 110 111 112",
			"000 001 002 010 012 110 111 222",
			"000 001 002 011 012 100 110 111",
			"000 001 002 011 012 110 111 222",
			"000 001 002 011 012 111 112 222",
			"000 001 002 012 102 110 111 112",
			"000 001 002 012 110 111 112 222",
			"000 001 002 012 110 111 220 222",
			"000 001 010 011 012 110 111 222",
			"000 001 010 012 101 110 111 222",
			"000 001 002 010 011 012 020 021 022",
			"000 001 002 010 011 012 110 111 112",
			"000 001 002 010 011 012 110 111 222",
			"000 001 002 010 012 101 110 111 112",
			"000 001 002 010 012 101 110 111 222",
			"000 001 002 010 012 110 111 112 222",
			"000 001 002 010 012 110 111 220 222",
			"000 001 002 011 012 100 110 111 222",
			"000 001 002 011 012 110 111 220 222",
			"000 001 002 012 102 110 111 112 222",
			"000 001 010 011 012 100 101 110 111",
			"000 001 002 010 011 012 100 101 110 111",
			"000 001 002 010 011 012 110 111 112 222",
			"000 001 002 010 011 012 110 111 220 222",
			"000 001 002 010 012 101 110 111 112 222",
			"000 001 002 010 012 101 110 111 220 222",
			"000 001 002 011 012 100 110 111 220 222",
			"000 001 002 012 110 111 112 220 221 222",
			"000 001 010 011 012 100 101 102 110 111",
			"000 001 010 011 012 100 101 110 111 222",
			"000 001 002 010 011 012 020 021 022 111 222",
			"000 001 002 010 011 012 100 101 110 111 112",
			"000 001 002 010 011 012 100 101 110 111 222",
			"000 001 002 010 012 110 111 112 220 221 222",
			"000 001 002 012 102 110 111 112 220 221 222",
			"000 001 010 011 012 100 101 102 110 111 222",
			"000 001 002 010 011 012 100 101 102 110 111 112",
			"000 001 002 010 011 012 100 101 110 111 112 222",
			"000 001 002 010 011 012 100 101 110 111 220 222",
			"000 001 002 010 011 012 110 111 112 220 221 222",
			"000 001 002 010 012 101 110 111 112 220 221 222",
			"000 001 002 010 011 012 100 101 102 110 111 112 222",
			"000 001 002 010 011 012 100 101 110 111 112 220 221 222",
			"000 001 002 010 011 012 100 101 102 110 111 112 220 221 222",
			"000 001 002 010 011 012 020 021 022 100 101 110 111 200 202 220 222" };

	public static String[] FINITE_MONOIDS = new String[] {
			"000 012 111",
			"012 120 201",
			"000 002 012 111 222",
			"000 012 021 102 111 120 201 210 222",
			"000 001 002 011 012 022 111 112 122 222",
			"000 001 002 010 011 012 020 022 100 101 110 111 112 121 122 200 202 211 212 220 221 222",
			"000 001 002 010 011 012 020 021 022 100 101 110 111 112 121 122 200 202 211 212 220 221 222",
			"000 001 002 010 011 012 020 022 100 101 110 111 112 120 121 122 200 201 202 211 212 220 221 222",
			"000 001 002 010 011 012 020 021 022 100 101 102 110 111 112 120 121 122 200 201 202 210 211 212 220 221 222" };

	public static String[] UNKNOWN_MONOIDS = new String[] { "012 021",
			"002 012 112", "002 012 220", "000 002 010 012", "000 002 012 022",
			"000 002 012 111", "000 002 012 222", "000 011 012 022",
			"002 012 102 112", "000 002 010 012 111", "000 002 012 022 222",
			"000 002 012 111 112", "000 002 012 220 222",
			"000 011 012 021 022", "002 012 022 200 220",
			"002 012 112 220 221", "000 001 002 010 012 020",
			"000 002 010 012 101 111",
			"000 002 010 012 111 222" /* countable? */,
			"000 002 012 022 111 222", "000 002 012 102 111 112",
			"000 002 012 111 112 222", "000 002 012 111 220 222",
			"002 012 022 200 210 220", "002 012 102 112 220 221",
			"000 001 002 010 012 020 021", "000 002 010 012 101 111 222",
			"000 002 012 022 200 220 222", "000 002 012 102 111 112 222",
			"000 001 002 010 011 012 020 022",
			"000 001 002 010 012 020 111 222",
			"000 001 002 011 012 022 111 222",
			"000 001 011 012 111 112 122 222",
			"000 002 010 012 101 111 220 222",
			"000 002 012 022 111 200 220 222",
			"000 002 012 022 200 210 220 222",
			"000 002 012 111 112 220 221 222",
			"000 001 002 010 012 020 021 111 222",
			"000 002 012 022 111 200 210 220 222",
			"000 002 012 102 111 112 220 221 222",
			"000 001 002 010 011 012 020 022 111 222",
			"000 001 002 010 011 012 020 022 100 101 110 111 200 202 220 222" };

	public static void main(String[] args) {
		System.out.println("*** BOOLEAN ***\n");
		for (String monoid : TWO_MONOIDS)
			printStatistics(2, monoid);

		System.out.println("*** FINITE INTERVALS ***\n");
		for (String monoid : FINITE_MONOIDS)
			printStatistics(3, monoid);

		System.out.println("*** INFINITE INTERVALS ***\n");
		for (String monoid : INFINITE_MONOIDS)
			printStatistics(3, monoid);

		System.out.println("*** UNKNOWN INTERVALS ***\n");
		for (String monoid : UNKNOWN_MONOIDS)
			printStatistics(3, monoid);
	}

	public static final String M512 = "000 002 012 111 222"; // finite
	public static final String M616 = "000 001 012 111 112 222"; // infinite
	public static final String M618 = "000 002 010 012 111 222"; // ?? finite
	public static final String M719 = "000 002 012 102 111 112 222"; // ?? count

	public static void main2(String[] args) {
		String monoid = M719;
		printStatistics(3, monoid);
	}

	public static final int LIMIT = 1000;
	public static final int PRINT_LIMIT = 100;
}
