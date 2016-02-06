/**
 *	Copyright (C) Miklos Maroti, 2016
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

package org.uapart.math;

import java.util.*;

import org.uapart.core.*;

public class Operation {
	private final Function fun;

	public Operation(Domain domain, int arity) {
		if (domain == null || arity < 0)
			throw new IllegalArgumentException();

		Domain[] ds = new Domain[arity];
		Arrays.fill(ds, domain);

		this.fun = Table.create(domain, ds);
	}

	public Operation(Function fun) {
		if (fun == null)
			throw new IllegalArgumentException();

		Domain d = fun.getCodomain();
		for (int i = 1; i < fun.getArity(); i++)
			if (fun.getDomain(i) != d)
				throw new IllegalArgumentException();

		this.fun = fun;
	}

	public int getArity() {
		return fun.getArity();
	}

	public Domain getDomain() {
		return fun.getCodomain();
	}

	public Table getTable() {
		if (fun instanceof Table)
			return (Table) fun;
		else
			throw new IllegalStateException();
	}

	public Term of(Term... subterms) {
		return fun.of(subterms);
	}

	public Term isIdempotent() {
		Table x = Table.create(getDomain());

		Term x0 = x.get();
		Term[] xs = new Term[getArity()];
		Arrays.fill(xs, x0);

		return Term.forall(x, fun.of(xs).equ(x0));
	}

	public Term isCommutative() {
		if (getArity() != 2)
			throw new IllegalArgumentException();

		Table x = Table.create(getDomain(), Domain.TWO);
		Term x0 = x.get(0);
		Term x1 = x.get(1);

		return Term.forall(x, fun.of(x0, x1).equ(fun.of(x1, x0)));
	}

	public Term isSurjective() {
		Domain d = new Domain(getArity());
		Table x = Table.create(getDomain());
		Table y = Table.create(getDomain(), d);

		Term x0 = x.get();
		Term[] ys = new Term[getArity()];
		for (int i = 0; i < ys.length; i++)
			ys[i] = y.get(i);

		return Term.forall(x, Term.exists(y, fun.of(ys).equ(x0)));
	}

	public Term isUnitElemen(int elem) {
		if (getArity() != 2 || elem < 0 || elem >= getDomain().getSize())
			throw new IllegalArgumentException();

		Table x = Table.create(getDomain());
		Term x0 = x.get();
		Term c0 = new Constant(getDomain(), elem);

		return Term.forall(x, fun.of(x0, c0).equ(x0)
				.and(fun.of(c0, x0).equ(x0)));
	}

	public Term isZeroElemen(int elem) {
		if (getArity() != 2 || elem < 0 || elem >= getDomain().getSize())
			throw new IllegalArgumentException();

		Table x = Table.create(getDomain());
		Term x0 = x.get();
		Term c0 = new Constant(getDomain(), elem);

		return Term.forall(x, fun.of(x0, c0).equ(c0)
				.and(fun.of(c0, x0).equ(c0)));
	}

	public Term isInverse(Operation inv, int unit) {
		if (getArity() != 2 || inv == null || inv.getArity() != 1
				|| getDomain() != inv.getDomain() || unit < 0
				|| unit > getDomain().getSize())
			throw new IllegalArgumentException();

		Table x = Table.create(getDomain());
		Term x0 = x.get();
		Term c0 = new Constant(getDomain(), unit);

		Term t = fun.of(x0, inv.of(x0)).equ(c0);
		t = t.and(fun.of(inv.of(x0), x0).equ(c0));
		return Term.forall(x, t);
	}

	public Term isAssociative() {
		if (getArity() != 2)
			throw new IllegalArgumentException();

		Table x = Table.create(getDomain(), Domain.THREE);

		Term x0 = x.get(0);
		Term x1 = x.get(1);
		Term x2 = x.get(2);

		Term t1 = fun.of(x0, fun.of(x1, x2));
		Term t2 = fun.of(fun.of(x0, x1), x2);
		return Term.forall(x, t1.equ(t2));
	}

	public Term isSemilattice() {
		return isIdempotent().and(isCommutative()).and(isAssociative());
	}

	public Term isMajority() {
		if (getArity() != 3)
			throw new IllegalArgumentException();

		Table x = Table.create(getDomain(), Domain.TWO);
		Term x0 = x.get(0);
		Term x1 = x.get(1);

		Term t = fun.of(x1, x0, x0).equ(x0);
		t = t.and(fun.of(x0, x1, x0).equ(x0));
		t = t.and(fun.of(x0, x0, x1).equ(x0));
		return Term.forall(x, t);
	}

	public Term isMinority() {
		if (getArity() != 3)
			throw new IllegalArgumentException();

		Table x = Table.create(getDomain(), Domain.TWO);
		Term x0 = x.get(0);
		Term x1 = x.get(1);

		Term t = fun.of(x1, x0, x0).equ(x1);
		t = t.and(fun.of(x0, x1, x0).equ(x1));
		t = t.and(fun.of(x0, x0, x1).equ(x1));
		return Term.forall(x, t);
	}

	public Term isMaltsev() {
		if (getArity() != 3)
			throw new IllegalArgumentException();

		Table x = Table.create(getDomain(), Domain.TWO);
		Term x0 = x.get(0);
		Term x1 = x.get(1);

		Term t = fun.of(x1, x0, x0).equ(x1);
		t = t.and(fun.of(x0, x0, x1).equ(x1));
		return Term.forall(x, t);
	}

	public static Term areSiggersTerms(Operation p, Operation q) {
		if (p.getArity() != 3 || q.getArity() != 3
				|| p.getDomain() != q.getDomain())
			throw new IllegalArgumentException();

		Table x = Table.create(p.getDomain(), Domain.TWO);
		Term x0 = x.get(0);
		Term x1 = x.get(1);

		Term t = p.of(x0, x0, x0).equ(x0);
		t.and(p.of(x0, x0, x1).equ(p.of(x1, x0, x0)));
		t.and(p.of(x0, x0, x1).equ(q.of(x0, x1, x1)));
		t.and(p.of(x0, x1, x0).equ(q.of(x0, x1, x0)));
		return Term.forall(x, t);
	}

	public static Term areJovanovicTerms(Operation p, Operation q) {
		if (p.getArity() != 3 || q.getArity() != 3
				|| p.getDomain() != q.getDomain())
			throw new IllegalArgumentException();

		Table x = Table.create(p.getDomain(), Domain.TWO);
		Term x0 = x.get(0);
		Term x1 = x.get(1);

		Term t = p.of(x0, x0, x0).equ(x0);
		t.and(p.of(x0, x0, x1).equ(p.of(x0, x1, x0)));
		t.and(p.of(x0, x0, x1).equ(p.of(x1, x0, x0)));
		t.and(p.of(x0, x0, x1).equ(q.of(x0, x1, x0)));
		t.and(q.of(x0, x0, x1).equ(q.of(x0, x1, x1)));
		return Term.forall(x, t);
	}

	public Term preserves(Relation rel) {
		if (rel == null || getDomain() != rel.getDomain())
			throw new IllegalArgumentException();

		int a1 = getArity();
		int a2 = rel.getArity();

		Domain d1 = new Domain(a1);
		Domain d2 = new Domain(a2);
		Table x = Table.create(getDomain(), d1, d2);

		Term[][] xs = new Term[a1][];
		for (int i = 0; i < a1; i++) {
			xs[i] = new Term[a2];
			for (int j = 0; j < a2; j++)
				xs[i][j] = x.get(i, j);
		}

		Term[] ys = new Term[a1];
		for (int i = 0; i < a1; i++)
			ys[i] = rel.of(xs[i]);
		Term t1 = Term.and(ys);

		Term[] zs = new Term[a2];
		for (int i = 0; i < a2; i++) {
			for (int j = 0; j < a1; j++)
				ys[j] = xs[j][i];
			zs[i] = fun.of(ys);
		}
		Term t2 = rel.of(zs);

		return Term.forall(x, t1.leq(t2));
	}

	public Operation conjugate(Permutation perm) {
		if (perm == null || getDomain() != perm.getDomain())
			throw new IllegalArgumentException();

		Variable[] xs = new Variable[getArity()];
		Term[] ys = new Term[xs.length];
		for (int i = 0; i < xs.length; i++) {
			xs[i] = new Variable(getDomain());
			ys[i] = perm.of(xs[i]);
		}

		Lambda lam = new Lambda(xs, perm.inverseOf(fun.of(ys)));
		return new Operation(lam);
	}

	public Term getLexicalOrder(Operation op) {
		if (op == null || getDomain() != op.getDomain()
				|| getArity() != op.getArity())
			throw new IllegalArgumentException();

		return new OrdLex(fun, op.fun);
	}

	public Term isLexMinimal() {
		Permutation perm = new Permutation(getDomain());
		Term t = getLexicalOrder(conjugate(perm)).neq(Constant.GT);
		return Term.forall(perm.getTable(), perm.isPartial().leq(t));
	}
}
