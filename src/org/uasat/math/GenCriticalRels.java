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

	public Set<Relation<Boolean>> getGenerators() {
		return generators;
	}

	public void addGeneratorRel(Relation<Boolean> rel) {
		assert rel.getSize() == size && rel.getArity() <= arity;

		if (rel.getArity() < arity) {
			int a = arity - rel.getArity();
			rel = rel.cartesian(Relation.full(rel.getSize(), a));
		}

		for (Permutation<Boolean> p : permutations)
			generators.add(rel.permute(p));
	}

	public void addSingletonRels() {
		for (int i = 0; i < size; i++)
			addGeneratorRel(Relation.singleton(size, i));
	}

	public void addGeneratorRels(Iterable<Relation<Boolean>> rels) {
		for (Relation<Boolean> rel : rels)
			addGeneratorRel(rel);
	}

	public void addGeneratorComp(Relation<Boolean> rel) {
		addGeneratorRel(rel.complement());
	}

	private <BOOL> Relation<BOOL> getMeetProj(Relation<BOOL> mask, int proj) {
		assert mask.getArity() == 1 && mask.getSize() == generators.size();

		BoolAlgebra<BOOL> alg = mask.getAlg();
		Relation<BOOL> rel = Relation.constant(alg, size, arity, alg.TRUE);

		int pos = 0;
		Iterator<Relation<Boolean>> iter = generators.iterator();
		while (iter.hasNext()) {
			Relation<BOOL> r = Relation.constant(alg, size, arity,
					mask.getValue(pos++));
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
				Relation<BOOL> mask = new Relation<BOOL>(alg, tensors.get(0));
				Relation<BOOL> rel = getMeetProj(mask, proj);

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

		return getMeetProj(Relation.wrap(sol.get(0)), proj);
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
				Relation<BOOL> mask = new Relation<BOOL>(alg, tensors.get(0));
				Relation<BOOL> rel1 = getMeetProj(mask, proj);
				Relation<BOOL> rel2 = getMeetCover(found, rel1);

				return alg.not(rel2.isSubsetOf(rel1));
			}
		};

		List<Permutation<Boolean>> perms = Permutation.symmetricGroup(proj);

		for (;;) {
			List<Tensor<Boolean>> sol = problem.solveOne(solver);
			if (sol == null)
				break;

			Relation<Boolean> above = getMeetProj(Relation.wrap(sol.get(0)),
					proj);
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

	private static <ELEM> boolean haveSameElems(List<ELEM> list1,
			List<ELEM> list2) {
		if (list1.size() != list2.size())
			return false;

		List<ELEM> list = new ArrayList<ELEM>(list1);
		list.removeAll(list2);

		return list.isEmpty();
	}

	public static List<Relation<Boolean>> genMeetIrredRels(
			List<Relation<Boolean>> gens, int full, int proj) {
		assert gens.size() >= 1 && full >= proj && proj >= 1;

		for (;;) {
			GenCriticalRels gen = new GenCriticalRels(gens.get(0).getSize(),
					full);
			gen.addGeneratorRels(gens);

			List<Relation<Boolean>> irreds = gen.findMeetIrredRels(proj);
			if (haveSameElems(gens, irreds))
				return irreds;

			gens = irreds;
		}
	}

	public static List<Relation<Boolean>> genCriticalRels(
			List<Relation<Boolean>> gens, int full, int proj) {
		List<Relation<Boolean>> list = genMeetIrredRels(gens, full, proj);

		ListIterator<Relation<Boolean>> iter = list.listIterator();
		while (iter.hasNext()) {
			Relation<Boolean> r = iter.next();
			if (!r.hasEssentialCoords())
				iter.remove();
		}

		return list;
	}

	public static List<Relation<Boolean>> genUniCriticalRels(
			List<Relation<Boolean>> gens, int full, int proj) {
		List<Relation<Boolean>> list = genCriticalRels(gens, full, proj);
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

	private void printGenerator(Relation<Boolean> gen) {
		List<Integer> coords = new ArrayList<Integer>();
		for (int i = 0; i < gen.getArity(); i++) {
			if (gen.isEssentialCoord(i))
				coords.add(i);
		}

		int[] c = new int[coords.size()];
		for (int i = 0; i < coords.size(); i++)
			c[i] = coords.get(i);

		Relation<Boolean> p = gen.project(c).complement();

		System.out.println(Relation.format(p) + " complement at "
				+ Util.formatTuple(gen.getArity(), c));
	}

	public List<Relation<Boolean>> findRepresentation(
			final Relation<Boolean> rel) {
		assert rel.getArity() <= arity;

		SatProblem problem = new SatProblem(new int[] { generators.size() }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> mask = new Relation<BOOL>(alg, tensors.get(0));
				Relation<BOOL> rel2 = getMeetProj(mask, rel.getArity());
				return rel2.isEqualTo(Relation.lift(alg, rel));
			}
		};

		List<Tensor<Boolean>> sol = problem.solveOne(solver);
		if (sol == null)
			return null;

		Relation<Boolean> mask = Relation.wrap(sol.get(0));

		for (;;) {
			final Relation<Boolean> m0 = mask;
			problem = new SatProblem(new int[] { generators.size() }) {
				@Override
				public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
						List<Tensor<BOOL>> tensors) {
					Relation<BOOL> m1 = Relation.lift(alg, m0);
					Relation<BOOL> mask2 = new Relation<BOOL>(alg,
							tensors.get(0));
					Relation<BOOL> rel2 = getMeetProj(mask2, rel.getArity());

					BOOL b = rel2.isEqualTo(Relation.lift(alg, rel));

					b = alg.and(b, m1.isSubsetOf(mask2));
					b = alg.and(b, alg.not(mask2.isSubsetOf(m1)));

					return b;
				}
			};

			sol = problem.solveOne(solver);
			if (sol == null)
				break;

			mask = Relation.wrap(sol.get(0));
		}

		List<Relation<Boolean>> rep = new ArrayList<Relation<Boolean>>();

		int pos = 0;
		Iterator<Relation<Boolean>> iter = generators.iterator();
		while (iter.hasNext()) {
			Relation<Boolean> r = iter.next();
			if (!mask.getValue(pos++))
				rep.add(r);
		}

		return rep;
	}

	public void printCompRepresentation(final Relation<Boolean> comp) {
		List<Relation<Boolean>> reps = findRepresentation(comp.complement());
		if (reps == null)
			System.out.println("not representable " + Relation.format(comp)
					+ " complement");
		else {
			System.out.println("representation of " + Relation.format(comp)
					+ " complement:");
			for (int i = 0; i < reps.size(); i++)
				printGenerator(reps.get(i));
		}
		System.out.println();
	}

	public void printMeetIrredRels(int proj) {
		Relation.print("meet irreducible rels of arity " + proj
				+ " projected from " + arity, findMeetIrredRels(proj));
	}

	public void printCriticalRels(int proj) {
		Relation.print("critical rels of arity " + proj + " projected from "
				+ arity, findCriticalRels(proj));
	}

	public void printCriticalComps(int proj) {
		List<Relation<Boolean>> list = findCriticalRels(proj);

		ListIterator<Relation<Boolean>> iter = list.listIterator();
		while (iter.hasNext())
			iter.set(iter.next().complement());

		Relation.print("critical complements of arity " + proj
				+ " projected from " + arity, list);
	}

	public void printUniCriticalRels(int proj) {
		Relation.print("unique critical rels of arity " + proj
				+ " projected from " + arity, findUniCriticalRels(proj));
	}

	public void printUniCriticalComps(int proj) {
		List<Relation<Boolean>> list = findUniCriticalRels(proj);

		ListIterator<Relation<Boolean>> iter = list.listIterator();
		while (iter.hasNext())
			iter.set(iter.next().complement());

		Relation.print("unique critical complements of arity " + proj
				+ " projected from " + arity, list);
	}
}
