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
	private final Table table;

	public Relation(Domain domain, int arity) {
		if (domain == null || arity < 0)
			throw new IllegalArgumentException();

		Domain[] ds = new Domain[arity];
		Arrays.fill(ds, domain);

		this.table = Table.create(Domain.BOOL, ds);
	}

	public Relation(Table table) {
		if (table == null || table.getCodomain() != Domain.BOOL)
			throw new IllegalArgumentException();

		Domain d = table.getDomain(0);
		for (int i = 1; i < table.getArity(); i++)
			if (table.getDomain(i) != d)
				throw new IllegalArgumentException();

		this.table = table;
	}

	public int getArity() {
		return table.getArity();
	}

	public Domain getDomain() {
		return table.getDomain(0);
	}

	public Table getTable() {
		return table;
	}

	public Term of(Term... subterms) {
		return table.of(subterms);
	}

	public Term isFull() {
		Domain d = new Domain(getArity());
		Table x = Table.create(getDomain(), d);

		Term[] xs = new Term[getArity()];
		for (int i = 0; i < xs.length; i++)
			xs[i] = x.get(i);

		return Term.forall(x, table.of(xs));
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

		return Term.forall(x, table.of(xs).equ(rel.table.of(xs)));
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

		return Term.forall(x, table.of(xs).leq(rel.table.of(xs)));
	}

	public Term isEmpty() {
		Domain d = new Domain(getArity());
		Table x = Table.create(getDomain(), d);

		Term[] xs = new Term[getArity()];
		for (int i = 0; i < xs.length; i++)
			xs[i] = x.get(i);

		return Term.exists(x, table.of(xs)).not();
	}

	public Term isReflexive() {
		Table x = Table.create(getDomain());

		Term[] xs = new Term[getArity()];
		Arrays.fill(xs, x.get());

		return Term.forall(x, table.of(xs));
	}

	public Term isSymmetric() {
		if (getArity() != 2)
			throw new IllegalArgumentException();

		Table x = Table.create(getDomain(), Domain.TWO);

		Term x0 = x.get(0);
		Term x1 = x.get(1);

		return Term.forall(x, table.of(x0, x1).leq(table.of(x1, x0)));
	}

	public Term isAntiSymmetric() {
		if (getArity() != 2)
			throw new IllegalArgumentException();

		Table x = Table.create(getDomain(), Domain.TWO);

		Term x0 = x.get(0);
		Term x1 = x.get(1);

		return Term.forall(x,
				table.of(x0, x1).and(table.of(x1, x0)).leq(x0.equ(x1)));
	}

	public Term isTransitive() {
		if (getArity() != 2)
			throw new IllegalArgumentException();

		Table x = Table.create(getDomain(), Domain.THREE);

		Term x0 = x.get(0);
		Term x1 = x.get(1);
		Term x2 = x.get(2);

		return Term.forall(x,
				table.of(x0, x1).and(table.of(x1, x2)).leq(table.of(x0, x2)));
	}

	public Term isEquivalence() {
		return isReflexive().and(isSymmetric()).and(isTransitive());
	}

	public Term isPartialOrder() {
		return isReflexive().and(isAntiSymmetric()).and(isTransitive());
	}
}
