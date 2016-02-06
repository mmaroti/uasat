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

package org.uapart.core;

public class OrdLex extends Term {
	private final Variable[] vars;
	private final Term term0;
	private final Term term1;

	public OrdLex(Function fun0, Function fun1) {
		if (fun0 == null || fun1 == null
				|| fun0.getCodomain() != fun1.getCodomain()
				|| fun0.getArity() != fun1.getArity())
			throw new IllegalArgumentException();

		vars = new Variable[fun0.getArity()];
		for (int i = 0; i < vars.length; i++) {
			Domain d = fun0.getDomain(i);
			if (d.getSize() <= 0 || d != fun1.getDomain(i))
				throw new IllegalArgumentException();
			vars[i] = new Variable(d);
		}

		this.term0 = fun0.of(vars);
		this.term1 = fun1.of(vars);
	}

	@Override
	public Domain getDomain() {
		return Domain.ORD;
	}

	@SuppressWarnings("unused")
	private void reset() {
		for (int i = 0; i < vars.length; i++)
			vars[i].$set(0);
	}

	@SuppressWarnings("unused")
	private boolean next() {
		for (int i = 0; i < vars.length; i++) {
			int a = vars[i].$evaluate() + 1;
			if (a >= vars[i].getSize())
				vars[i].$set(0);
			else {
				vars[i].$set(a);
				return true;
			}
		}
		return false;
	}

	int limit;
	int coord; // the first coordinate with limit value

	private void reset2() {
		for (int i = 0; i < vars.length; i++)
			vars[i].$set(0);

		limit = 0;
		coord = 0;
	}

	private boolean next2() {
		assert vars[coord].$evaluate() == limit;

		for (int i = 0; i < coord; i++) {
			int a = vars[i].$evaluate() + 1;
			assert a <= limit;

			if (a >= limit || a >= vars[i].getSize())
				vars[i].$set(0);
			else {
				vars[i].$set(a);
				return true;
			}
		}

		for (int i = coord + 1; i < vars.length; i++) {
			int a = vars[i].$evaluate() + 1;
			assert a <= limit + 1;

			if (a > limit || a >= vars[i].getSize())
				vars[i].$set(0);
			else {
				vars[i].$set(a);
				return true;
			}
		}

		vars[coord].$set(0);
		while (--coord >= 0) {
			if (limit < vars[coord].getSize()) {
				vars[coord].$set(limit);
				return true;
			}
		}

		limit += 1;
		coord = vars.length;
		while (--coord >= 0) {
			if (limit < vars[coord].getSize()) {
				vars[coord].$set(limit);
				return true;
			}
		}

		return false;
	}

	@Override
	public int $evaluate() {
		reset2();
		do {
			int a = term0.$evaluate();
			if (a < 0)
				return a;

			int b = term1.$evaluate();
			if (b < 0)
				return b;

			if (a != b)
				return a < b ? 0 : 2;
		} while (next2());
		return 1;
	}

	@Override
	public int getBound() {
		int a = term0.getBound();
		int b = term1.getBound();
		return a < b ? a : b;
	}
}
