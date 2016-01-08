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

public class FewSubpowers {
	private SatSolver<?> solver = new Sat4J();
	private static DecimalFormat TIME_FORMAT = new DecimalFormat("0.00");

	public static Algebra<Boolean> IMPALG = Algebra.wrap(Operation.parseTable(
			2, 2, "11 01"));

	public static Algebra<Boolean> IMPALG0 = IMPALG.extend(Operation
			.parseTable(2, 0, "0"));

	public static Algebra<Boolean> IMPALG1 = IMPALG.extend(Operation
			.parseTable(2, 0, "1"));

	public static Algebra<Boolean> LATTICE = Algebra.wrap(
			Operation.parseTable(2, 2, "00 01"),
			Operation.parseTable(2, 2, "01 11"));

	public void printAllSubpowers(Algebra<Boolean> ua, int arity) {
		List<Relation<Boolean>> list = Algebra.findAllSubpowers(solver, ua,
				arity);

		System.out.println("Subpowers of arity " + arity);
		for (int i = 0; i < list.size(); i++)
			System.out.println("" + i + ": "
					+ Relation.formatMembers(list.get(i)));
		System.out.println();
	}

	public void printMaxSubpowers(Algebra<Boolean> ua, int arity) {
		List<Relation<Boolean>> list = Algebra.findMaximalSubpowers(solver, ua,
				arity);

		System.out.println("Maximal subpowers of arity " + arity + ":");
		for (int i = 0; i < list.size(); i++)
			System.out.println("" + i + ": "
					+ Relation.formatMembers(list.get(i)));
		System.out.println();
	}

	public void printMinSubpowers(Algebra<Boolean> ua, int arity) {
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

	public static void main(String[] args) {
		long time = System.currentTimeMillis();
		FewSubpowers test = new FewSubpowers();

		Algebra<Boolean> alg = IMPALG;
		int arity = 3;

		test.printAllSubpowers(alg, arity);
		// test.printMaxSubpowers(alg, arity);
		// test.printMinSubpowers(alg, arity);
		// test.printSmallestSubpower(alg, arity);

		time = System.currentTimeMillis() - time;
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time)
				+ " seconds.");
	}
}
