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
			GaloisConn<Boolean> rel) {
		Tensor<BOOL> tensor = Tensor.map(alg.LIFT, rel.tensor);
		return new GaloisConn<BOOL>(alg, tensor);
	}

	public static GaloisConn<Boolean> wrap(Tensor<Boolean> tensor) {
		return new GaloisConn<Boolean>(BoolAlgebra.INSTANCE, tensor);
	}

	private static int[] MAP0 = new int[] { 0 };
	private static int[] MAP1 = new int[] { 1 };
	private static int[] MAP10 = new int[] { 1, 0 };

	private Tensor<BOOL> rightClosure(Tensor<BOOL> left) {
		Tensor<BOOL> t = Tensor.reshape(left, tensor.getShape(), MAP0);
		return Tensor.fold(alg.ALL, 1, Tensor.map2(alg.LEQ, t, tensor));
	}

	private Tensor<BOOL> leftClosure(Tensor<BOOL> right) {
		int[] shape = tensor.getShape();
		Tensor<BOOL> t = Tensor.reshape(right, shape, MAP1);
		t = Tensor.map2(alg.LEQ, t, tensor);
		t = Tensor.reshape(t, new int[] { shape[1], shape[0] }, MAP10);
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
		Tensor<BOOL> t = rightClosure(leftClosure(left.getTensor()));
		t = Tensor.map2(alg.EQU, t, left.getTensor());
		return Tensor.fold(alg.ALL, 1, t).get();
	}

	public BOOL isRightClosed(Relation<BOOL> right) {
		Tensor<BOOL> t = leftClosure(rightClosure(right.getTensor()));
		t = Tensor.map2(alg.EQU, t, right.getTensor());
		return Tensor.fold(alg.ALL, 1, t).get();
	}
}
