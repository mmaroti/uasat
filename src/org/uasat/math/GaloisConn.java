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

public final class GaloisConn<BOOL> {
	private final BoolAlgebra<BOOL> alg;
	private final Tensor<BOOL> tensor;

	public BoolAlgebra<BOOL> getAlg() {
		return alg;
	}

	public Tensor<BOOL> getTensor() {
		return tensor;
	}

	public int getLeftSize() {
		return tensor.getDim(0);
	}

	public int getRightSize() {
		return tensor.getDim(1);
	}

	public GaloisConn(BoolAlgebra<BOOL> alg, Tensor<BOOL> tensor) {
		assert tensor.getOrder() == 2;

		this.alg = alg;
		this.tensor = tensor;
	}

	public static <BOOL> GaloisConn<BOOL> lift(BoolAlgebra<BOOL> alg,
			GaloisConn<Boolean> galois) {
		Tensor<BOOL> tensor = Tensor.map(alg.LIFT, galois.tensor);
		return new GaloisConn<BOOL>(alg, tensor);
	}

	public static GaloisConn<Boolean> wrap(Tensor<Boolean> tensor) {
		return new GaloisConn<Boolean>(BoolAlgebra.INSTANCE, tensor);
	}

	public static <BOOL> GaloisConn<BOOL> compatibility(
			final BoolAlgebra<BOOL> alg, final List<Operation<BOOL>> ops,
			final List<Relation<BOOL>> rels) {
		Tensor<BOOL> t = Tensor.generate(ops.size(), rels.size(),
				new Func2<BOOL, Integer, Integer>() {
					@Override
					public BOOL call(Integer a, Integer b) {
						return ops.get(a).preserves(rels.get(b));
					}
				});
		return new GaloisConn<BOOL>(alg, t);
	}

	public static GaloisConn<Boolean> compatiblity(
			List<Operation<Boolean>> ops, List<Relation<Boolean>> rels) {
		return compatibility(BoolAlgebra.INSTANCE, ops, rels);
	}

	public GaloisConn<BOOL> transpose() {
		int[] s = new int[] { tensor.getDim(1), tensor.getDim(0) };
		return new GaloisConn<BOOL>(alg, Tensor.reshape(tensor, s, MAP10));
	}

	private static int[] MAP0 = new int[] { 0 };
	private static int[] MAP1 = new int[] { 1 };
	private static int[] MAP10 = new int[] { 1, 0 };

	private Tensor<BOOL> rightClosure(Tensor<BOOL> left) {
		Tensor<BOOL> t = Tensor.reshape(left, tensor.getShape(), MAP0);
		return Tensor.fold(alg.ALL, 1, Tensor.map2(alg.LEQ, t, tensor));
	}

	private Tensor<BOOL> leftClosure(Tensor<BOOL> right) {
		Tensor<BOOL> t = Tensor.reshape(right, tensor.getShape(), MAP1);
		t = Tensor.map2(alg.LEQ, t, tensor);
		int[] s = new int[] { tensor.getDim(1), tensor.getDim(0) };
		t = Tensor.reshape(t, s, MAP10);
		return Tensor.fold(alg.ALL, 1, t);
	}

	public Relation<BOOL> leftClosure(Relation<BOOL> right) {
		assert right.getArity() == 1 && right.getSize() == tensor.getDim(1);

		return new Relation<BOOL>(alg, leftClosure(right.getTensor()));
	}

	public Relation<BOOL> rightClosure(Relation<BOOL> left) {
		assert left.getArity() == 1 && left.getSize() == tensor.getDim(0);

		return new Relation<BOOL>(alg, rightClosure(left.getTensor()));
	}

	public BOOL isLeftClosed(Relation<BOOL> left) {
		assert left.getArity() == 1 && left.getSize() == tensor.getDim(0);

		Tensor<BOOL> t = leftClosure(rightClosure(left.getTensor()));
		t = Tensor.map2(alg.EQU, t, left.getTensor());
		return Tensor.fold(alg.ALL, 1, t).get();
	}

	public BOOL isRightClosed(Relation<BOOL> right) {
		assert right.getArity() == 1 && right.getSize() == tensor.getDim(1);

		Tensor<BOOL> t = rightClosure(leftClosure(right.getTensor()));
		t = Tensor.map2(alg.EQU, t, right.getTensor());
		return Tensor.fold(alg.ALL, 1, t).get();
	}

	public static List<Relation<Boolean>> findLeftClosedSets(
			SatSolver<?> solver, final GaloisConn<Boolean> galois, int limit) {
		SatProblem problem = new SatProblem(new int[] { galois.getLeftSize() }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> rel = new Relation<BOOL>(alg, tensors.get(0));
				GaloisConn<BOOL> gal = GaloisConn.lift(alg, galois);
				return gal.isLeftClosed(rel);
			}
		};

		Tensor<Boolean> sol = problem.solveAll(solver, limit).get(0);
		return Relation.wrap(Tensor.unstack(sol));
	}

	public static List<Relation<Boolean>> findRightClosedSets(
			SatSolver<?> solver, final GaloisConn<Boolean> galois, int limit) {
		SatProblem problem = new SatProblem(new int[] { galois.getRightSize() }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> rel = new Relation<BOOL>(alg, tensors.get(0));
				GaloisConn<BOOL> gal = GaloisConn.lift(alg, galois);
				return gal.isRightClosed(rel);
			}
		};

		Tensor<Boolean> sol = problem.solveAll(solver, limit).get(0);
		return Relation.wrap(Tensor.unstack(sol));
	}

	public static void print(GaloisConn<Boolean> galois) {
		Tensor<Boolean> t = galois.tensor;
		System.out.println("galois connection: " + t.getDim(0) + " by "
				+ t.getDim(1));

		StringBuilder s = new StringBuilder();
		for (int i = 0; i < t.getDim(0); i++) {
			for (int j = 0; j < t.getDim(1); j++)
				s.append(t.getElem(i, j) ? '1' : '0');
			s.append('\n');
		}
		System.out.print(s.toString());
	}
}
