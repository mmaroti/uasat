/**
 * Copyright (C) Miklos Maroti, 2015-2016
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

public class PartialOperation<BOOL> {
	protected final BoolAlgebra<BOOL> alg;
	protected final Tensor<BOOL> tensor;

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

	public PartialOperation(BoolAlgebra<BOOL> alg, Tensor<BOOL> tensor) {
		assert 1 <= tensor.getOrder();

		int size = tensor.getDim(0);
		for (int i = 1; i < tensor.getOrder(); i++)
			assert tensor.getDim(i) == size;

		this.alg = alg;
		this.tensor = tensor;

		if (alg == BoolAlgebra.INSTANCE)
			assert (Boolean) isPartialOperation();
	}

	public static PartialOperation<Boolean> wrap(Tensor<Boolean> tensor) {
		return new PartialOperation<Boolean>(BoolAlgebra.INSTANCE, tensor);
	}

	public static <BOOL> PartialOperation<BOOL> lift(BoolAlgebra<BOOL> alg, PartialOperation<Boolean> op) {
		return new PartialOperation<BOOL>(alg, alg.lift(op.tensor));
	}

	public BOOL isOperation() {
		return asRelation().isOperation();
	}

	public BOOL isPartialOperation() {
		return asRelation().isPartialOperation();
	}

	public Operation<BOOL> asOperation() {
		return new Operation<BOOL>(alg, tensor);
	}

	public Relation<BOOL> asRelation() {
		return new Relation<BOOL>(alg, tensor);
	}

	public Relation<BOOL> range() {
		Tensor<BOOL> tmp = asRelation().rotate(-1).getTensor();
		tmp = Tensor.fold(alg.ANY, tensor.getOrder() - 1, tmp);
		return new Relation<BOOL>(alg, tmp);
	}

	/*
	 * The first element is the result in the index
	 */
	public BOOL hasValue(int... index) {
		assert index.length == getArity() + 1;
		return tensor.getElem(index);
	}

	public BOOL isSurjective() {
		return range().isFull();
	}

	public BOOL isEqualTo(PartialOperation<BOOL> op) {
		return asRelation().isEqualTo(op.asRelation());
	}

	public PartialOperation<BOOL> polymer(int... variables) {
		assert getArity() == variables.length;

		int[] map = new int[variables.length + 1];

		int a = 0;
		for (int i = 0; i < variables.length; i++) {
			assert 0 <= variables[i];
			a = Math.max(a, variables[i] + 1);
			map[i + 1] = variables[i] + 1;
		}

		Tensor<BOOL> tmp = Tensor.reshape(tensor, Util.createShape(getSize(), 1 + a), map);
		return new PartialOperation<BOOL>(alg, tmp);
	}

	public PartialOperation<BOOL> permute(Permutation<Boolean> perm) {
		assert getArity() == perm.getSize();
		return polymer(Permutation.decode(perm));
	}

	public BOOL isPermuteMinimal() {
		List<Permutation<Boolean>> perms = Permutation.nontrivialPerms(getArity());

		BOOL b = alg.TRUE;
		for (Permutation<Boolean> p : perms)
			b = alg.and(b, isLexLeq(permute(p)));

		return b;
	}

	@SuppressWarnings("unchecked")
	public PartialOperation<BOOL> compose(PartialOperation<BOOL> op) {
		return compose(new PartialOperation[] { op });
	}

	@SuppressWarnings("unchecked")
	public PartialOperation<BOOL> compose(PartialOperation<BOOL> op1, PartialOperation<BOOL> op2) {
		return compose(new PartialOperation[] { op1, op2 });
	}

	@SuppressWarnings("unchecked")
	public PartialOperation<BOOL> compose(PartialOperation<BOOL> op1, PartialOperation<BOOL> op2,
		PartialOperation<BOOL> op3) {
		return compose(new PartialOperation[] { op1, op2, op3 });
	}

	public PartialOperation<BOOL> compose(PartialOperation<BOOL>[] ops) {
		assert getArity() == ops.length && ops.length >= 1;

		int a = getArity();
		int b = ops[0].getArity();
		Contract<BOOL> c = Contract.logical(alg);

		c.add(tensor, Contract.range(0, a + 1));
		for (int i = 0; i < ops.length; i++) {
			assert alg == ops[i].alg && getSize() == ops[i].getSize() && ops[i].getArity() == b;

			c.add(ops[i].tensor, Contract.range(1 + i, a + 1, a + b + 1));
		}
		Tensor<BOOL> t = c.get(Contract.range(0, a + 1, a + b + 1));

		return new PartialOperation<BOOL>(alg, t);
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
		else if (getArity() == 4 && rel.getArity() == 3)
			return evaluate_op4_rel3(rel);
		else if (getArity() == 4 && rel.getArity() == 4)
			return evaluate_op4_rel4(rel);
		else if (getArity() == 4 && rel.getArity() == 5)
			return evaluate_op4_rel5(rel);
		else if (getArity() == 5 && rel.getArity() == 3)
			return evaluate_op5_rel3(rel);
		else if (getArity() == 5 && rel.getArity() == 4)
			return evaluate_op5_rel4(rel);

		throw new UnsupportedOperationException("not implemented for these arities");
	}

	private Relation<BOOL> evaluate_op0(int arity) {
		assert getArity() == 0;
		Tensor<BOOL> t = Tensor.diagonal(alg.getType(), tensor, new int[arity], alg.FALSE);
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
		c.add(tensor, "xafk");
		c.add(rel.getTensor(), "abcde");
		c.add(tensor, "ybgl");
		c.add(rel.getTensor(), "fghij");
		c.add(tensor, "zchm");
		c.add(rel.getTensor(), "klmno");
		c.add(tensor, "udin");
		c.add(tensor, "vejo");
		Tensor<BOOL> t = c.get("xyzuv");

		return new Relation<BOOL>(alg, t);
	}

	private Relation<BOOL> evaluate_op4_rel3(Relation<BOOL> rel) {
		assert getArity() == 4 && rel.getArity() == 3;
		Contract<BOOL> c = Contract.logical(alg);

		// the order matters for performance
		c.add(rel.getTensor(), "abc");
		c.add(tensor, "xadgj");
		c.add(rel.getTensor(), "def");
		c.add(rel.getTensor(), "ghi");
		c.add(tensor, "ybehk");
		c.add(rel.getTensor(), "jkl");
		c.add(tensor, "zcfil");
		Tensor<BOOL> t = c.get("xyz");

		return new Relation<BOOL>(alg, t);
	}

	private Relation<BOOL> evaluate_op4_rel4(Relation<BOOL> rel) {
		assert getArity() == 4 && rel.getArity() == 4;
		Contract<BOOL> c = Contract.logical(alg);

		// the order matters for performance
		c.add(rel.getTensor(), "abcd");
		c.add(tensor, "xaeim");
		c.add(rel.getTensor(), "efgh");
		c.add(tensor, "ybfjn");
		c.add(rel.getTensor(), "ijkl");
		c.add(tensor, "zcgko");
		c.add(rel.getTensor(), "mnop");
		c.add(tensor, "udhlp");
		Tensor<BOOL> t = c.get("xyzu");

		return new Relation<BOOL>(alg, t);
	}

	private Relation<BOOL> evaluate_op4_rel5(Relation<BOOL> rel) {
		assert getArity() == 4 && rel.getArity() == 5;
		Contract<BOOL> c = Contract.logical(alg);

		// the order matters for performance
		c.add(tensor, "xafkp");
		c.add(rel.getTensor(), "abcde");
		c.add(tensor, "ybglq");
		c.add(rel.getTensor(), "fghij");
		c.add(tensor, "zchmr");
		c.add(rel.getTensor(), "klmno");
		c.add(tensor, "udins");
		c.add(rel.getTensor(), "pqrst");
		c.add(tensor, "vejot");
		Tensor<BOOL> t = c.get("xyzuv");

		return new Relation<BOOL>(alg, t);
	}

	private Relation<BOOL> evaluate_op5_rel3(Relation<BOOL> rel) {
		assert getArity() == 5 && rel.getArity() == 3;
		Contract<BOOL> c = Contract.logical(alg);

		// the order matters for performance
		c.add(rel.getTensor(), "abc");
		c.add(tensor, "xadgjm");
		c.add(rel.getTensor(), "def");
		c.add(rel.getTensor(), "ghi");
		c.add(tensor, "ybehkn");
		c.add(rel.getTensor(), "jkl");
		c.add(rel.getTensor(), "mno");
		c.add(tensor, "zcfilo");
		Tensor<BOOL> t = c.get("xyz");

		return new Relation<BOOL>(alg, t);
	}

	private Relation<BOOL> evaluate_op5_rel4(Relation<BOOL> rel) {
		assert getArity() == 5 && rel.getArity() == 4;
		Contract<BOOL> c = Contract.logical(alg);

		// the order matters for performance
		c.add(rel.getTensor(), "abcd");
		c.add(tensor, "xaeimq");
		c.add(rel.getTensor(), "efgh");
		c.add(tensor, "ybfjnr");
		c.add(rel.getTensor(), "ijkl");
		c.add(rel.getTensor(), "mnop");
		c.add(tensor, "zcgkos");
		c.add(rel.getTensor(), "qrst");
		c.add(tensor, "udhlpt");
		Tensor<BOOL> t = c.get("xyzu");

		return new Relation<BOOL>(alg, t);
	}

	public BOOL preserves(Relation<BOOL> rel) {
		if (getArity() == 0)
			return asRelation().isSubsetOf(rel.diagonal());
		else
			return evaluate(rel).isSubsetOf(rel);
	}

	public PartialOperation<BOOL> product(PartialOperation<BOOL> op) {
		return asRelation().product(op.asRelation()).asPartialOperation();
	}

	public PartialOperation<BOOL> power(int exp) {
		return asRelation().power(exp).asPartialOperation();
	}

	public PartialOperation<BOOL> conjugate(Permutation<BOOL> perm) {
		return asRelation().conjugate(perm).asPartialOperation();
	}

	public BOOL isLexLeq(PartialOperation<BOOL> op) {
		assert getAlg() == op.getAlg() && getSize() == op.getSize() && getArity() == op.getArity();

		return alg.lexLeq(tensor, op.tensor);
	}

	public BOOL isLexLess(PartialOperation<BOOL> op) {
		assert getAlg() == op.getAlg() && getSize() == op.getSize() && getArity() == op.getArity();

		return alg.lexLess(tensor, op.tensor);
	}

	public static String format(PartialOperation<Boolean> op) {
		return Relation.format(op.asRelation());
	}

	public static PartialOperation<Boolean> parse(int size, int arity, String str) {
		assert size >= 1 && arity >= 0;

		Relation<Boolean> rel = Relation.parse(size, arity, str);
		assert rel.isPartialOperation();

		return rel.asPartialOperation();
	}

	@Override
	public boolean equals(Object other) {
		@SuppressWarnings("unchecked")
		PartialOperation<BOOL> op = (PartialOperation<BOOL>) other;
		assert alg == op.alg;

		return tensor.equals(op.tensor);
	}

	public static final Comparator<PartialOperation<Boolean>> COMPARATOR = new Comparator<PartialOperation<Boolean>>() {
		final Comparator<Tensor<Boolean>> comp = Tensor.comparator(BoolAlgebra.COMPARATOR);

		@Override
		public int compare(PartialOperation<Boolean> o1, PartialOperation<Boolean> o2) {
			assert o1.getSize() == o2.getSize() && o1.getArity() == o2.getArity();

			return comp.compare(o2.tensor, o1.tensor);
		}
	};
}
