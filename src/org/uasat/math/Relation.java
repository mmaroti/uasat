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

import java.io.*;
import java.util.*;

import org.uasat.core.*;

public final class Relation<BOOL> {
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

	public int getArity() {
		return tensor.getOrder();
	}

	public BOOL getValue(int... index) {
		return tensor.getElem(index);
	}

	public Relation(BoolAlgebra<BOOL> alg, Tensor<BOOL> tensor) {
		assert 1 <= tensor.getOrder();

		int size = tensor.getDim(0);
		for (int i = 1; i < tensor.getOrder(); i++)
			assert tensor.getDim(i) == size;

		this.alg = alg;
		this.tensor = tensor;
	}

	public static Relation<Boolean> wrap(Tensor<Boolean> tensor) {
		return new Relation<Boolean>(BoolAlgebra.INSTANCE, tensor);
	}

	public static List<Relation<Boolean>> wrap(Iterable<Tensor<Boolean>> tensors) {
		List<Relation<Boolean>> list = new ArrayList<Relation<Boolean>>();
		for (Tensor<Boolean> t : tensors)
			list.add(wrap(t));
		return list;
	}

	public static <BOOL> Relation<BOOL> lift(BoolAlgebra<BOOL> alg,
			Relation<Boolean> rel) {
		return new Relation<BOOL>(alg, alg.lift(rel.tensor));
	}

	public static <BOOL> List<Relation<BOOL>> lift(BoolAlgebra<BOOL> alg,
			Iterable<Relation<Boolean>> rels) {
		List<Relation<BOOL>> list = new ArrayList<Relation<BOOL>>();
		for (Relation<Boolean> rel : rels)
			list.add(lift(alg, rel));
		return list;
	}

	public static <BOOL> Relation<BOOL> constant(BoolAlgebra<BOOL> alg,
			int size, int arity, BOOL value) {
		int[] shape = Util.createShape(size, arity);
		Tensor<BOOL> tensor = Tensor.constant(alg.getType(), shape, value);
		return new Relation<BOOL>(alg, tensor);
	}

	public static Relation<Boolean> full(int size, int arity) {
		Tensor<Boolean> tensor = Tensor.constant(Util.createShape(size, arity),
				Boolean.TRUE);
		return wrap(tensor);
	}

	public static Relation<Boolean> empty(int size, int arity) {
		Tensor<Boolean> tensor = Tensor.constant(Util.createShape(size, arity),
				Boolean.FALSE);
		return wrap(tensor);
	}

	public static Relation<Boolean> singleton(int size, int... tuple) {
		assert tuple.length >= 1;
		Tensor<Boolean> tensor = Tensor.constant(
				Util.createShape(size, tuple.length), Boolean.FALSE);
		tensor.setElem(Boolean.TRUE, tuple);
		return wrap(tensor);
	}

	public static Relation<Boolean> equal(int size) {
		Tensor<Boolean> tensor = Tensor.generate(size, size,
				new Func2<Boolean, Integer, Integer>() {
					@Override
					public Boolean call(Integer elem1, Integer elem2) {
						return elem1.intValue() == elem2.intValue();
					}
				});
		return wrap(tensor);
	}

	public static Relation<Boolean> notEqual(int size) {
		Tensor<Boolean> tensor = Tensor.generate(size, size,
				new Func2<Boolean, Integer, Integer>() {
					@Override
					public Boolean call(Integer elem1, Integer elem2) {
						return elem1.intValue() != elem2.intValue();
					}
				});
		return wrap(tensor);
	}

	public static Relation<Boolean> lessThan(int size) {
		Tensor<Boolean> tensor = Tensor.generate(size, size,
				new Func2<Boolean, Integer, Integer>() {
					@Override
					public Boolean call(Integer elem1, Integer elem2) {
						return elem1.intValue() < elem2.intValue();
					}
				});
		return wrap(tensor);
	}

	public static Relation<Boolean> lessOrEqual(int size) {
		Tensor<Boolean> tensor = Tensor.generate(size, size,
				new Func2<Boolean, Integer, Integer>() {
					@Override
					public Boolean call(Integer elem1, Integer elem2) {
						return elem1.intValue() <= elem2.intValue();
					}
				});
		return wrap(tensor);
	}

	public static Relation<Boolean> greaterThan(int size) {
		Tensor<Boolean> tensor = Tensor.generate(size, size,
				new Func2<Boolean, Integer, Integer>() {
					@Override
					public Boolean call(Integer elem1, Integer elem2) {
						return elem1.intValue() > elem2.intValue();
					}
				});
		return wrap(tensor);
	}

	public static Relation<Boolean> greaterOrEqual(int size) {
		Tensor<Boolean> tensor = Tensor.generate(size, size,
				new Func2<Boolean, Integer, Integer>() {
					@Override
					public Boolean call(Integer elem1, Integer elem2) {
						return elem1.intValue() >= elem2.intValue();
					}
				});
		return wrap(tensor);
	}

	public static Relation<Boolean> random(int size, int arity, double density) {
		assert size >= 0 && arity >= 1;
		final float d = (float) density;

		Func0<Boolean> rand = new Func0<Boolean>() {
			private Random random = new Random();

			@Override
			public Boolean call() {
				return random.nextFloat() < d ? Boolean.TRUE : Boolean.FALSE;
			}
		};

		return wrap(Tensor.generate(Util.createShape(size, arity), rand));
	}

	private void checkSize(Relation<BOOL> rel) {
		assert getAlg() == rel.getAlg();
		assert getSize() == rel.getSize();
	}

	protected void checkArity(Relation<BOOL> rel) {
		checkSize(rel);
		assert getArity() == rel.getArity();
	}

	public Relation<BOOL> intersect(Relation<BOOL> rel) {
		checkArity(rel);
		Tensor<BOOL> tmp = Tensor.map2(alg.AND, tensor, rel.tensor);
		return new Relation<BOOL>(alg, tmp);
	}

	public Relation<BOOL> union(Relation<BOOL> rel) {
		checkArity(rel);
		Tensor<BOOL> tmp = Tensor.map2(alg.OR, tensor, rel.tensor);
		return new Relation<BOOL>(alg, tmp);
	}

	public Relation<BOOL> symmDiff(Relation<BOOL> rel) {
		checkArity(rel);
		Tensor<BOOL> tmp = Tensor.map2(alg.ADD, tensor, rel.tensor);
		return new Relation<BOOL>(alg, tmp);
	}

	public Relation<BOOL> complement() {
		return new Relation<BOOL>(alg, alg.not(tensor));
	}

	public Relation<BOOL> subtract(Relation<BOOL> rel) {
		return intersect(rel.complement());
	}

	public Relation<BOOL> revert() {
		int[] map = new int[getArity()];
		for (int i = 0; i < map.length; i++)
			map[i] = map.length - 1 - i;

		Tensor<BOOL> tmp = Tensor.reshape(tensor, tensor.getShape(), map);
		return new Relation<BOOL>(alg, tmp);
	}

	// offset 1 shifts dim0 to dim1
	public Relation<BOOL> rotate(int offset) {
		int a = getArity();
		offset %= a;
		if (offset < 0)
			offset += a;

		int[] map = new int[a];
		for (int i = 0; i < map.length; i++)
			map[i] = (i + offset) % a;

		Tensor<BOOL> tmp = Tensor.reshape(tensor, tensor.getShape(), map);
		return new Relation<BOOL>(alg, tmp);
	}

	public Relation<BOOL> permute(int... coords) {
		assert coords.length <= getArity();

		int[] map = new int[getArity()];
		Arrays.fill(map, -1);

		for (int i = 0; i < coords.length; i++) {
			assert map[coords[i]] < 0;
			map[coords[i]] = i;
		}

		int a = coords.length;
		for (int i = 0; i < map.length; i++)
			if (map[i] < 0)
				map[i] = a++;
		assert a == map.length;

		Tensor<BOOL> tmp = Tensor.reshape(tensor, tensor.getShape(), map);
		return new Relation<BOOL>(alg, tmp);
	}

	public Relation<BOOL> permute(Permutation<Boolean> perm) {
		assert getArity() == perm.getSize();
		return permute(Permutation.decode(perm));
	}

	public BOOL isPermuteMinimal() {
		List<Permutation<Boolean>> perms = Permutation
				.nontrivialPerms(getArity());

		BOOL b = alg.TRUE;
		for (Permutation<Boolean> p : perms)
			b = alg.and(b, isLexLeq(permute(p)));

		return b;
	}

	public Relation<BOOL> project(int... coords) {
		assert coords.length <= getArity();

		int[] map = new int[getArity()];
		Arrays.fill(map, -1);

		int pos = map.length - coords.length;
		for (int i = 0; i < coords.length; i++)
			map[coords[i]] = pos++;

		pos = 0;
		for (int i = 0; i < map.length; i++)
			if (map[i] < 0)
				map[i] = pos++;
		assert pos == map.length - coords.length;

		Tensor<BOOL> tmp;
		tmp = Tensor.reshape(tensor, tensor.getShape(), map);
		if (map.length != coords.length)
			tmp = Tensor.fold(alg.ANY, pos, tmp);

		return new Relation<BOOL>(alg, tmp);
	}

	public Relation<BOOL> projectTail(int coords) {
		assert 0 <= coords && coords <= getArity();

		return new Relation<BOOL>(alg, Tensor.fold(alg.ANY,
				getArity() - coords, tensor));
	}

	public Relation<BOOL> projectHead(int coords) {
		assert 0 <= coords && coords <= getArity();
		return rotate(-coords).projectTail(coords);
	}

	private Tensor<BOOL> combine(Relation<BOOL> rel) {
		checkSize(rel);
		assert getArity() + rel.getArity() >= 3;

		int[] shape = Util.createShape(getSize(), getArity() + rel.getArity()
				- 1);

		int[] map = new int[getArity()];
		for (int i = 0; i < map.length - 1; i++)
			map[i] = i + 1;
		Tensor<BOOL> tmp1 = Tensor.reshape(tensor, shape, map);

		map = new int[rel.getArity()];
		for (int i = 1; i < map.length; i++)
			map[i] = getArity() + i - 1;
		Tensor<BOOL> tmp2 = Tensor.reshape(rel.tensor, shape, map);

		return Tensor.map2(alg.AND, tmp1, tmp2);
	}

	public Relation<BOOL> compose(Relation<BOOL> rel) {
		Tensor<BOOL> tmp = combine(rel);
		tmp = Tensor.fold(alg.ANY, 1, tmp);
		return new Relation<BOOL>(alg, tmp);
	}

	public Relation<BOOL> multiply(Relation<BOOL> rel) {
		Tensor<BOOL> tmp = combine(rel);
		tmp = Tensor.fold(alg.SUM, 1, tmp);
		return new Relation<BOOL>(alg, tmp);
	}

	public Relation<BOOL> diagonal() {
		int[] shape = new int[] { getSize() };
		int[] map = new int[getArity()];

		Tensor<BOOL> tmp = Tensor.reshape(tensor, shape, map);
		return new Relation<BOOL>(alg, tmp);
	}

	public Relation<BOOL> diagonal(int arity) {
		assert getArity() == 1;

		Tensor<BOOL> tmp = Tensor.constant(alg.getType(),
				Util.createShape(getSize(), arity), alg.FALSE);

		int[] index = new int[arity];
		for (int i = 0; i < getSize(); i++) {
			Arrays.fill(index, i);
			tmp.setElem(tensor.getElem(i), index);
		}

		return new Relation<BOOL>(alg, tmp);
	}

	public Relation<BOOL> cartesian(Relation<BOOL> rel) {
		assert alg == rel.getAlg() && getSize() == rel.getSize();

		int size = getSize();
		int[] shape = new int[getArity() + rel.getArity()];
		Arrays.fill(shape, size);

		int[] map = new int[rel.getArity()];
		for (int i = 0; i < map.length; i++)
			map[i] = getArity() + i;

		Tensor<BOOL> tmp = Tensor.reshape(rel.getTensor(), shape, map);

		map = new int[getArity()];
		for (int i = 0; i < map.length; i++)
			map[i] = i;

		tmp = Tensor.map2(alg.AND, tmp, Tensor.reshape(tensor, shape, map));
		return new Relation<BOOL>(alg, tmp);
	}

	public Relation<BOOL> product(Relation<BOOL> rel) {
		assert alg == rel.getAlg() && getArity() == rel.getArity();

		final int a = getSize();
		int s = a * rel.getSize();
		int[] shape = new int[getArity()];
		for (int i = 0; i < shape.length; i++)
			shape[i] = s;

		final Tensor<BOOL> t1 = tensor;
		final Tensor<BOOL> t2 = rel.getTensor();

		final int[] idx1 = new int[getArity()];
		final int[] idx2 = new int[getArity()];
		Tensor<BOOL> tmp = Tensor.generate(alg.getType(), shape,
				new Func1<BOOL, int[]>() {
					@Override
					public BOOL call(int[] elem) {
						for (int i = 0; i < elem.length; i++) {
							idx1[i] = elem[i] % a;
							idx2[i] = elem[i] / a;
						}

						return alg.and(t1.getElem(idx1), t2.getElem(idx2));
					}
				});

		return new Relation<BOOL>(alg, tmp);
	}

	public Relation<BOOL> power(int exp) {
		assert exp >= 1;

		Relation<BOOL> rel = this;
		for (int i = 1; i < exp; i++)
			rel = product(rel);

		return rel;
	}

	public Relation<BOOL> reflexiveClosure() {
		return union(lift(alg, equal(getSize())));
	}

	public Relation<BOOL> symmetricClosure() {
		assert getArity() == 2;
		return union(rotate(1));
	}

	public static Relation<Boolean> transitiveClosure(Relation<Boolean> rel) {
		for (;;) {
			Relation<Boolean> r = rel;
			rel = rel.union(rel.compose(rel));
			if (r.isEqualTo(rel))
				return r;
		}
	}

	public Relation<BOOL> conjugate(Permutation<BOOL> perm) {
		assert perm.getAlg() == getAlg() && perm.getSize() == getSize();
		Contract<BOOL> c = Contract.logical(alg);

		int a = getArity();
		c.add(tensor, Contract.range(0, a));
		for (int i = 0; i < a; i++)
			c.add(perm.getTensor(), i, i + a);
		Tensor<BOOL> t = c.get(Contract.range(a, 2 * a));

		return new Relation<BOOL>(alg, t);
	}

	public BOOL isLexLess(Relation<BOOL> rel) {
		assert getAlg() == rel.getAlg() && getSize() == rel.getSize()
				&& getArity() == rel.getArity();

		return alg.lexLess(getTensor(), rel.getTensor());
	}

	public BOOL isLexLeq(Relation<BOOL> rel) {
		assert getAlg() == rel.getAlg() && getSize() == rel.getSize()
				&& getArity() == rel.getArity();

		return alg.lexLeq(getTensor(), rel.getTensor());

		// return alg.lexLeq(Tensor.getLexOrder(getTensor()),
		// Tensor.getLexOrder(rel.getTensor()));
	}

	public BOOL isFull() {
		return Tensor.fold(alg.ALL, getArity(), tensor).get();
	}

	public BOOL isNotFull() {
		return alg.not(isFull());
	}

	public BOOL isEmpty() {
		return alg.not(isNotEmpty());
	}

	public BOOL isNotEmpty() {
		return Tensor.fold(alg.ANY, getArity(), tensor).get();
	}

	public BOOL isOddCard() {
		return Tensor.fold(alg.SUM, getArity(), tensor).get();
	}

	public BOOL isEvenCard() {
		return alg.not(isOddCard());
	}

	public BOOL isSingleton() {
		return Tensor.fold(alg.ONE, getArity(), tensor).get();
	}

	public BOOL isEqualTo(Relation<BOOL> rel) {
		checkArity(rel);
		Tensor<BOOL> tmp = Tensor.map2(alg.EQU, tensor, rel.tensor);
		return Tensor.fold(alg.ALL, getArity(), tmp).get();
	}

	public BOOL isSubsetOf(Relation<BOOL> rel) {
		checkArity(rel);

		Tensor<BOOL> tmp = Tensor.map2(alg.LEQ, tensor, rel.tensor);
		tmp = Tensor.fold(alg.ALL, getArity(), tmp);
		return tmp.get();
	}

	// TODO: this is probably not optimal
	public BOOL isProperSubsetOf(Relation<BOOL> rel) {
		return alg.and(isSubsetOf(rel), alg.not(rel.isSubsetOf(this)));
	}

	public BOOL isDisjointOf(Relation<BOOL> rel) {
		return isSubsetOf(rel.complement());
	}

	public BOOL isOperation() {
		Tensor<BOOL> rel = Tensor.fold(alg.ONE, 1, tensor);
		return Tensor.fold(alg.ALL, rel.getOrder(), rel).get();
	}

	public BOOL isReflexive() {
		return diagonal().isFull();
	}

	public BOOL isAntiReflexive() {
		return diagonal().isEmpty();
	}

	public BOOL isSymmetric() {
		return isSubsetOf(rotate(1));
	}

	public BOOL isTransitive() {
		assert tensor.getOrder() == 2;
		// mask out diagonal to get fewer literals
		Relation<BOOL> rel = intersect(lift(alg, notEqual(getSize())));
		return rel.compose(rel).isSubsetOf(this);
	}

	public BOOL isAntiSymmetric() {
		assert tensor.getOrder() == 2;
		Relation<BOOL> rel = intersect(lift(alg, notEqual(getSize())));
		rel = rel.intersect(rel.rotate(1));
		return rel.isEmpty();
	}

	public BOOL isTrichotome() {
		assert tensor.getOrder() == 2;
		Relation<BOOL> rel1, rel2;
		rel1 = lift(alg, lessThan(getSize()));
		rel2 = rotate(1).complement().intersect(rel1);
		rel1 = intersect(rel1);
		return rel1.isEqualTo(rel2);
	}

	public BOOL isEquivalence() {
		BOOL b = isReflexive();
		b = alg.and(b, isSymmetric());
		return alg.and(b, isTransitive());
	}

	public BOOL isPartialOrder() {
		BOOL b = isReflexive();
		b = alg.and(b, isAntiSymmetric());
		return alg.and(b, isTransitive());
	}

	public BOOL isQuasiOrder() {
		BOOL b = isReflexive();
		return alg.and(b, isTransitive());
	}

	public BOOL isTotalOrder() {
		BOOL b = isReflexive();
		b = alg.and(b, isTrichotome());
		return alg.and(b, isTransitive());
	}

	public BOOL isEssentialCoord(int coord) {
		assert 0 <= coord && coord < getArity();

		Tensor<BOOL> tmp = tensor;
		if (coord != 0) {
			int[] map = new int[getArity()];

			for (int i = 0; i < coord; i++)
				map[i] = i + 1;

			map[coord] = 0;

			for (int i = coord + 1; i < map.length; i++)
				map[i] = i;

			tmp = Tensor.reshape(tensor, tensor.getShape(), map);
		}

		tmp = Tensor.fold(alg.EQS, 1, tmp);
		tmp = Tensor.fold(alg.ALL, tmp.getOrder(), tmp);
		return alg.not(tmp.get());
	}

	public BOOL hasEssentialCoords() {
		BOOL b = alg.TRUE;
		for (int i = 0; i < getArity(); i++)
			b = alg.and(b, isEssentialCoord(i));

		return b;
	}

	public static Relation<Boolean> removeNonessentialCoords(
			Relation<Boolean> rel) {
		BoolAlgebra<Boolean> alg = rel.getAlg();
		Tensor<Boolean> tensor = rel.tensor;

		int a = rel.getArity();
		for (int i = 0; i < a; i++) {
			if (tensor.getOrder() <= 1)
				break;

			Tensor<Boolean> t = Tensor.fold(alg.EQS, 1, tensor);
			Boolean nonessential = Tensor.fold(alg.ALL, t.getOrder(), t).get();

			int[] map = new int[tensor.getOrder()];
			for (int j = 1; j < map.length; j++)
				map[j] = j - 1;
			map[0] = nonessential ? 0 : map.length - 1;

			tensor = Tensor.reshape(tensor,
					(nonessential ? t : tensor).getShape(), map);
		}

		return Relation.wrap(tensor);
	}

	public PartialOrder<BOOL> asPartialOrder() {
		return new PartialOrder<BOOL>(alg, tensor);
	}

	public Operation<BOOL> asOperation() {
		return new Operation<BOOL>(alg, tensor);
	}

	public BOOL isEssential() {
		if (getArity() <= 1)
			return alg.or(isEmpty(), isFull());

		int[] v = new int[getArity()];
		for (int i = 0; i < v.length; i++)
			v[i] = i;

		Contract<BOOL> c = Contract.logical(alg);
		c.add(alg.not(tensor), v);
		for (int i = 0; i < getArity(); i++) {
			v[i] = i + getArity();
			c.add(tensor, v);
			v[i] = i;
		}
		return c.get(new int[0]).get();
	}

	public BOOL isSubdirect() {
		BOOL b = alg.TRUE;
		for (int i = 0; i < getArity(); i++)
			b = alg.and(b, project(i).isFull());

		return b;
	}

	public BOOL isSubdirect(List<Relation<BOOL>> coords) {
		assert getArity() == coords.size();

		BOOL b = alg.TRUE;
		for (int i = 0; i < getArity(); i++)
			b = alg.and(b, project(i).isEqualTo(coords.get(i)));

		return b;
	}

	public BOOL isMemberOf(Iterable<Relation<Boolean>> coll) {
		BOOL b = alg.FALSE;
		for (Relation<Boolean> rel : coll)
			b = alg.or(b, isEqualTo(Relation.lift(alg, rel)));

		return b;
	}

	public Relation<BOOL> makeComplexRelation(final List<Relation<BOOL>> subsets) {
		int[] shape = new int[getArity()];
		Arrays.fill(shape, subsets.size());

		Tensor<BOOL> tensor = Tensor.generate(alg.getType(), shape,
				new Func1<BOOL, int[]>() {
					@Override
					public BOOL call(int[] elem) {
						Relation<BOOL> r = subsets.get(elem[0]);
						assert r.getAlg() == alg;

						for (int i = 1; i < elem.length; i++)
							r = r.cartesian(subsets.get(elem[i]));

						r = intersect(r); // with this

						BOOL b = alg.TRUE;
						for (int i = 0; i < elem.length; i++)
							b = alg.and(
									b,
									subsets.get(elem[i]).isSubsetOf(
											r.project(i)));

						return b;
					}
				});

		return new Relation<BOOL>(alg, tensor);
	}

	public static String format(Relation<Boolean> rel) {
		StringBuilder s = new StringBuilder();
		int size = rel.getSize();

		Iterator<int[]> iter = Util.cubeIterator(rel.getSize(), rel.getArity());
		while (iter.hasNext()) {
			int[] index = iter.next();
			if (rel.getValue(index)) {
				if (s.length() != 0)
					s.append(' ');

				s.append(Util.formatTuple(size, index));
			}
		}

		return s.toString();
	}

	public static Relation<Boolean> parse(int size, int arity, String str) {
		assert arity >= 1;

		Tensor<Boolean> tensor = Tensor.constant(Util.createShape(size, arity),
				Boolean.FALSE);

		for (String s : str.split(" ")) {
			if (s.isEmpty())
				continue;

			int[] index = Util.parseTuple(size, s);
			tensor.setElem(Boolean.TRUE, index);
		}

		return Relation.wrap(tensor);
	}

	public static Relation<Boolean> parse(int size, String str) {
		if (str.isEmpty())
			return Relation.empty(size, 1);
		String tuple = str.split(" ", 2)[0];

		int arity;
		if (size > 'z' - 'a' + 10)
			arity = tuple.split(",").length;
		else
			arity = tuple.length();

		return parse(size, arity, str);
	}

	public static int cardinality(Relation<Boolean> rel) {
		int c = 0;
		for (Boolean b : rel.getTensor())
			if (b.booleanValue())
				c += 1;

		return c;
	}

	public static List<Relation<Boolean>> subsets(int size, int card) {
		assert 0 <= card && card <= size;

		int[] s = new int[card];
		for (int i = 0; i < card; i++)
			s[i] = i;

		int[] shape = new int[] { size };
		List<Relation<Boolean>> list = new ArrayList<Relation<Boolean>>();

		outer: for (;;) {
			Tensor<Boolean> tensor = Tensor.constant(shape, Boolean.FALSE);
			for (int i = 0; i < card; i++)
				tensor.setElem(Boolean.TRUE, s[i]);

			list.add(wrap(tensor));

			for (int i = 0; i < card - 1; i++)
				if (++s[i] < s[i + 1])
					continue outer;
				else
					s[i] = i;

			if (card == 0 || ++s[card - 1] >= size)
				break;
		}

		return list;
	}

	public static List<Relation<Boolean>> subsets(int size, int minCard,
			int maxCard) {
		assert 0 <= minCard && minCard <= maxCard && maxCard <= size;

		List<Relation<Boolean>> list = new ArrayList<Relation<Boolean>>();
		for (int i = minCard; i <= maxCard; i++)
			list.addAll(subsets(size, i));

		return list;
	}

	@Override
	public boolean equals(Object other) {
		@SuppressWarnings("unchecked")
		Relation<BOOL> rel = (Relation<BOOL>) other;
		assert alg == rel.alg;

		return tensor.equals(rel.tensor);
	}

	public static final Comparator<Relation<Boolean>> COMPARATOR = new Comparator<Relation<Boolean>>() {
		final Comparator<Tensor<Boolean>> comp = Tensor
				.comparator(BoolAlgebra.COMPARATOR);

		@Override
		public int compare(Relation<Boolean> o1, Relation<Boolean> o2) {
			assert o1.getSize() == o2.getSize();

			if (o1.getArity() != o2.getArity())
				return o1.getArity() - o2.getArity();

			int c1 = cardinality(o1);
			int c2 = cardinality(o2);
			if (c1 != c2)
				return c1 - c2;

			return comp.compare(o2.getTensor(), o1.getTensor());
		}
	};

	public static List<Relation<Boolean>> sort(List<Relation<Boolean>> list) {
		ArrayList<Relation<Boolean>> list2 = new ArrayList<Relation<Boolean>>(
				list);
		Collections.sort(list2, COMPARATOR);
		return list2;
	}

	public static void print(Relation<Boolean> rel) {
		System.out.println("relation of size " + rel.getSize() + " arity "
				+ rel.getArity() + " cardinality " + Relation.cardinality(rel));

		int a = rel.getArity();
		boolean poset = false;
		boolean equiv = false;

		String s = "properties:";
		if (a == 2 && rel.isPartialOrder()) {
			poset = true;
			s += " partial order";
		}
		if (a == 2 && rel.isEquivalence()) {
			equiv = true;
			s += " equivalence";
		}
		if (!poset && !equiv && rel.isReflexive())
			s += " reflexive";
		if (rel.isAntiReflexive())
			s += " antireflexive";
		if (!equiv && rel.isSymmetric())
			s += " symmetric";
		if (a == 2 && !poset && rel.isAntiSymmetric())
			s += " antisymmetric";
		if (a == 2 && !poset && !equiv && rel.isTransitive())
			s += " transitive";
		if (a == 2 && rel.isTrichotome())
			s += " trichotome";
		if (rel.isEssential())
			s += " essential";
		if (rel.isEssential())
			s += " subdirect";

		System.out.println(s);
		if (poset) {
			Relation<Boolean> covers = rel.asPartialOrder().covers();
			System.out.println("covers: " + Relation.format(covers));
		}

		System.out.println("members: " + Relation.format(rel));
	}

	public static void print(String message, List<Relation<Boolean>> rels,
			PrintStream out) {
		out.println(message + ": " + rels.size());

		Collections.sort(rels, Relation.COMPARATOR);
		for (int i = 0; i < rels.size(); i++)
			out.println(i + ":\t" + Relation.format(rels.get(i)));

		out.println();
	}

	public static void print(String message, List<Relation<Boolean>> rels) {
		print(message, rels, System.out);
	}
}
