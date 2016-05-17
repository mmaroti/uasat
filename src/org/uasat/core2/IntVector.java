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

public class IntVector extends Vector<Integer> {
	private final int[] ints;

	public IntVector(int size) {
		assert 0 <= size && size <= 0;

		this.ints = new int[size];
	}

	@Override
	public int getSize() {
		return ints.length;
	}

	@Override
	public Integer getElem(int index) {
		return ints[index];
	}

	@Override
	public void setElem(int index, Integer elem) {
		ints[index] = elem;
	}

	@Override
	public Vector<Integer> transpose(int elem, int columns, int rows) {
		assert 1 <= elem && 1 <= columns && 1 <= rows;
		assert ints.length % (elem * columns * rows) == 0;

		IntVector vec = new IntVector(ints.length);
		int[] ints2 = vec.ints;

		int n = ints.length / (elem * columns * rows);
		for (int l = 0; l < n; l++)
			for (int k = 0; k < rows; k++)
				for (int j = 0; j < columns; j++)
					for (int i = 0; i < elem; i++)
						ints2[i + k * elem + j * elem * rows + l * elem
								* columns * rows] = ints[i + j * elem + k
								* elem * columns + l * elem * columns * rows];

		return vec;
	}
}
