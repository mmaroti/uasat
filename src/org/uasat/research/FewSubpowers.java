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

	public void printSubpowers(Algebra<Boolean> ua, int arity) {
		List<Relation<Boolean>> list = Algebra.getSubpowers(solver, ua, arity);

		for (int i = 0; i < list.size(); i++)
			System.out.println("" + i + ": "
					+ Relation.formatMembers(list.get(i)));
	}

	public static void main(String[] args) {
		long time = System.currentTimeMillis();
		FewSubpowers test = new FewSubpowers();

		test.printSubpowers(IMPALG, 3);

		time = System.currentTimeMillis() - time;
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time)
				+ " seconds.");
	}
}
