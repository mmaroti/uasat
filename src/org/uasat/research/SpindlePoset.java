/**
 * Copyright (C) Miklos Maroti, 2015
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
import org.uasat.clone.*;

public class SpindlePoset {
	private static DecimalFormat TIME_FORMAT = new DecimalFormat("0.00");

	public final Relation<Boolean> poset = PartialOrder.antiChain(1)
			.plus(PartialOrder.crown(4)).plus(PartialOrder.antiChain(1))
			.asRelation();

	public final List<Relation<Boolean>> idemp_crit1 = parseRels(1, "0 1",
			"0 2", "3 5", "4 5", "0 1 2 3", "0 1 2 4", "1 3 4 5", "2 3 4 5");
	public final List<Relation<Boolean>> idemp_crit2 = parseRels(2,
			"00 10 20 01 11 02 22", "33 53 44 54 35 45 55",
			"01 11 03 13 23 33 04 14 05 15 25 35",
			"01 11 03 13 04 14 24 44 05 15 25 45",
			"02 22 03 13 23 33 04 24 05 15 25 35",
			"02 22 03 23 04 14 24 44 05 15 25 45",
			"00 01 11 02 22 03 13 23 33 04 14 24 44 05 15 25 35 45 55");
	public final List<Relation<Boolean>> idemp_crit3 = parseRels(
			3,
			"000 001 101 011 111 002 202 022 222 003 103 203 303 013 113 213 313 023 123 223 323 033 133 233 333 004 104 204 014 114 024 224 005 105 205 305 015 115 215 315 025 125 225 325 035 135 235 335",
			"000 001 101 011 111 002 202 022 222 003 103 203 013 113 023 223 004 104 204 404 014 114 214 414 024 124 224 424 044 144 244 444 005 105 205 405 015 115 215 415 025 125 225 425 045 145 245 445",
			"011 111 031 131 041 141 051 151 013 113 033 133 233 333 043 143 053 153 253 353 014 114 034 134 044 144 244 444 054 154 254 454 015 115 035 135 235 335 045 145 245 445 055 155 255 355 455 555",
			"022 222 032 232 042 242 052 252 023 223 033 133 233 333 043 243 053 153 253 353 024 224 034 234 044 144 244 444 054 154 254 454 025 225 035 135 235 335 045 145 245 445 055 155 255 355 455 555");

	public final List<Relation<Boolean>> idop2_crit1 = idemp_crit1;
	public final List<Relation<Boolean>> idop2_crit2 = parseRels(2,
			"00 10 20 01 11 02 12 22", "33 53 34 44 54 35 45 55",
			"01 11 03 13 23 33 04 14 05 15 25 35",
			"01 11 03 13 04 14 24 44 05 15 25 45",
			"02 22 03 13 23 33 04 24 05 15 25 35",
			"02 22 03 23 04 14 24 44 05 15 25 45",
			"00 01 11 02 22 03 13 23 33 04 14 24 44 05 15 25 35 45 55");

	public final List<Relation<Boolean>> idop3_crit1 = idemp_crit1;
	public final List<Relation<Boolean>> idop3_crit2 = idemp_crit2;
	public final List<Relation<Boolean>> idop3_crit3 = idemp_crit3;

	public List<Relation<Boolean>> parseRels(int arity, String... rels) {
		List<Relation<Boolean>> list = new ArrayList<Relation<Boolean>>();
		for (String rel : rels)
			list.add(Relation.parse(poset.getSize(), arity, rel));
		return list;
	}

	public void compare(List<Relation<Boolean>> list1,
			List<Relation<Boolean>> list2) {

		List<Relation<Boolean>> list = new ArrayList<Relation<Boolean>>();
		list.addAll(list1);
		list.removeAll(list2);
		Relation.print("in first not in second", list);

		list.clear();
		list.addAll(list2);
		list.removeAll(list1);
		Relation.print("in second not in first", list);
	}

	public List<Operation<Boolean>> parseOps(int arity, String... ops) {
		List<Operation<Boolean>> list = new ArrayList<Operation<Boolean>>();
		for (String op : ops)
			list.add(Operation.parse(poset.getSize(), arity, op));
		return list;
	}

	public void explainIdemp(Relation<Boolean> rel, int arity) {
		assert rel.getArity() <= arity && 2 <= arity;

		CriticalRelsGen gen = new CriticalRelsGen(poset.getSize(),
				rel.getArity(), arity);
		gen.addGenerator(poset);
		gen.addGenerators(idemp_crit1);
		gen.printRepresentation(rel);
		gen.printStats();
	}

	// Idempotent clone

	public void findIdempUnaryCriticals() {
		CriticalRelsGen gen = new CriticalRelsGen(poset.getSize(), 1, 2);

		gen.addGenerator(poset);
		gen.addSingletons();
		gen.generate2();
		gen.printUniCriticals1();

		List<Relation<Boolean>> rels = Relation.sort(gen.getFullCriticals1());
		assert rels.equals(idemp_crit1);
	}

	public void printIdempUnaryCritCovers() {
		MeetClosedRels rels = new MeetClosedRels(5, 1);
		rels.addPermutedGens(idemp_crit1);

		for (Relation<Boolean> rel : idemp_crit1) {
			Relation<Boolean> cov = rels.getIrredCover(rel);

			System.out.println(Relation.format(rel));
			System.out.println(Relation.format(cov));
			System.out.println(Relation.format(cov.subtract(rel)));
			System.out.println();
		}
	}

	public void findIdempBinaryCriticals() {
		CriticalRelsGen gen = new CriticalRelsGen(poset.getSize(), 2, 3);

		gen.addGenerator(poset);
		gen.addGenerators(idemp_crit1);
		gen.generate2();
		gen.printUniCriticals1();
		// gen.printStats();

		List<Relation<Boolean>> rels = Relation.sort(gen.getFullCriticals1());
		assert rels.equals(idemp_crit2);
	}

	public void printIdempBinaryCritCovers() {
		MeetClosedRels rels = new MeetClosedRels(5, 2);
		rels.addPermutedGens(idemp_crit1);
		rels.addPermutedGens(idemp_crit2);

		for (Relation<Boolean> rel : idemp_crit2) {
			Relation<Boolean> cov = rels.getIrredCover(rel);

			System.out.println(Relation.format(rel));
			System.out.println(Relation.format(cov));
			System.out.println(Relation.format(cov.subtract(rel)));
			System.out.println();
		}
	}

	public void findIdempTernaryCriticals() {
		CriticalRelsGen gen = new CriticalRelsGen(poset.getSize(), 3, 4);
		gen.trace = true;

		gen.addGenerators(idemp_crit1);
		gen.addGenerators(idemp_crit2);
		gen.generate2();
		gen.printUniCriticals1();

		List<Relation<Boolean>> rels = Relation.sort(gen.getFullCriticals1());
		assert rels.equals(idemp_crit3);
	}

	public void printIdempTernaryCritCovers() {
		MeetClosedRels rels = new MeetClosedRels(5, 3);
		rels.addPermutedGens(idemp_crit1);
		rels.addPermutedGens(idemp_crit2);
		rels.addPermutedGens(idemp_crit3);

		for (Relation<Boolean> rel : idemp_crit3) {
			Relation<Boolean> cov = rels.getIrredCover(rel);

			System.out.println(Relation.format(rel));
			System.out.println(Relation.format(cov));
			System.out.println(Relation.format(cov.subtract(rel)));
			System.out.println();
		}
	}

	public void findIdempQuaternaryCriticals() {
		CriticalRelsGen gen = new CriticalRelsGen(poset.getSize(), 4, 5);
		gen.trace = true;

		gen.addGenerators(idemp_crit1);
		gen.addGenerators(idemp_crit2);
		System.out.println("done adding 2-criticals");
		gen.addGenerators(idemp_crit3);
		System.out.println("done adding 3-criticals");
		gen.generate1();
		gen.printUniCriticals1();
	}

	// Clone generated by binary idempotent ops

	public void findIdempOp2UnaryCriticals() {
		ClonePair clone = new ClonePair(poset.getSize());
		// clone.trace = true;
		clone.addRelation(poset);
		clone.addSingletonRels();
		clone.addCriticalOps(2, 1);
		clone.print();

		CompatibleRels comp = new CompatibleRels(clone.getAlgebra());
		List<Relation<Boolean>> rels = comp.findUniCriticalRels(1);
		Relation.print("idemp op2 unary criticals", rels);

		assert rels.equals(idop2_crit1);
	}

	public void findIdempOp2BinaryCriticals() {
		ClonePair clone = new ClonePair(poset.getSize());
		// clone.trace = true;
		clone.addRelation(poset);
		clone.addSingletonRels();
		clone.addCriticalOps(2, 2);
		clone.print();

		CompatibleRels comp = new CompatibleRels(clone.getAlgebra());
		List<Relation<Boolean>> rels = comp.findUniCriticalRels(2);
		Relation.print("idemp op2 binary criticals", rels);

		rels = Relation.sort(rels);
		assert rels.equals(idop2_crit2);
	}

	public void findIdempOp2BinaryCriticalsFast() {
		CriticalRelsGen2 gen = new CriticalRelsGen2(poset.getSize(), 2);
		gen.addRelations(idemp_crit1);
		gen.addRelations(idemp_crit2);
		gen.trace = true;

		gen.generate(2);
		gen.printUniCriticals();

		List<Relation<Boolean>> rels = Relation.sort(gen.getFullCriticals());
		assert rels.equals(idop2_crit2);
	}

	// Clone generated by all idempotent ternary ops

	public void findIdempOp3UnaryCriticals() {
		CriticalRelsGen2 gen = new CriticalRelsGen2(poset.getSize(), 1);
		gen.addRelations(idemp_crit1);
		gen.trace = true;

		gen.generate(3);
		gen.printUniCriticals();

		List<Relation<Boolean>> rels = Relation.sort(gen.getFullCriticals());
		assert rels.equals(idop3_crit1);
	}

	public void findIdempOp3BinaryCriticals() {
		CriticalRelsGen2 gen = new CriticalRelsGen2(poset.getSize(), 2);
		gen.addRelations(idemp_crit1);
		gen.addRelations(idemp_crit2);
		gen.trace = true;

		gen.generate(3);
		gen.printUniCriticals();

		List<Relation<Boolean>> rels = Relation.sort(gen.getFullCriticals());
		assert rels.equals(idop3_crit2);
	}

	public void findIdempOp3TernaryCriticals() {
		CriticalRelsGen2 gen = new CriticalRelsGen2(poset.getSize(), 3);
		gen.addRelations(idemp_crit1);
		gen.addRelations(idemp_crit2);
		gen.addRelations(idemp_crit3);
		gen.trace = true;

		gen.generate(3);
		gen.printUniCriticals();

		List<Relation<Boolean>> rels = Relation.sort(gen.getFullCriticals());
		assert rels.equals(idop3_crit3);
	}

	public void test1() {
		FinitelyRel clone = new CloneRel(poset);
		FinitelyRel idemp = new CloneMeet(clone, CloneRel.idempotentClone(5));

		FinitelyGen clone1 = new LowerBound(clone, 1);
		FinitelyGen idemp2 = new LowerBound(idemp, 2);
		FinitelyGen generated = new CloneJoin(clone1, idemp2);

		Split split = new Split(generated, clone);
		split.trace = true;
		split.findMinRel(4, 2);
		split.print();
	}

	public static void main(String[] args) {
		long time = System.currentTimeMillis();

		SpindlePoset h = new SpindlePoset();

		// h.findIdempUnaryCriticals();
		// h.findIdempBinaryCriticals();
		// h.findIdempTernaryCriticals();
		// h.findIdempQuaternaryCriticals();
		// h.explainIdemp(Relation.parse(2,
		// "02 12 22 32 42 23 33 43 04 14 24 34 44"), 6);
		// h.printIdempBinaryCritCovers();

		// h.findIdempOp2UnaryCriticals();
		// h.findIdempOp2BinaryCriticals();
		// h.compare(h.idemp_crit2, h.idop2_crit2);

		// h.findIdempOp3UnaryCriticals();
		// h.findIdempOp3BinaryCriticals();
		h.findIdempOp3TernaryCriticals();

		// h.test1();

		time = System.currentTimeMillis() - time;
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time)
				+ " seconds.");
	}
}
