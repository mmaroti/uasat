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

			ops.add(Operation.parse(size, 1, m));
		}

		assert ops.isSelfClosed();
		return ops;
	}

	protected static DecimalFormat TIME_FORMAT = new DecimalFormat("0.00");

	public static void printClones(String what, List<Operation<Boolean>> ops,
			List<Relation<Boolean>> rels) {
		if (ops.size() >= LIMIT || rels.size() >= LIMIT)
			return;

		GaloisConn<Boolean> gal = GaloisConn.compatiblity(ops, rels);
		List<Relation<Boolean>> list = GaloisConn.findLeftClosedSets(
				SatSolver.getDefault(), gal, LIMIT);

		System.out.println("clones (" + what + "): " + list.size());
		if (list.size() <= PRINT_LIMIT && ops.size() <= PRINT_LIMIT
				&& rels.size() <= PRINT_LIMIT) {
			Collections.sort(list, Relation.COMPARATOR);
			int c = 0;
			for (Relation<Boolean> set : list) {
				String o = Relation.format(set);
				String r = Relation.format(gal.rightClosure(set));
				System.out.println((c++) + ":\t" + o);
				System.out.println("\t" + r);
			}
		}
	}

	public static void printRels(String what, List<Relation<Boolean>> rels) {
		System.out.println(what + (rels.size() < LIMIT ? ": " : ": >= ")
				+ rels.size());

		if (rels.size() <= PRINT_LIMIT) {
			Collections.sort(rels, Relation.COMPARATOR);
			int c = 0;
			for (Relation<Boolean> rel : rels)
				System.out.println((c++) + ":\t" + Relation.format(rel));
		}
	}

	public static void printOps(String what, List<Operation<Boolean>> ops) {
		System.out.println(what + (ops.size() < LIMIT ? ": " : ": >= ")
				+ ops.size());

		if (ops.size() <= PRINT_LIMIT) {
			Collections.sort(ops, Operation.COMPARATOR);
			int c = 0;
			for (Operation<Boolean> op : ops)
				System.out.println((c++) + ":\t" + Operation.format(op));
		}
	}

	public static void printStatistics(int size, String monoid) {
		SatSolver<?> solver = SatSolver.getDefault();
		solver.debugging = false;

		long time = System.currentTimeMillis();

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

		List<Relation<Boolean>> quaternaryRels = crel.findUniqueRels(4, LIMIT);
		printRels("unique quaternary rels", quaternaryRels);
		printRels("unique critical quaternary rels",
				crel.findUniqueCriticalRels(4));

		CompatibleOps cops = new CompatibleOps(
				Structure.trivial(ops.getSize()), ops);

		List<Operation<Boolean>> binaryOps = cops
				.findBinaryOps("unique", LIMIT);
		printOps("unique binary ops", binaryOps);

		List<Operation<Boolean>> ternaryOps = cops.findTernaryOps("unique",
				LIMIT);
		printOps("unique ternary ops", ternaryOps);

		printClones("op 2 rel 2", binaryOps, binaryRels);
		printClones("op 2 rel 3", binaryOps, ternaryRels);
		printClones("op 2 rel 4", binaryOps, quaternaryRels);
		printClones("op 3 rel 2", ternaryOps, binaryRels);
		printClones("op 3 rel 3", ternaryOps, ternaryRels);
		printClones("op 3 rel 4", ternaryOps, quaternaryRels);

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

	public static void main4(String[] args) {
		System.out.println("*** BOOLEAN ***\n");
		for (String monoid : TWO_MONOIDS)
			printStatistics(2, monoid);

		System.out.println("*** UNKNOWN INTERVALS ***\n");
		for (String monoid : UNKNOWN_MONOIDS)
			printStatistics(3, monoid);

		System.out.println("*** FINITE INTERVALS ***\n");
		for (String monoid : FINITE_MONOIDS)
			printStatistics(3, monoid);

		// System.out.println("*** INFINITE INTERVALS ***\n");
		// for (String monoid : INFINITE_MONOIDS)
		// printStatistics(3, monoid);
	}

	public static final String M512 = "000 002 012 111 222"; // finite
	public static final String M616 = "000 001 012 111 112 222"; // infinite
	public static final String M618 = "000 002 010 012 111 222"; // ?? finite
	public static final String M719 = "000 002 012 102 111 112 222"; // ?? count

	public static void main2(String[] args) {
		String monoid = M719;
		printStatistics(3, monoid);
	}

	public static void main(String[] args) {
		long time = System.currentTimeMillis();

		GeneratedOps gen = new GeneratedOps(3, 1);
		String monoid = "000 002 012 022 200 220 222";
		for (String m : monoid.split(" "))
			gen.add(Operation.parse(3, 1, m));
		gen.print();

		CloneInterval clone = new CloneInterval(gen, SatSolver.getDefault());
		clone.trace = true;
		clone.generate(2, 4);
		System.out.println();
		clone.print();

		// clone.printClosedOpSets(-1);
		clone.printClosedRelSets(-1);
		System.out.println();

		time = System.currentTimeMillis() - time;
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time)
				+ " seconds.");
	}

	public static final int LIMIT = 100000;
	public static final int PRINT_LIMIT = 50;
}
