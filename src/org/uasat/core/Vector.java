/**
 *	Copyright (C) Miklos Maroti, 2015-2016
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

package org.uasat.core;

import java.util.*;

public abstract class Vector<ELEM> implements Iterable<ELEM> {
	@SuppressWarnings("unchecked")
	public static <ELEM> Vector<ELEM> create(Class<ELEM> type, int size) {
		if (type == Integer.TYPE)
			return (Vector<ELEM>) new IntVector(size);
		// else if (type == Boolean.TYPE)
		//	return (Vector<ELEM>) new BitVector(size);
		else
			return new ObjVector<ELEM>(size);
	}

	public abstract int size();

	public abstract ELEM get(int index);

	public abstract void set(int index, ELEM elem);

	public abstract void fill(ELEM elem);

	private class Iter implements Iterator<ELEM> {
		private int pos;
		private final int end;

		Iter(int start, int length) {
			pos = start;
			end = start + length;
		}

		@Override
		public boolean hasNext() {
			return pos < end;
		}

		@Override
		public ELEM next() {
			return get(pos++);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Iterator<ELEM> iterator() {
		return new Iter(0, size());
	}

	public Iterable<ELEM> slice(final int start, final int length) {
		return new Iterable<ELEM>() {
			@Override
			public Iterator<ELEM> iterator() {
				return new Iter(start, length);
			}
		};
	}

	protected static class IntVector extends Vector<Integer> {
		private final int[] ints;

		public IntVector(int size) {
			assert 0 <= size;
			this.ints = new int[size];
		}

		@Override
		public int size() {
			return ints.length;
		}

		@Override
		public Integer get(int index) {
			return ints[index];
		}

		@Override
		public void set(int index, Integer elem) {
			ints[index] = elem;
		}

		@Override
		public void fill(Integer elem) {
			Arrays.fill(ints, elem);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof IntVector) {
				IntVector vec = (IntVector) obj;
				return Arrays.equals(ints, vec.ints);
			} else
				return false;
		}
	}

	protected static class BitVector extends Vector<Boolean> {
		private final int size;
		private final long[] bits;

		public BitVector(int size) {
			assert 0 <= size && size < Integer.MAX_VALUE - 63;
			this.size = size;
			this.bits = new long[(size + 63) >> 6];
		}

		@Override
		public int size() {
			return size;
		}

		@Override
		public Boolean get(int index) {
			assert 0 <= index && index < size;
			return (bits[index >> 6] & (1L << (index & 63))) != 0;
		}

		@Override
		public void set(int index, Boolean elem) {
			assert 0 <= index && index < size;
			bits[index >> 6] |= 1L << (index & 63);
		}

		@Override
		public void fill(Boolean elem) {
			if (elem) {
				Arrays.fill(bits, -1L);
				if ((size & 63) != 0)
					bits[(size - 1) >> 6] = (1L << (size & 63)) - 1;
			} else
				Arrays.fill(bits, 0);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof BitVector) {
				BitVector vec = (BitVector) obj;
				return size == vec.size && Arrays.equals(bits, vec.bits);
			} else
				return false;
		}
	}

	protected static class ObjVector<ELEM> extends Vector<ELEM> {
		private final ELEM[] objs;

		@SuppressWarnings("unchecked")
		public ObjVector(int size) {
			assert 0 <= size;
			this.objs = (ELEM[]) new Object[size];
		}

		@Override
		public int size() {
			return objs.length;
		}

		@Override
		public ELEM get(int index) {
			return objs[index];
		}

		@Override
		public void set(int index, ELEM elem) {
			objs[index] = elem;
		}

		@Override
		public void fill(ELEM elem) {
			Arrays.fill(objs, elem);
		}

		@Override
		@SuppressWarnings("unchecked")
		public boolean equals(Object obj) {
			if (obj instanceof ObjVector) {
				ObjVector<ELEM> vec = (ObjVector<ELEM>) obj;
				return Arrays.equals(objs, vec.objs);
			} else
				return false;
		}
	}
}
