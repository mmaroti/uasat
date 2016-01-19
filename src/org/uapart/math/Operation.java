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
	private final Table table;

	public Operation(Domain domain, int arity) {
		if (domain == null || arity < 0)
			throw new IllegalArgumentException();

		Domain[] ds = new Domain[arity];
		Arrays.fill(ds, domain);

		this.table = Table.create(domain, ds);
	}

	public Operation(Table table) {
		if (table == null)
			throw new IllegalArgumentException();

		Domain d = table.getCodomain();
		for (int i = 1; i < table.getArity(); i++)
			if (table.getDomain(i) != d)
				throw new IllegalArgumentException();

		this.table = table;
	}

	public int getArity() {
		return table.getArity();
	}

	public Domain getDomain() {
		return table.getCodomain();
	}

	public Table getTable() {
		return table;
	}

	public Term of(Term... subterms) {
		return table.of(subterms);
	}

	public Term isIdempotent() {
		Table x = Table.create(getDomain());

		Term x0 = x.get();
		Term[] xs = new Term[getArity()];
		Arrays.fill(xs, x0);

		return Term.forall(x, table.of(xs).equ(x0));
	}

	public Term isCommutative() {
		if (getArity() != 2)
			throw new IllegalArgumentException();

		Table x = Table.create(getDomain(), Domain.TWO);
		Term x0 = x.get(0);
		Term x1 = x.get(1);

		return Term.forall(x, table.of(x0, x1).equ(table.of(x1, x0)));
	}

	public Term isSurjective() {
		Domain d = new Domain(getArity());
		Table x = Table.create(getDomain());
		Table y = Table.create(getDomain(), d);

		Term x0 = x.get();
		Term[] ys = new Term[getArity()];
		for (int i = 0; i < ys.length; i++)
			ys[i] = y.get(i);

		return Term.forall(x, Term.exists(y, table.of(ys).equ(x0)));
	}

	public Term isUnitElemen(int elem) {
		if (getArity() != 2 || elem < 0 || elem >= getDomain().getSize())
			throw new IllegalArgumentException();

		Table x = Table.create(getDomain());
		Term x0 = x.get();
		Term c0 = new Constant(getDomain(), elem);

		return Term.forall(x,
				table.of(x0, c0).equ(x0).and(table.of(c0, x0).equ(x0)));
	}

	public Term isZeroElemen(int elem) {
		if (getArity() != 2 || elem < 0 || elem >= getDomain().getSize())
			throw new IllegalArgumentException();

		Table x = Table.create(getDomain());
		Term x0 = x.get();
		Term c0 = new Constant(getDomain(), elem);

		return Term.forall(x,
				table.of(x0, c0).equ(c0).and(table.of(c0, x0).equ(c0)));
	}

	public Term isAssociative() {
		if (getArity() != 2)
			throw new IllegalArgumentException();

		Table x = Table.create(getDomain(), Domain.THREE);

		Term x0 = x.get(0);
		Term x1 = x.get(1);
		Term x2 = x.get(2);

		Term t1 = table.of(x0, table.of(x1, x2));
		Term t2 = table.of(table.of(x0, x1), x2);
		return Term.forall(x, t1.equ(t2));
	}

	public Term isSemilattice() {
		return isIdempotent().and(isCommutative()).and(isAssociative());
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
				ys[j] = xs[i][j];
			zs[i] = table.of(ys);
		}
		Term t2 = rel.of(zs);

		return Term.forall(x, t1.leq(t2));
	}
}
