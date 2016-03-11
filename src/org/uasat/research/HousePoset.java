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

public class HousePoset {
	private static DecimalFormat TIME_FORMAT = new DecimalFormat("0.00");

	public final Relation<Boolean> poset;
	public final List<Relation<Boolean>> crit1;

	public final List<Relation<Boolean>> crit2_gen;
	public final Relation<Boolean> spec2_irred;

	public final List<Relation<Boolean>> crit2_op2;
	public final Relation<Boolean> spec2_cover;

	// public final List<Relation<Boolean>> crit2_op3;

	public HousePoset() {
		poset = PartialOrder.crown(4).plus(PartialOrder.antiChain(1))
				.asRelation();

		// crit1 = findUnaryCriticals();
		crit1 = parseRels(1, "2 4", "3 4", "0 1 2", "0 1 3", "0 2 3 4",
				"1 2 3 4");

		// crit2_gen = findBinaryCriticals();
		crit2_gen = parseRels(2, "21 41 02 22 32 42 23 43 04 24 34 44",
				"21 41 22 42 03 23 33 43 04 24 34 44",
				"31 41 02 22 32 42 33 43 04 24 34 44",
				"31 41 32 42 03 23 33 43 04 24 34 44",
				"02 12 22 32 42 23 43 04 14 24 34 44",
				"02 12 22 32 42 33 43 04 14 24 34 44",
				"22 42 03 13 23 33 43 04 14 24 34 44",
				"32 42 03 13 23 33 43 04 14 24 34 44",
				"00 11 02 12 22 03 13 33 04 14 24 34 44",
				"00 20 30 40 02 12 22 32 42 03 23 33 43 04 14 24 34 44",
				"00 20 30 40 02 22 32 42 03 13 23 33 43 04 14 24 34 44",
				"11 21 31 41 02 12 22 32 42 13 23 33 43 04 14 24 34 44",
				"11 21 31 41 12 22 32 42 03 13 23 33 43 04 14 24 34 44",
				"00 20 30 40 11 21 31 41 02 12 22 32 42 03 13 23 33 43 04 14 24 34 44");
		spec2_irred = crit2_gen.get(crit2_gen.size() - 1);

		// crit2_op2 = findBinaryCritOp2();
		crit2_op2 = parseRels(2, "21 41 02 22 32 42 23 43 04 24 34 44",
				"21 41 22 42 03 23 33 43 04 24 34 44",
				"31 41 02 22 32 42 33 43 04 24 34 44",
				"31 41 32 42 03 23 33 43 04 24 34 44",
				"02 12 22 32 42 23 43 04 14 24 34 44",
				"02 12 22 32 42 33 43 04 14 24 34 44",
				"22 42 03 13 23 33 43 04 14 24 34 44",
				"32 42 03 13 23 33 43 04 14 24 34 44",
				"00 11 02 12 22 03 13 33 04 14 24 34 44",
				"00 20 30 40 02 12 22 32 42 03 23 33 43 04 14 24 34 44",
				"00 20 30 40 02 22 32 42 03 13 23 33 43 04 14 24 34 44",
				"11 21 31 41 02 12 22 32 42 13 23 33 43 04 14 24 34 44",
				"11 21 31 41 12 22 32 42 03 13 23 33 43 04 14 24 34 44",
				"00 20 30 40 01 11 21 31 41 02 12 22 32 42 03 13 23 33 43 04 14 24 34 44");
		spec2_cover = crit2_op2.get(crit2_op2.size() - 1);

		// crit2_op3 = findBinaryCritOp3();
	}

	public List<Relation<Boolean>> parseRels(int arity, String... rels) {
		List<Relation<Boolean>> list = new ArrayList<Relation<Boolean>>();
		for (String rel : rels)
			list.add(Relation.parse(poset.getSize(), arity, rel));
		return list;
	}

	public List<Relation<Boolean>> findUnaryCriticals() {
		List<Relation<Boolean>> rels = new ArrayList<Relation<Boolean>>();

		rels.add(poset);
		for (int i = 0; i < poset.getSize(); i++)
			rels.add(Relation.singleton(poset.getSize(), i));

		rels = GenCriticalRels.genUniCriticalRels(rels, 2, 1);
		Relation.print("crit1", rels);

		return rels;
	}

	public List<Relation<Boolean>> findBinaryCriticals() {
		List<Relation<Boolean>> rels = new ArrayList<Relation<Boolean>>();

		rels.add(poset);
		rels.addAll(crit1);

		rels = GenCriticalRels.genUniCriticalRels(rels, 3, 2);
		Relation.print("crit2 gen", rels);

		return rels;
	}

	public List<Relation<Boolean>> findBinaryCritOp2() {
		ClonePair clone = new ClonePair(poset.getSize());
		// clone.trace = true;
		clone.add(poset);
		clone.addSingletons();
		clone.addCriticalOps(2, 2);
		clone.print();

		CompatibleRels comp = new CompatibleRels(clone.getAlgebra());
		List<Relation<Boolean>> rels = comp.findUniCriticalRels(2);
		Relation.print("crit2 op2", rels);

		return rels;
	}

	public List<Relation<Boolean>> findBinaryCritOp3() {
		ClonePair clone = new ClonePair(poset.getSize());
		clone.add(poset);
		clone.addSingletons();
		clone.addCriticalOps(2, 2);
		clone.addCriticalOps(3, 2);
		clone.print();

		CompatibleRels comp = new CompatibleRels(clone.getAlgebra());
		List<Relation<Boolean>> rels = comp.findUniCriticalRels(2);
		Relation.print("crit2 op3", rels);

		return rels;
	}

	public List<Relation<Boolean>> findTernaryCriticals() {
		List<Relation<Boolean>> rels = new ArrayList<Relation<Boolean>>();

		rels.addAll(crit1);
		rels.addAll(crit2_gen);

		rels = GenCriticalRels.genUniCriticalRels(rels, 4, 3);
		Relation.print("crit3 gen", rels);

		return rels;
	}

	public List<Relation<Boolean>> findTernaryCritOp2() {
		ClonePair clone = new ClonePair(poset.getSize());
		clone.trace = true;
		clone.add(poset);
		clone.addSingletons();
		clone.addCriticalOps(2, 2);
		clone.print();
		// clone.addCriticalOps(2, 3);
		// clone.print();

		CompatibleRels comp = new CompatibleRels(clone.getAlgebra());
		List<Relation<Boolean>> rels = comp.findUniCriticalRels(3);
		Relation.print("crit3 op2", rels);

		return rels;
	}

	public List<Relation<Boolean>> findTernaryCritOp3() {
		ClonePair clone = new ClonePair(poset.getSize());
		clone.add(poset);
		clone.addSingletons();
		clone.addCriticalOps(2, 2);
		clone.print();

		clone.trace = true;
		while (clone.addCriticalOp(3, 3)) {
			clone.print();

			CompatibleRels comp = new CompatibleRels(clone.getAlgebra());
			List<Relation<Boolean>> rels = comp.findUniCriticalRels(3);
			Relation.print("crit3 op3", rels);
		}

		CompatibleRels comp = new CompatibleRels(clone.getAlgebra());
		List<Relation<Boolean>> rels = comp.findUniCriticalRels(3);
		Relation.print("crit3 op3", rels);

		return rels;
	}

	public List<Relation<Boolean>> findTernaryCritOp4() {
		ClonePair clone = new ClonePair(poset.getSize());
		clone.add(poset);
		clone.addSingletons();
		clone.addCriticalOps(4, 3);
		clone.print();

		CompatibleRels comp = new CompatibleRels(clone.getAlgebra());
		List<Relation<Boolean>> rels = comp.findUniCriticalRels(2);
		Relation.print("crit3 op4", rels);

		return rels;
	}

	public void explain(Relation<Boolean> rel, int full) {
		GenCriticalRels gen = new GenCriticalRels(poset.getSize(), full);
		gen.addGeneratorRel(poset);
		gen.addGeneratorRels(crit1);
		gen.printRepresentation(rel);
	}

	public static void main(String[] args) {
		long time = System.currentTimeMillis();

		HousePoset h = new HousePoset();
		// h.explain(h.spec2_irred, 3);
		// h.findTernaryCriticals();
		h.findTernaryCritOp3();

		time = System.currentTimeMillis() - time;
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time)
				+ " seconds.");
	}
}
