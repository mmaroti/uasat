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

import org.uasat.core.*;

public final class PartialOrder<BOOL> {
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

	public BOOL getValue(int index1, int index2) {
		return tensor.getElem(index1, index2);
	}

	public PartialOrder(BoolAlgebra<BOOL> alg, Tensor<BOOL> tensor) {
		assert tensor.getOrder() == 2;
		assert tensor.getDim(0) == tensor.getDim(1);

		this.alg = alg;
		this.tensor = tensor;

		if (alg == BoolAlgebra.INSTANCE && getSize() <= 10)
			assert (Boolean) isPartialOrder();
	}

	public static PartialOrder<Boolean> chain(int size) {
		return Relation.lessOrEqual(size).asPartialOrder();
	}

	public static PartialOrder<Boolean> antiChain(int size) {
		return Relation.equal(size).asPartialOrder();
	}

	public static PartialOrder<Boolean> crown(int size) {
		assert size % 2 == 0;

		final int c = size / 2;
		Tensor<Boolean> tensor = Tensor.generate(size, size,
				new Func2<Boolean, Integer, Integer>() {
					@Override
					public Boolean call(Integer elem1, Integer elem2) {
						int a = elem1, b = elem2;

						if (a == b)
							return true;

						if (a + c == b)
							return true;

						if (a + c + 1 == b)
							return true;

						if (a == c - 1 && b == c)
							return true;

						return false;
					}
				});

		return wrap(tensor);
	}

	public static PartialOrder<Boolean> random(int size, double density) {
		Relation<Boolean> rel = Relation.random(size, 2, density);
		rel = Relation.transitiveClosure(rel.reflexiveClosure());

		Relation<Boolean> neq = Relation.notEqual(size);
		Relation<Boolean> ant = rel.intersect(rel.rotate(1)).intersect(neq);
		rel = rel.intersect(ant.complement());

		assert rel.isPartialOrder();
		return rel.asPartialOrder();
	}

	public static PartialOrder<Boolean> wrap(Tensor<Boolean> tensor) {
		return new PartialOrder<Boolean>(BoolAlgebra.INSTANCE, tensor);
	}

	public static PartialOrder<Boolean> powerset(int base) {
		assert 0 <= base && base <= 30;

		int size = 1 << base;
		Tensor<Boolean> tmp = Tensor.generate(size, size,
				new Func2<Boolean, Integer, Integer>() {
					@Override
					public Boolean call(Integer elem1, Integer elem2) {
						int a = elem1, b = elem2;
						return (a & b) == a;
					}
				});

		return wrap(tmp);
	}

	public Relation<BOOL> asRelation() {
		return new Relation<BOOL>(alg, tensor);
	}

	public BOOL isPartialOrder() {
		return asRelation().isPartialOrder();
	}

	public PartialOrder<BOOL> invert() {
		return asRelation().revert().asPartialOrder();
	}

	public PartialOrder<BOOL> intersect(PartialOrder<BOOL> ord) {
		return asRelation().intersect(ord.asRelation()).asPartialOrder();
	}

	public Relation<BOOL> covers() {
		Relation<BOOL> tmp = Relation.lift(alg, Relation.notEqual(getSize()));
		tmp = tmp.intersect(asRelation());
		return tmp.subtract(tmp.compose(tmp));
	}

	public PartialOrder<BOOL> product(PartialOrder<BOOL> ord) {
		return asRelation().product(ord.asRelation()).asPartialOrder();
	}

	public PartialOrder<BOOL> plus(final PartialOrder<BOOL> ord) {
		assert alg == ord.alg;

		final int s = getSize();
		Tensor<BOOL> t = Tensor.generate(s + ord.getSize(), s + ord.getSize(),
				new Func2<BOOL, Integer, Integer>() {
					@Override
					public BOOL call(Integer elem1, Integer elem2) {
						if (elem1 < s && elem2 < s)
							return tensor.getElem(elem1, elem2);
						else if (elem1 >= s && elem2 >= s)
							return ord.tensor.getElem(elem1 - s, elem2 - s);
						else if (elem1 < s && elem2 >= s)
							return alg.TRUE;
						else
							return alg.FALSE;
					}
				});

		return new PartialOrder<BOOL>(alg, t);
	}

	public PartialOrder<BOOL> join(final PartialOrder<BOOL> ord) {
		assert alg == ord.alg;

		final int s = getSize();
		Tensor<BOOL> t = Tensor.generate(s + ord.getSize(), s + ord.getSize(),
				new Func2<BOOL, Integer, Integer>() {
					@Override
					public BOOL call(Integer elem1, Integer elem2) {
						if (elem1 < s && elem2 < s)
							return tensor.getElem(elem1, elem2);
						else if (elem1 >= s && elem2 >= s)
							return ord.tensor.getElem(elem1 - s, elem2 - s);
						else
							return alg.FALSE;
					}
				});

		return new PartialOrder<BOOL>(alg, t);
	}

	public Relation<BOOL> downsetOf(Relation<BOOL> rel) {
		assert rel.getArity() == 1;
		return asRelation().compose(rel);
	}

	public BOOL isDownset(Relation<BOOL> rel) {
		return rel.isSubsetOf(downsetOf(rel));
	}

	public Relation<BOOL> upsetOf(Relation<BOOL> rel) {
		assert rel.getArity() == 1;
		return rel.compose(asRelation());
	}

	public BOOL isUpset(Relation<BOOL> rel) {
		return rel.isSubsetOf(upsetOf(rel));
	}

	public BOOL isAntiChain(Relation<BOOL> rel) {
		assert rel.getArity() == 1;

		Relation<BOOL> tmp = Relation.lift(alg, Relation.notEqual(getSize()));
		tmp = tmp.intersect(asRelation());
		tmp = rel.compose(tmp).intersect(rel);
		return tmp.isEmpty();
	}

	public static <BOOL> PartialOrder<BOOL> lift(BoolAlgebra<BOOL> alg,
			PartialOrder<Boolean> rel) {
		Tensor<BOOL> tensor = Tensor.map(alg.LIFT, rel.tensor);
		return new PartialOrder<BOOL>(alg, tensor);
	}

	public static String format(PartialOrder<Boolean> ord) {
		return Relation.format(ord.covers());
	}

	public static PartialOrder<Boolean> parse(String str, int size) {
		Relation<Boolean> rel = Relation.parse(size, 2, str);
		rel = Relation.transitiveClosure(rel).reflexiveClosure();

		if (!rel.isAntiReflexive())
			throw new IllegalArgumentException();

		return rel.asPartialOrder();
	}

	public static int[] linearize(PartialOrder<Boolean> poset) {
		int[] order = new int[poset.getSize()];
		int head = 0, tail = 0;

		int[] smaller = new int[order.length];
		for (int i = 0; i < smaller.length; i++) {
			int s = 0;
			for (int j = 0; j < smaller.length; j++) {
				if (j != i && poset.getValue(j, i))
					s += 1;
			}

			if (s == 0)
				order[head++] = i;
			else
				smaller[i] = s;
		}

		assert head != 0;
		while (tail < head) {
			for (int i = 0; i < smaller.length; i++) {
				if (order[tail] != i && poset.getValue(order[tail], i)) {
					assert smaller[i] > 0;
					if (--smaller[i] == 0)
						order[head++] = i;
				}
			}
			tail += 1;
		}
		assert tail == order.length;

		return order;
	}

	public static Relation<Boolean> covers(PartialOrder<Boolean> poset) {
		Tensor<Boolean> tensor = poset.getTensor();
		Tensor<Boolean> cover = Tensor.constant(tensor.getShape(),
				Boolean.FALSE);

		int size = poset.getSize();
		for (int i = 0; i < size; i++)
			outer: for (int j = 0; j < size; j++) {
				if (i == j || !tensor.getElem(i, j))
					continue;

				for (int k = 0; k < size; k++) {
					if (k == i || k == j)
						continue;

					if (tensor.getElem(i, k) && tensor.getElem(k, j))
						continue outer;
				}

				cover.setElem(Boolean.TRUE, i, j);
			}

		return Relation.wrap(cover);
	}
}
