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

public class Validation {
	private static DecimalFormat TIME_FORMAT = new DecimalFormat("0.00");

	public static void main(String[] args) {
		Domain dom = new Domain(5);
		Relation rel = new Relation(dom, 2);
		Equivalence equ = new Equivalence(dom);
		Permutation perm = new Permutation(dom);

		long time = System.currentTimeMillis();

		Term t1 = Term.count(rel.getTable(), rel.isEquivalence());
		System.out.println(t1.$evaluate());

		Term t2 = Term.count(equ.getTable(), equ.isValid());
		System.out.println(t2.$evaluate());

		Term t3 = Term.count(perm.getTable(), perm.isValid());
		System.out.println(t3.$evaluate());

		time = System.currentTimeMillis() - time;
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time)
				+ " seconds.");
	}
}
