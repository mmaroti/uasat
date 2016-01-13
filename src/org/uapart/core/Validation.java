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

package org.uapart.core;

import java.text.*;

public class Validation {
	private static DecimalFormat TIME_FORMAT = new DecimalFormat("0.00");

	public static void main(String[] args) {
		Domain a = new Domain("a", 3);
		UnaryTable f = new UnaryTable("f", a, a);
		UnaryTable g = new UnaryTable("g", a, a);
		Variables x = new Variables("x", a, 1);

		Term e1 = x.get(0).by(g).by(f).equ(x.get(0));
		Term e2 = x.get(0).by(f).by(g).equ(x.get(0));
		Term t = e1.and(e2).forall(x).print(1, f,g).exists(g).count(f);

		long time = System.currentTimeMillis();

		System.out.println(t.evaluate());

		time = System.currentTimeMillis() - time;
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time)
				+ " seconds.");
	}
}
