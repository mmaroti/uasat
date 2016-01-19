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

public class BoolLeq extends Term {
	private final Term subterm0;
	private final Term subterm1;
	private final int maxval1;

	public BoolLeq(Term subterm0, Term subterm1) {
		if (subterm0 == null || subterm1 == null
				|| subterm0.getDomain() != subterm1.getDomain())
			throw new IllegalArgumentException();

		this.subterm0 = subterm0;
		this.subterm1 = subterm1;
		this.maxval1 = subterm1.getDomain().getSize() - 1;
	}

	@Override
	public Domain getDomain() {
		return Domain.BOOL;
	}

	@Override
	public int $evaluate() {
		int a = subterm0.$evaluate();
		if (a == 0)
			return 1;

		int b = subterm1.$evaluate();
		if (b == maxval1)
			return 1;

		if (a < 0)
			return a;
		else if (b < 0)
			return b;
		else
			return a <= b ? 1 : 0;
	}

	@Override
	public int getBound() {
		int a = subterm0.getBound();
		int b = subterm1.getBound();
		return a <= b ? a : b;
	}
}
