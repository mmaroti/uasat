/**
 * Copyright (C) Miklos Maroti, 2015-2016
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
import org.uasat.core.*;
import org.uasat.math.*;
import org.uasat.solvers.*;

public class TestMinimalClones {
	private static DecimalFormat TIME_FORMAT = new DecimalFormat("0.00");

	public static void main(String[] args) {
		SatSolver<?> solver = new MiniSat();
		long time = System.currentTimeMillis();

		MinimalClones clones = new MinimalClones("nontriv3", 2, 3, solver);

		clones.addGenerator(Operation.parse(2, 1, "00"));
		clones.addGenerator(Operation.parse(2, 1, "11"));
		clones.addGenerator(Operation.parse(2, 1, "10"));
		clones.addGenerator(Operation.parse(2, 2, "00 01")); // meet
		clones.addGenerator(Operation.parse(2, 2, "01 11")); // join
		clones.addGenerator(Operation.parse(2, 3, "01 10 10 01")); // x+y+z
		clones.addGenerator(Operation.parse(2, 3, "00 01 01 11")); // majority
		clones.findAll();
		clones.print();

		time = System.currentTimeMillis() - time;
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time) + " seconds.");
	}

	public static void main2(String[] args) {
		SatSolver<?> solver = new MiniSat();
		long time = System.currentTimeMillis();

		MinimalClones clones = new MinimalClones("majority", 3, 2, solver);

		clones.findAll();
		clones.print();

		time = System.currentTimeMillis() - time;
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time) + " seconds.");
	}
}
