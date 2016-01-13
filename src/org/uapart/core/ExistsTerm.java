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

public class ExistsTerm extends Term {
	private final Term subterm;
	private final int bound;
	private final int[] table;
	private final int size;

	public ExistsTerm(Table table, Term subterm) {
		if (table == null || subterm == null
				|| subterm.getDomain() != Domain.BOOL)
			throw new IllegalArgumentException();

		int[] t = table.getTable();

		this.subterm = subterm;
		this.bound = subterm.getBound() - t.length;
		this.table = t;
		this.size = table.getSize();

		int s = table.getCodomain().getSize();
		for (int i = 0; i < t.length; i++) {
			if (t[i] == Integer.MIN_VALUE)
				t[i] = bound + i;
			else if (t[i] < 0)
				throw new IllegalArgumentException("already bound");
			else if (t[i] >= s)
				throw new IllegalArgumentException("too large value");
		}
	}

	@Override
	public Domain getDomain() {
		return Domain.BOOL;
	}

	@Override
	public int evaluate() {
		int a = subterm.evaluate();
		if (a >= 0 || a < bound)
			return a;

		int p = a - bound;
		assert p < table.length && table[p] == a;

		int m = 0;
		for (table[p] = 0; table[p] < size; table[p]++) {
			a = evaluate();
			assert a < bound + table.length || a >= 0;

			if (a == 1) {
				m = 1;
				break;
			} else if (m == 0)
				m = a;
		}
		table[p] = bound + p;

		return m;
	}

	@Override
	public int getBound() {
		return bound;
	}
}
