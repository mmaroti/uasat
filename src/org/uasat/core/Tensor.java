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

import java.io.*;
import java.util.*;

public class Tensor<ELEM> implements Iterable<ELEM> {
	private final int[] shape;
	private final ELEM[] elems;

	public int[] getShape() {
		return shape;
	}

	public int getOrder() {
		return shape.length;
	}

	public int getDim(int index) {
		return shape[index];
	}

	public int getLastDim() {
		return shape[shape.length - 1];
	}

	private static class Iter<ELEM> implements Iterator<ELEM> {
		private final ELEM[] elems;
		private int index;

		Iter(ELEM[] array) {
			this.elems = array;
			index = 0;
		}

		@Override
		public boolean hasNext() {
			return index < elems.length;
		}

		@Override
		public ELEM next() {
			return elems[index++];
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	};

	@Override
	public Iterator<ELEM> iterator() {
		return new Iter<ELEM>(elems);
	}

	public ELEM getElem(int... index) {
		assert index.length == shape.length;

		int pos = 0;
		int size = 1;
		for (int i = 0; i < shape.length; i++) {
			pos += size * index[i];
			size *= shape[i];
		}

		return elems[pos];
	}

	public void setElem(ELEM elem, int... index) {
		assert index.length == shape.length;

		int pos = 0;
		int size = 1;
		for (int i = 0; i < shape.length; i++) {
			pos += size * index[i];
			size *= shape[i];
		}

		elems[pos] = elem;
	}

	public ELEM[] getElems() {
		return elems;
	}

	public ELEM get() {
		assert elems.length == 1;
		return elems[0];
	}

	@SuppressWarnings("unchecked")
	private Tensor(final int[] shape) {
		this.shape = shape;
		this.elems = (ELEM[]) new Object[getSize(shape)];
	}

	private static int getSize(int[] shape) {
		int size = 1;
		for (int i = 0; i < shape.length; i++) {
			assert 0 <= shape[i];
			size *= shape[i];
		}
		return size;
	}

	public static <ELEM> Tensor<ELEM> generate(final int[] shape,
			final Func0<ELEM> func) {
		Tensor<ELEM> tensor = new Tensor<ELEM>(shape);

		for (int i = 0; i < tensor.elems.length; i++)
			tensor.elems[i] = func.call();

		return tensor;
	}

	public static <ELEM> Tensor<ELEM> generate(final int[] shape,
			final Func1<ELEM, int[]> func) {
		Tensor<ELEM> tensor = new Tensor<ELEM>(shape);

		if (tensor.elems.length > 0) {
			int[] index = new int[shape.length];
			int pos = 0;
			outer: for (;;) {
				tensor.elems[pos++] = func.call(index);
				for (int i = 0; i < index.length; i++) {
					if (++index[i] >= shape[i])
						index[i] = 0;
					else
						continue outer;
				}
				break;
			}
		}

		return tensor;
	}

	public static <ELEM> Tensor<ELEM> generate(int dim,
			final Func1<ELEM, Integer> func) {
		Tensor<ELEM> tensor = new Tensor<ELEM>(new int[] { dim });

		for (int i = 0; i < dim; i++)
			tensor.elems[i] = func.call(i);

		return tensor;
	}

	public static <ELEM> Tensor<ELEM> generate(int dim1, int dim2,
			final Func2<ELEM, Integer, Integer> func) {
		Tensor<ELEM> tensor = new Tensor<ELEM>(new int[] { dim1, dim2 });

		int pos = 0;
		for (int j = 0; j < dim2; j++)
			for (int i = 0; i < dim1; i++)
				tensor.elems[pos++] = func.call(i, j);

		return tensor;
	}

	public static <ELEM> Tensor<ELEM> constant(final int[] shape,
			final ELEM elem) {
		Tensor<ELEM> tensor = new Tensor<ELEM>(shape);
		Arrays.fill(tensor.elems, elem);

		return tensor;
	}

	public static <ELEM> Tensor<ELEM> scalar(final ELEM elem) {
		Tensor<ELEM> tensor = new Tensor<ELEM>(new int[0]);
		tensor.elems[0] = elem;

		return tensor;
	}

	public static <ELEM> Tensor<ELEM> vector(final List<ELEM> elems) {
		Tensor<ELEM> tensor = new Tensor<ELEM>(new int[] { elems.size() });

		int pos = 0;
		for (ELEM elem : elems)
			tensor.elems[pos++] = elem;

		return tensor;
	}

	public static <ELEM> Tensor<ELEM> matrix(int dim1, int dim2,
			final List<ELEM> elems) {
		Tensor<ELEM> tensor = new Tensor<ELEM>(new int[] { dim1, dim2 });
		assert tensor.elems.length == elems.size();

		int pos = 0;
		for (ELEM elem : elems)
			tensor.elems[pos++] = elem;

		return tensor;
	}

	public static <ELEM> Tensor<ELEM> diagonal(final Tensor<ELEM> tensor,
			final int[] map, final ELEM defval) {
		final int[] shape = new int[map.length];
		for (int i = 0; i < shape.length; i++)
			shape[i] = tensor.getDim(map[i]);

		final int[] index = new int[tensor.getOrder()];
		for (int i = 0; i < index.length; i++) {
			boolean b = false;
			for (int j = 0; j < map.length; j++)
				b |= map[j] == i;
			assert b;
		}

		return Tensor.generate(shape, new Func1<ELEM, int[]>() {
			@Override
			public ELEM call(int[] elem) {
				Arrays.fill(index, -1);
				for (int i = 0; i < elem.length; i++)
					if (index[map[i]] == -1)
						index[map[i]] = elem[i];
					else if (index[map[i]] != elem[i])
						return defval;

				return tensor.getElem(index);
			}
		});
	}

	public static <ELEM> Tensor<ELEM> reshape_old(final Tensor<ELEM> arg,
			final int[] shape, final int[] map) {
		assert arg.getOrder() == map.length;

		if (Arrays.equals(arg.shape, shape) && map.length == shape.length) {
			boolean b = true;
			for (int i = 0; i < map.length; i++)
				b &= map[i] == i;

			if (b)
				return arg;
		}

		final int[] index = new int[map.length];

		return Tensor.generate(shape, new Func1<ELEM, int[]>() {
			@Override
			public ELEM call(int[] elem) {
				for (int i = 0; i < index.length; i++)
					index[i] = elem[map[i]];
				return arg.getElem(index);
			}
		});
	}

	/**
	 * Reshapes the tensor to the given new shape, so that the old coordinates
	 * are mapped to the new one using the map vector.
	 */
	public static <ELEM> Tensor<ELEM> reshape(Tensor<ELEM> arg, int[] shape,
			int[] map) {
		assert arg.getOrder() == map.length;

		Tensor<ELEM> tensor = new Tensor<ELEM>(shape);

		int[] index = new int[shape.length];
		int[] stepa = new int[shape.length];
		int[] stepb = new int[shape.length];

		for (int s = 1, i = 0; i < map.length; i++) {
			assert shape[map[i]] == arg.shape[i];

			stepa[map[i]] += s;
			s *= arg.shape[i];
		}

		for (int i = 0; i < shape.length; i++)
			stepb[i] = stepa[i] * (shape[i] - 1);

		ELEM[] src = arg.elems;
		ELEM[] dst = tensor.elems;

		if (dst.length > 0) {
			int pos = 0;
			int idx = 0;
			outer: for (;;) {
				dst[idx++] = src[pos];

				for (int i = 0; i < index.length; i++) {
					if (++index[i] >= shape[i]) {
						index[i] = 0;
						pos -= stepb[i];
					} else {
						pos += stepa[i];
						continue outer;
					}
				}
				break;
			}
			assert idx == dst.length;
		}

		return tensor;
	}

	public static <ELEM, ELEM1> Tensor<ELEM> map(Func1<ELEM, ELEM1> func,
			Tensor<ELEM1> arg) {
		Tensor<ELEM> tensor = new Tensor<ELEM>(arg.shape);

		for (int i = 0; i < tensor.elems.length; i++)
			tensor.elems[i] = func.call(arg.elems[i]);

		return tensor;
	}

	public static <ELEM, ELEM1, ELEM2> Tensor<ELEM> map2(
			Func2<ELEM, ELEM1, ELEM2> func, Tensor<ELEM1> arg1,
			Tensor<ELEM2> arg2) {
		assert Arrays.equals(arg1.shape, arg2.shape);
		Tensor<ELEM> tensor = new Tensor<ELEM>(arg1.shape);

		for (int i = 0; i < tensor.elems.length; i++)
			tensor.elems[i] = func.call(arg1.elems[i], arg2.elems[i]);

		return tensor;
	}

	public static <ELEM1, ELEM2> Tensor<ELEM2> fold(
			Func1<ELEM2, Iterable<ELEM1>> func, int proj, Tensor<ELEM1> arg) {

		int[] shape1 = new int[proj];
		System.arraycopy(arg.shape, 0, shape1, 0, proj);
		Tensor<ELEM1> tensor1 = new Tensor<ELEM1>(shape1);

		int[] shape2 = new int[arg.getOrder() - proj];
		System.arraycopy(arg.shape, proj, shape2, 0, shape2.length);
		Tensor<ELEM2> tensor2 = new Tensor<ELEM2>(shape2);

		int pos = 0;
		for (int i = 0; i < tensor2.elems.length; i++) {
			System.arraycopy(arg.elems, pos, tensor1.elems, 0,
					tensor1.elems.length);

			tensor2.elems[i] = func.call(tensor1);
			pos += tensor1.elems.length;
		}

		return tensor2;
	}

	public static <ELEM> Tensor<ELEM> stack(int[] commonShape,
			List<Tensor<ELEM>> list) {
		for (Tensor<ELEM> tensor : list)
			assert Arrays.equals(commonShape, tensor.shape);

		int count = list.size();
		int size = getSize(commonShape);

		int[] shape = new int[commonShape.length + 1];
		System.arraycopy(commonShape, 0, shape, 0, commonShape.length);
		shape[commonShape.length] = list.size();

		Tensor<ELEM> tensor = new Tensor<ELEM>(shape);

		int pos = 0;
		for (int i = 0; i < count; i++) {
			System.arraycopy(list.get(i).elems, 0, tensor.elems, pos, size);
			pos += size;
		}

		return tensor;
	}

	public static <ELEM> Tensor<ELEM> stack(List<Tensor<ELEM>> list) {
		assert list.size() >= 1;
		return stack(list.get(0).shape, list);
	}

	public static <ELEM> List<Tensor<ELEM>> unstack(Tensor<ELEM> tensor) {
		assert tensor.getOrder() >= 1;

		int[] shape = new int[tensor.getOrder() - 1];
		System.arraycopy(tensor.getShape(), 0, shape, 0, shape.length);
		int size = getSize(shape);
		int last = tensor.getDim(shape.length);

		List<Tensor<ELEM>> list = new ArrayList<Tensor<ELEM>>();
		for (int i = 0; i < last; i++) {
			Tensor<ELEM> t = new Tensor<ELEM>(shape);
			System.arraycopy(tensor.elems, i * size, t.elems, 0, size);
			list.add(t);
		}

		return list;
	}

	public static <ELEM> List<ELEM> getLexOrder(Tensor<ELEM> tensor) {
		List<ELEM> list = new ArrayList<ELEM>();

		int[] shape = tensor.getShape();
		int[] index = new int[shape.length];

		int limit = 0;
		int coord = 0;

		outer: for (;;) {
			assert index[coord] == limit;

			list.add(tensor.getElem(index));

			for (int i = 0; i < coord; i++) {
				int a = index[i] + 1;
				assert a <= limit;

				if (a >= limit || a >= shape[i])
					index[i] = 0;
				else {
					index[i] = a;
					continue outer;
				}
			}

			for (int i = coord + 1; i < index.length; i++) {
				int a = index[i] + 1;
				assert a <= limit + 1;

				if (a > limit || a >= shape[i])
					index[i] = 0;
				else {
					index[i] = a;
					continue outer;
				}
			}

			index[coord] = 0;
			while (--coord >= 0) {
				if (limit < shape[coord]) {
					index[coord] = limit;
					continue outer;
				}
			}

			limit += 1;
			coord = shape.length;
			while (--coord >= 0) {
				if (limit < shape[coord]) {
					index[coord] = limit;
					continue outer;
				}
			}

			break;
		}

		assert list.size() == tensor.elems.length;
		return list;
	}

	public static <ELEM> Tensor<ELEM> concat(Tensor<ELEM> arg1,
			Tensor<ELEM> arg2) {
		assert arg1.getOrder() == arg2.getOrder();

		int a = arg1.getOrder() - 1;
		for (int i = 0; i < a; i++)
			assert arg1.getDim(i) == arg2.getDim(i);

		int[] shape = new int[arg1.getOrder()];
		System.arraycopy(arg1.getShape(), 0, shape, 0, a);
		shape[a] = arg1.getDim(a) + arg2.getDim(a);

		Tensor<ELEM> tensor = new Tensor<ELEM>(shape);
		assert arg1.elems.length + arg2.elems.length == tensor.elems.length;
		System.arraycopy(arg1.elems, 0, tensor.elems, 0, arg1.elems.length);
		System.arraycopy(arg2.elems, 0, tensor.elems, arg1.elems.length,
				arg2.elems.length);

		return tensor;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		str.append("Tensor [");
		for (int i = 0; i < shape.length; i++) {
			if (i != 0)
				str.append(',');
			str.append(shape[i]);
		}
		str.append("] [");
		for (int i = 0; i < elems.length; i++) {
			if (i != 0)
				str.append(',');
			str.append(elems[i]);
		}
		str.append("]");

		return str.toString();
	}

	@Override
	public boolean equals(Object other) {
		Tensor<?> tensor = (Tensor<?>) other;

		return Arrays.equals(shape, tensor.shape)
				&& Arrays.equals(elems, tensor.elems);
	}

	public static <ELEM> Comparator<Tensor<ELEM>> comparator(
			final Comparator<ELEM> comp) {
		return new Comparator<Tensor<ELEM>>() {
			@Override
			public int compare(Tensor<ELEM> arg0, Tensor<ELEM> arg1) {
				Iterator<ELEM> iter0 = arg0.iterator();
				Iterator<ELEM> iter1 = arg1.iterator();

				while (iter0.hasNext()) {
					assert iter1.hasNext();

					int c = comp.compare(iter0.next(), iter1.next());
					if (c != 0)
						return c;
				}

				assert !iter1.hasNext();
				return 0;
			}
		};
	}

	public String info() {
		StringBuilder str = new StringBuilder();

		str.append("Tensor [");
		for (int i = 0; i < shape.length; i++) {
			if (i != 0)
				str.append(',');
			str.append(shape[i]);
		}
		str.append("]");

		return str.toString();
	}

	public static <ELEM> void print(Tensor<ELEM> tensor, PrintStream stream) {
		if (tensor == null)
			stream.println("Null");
		else
			stream.println(tensor);
	}

	public static <ELEM> void print(Tensor<ELEM> tensor) {
		print(tensor, System.out);
	}
}
