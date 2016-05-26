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

	public static List<Operation<Boolean>> wrap(
			Iterable<Tensor<Boolean>> tensors) {
		List<Operation<Boolean>> list = new ArrayList<Operation<Boolean>>();
		for (Tensor<Boolean> t : tensors)
			list.add(wrap(t));
		return list;
	}

	public static <BOOL> Operation<BOOL> lift(BoolAlgebra<BOOL> alg,
			Operation<Boolean> op) {
		return new Operation<BOOL>(alg, alg.lift(op.tensor));
	}

	public static <BOOL> List<Operation<BOOL>> lift(BoolAlgebra<BOOL> alg,
			Iterable<Operation<Boolean>> ops) {
		List<Operation<BOOL>> list = new ArrayList<Operation<BOOL>>();
		for (Operation<Boolean> op : ops)
			list.add(lift(alg, op));
		return list;
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
		assert size >= 1 && arity >= 0;

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

	public static Operation<Boolean> moduloAdd(final int size) {
		Tensor<Boolean> tensor = Tensor.generate(
				new int[] { size, size, size }, new Func1<Boolean, int[]>() {
					@Override
					public Boolean call(int[] elem) {
						return elem[0] == (elem[1] + elem[2]) % size;
					}
				});
		return Operation.wrap(tensor);
	}

	public static Operation<Boolean> moduloMul(final int size) {
		Tensor<Boolean> tensor = Tensor.generate(
				new int[] { size, size, size }, new Func1<Boolean, int[]>() {
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

		Tensor<BOOL> tmp = Tensor.reshape(tensor,
				createShape(getSize(), getArity()), map);
		return Tensor.fold(alg.ALL, tmp.getOrder(), tmp).get();
	}

	public BOOL isProjection() {
		BOOL b = alg.TRUE;
		for (int i = 0; i < getArity(); i++)
			b = alg.and(b, isProjection(i));

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

		Tensor<BOOL> tmp = Tensor.reshape(tensor,
				createShape(getSize(), 1 + a), map);
		return new Operation<BOOL>(alg, tmp);
	}

	public Operation<BOOL> permute(Permutation<Boolean> perm) {
		assert getArity() == perm.getSize();
		return polymer(Permutation.decode(perm));
	}

	public BOOL isPermuteMinimal() {
		List<Permutation<Boolean>> perms = Permutation
				.nontrivialPerms(getArity());

		BOOL b = alg.TRUE;
		for (Permutation<Boolean> p : perms)
			b = alg.and(b, isLexLeq(permute(p)));

		return b;
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

	/**
	 * Testing Taylor property (omitting type 1):
	 * 
	 * p(x,x,x) = x. p(x,x,y) = p(y,x,x) = q(x,y,y). p(x,y,x) = q(x,y,x).
	 */
	public static <BOOL> BOOL areSiggersTerms(Operation<BOOL> p,
			Operation<BOOL> q) {
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
	public static <BOOL> BOOL areJovanovicTerms(Operation<BOOL> p,
			Operation<BOOL> q) {
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
			BOOL c = ops.get(i).polymer(0, 1, 1)
					.isEqualTo(ops.get(i + 1).polymer(0, 1, 1));
			b = alg.and(b, c);
		}

		for (int i = 1; i + 1 < ops.size(); i += 2) {
			BOOL c = ops.get(i).polymer(0, 0, 1)
					.isEqualTo(ops.get(i + 1).polymer(0, 0, 1));
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
			BOOL c = ops.get(i).polymer(0, 0, 1)
					.isEqualTo(ops.get(i + 1).polymer(0, 0, 1));
			b = alg.and(b, c);
		}

		for (int i = 1; i + 1 < ops.size(); i += 2) {
			BOOL c = ops.get(i).polymer(0, 1, 1)
					.isEqualTo(ops.get(i + 1).polymer(0, 1, 1));
			BOOL d = ops.get(i).polymer(0, 1, 0)
					.isEqualTo(ops.get(i + 1).polymer(0, 1, 0));
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
		assert getArity() == ops.length && ops.length >= 1;

		int a = getArity();
		int b = ops[0].getArity();
		Contract<BOOL> c = Contract.logical(alg);

		c.add(tensor, Contract.range(0, a + 1));
		for (int i = 0; i < ops.length; i++) {
			assert alg == ops[i].alg && getSize() == ops[i].getSize()
					&& ops[i].getArity() == b;

			c.add(ops[i].tensor, Contract.range(1 + i, a + 1, a + b + 1));
		}
		Tensor<BOOL> t = c.get(Contract.range(0, a + 1, a + b + 1));

		return new Operation<BOOL>(alg, t);
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
		else if (getArity() == 2 && rel.getArity() == 5)
			return evaluate_op2_rel5(rel);
		else if (getArity() == 2 && rel.getArity() == 6)
			return evaluate_op2_rel6(rel);
		else if (getArity() == 3 && rel.getArity() == 3)
			return evaluate_op3_rel3(rel);
		else if (getArity() == 3 && rel.getArity() == 4)
			return evaluate_op3_rel4(rel);
		else if (getArity() == 3 && rel.getArity() == 5)
			return evaluate_op3_rel5(rel);

		throw new UnsupportedOperationException(
				"not implemented for these arities");
	}

	private Relation<BOOL> evaluate_op0(int arity) {
		assert getArity() == 0;
		Tensor<BOOL> t = Tensor.diagonal(alg.type, tensor, new int[arity],
				alg.FALSE);
		return new Relation<BOOL>(alg, t);
	}

	private Relation<BOOL> evaluate_rel1(Relation<BOOL> rel) {
		assert rel.getArity() == 1;
		int a = getArity();
		Contract<BOOL> c = Contract.logical(alg);

		c.add(tensor, Contract.range(0, a + 1));
		for (int i = 1; i <= a; i++)
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

	private Relation<BOOL> evaluate_op2_rel5(Relation<BOOL> rel) {
		assert getArity() == 2 && rel.getArity() == 5;
		Contract<BOOL> c = Contract.logical(alg);

		// the order matters for performance
		c.add(rel.getTensor(), "abcde");
		c.add(tensor, "xaf");
		c.add(tensor, "ybg");
		c.add(rel.getTensor(), "fghij");
		c.add(tensor, "zch");
		c.add(tensor, "udi");
		c.add(tensor, "vej");
		Tensor<BOOL> t = c.get("xyzuv");

		return new Relation<BOOL>(alg, t);
	}

	private Relation<BOOL> evaluate_op2_rel6(Relation<BOOL> rel) {
		assert getArity() == 2 && rel.getArity() == 6;
		Contract<BOOL> c = Contract.logical(alg);

		// the order matters for performance
		c.add(rel.getTensor(), "abcdef");
		c.add(tensor, "xag");
		c.add(tensor, "ybh");
		c.add(tensor, "zci");
		c.add(rel.getTensor(), "ghijkl");
		c.add(tensor, "udj");
		c.add(tensor, "vek");
		c.add(tensor, "wfl");
		Tensor<BOOL> t = c.get("xyzuvw");

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

	private Relation<BOOL> evaluate_op3_rel5(Relation<BOOL> rel) {
		assert getArity() == 3 && rel.getArity() == 5;
		Contract<BOOL> c = Contract.logical(alg);

		// the order matters for performance
		c.add(rel.getTensor(), "abcde");
		c.add(rel.getTensor(), "fghij");
		c.add(tensor, "xafk");
		c.add(tensor, "ybgl");
		c.add(tensor, "zchm");
		c.add(tensor, "udin");
		c.add(rel.getTensor(), "klmno");
		c.add(tensor, "vejo");
		Tensor<BOOL> t = c.get("xyzuv");

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

		return alg.lexLeq(tensor, op.tensor);
	}

	public BOOL isLexLess(Operation<BOOL> op) {
		assert getAlg() == op.getAlg() && getSize() == op.getSize()
				&& getArity() == op.getArity();

		return alg.lexLess(tensor, op.tensor);
	}

	public static Tensor<Integer> decode(Operation<Boolean> op) {
		assert op.isOperation();

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
				throw new IllegalArgumentException("this cannot happen");
			}
		};

		return Tensor.fold(Integer.TYPE, lookup, 1, op.tensor);
	}

	public static Operation<Boolean> encode(int size,
			final Tensor<Integer> tensor) {
		assert size >= 1;

		int arity = tensor.getOrder();
		for (int i = 0; i < arity; i++)
			assert tensor.getDim(i) == size;

		final int[] index = new int[arity];
		Func1<Boolean, int[]> lookup = new Func1<Boolean, int[]>() {
			@Override
			public Boolean call(int[] elem) {
				System.arraycopy(elem, 1, index, 0, index.length);
				return tensor.getElem(index) == elem[0];
			}
		};

		int[] shape = Util.createShape(size, arity + 1);
		return Operation.wrap(Tensor.generate(shape, lookup));
	}

	public static String format(Operation<Boolean> op) {
		StringBuilder s = new StringBuilder();

		int size = op.getSize();
		Tensor<Integer> tensor = decode(op);

		if (op.getArity() == 0)
			s.append(Util.formatElement(size, tensor.get()));
		else {
			int[] tuple = new int[size];
			Iterator<Integer> iter = tensor.iterator();
			while (iter.hasNext()) {
				for (int i = 0; i < size; i++)
					tuple[i] = iter.next();

				if (s.length() != 0)
					s.append(' ');
				s.append(Util.formatTuple(size, tuple));
			}
		}

		return s.toString();
	}

	public static Operation<Boolean> parse(int size, int arity, String str) {
		assert size >= 1 && arity >= 0;

		Tensor<Integer> tensor = Tensor.constant(Integer.TYPE,
				Util.createShape(size, arity), 0);

		if (arity == 0)
			tensor.setElem(Util.parseElement(size, str));
		else {
			Iterator<int[]> iter = Util.cubeIterator(size, arity);
			for (String s : str.split(" ")) {
				int[] tuple = Util.parseTuple(size, s);
				if (tuple.length != size || !iter.hasNext())
					throw new IllegalArgumentException();

				for (int i = 0; i < size; i++)
					tensor.setElem(tuple[i], iter.next());
			}
			if (iter.hasNext())
				throw new IllegalArgumentException();
		}

		return encode(size, tensor);
	}

	@Override
	public boolean equals(Object other) {
		@SuppressWarnings("unchecked")
		Operation<BOOL> op = (Operation<BOOL>) other;
		assert alg == op.alg;

		return tensor.equals(op.tensor);
	}

	public static final Comparator<Operation<Boolean>> COMPARATOR = new Comparator<Operation<Boolean>>() {
		final Comparator<Tensor<Boolean>> comp = Tensor
				.comparator(BoolAlgebra.COMPARATOR);

		@Override
		public int compare(Operation<Boolean> o1, Operation<Boolean> o2) {
			assert o1.getSize() == o2.getSize()
					&& o1.getArity() == o2.getArity();

			return comp.compare(o2.tensor, o1.tensor);
		}
	};

	public static void print(Operation<Boolean> op) {
		System.out.println("operation of size " + op.getSize() + " arity "
				+ op.getArity());

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
