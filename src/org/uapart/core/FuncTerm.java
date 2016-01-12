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

public class FuncTerm extends Term {
	private final FuncTable table;
	private final Term[] subterms;
	private final int[] args;

	public FuncTerm(FuncTable table, Term[] subterms) {
		if (table == null || subterms == null
				|| table.getArity() != subterms.length)
			throw new IllegalArgumentException();

		for (int i = 0; i < subterms.length; i++)
			if (subterms[i] == null
					|| table.getDomain(i) != subterms[i].getDomain())
				throw new IllegalArgumentException();

		this.table = table;
		this.subterms = subterms;
		this.args = new int[subterms.length];
	}

	@Override
	public Domain getDomain() {
		return table.getCodomain();
	}

	@Override
	public int evaluate() {
		int m = Integer.MAX_VALUE;

		for (int i = 0; i < args.length; i++) {
			int a = subterms[i].evaluate();
			args[i] = a;

			if (a < m)
				m = a;
		}

		return m < 0 ? m : table.evaluate(args);
	}

	@Override
	public int getBound() {
		int b = -1;

		for (int i = 0; i < subterms.length; i++) {
			int a = subterms[i].getBound();
			if (a < b)
				b = a;
		}

		return b;
	}
}
