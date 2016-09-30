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

public class SMPforMaltsev {
	private static DecimalFormat TIME_FORMAT = new DecimalFormat("0.00");

	static final Algebra<Boolean> BULIN_LOOP = new Algebra<Boolean>(
			BoolAlgebra.INSTANCE, Operation.parse(6, 2,
					"012345 103254 234501 325410 451023 540132"));

	public void checkBulin() {
		CompatibleRels comp = new CompatibleRels(BULIN_LOOP);
		comp.printUniqueRels(1);
		comp.printUniCriticalRels(1);
		comp.printEquivalences();
		comp.printUniCriticalRels(2);
		comp.printUniCriticalRels(3);
		// comp.printUniCriticalRels(4); // there are 4
	}
	
	public static void main(String[] args) {
		SatSolver.setDefault("jni-cominisatps");
		long time = System.currentTimeMillis();
		SMPforMaltsev test = new SMPforMaltsev();

		test.checkBulin();
		
		time = System.currentTimeMillis() - time;
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time)
				+ " seconds.");
	}
}
