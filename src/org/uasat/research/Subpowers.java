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
import org.uasat.solvers.*;

public class Subpowers {
	private SatSolver<?> solver = new Sat4J();
	private static DecimalFormat TIME_FORMAT = new DecimalFormat("0.00");

	public static Operation<Boolean> ZERO = Operation.parseTable(2, 0, "0");
	public static Operation<Boolean> ONE = Operation.parseTable(2, 0, "1");
	public static Operation<Boolean> NOT = Operation.parseTable(2, 1, "10");
	public static Operation<Boolean> MEET = Operation.parseTable(2, 2, "00 01");
	public static Operation<Boolean> JOIN = Operation.parseTable(2, 2, "01 11");
	public static Operation<Boolean> ADD = Operation.parseTable(2, 2, "01 10");
	public static Operation<Boolean> EQU = Operation.parseTable(2, 2, "10 01");
	public static Operation<Boolean> IMPL = Operation.parseTable(2, 2, "11 01");
	public static Operation<Boolean> MAJOR = Operation.parseTable(2, 3,
			"00 01 01 11");
	public static Operation<Boolean> MINOR = Operation.parseTable(2, 3,
			"01 10 10 01");

	public void printAllSubpowers(Algebra<Boolean> ua, int arity) {
		List<Relation<Boolean>> list = Algebra.findAllSubpowers(solver, ua,
				arity);

		System.out.println("Subpowers of arity " + arity + ":");
		for (int i = 0; i < list.size(); i++)
			System.out.println("" + i + ": "
					+ Relation.formatMembers(list.get(i)));
		System.out.println();
	}

	public void printMaximalSubpowers(Algebra<Boolean> ua, int arity) {
		List<Relation<Boolean>> list = Algebra.findMaximalSubpowers(solver, ua,
				arity);

		System.out.println("Maximal subpowers of arity " + arity + ":");
		for (int i = 0; i < list.size(); i++)
			System.out.println("" + i + ": "
					+ Relation.formatMembers(list.get(i)));
		System.out.println();
	}

	public void printMinimalSubpowers(Algebra<Boolean> ua, int arity) {
		List<Relation<Boolean>> list = Algebra.findMinimalSubpowers(solver, ua,
				arity);

		System.out.println("Minimal subpowers of arity " + arity + ":");
		for (int i = 0; i < list.size(); i++)
			System.out.println("" + i + ": "
					+ Relation.formatMembers(list.get(i)));
		System.out.println();
	}

	public void printSmallestSubpower(Algebra<Boolean> ua, int arity) {
		Relation<Boolean> rel = Algebra.findSmallestSubpower(solver, ua, arity);

		System.out.println("Smallest subpower of arity " + arity + ":");
		System.out.println(Relation.formatMembers(rel));
		System.out.println();
	}

	public void printMeetIrredSubpowers(Algebra<Boolean> ua, int arity) {
		List<Relation<Boolean>> list = Algebra.findMeetIrredSubpowers(solver,
				ua, arity);

		System.out
				.println("Meet irreducible subpowers of arity " + arity + ":");
		for (int i = 0; i < list.size(); i++)
			System.out.println("" + i + ": "
					+ Relation.formatMembers(list.get(i)));
		System.out.println();
	}

	public void printCriticalSubpowers(Algebra<Boolean> ua, int arity) {
		List<Relation<Boolean>> list = Algebra.findCriticalSubpowers(solver,
				ua, arity);

		System.out.println("Critical subpowers of arity " + arity + ":");
		for (int i = 0; i < list.size(); i++)
			System.out.println("" + i + ": "
					+ Relation.formatMembers(list.get(i)));
		System.out.println();
	}

	public static void main(String[] args) {
		long time = System.currentTimeMillis();
		Subpowers test = new Subpowers();

		Algebra<Boolean> alg = Algebra.wrap(IMPL, JOIN);

		test.printCriticalSubpowers(alg, 1);
		test.printCriticalSubpowers(alg, 2);
		test.printCriticalSubpowers(alg, 3);
		test.printCriticalSubpowers(alg, 4);
		test.printCriticalSubpowers(alg, 5);
		// test.printCriticalSubpowers(alg, 6);

		// int arity = 1;
		// test.printAllSubpowers(alg, arity);
		// test.printMeetIrredSubpowers(alg, arity);
		// test.printMaximalSubpowers(alg, arity);
		// test.printMinimalSubpowers(alg, arity);
		// test.printSmallestSubpower(alg, arity);

		time = System.currentTimeMillis() - time;
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time)
				+ " seconds.");
	}
}