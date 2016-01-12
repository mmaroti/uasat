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

public class FuncTable extends Table {
	private final Domain[] domains;
	private final int[] steps;

	public FuncTable(String name, Domain[] domains, Domain codomain) {
		super(name, codomain, getTableLength(domains));

		this.domains = domains;
		steps = new int[domains.length];

		int s = 1;
		for (int i = 0; i < domains.length; i++) {
			steps[i] = s;
			s *= domains[i].getSize();
		}
	}

	public int getArity() {
		return domains.length;
	}

	public Domain getDomain(int index) {
		return domains[index];
	}

	public int evaluate(int[] args) {
		assert args.length == steps.length;

		int p = 0;
		for (int i = 0; i < args.length; i++)
			p += args[i] * steps[i];

		return table[p];
	}
}
