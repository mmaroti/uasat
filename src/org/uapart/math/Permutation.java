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

import org.uapart.core.*;

public class Permutation {
	private final Table table;

	public Permutation(Domain domain) {
		if (domain == null)
			throw new IllegalArgumentException();

		this.table = Table.create(domain, domain, Domain.TWO);
	}

	public Permutation(Table table) {
		if (table == null || table.getArity() != 2)
			throw new IllegalArgumentException();

		if (table.getCodomain() != table.getDomain(0)
				|| table.getDomain(1) != Domain.TWO)
			throw new IllegalArgumentException();

		this.table = table;
	}

	public Term isValid() {
		Table x = Table.create(getDomain());

		Term x0 = x.get();
		Term c0 = new Constant(getDomain(), 0);
		Term c1 = new Constant(getDomain(), 1);

		return Term.forall(x, table.of(table.of(x0, c0), c1).equ(x0));
	}

	public Domain getDomain() {
		return table.getCodomain();
	}

	public Table getTable() {
		return table;
	}

	public Term of(Term term) {
		if (term == null || term.getDomain() != getDomain())
			throw new IllegalArgumentException();

		Term c0 = new Constant(getDomain(), 0);
		return table.of(term, c0);
	}

	public Term inverseOf(Term term) {
		if (term == null || term.getDomain() != getDomain())
			throw new IllegalArgumentException();

		Term c1 = new Constant(getDomain(), 1);
		return table.of(term, c1);
	}

	public Term isIdentity() {
		Table x = Table.create(getDomain());

		Term x0 = x.get();
		Term c0 = new Constant(getDomain(), 0);

		return Term.forall(x, table.of(x0, c0).equ(x0));
	}
}
