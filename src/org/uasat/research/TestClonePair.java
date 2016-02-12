/**
 *	Copyright (C) Miklos Maroti, 2015-2016
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
import org.uasat.solvers.*;

@SuppressWarnings("unused")
public class TestClonePair {
	private static DecimalFormat TIME_FORMAT = new DecimalFormat("0.00");

	private static PartialOrder<Boolean> POINT1 = PartialOrder.antiChain(1);
	private static PartialOrder<Boolean> POINT2 = PartialOrder.antiChain(2);
	private static PartialOrder<Boolean> CROWN4 = PartialOrder.crown(4);
	private static PartialOrder<Boolean> CROWN6 = PartialOrder.crown(6);

	public static void main(String[] args) {
		SatSolver<?> solver = new MiniSat();
		long time = System.currentTimeMillis();

		// PartialOrder<Boolean> pos = CROWN4.plus(POINT2).plus(POINT1);
		PartialOrder<Boolean> pos = CROWN4.plus(POINT1);
		Structure<Boolean> str = Structure.wrap(pos.asRelation());
		Structure.print(str);

		GeneratedRels gen1 = GeneratedRels.getTreeDefUnary(str);
		gen1.print();

		GeneratedRels gen2 = GeneratedRels.getTreeDefBinary(str,
				gen1.getRelations());
		// gen2.print();
		System.out.println(gen2.getCount());
		gen2.addIntersections();
		System.out.println(gen2.getCount());
		gen2.addCompositions();
		System.out.println(gen2.getCount());
		gen2.addIntersections();
		gen2.print();
		// System.out.println(gen2.isIntersectionClosed() + "\n");

		ClonePair clone = new ClonePair(str.getSize(), solver);
		clone.trace = false;
		clone.addRels(str);
		clone.addSingletons();
		// clone.print();

		clone.addCriticalOps(2, 1);
		clone.print();

		clone.addCriticalOps(2, 2);
		clone.print();

		clone.addCriticalOps(3, 2);
		clone.print();

		Algebra<Boolean> alg = clone.getAlgebra();
		Algebra.print(alg);

		CompatibleRels com = new CompatibleRels(alg);
		com.printAllRels(1);
		com.printCriticalRels(1);

		com.printAllRels(2);
		com.printCriticalRels(2);
		
		List<Relation<Boolean>> rels = com.findAllRels(2, -1);
		for (Relation<Boolean> rel : rels) {
			if (rel.isEquivalence())
				System.out.println("equivalence " + Relation.format(rel));
		}

		// clone.addCriticalOps(2, 3);
		// clone.print();

		// clone.addCriticalOps(3, 3);
		// clone.print();

		time = System.currentTimeMillis() - time;
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time)
				+ " seconds.");
	}
}
