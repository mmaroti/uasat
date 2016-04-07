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

public class House2Poset {
	public final Relation<Boolean> poset;
	public final List<Relation<Boolean>> icrit1;
	public final List<Relation<Boolean>> ucrit1;
	public final List<Relation<Boolean>> icrit2;
	public final List<Relation<Boolean>> ucrit2;

	public House2Poset() {
		poset = PartialOrder.crown(4).plus(PartialOrder.antiChain(2))
				.plus(PartialOrder.antiChain(1)).asRelation();

		icrit1 = parseRels(1, "4 6", "5 6", "0 1 2", "0 1 3", "2 4 5 6",
				"3 4 5 6", "0 1 2 3 4", "0 1 2 3 5", "0 2 3 4 5 6",
				"1 2 3 4 5 6");
		ucrit1 = parseRels(1, "");

		icrit2 = parseRels(
				2,
				"00 10 20 30 01 11 21 31 02 12 22 32 03 13 23",
				"00 10 20 30 01 11 21 31 02 12 22 03 13 23 33",
				"00 10 20 30 01 11 21 31 02 12 32 03 13 23 33",
				"02 12 22 04 14 24 34 44 05 15 25 06 16 26 36 46",
				"02 12 22 04 14 24 05 15 25 35 55 06 16 26 36 56",
				"03 13 33 04 14 24 34 44 05 15 35 06 16 26 36 46",
				"03 13 33 04 14 34 05 15 25 35 55 06 16 26 36 56",
				"00 10 20 01 11 21 02 12 22 03 13 23 04 14 24 34 05 15 25 06 16 26 36",
				"00 10 20 01 11 21 02 12 22 03 13 23 04 14 24 05 15 25 35 06 16 26 36",
				"00 10 30 01 11 31 02 12 32 03 13 33 04 14 24 34 05 15 35 06 16 26 36",
				"00 10 30 01 11 31 02 12 32 03 13 33 04 14 34 05 15 25 35 06 16 26 36",
				"40 60 41 61 42 62 43 63 04 14 24 34 44 54 64 45 65 06 16 26 36 46 56 66",
				"50 60 51 61 52 62 53 63 04 14 24 34 44 54 64 55 65 06 16 26 36 46 56 66",
				"50 60 51 61 52 62 53 63 54 64 05 15 25 35 45 55 65 06 16 26 36 46 56 66",
				"00 10 20 01 11 21 02 12 22 32 03 13 23 04 14 24 34 05 15 25 35 06 16 26 36",
				"00 10 20 01 11 21 02 12 22 03 13 23 33 04 14 24 34 05 15 25 35 06 16 26 36",
				"00 10 30 01 11 31 02 12 22 32 03 13 33 04 14 24 34 05 15 25 35 06 16 26 36",
				"00 10 30 01 11 31 02 12 32 03 13 23 33 04 14 24 34 05 15 25 35 06 16 26 36",
				"00 11 02 12 22 03 13 33 04 14 24 34 44 05 15 25 35 55 06 16 26 36 46 56 66",
				"00 10 20 30 01 11 21 02 12 22 32 03 13 23 33 04 14 24 34 05 15 25 35 06 16 26 36",
				"00 10 20 30 01 11 31 02 12 22 32 03 13 23 33 04 14 24 34 05 15 25 35 06 16 26 36",
				"00 10 20 01 11 21 31 02 12 22 32 03 13 23 33 04 14 24 34 05 15 25 35 06 16 26 36",
				"00 10 30 01 11 21 31 02 12 22 32 03 13 23 33 04 14 24 34 05 15 25 35 06 16 26 36",
				"40 60 41 61 02 12 22 32 42 52 62 43 63 04 14 24 34 44 54 64 05 15 25 35 45 55 65 06 16 26 36 46 56 66",
				"40 60 41 61 42 62 03 13 23 33 43 53 63 04 14 24 34 44 54 64 05 15 25 35 45 55 65 06 16 26 36 46 56 66",
				"50 60 51 61 02 12 22 32 42 52 62 53 63 04 14 24 34 44 54 64 05 15 25 35 45 55 65 06 16 26 36 46 56 66",
				"50 60 51 61 52 62 03 13 23 33 43 53 63 04 14 24 34 44 54 64 05 15 25 35 45 55 65 06 16 26 36 46 56 66",
				"20 40 50 60 21 41 51 61 02 12 22 32 42 52 62 23 43 53 63 04 14 24 34 44 54 64 05 15 25 35 45 55 65 06 16 26 36 46 56 66",
				"30 40 50 60 31 41 51 61 02 12 22 32 42 52 62 33 43 53 63 04 14 24 34 44 54 64 05 15 25 35 45 55 65 06 16 26 36 46 56 66",
				"30 40 50 60 31 41 51 61 32 42 52 62 03 13 23 33 43 53 63 04 14 24 34 44 54 64 05 15 25 35 45 55 65 06 16 26 36 46 56 66",
				"00 20 30 40 50 60 01 21 31 41 51 61 02 22 32 42 52 62 03 23 33 43 53 63 04 14 24 34 44 54 64 05 25 35 45 55 65 06 16 26 36 46 56 66",
				"00 20 30 40 50 60 01 21 31 41 51 61 02 22 32 42 52 62 03 23 33 43 53 63 04 24 34 44 54 64 05 15 25 35 45 55 65 06 16 26 36 46 56 66",
				"40 60 01 11 21 31 41 51 61 02 12 22 32 42 52 62 03 13 23 33 43 53 63 04 14 24 34 44 54 64 05 15 25 35 45 55 65 06 16 26 36 46 56 66",
				"50 60 01 11 21 31 41 51 61 02 12 22 32 42 52 62 03 13 23 33 43 53 63 04 14 24 34 44 54 64 05 15 25 35 45 55 65 06 16 26 36 46 56 66",
				"00 20 30 40 50 60 01 21 31 41 51 61 02 12 22 32 42 52 62 03 23 33 43 53 63 04 14 24 34 44 54 64 05 15 25 35 45 55 65 06 16 26 36 46 56 66",
				"00 20 30 40 50 60 01 21 31 41 51 61 02 22 32 42 52 62 03 13 23 33 43 53 63 04 14 24 34 44 54 64 05 15 25 35 45 55 65 06 16 26 36 46 56 66",
				"20 40 50 60 01 11 21 31 41 51 61 02 12 22 32 42 52 62 03 13 23 33 43 53 63 04 14 24 34 44 54 64 05 15 25 35 45 55 65 06 16 26 36 46 56 66",
				"30 40 50 60 01 11 21 31 41 51 61 02 12 22 32 42 52 62 03 13 23 33 43 53 63 04 14 24 34 44 54 64 05 15 25 35 45 55 65 06 16 26 36 46 56 66",
				"00 10 20 30 40 50 60 01 21 31 41 51 61 02 12 22 32 42 52 62 03 13 23 33 43 53 63 04 14 24 34 44 54 64 05 15 25 35 45 55 65 06 16 26 36 46 56 66",
				"00 20 30 40 50 60 01 11 21 31 41 51 61 02 12 22 32 42 52 62 03 13 23 33 43 53 63 04 14 24 34 44 54 64 05 15 25 35 45 55 65 06 16 26 36 46 56 66",
				"10 20 30 40 50 60 01 11 21 31 41 51 61 02 12 22 32 42 52 62 03 13 23 33 43 53 63 04 14 24 34 44 54 64 05 15 25 35 45 55 65 06 16 26 36 46 56 66");

		ucrit2 = parseRels(
				2,
				"00 11 02 12 22 03 13 33 04 14 24 34 44 05 15 25 35 55 06 16 26 36 46 56 66",
				"00 20 30 40 50 60 11 21 31 41 51 61 02 12 22 32 42 52 62 03 13 23 33 43 53 63 04 14 24 34 44 54 64 05 15 25 35 45 55 65 06 16 26 36 46 56 66");
	}

	public List<Relation<Boolean>> parseRels(int arity, String... rels) {
		List<Relation<Boolean>> list = new ArrayList<Relation<Boolean>>();
		for (String rel : rels)
			list.add(Relation.parse(poset.getSize(), arity, rel));
		return list;
	}

	public List<Operation<Boolean>> parseOps(int arity, String... ops) {
		List<Operation<Boolean>> list = new ArrayList<Operation<Boolean>>();
		for (String op : ops)
			list.add(Operation.parse(poset.getSize(), arity, op));
		return list;
	}

	public static List<Relation<Boolean>> concat(List<Relation<Boolean>> list1,
			List<Relation<Boolean>> list2) {
		List<Relation<Boolean>> list = new ArrayList<Relation<Boolean>>();
		list.addAll(list1);
		list.addAll(list2);
		return list;
	}

	public void iFindUnaryCriticals() {
		CriticalRelsGen gen = new CriticalRelsGen(poset.getSize(), 1, 2);

		gen.addGenerator(poset);
		gen.addSingletons();
		gen.generate2();
		gen.printUniCriticals1();

		List<Relation<Boolean>> list = gen.getUniCriticals1();
		assert Relation.sort(list).equals(icrit1);

		gen = new CriticalRelsGen(poset.getSize(), 1, 3);
		gen.addGenerators(list);
		gen.generate1();
		assert list.equals(gen.getUniCriticals1());
	}

	public void uFindUnaryCriticals() {
		CriticalRelsGen gen = new CriticalRelsGen(poset.getSize(), 1, 2);

		gen.addGenerator(poset);
		gen.addGenerator(Relation.empty(poset.getSize(), 1));
		gen.generate2();
		gen.printUniCriticals1();

		List<Relation<Boolean>> list = gen.getUniCriticals1();
		assert Relation.sort(list).equals(ucrit1);

		gen = new CriticalRelsGen(poset.getSize(), 1, 3);
		gen.addGenerators(list);
		gen.generate1();
		assert list.equals(gen.getUniCriticals1());
	}

	public void iFindBinaryCriticals() {
		CriticalRelsGen gen = new CriticalRelsGen(poset.getSize(), 2, 3);

		gen.addGenerator(poset);
		gen.addGenerators(icrit1);
		gen.generate2();
		gen.printUniCriticals1();

		List<Relation<Boolean>> list = gen.getUniCriticals1();
		assert Relation.sort(list).equals(concat(icrit1, icrit2));

		gen = new CriticalRelsGen(poset.getSize(), 2, 4);
		gen.addGenerators(list);
		gen.generate1();
		assert list.equals(gen.getUniCriticals1());
	}

	public void uFindBinaryCriticals() {
		CriticalRelsGen gen = new CriticalRelsGen(poset.getSize(), 2, 3);

		gen.addGenerator(poset);
		gen.addGenerators(ucrit1);
		gen.generate2();
		gen.printUniCriticals1();

		List<Relation<Boolean>> list = gen.getUniCriticals1();
		assert Relation.sort(list).equals(concat(ucrit1, ucrit2));

		gen = new CriticalRelsGen(poset.getSize(), 2, 4);
		gen.addGenerators(list);
		gen.generate1();
		assert list.equals(gen.getUniCriticals1());
	}

	public boolean iExplain(Relation<Boolean> rel, int arity) {
		assert rel.getArity() <= arity && 2 <= arity;

		CriticalRelsGen gen = new CriticalRelsGen(poset.getSize(),
				rel.getArity(), arity);
		gen.addGenerator(poset);
		gen.addGenerators(icrit1);
		return gen.printRepresentation(rel);
	}

	public void iExplainRel2() {
		for (int i = 0; i < icrit2.size(); i++) {
			Relation<Boolean> rel = icrit2.get(i);
			System.out.println(i);
			for (int j = 3; j <= 6; j++)
				if (iExplain(rel, j))
					break;
		}
	}

	public boolean uExplain(Relation<Boolean> rel, int arity) {
		assert rel.getArity() <= arity && 2 <= arity;

		CriticalRelsGen gen = new CriticalRelsGen(poset.getSize(),
				rel.getArity(), arity);
		gen.addGenerator(poset);
		return gen.printRepresentation(rel);
	}

	private static DecimalFormat TIME_FORMAT = new DecimalFormat("0.00");

	public static void main(String[] args) {
		long time = System.currentTimeMillis();

		House2Poset p = new House2Poset();
		// p.iFindUnaryCriticals();
		// p.uFindUnaryCriticals();
		// p.iFindBinaryCriticals();
		p.iExplainRel2();
		// p.uFindBinaryCriticals();

		time = System.currentTimeMillis() - time;
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time)
				+ " seconds.");
	}
}
