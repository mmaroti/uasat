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

public class Crown4Poset {
	public final Relation<Boolean> poset;
	public final List<Relation<Boolean>> icrit1;
	public final List<Relation<Boolean>> ucrit1;
	public final List<Relation<Boolean>> icrit2;
	public final List<Relation<Boolean>> ucrit2;
	public final List<Relation<Boolean>> icrit3;
	public final List<Relation<Boolean>> ucrit3;
	public final List<Relation<Boolean>> ucrit4;
	public final List<Relation<Boolean>> ucrit5;

	public Crown4Poset() {
		poset = PartialOrder.crown(4).asRelation();

		icrit1 = parseRels(1, "0 1 2", "0 1 3", "0 2 3", "1 2 3");
		ucrit1 = parseRels(1, "");

		icrit2 = parseRelComps(2, "33", "32", "22", "31", "21", "11", "30",
				"20", "10", "00");

		ucrit2 = concat(
				ucrit1,
				parseRels(2, "00 11 02 12 22 03 13 33",
						"00 10 20 30 01 11 21 31 02 12 22 03 13 33",
						"00 20 30 11 21 31 02 12 22 32 03 13 23 33"));

		ucrit3 = ucrit2;

		icrit3 = parseRelComps(3, "333", "332", "322", "222", "331", "321",
				"221", "311", "211", "111", "330", "320", "220", "310", "210",
				"110", "300", "200", "100", "000");

		ucrit4 = concat(ucrit2, parseRelComps(4, "3210 2310 3201 2301"));

		ucrit5 = concat(
				ucrit2,
				parseRelComps(5, "32210 23310 32201 23301",
						"32100 23100 32011 23011"));
	}

	public List<Relation<Boolean>> parseRels(int arity, String... rels) {
		List<Relation<Boolean>> list = new ArrayList<Relation<Boolean>>();
		for (String rel : rels)
			list.add(Relation.parse(poset.getSize(), arity, rel));
		return list;
	}

	public List<Relation<Boolean>> parseRelComps(int arity, String... rels) {
		List<Relation<Boolean>> list = new ArrayList<Relation<Boolean>>();
		for (String rel : rels)
			list.add(Relation.parse(poset.getSize(), arity, rel).complement());
		return list;
	}

	public List<Operation<Boolean>> parseOps(int arity, String... ops) {
		List<Operation<Boolean>> list = new ArrayList<Operation<Boolean>>();
		for (String op : ops)
			list.add(Operation.parse(poset.getSize(), arity, op));
		return list;
	}

	@SafeVarargs
	public static List<Relation<Boolean>> concat(
			List<Relation<Boolean>>... lists) {
		List<Relation<Boolean>> list = new ArrayList<Relation<Boolean>>();
		for (List<Relation<Boolean>> l : lists)
			list.addAll(l);
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
		assert Relation.sort(list).equals(icrit2);

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
		assert Relation.sort(list).equals(ucrit2);

		gen = new CriticalRelsGen(poset.getSize(), 2, 4);
		gen.addGenerators(list);
		gen.generate1();
		assert list.equals(gen.getUniCriticals1());
	}

	public void iFindTernaryCriticals() {
		CriticalRelsGen gen = new CriticalRelsGen(poset.getSize(), 3, 4);

		gen.addGenerators(icrit2);
		gen.generate2();
		gen.printUniCriticals1();

		List<Relation<Boolean>> list = gen.getUniCriticals1();
		assert Relation.sort(list).equals(icrit3);

		gen = new CriticalRelsGen(poset.getSize(), 3, 5);
		gen.addGenerators(list);
		gen.generate1();
		assert list.equals(gen.getUniCriticals1());
	}

	public void uFindTernaryCriticals() {
		CriticalRelsGen gen = new CriticalRelsGen(poset.getSize(), 3, 4);

		gen.addGenerators(ucrit2);
		gen.generate2();
		gen.printUniCriticals1();

		List<Relation<Boolean>> list = gen.getUniCriticals1();
		assert Relation.sort(list).equals(ucrit3);

		gen = new CriticalRelsGen(poset.getSize(), 3, 5);
		gen.addGenerators(list);
		gen.generate1();
		assert list.equals(gen.getUniCriticals1());
	}

	public void uFindQuaternaryCriticals() {
		CriticalRelsGen gen = new CriticalRelsGen(poset.getSize(), 4, 5);

		gen.addGenerators(ucrit2);
		gen.generate2();
		gen.printUniCriticals1();

		List<Relation<Boolean>> list = gen.getUniCriticals1();
		assert Relation.sort(list).equals(ucrit4);

		gen = new CriticalRelsGen(poset.getSize(), 4, 6);
		gen.addGenerators(list);
		gen.generate1();
		assert list.equals(gen.getUniCriticals1());
	}

	public void uFindPentaryCriticals() {
		CriticalRelsGen gen = new CriticalRelsGen(poset.getSize(), 5, 6);
		gen.trace = true;

		gen.addGenerators(ucrit2);
		gen.generate2();
		gen.printUniCriticals1();

		List<Relation<Boolean>> list = gen.getUniCriticals1();
		assert Relation.sort(list).equals(ucrit5);

		gen = new CriticalRelsGen(poset.getSize(), 5, 7);
		gen.addGenerators(list);
		gen.generate1();
		assert list.equals(gen.getUniCriticals1());
	}

	public void uFindSixaryCriticals() {
		CriticalRelsGen gen = new CriticalRelsGen(poset.getSize(), 6, 7);
		gen.trace = true;

		gen.addGenerators(ucrit2);
		gen.generate2();
		gen.printUniCriticals1();

		// List<Relation<Boolean>> list = gen.getUniCriticals1();
		// assert Relation.sort(list).equals(ucrit5);

		// gen = new CriticalRelsGen(poset.getSize(), 5, 7);
		// gen.addGenerators(list);
		// gen.generate1();
		// assert list.equals(gen.getUniCriticals1());
	}

	public boolean iExplain(Relation<Boolean> rel, int arity) {
		assert rel.getArity() <= arity && 2 <= arity;

		CriticalRelsGen gen = new CriticalRelsGen(poset.getSize(),
				rel.getArity(), arity);
		gen.addGenerator(poset);
		gen.addGenerators(icrit1);

		return gen.printRepresentation(rel);
	}

	public boolean uExplain(Relation<Boolean> rel, int arity) {
		assert rel.getArity() <= arity && 2 <= arity;

		CriticalRelsGen gen = new CriticalRelsGen(poset.getSize(),
				rel.getArity(), arity);
		gen.addGenerators(ucrit2);
		return gen.printRepresentation(rel);
	}

	private static DecimalFormat TIME_FORMAT = new DecimalFormat("0.00");

	public static void main(String[] args) {
		long time = System.currentTimeMillis();

		Crown4Poset p = new Crown4Poset();
		// p.iFindUnaryCriticals();
		// p.uFindUnaryCriticals();
		// p.iFindBinaryCriticals();
		// p.iExplain(p.icrit2.get(9), 7);
		// p.iExplain(p.icrit2.get(8), 7);
		// p.iExplain(p.icrit2.get(7), 5);
		// p.uFindBinaryCriticals();
		// p.iFindTernaryCriticals();
		// p.uFindTernaryCriticals();
		// p.uFindQuaternaryCriticals();
		// p.uExplain(p.ucrit4.get(4), 5);
		p.uFindPentaryCriticals();
		// p.uExplain(p.ucrit5.get(4), 7);
		p.uFindSixaryCriticals();

		time = System.currentTimeMillis() - time;
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time)
				+ " seconds.");
	}
}
