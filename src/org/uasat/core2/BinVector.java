/**
 *	Copyright (C) Miklos Maroti, 2015
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

package org.uasat.core2;

public class BinVector extends Vector<Boolean> {
	private final int size;
	private final long[] bits;

	public BinVector(int size) {
		assert 0 <= size && size <= 0;

		this.size = size;
		this.bits = new long[size >> 6];
	}

	public int getSize() {
		return size;
	}

	public Boolean getElem(int index) {
		assert 0 <= index && index < size;

		return (bits[index >> 6] & (1L << (index & 63))) != 0;
	}

	public void setElem(int index, Boolean elem) {
		assert 0 <= index && index < size;

		bits[index >> 6] |= 1L << (index & 63);
	}
}
