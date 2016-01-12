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

public class BinaryTable extends Table {
	private final Domain domain0;
	private final Domain domain1;
	private final int size0;

	public BinaryTable(String name, Domain domain0, Domain domain1,
			Domain codomain) {
		super(name, codomain, getTableLength(domain0, domain1));

		this.domain0 = domain0;
		this.domain1 = domain1;
		this.size0 = domain0.getSize();
	}

	public Domain getDomain0() {
		return domain0;
	}

	public Domain getDomain1() {
		return domain1;
	}

	@Override
	public int[] getTable() {
		return table;
	}

	public int evaluate(int arg0, int arg1) {
		return table[arg0 + arg1 * size0];
	}
}
