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

	public static List<Structure<Boolean>> getSDMeetFailure(int size) {
		final List<Permutation<Boolean>> perms = Permutation
				.nontrivialPerms(size);

		int[] rshape = new int[] { size, size };
		SatProblem problem = new SatProblem(rshape, rshape, rshape) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> a = new Relation<BOOL>(alg, tensors.get(0));
				Relation<BOOL> b = new Relation<BOOL>(alg, tensors.get(1));
				Relation<BOOL> c = new Relation<BOOL>(alg, tensors.get(2));

				BOOL r = a.isQuasiOrder();
				r = alg.and(r, b.isQuasiOrder());
				r = alg.and(r, c.isQuasiOrder());

				Relation<BOOL> d = a.intersect(b);
				r = alg.and(r, d.isAntiSymmetric());

				r = alg.and(r, d.isSubsetOf(c));
				r = alg.and(r, a.intersect(c).isSubsetOf(b));

				Relation<BOOL> e = b.compose(c).compose(b).compose(c);
				r = alg.and(r, alg.not(a.intersect(e).isSubsetOf(b)));

				r = alg.and(r, b.isLexLeq(c));
				for (Permutation<Boolean> p : perms) {
					Permutation<BOOL> q = Permutation.lift(alg, p);
					r = alg.and(r, a.isLexLeq(a.conjugate(q)));
				}

				return r;
			}
		};

		List<Tensor<Boolean>> sol = problem.solveAll(SatSolver.getDefault());
		List<Relation<Boolean>> as = Relation.wrap(Tensor.unstack(sol.get(0)));
		List<Relation<Boolean>> bs = Relation.wrap(Tensor.unstack(sol.get(1)));
		List<Relation<Boolean>> cs = Relation.wrap(Tensor.unstack(sol.get(2)));
		assert as.size() == bs.size() && as.size() == cs.size();

		List<Structure<Boolean>> list = new ArrayList<Structure<Boolean>>();
		for (int i = 0; i < as.size(); i++) {
			Structure<Boolean> s = Structure.wrap(as.get(i), bs.get(i),
					cs.get(i));
			Structure.print(s);
			list.add(s);
		}

		return list;
	}

	public static void main2(String[] args) {
		List<?> list = getSDMeetFailure(4);
		System.out.println("Total: " + list.size());
	}

	public static void main(String[] args) {
		long time = System.currentTimeMillis();

		int size = 5;
		final List<Permutation<Boolean>> perms = Permutation
				.nontrivialPerms(size);

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

				r = alg.and(r, b.isLexLeq(c));
				for (Permutation<Boolean> p : perms) {
					Permutation<BOOL> q = Permutation.lift(alg, p);
					r = alg.and(r, a.isLexLeq(a.conjugate(q)));
				}

				return r;
			}
		};

		// SatSolver<?> solver = new Sat4J();
		// SatSolver<?> solver = new MiniSat();
		SatSolver<?> solver = new JniSat("minisat");
		List<Tensor<Boolean>> tensors = problem.solveOne(solver);
		time = System.currentTimeMillis() - time;

		if (tensors == null) {
			System.out.println("no solution");
		} else {

			for (int i = 0; i < 3; i++)
				Relation.format(Relation.wrap(tensors.get(i)));

			for (int i = 3; i < tensors.size(); i++)
				Operation.format(Operation.wrap(tensors.get(i)));
		}

		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time)
				+ " seconds.");
	}
}
