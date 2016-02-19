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

public class TestJoinSemiQuo {
	private static DecimalFormat TIME_FORMAT = new DecimalFormat("0.00");

	public static void main(String[] args) {
		long time = System.currentTimeMillis();

		int size = 3;
		int[] fshape = new int[] { size, size, size, size };
		int[] rshape = new int[] { size, size };

		SatProblem problem = new SatProblem(rshape, rshape, rshape, fshape,
				fshape) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> a = new Relation<BOOL>(alg, tensors.get(0));
				Relation<BOOL> b = new Relation<BOOL>(alg, tensors.get(1));
				Relation<BOOL> c = new Relation<BOOL>(alg, tensors.get(2));

				Operation<BOOL> d1 = new Operation<BOOL>(alg, tensors.get(3));
				Operation<BOOL> d2 = new Operation<BOOL>(alg, tensors.get(4));

				BOOL r = a.isQuasiOrder();
				r = alg.and(r, b.isQuasiOrder());
				r = alg.and(r, c.isQuasiOrder());

				Relation<BOOL> m = a.intersect(b);
				r = alg.and(r, m.isEqualTo(a.intersect(c)));
				r = alg.and(
						r,
						alg.not(m.isSubsetOf(a.intersect(b.compose(c)
								.compose(b).compose(c)))));

				r = alg.and(r, d1.isOperation());
				r = alg.and(r, d2.isOperation());

				r = alg.and(r, d1.preserves(a));
				r = alg.and(r, d1.preserves(b));
				r = alg.and(r, d1.preserves(c));

				r = alg.and(r, d2.preserves(a));
				r = alg.and(r, d2.preserves(b));
				r = alg.and(r, d2.preserves(c));
			}
		};

		time = System.currentTimeMillis() - time;
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time)
				+ " seconds.");
	}
}
