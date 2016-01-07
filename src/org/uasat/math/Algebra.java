/**
 *	Copyright (C) Miklos Maroti, 2015
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

package org.uasat.math;

import java.util.*;

import org.uasat.core.*;
import org.uasat.solvers.*;

public final class Algebra<BOOL> {
	private final BoolAlgebra<BOOL> alg;
	private final int size;
	private final List<Operation<BOOL>> ops;

	public Algebra(BoolAlgebra<BOOL> alg, int size, List<Operation<BOOL>> ops) {
		assert alg != null && !ops.isEmpty();

		for (Operation<BOOL> op : ops)
			assert op.getSize() == size;

		this.alg = alg;
		this.size = size;
		this.ops = ops;
	}

	@SafeVarargs
	public Algebra(BoolAlgebra<BOOL> alg, Operation<BOOL>... ops) {
		assert alg != null && ops.length > 0;

		size = ops[0].getSize();
		for (int i = 1; i < ops.length; i++)
			assert ops[i].getSize() == size;

		this.alg = alg;
		this.ops = Arrays.asList(ops);
	}

	public BoolAlgebra<BOOL> getAlg() {
		return alg;
	}

	public int getSize() {
		return size;
	}

	public BOOL isSubuniverse(Relation<BOOL> rel) {
		assert alg == rel.getAlg() && size == rel.getSize();

		BOOL b = alg.TRUE;
		for (Operation<BOOL> op : ops)
			b = alg.and(b, op.preserves(rel));

		return b;
	}

	public static <BOOL> Algebra<BOOL> lift(BoolAlgebra<BOOL> alg,
			Algebra<Boolean> ua) {
		List<Operation<BOOL>> ops = new ArrayList<Operation<BOOL>>();
		for (Operation<Boolean> op : ua.ops)
			ops.add(Operation.lift(alg, op));

		return new Algebra<BOOL>(alg, ua.getSize(), ops);
	}

	public static Algebra<Boolean> wrap(Operation<Boolean> ops) {
		return new Algebra<Boolean>(BoolAlgebra.INSTANCE, ops);
	}

	public static List<Relation<Boolean>> getSubpowers(SatSolver<?> solver,
			final Algebra<Boolean> ua, int arity) {
		BoolProblem problem = new BoolProblem(Util.createShape(ua.getSize(),
				arity)) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> rel = new Relation<BOOL>(alg, tensors.get(0));
				Algebra<BOOL> ualg = Algebra.lift(alg, ua);

				return ualg.isSubuniverse(rel);
			}
		};

		Tensor<Boolean> sol = problem.solveAll(solver).get(0);

		List<Relation<Boolean>> list = new ArrayList<Relation<Boolean>>();
		for (Tensor<Boolean> t : Tensor.unstack(sol))
			list.add(Relation.wrap(t));

		return list;
	}

	public static Relation<Boolean> findOneSubpower(SatSolver<?> solver,
			final Algebra<Boolean> ua, final Relation<Boolean> above,
			final Relation<Boolean> below,
			final List<Relation<Boolean>> notabove,
			final List<Relation<Boolean>> notbelow) {
		assert ua.getSize() == below.getSize()
				&& above.getArity() == below.getArity();

		int arity = above.getArity();

		for (Relation<Boolean> r : notabove)
			assert ua.getSize() == r.getSize() && arity == r.getArity();

		for (Relation<Boolean> r : notbelow)
			assert ua.getSize() == r.getSize() && arity == r.getArity();

		BoolProblem problem = new BoolProblem(below.getTensor().getShape()) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> rel = new Relation<BOOL>(alg, tensors.get(0));
				Algebra<BOOL> ualg = Algebra.lift(alg, ua);

				BOOL b = ualg.isSubuniverse(rel);
				b = alg.and(b, Relation.lift(alg, above).isSubsetOf(rel));
				b = alg.and(b, rel.isSubsetOf(Relation.lift(alg, below)));

				for (Relation<Boolean> r : notabove)
					b = alg.and(b,
							alg.not(Relation.lift(alg, r).isSubsetOf(rel)));

				for (Relation<Boolean> r : notbelow)
					b = alg.and(b,
							alg.not(rel.isSubsetOf(Relation.lift(alg, r))));

				return b;
			}
		};

		List<Tensor<Boolean>> sol = problem.solveOne(solver);
		if (sol == null)
			return null;

		return Relation.wrap(sol.get(0));
	}

	public static List<Relation<Boolean>> findMaximalSubpowers(
			SatSolver<?> solver, final Algebra<Boolean> ua,
			final Relation<Boolean> below) {
		assert ua.getSize() == below.getSize();

		List<Relation<Boolean>> list = new ArrayList<Relation<Boolean>>();
		for (;;) {
			Relation<Boolean> c = findOneSubpower(solver, ua, null, below,
					null, list);
			if (c == null)
				break;

			list.add(c);
			for (;;) {
				c = findOneSubpower(solver, ua, null, below, null, list);

				if (c != null)
					list.set(list.size() - 1, c);
				else
					break;
			}
		}

		return list;
	}
}
