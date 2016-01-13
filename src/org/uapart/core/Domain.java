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

public class Domain {
	private final String name;
	private final int size;

	public Domain(String name, int size) {
		if (name == null || size < 1)
			throw new IllegalArgumentException();

		this.name = name;
		this.size = size;
	}

	public String getName() {
		return name;
	}

	public int getSize() {
		return size;
	}

	public static final Domain BOOL = new Domain("BOOL", 2);
	public static final Domain INT = new Domain("INT", Integer.MAX_VALUE);
	
	public static final Domain ONE = new Domain("INT1", 1);
	public static final Domain TWO = new Domain("INT2", 2);
	public static final Domain THREE = new Domain("INT3", 3);
	public static final Domain FOUR = new Domain("INT4", 4);
}
