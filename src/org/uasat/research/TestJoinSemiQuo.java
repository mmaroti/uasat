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

public class TestJoinSemiQuo {
	private static DecimalFormat TIME_FORMAT = new DecimalFormat("0.00");

	public static void main(String[] args) {
		long time = System.currentTimeMillis();

		int size = 6;
		int[] fshape = new int[] { size, size, size, size };
		int[] rshape = new int[] { size, size };

		SatProblem problem = new SatProblem(rshape, rshape, rshape, fshape,
				fshape, fshape) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> a = new Relation<BOOL>(alg, tensors.get(0));
				Relation<BOOL> b = new Relation<BOOL>(alg, tensors.get(1));
				Relation<BOOL> c = new Relation<BOOL>(alg, tensors.get(2));

				List<Operation<BOOL>> ops = new ArrayList<Operation<BOOL>>();
				for (int i = 3; i < tensors.size(); i++)
					ops.add(new Operation<BOOL>(alg, tensors.get(i)));

				BOOL r = a.isQuasiOrder();
				r = alg.and(r, b.isQuasiOrder());
				r = alg.and(r, c.isQuasiOrder());

				r = alg.and(r, a.intersect(b).isSubsetOf(c));
				r = alg.and(r, a.intersect(c).isSubsetOf(b));

				Relation<BOOL> d = b.compose(c).compose(b).compose(c);
				r = alg.and(r, alg.not(a.intersect(d).isSubsetOf(b)));

				for (Operation<BOOL> op : ops) {
					r = alg.and(r, op.isOperation());
					r = alg.and(r, op.preserves(a));
					r = alg.and(r, op.preserves(b));
					r = alg.and(r, op.preserves(c));
				}

				r = alg.and(r, Operation.areSDJoinTerms(ops));

				return r;
			}
		};

		List<Tensor<Boolean>> tensors = problem.solveOne(new MiniSat());
		if (tensors == null) {
			System.out.println("no solution");
			return;
		}

		for (int i = 0; i < 3; i++)
			Relation.format(Relation.wrap(tensors.get(i)));

		for (int i = 3; i < tensors.size(); i++)
			Operation.format(Operation.wrap(tensors.get(i)));

		time = System.currentTimeMillis() - time;
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time)
				+ " seconds.");
	}
}
