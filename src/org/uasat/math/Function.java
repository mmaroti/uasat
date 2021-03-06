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

import java.util.*;

import org.uasat.core.*;

public final class Function<BOOL> {
	private final BoolAlgebra<BOOL> alg;
	private final Tensor<BOOL> tensor;

	public BoolAlgebra<BOOL> getAlg() {
		return alg;
	}

	public Tensor<BOOL> getTensor() {
		return tensor;
	}

	public int getCodomain() {
		return tensor.getDim(0);
	}

	public int getDomain() {
		return tensor.getDim(1);
	}

	public Function(BoolAlgebra<BOOL> alg, Tensor<BOOL> tensor) {
		assert tensor.getOrder() == 2;

		this.alg = alg;
		this.tensor = tensor;

		if (alg == BoolAlgebra.INSTANCE)
			assert (Boolean) isPartialFunction();
	}

	public static Function<Boolean> wrap(Tensor<Boolean> tensor) {
		return new Function<Boolean>(BoolAlgebra.INSTANCE, tensor);
	}

	public static <BOOL> Function<BOOL> lift(BoolAlgebra<BOOL> alg,
			Function<Boolean> fun) {
		return new Function<BOOL>(alg, alg.lift(fun.tensor));
	}

	public BOOL isFunction() {
		Tensor<BOOL> t = Tensor.fold(alg.ONE, 1, tensor);
		return Tensor.fold(alg.ALL, 1, t).get();
	}

	public BOOL isPartialFunction() {
		Tensor<BOOL> t = Tensor.fold(alg.MANY, 1, tensor);
		t = Tensor.fold(alg.ANY, 1, t);
		return alg.not(t.get());
	}

	public Relation<BOOL> range() {
		int[] shape = new int[] { tensor.getDim(1), tensor.getDim(0) };
		Tensor<BOOL> tmp = Tensor.reshape(tensor, shape, new int[] { 1, 0 });
		tmp = Tensor.fold(alg.ANY, 1, tmp);

		return new Relation<BOOL>(alg, tmp);
	}

	public BOOL isSurjective() {
		if (getDomain() < getCodomain())
			return alg.FALSE;

		return range().isFull();
	}

	public BOOL isInjective() {
		int[] shape = new int[] { tensor.getDim(1), tensor.getDim(0) };
		Tensor<BOOL> tmp = Tensor.reshape(tensor, shape, new int[] { 1, 0 });
		tmp = Tensor.fold(alg.MANY, 1, tmp);
		tmp = Tensor.fold(alg.ANY, 1, tmp);
		return alg.not(tmp.get());
	}

	public BOOL isEqualTo(Function<BOOL> fun) {
		assert alg == fun.alg && getDomain() == fun.getDomain()
				&& getCodomain() == fun.getCodomain();

		Tensor<BOOL> tmp = Tensor.map2(alg.EQU, tensor, fun.tensor);
		return Tensor.fold(alg.ALL, 2, tmp).get();
	}

	public BOOL hasValue(int res, int arg) {
		return tensor.getElem(res, arg);
	}

	public Function<BOOL> compose(Function<BOOL> fun) {
		assert alg == fun.alg && getDomain() == fun.getCodomain();

		int[] shape = new int[] { getDomain(), getCodomain(), fun.getDomain() };

		Tensor<BOOL> t1 = Tensor.reshape(tensor, shape, new int[] { 1, 0 });
		Tensor<BOOL> t2 = Tensor.reshape(fun.tensor, shape, new int[] { 0, 2 });
		t1 = Tensor.map2(alg.AND, t1, t2);
		t1 = Tensor.fold(alg.ANY, 1, t1);

		return new Function<BOOL>(alg, t1);
	}

	public BOOL isLexLeq(Function<BOOL> fun) {
		assert alg == fun.alg && getDomain() == fun.getDomain()
				&& getCodomain() == fun.getCodomain();

		return alg.lexLeq(getTensor(), fun.getTensor());
	}

	public BOOL isLexLess(Function<BOOL> fun) {
		assert alg == fun.alg && getDomain() == fun.getDomain()
				&& getCodomain() == fun.getCodomain();

		return alg.lexLess(getTensor(), fun.getTensor());
	}

	public Relation<BOOL> evaluate(Relation<BOOL> rel) {
		assert alg == rel.getAlg() && getDomain() == rel.getSize();
		Contract<BOOL> c = Contract.logical(alg);

		int a = rel.getArity();
		c.add(rel.getTensor(), Contract.range(0, a));
		for (int i = 0; i < a; i++)
			c.add(tensor, a + i, i);
		Tensor<BOOL> t = c.get(Contract.range(a, 2 * a));

		return new Relation<BOOL>(alg, t);
	}

	public BOOL preserves(Relation<BOOL> rel1, Relation<BOOL> rel2) {
		return evaluate(rel1).isSubsetOf(rel2);
	}

	public BOOL preserve(Structure<BOOL> str1, Structure<BOOL> str2) {
		List<Relation<BOOL>> rels1 = str1.getRelations();
		List<Relation<BOOL>> rels2 = str2.getRelations();
		assert rels1.size() == rels2.size();

		BOOL b = alg.TRUE;
		for (int i = 0; i < rels1.size(); i++)
			b = alg.and(b, preserves(rels1.get(i), rels2.get(i)));

		return b;
	}

	public static Tensor<Integer> decode(Function<Boolean> fun) {
		Func1<Integer, Iterable<Boolean>> lookup = new Func1<Integer, Iterable<Boolean>>() {
			@Override
			public Integer call(Iterable<Boolean> elem) {
				int count = 0;
				Iterator<Boolean> iter = elem.iterator();
				while (iter.hasNext()) {
					if (iter.next().booleanValue())
						return count;

					count += 1;
				}
				throw new IllegalArgumentException();
			}
		};

		return Tensor.fold(Integer.TYPE, lookup, 1, fun.getTensor());
	}

	public static String format(Function<Boolean> fun) {
		Tensor<Boolean> t = fun.getTensor();

		int[] tuple = new int[t.getDim(1)];
		outer: for (int i = 0; i < tuple.length; i++) {
			for (int j = 0; j < t.getDim(0); j++) {
				if (t.getElem(j, i)) {
					tuple[i] = j;
					continue outer;
				}
			}
		}

		return Util.formatTuple(t.getDim(0), tuple);
	}

	public static Function<Boolean> parse(int codomain, int domain, String str) {
		assert domain >= 0 && codomain >= 0;

		Tensor<Boolean> tensor = Tensor.constant(Boolean.TYPE, new int[] {
				codomain, domain }, Boolean.FALSE);

		if (domain > 0) {
			int[] tuple = Util.parseTuple(codomain, str);
			if (tuple.length != domain)
				throw new IllegalArgumentException();

			for (int i = 0; i < tuple.length; i++)
				tensor.setElem(Boolean.TRUE, tuple[i], i);
		}

		return wrap(tensor);
	}
}
