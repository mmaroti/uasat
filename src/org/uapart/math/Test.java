/**
 *	Copyright (C) Miklos Maroti, 2016
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

package org.uapart.math;

import java.text.*;

import org.uapart.core.*;

public class Test {
	private static DecimalFormat TIME_FORMAT = new DecimalFormat("0.00");

	public static void main(String[] args) {
		Domain dom = new Domain(5);

		Relation rel = new Relation(dom, 2);
		Operation op = new Operation(dom, 3);

		// Term s = op.preserves(rel).and(op.isMajority());
		Term s = op.isMajority().and(op.preserves(rel));
		// Printer p = new Printer(s, -1, rel.getTable(), op.getTable());
		s = Term.exists(op.getTable(), s);

		Term t = rel.isPartialOrder();
		t = t.and(rel.isLexMinimal());
		// t = t.print(1, rel.getTable());
		t = t.andThen(s);
		t = Term.count(rel.getTable(), t);

		long time = System.currentTimeMillis();

		System.out.println(t.$evaluate());

		time = System.currentTimeMillis() - time;
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time)
				+ " seconds.");
	}

	public static void main2(String[] args) {
		Domain dom = new Domain(5);

		Relation rel = new Relation(dom, 2);
		int[] table = new int[] { 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1,
				0, 0, 0, 1, 1, 0, 0, 0, 0, 1 };
		assert table.length == rel.getTable().getTable().length;
		System.arraycopy(table, 0, rel.getTable().getTable(), 0, table.length);

		Operation op = new Operation(dom, 3);

		// Term s = op.preserves(rel).and(op.isMajority());
		Term s = op.isMajority().and(op.preserves(rel));
		// Term s = op.preserves(rel);
		// Printer p = new Printer(s, -1, rel.getTable(), op.getTable());
		s = Term.exists(op.getTable(), s);

		System.out.println(s.$evaluate());

		// Term t = rel.isPartialOrder();
		// t = rel.isLexMinimal().and(t);
		// t = t.print(1, rel.getTable()).andThen(s);
		// t = Term.count(rel.getTable(), t);

		// System.out.println(t.$evaluate());
	}
}
