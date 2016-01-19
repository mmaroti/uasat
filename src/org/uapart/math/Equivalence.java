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

public class Equivalence {
	private final Table table;

	public Equivalence(Domain domain) {
		if (domain == null)
			throw new IllegalArgumentException();

		this.table = Table.create(domain, domain);
	}

	public Equivalence(Table table) {
		if (table == null || table.getArity() != 1
				|| table.getDomain(0) != table.getCodomain())
			throw new IllegalArgumentException();

		this.table = table;
	}

	public Term isValid() {
		Table x = Table.create(getDomain());

		Term x0 = x.get();
		Term fx = table.of(x0);
		return Term.forall(x, fx.leq(x0).and(table.of(fx).equ(fx)));
	}

	public Domain getDomain() {
		return table.getCodomain();
	}

	public Table getTable() {
		return table;
	}

	public Term isIdentity() {
		Table x = Table.create(getDomain());

		Term x0 = x.of();
		return Term.forall(x, table.of(x0).equ(x0));
	}

	public Term isUniversal() {
		Table x = Table.create(getDomain());

		Term z = new Constant(getDomain(), 0);
		return Term.forall(x, table.of(x.get()).equ(z));
	}

	public Term of(Term a, Term b) {
		if (a == null || b == null)
			throw new IllegalArgumentException();

		if (a.getDomain() != table.getCodomain()
				|| b.getDomain() != table.getCodomain())
			throw new IllegalArgumentException();

		return table.of(a).equ(table.of(b));
	}
}
