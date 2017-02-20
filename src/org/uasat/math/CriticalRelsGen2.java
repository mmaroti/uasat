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

public class CriticalRelsGen2 {
	private final int size;
	private final int arity;

	private final MeetClosedRels relations;
	private final SatSolver<?> solver;

	public boolean trace = false;
	public int totalSteps = 0;

	public CriticalRelsGen2(int size, int arity) {
		this(size, arity, SatSolver.getDefault());
	}

	public CriticalRelsGen2(int size, int arity, SatSolver<?> solver) {
		assert size >= 0 && arity >= 1;

		this.size = size;
		this.arity = arity;

		this.relations = new MeetClosedRels(size, arity);
		this.solver = solver;
	}

	public int getSize() {
		return size;
	}

	public int getArity() {
		return arity;
	}

	public List<Relation<Boolean>> getMeetIrreds() {
		return relations.getMeetIrreds();
	}

	public List<Relation<Boolean>> getUniCriticals() {
		return relations.getUniCriticals();
	}

	public List<Relation<Boolean>> getFullCriticals() {
		return relations.getFullCriticals();
	}

	public void addRelation(Relation<Boolean> rel) {
		assert rel.getSize() == size && rel.getArity() <= arity;
		relations.addPermutedGen(rel);
	}

	public void addRelations(Iterable<Relation<Boolean>> rels) {
		for (Relation<Boolean> rel : rels)
			addRelation(rel);
	}

	private <BOOL> BOOL isSubClosed1(Relation<BOOL> rel) {
		assert rel.getSize() == size && rel.getArity() == arity;
		BoolAlgebra<BOOL> alg = rel.getAlg();
		BOOL b = alg.TRUE;

		Relation<Boolean> sel = Relation.empty(size, arity);
		Tensor<Boolean> tensor = sel.getTensor();
		int count = tensor.getElemCount();

		for (int i = 0; i < count; i++) {
			tensor.setElemAt(i, Boolean.TRUE);

			Relation<Boolean> gen = relations.getClosure2(sel);
			gen = gen.subtract(sel);

			if (!gen.isEmpty()) {
				BOOL c = Relation.lift(alg, sel).isSubsetOf(rel);
				BOOL d = Relation.lift(alg, gen).isSubsetOf(rel);
				b = alg.and(b, alg.leq(c, d));
			}

			tensor.setElemAt(i, Boolean.FALSE);
		}

		return b;
	}

	private <BOOL> BOOL isSubClosed2(Relation<BOOL> rel) {
		assert rel.getSize() == size && rel.getArity() == arity;
		BoolAlgebra<BOOL> alg = rel.getAlg();
		BOOL b = alg.TRUE;

		Relation<Boolean> sel = Relation.empty(size, arity);
		Tensor<Boolean> tensor = sel.getTensor();
		int count = tensor.getElemCount();

		for (int i = 0; i < count; i++) {
			tensor.setElemAt(i, Boolean.TRUE);
			for (int j = i; j < count; j++) {
				tensor.setElemAt(j, Boolean.TRUE);

				Relation<Boolean> gen = relations.getClosure2(sel);
				gen = gen.subtract(sel);

				if (!gen.isEmpty()) {
					BOOL c = Relation.lift(alg, sel).isSubsetOf(rel);
					BOOL d = Relation.lift(alg, gen).isSubsetOf(rel);
					b = alg.and(b, alg.leq(c, d));
				}

				if (j != i)
					tensor.setElemAt(j, Boolean.FALSE);
			}
			tensor.setElemAt(i, Boolean.FALSE);
		}

		return b;
	}

	private <BOOL> BOOL isSubClosed3(Relation<BOOL> rel) {
		assert rel.getSize() == size && rel.getArity() == arity;
		BoolAlgebra<BOOL> alg = rel.getAlg();
		BOOL b = alg.TRUE;

		Relation<Boolean> sel = Relation.empty(size, arity);
		Tensor<Boolean> tensor = sel.getTensor();
		int count = tensor.getElemCount();

		for (int i = 0; i < count; i++) {
			tensor.setElemAt(i, Boolean.TRUE);
			for (int j = i; j < count; j++) {
				tensor.setElemAt(j, Boolean.TRUE);
				for (int k = j; k < count; k++) {
					tensor.setElemAt(k, Boolean.TRUE);

					Relation<Boolean> gen = relations.getClosure2(sel);
					gen = gen.subtract(sel);

					if (!gen.isEmpty()) {
						BOOL c = Relation.lift(alg, sel).isSubsetOf(rel);
						BOOL d = Relation.lift(alg, gen).isSubsetOf(rel);
						b = alg.and(b, alg.leq(c, d));
					}

					if (k != j)
						tensor.setElemAt(k, Boolean.FALSE);
				}
				if (j != i)
					tensor.setElemAt(j, Boolean.FALSE);
			}
			tensor.setElemAt(i, Boolean.FALSE);
		}

		return b;
	}

	public <BOOL> BOOL isSubClosed(Relation<BOOL> rel, int subArity) {
		if (subArity == 1)
			return isSubClosed1(rel);
		else if (subArity == 2)
			return isSubClosed2(rel);
		else if (subArity == 3)
			return isSubClosed3(rel);
		else
			throw new IllegalArgumentException();
	}

	private Relation<Boolean> findOne(final Relation<Boolean> above,
			final int subArity) {
		assert above == null
				|| (above.getSize() == size && above.getArity() == arity);

		SatProblem problem = new SatProblem(Util.createShape(size, arity)) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> rel = new Relation<BOOL>(alg, tensors.get(0));

				BOOL b = alg.not(relations.isGenerated(rel));
				if (above != null) {
					Relation<BOOL> ab = Relation.lift(alg, above);
					b = alg.and(b, ab.isProperSubsetOf(rel));
				}

				b = alg.and(b, isSubClosed(rel, subArity));

				return b;
			}
		};

		totalSteps += 1;
		List<Tensor<Boolean>> sol = problem.solveOne(solver);
		if (sol == null)
			return null;

		return Relation.wrap(sol.get(0));
	}

	public void generate(int subArity) {
		for (;;) {
			Relation<Boolean> rel = findOne(null, subArity);
			if (rel == null)
				break;

			for (;;) {
				Relation<Boolean> r = findOne(rel, subArity);

				if (r == null)
					break;
				else
					rel = r;
			}

			relations.addPermutedGen(rel);
			relations.removeMeetReducibles();

			if (trace)
				System.out.println("found (" + relations.getGeneratorCount()
						+ "):\t" + Relation.format(rel));
		}

		if (trace)
			System.out.println();
	}

	public Operation<Boolean> findOperation(int opArity,
			final List<Relation<Boolean>> compatibles) {

		SatProblem problem = new SatProblem(Util.createShape(size, opArity + 1)) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Operation<BOOL> op = new Operation<BOOL>(alg, tensors.get(0));
				BOOL b = op.isOperation();

				for (Relation<Boolean> rel : compatibles) {
					Relation<BOOL> r = Relation.lift(alg, rel);
					b = alg.and(b, op.preserves(r));
				}

				BOOL c = alg.TRUE;
				List<Relation<Boolean>> irreds = relations.getMeetIrreds();
				for (Relation<Boolean> rel : irreds) {
					Relation<BOOL> r = Relation.lift(alg, rel);
					c = alg.and(c, op.preserves(r));
				}

				return alg.and(b, alg.not(c));
			}
		};

		List<Tensor<Boolean>> sol = problem.solveOne(solver);
		if (sol == null)
			return null;

		return Operation.wrap(sol.get(0));
	}

	public void printMeetIrreds() {
		Relation.print("meet irreducible rels of arity " + arity,
				getMeetIrreds());
	}

	public void printUniCriticals() {
		Relation.print("unique critical rels of arity " + arity,
				getUniCriticals());
	}

	public void printStats() {
		System.out.println("total steps: " + totalSteps + ", meet irreds: "
				+ getMeetIrreds().size() + ", uniqie criticals: "
				+ getUniCriticals().size());
	}
}
