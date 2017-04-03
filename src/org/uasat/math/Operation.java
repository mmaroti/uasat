/**
 * Copyright (C) Miklos Maroti, 2015-2017
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

public class Operation<BOOL> extends PartialOperation<BOOL> {
	public Operation(BoolAlgebra<BOOL> alg, Tensor<BOOL> tensor) {
		super(alg, tensor);

		if (alg == BoolAlgebra.INSTANCE)
			assert (Boolean) isOperation();
	}

	public static Operation<Boolean> wrap(Tensor<Boolean> tensor) {
		return new Operation<Boolean>(BoolAlgebra.INSTANCE, tensor);
	}

	public static List<Operation<Boolean>> wrap(Iterable<Tensor<Boolean>> tensors) {
		List<Operation<Boolean>> list = new ArrayList<Operation<Boolean>>();
		for (Tensor<Boolean> t : tensors)
			list.add(wrap(t));
		return list;
	}

	public static <BOOL> Operation<BOOL> lift(BoolAlgebra<BOOL> alg, Operation<Boolean> op) {
		return new Operation<BOOL>(alg, alg.lift(op.tensor));
	}

	public static <BOOL> List<Operation<BOOL>> lift(BoolAlgebra<BOOL> alg, Iterable<Operation<Boolean>> ops) {
		List<Operation<BOOL>> list = new ArrayList<Operation<BOOL>>();
		for (Operation<Boolean> op : ops)
			list.add(lift(alg, op));
		return list;
	}

	public static Operation<Boolean> projection(int size, int arity, final int coord) {
		assert 0 <= coord && coord < arity;

		Tensor<Boolean> tensor = Tensor.generate(Util.createShape(size, 1 + arity), new Func1<Boolean, int[]>() {
			@Override
			public Boolean call(int[] elem) {
				return elem[0] == elem[1 + coord];
			}
		});
		return Operation.wrap(tensor);
	}

	public static Operation<Boolean> unaryConstant(int size, final int value) {
		assert 0 <= value && value < size;

		Tensor<Boolean> tensor = Tensor.generate(size, size, new Func2<Boolean, Integer, Integer>() {
			@Override
			public Boolean call(Integer elem1, Integer elem2) {
				return elem1 == value;
			}
		});
		return Operation.wrap(tensor);
	}

	public static Operation<Boolean> moduloAdd(final int size) {
		Tensor<Boolean> tensor = Tensor.generate(new int[] { size, size, size }, new Func1<Boolean, int[]>() {
			@Override
			public Boolean call(int[] elem) {
				return elem[0] == (elem[1] + elem[2]) % size;
			}
		});
		return Operation.wrap(tensor);
	}

	public static Operation<Boolean> moduloMul(final int size) {
		Tensor<Boolean> tensor = Tensor.generate(new int[] { size, size, size }, new Func1<Boolean, int[]>() {
			@Override
			public Boolean call(int[] elem) {
				return elem[0] == (elem[1] * elem[2]) % size;
			}
		});
		return Operation.wrap(tensor);
	}

	public BOOL isEqualTo(Operation<BOOL> op) {
		return asRelation().isEqualTo(op.asRelation());
	}

	public BOOL isProjection(int coord) {
		assert 0 <= coord && coord < getArity();

		int[] map = new int[tensor.getOrder()];
		for (int i = 1; i < coord + 1; i++)
			map[i] = i;
		map[coord + 1] = 0;
		for (int i = coord + 2; i < map.length; i++)
			map[i] = i - 1;

		Tensor<BOOL> tmp = Tensor.reshape(tensor, Util.createShape(getSize(), getArity()), map);
		return Tensor.fold(alg.ALL, tmp.getOrder(), tmp).get();
	}

	public BOOL isProjection() {
		BOOL b = alg.FALSE;
		for (int i = 0; i < getArity(); i++)
			b = alg.or(b, isProjection(i));

		return b;
	}

	public BOOL isRetraction() {
		return compose(this).isEqualTo(this);
	}

	public Operation<BOOL> polymer(int... variables) {
		assert getArity() == variables.length;

		int[] map = new int[variables.length + 1];

		int a = 0;
		for (int i = 0; i < variables.length; i++) {
			assert 0 <= variables[i];
			a = Math.max(a, variables[i] + 1);
			map[i + 1] = variables[i] + 1;
		}

		Tensor<BOOL> tmp = Tensor.reshape(tensor, Util.createShape(getSize(), 1 + a), map);
		return new Operation<BOOL>(alg, tmp);
	}

	public Operation<BOOL> permute(Permutation<Boolean> perm) {
		assert getArity() == perm.getSize();
		return polymer(Permutation.decode(perm));
	}

	private BOOL isSatisfied(int... variables) {
		assert getArity() == variables.length;

		int[] map = new int[variables.length + 1];

		int a = 0;
		for (int i = 0; i < variables.length; i++) {
			assert 0 <= variables[i];
			a = Math.max(a, variables[i]);
			map[1 + i] = variables[i];
		}

		Tensor<BOOL> tmp = Tensor.reshape(tensor, Util.createShape(getSize(), 1 + a), map);
		return Tensor.fold(alg.ALL, 1 + a, tmp).get();
	}

	public BOOL isIdempotent() {
		return isSatisfied(new int[getArity()]);
	}

	public BOOL isUnitElement(int elem) {
		assert getArity() == 2;

		BOOL b = alg.TRUE;
		for (int i = 0; i < getSize(); i++) {
			b = alg.and(b, tensor.getElem(i, elem, i));
			if (i != elem)
				b = alg.and(b, tensor.getElem(i, i, elem));
		}

		return b;
	}

	public BOOL isZeroElement(int elem) {
		assert getArity() == 2;

		BOOL b = alg.TRUE;
		for (int i = 0; i < getSize(); i++) {
			b = alg.and(b, tensor.getElem(elem, elem, i));
			if (i != elem)
				b = alg.and(b, tensor.getElem(elem, i, elem));
		}

		return b;
	}

	public BOOL isCommutative() {
		return isEqualTo(polymer(1, 0));
	}

	public BOOL isAssociative() {
		assert getArity() == 2;
		Contract<BOOL> c = Contract.logical(alg);

		c.add(tensor, "txy");
		c.add(tensor, "atz");
		Tensor<BOOL> tmp = c.get("axyz");
		c.add(tensor, "tyz");
		c.add(tensor, "axt");
		tmp = Tensor.map2(alg.EQU, tmp, c.get("axyz"));
		tmp = Tensor.fold(alg.ALL, 4, tmp);

		return tmp.get();
	}

	public BOOL isSemilattice() {
		return alg.and(alg.and(isIdempotent(), isCommutative()), isAssociative());
	}

	public BOOL isTwoSemilattice() {
		Operation<BOOL> x = lift(alg, projection(getSize(), 2, 0));
		BOOL b = this.isEqualTo(this.compose(x, this));

		return alg.and(alg.and(isIdempotent(), isCommutative()), b);
	}

	public BOOL isMajority() {
		BOOL b = isSatisfied(1, 0, 0);
		b = alg.and(b, isSatisfied(0, 1, 0));
		b = alg.and(b, isSatisfied(0, 0, 1));
		return b;
	}

	public BOOL isMinority() {
		BOOL b = isSatisfied(0, 1, 1);
		b = alg.and(b, isSatisfied(1, 0, 1));
		b = alg.and(b, isSatisfied(1, 1, 0));
		return b;
	}

	public BOOL isMaltsev() {
		BOOL b = isSatisfied(0, 1, 1);
		b = alg.and(b, isSatisfied(1, 1, 0));
		return b;
	}

	public BOOL isEssential() {
		BOOL b = alg.FALSE;

		int a = getArity() + 1;
		int[] map = new int[a];

		for (int i = 1; i < a; i++) {
			for (int j = 0; j < i; j++)
				map[j] = j + 1;

			map[i] = 0;

			for (int j = i + 1; j < a; j++)
				map[j] = j;

			Tensor<BOOL> t = Tensor.reshape(tensor, tensor.getShape(), map);
			t = Tensor.fold(alg.ALL, getArity(), Tensor.fold(alg.EQS, 1, t));
			b = alg.or(b, t.get());
		}

		return alg.not(b);
	}

	public BOOL isNearUnanimity() {
		assert getArity() >= 3;

		int[] vars = new int[getArity()];
		BOOL b = alg.TRUE;
		for (int i = 0; i < vars.length; i++) {
			vars[i] = 1;
			b = alg.and(b, polymer(vars).isProjection(0));
			vars[i] = 0;
		}

		return b;
	}

	public BOOL isWeakNearUnanimity() {
		assert getArity() >= 2;

		int[] vars = new int[getArity()];

		vars[0] = 1;
		Operation<BOOL> p = polymer(vars);
		vars[0] = 0;

		BOOL b = isIdempotent();
		for (int i = 1; i < vars.length; i++) {
			vars[i] = 1;
			b = alg.and(b, p.isEqualTo(polymer(vars)));
			vars[i] = 0;
		}

		return b;
	}

	/**
	 * Testing Taylor property (omitting type 1):
	 * 
	 * p(x,x,x) = x. p(x,x,y) = p(y,x,x) = q(x,y,y). p(x,y,x) = q(x,y,x).
	 */
	public static <BOOL> BOOL areSiggersTerms(Operation<BOOL> p, Operation<BOOL> q) {
		assert p.alg == q.alg && p.getArity() == 3 && q.getArity() == 3;

		BOOL b = p.isIdempotent();

		Operation<BOOL> r = p.polymer(0, 0, 1);
		b = p.alg.and(b, r.isEqualTo(p.polymer(1, 0, 0)));
		b = p.alg.and(b, r.isEqualTo(q.polymer(0, 1, 1)));

		r = p.polymer(0, 1, 0);
		b = p.alg.and(b, r.isEqualTo(q.polymer(0, 1, 0)));

		return b;
	}

	/**
	 * Testing congruence meet semi-distributivity (omitting types 1 and 2):
	 * 
	 * p(x,x,x) = x. p(x,x,y) = p(x,y,x) = p(y,x,x) = q(x,y,x). q(x,x,y) =
	 * q(x,y,y).
	 */
	public static <BOOL> BOOL areJovanovicTerms(Operation<BOOL> p, Operation<BOOL> q) {
		assert p.alg == q.alg && p.getArity() == 3 && q.getArity() == 3;

		BOOL b = p.isIdempotent();

		Operation<BOOL> r = p.polymer(0, 0, 1);
		b = p.alg.and(b, r.isEqualTo(p.polymer(0, 1, 0)));
		b = p.alg.and(b, r.isEqualTo(p.polymer(1, 0, 0)));
		b = p.alg.and(b, r.isEqualTo(q.polymer(0, 1, 0)));

		r = q.polymer(0, 0, 1);
		b = p.alg.and(b, r.isEqualTo(q.polymer(0, 1, 1)));

		return b;
	}

	/**
	 * Testing congruence meet semi-distributivity (omitting types 1 and 2):
	 * 
	 * p(x,x,x) = x. p(x,x,y) = p(x,y,x) = p(y,x,x) = q(x,y,x) = q(x,x,y) =
	 * q(x,y,y).
	 */
	public static <BOOL> BOOL areJovanovicTerms2(Operation<BOOL> p, Operation<BOOL> q) {
		assert p.alg == q.alg && p.getArity() == 3 && q.getArity() == 3;

		BOOL b = p.isIdempotent();

		Operation<BOOL> r = p.polymer(0, 0, 1);
		b = p.alg.and(b, r.isEqualTo(p.polymer(0, 1, 0)));
		b = p.alg.and(b, r.isEqualTo(p.polymer(1, 0, 0)));
		b = p.alg.and(b, r.isEqualTo(q.polymer(0, 1, 0)));
		b = p.alg.and(b, r.isEqualTo(q.polymer(0, 0, 1)));
		b = p.alg.and(b, r.isEqualTo(q.polymer(0, 1, 1)));

		return b;
	}

	/**
	 * Testing congruence distributivity (omitting types 1, 2 and 5 and no
	 * tails)
	 * 
	 * p_i(x,y,x) = x. x = p_0(x,x,y). p_0(x,y,y) = p_1(x,y,y). p_1(x,x,y) =
	 * p_2(x,x,y). p_{n-1}(x,y,y) = y (for n odd). p_{n-1}(x,x,y) = y (for n
	 * even).
	 */
	public static <BOOL> BOOL areJonssonTerms(List<Operation<BOOL>> ops) {
		assert ops.size() >= 1;
		BoolAlgebra<BOOL> alg = ops.get(0).getAlg();

		BOOL b = alg.TRUE;
		for (int i = 0; i < ops.size(); i++) {
			assert ops.get(i).getArity() == 3 && ops.get(i).getAlg() == alg;
			b = alg.and(b, ops.get(i).isSatisfied(0, 1, 0));
		}

		b = alg.and(b, ops.get(0).isSatisfied(0, 0, 1));

		for (int i = 0; i + 1 < ops.size(); i += 2) {
			BOOL c = ops.get(i).polymer(0, 1, 1).isEqualTo(ops.get(i + 1).polymer(0, 1, 1));
			b = alg.and(b, c);
		}

		for (int i = 1; i + 1 < ops.size(); i += 2) {
			BOOL c = ops.get(i).polymer(0, 0, 1).isEqualTo(ops.get(i + 1).polymer(0, 0, 1));
			b = alg.and(b, c);
		}

		if (ops.size() % 2 == 1)
			b = alg.and(b, ops.get(ops.size() - 1).isSatisfied(1, 0, 0));
		else
			b = alg.and(b, ops.get(ops.size() - 1).isSatisfied(1, 1, 0));

		return b;
	}

	/**
	 * Testing congruence join semi-distributivity (omitting types 1, 2 and 5).
	 * 
	 * x = d_0(x,y,y). x=d_0(x,y,x). d_0(x,x,y)=d_1(x,x,y). d_1(x,y,y) =
	 * d_2(x,y,y). d_1(x,y,x)=d_2(x,y,x). d_{n-1}(x,x,y)=y (for n odd).
	 * d_{n-1}(x,y,y)=y and d_{n-1}(x,y,x)=x (for n even).
	 */
	public static <BOOL> BOOL areSDJoinTerms(List<Operation<BOOL>> ops) {
		assert ops.size() >= 1;
		BoolAlgebra<BOOL> alg = ops.get(0).getAlg();

		for (int i = 0; i < ops.size(); i++)
			assert ops.get(i).getArity() == 3 && ops.get(i).getAlg() == alg;

		BOOL b = ops.get(0).isSatisfied(0, 1, 1);
		b = alg.and(b, ops.get(0).isSatisfied(0, 1, 0));

		for (int i = 0; i + 1 < ops.size(); i += 2) {
			BOOL c = ops.get(i).polymer(0, 0, 1).isEqualTo(ops.get(i + 1).polymer(0, 0, 1));
			b = alg.and(b, c);
		}

		for (int i = 1; i + 1 < ops.size(); i += 2) {
			BOOL c = ops.get(i).polymer(0, 1, 1).isEqualTo(ops.get(i + 1).polymer(0, 1, 1));
			BOOL d = ops.get(i).polymer(0, 1, 0).isEqualTo(ops.get(i + 1).polymer(0, 1, 0));
			b = alg.and(b, alg.and(c, d));
		}

		if (ops.size() % 2 == 1)
			b = alg.and(b, ops.get(ops.size() - 1).isSatisfied(1, 1, 0));
		else {
			b = alg.and(b, ops.get(ops.size() - 1).isSatisfied(1, 0, 0));
			b = alg.and(b, ops.get(ops.size() - 1).isSatisfied(0, 1, 0));
		}

		return b;
	}

	public Operation<BOOL> compose(Operation<BOOL> op) {
		return super.compose(op).asOperation();
	}

	public Operation<BOOL> compose(Operation<BOOL> op1, Operation<BOOL> op2) {
		return super.compose(op1, op2).asOperation();
	}

	public Operation<BOOL> compose(Operation<BOOL> op1, Operation<BOOL> op2, Operation<BOOL> op3) {
		return super.compose(op1, op2, op3).asOperation();
	}

	public Operation<BOOL> compose(Operation<BOOL>[] ops) {
		assert getArity() == ops.length && ops.length >= 1;
		return super.compose(ops).asOperation();
	}

	public Operation<BOOL> product(Operation<BOOL> op) {
		return super.product(op).asOperation();
	}

	public Operation<BOOL> power(int exp) {
		return super.power(exp).asOperation();
	}

	public Operation<BOOL> conjugate(Permutation<BOOL> perm) {
		return super.conjugate(perm).asOperation();
	}

	public static String format(Operation<Boolean> op) {
		return PartialOperation.format(op);
	}

	public static Operation<Boolean> parse(int size, int arity, String str) {
		PartialOperation<Boolean> op = PartialOperation.parse(size, arity, str);
		assert op.isOperation();
		return op.asOperation();
	}

	public static final Comparator<Operation<Boolean>> COMPARATOR = new Comparator<Operation<Boolean>>() {
		final Comparator<Tensor<Boolean>> comp = Tensor.comparator(BoolAlgebra.COMPARATOR);

		@Override
		public int compare(Operation<Boolean> o1, Operation<Boolean> o2) {
			assert o1.getSize() == o2.getSize() && o1.getArity() == o2.getArity();

			return comp.compare(o2.tensor, o1.tensor);
		}
	};

	public static void print(Operation<Boolean> op) {
		System.out.println("operation of size " + op.getSize() + " arity " + op.getArity());

		int a = op.getArity();
		boolean proj = false;
		boolean nu = false;
		boolean minor = false;

		String s = "properties:";
		if (op.isSurjective())
			s += " surjective";
		if (op.isProjection()) {
			proj = true;
			s += " projection";
		}
		if (!proj && op.isEssential())
			s += " essential";
		if (a == 1 && !proj && op.isRetraction())
			s += " retraction";
		if (op.isIdempotent())
			s += " idempotent";
		if (a == 2 && op.isCommutative())
			s += " commutative";
		if (a == 2 && op.isAssociative())
			s += " associative";
		if (a == 3 && op.isMajority()) {
			nu = true;
			s += " majority";
		}
		if (a == 3 && op.isMinority()) {
			minor = true;
			s += " minority";
		}
		if (a == 3 && !minor && op.isMaltsev())
			s += " maltsev";
		if (a >= 4 && op.isNearUnanimity()) {
			nu = true;
			s += " nu";
		}
		if (a >= 2 && !nu && op.isWeakNearUnanimity())
			s += " weak-nu";
		if (a == 2) {
			for (int i = 0; i < op.getSize(); i++)
				if (op.isZeroElement(i))
					s += " zero=" + i;
			for (int i = 0; i < op.getSize(); i++)
				if (op.isUnitElement(i))
					s += " unit=" + i;
		}

		System.out.println(s);
		System.out.println("table: " + Operation.format(op));
	}
}
