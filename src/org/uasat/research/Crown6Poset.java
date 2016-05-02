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

public class Crown6Poset {
	public final Relation<Boolean> poset;
	public final List<Relation<Boolean>> ucrit1;
	public final List<Relation<Boolean>> ucrit2;
	public final List<Relation<Boolean>> ucrit3;
	public final List<Relation<Boolean>> ucrit4;
	public final List<Relation<Boolean>> ucrit5;
	public final List<Relation<Boolean>> ucrit6;

	public Crown6Poset() {
		poset = PartialOrder.crown(6).asRelation();

		ucrit1 = parseRels(1, "");

		ucrit2 = concat(
				ucrit1,
				parseRels(
						2,
						"00 11 22 03 23 33 04 14 44 15 25 55",
						"00 10 20 30 40 01 11 21 41 51 02 12 22 32 52 03 23 33 04 14 44 15 25 55",
						"00 30 40 11 41 51 22 32 52 03 23 33 43 53 04 14 34 44 54 15 25 35 45 55",
						"00 10 20 30 40 01 11 21 41 51 02 12 22 32 52 03 13 23 33 43 53 04 14 24 34 44 54 05 15 25 35 45 55"));

		ucrit3 = concat(
				ucrit2,
				parseRelComps(3, "543 453 534 354 435 345",
						"210 120 201 021 102 012"));

		ucrit4 = concat(ucrit2, parseRelComps(4));

		ucrit5 = concat(ucrit2, parseRelComps(5));

		ucrit6 = concat(ucrit2, parseRelComps(5));
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

		List<Relation<Boolean>> list = gen.getUniCriticals1();
		assert Relation.sort(list).equals(ucrit6);

		// gen = new CriticalRelsGen(poset.getSize(), 6, 8);
		// gen.addGenerators(list);
		// gen.generate1();
		// assert list.equals(gen.getUniCriticals1());
	}

	public boolean uExplain1(Relation<Boolean> rel, int arity) {
		assert rel.getArity() <= arity && 2 <= arity;

		CriticalRelsGen gen = new CriticalRelsGen(poset.getSize(),
				rel.getArity(), arity);
		gen.addGenerator(poset);
		return gen.printRepresentation(rel);
	}

	public boolean uExplain2(Relation<Boolean> rel, int arity) {
		assert rel.getArity() <= arity && 2 <= arity;

		CriticalRelsGen gen = new CriticalRelsGen(poset.getSize(),
				rel.getArity(), arity);
		gen.addGenerators(ucrit2);
		return gen.printRepresentation(rel);
	}

	private static DecimalFormat TIME_FORMAT = new DecimalFormat("0.00");

	public static void main(String[] args) {
		long time = System.currentTimeMillis();

		Crown6Poset p = new Crown6Poset();
		// System.out.println(Relation.format(p.poset));
		// p.uFindUnaryCriticals();
		// p.uFindBinaryCriticals();
		// p.uExplain1(p.ucrit2.get(4), 4);
		// p.uFindTernaryCriticals();
		p.uFindQuaternaryCriticals();
		// p.uExplain(p.ucrit4.get(4), 5);
		// p.uFindPentaryCriticals();
		// p.uExplain(p.ucrit5.get(4), 7);
		// p.uFindSixaryCriticals();

		time = System.currentTimeMillis() - time;
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time)
				+ " seconds.");
	}
}
