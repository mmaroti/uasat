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

package org.uasat.core;

import java.util.*;

public final class Util {
	public static int[] createShape(int size, int arity) {
		assert size >= 1 && arity >= 0;

		int[] shape = new int[arity];
		for (int i = 0; i < arity; i++)
			shape[i] = size;
		return shape;
	}

	public static char formatIndex(int elem) {
		if (0 <= elem && elem < 10)
			return (char) ('0' + elem);
		else if (10 <= elem && elem < 36)
			return (char) ('a' + elem - 10);
		else
			throw new IllegalArgumentException();
	}

	public static int parseIndex(int size, char c) {
		int i;
		if ('0' <= c && c <= '9')
			i = c - '0';
		else if ('a' <= c && c <= 'z')
			i = c - 'a' + 10;
		else
			i = size;

		if (i < size)
			return i;
		else
			throw new IllegalArgumentException("invalid coordinate: " + c);
	}

	public static void formatTuple(int[] tuple, StringBuilder s) {
		for (int i = 0; i < tuple.length; i++)
			s.append(formatIndex(tuple[i]));
	}

	public static int[] parseTuple(int size, String str) {
		int[] tuple = new int[str.length()];

		for (int i = 0; i < str.length(); i++)
			tuple[i] = parseIndex(size, str.charAt(i));

		return tuple;
	}

	private static class NullIterator implements Iterator<int[]> {
		private static final int[] EMPTY = new int[0];
		private boolean hasNext;

		public NullIterator(boolean hasNext) {
			this.hasNext = hasNext;
		}

		@Override
		public boolean hasNext() {
			return hasNext;
		}

		@Override
		public int[] next() {
			hasNext = false;
			return EMPTY;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	private static final Iterator<int[]> NULL_ITERATOR = new NullIterator(false);

	private static class CubeIterator implements Iterator<int[]> {
		private final int[] shape;
		private final int[] index;

		public CubeIterator(int[] shape) {
			assert shape.length >= 1;

			this.shape = shape;
			index = new int[shape.length];
			index[0] = -1;
		}

		@Override
		public boolean hasNext() {
			for (int i = 0; i < index.length; i++) {
				if (++index[i] >= shape[i])
					index[i] = 0;
				else
					return true;
			}
			return false;
		}

		@Override
		public int[] next() {
			return index;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	public static Iterator<int[]> cubeIterator(int[] shape) {
		assert shape != null;

		if (shape.length == 0)
			return new NullIterator(true);

		for (int i = 0; i < shape.length; i++) {
			assert shape[i] >= 0;
			if (shape[i] <= 0)
				return NULL_ITERATOR;
		}

		return new CubeIterator(shape);
	}

	private static class CubeIterator2 implements Iterator<int[]> {
		private final int size;
		private final int[] index;

		public CubeIterator2(int size, int arity) {
			assert size >= 1 && arity >= 1;

			this.size = size;
			index = new int[arity];
			index[0] = -1;
		}

		@Override
		public boolean hasNext() {
			for (int i = 0; i < index.length; i++) {
				if (++index[i] >= size)
					index[i] = 0;
				else
					return true;
			}
			return false;
		}

		@Override
		public int[] next() {
			return index;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	public static Iterator<int[]> cubeIterator(int size, int arity) {
		assert size >= 0 && arity >= 0;

		if (arity == 0)
			return new NullIterator(true);
		else if (size == 0)
			return NULL_ITERATOR;
		else
			return new CubeIterator2(size, arity);
	}

	private static class HullIterator implements Iterator<int[]> {
		private final int radius;
		private int coord;
		private final int[] index;

		public HullIterator(int radius, int arity) {
			assert 0 <= radius && 0 < arity;

			this.radius = radius;
			index = new int[arity];

			coord = 0;
			index[0] = radius;
			if (arity >= 2 && radius > 0)
				index[1] = -1;
		}

		@Override
		public boolean hasNext() {
			for (int i = 0; i < coord; i++) {
				int a = index[i] + 1;
				if (a >= radius)
					index[i] = 0;
				else {
					index[i] = a;
					return true;
				}
			}

			for (int i = coord + 1; i < index.length; i++) {
				int a = index[i] + 1;
				if (a > radius)
					index[i] = 0;
				else {
					index[i] = a;
					return true;
				}
			}

			if (radius == 0 || index.length == 1)
				return coord++ == 0;

			index[coord] = 0;
			if (++coord < index.length) {
				index[coord] = radius;
				return true;
			}

			return false;
		}

		@Override
		public int[] next() {
			return index;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	public static Iterator<int[]> hullIterator(int radius, int arity) {
		assert 0 <= radius && 0 < arity;
		return new HullIterator(radius, arity);
	}

	private static void printTuples(Iterator<int[]> iter) {
		StringBuilder s = new StringBuilder();
		while (iter.hasNext()) {
			s.setLength(0);
			s.append('[');
			formatTuple(iter.next(), s);
			s.append(']');
			System.out.println(s.toString());
		}
		System.out.println("--");
	}

	public static void main(String[] args) {
		printTuples(cubeIterator(new int[] {}));
		printTuples(cubeIterator(new int[] { 1 }));
		printTuples(cubeIterator(new int[] { 0 }));
		printTuples(cubeIterator(new int[] { 2, 3 }));
		printTuples(cubeIterator(2, 3));

		printTuples(hullIterator(0, 1));
		printTuples(hullIterator(1, 1));
		printTuples(hullIterator(2, 1));
		printTuples(hullIterator(0, 2));
		printTuples(hullIterator(1, 2));
		printTuples(hullIterator(2, 2));
		printTuples(hullIterator(0, 3));
		printTuples(hullIterator(1, 3));
		printTuples(hullIterator(2, 3));
	}
}
