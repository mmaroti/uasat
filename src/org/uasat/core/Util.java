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

	public static String formatElement(int size, int elem) {
		if (elem < 0 || elem >= size)
			throw new IllegalArgumentException();
		else if (size > 'z' - 'a' + 10)
			return Integer.toString(elem);
		else if (elem <= 9)
			return Character.toString((char) ('0' + elem));
		else
			return Character.toString((char) ('a' + elem - 10));
	}

	public static int parseElement(int size, String elem) {
		int e;
		if (size > 'z' - 'a' + 10)
			e = Integer.parseInt(elem);
		else if (elem.length() != 1)
			throw new IllegalArgumentException();
		else {
			char c = elem.charAt(0);
			if ('0' <= c && c <= '9')
				e = c - '0';
			else if ('a' <= c && c <= 'z')
				e = c - 'a' + 10;
			else
				throw new IllegalArgumentException();
		}

		if (e < 0 || e >= size)
			throw new IllegalArgumentException();

		return e;
	}

	public static String formatTuple(int size, int[] tuple) {
		StringBuilder s = new StringBuilder();

		if (size > 'z' - 'a' + 10) {
			for (int i = 0; i < tuple.length; i++) {
				if (i != 0)
					s.append(',');
				s.append(tuple[i]);
			}
		} else {
			for (int i = 0; i < tuple.length; i++) {
				int t = tuple[i];
				if (t < 0 || t >= size)
					throw new IllegalArgumentException();
				else if (t <= 9)
					s.append((char) ('0' + t));
				else
					s.append((char) ('a' + t - 10));
			}
		}

		return s.toString();
	}

	public static int[] parseTuple(int size, String str) {
		int[] tuple;

		if (size > 'z' - 'a' + 10) {
			String[] s = str.split(",");
			tuple = new int[s.length];

			for (int i = 0; i < s.length; i++) {
				int t = Integer.parseInt(s[i]);

				if (t < 0 || t >= size)
					throw new IllegalArgumentException();
				tuple[i] = t;
			}
		} else {
			tuple = new int[str.length()];
			for (int i = 0; i < tuple.length; i++) {
				char c = str.charAt(i);

				int t;
				if ('0' <= c && c <= '9')
					t = c - '0';
				else if ('a' <= c && c <= 'a')
					t = c - 'a' + 10;
				else
					throw new IllegalArgumentException();

				if (t < 0 || t >= size)
					throw new IllegalArgumentException();
				tuple[i] = t;
			}
		}

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
		private int state;

		public CubeIterator(int[] shape) {
			assert shape.length >= 1;

			this.shape = shape;
			index = new int[shape.length];
			state = 1;
		}

		@Override
		public boolean hasNext() {
			if (state != 0)
				return state > 0;

			for (int i = 0; i < index.length; i++) {
				if (++index[i] >= shape[i])
					index[i] = 0;
				else {
					state = 1;
					return true;
				}
			}

			state = -1;
			return false;
		}

		@Override
		public int[] next() {
			if (state == 0)
				hasNext();

			state = 0;
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
		private int state;

		public CubeIterator2(int size, int arity) {
			assert size >= 1 && arity >= 1;

			this.size = size;
			index = new int[arity];
			state = 1;
		}

		@Override
		public boolean hasNext() {
			if (state != 0)
				return state > 0;

			for (int i = 0; i < index.length; i++) {
				if (++index[i] >= size)
					index[i] = 0;
				else {
					state = 1;
					return true;
				}
			}

			state = -1;
			return false;
		}

		@Override
		public int[] next() {
			if (state == 0)
				hasNext();

			state = 0;
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
		private final int[] index;
		private int coord;
		private int state;

		public HullIterator(int radius, int arity) {
			assert 0 <= radius && 0 < arity;

			this.radius = radius;
			index = new int[arity];

			index[0] = radius;
			coord = radius == 0 ? arity - 1 : 0;
			state = 1;
		}

		@Override
		public boolean hasNext() {
			if (state != 0)
				return state > 0;

			for (int i = 0; i < coord; i++) {
				int a = index[i] + 1;
				if (a >= radius)
					index[i] = 0;
				else {
					index[i] = a;
					state = 1;
					return true;
				}
			}

			for (int i = coord + 1; i < index.length; i++) {
				int a = index[i] + 1;
				if (a > radius)
					index[i] = 0;
				else {
					index[i] = a;
					state = 1;
					return true;
				}
			}

			index[coord] = 0;
			if (++coord < index.length) {
				index[coord] = radius;
				state = 1;
				return true;
			}

			state = -1;
			return false;
		}

		@Override
		public int[] next() {
			if (state == 0)
				hasNext();

			state = 0;
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
			if (!iter.hasNext())
				throw new IllegalStateException();

			s.setLength(0);
			s.append('[');
			s.append(formatTuple(9, iter.next()));
			s.append(']');
			System.out.println(s.toString());
		}
		if (iter.hasNext())
			throw new IllegalStateException();

		System.out.println("--");
	}

	public static void main(String[] args) {
		printTuples(cubeIterator(new int[] {}));
		printTuples(cubeIterator(new int[] { 1 }));
		printTuples(cubeIterator(new int[] { 0 }));
		printTuples(cubeIterator(new int[] { 2, 3 }));
		printTuples(cubeIterator(2, 0));
		printTuples(cubeIterator(2, 1));
		printTuples(cubeIterator(2, 2));
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
