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

import org.uasat.math.*;

public class MonInt719 {
	protected static DecimalFormat TIME_FORMAT = new DecimalFormat("0.00");

	public GeneratedOps parseMonoid(String monoid) {
		GeneratedOps ops = new GeneratedOps(3, 1);

		for (String m : monoid.split(" "))
			ops.add(Operation.parse(3, 1, m));

		assert ops.isSelfClosed();
		return ops;
	}

	public GeneratedOps MONOID = parseMonoid("000 002 012 102 111 112 222");

	public void printMonoid() {
		MONOID.print();
	}

	public void printCloneInterval() {
		long time = System.currentTimeMillis();

		CloneInterval clone = new CloneInterval(MONOID);
		clone.trace = true;
		clone.generate(2, 4);
		System.out.println();
		clone.print();

		clone.printClosedOpSets(-1);
		clone.printClosedRelSets(-1);
		System.out.println();

		time = System.currentTimeMillis() - time;
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time)
				+ " seconds.");
	}

	public Operation<Boolean> OP0 = Operation.parse(3, 2, "012 112 222");
	public Operation<Boolean> OP1 = Operation.parse(3, 2, "112 002 222");
	public Operation<Boolean> OP2 = Operation.parse(3, 2, "002 002 222");
	public Operation<Boolean> OP3 = Operation.parse(3, 2, "000 000 002");
	public Operation<Boolean> OP4 = Operation.parse(3, 2, "012 102 222");

	public Relation<Boolean> REL0 = Relation.parse(3, 3,
			"000 110 220 001 111 221 022 122 222");
	public Relation<Boolean> REL1 = Relation.parse(3, 3,
			"000 010 110 001 101 111 222");
	public Relation<Boolean> REL2 = Relation.parse(3, 3,
			"000 020 111 121 002 112 222");
	public Relation<Boolean> REL3 = Relation.parse(3, 3,
			"000 200 110 020 120 220 001 111 211 021 121 221 202 212 222");
	public Relation<Boolean> REL4 = Relation.parse(3, 4,
			"0000 1100 0200 1200 1010 0110 0210 1210 0020 1020 0120 "
					+ "1120 0220 1220 1001 0101 0201 1201 0011 1111 "
					+ "0211 1211 0021 1021 0121 1121 0221 1221 0002 "
					+ "1002 0102 1102 0202 1202 0012 1012 0112 1112 "
					+ "0212 1212 0022 1022 0122 1122 0222 1222 2222");

	public List<Operation<Boolean>> CLONE0_OPS = Arrays.asList();
	public List<Operation<Boolean>> CLONE1_OPS = Arrays.asList(OP3);
	public List<Operation<Boolean>> CLONE2_OPS = Arrays.asList(OP2);
	public List<Operation<Boolean>> CLONE3_OPS = Arrays.asList(OP2, OP3);
	public List<Operation<Boolean>> CLONE4_OPS = Arrays.asList(OP2, OP1);
	public List<Operation<Boolean>> CLONE5_OPS = Arrays.asList(OP2, OP1, OP3);
	public List<Operation<Boolean>> CLONE6_OPS = Arrays.asList(OP2, OP1, OP4);
	public List<Operation<Boolean>> CLONE7_OPS = Arrays.asList(OP2, OP1, OP4,
			OP3);
	public List<Operation<Boolean>> CLONE8_OPS = Arrays.asList(OP2, OP1, OP4,
			OP0);
	public List<Operation<Boolean>> CLONE9_OPS = Arrays.asList(OP2, OP1, OP4,
			OP0, OP3);

	public List<Relation<Boolean>> CLONE0_RELS = Arrays.asList(REL4, REL1,
			REL3, REL2, REL0);
	public List<Relation<Boolean>> CLONE1_RELS = Arrays.asList(REL4, REL1,
			REL3, REL2);
	public List<Relation<Boolean>> CLONE2_RELS = Arrays.asList(REL4, REL1,
			REL3, REL0);
	public List<Relation<Boolean>> CLONE3_RELS = Arrays
			.asList(REL4, REL1, REL3);
	public List<Relation<Boolean>> CLONE4_RELS = Arrays
			.asList(REL4, REL1, REL0);
	public List<Relation<Boolean>> CLONE5_RELS = Arrays.asList(REL4, REL1);
	public List<Relation<Boolean>> CLONE6_RELS = Arrays.asList(REL4, REL0);
	public List<Relation<Boolean>> CLONE7_RELS = Arrays.asList(REL4);
	public List<Relation<Boolean>> CLONE8_RELS = Arrays.asList(REL0);
	public List<Relation<Boolean>> CLONE9_RELS = Arrays.asList();

	public void printCriticalRels(List<Operation<Boolean>> gens) {
		long time = System.currentTimeMillis();

		Algebra<Boolean> alg = Algebra.wrap(MONOID.getOperations());
		alg.addAll(gens);
		Algebra.print(alg);

		CompatibleRels com = new CompatibleRels(alg);
		com.printUniCriticalComps(1);
		com.printUniCriticalComps(2);
		com.printUniCriticalComps(3);
		com.printUniCriticalComps(4);
		com.printUniCriticalComps(5);
		// com.printUniCriticalComps(6);

		time = System.currentTimeMillis() - time;
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time)
				+ " seconds.");
	}

	public Relation<Boolean> getAlpha(int arity) {
		assert arity >= 2;

		Relation<Boolean> r1 = Relation.parse(3, 2, "10 01");
		Relation<Boolean> r2 = Relation.parse(3, 1, "0 1");

		while (r1.getArity() < arity)
			r1 = r1.cartesian(r2);

		return r1.complement();
	}

	public Relation<Boolean> getBeta(int arity) {
		assert arity >= 2;

		Relation<Boolean> r1 = Relation.parse(3, 1, "2");
		Relation<Boolean> r2 = Relation.parse(3, 1, "0 1");

		while (r1.getArity() < arity)
			r1 = r1.cartesian(r2);

		return r1.complement();
	}

	public Relation<Boolean> getGamma(int arity) {
		assert arity >= 4;

		if (arity == 4)
			return Relation.parse(3, 4,
					"1000 0100 0010 1110 0001 1101 1011 0111").complement();
		else if (arity == 5)
			return Relation.parse(
					3,
					5,
					"10000 01000 00100 11100 00010 11010 "
							+ "10110 01110 10001 01001 00101 11101 00011 "
							+ "11011 10111 01111").complement();
		else
			return null;
	}

	public Relation<Boolean> getDelta(int arity, int ones) {
		assert ones >= 1 && 2 * ones <= arity;

		int[] tuple = new int[arity];
		for (int i = 0; i < ones; i++)
			tuple[i] = 1;
		Relation<Boolean> r1 = Relation.singleton(3, tuple);

		for (int i = 0; i < arity; i++)
			tuple[i] = 1 - tuple[i];
		Relation<Boolean> r2 = Relation.singleton(3, tuple);

		return r1.union(r2).complement();
	}

	public Relation<Boolean> getEpsilon(int arity, int twos, int ones) {
		assert twos >= 1 && ones >= 1 && twos + ones + ones <= arity;

		int[] tuple = new int[arity];
		for (int i = 0; i < twos; i++)
			tuple[i] = 2;
		for (int i = twos; i < twos + ones; i++)
			tuple[i] = 1;
		Relation<Boolean> r1 = Relation.singleton(3, tuple);

		for (int i = twos; i < arity; i++)
			tuple[i] = 1 - tuple[i];
		Relation<Boolean> r2 = Relation.singleton(3, tuple);

		return r1.union(r2).complement();
	}

	public Relation<Boolean> getZeta(int arity) {
		assert arity >= 2;

		int[] tuple = new int[arity];
		for (int i = 0; i < arity - 1; i++)
			tuple[i] = 2;
		Relation<Boolean> r1 = Relation.singleton(3, tuple);

		tuple[arity - 1] = 1;
		Relation<Boolean> r2 = Relation.singleton(3, tuple);

		return r1.union(r2).complement();
	}

	public void verifyCriticalRels(List<Operation<Boolean>> gens,
			List<Relation<Boolean>> crits) {
		long time = System.currentTimeMillis();

		Algebra<Boolean> alg = Algebra.wrap(MONOID.getOperations());
		alg.addAll(gens);
		Algebra.print(alg);

		CompatibleRels com = new CompatibleRels(alg);
		List<Relation<Boolean>> rels = new ArrayList<Relation<Boolean>>();
		for (int i = 1; i <= 5; i++)
			rels.addAll(com.findUniCriticalRels(i));

		System.out.println("unqiue critical complements found:");
		for (Relation<Boolean> r : rels)
			System.out.println(Relation.format(r.complement()));
		System.out.println();

		System.out.println("unqiue critical complements expected:");
		for (Relation<Boolean> r : crits)
			System.out.println(Relation.format(r.complement()));
		System.out.println();

		if (rels.size() != crits.size())
			System.out.println("ERROR: count mismatch");
		else {
			rels.removeAll(crits);
			if (rels.isEmpty())
				System.out.println("OK: complete match");
			else
				System.out.println("ERROR: member mismatch");
		}
		System.out.println();

		time = System.currentTimeMillis() - time;
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time)
				+ " seconds.");
	}

	List<Relation<Boolean>> CLONE9_CRITS = Arrays.asList(getAlpha(2),
			getBeta(2), getAlpha(3), getAlpha(4), getAlpha(5));

	List<Relation<Boolean>> CLONE8_CRITS = Arrays.asList(getAlpha(2),
			getBeta(2), getAlpha(3), getBeta(3), getAlpha(4), getBeta(4),
			getAlpha(5), getBeta(5));

	List<Relation<Boolean>> CLONE7_CRITS = Arrays.asList(getAlpha(2),
			getBeta(2), getAlpha(3), getAlpha(4), getGamma(4), getAlpha(5),
			getGamma(5));

	List<Relation<Boolean>> CLONE6_CRITS = Arrays.asList(getAlpha(2),
			getBeta(2), getAlpha(3), getBeta(3), getAlpha(4), getBeta(4),
			getGamma(4), getAlpha(5), getBeta(5), getGamma(5));

	List<Relation<Boolean>> CLONE5_CRITS = Arrays.asList(getBeta(2),
			getDelta(2, 1), getDelta(3, 1), getDelta(4, 1), getDelta(4, 2),
			getDelta(5, 1), getDelta(5, 2));

	List<Relation<Boolean>> CLONE4_CRITS = Arrays.asList(getBeta(2),
			getDelta(2, 1), getBeta(3), getDelta(3, 1), getBeta(4),
			getDelta(4, 1), getDelta(4, 2), getBeta(5), getDelta(5, 1),
			getDelta(5, 2));

	List<Relation<Boolean>> CLONE3_CRITS = Arrays.asList(getBeta(2),
			getDelta(2, 1), getDelta(3, 1), getEpsilon(3, 1, 1),
			getDelta(4, 1), getDelta(4, 2), getEpsilon(4, 1, 1),
			getEpsilon(4, 2, 1), getDelta(5, 1), getDelta(5, 2),
			getEpsilon(5, 1, 1), getEpsilon(5, 1, 2), getEpsilon(5, 2, 1),
			getEpsilon(5, 3, 1));

	List<Relation<Boolean>> CLONE2_CRITS = Arrays.asList(getBeta(2),
			getDelta(2, 1), getBeta(3), getDelta(3, 1), getEpsilon(3, 1, 1),
			getBeta(4), getDelta(4, 1), getDelta(4, 2), getEpsilon(4, 1, 1),
			getEpsilon(4, 2, 1), getBeta(5), getDelta(5, 1), getDelta(5, 2),
			getEpsilon(5, 1, 1), getEpsilon(5, 1, 2), getEpsilon(5, 2, 1),
			getEpsilon(5, 3, 1));

	List<Relation<Boolean>> CLONE1_CRITS = Arrays.asList(getDelta(2, 1),
			getZeta(2), getDelta(3, 1), getEpsilon(3, 1, 1), getZeta(3),
			getDelta(4, 1), getDelta(4, 2), getEpsilon(4, 1, 1),
			getEpsilon(4, 2, 1), getZeta(4), getDelta(5, 1), getDelta(5, 2),
			getEpsilon(5, 1, 1), getEpsilon(5, 1, 2), getEpsilon(5, 2, 1),
			getEpsilon(5, 3, 1), getZeta(5));

	public static void main(String[] args) {
		MonInt719 m = new MonInt719();
		// m.printMonoid();
		// monint.printCloneInterval();
		// m.printCriticalRels(m.CLONE1_OPS);
		m.verifyCriticalRels(m.CLONE1_OPS, m.CLONE1_CRITS);
		// System.out.println(Relation.format(m.getDelta(4,2).complement()));
	}
}
