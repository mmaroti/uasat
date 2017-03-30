/**
 * Copyright (C) Miklos Maroti, 2017
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

package org.uasat2.core;

public abstract class SmallDomain {
	public abstract int getSize();

	public static class Primite extends SmallDomain {
		public final int size;

		public Primite(int size) {
			assert size >= 0;
			this.size = size;
		}

		@Override
		public int getSize() {
			return size;
		}
	}

	public static class Product extends SmallDomain {
		public final SmallDomain[] factors;

		public Product(SmallDomain... factors) {
			this.factors = factors;
		}

		@Override
		public int getSize() {
			int s = 1;
			for (SmallDomain factor : factors)
				s *= factor.getSize();
			return s;
		}
	}

	public static class Union extends SmallDomain {
		public final SmallDomain[] factors;

		public Union(SmallDomain... factors) {
			this.factors = factors;
		}

		@Override
		public int getSize() {
			int s = 0;
			for (SmallDomain factor : factors)
				s += factor.getSize();
			return s;
		}
	}
}
