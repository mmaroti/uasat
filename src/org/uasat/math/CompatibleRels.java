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

package org.uasat.math;

import java.util.*;

import org.uasat.core.*;

public class CompatibleRels {
	private final Algebra<Boolean> algebra;
	private final SatSolver<?> solver;

	public CompatibleRels(Algebra<Boolean> algebra) {
		this(algebra, SatSolver.getDefault());
	}

	public CompatibleRels(Algebra<Boolean> algebra, SatSolver<?> solver) {
		assert algebra != null && solver != null;

		this.algebra = algebra;
		this.solver = solver;
	}

	public Algebra<Boolean> getAlgebra() {
		return algebra;
	}

	public SatSolver<?> getSolver() {
		return solver;
	}

	public List<Relation<Boolean>> findAllRels(int arity, int limit) {
		SatProblem problem = new SatProblem(Util.createShape(algebra.getSize(),
				arity)) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> rel = new Relation<BOOL>(alg, tensors.get(0));
				Algebra<BOOL> ualg = Algebra.lift(alg, algebra);

				return ualg.isSubuniverse(rel);
			}
		};

		Tensor<Boolean> sol = problem.solveAll(solver, limit).get(0);

		List<Relation<Boolean>> list = new ArrayList<Relation<Boolean>>();
		for (Tensor<Boolean> t : Tensor.unstack(sol))
			list.add(Relation.wrap(t));

		return list;
	}

	public List<Relation<Boolean>> findEquivalences(int limit) {
		SatProblem problem = new SatProblem(Util.createShape(algebra.getSize(),
				2)) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> rel = new Relation<BOOL>(alg, tensors.get(0));
				Algebra<BOOL> ualg = Algebra.lift(alg, algebra);

				BOOL b = rel.isEquivalence();
				return alg.and(b, ualg.isSubuniverse(rel));
			}
		};

		Tensor<Boolean> sol = problem.solveAll(solver, limit).get(0);

		List<Relation<Boolean>> list = new ArrayList<Relation<Boolean>>();
		for (Tensor<Boolean> t : Tensor.unstack(sol))
			list.add(Relation.wrap(t));

		return list;
	}

	public List<Relation<Boolean>> findUniqueRels(int arity, int limit) {
		SatProblem problem = new SatProblem(Util.createShape(algebra.getSize(),
				arity)) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> rel = new Relation<BOOL>(alg, tensors.get(0));
				Algebra<BOOL> ualg = Algebra.lift(alg, algebra);

				BOOL b = ualg.isSubuniverse(rel);
				b = alg.and(b, rel.isLexMinimal());

				return b;
			}
		};

		Tensor<Boolean> sol = problem.solveAll(solver, limit).get(0);

		List<Relation<Boolean>> list = new ArrayList<Relation<Boolean>>();
		for (Tensor<Boolean> t : Tensor.unstack(sol))
			list.add(Relation.wrap(t));

		return list;
	}

	public Relation<Boolean> findSmallestRel(int arity) {
		if (!algebra.hasConstants())
			return Relation.empty(algebra.getSize(), arity);

		List<Relation<Boolean>> list = findMinimalRels(Relation.empty(
				algebra.getSize(), 1));

		if (list.size() != 1)
			throw new IllegalStateException("this cannot happen");

		return list.get(0).diagonal(arity);
	}

	public Relation<Boolean> findOneRel(final Relation<Boolean> above,
			final Relation<Boolean> below,
			final List<Relation<Boolean>> notabove,
			final List<Relation<Boolean>> notbelow) {
		final int arity = (above != null ? above : below).getArity();

		assert above == null || above.getSize() == algebra.getSize();
		assert above == null || above.getArity() == arity;
		assert below == null || below.getSize() == algebra.getSize();
		assert below == null || below.getArity() == arity;

		SatProblem problem = new SatProblem(Util.createShape(algebra.getSize(),
				arity)) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> rel = new Relation<BOOL>(alg, tensors.get(0));
				Algebra<BOOL> ua = Algebra.lift(alg, algebra);

				BOOL b = ua.isSubuniverse(rel);

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
						assert algebra.getSize() == r.getSize()
								&& arity == r.getArity();
						rel2 = Relation.lift(alg, r);
						b = alg.and(b, alg.not(rel2.isSubsetOf(rel)));
					}

				if (notbelow != null)
					for (Relation<Boolean> r : notbelow) {
						assert algebra.getSize() == r.getSize()
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

	public List<Relation<Boolean>> findMaximalRels(final Relation<Boolean> below) {
		assert algebra.getSize() == below.getSize();

		List<Relation<Boolean>> list = new ArrayList<Relation<Boolean>>();
		for (;;) {
			Relation<Boolean> rel = findOneRel(null, below, null, list);
			if (rel == null)
				break;

			for (;;) {
				Relation<Boolean> r = findOneRel(rel, below, null, list);
				if (r == null)
					break;
				else
					rel = r;
			}
			list.add(rel);
		}

		return list;
	}

	public List<Relation<Boolean>> findMaximalRels(int arity) {
		return findMaximalRels(Relation.full(algebra.getSize(), arity));
	}

	public List<Relation<Boolean>> findMinimalRels(final Relation<Boolean> above) {
		assert algebra.getSize() == above.getSize();

		List<Relation<Boolean>> list = new ArrayList<Relation<Boolean>>();
		for (;;) {
			Relation<Boolean> rel = findOneRel(above, null, list, null);
			if (rel == null)
				break;

			for (;;) {
				Relation<Boolean> r = findOneRel(above, rel, list, null);
				if (r == null)
					break;
				else
					rel = r;
			}
			list.add(rel);
		}

		return list;
	}

	public List<Relation<Boolean>> findMinimalRels(int arity) {
		Relation<Boolean> rel = findSmallestRel(arity);
		return findMinimalRels(rel);
	}

	public List<Relation<Boolean>> findMeetIrredRels(int arity) {
		assert arity >= 1;

		final List<Permutation<Boolean>> perms = Permutation
				.symmetricGroup(arity);
		final Set<Relation<Boolean>> rels = new TreeSet<Relation<Boolean>>(
				Relation.COMPARATOR);

		int[] shape = Util.createShape(algebra.getSize(), arity);
		SatProblem problem = new SatProblem(shape, shape) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> rel1 = new Relation<BOOL>(alg, tensors.get(0));
				Relation<BOOL> rel2 = new Relation<BOOL>(alg, tensors.get(1));
				Algebra<BOOL> ua = Algebra.lift(alg, algebra);

				BOOL b = ua.isSubuniverse(rel1);
				b = alg.and(b, ua.isSubuniverse(rel2));
				b = alg.and(b, rel1.isSubsetOf(rel2));
				b = alg.and(b, alg.not(rel2.isSubsetOf(rel1)));

				for (Relation<Boolean> r : rels) {
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
				Relation<Boolean> r = findOneRel(above, null, notabove, null);

				if (r == null)
					break;
				else
					above = r;
			}

			for (Permutation<Boolean> p : perms)
				rels.add(above.permute(p));
		}

		return new ArrayList<Relation<Boolean>>(rels);
	}

	public List<Relation<Boolean>> findCriticalRels(int arity) {
		List<Relation<Boolean>> list = findMeetIrredRels(arity);

		ListIterator<Relation<Boolean>> iter = list.listIterator();
		while (iter.hasNext()) {
			Relation<Boolean> r = iter.next();
			if (!r.hasEssentialCoords())
				iter.remove();
		}

		return list;
	}

	public List<Relation<Boolean>> findUniCriticalRels(int arity) {
		List<Relation<Boolean>> list = findCriticalRels(arity);
		List<Permutation<Boolean>> perms = Permutation.nontrivialPerms(arity);

		ListIterator<Relation<Boolean>> iter = list.listIterator();
		outer: while (iter.hasNext()) {
			Relation<Boolean> r = iter.next();
			for (Permutation<Boolean> p : perms) {
				if (r.permute(p).isLexLess(r)) {
					iter.remove();
					continue outer;
				}
			}
		}

		return list;
	}

	private static void printRels(String what, int arity,
			List<Relation<Boolean>> list) {
		System.out.println(what + " subpowers of arity " + arity + ": "
				+ list.size());

		Collections.sort(list, Relation.COMPARATOR);
		for (int i = 0; i < list.size(); i++)
			System.out.println(i + ":\t" + Relation.format(list.get(i)));

		System.out.println();
	}

	public void printAllRels(int arity) {
		printRels("all", arity, findAllRels(arity, -1));
	}

	public void printEquivalences() {
		printRels("equivalence", 2, findEquivalences(-1));
	}

	public void printUniqueRels(int arity) {
		printRels("unique", arity, findUniqueRels(arity, -1));
	}

	public void printMaximalRels(int arity) {
		printRels("maximal", arity, findMaximalRels(arity));
	}

	public void printMinimalRels(int arity) {
		printRels("minimal", arity, findMinimalRels(arity));
	}

	public void printMeetIrredRels(int arity) {
		printRels("meet irred", arity, findMeetIrredRels(arity));
	}

	public void printCriticalRels(int arity) {
		printRels("critical", arity, findCriticalRels(arity));
	}

	public void printCriticalComps(int arity) {
		List<Relation<Boolean>> list = findCriticalRels(arity);

		ListIterator<Relation<Boolean>> iter = list.listIterator();
		while (iter.hasNext())
			iter.set(iter.next().complement());

		printRels("critical complement", arity, list);
	}

	public void printUniCriticalRels(int arity) {
		printRels("unique critical", arity, findUniCriticalRels(arity));
	}

	public void printUniCriticalComps(int arity) {
		List<Relation<Boolean>> list = findUniCriticalRels(arity);

		ListIterator<Relation<Boolean>> iter = list.listIterator();
		while (iter.hasNext())
			iter.set(iter.next().complement());

		printRels("unique critical complement", arity, list);
	}
}
