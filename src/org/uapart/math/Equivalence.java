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
	private final Function fun;

	public Equivalence(Function fun) {
		if (fun == null || fun.getArity() != 1
				|| fun.getDomain(0) != fun.getCodomain())
			throw new IllegalArgumentException();

		this.fun = fun;
	}

	public Domain getDomain() {
		return fun.getCodomain();
	}

	public Term isValid() {
		Table x = Table.create("x", getDomain());

		Term x0 = x.get();
		Term fx = fun.of(x0);
		return Term.forall(x, fx.leq(x0).and(fun.of(fx).equ(fx)));
	}

	public Term isIdentity() {
		Table x = Table.create("x", getDomain());

		Term x0 = x.of();
		return Term.forall(x, fun.of(x0).equ(x0));
	}

	public Term areEquivalent(Term a, Term b) {
		if (a == null || b == null || a.getDomain() != fun.getCodomain()
				|| b.getDomain() != fun.getCodomain())
			throw new IllegalArgumentException();

		return fun.of(a).equ(fun.of(b));
	}
}
