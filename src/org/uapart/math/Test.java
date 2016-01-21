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
		Operation op1 = new Operation(dom, 3);
		Operation op2 = new Operation(dom, 3);

		// Term t = op1.preserves(rel);
		// t = t.and(op2.preserves(rel));
		// t = Operation.areSiggersTerms(op1, op2).and(t);
		// t = Term.exists(op1.getTable(), t);
		// t = Term.exists(op2.getTable(), t);
		// t = rel.isPartialOrder().and(t);
		// t = rel.isLexMinimal().and(t);
		// t = Term.count(rel.getTable(), t);

		Term s = op1.preserves(rel);
		s = op2.preserves(rel).and(s);
		s = Operation.areSiggersTerms(op1, op2).and(s);
		// s = op1.isMajority().and(s);
		s = Term.exists(op1.getTable(), s);
		s = Term.exists(op2.getTable(), s);

		Term t = rel.isPartialOrder();
		t = rel.isLexMinimal().and(t);
		t = t.andThen(s);
		t = Term.count(rel.getTable(), t);

		long time = System.currentTimeMillis();

		System.out.println(t.$evaluate());

		time = System.currentTimeMillis() - time;
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time)
				+ " seconds.");

	}
}
