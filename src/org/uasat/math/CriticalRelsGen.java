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

public class CriticalRelsGen {
	private final int size;
	private final int arity1;
	private final int arity2;
	private MeetClosedRels relations1;
	private MeetClosedRels relations2;
	private final SatSolver<?> solver;

	public boolean trace = false;
	public int totalSteps = 0;

	public CriticalRelsGen(int size, int arity1, int arity2) {
		this(size, arity1, arity2, SatSolver.getDefault());
	}

	public CriticalRelsGen(int size, int arity1, int arity2,
			SatSolver<?> solver) {
		assert 1 <= size && 1 <= arity1 && arity1 <= arity2 && solver != null;

		this.size = size;
		this.arity1 = arity1;
		this.arity2 = arity2;
		this.relations1 = new MeetClosedRels(size, arity1);
		this.relations2 = new MeetClosedRels(size, arity2);
		this.solver = solver;
	}

	public SatSolver<?> getSolver() {
		return solver;
	}

	public int getSize() {
		return size;
	}

	public int getArity1() {
		return arity1;
	}

	public int getArity2() {
		return arity2;
	}

	public void addGenerator(Relation<Boolean> rel) {
		assert rel.getSize() == size && rel.getArity() <= arity2;

		if (rel.getArity() <= arity1) {
			relations1.addPermutedGen(rel);
			relations1.removeMeetReducibles();
		}

		relations2.addPermutedGen(rel);
		relations2.removeMeetReducibles();
	}

	public void addSingletons() {
		for (int i = 0; i < size; i++)
			addGenerator(Relation.singleton(size, i));
	}

	public void addGenerators(Iterable<Relation<Boolean>> rels) {
		for (Relation<Boolean> rel : rels)
			addGenerator(rel);
	}

	private Relation<Boolean> findOne(final Relation<Boolean> above) {
		assert above == null
				|| (above.getSize() == size && above.getArity() == arity1);

		SatProblem problem = new SatProblem(Util.createShape(size, arity2)) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> rel2 = new Relation<BOOL>(alg, tensors.get(0));
				Relation<BOOL> rel1 = rel2.projectTail(arity1);

				BOOL b = alg.not(relations1.isClosed(rel1));
				b = alg.and(b, relations2.isClosed(rel2));
				if (above != null)
					b = alg.and(b,
							Relation.lift(alg, above).isProperSubsetOf(rel1));

				return b;
			}
		};

		totalSteps += 1;
		List<Tensor<Boolean>> sol = problem.solveOne(solver);
		if (sol == null)
			return null;

		Relation<Boolean> full = Relation.wrap(sol.get(0));
		return full.projectTail(arity1);
	}

	public void generate1() {
		for (;;) {
			Relation<Boolean> rel = findOne(null);
			if (rel == null)
				break;

			for (;;) {
				Relation<Boolean> r = findOne(rel);

				if (r == null)
					break;
				else
					rel = r;
			}

			relations1.addPermutedGen(rel);
			relations1.removeMeetReducibles();

			if (trace)
				System.out.println("found (" + relations1.getGeneratorCount()
						+ "):\t" + Relation.format(rel));
		}
	}

	public void generate2() {
		for (;;) {
			Relation<Boolean> rel = findOne(null);
			if (rel == null)
				break;

			for (;;) {
				Relation<Boolean> r = findOne(rel);

				if (r == null)
					break;
				else
					rel = r;
			}

			relations1.addPermutedGen(rel);
			relations1.removeMeetReducibles();

			if (trace)
				System.out.println("found (" + relations1.getGeneratorCount()
						+ "," + relations2.getGeneratorCount() + "):\t"
						+ Relation.format(rel));

			relations2.addPermutedGen(rel);
			relations2.removeMeetReducibles();
		}
	}

	public List<Relation<Boolean>> getMeetIrreds1() {
		return relations1.getMeetIrreds();
	}

	public List<Relation<Boolean>> getMeetIrreds2() {
		return relations2.getMeetIrreds();
	}

	public List<Relation<Boolean>> getUniCriticals1() {
		return relations1.getUniCriticals();
	}

	public List<Relation<Boolean>> getUniCriticals2() {
		return relations2.getUniCriticals();
	}

	public void printMeetIrreds1() {
		Relation.print("meet irreducible rels of arity " + arity1,
				getMeetIrreds1());
	}

	public void printMeetIrreds2() {
		Relation.print("meet irreducible rels of arity " + arity2,
				getMeetIrreds2());
	}

	public void printUniCriticals1() {
		Relation.print("unique critical rels of arity " + arity1,
				getUniCriticals1());
	}

	public void printUniCriticals2() {
		Relation.print("unique critical rels of arity " + arity2,
				getUniCriticals2());
	}

	public void printStats() {
		System.out.println("total steps: " + totalSteps + ", meet irreds1: "
				+ getMeetIrreds1().size() + ", meet irreds2: "
				+ getMeetIrreds2().size());
	}

	private Relation<Boolean> findMask(final Relation<Boolean> rel,
			final Relation<Boolean> below) {
		assert rel.getSize() == size && rel.getArity() == arity1;
		assert below == null
				|| (below.getArity() == 1 && below.getSize() == relations2
						.getGeneratorCount());

		SatProblem problem = new SatProblem(
				new int[] { relations2.getGeneratorCount() }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> mask = new Relation<BOOL>(alg, tensors.get(0));
				BOOL b = relations2.isUpsetMask(mask);
				if (below != null)
					b = alg.and(b,
							mask.isProperSubsetOf(Relation.lift(alg, below)));

				Relation<BOOL> rel1 = relations2.getClosureFromMask(mask)
						.projectHead(arity1);
				b = alg.and(b, rel1.isEqualTo(Relation.lift(alg, rel)));

				return b;
			}
		};

		totalSteps += 1;
		List<Tensor<Boolean>> sol = problem.solveOne(solver);
		if (sol == null)
			return null;

		return Relation.wrap(sol.get(0));
	}

	public List<Relation<Boolean>> findRepresentation(Relation<Boolean> rel) {
		assert rel.getSize() == size && rel.getArity() == arity1;

		Relation<Boolean> mask = findMask(rel, null);
		if (mask == null)
			return null;

		for (;;) {
			Relation<Boolean> m = findMask(rel, mask);

			if (m == null)
				break;
			else
				mask = m;
		}

		mask = PartialOrder.minimalElems(relations2.getComparability(), mask);

		List<Relation<Boolean>> list = new ArrayList<Relation<Boolean>>();
		for (int i = 0; i < mask.getSize(); i++)
			if (mask.getValue(i))
				list.add(relations2.getMeetIrreds().get(i));

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

		Relation<Boolean> p = gen.project(c);
		System.out.println("at " + Util.formatTuple(gen.getArity(), c) + ":\t"
				+ Relation.format(p));
	}

	public void printRepresentation(Relation<Boolean> rel) {
		assert rel.getSize() == size && rel.getArity() == arity1;

		List<Relation<Boolean>> reps = findRepresentation(rel);
		if (reps == null)
			System.out.println("not representable " + Relation.format(rel));
		else {
			System.out.println("representation of " + Relation.format(rel)
					+ ":");
			for (int i = 0; i < reps.size(); i++)
				printGenerator(reps.get(i));
		}
		System.out.println();
	}
}
