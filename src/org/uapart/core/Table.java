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

import java.util.*;

public abstract class Table {
	protected final int[] table;
	private final Domain codomain;
	private final String name;

	public Table(String name, Domain codomain, int length) {
		if (name == null || codomain == null || length < 1)
			throw new IllegalArgumentException();

		this.table = new int[length];
		Arrays.fill(table, Integer.MIN_VALUE);

		this.codomain = codomain;
		this.name = name;
	}

	protected static int getTableLength(Domain... domains) {
		if (domains == null)
			throw new IllegalArgumentException();

		long s = 1;
		for (int i = 0; i < domains.length; i++) {
			s *= domains[i].getSize();
			if (s >= Integer.MAX_VALUE)
				throw new IllegalArgumentException();
		}

		return (int) s;
	}

	public String getName() {
		return name;
	}

	public Domain getCodomain() {
		return codomain;
	}

	public int getSize() {
		return codomain.getSize();
	}

	public int[] getTable() {
		return table;
	}

	public int getLength() {
		return table.length;
	}

	@Override
	public String toString() {
		String s = name + ":";

		for (int i = 0; i < table.length; i++)
			s += " " + table[i];

		return s;
	}
}
