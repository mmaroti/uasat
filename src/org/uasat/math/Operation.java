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

public final class Operation<BOOL> {
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
		return tensor.getOrder() - 1;
	}

	public Operation(BoolAlgebra<BOOL> alg, Tensor<BOOL> tensor) {
		assert 1 <= tensor.getOrder();

		int size = tensor.getDim(0);
		for (int i = 1; i < tensor.getOrder(); i++)
			assert tensor.getDim(i) == size;

		this.alg = alg;
		this.tensor = tensor;

		if (alg == BoolAlgebra.INSTANCE)
			assert (Boolean) isOperation();
	}

	public static Operation<Boolean> wrap(Tensor<Boolean> tensor) {
		return new Operation<Boolean>(BoolAlgebra.INSTANCE, tensor);
	}

	public BOOL isOperation() {
		return asRelation().isOperation();
	}

	public Relation<BOOL> asRelation() {
		return new Relation<BOOL>(alg, tensor);
	}

	public Relation<BOOL> range() {
		Tensor<BOOL> tmp = asRelation().rotate(-1).getTensor();
		tmp = Tensor.fold(alg.ANY, tensor.getOrder() - 1, tmp);
		return new Relation<BOOL>(alg, tmp);
	}

	public BOOL hasValue(int... index) {
		assert index.length == getArity() + 1;
		return tensor.getElem(index);
	}

	public BOOL isSurjective() {
		return range().isFull();
	}

	private static int[] createShape(int size, int arity) {
		assert size > 1 && arity >= 0;

		int[] shape = new int[arity];
		for (int i = 0; i < arity; i++)
			shape[i] = size;
		return shape;
	}

	public static Operation<Boolean> projection(int size, int arity,
			final int coord) {
		assert 0 <= coord && coord < arity;

		Tensor<Boolean> tensor = Tensor.generate(createShape(size, 1 + arity),
				new Func1<Boolean, int[]>() {
					@Override
					public Boolean call(int[] elem) {
						return elem[0] == elem[1 + coord];
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

		Tensor<BOOL> tmp = Tensor.reshape(tensor,
				createShape(getSize(), getArity()), map);
		return Tensor.fold(alg.ALL, tmp.getOrder(), tmp).get();
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

		Tensor<BOOL> tmp = Tensor.reshape(tensor,
				createShape(getSize(), 1 + a), map);
		return new Operation<BOOL>(alg, tmp);
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

		Tensor<BOOL> tmp = Tensor.reshape(tensor,
				createShape(getSize(), 1 + a), map);
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
		return alg.and(alg.and(isIdempotent(), isCommutative()),
				isAssociative());
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

	@SuppressWarnings("unchecked")
	public Operation<BOOL> compose(Operation<BOOL> op) {
		return compose(new Operation[] { op });
	}

	@SuppressWarnings("unchecked")
	public Operation<BOOL> compose(Operation<BOOL> op1, Operation<BOOL> op2) {
		return compose(new Operation[] { op1, op2 });
	}

	@SuppressWarnings("unchecked")
	public Operation<BOOL> compose(Operation<BOOL> op1, Operation<BOOL> op2,
			Operation<BOOL> op3) {
		return compose(new Operation[] { op1, op2, op3 });
	}

	public Operation<BOOL> compose(Operation<BOOL>[] ops) {
		assert getArity() == ops.length;

		if (getArity() == 0)
			return this;

		int a = getArity();
		int b = ops[0].getArity();
		Contract<BOOL> c = Contract.logical(alg);

		c.add(tensor, Contract.range(0, a + 1));
		for (int i = 0; i < ops.length; i++) {
			assert alg == ops[i].alg && getSize() == ops[i].getSize()
					&& ops[i].getArity() == ops[0].getArity();

			c.add(ops[i].tensor, Contract.range(1 + i, a + 1, a + b + 1));
		}
		Tensor<BOOL> t = c.get(Contract.range(0, a + 1, a + b + 1));

		return new Operation<BOOL>(alg, t);
	}

	public static <BOOL> Operation<BOOL> lift(BoolAlgebra<BOOL> alg,
			Operation<Boolean> op) {
		Tensor<BOOL> tensor = Tensor.map(alg.LIFT, op.tensor);
		return new Operation<BOOL>(alg, tensor);
	}

	public Relation<BOOL> evaluate(Relation<BOOL> rel) {
		assert alg == rel.getAlg();

		if (getArity() == 0)
			return evaluate_op0(rel.getArity());
		else if (rel.getArity() == 1)
			return evaluate_rel1(rel);
		else if (getArity() == 1)
			return evaluate_op1(rel);
		else if (rel.getArity() == 2)
			return evaluate_rel2(rel);
		else if (getArity() == 2 && rel.getArity() == 3)
			return evaluate_op2_rel3(rel);
		else if (getArity() == 2 && rel.getArity() == 4)
			return evaluate_op2_rel4(rel);
		else if (getArity() == 3 && rel.getArity() == 3)
			return evaluate_op3_rel3(rel);
		else if (getArity() == 3 && rel.getArity() == 4)
			return evaluate_op3_rel4(rel);

		throw new IllegalArgumentException("not implemented for these arities");
	}

	private Relation<BOOL> evaluate_op0(int arity) {
		assert getArity() == 0;
		Tensor<BOOL> t = Tensor.diagonal(tensor, new int[arity], alg.FALSE);
		return new Relation<BOOL>(alg, t);
	}

	private Relation<BOOL> evaluate_rel1(Relation<BOOL> rel) {
		assert rel.getArity() == 1;
		int a = getArity();
		Contract<BOOL> c = Contract.logical(alg);

		c.add(tensor, Contract.range(0, a));
		for (int i = 1; i < a; i++)
			c.add(rel.getTensor(), i);
		Tensor<BOOL> t = c.get(0);

		return new Relation<BOOL>(alg, t);
	}

	private Relation<BOOL> evaluate_op1(Relation<BOOL> rel) {
		assert getArity() == 1;
		int a = rel.getArity();
		Contract<BOOL> c = Contract.logical(alg);

		c.add(rel.getTensor(), Contract.range(a, 2 * a));
		for (int i = 0; i < a; i++)
			c.add(tensor, i, i + a);
		Tensor<BOOL> t = c.get(Contract.range(0, a));

		return new Relation<BOOL>(alg, t);
	}

	private Relation<BOOL> evaluate_rel2(Relation<BOOL> rel) {
		assert rel.getArity() == 2;
		int a = getArity() + 1;
		Contract<BOOL> c = Contract.logical(alg);

		c.add(tensor, Contract.range(0, a));
		for (int i = 1; i < a; i++)
			c.add(rel.getTensor(), i, i + a);
		c.add(tensor, Contract.range(a, 2 * a));
		Tensor<BOOL> t = c.get(0, a);

		return new Relation<BOOL>(alg, t);
	}

	private Relation<BOOL> evaluate_op2_rel3(Relation<BOOL> rel) {
		assert getArity() == 2 && rel.getArity() == 3;
		Contract<BOOL> c = Contract.logical(alg);

		// the order matters for performance
		c.add(tensor, "xad");
		c.add(rel.getTensor(), "abc");
		c.add(tensor, "ybe");
		c.add(rel.getTensor(), "def");
		c.add(tensor, "zcf");
		Tensor<BOOL> t = c.get("xyz");

		return new Relation<BOOL>(alg, t);
	}

	private Relation<BOOL> evaluate_op2_rel4(Relation<BOOL> rel) {
		assert getArity() == 2 && rel.getArity() == 4;
		Contract<BOOL> c = Contract.logical(alg);

		// the order matters for performance
		c.add(rel.getTensor(), "abcd");
		c.add(tensor, "xae");
		c.add(tensor, "ybf");
		c.add(rel.getTensor(), "efgh");
		c.add(tensor, "zcg");
		c.add(tensor, "udh");
		Tensor<BOOL> t = c.get("xyzu");

		return new Relation<BOOL>(alg, t);
	}

	private Relation<BOOL> evaluate_op3_rel3(Relation<BOOL> rel) {
		assert getArity() == 3 && rel.getArity() == 3;
		Contract<BOOL> c = Contract.logical(alg);

		// the order matters for performance
		c.add(rel.getTensor(), "abc");
		c.add(tensor, "xadg");
		c.add(rel.getTensor(), "def");
		c.add(tensor, "ybeh");
		c.add(rel.getTensor(), "ghi");
		c.add(tensor, "zcfi");
		Tensor<BOOL> t = c.get("xyz");

		return new Relation<BOOL>(alg, t);
	}

	private Relation<BOOL> evaluate_op3_rel4(Relation<BOOL> rel) {
		assert getArity() == 3 && rel.getArity() == 4;
		Contract<BOOL> c = Contract.logical(alg);

		// the order matters for performance
		c.add(tensor, "xaei");
		c.add(rel.getTensor(), "abcd");
		c.add(tensor, "ybfj");
		c.add(rel.getTensor(), "efgh");
		c.add(tensor, "zcgk");
		c.add(rel.getTensor(), "ijkl");
		c.add(tensor, "udhl");
		Tensor<BOOL> t = c.get("xyzu");

		return new Relation<BOOL>(alg, t);
	}

	public BOOL preserves(Relation<BOOL> rel) {
		if (getArity() == 0)
			return asRelation().isSubsetOf(rel.diagonal());
		else
			return evaluate(rel).isSubsetOf(rel);
	}

	public Operation<BOOL> product(Operation<BOOL> op) {
		return asRelation().product(op.asRelation()).asOperation();
	}

	public Operation<BOOL> power(int exp) {
		return asRelation().power(exp).asOperation();
	}

	public Operation<BOOL> conjugate(Permutation<BOOL> perm) {
		return asRelation().conjugate(perm).asOperation();
	}

	public BOOL isLexLeq(Operation<BOOL> op) {
		assert getAlg() == op.getAlg() && getSize() == op.getSize()
				&& getArity() == op.getArity();

		return alg.lexLeq(getTensor(), op.getTensor());
	}

	public BOOL isLexLess(Operation<BOOL> op) {
		assert getAlg() == op.getAlg() && getSize() == op.getSize()
				&& getArity() == op.getArity();

		return alg.lexLess(getTensor(), op.getTensor());
	}

	public static Tensor<Integer> decode(Operation<Boolean> op) {
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

		return Tensor.fold(lookup, 1, op.getTensor());
	}

	public static String format(Operation<Boolean> op) {
		int size = op.getSize();
		Tensor<Integer> tensor = decode(op);

		String s = "";
		int c = 0;
		for (Integer elem : tensor) {
			if (++c > size) {
				c = 1;
				s += ' ';
			}
			s += Relation.formatIndex(elem);
		}

		return s;
	}
}
