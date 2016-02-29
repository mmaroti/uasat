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

import org.uasat.math.*;

public class HousePoset {
	private static DecimalFormat TIME_FORMAT = new DecimalFormat("0.00");

	public static PartialOrder<Boolean> POSET = PartialOrder.crown(4).plus(
			PartialOrder.antiChain(1));

	public static Structure<Boolean> STRUCT = Structure.wrap(
			POSET.asRelation(), Relation.parse(5, 1, "2 4"),
			Relation.parse(5, 1, "3 4"), Relation.parse(5, 1, "0 1 2"),
			Relation.parse(5, 1, "0 1 3"), Relation.parse(5, 1, "0 2 3 4"),
			Relation.parse(5, 1, "1 2 3 4"));

	public static void main(String[] args) {
		long time = System.currentTimeMillis();

		ClonePair clone = new ClonePair(POSET.getSize());
		clone.addRels(STRUCT);
		clone.print();

		GenCriticalRels gen = new GenCriticalRels(POSET.getSize(), 3);
		gen.addGeneratorRels(clone.getStructure());
		gen.printUniCriticalRels(2);

		gen = new GenCriticalRels(POSET.getSize(), 4);
		gen.addGeneratorRels(clone.getStructure());
		gen.printUniCriticalRels(2);
		
		gen = new GenCriticalRels(POSET.getSize(), 5);
		gen.addGeneratorRels(clone.getStructure());
		gen.printUniCriticalRels(2);
		
		clone.addCriticalOps(2, 2);
		clone.print();

		CompatibleRels com = new CompatibleRels(clone.getAlgebra());
		com.printUniCriticalRels(2);

		clone.addCriticalOps(3, 2);
		clone.print();

		com = new CompatibleRels(clone.getAlgebra());
		com.printUniCriticalRels(2);

		time = System.currentTimeMillis() - time;
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time)
				+ " seconds.");
	}
}
