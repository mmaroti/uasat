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
		long time = System.currentTimeMillis();

		Relation<Boolean> rel = CROWN4.plus(POINT2).plus(POINT1).asRelation();
		GeneratedRels gen = GeneratedRels.getTreeDefUnary(Structure.wrap(rel));
		gen.print();

		ClonePair clone = new ClonePair(rel.getSize(), new MiniSat());
		clone.add(rel);
		clone.addSingletons();
		clone.print();
		System.out.println();

		clone.addCriticalOps(2, 1);
		clone.print();
		System.out.println();

		CompatibleRels com = new CompatibleRels(clone.getAlgebra());
		com.printAllRels(1);

		time = System.currentTimeMillis() - time;
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time)
				+ " seconds.");
	}
}
