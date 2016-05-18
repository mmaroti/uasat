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
		else if (type == Boolean.TYPE)
			return (Vector<ELEM>) new BoolVector(size);
		else
			return new ObjVector<ELEM>(size);
	}

	public abstract int size();

	public abstract ELEM get(int index);

	public abstract void set(int index, ELEM elem);

	public abstract void fill(ELEM elem);

	public abstract void copy(int srcPos, Vector<ELEM> dst, int dstPos,
			int length);

	private class Iter implements Iterator<ELEM> {
		private int pos;
		private final int end;

		Iter(int start, int length) {
			assert 0 <= start && 0 <= length && start + length <= size();
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
		private final int[] array;

		public IntVector(int size) {
			this.array = new int[size];
		}

		@Override
		public int size() {
			return array.length;
		}

		@Override
		public Integer get(int index) {
			return array[index];
		}

		@Override
		public void set(int index, Integer elem) {
			array[index] = elem;
		}

		@Override
		public void fill(Integer elem) {
			Arrays.fill(array, elem);
		}

		@Override
		public void copy(int srcPos, Vector<Integer> dst, int dstPos, int length) {
			System.arraycopy(array, srcPos, ((IntVector) dst).array, dstPos,
					length);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof IntVector) {
				IntVector vec = (IntVector) obj;
				return Arrays.equals(array, vec.array);
			} else
				return false;
		}
	}

	protected static class BoolVector extends Vector<Boolean> {
		private final boolean[] array;

		public BoolVector(int size) {
			this.array = new boolean[size];
		}

		@Override
		public int size() {
			return array.length;
		}

		@Override
		public Boolean get(int index) {
			return array[index];
		}

		@Override
		public void set(int index, Boolean elem) {
			array[index] = elem;
		}

		@Override
		public void fill(Boolean elem) {
			Arrays.fill(array, elem);
		}

		@Override
		public void copy(int srcPos, Vector<Boolean> dst, int dstPos, int length) {
			System.arraycopy(array, srcPos, ((BoolVector) dst).array, dstPos,
					length);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof BoolVector) {
				BoolVector vec = (BoolVector) obj;
				return Arrays.equals(array, vec.array);
			} else
				return false;
		}
	}

	protected static class BitVector extends Vector<Boolean> {
		private final int size;
		private final long[] array;

		public BitVector(int size) {
			assert 0 <= size && size < Integer.MAX_VALUE - 63;
			this.size = size;
			this.array = new long[(size + 63) >> 6];
		}

		@Override
		public int size() {
			return size;
		}

		@Override
		public Boolean get(int index) {
			assert 0 <= index && index < size;
			return (array[index >> 6] & (1L << (index & 63))) != 0;
		}

		@Override
		public void set(int index, Boolean elem) {
			assert 0 <= index && index < size;
			array[index >> 6] |= 1L << (index & 63);
		}

		@Override
		public void fill(Boolean elem) {
			if (elem) {
				Arrays.fill(array, -1L);
				if ((size & 63) != 0)
					array[(size - 1) >> 6] = (1L << (size & 63)) - 1;
			} else
				Arrays.fill(array, 0);
		}

		@Override
		public void copy(int srcPos, Vector<Boolean> dst, int dstPos, int length) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof BitVector) {
				BitVector vec = (BitVector) obj;
				return size == vec.size && Arrays.equals(array, vec.array);
			} else
				return false;
		}
	}

	protected static class ObjVector<ELEM> extends Vector<ELEM> {
		private final ELEM[] array;

		@SuppressWarnings("unchecked")
		public ObjVector(int size) {
			this.array = (ELEM[]) new Object[size];
		}

		@Override
		public int size() {
			return array.length;
		}

		@Override
		public ELEM get(int index) {
			return array[index];
		}

		@Override
		public void set(int index, ELEM elem) {
			array[index] = elem;
		}

		@Override
		public void copy(int srcPos, Vector<ELEM> dst, int dstPos, int length) {
			System.arraycopy(array, srcPos, ((ObjVector<ELEM>) dst).array,
					dstPos, length);
		}

		@Override
		public void fill(ELEM elem) {
			Arrays.fill(array, elem);
		}

		@Override
		@SuppressWarnings("unchecked")
		public boolean equals(Object obj) {
			if (obj instanceof ObjVector) {
				ObjVector<ELEM> vec = (ObjVector<ELEM>) obj;
				return Arrays.equals(array, vec.array);
			} else
				return false;
		}
	}
}
