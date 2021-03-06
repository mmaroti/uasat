/**
 * Copyright (C) Miklos Maroti, 2015
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

public final class Permutation<BOOL> {
	private final BoolAlgebra<BOOL> alg;
	private final Tensor<BOOL> tensor;

	public BoolAlgebra<BOOL> getAlg() {
		return alg;
	}

	public Tensor<BOOL> getTensor() {
		return tensor;
	}

	public int getSize() {
		return tensor.getDim(0);
	}

	public Permutation(BoolAlgebra<BOOL> alg, Tensor<BOOL> tensor) {
		assert tensor.getOrder() == 2;
		assert tensor.getDim(0) == tensor.getDim(1);

		this.alg = alg;
		this.tensor = tensor;

		if (alg == BoolAlgebra.INSTANCE)
			assert (Boolean) isPermutation();
	}

	public BOOL hasValue(int result, int argument) {
		return tensor.getElem(result, argument);
	}

	public static Permutation<Boolean> wrap(Tensor<Boolean> tensor) {
		return new Permutation<Boolean>(BoolAlgebra.INSTANCE, tensor);
	}

	public static <BOOL> Permutation<BOOL> lift(BoolAlgebra<BOOL> alg, Permutation<Boolean> perm) {
		return new Permutation<BOOL>(alg, alg.lift(perm.tensor));
	}

	public static Permutation<Boolean> create(final int[] perm) {
		return wrap(Tensor.generate(perm.length, perm.length, new Func2<Boolean, Integer, Integer>() {
			@Override
			public Boolean call(Integer elem1, Integer elem2) {
				return perm[elem2] == elem1;
			}
		}));
	}

	public static Permutation<Boolean> identity(int size) {
		return wrap(Relation.equal(size).getTensor());
	}

	public static List<Permutation<Boolean>> transpositions(int size) {
		assert size >= 1;
		List<Permutation<Boolean>> list = new ArrayList<Permutation<Boolean>>();

		int[] perm = new int[size];
		for (int i = 0; i < size; i++)
			perm[i] = i;

		for (int i = 0; i < size - 1; i++) {
			for (int j = i + 1; j < size; j++) {
				perm[i] = j;
				perm[j] = i;
				list.add(create(perm));
				perm[j] = j;
			}
			perm[i] = i;
		}

		for (int i = 0; i < size; i++)
			assert perm[i] == i;

		return list;
	}

	public static List<Permutation<Boolean>> threeCycles(int size) {
		assert size >= 1;
		List<Permutation<Boolean>> list = new ArrayList<Permutation<Boolean>>();

		int[] perm = new int[size];
		for (int i = 0; i < size; i++)
			perm[i] = i;

		for (int i = 0; i < size - 2; i++) {
			for (int j = i + 1; j < size - 1; j++) {
				for (int k = j + 1; k < size; k++) {
					perm[i] = j;
					perm[j] = k;
					perm[k] = i;
					list.add(create(perm));
					perm[i] = k;
					perm[j] = i;
					perm[k] = j;
					list.add(create(perm));
					perm[k] = k;
				}
				perm[j] = j;
			}
			perm[i] = i;
		}

		for (int i = 0; i < size; i++)
			assert perm[i] == i;

		return list;
	}

	public static List<Permutation<Boolean>> symmetricGroup(int size) {
		List<Permutation<Boolean>> list = new ArrayList<Permutation<Boolean>>();

		Iterator<int[]> iter = Util.permIterator(size);
		while (iter.hasNext())
			list.add(create(iter.next()));

		return list;
	}

	public static List<Permutation<Boolean>> nontrivialPerms(int size) {
		List<Permutation<Boolean>> list = symmetricGroup(size);
		list.remove(0);
		return list;
	}

	public BOOL isPermutation() {
		Operation<BOOL> op = asOperation();
		return alg.and(op.isOperation(), op.isSurjective());
	}

	public Operation<BOOL> asOperation() {
		return new Operation<BOOL>(alg, tensor);
	}

	public Relation<BOOL> asRelation() {
		return new Relation<BOOL>(alg, tensor);
	}

	public Permutation<BOOL> invert() {
		Tensor<BOOL> tmp;
		tmp = Tensor.reshape(tensor, tensor.getShape(), new int[] { 1, 0 });
		return new Permutation<BOOL>(alg, tmp);
	}

	public BOOL isOdd() {
		Relation<BOOL> tmp1 = Relation.lift(alg, Relation.lessThan(getSize()));
		tmp1 = tmp1.compose(asRelation());

		Relation<BOOL> tmp2 = Relation.lift(alg, Relation.greaterThan(getSize()));
		tmp2 = asRelation().compose(tmp2);

		return tmp1.intersect(tmp2).isOddCard();
	}

	public BOOL isEven() {
		return alg.not(isOdd());
	}

	public BOOL isIdentity() {
		return asOperation().isIdempotent();
	}

	public static int[] decode(Permutation<Boolean> perm) {
		assert perm.isPermutation();

		int[] map = new int[perm.getSize()];
		outer: for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map.length; j++)
				if (perm.tensor.getElem(j, i)) {
					map[i] = j;
					continue outer;
				}
			throw new IllegalStateException("this cannot happen");
		}

		return map;
	}
}
