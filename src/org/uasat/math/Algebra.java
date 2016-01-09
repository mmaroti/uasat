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

	public BoolAlgebra<BOOL> getAlg() {
		return alg;
	}

	public int getSize() {
		return size;
	}

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

	@SafeVarargs
	final public Algebra<BOOL> extend(Operation<BOOL>... ops) {
		List<Operation<BOOL>> list = new ArrayList<Operation<BOOL>>(this.ops);

		for (int i = 0; i < ops.length; i++) {
			assert ops[i].getSize() == size && ops[i].getAlg() == alg;
			list.add(ops[i]);
		}

		return new Algebra<BOOL>(alg, size, list);
	}

	public BOOL isSubuniverse(Relation<BOOL> rel) {
		assert alg == rel.getAlg() && size == rel.getSize();

		BOOL b = alg.TRUE;
		for (Operation<BOOL> op : ops)
			b = alg.and(b, op.preserves(rel));

		return b;
	}

	public boolean hasConstants() {
		for (Operation<BOOL> op : ops)
			if (op.getArity() == 0)
				return true;

		return false;
	}

	public static <BOOL> Algebra<BOOL> lift(BoolAlgebra<BOOL> alg,
			Algebra<Boolean> ua) {
		List<Operation<BOOL>> ops = new ArrayList<Operation<BOOL>>();
		for (Operation<Boolean> op : ua.ops)
			ops.add(Operation.lift(alg, op));

		return new Algebra<BOOL>(alg, ua.getSize(), ops);
	}

	@SafeVarargs
	public static Algebra<Boolean> wrap(Operation<Boolean>... ops) {
		return new Algebra<Boolean>(BoolAlgebra.INSTANCE, ops);
	}

	public static List<Relation<Boolean>> findAllSubpowers(SatSolver<?> solver,
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

	public static Relation<Boolean> findSmallestSubpower(SatSolver<?> solver,
			Algebra<Boolean> ua, int arity) {
		if (!ua.hasConstants())
			return Relation.empty(ua.getSize(), arity);

		List<Relation<Boolean>> list = findMinimalSubpowers(solver, ua,
				Relation.empty(ua.getSize(), 1));

		if (list.size() != 1)
			throw new IllegalStateException("this cannot happen");

		return list.get(0).diagExtend(arity);
	}

	public static Relation<Boolean> findOneSubpower(SatSolver<?> solver,
			final Algebra<Boolean> ua, final Relation<Boolean> above,
			final Relation<Boolean> below,
			final List<Relation<Boolean>> notabove,
			final List<Relation<Boolean>> notbelow) {
		final int arity = (above != null ? above : below).getArity();

		assert above == null || above.getSize() == ua.getSize();
		assert above == null || above.getArity() == arity;
		assert below == null || below.getSize() == ua.getSize();
		assert below == null || below.getArity() == arity;

		BoolProblem problem = new BoolProblem(Util.createShape(ua.getSize(),
				arity)) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> rel = new Relation<BOOL>(alg, tensors.get(0));
				Algebra<BOOL> ualg = Algebra.lift(alg, ua);

				BOOL b = ualg.isSubuniverse(rel);

				Relation<BOOL> rel2;
				if (above != null) {
					rel2 = Relation.lift(alg, above);
					b = alg.and(b, rel2.isSubsetOf(rel));
					b = alg.and(b, alg.not(rel.isSubsetOf(rel2)));
				}

				if (below != null) {
					rel2 = Relation.lift(alg, below);
					b = alg.and(b, rel.isSubsetOf(rel2));
					b = alg.and(b, alg.not(rel2.isSubsetOf(rel)));
				}

				if (notabove != null)
					for (Relation<Boolean> r : notabove) {
						assert ua.getSize() == r.getSize()
								&& arity == r.getArity();
						rel2 = Relation.lift(alg, r);
						b = alg.and(b, alg.not(rel2.isSubsetOf(rel)));
					}

				if (notbelow != null)
					for (Relation<Boolean> r : notbelow) {
						assert ua.getSize() == r.getSize()
								&& arity == r.getArity();
						rel2 = Relation.lift(alg, r);
						b = alg.and(b, alg.not(rel.isSubsetOf(rel2)));
					}

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
			Relation<Boolean> rel = findOneSubpower(solver, ua, null, below,
					null, list);
			if (rel == null)
				break;

			for (;;) {
				Relation<Boolean> r = findOneSubpower(solver, ua, rel, below,
						null, list);
				if (r == null)
					break;
				else
					rel = r;
			}
			list.add(rel);
		}

		return list;
	}

	public static List<Relation<Boolean>> findMaximalSubpowers(
			SatSolver<?> solver, Algebra<Boolean> ua, int arity) {
		return findMaximalSubpowers(solver, ua,
				Relation.full(ua.getSize(), arity));
	}

	public static List<Relation<Boolean>> findMinimalSubpowers(
			SatSolver<?> solver, final Algebra<Boolean> ua,
			final Relation<Boolean> above) {
		assert ua.getSize() == above.getSize();

		List<Relation<Boolean>> list = new ArrayList<Relation<Boolean>>();
		for (;;) {
			Relation<Boolean> rel = findOneSubpower(solver, ua, above, null,
					list, null);
			if (rel == null)
				break;

			for (;;) {
				Relation<Boolean> r = findOneSubpower(solver, ua, above, rel,
						list, null);
				if (r == null)
					break;
				else
					rel = r;
			}
			list.add(rel);
		}

		return list;
	}

	public static List<Relation<Boolean>> findMinimalSubpowers(
			SatSolver<?> solver, Algebra<Boolean> ua, int arity) {
		Relation<Boolean> rel = findSmallestSubpower(solver, ua, arity);
		return findMinimalSubpowers(solver, ua, rel);
	}

	public static List<Relation<Boolean>> findMeetIrredSubpowers(
			SatSolver<?> solver, final Algebra<Boolean> ua, int arity) {
		assert arity >= 1;

		final List<Relation<Boolean>> list = new ArrayList<Relation<Boolean>>();

		int[] shape = Util.createShape(ua.getSize(), arity);
		BoolProblem problem = new BoolProblem(shape, shape) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> rel1 = new Relation<BOOL>(alg, tensors.get(0));
				Relation<BOOL> rel2 = new Relation<BOOL>(alg, tensors.get(1));
				Algebra<BOOL> ualg = Algebra.lift(alg, ua);

				BOOL b = ualg.isSubuniverse(rel1);
				b = alg.and(b, ualg.isSubuniverse(rel2));
				b = alg.and(b, rel1.isSubsetOf(rel2));
				b = alg.and(b, alg.not(rel2.isSubsetOf(rel1)));

				for (Relation<Boolean> r : list) {
					Relation<BOOL> irr = Relation.lift(alg, r);

					BOOL c = rel1.isSubsetOf(irr);
					c = alg.leq(c, rel2.isSubsetOf(irr));
					b = alg.and(b, c);
				}

				return b;
			}
		};

		for (;;) {
			List<Tensor<Boolean>> sol = problem.solveOne(solver);
			if (sol == null)
				break;

			Relation<Boolean> above = Relation.wrap(sol.get(0));
			Relation<Boolean> cover = Relation.wrap(sol.get(1));

			List<Relation<Boolean>> notabove = new ArrayList<Relation<Boolean>>();
			notabove.add(cover);

			for (;;) {
				Relation<Boolean> r = findOneSubpower(solver, ua, above, null,
						notabove, null);

				if (r == null)
					break;
				else
					above = r;
			}

			list.add(above);
		}

		return list;
	}

	public static List<Relation<Boolean>> findCriticalSubpowers(
			SatSolver<?> solver, final Algebra<Boolean> ua, int arity) {
		List<Relation<Boolean>> list = findMeetIrredSubpowers(solver, ua, arity);
		List<Relation<Boolean>> crit = new ArrayList<Relation<Boolean>>();

		for (Relation<Boolean> r : list)
			if (r.hasEssentialCoords())
				crit.add(r);

		return crit;
	}
}
