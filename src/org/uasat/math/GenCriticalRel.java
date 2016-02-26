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

public class GenCriticalRel {
	private final int size;
	private final int arity;
	private final int extra;
	private final SatSolver<?> solver;

	private final List<Permutation<Boolean>> fullperms;

	private final TreeSet<Relation<Boolean>> fullirreds;
	private final ArrayList<Relation<Boolean>> projirreds;

	public GenCriticalRel(int size, int arity, int extra) {
		this(size, arity, extra, SatSolver.getDefault());
	}

	public GenCriticalRel(int size, int arity, int extra, SatSolver<?> solver) {
		assert size >= 1 && arity >= 1 && extra >= 1 && solver != null;

		this.size = size;
		this.arity = arity;
		this.extra = extra;
		this.solver = solver;

		fullperms = Permutation.symmetricGroup(arity + extra);

		fullirreds = new TreeSet<Relation<Boolean>>(Relation.COMPARATOR);
		projirreds = new ArrayList<Relation<Boolean>>();
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

	public int getExtra() {
		return extra;
	}

	private static <BOOL> BOOL isCriticalPair(Relation<BOOL> rel1,
			Relation<BOOL> rel2, Iterable<Relation<Boolean>> irreds) {
		BoolAlgebra<BOOL> alg = rel1.getAlg();
		int size = rel1.getSize();
		int arity = rel1.getArity();

		assert rel2.getAlg() == alg && rel2.getSize() == size
				&& rel2.getArity() == arity;

		BOOL b = alg.TRUE;
		for (Relation<Boolean> r1 : irreds) {
			assert r1.getSize() == size && r1.getArity() == arity;
			Relation<BOOL> r2 = Relation.lift(alg, r1);
			BOOL c1 = rel1.isSubsetOf(r2);
			BOOL c2 = rel2.isSubsetOf(r2);
			b = alg.and(b, alg.leq(c1, c2));
		}

		return b;
	}

	public boolean isIrreducibleFull(final Relation<Boolean> rel) {
		assert rel.getSize() == size && rel.getArity() == arity + extra;

		SatProblem problem = new SatProblem(rel.getTensor().getShape()) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> rel1 = Relation.lift(alg, rel);
				Relation<BOOL> rel2 = new Relation<BOOL>(alg, tensors.get(0));

				BOOL b = alg.not(rel2.isSubsetOf(rel1));
				return alg.and(b, isCriticalPair(rel1, rel2, fullirreds));
			}
		};

		List<Tensor<Boolean>> sol = problem.solveOne(solver);
		return sol == null;
	}

	public void addIrreducibleRel(Relation<Boolean> rel) {
		assert rel.getSize() == size && rel.getArity() == arity + extra;

		for (Permutation<Boolean> p : fullperms)
			fullirreds.add(rel.permute(p));
	}
}
