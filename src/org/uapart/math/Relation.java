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

public class Relation {
	private final Function fun;

	public Relation(Domain domain, int arity) {
		if (domain == null || arity < 0)
			throw new IllegalArgumentException();

		Domain[] ds = new Domain[arity];
		Arrays.fill(ds, domain);

		this.fun = Table.create(Domain.BOOL, ds);
	}

	public Relation(Function fun) {
		if (fun == null || fun.getCodomain() != Domain.BOOL)
			throw new IllegalArgumentException();

		Domain d = fun.getDomain(0);
		for (int i = 1; i < fun.getArity(); i++)
			if (fun.getDomain(i) != d)
				throw new IllegalArgumentException();

		this.fun = fun;
	}

	public int getArity() {
		return fun.getArity();
	}

	public Domain getDomain() {
		return fun.getDomain(0);
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

	public Term isFull() {
		Domain d = new Domain(getArity());
		Table x = Table.create(getDomain(), d);

		Term[] xs = new Term[getArity()];
		for (int i = 0; i < xs.length; i++)
			xs[i] = x.get(i);

		return Term.forall(x, fun.of(xs));
	}

	public Term isEqualTo(Relation rel) {
		if (rel == null || rel.getDomain() != getDomain()
				|| rel.getArity() != getArity())
			throw new IllegalArgumentException();

		Domain d = new Domain(getArity());
		Table x = Table.create(getDomain(), d);

		Term[] xs = new Term[getArity()];
		for (int i = 0; i < xs.length; i++)
			xs[i] = x.get(i);

		return Term.forall(x, fun.of(xs).equ(rel.of(xs)));
	}

	public Term isSubsetOf(Relation rel) {
		if (rel == null || rel.getDomain() != getDomain()
				|| rel.getArity() != getArity())
			throw new IllegalArgumentException();

		Domain d = new Domain(getArity());
		Table x = Table.create(getDomain(), d);

		Term[] xs = new Term[getArity()];
		for (int i = 0; i < xs.length; i++)
			xs[i] = x.get(i);

		return Term.forall(x, fun.of(xs).leq(rel.of(xs)));
	}

	public Term isEmpty() {
		Domain d = new Domain(getArity());
		Table x = Table.create(getDomain(), d);

		Term[] xs = new Term[getArity()];
		for (int i = 0; i < xs.length; i++)
			xs[i] = x.get(i);

		return Term.exists(x, fun.of(xs)).not();
	}

	public Term isReflexive() {
		Table x = Table.create(getDomain());

		Term[] xs = new Term[getArity()];
		Arrays.fill(xs, x.get());

		return Term.forall(x, fun.of(xs));
	}

	public Term isSymmetric() {
		if (getArity() != 2)
			throw new IllegalArgumentException();

		Table x = Table.create(getDomain(), Domain.TWO);

		Term x0 = x.get(0);
		Term x1 = x.get(1);

		return Term.forall(x, fun.of(x0, x1).leq(fun.of(x1, x0)));
	}

	public Term isAntiSymmetric() {
		if (getArity() != 2)
			throw new IllegalArgumentException();

		Table x = Table.create(getDomain(), Domain.TWO);

		Term x0 = x.get(0);
		Term x1 = x.get(1);

		return Term.forall(x, fun.of(x0, x1).and(fun.of(x1, x0))
				.leq(x0.equ(x1)));
	}

	public Term isTransitive() {
		if (getArity() != 2)
			throw new IllegalArgumentException();

		Table x = Table.create(getDomain(), Domain.THREE);

		Term x0 = x.get(0);
		Term x1 = x.get(1);
		Term x2 = x.get(2);

		return Term.forall(x,
				fun.of(x0, x1).and(fun.of(x1, x2)).leq(fun.of(x0, x2)));
	}

	public Term isEquivalence() {
		return isReflexive().and(isSymmetric()).and(isTransitive());
	}

	public Term isPartialOrder() {
		return isReflexive().and(isAntiSymmetric()).and(isTransitive());
	}

	public Relation intersection(Relation rel) {
		if (rel == null || getDomain() != rel.getDomain()
				|| getArity() != rel.getArity())
			throw new IllegalArgumentException();

		Variable[] xs = new Variable[getArity()];
		for (int i = 0; i < xs.length; i++)
			xs[i] = new Variable(getDomain());

		Lambda lam = new Lambda(xs, fun.of(xs).and(rel.of(xs)));
		return new Relation(lam);
	}

	public Relation conjugate(Permutation perm) {
		if (perm == null || getDomain() != perm.getDomain())
			throw new IllegalArgumentException();

		Variable[] xs = new Variable[getArity()];
		Term[] ys = new Term[xs.length];
		for (int i = 0; i < xs.length; i++) {
			xs[i] = new Variable(getDomain());
			ys[i] = perm.of(xs[i]);
		}

		Lambda lam = new Lambda(xs, fun.of(ys));
		return new Relation(lam);
	}

	public Term getLexicalOrder(Relation rel) {
		if (rel == null || getDomain() != rel.getDomain()
				|| getArity() != rel.getArity())
			throw new IllegalArgumentException();

		return new OrdLex(fun, rel.fun);
	}

	public Term isLexMinimal() {
		Permutation perm = new Permutation(getDomain());
		Term t = getLexicalOrder(conjugate(perm)).neq(Constant.GT);
		return Term.forall(perm.getTable(), perm.isPartial().leq(t));
	}
}
