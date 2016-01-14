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
	private final Table fun;

	protected Equivalence(String name, Domain domain) {
		fun = Table.create(name, domain, domain);
	}

	public Domain getDomain() {
		return fun.getCodomain();
	}

	public Term isValid() {
		Table t = Table.create("x", getDomain());
		Term x = t.of();
		Term fx = fun.of(x);
		return new ForAll(t, fx.leq(x).and(fun.of(fx).equ(fx)));
	}

	public Term isIdentity() {
		Table x = Table.create("x", getDomain());
		Term v = x.of();
		return new ForAll(x, fun.of(v).equ(v));
	}

	public Term areEquivalent(Term a, Term b) {
		if (a == null || b == null || a.getDomain() != fun.getCodomain()
				|| b.getDomain() != fun.getCodomain())
			throw new IllegalArgumentException();

		return fun.of(a).equ(fun.of(b));
	}
}
