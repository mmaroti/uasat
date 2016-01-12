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

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		Domain a = new Domain("a", 3);
		UnaryTable f = new UnaryTable("f", a, a);
		UnaryTable g = new UnaryTable("g", a, a);
		Variables x = new Variables("x", a, 1);

		/*
		 * Term t = new ExistsTerm(g, new ExistsTerm(f, new Printer( new
		 * ForAllTerm(x, new BoolEqu(new UnaryTerm(f, new UnaryTerm(g,
		 * x.get(0))), x.get(0))), 1, f, g)));
		 */

		Term e1 = new BoolEqu(new UnaryTerm(f, new UnaryTerm(g, x.get(0))),
				x.get(0));
		Term e2 = new BoolEqu(new UnaryTerm(g, new UnaryTerm(f, x.get(0))),
				x.get(0));

		Term t = new ExistsTerm(f, new ExistsTerm(g, new ForAllTerm(x,
				new Printer(e2, -1, f, g, x))));

		long time = System.currentTimeMillis();

		System.out.println(t.evaluate());

		time = System.currentTimeMillis() - time;
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time)
				+ " seconds.");
	}
}
