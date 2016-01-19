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

	protected Operation(Table table) {
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
}
