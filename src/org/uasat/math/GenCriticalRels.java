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

package org.uasat.math;

import java.util.*;

import org.uasat.core.*;

public class GenCriticalRels {
	private final int size;
	private final int arity;
	private final SatSolver<?> solver;

	private final List<Permutation<Boolean>> permutations;
	private final TreeSet<Relation<Boolean>> generators;

	public GenCriticalRels(int size, int arity) {
		this(size, arity, SatSolver.getDefault());
	}

	public GenCriticalRels(int size, int arity, SatSolver<?> solver) {
		assert size >= 1 && arity >= 1 && solver != null;

		this.size = size;
		this.arity = arity;
		this.solver = solver;

		permutations = Permutation.symmetricGroup(arity);
		generators = new TreeSet<Relation<Boolean>>(Relation.COMPARATOR);
	}

	public SatSolver<?> getSolver() {
		return solver;
	}

	public int getSize() {
		return size;
	}

	public int getArity() {
		return arity;
	}

	public void addCriticalRel(Relation<Boolean> rel) {
		assert rel.getSize() == size && rel.getArity() <= arity;

		if (rel.getArity() < arity) {
			int a = arity - rel.getArity();
			rel = rel.cartesian(Relation.full(rel.getSize(), a));
		}

		for (Permutation<Boolean> p : permutations)
			generators.add(rel.permute(p));
	}

	private <BOOL> Relation<BOOL> getMeetProj(BoolAlgebra<BOOL> alg,
			Tensor<BOOL> mask, int proj) {
		assert mask.getOrder() == 1 && mask.getDim(0) == generators.size();

		Relation<BOOL> rel = Relation.constant(alg, size, arity, alg.TRUE);

		int pos = 0;
		Iterator<Relation<Boolean>> iter = generators.iterator();
		while (iter.hasNext()) {
			Relation<BOOL> r = Relation.constant(alg, size, arity,
					mask.getElem(pos++));
			rel = rel.intersect(r.union(Relation.lift(alg, iter.next())));
		}

		assert 1 <= proj && proj <= arity;
		if (proj < arity) {
			int[] p = new int[proj];
			for (int i = 0; i < proj; i++)
				p[i] = i;
			rel = rel.project(p);
		}

		return rel;
	}

	private Relation<Boolean> findOneRel(final Relation<Boolean> above,
			final Relation<Boolean> notabove, final int proj) {
		assert above.getSize() == size && above.getArity() == proj;
		assert notabove.getSize() == size && notabove.getArity() == proj;

		SatProblem problem = new SatProblem(new int[] { generators.size() }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> rel = getMeetProj(alg, tensors.get(0), proj);

				Relation<BOOL> rel2 = Relation.lift(alg, above);
				BOOL b = rel2.isSubsetOf(rel);
				b = alg.and(b, alg.not(rel.isSubsetOf(rel2)));

				rel2 = Relation.lift(alg, notabove);
				b = alg.and(b, alg.not(rel2.isSubsetOf(rel)));

				return b;
			}
		};

		List<Tensor<Boolean>> sol = problem.solveOne(solver);
		if (sol == null)
			return null;

		return getMeetProj(BoolAlgebra.INSTANCE, sol.get(0), proj);
	}

	private static <BOOL> Relation<BOOL> getMeetCover(
			Iterable<Relation<Boolean>> irreds, Relation<BOOL> rel) {
		int size = rel.getSize();
		int arity = rel.getArity();

		BoolAlgebra<BOOL> alg = rel.getAlg();
		Relation<BOOL> rel2 = Relation.constant(alg, size, arity, alg.TRUE);

		Iterator<Relation<Boolean>> iter = irreds.iterator();
		while (iter.hasNext()) {
			Relation<BOOL> r = Relation.lift(alg, iter.next());
			assert r.getSize() == size && r.getArity() == arity;

			BOOL b = alg.not(rel.isSubsetOf(r));
			Relation<BOOL> m = Relation.constant(alg, size, arity, b);
			rel2 = rel2.intersect(r.union(m));
		}

		return rel2;
	}

	public List<Relation<Boolean>> findMeetIrredRels(final int proj) {
		assert 1 <= proj && proj <= arity;

		final TreeSet<Relation<Boolean>> found = new TreeSet<Relation<Boolean>>(
				Relation.COMPARATOR);

		SatProblem problem = new SatProblem(new int[] { generators.size() }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> rel1 = getMeetProj(alg, tensors.get(0), proj);
				Relation<BOOL> rel2 = getMeetCover(found, rel1);

				return alg.not(rel2.isSubsetOf(rel1));
			}
		};

		List<Permutation<Boolean>> perms = Permutation.symmetricGroup(proj);

		for (;;) {
			List<Tensor<Boolean>> sol = problem.solveOne(solver);
			if (sol == null)
				break;

			Relation<Boolean> above = getMeetProj(BoolAlgebra.INSTANCE,
					sol.get(0), proj);
			Relation<Boolean> notabove = getMeetCover(found, above);
			assert above.isSubsetOf(notabove) && !notabove.isSubsetOf(above);

			for (;;) {
				Relation<Boolean> r = findOneRel(above, notabove, proj);

				if (r == null)
					break;
				else
					above = r;
			}

			for (Permutation<Boolean> p : perms)
				found.add(above.permute(p));
		}

		return new ArrayList<Relation<Boolean>>(found);
	}

	public List<Relation<Boolean>> findCriticalRels(int proj) {
		List<Relation<Boolean>> list = findMeetIrredRels(proj);

		ListIterator<Relation<Boolean>> iter = list.listIterator();
		while (iter.hasNext()) {
			Relation<Boolean> r = iter.next();
			if (!r.hasEssentialCoords())
				iter.remove();
		}

		return list;
	}

	public List<Relation<Boolean>> findUniCriticalRels(int proj) {
		List<Relation<Boolean>> list = findCriticalRels(proj);
		List<Permutation<Boolean>> perms = Permutation.nontrivialPerms(proj);

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

	private void printRels(String what, int proj, List<Relation<Boolean>> list) {
		System.out.println(what + " generated relations of arity " + proj
				+ " projected from " + arity + ": " + list.size());

		Collections.sort(list, Relation.COMPARATOR);
		for (int i = 0; i < list.size(); i++)
			System.out.println(i + ":\t" + Relation.format(list.get(i)));

		System.out.println();
	}

	public void printMeetIrredRels(int proj) {
		printRels("meet irred", proj, findMeetIrredRels(proj));
	}

	public void printCriticalRels(int proj) {
		printRels("critical", proj, findCriticalRels(proj));
	}

	public void printCriticalComps(int proj) {
		List<Relation<Boolean>> list = findCriticalRels(proj);

		ListIterator<Relation<Boolean>> iter = list.listIterator();
		while (iter.hasNext())
			iter.set(iter.next().complement());

		printRels("critical complement", proj, list);
	}

	public void printUniCriticalRels(int proj) {
		printRels("critical unique", proj, findUniCriticalRels(proj));
	}

	public void printUniCriticalComps(int proj) {
		List<Relation<Boolean>> list = findUniCriticalRels(proj);

		ListIterator<Relation<Boolean>> iter = list.listIterator();
		while (iter.hasNext())
			iter.set(iter.next().complement());

		printRels("critical unique complement", proj, list);
	}
}
