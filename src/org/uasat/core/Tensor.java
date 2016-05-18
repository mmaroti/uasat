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
	public final Class<ELEM> type;
	private final int[] shape;
	private final Vector<ELEM> elems;

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

	@Override
	public Iterator<ELEM> iterator() {
		return elems.iterator();
	}

	public ELEM getElem(int... index) {
		assert index.length == shape.length;

		int pos = 0;
		int size = 1;
		for (int i = 0; i < shape.length; i++) {
			assert 0 <= index[i] && index[i] < shape[i];
			pos += size * index[i];
			size *= shape[i];
		}

		return elems.get(pos);
	}

	public void setElem(ELEM elem, int... index) {
		assert index.length == shape.length;

		int pos = 0;
		int size = 1;
		for (int i = 0; i < shape.length; i++) {
			assert 0 <= index[i] && index[i] < shape[i];
			pos += size * index[i];
			size *= shape[i];
		}

		elems.set(pos, elem);
	}

	public int getElemCount() {
		return getSize(shape);
	}

	public ELEM getElemAt(int pos) {
		return elems.get(pos);
	}

	public void setElemAt(int pos, ELEM elem) {
		elems.set(pos, elem);
	}

	public ELEM get() {
		assert elems.size() == 1;
		return elems.get(0);
	}

	private Tensor(Class<ELEM> type, int[] shape) {
		this.type = type;
		this.shape = shape;
		this.elems = Vector.create(type, getSize(shape));
	}

	private static int getSize(int[] shape) {
		int size = 1;
		for (int i = 0; i < shape.length; i++) {
			assert 0 <= shape[i];
			size *= shape[i];
		}
		return size;
	}

	public static <ELEM> Tensor<ELEM> generate(Class<ELEM> type, int[] shape,
			Func0<ELEM> func) {
		Tensor<ELEM> tensor = new Tensor<ELEM>(type, shape);

		for (int i = 0; i < tensor.elems.size(); i++)
			tensor.elems.set(i, func.call());

		return tensor;
	}

	public static Tensor<Boolean> generate(int[] shape, Func0<Boolean> func) {
		return generate(Boolean.TYPE, shape, func);
	}

	public static <ELEM> Tensor<ELEM> generate(Class<ELEM> type, int[] shape,
			Func1<ELEM, int[]> func) {
		Tensor<ELEM> tensor = new Tensor<ELEM>(type, shape);

		if (tensor.elems.size() > 0) {
			int[] index = new int[shape.length];
			int pos = 0;
			outer: for (;;) {
				tensor.elems.set(pos++, func.call(index));
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

	public static Tensor<Boolean> generate(int[] shape,
			Func1<Boolean, int[]> func) {
		return generate(Boolean.TYPE, shape, func);
	}

	public static <ELEM> Tensor<ELEM> generate(Class<ELEM> type, int dim,
			Func1<ELEM, Integer> func) {
		Tensor<ELEM> tensor = new Tensor<ELEM>(type, new int[] { dim });

		for (int i = 0; i < dim; i++)
			tensor.elems.set(i, func.call(i));

		return tensor;
	}

	public static Tensor<Boolean> generate(int dim, Func1<Boolean, Integer> func) {
		return generate(Boolean.TYPE, dim, func);
	}

	public static <ELEM> Tensor<ELEM> generate(Class<ELEM> type, int dim1,
			int dim2, Func2<ELEM, Integer, Integer> func) {
		Tensor<ELEM> tensor = new Tensor<ELEM>(type, new int[] { dim1, dim2 });

		int pos = 0;
		for (int j = 0; j < dim2; j++)
			for (int i = 0; i < dim1; i++)
				tensor.elems.set(pos++, func.call(i, j));

		return tensor;
	}

	public static Tensor<Boolean> generate(int dim1, int dim2,
			Func2<Boolean, Integer, Integer> func) {
		return generate(Boolean.TYPE, dim1, dim2, func);
	}

	public static <ELEM> Tensor<ELEM> constant(Class<ELEM> type, int[] shape,
			ELEM elem) {
		Tensor<ELEM> tensor = new Tensor<ELEM>(type, shape);
		tensor.elems.fill(elem);

		return tensor;
	}

	public static Tensor<Boolean> constant(int[] shape, Boolean elem) {
		return constant(Boolean.TYPE, shape, elem);
	}

	public static <ELEM> Tensor<ELEM> scalar(Class<ELEM> type, ELEM elem) {
		Tensor<ELEM> tensor = new Tensor<ELEM>(type, new int[0]);
		tensor.elems.set(0, elem);

		return tensor;
	}

	public static Tensor<Boolean> scalar(Boolean elem) {
		return scalar(Boolean.TYPE, elem);
	}

	public static <ELEM> Tensor<ELEM> vector(Class<ELEM> type, List<ELEM> elems) {
		Tensor<ELEM> tensor = new Tensor<ELEM>(type, new int[] { elems.size() });

		int pos = 0;
		for (ELEM elem : elems)
			tensor.elems.set(pos++, elem);

		return tensor;
	}

	public static Tensor<Boolean> vector(List<Boolean> elems) {
		return vector(Boolean.TYPE, elems);
	}

	public static <ELEM> Tensor<ELEM> matrix(Class<ELEM> type, int dim1,
			int dim2, List<ELEM> elems) {
		Tensor<ELEM> tensor = new Tensor<ELEM>(type, new int[] { dim1, dim2 });
		assert tensor.elems.size() == elems.size();

		int pos = 0;
		for (ELEM elem : elems)
			tensor.elems.set(pos++, elem);

		return tensor;
	}

	public static Tensor<Boolean> matrix(int dim1, int dim2, List<Boolean> elems) {
		return matrix(Boolean.TYPE, dim1, dim2, elems);
	}

	public static <ELEM> Tensor<ELEM> diagonal(Class<ELEM> type,
			final Tensor<ELEM> tensor, final int[] map, final ELEM defval) {
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

		return Tensor.generate(type, shape, new Func1<ELEM, int[]>() {
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

	/**
	 * Reshapes the tensor to the given new shape, so that the old coordinates
	 * are mapped to the new one using the map vector.
	 */
	public static <ELEM> Tensor<ELEM> reshape(Tensor<ELEM> arg, int[] shape,
			int[] map) {
		assert arg.getOrder() == map.length;

		Tensor<ELEM> tensor = new Tensor<ELEM>(arg.type, shape);

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

		Vector<ELEM> src = arg.elems;
		Vector<ELEM> dst = tensor.elems;

		if (dst.size() > 0) {
			int pos = 0;
			int idx = 0;
			outer: for (;;) {
				dst.set(idx++, src.get(pos));

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
			assert idx == dst.size();
		}

		return tensor;
	}

	public static <ELEM, ELEM1> Tensor<ELEM> map(Class<ELEM> type,
			Func1<ELEM, ELEM1> func, Tensor<ELEM1> arg) {
		Tensor<ELEM> tensor = new Tensor<ELEM>(type, arg.shape);

		for (int i = 0; i < tensor.elems.size(); i++)
			tensor.elems.set(i, func.call(arg.elems.get(i)));

		return tensor;
	}

	public static <ELEM> Tensor<ELEM> map(Func1<ELEM, ELEM> func,
			Tensor<ELEM> arg) {
		return map(arg.type, func, arg);
	}

	public static <ELEM, ELEM1, ELEM2> Tensor<ELEM> map2(Class<ELEM> type,
			Func2<ELEM, ELEM1, ELEM2> func, Tensor<ELEM1> arg1,
			Tensor<ELEM2> arg2) {
		assert Arrays.equals(arg1.shape, arg2.shape);
		Tensor<ELEM> tensor = new Tensor<ELEM>(type, arg1.shape);

		for (int i = 0; i < tensor.elems.size(); i++)
			tensor.elems
					.set(i, func.call(arg1.elems.get(i), arg2.elems.get(i)));

		return tensor;
	}

	public static <ELEM> Tensor<ELEM> map2(Func2<ELEM, ELEM, ELEM> func,
			Tensor<ELEM> arg1, Tensor<ELEM> arg2) {
		assert arg1.type == arg2.type;
		return map2(arg1.type, func, arg1, arg2);
	}

	public static <ELEM1, ELEM2> Tensor<ELEM2> fold(Class<ELEM2> type,
			Func1<ELEM2, Iterable<ELEM1>> func, int proj, Tensor<ELEM1> arg) {
		int[] shape = new int[proj];
		System.arraycopy(arg.shape, 0, shape, 0, proj);
		int step = getSize(shape);

		shape = new int[arg.getOrder() - proj];
		System.arraycopy(arg.shape, proj, shape, 0, shape.length);
		Tensor<ELEM2> tensor = new Tensor<ELEM2>(type, shape);

		int pos = 0;
		for (int i = 0; i < tensor.elems.size(); i++) {
			tensor.elems.set(i, func.call(arg.elems.slice(pos, step)));
			pos += step;
		}

		return tensor;
	}

	public static <ELEM> Tensor<ELEM> fold(Func1<ELEM, Iterable<ELEM>> func,
			int proj, Tensor<ELEM> arg) {
		return fold(arg.type, func, proj, arg);
	}

	public static <ELEM> Tensor<ELEM> stack(Class<ELEM> type,
			int[] commonShape, List<Tensor<ELEM>> list) {
		for (Tensor<ELEM> tensor : list)
			assert Arrays.equals(commonShape, tensor.shape)
					&& tensor.type == type;

		int count = list.size();
		int size = getSize(commonShape);

		int[] shape = new int[commonShape.length + 1];
		System.arraycopy(commonShape, 0, shape, 0, commonShape.length);
		shape[commonShape.length] = list.size();

		Tensor<ELEM> tensor = new Tensor<ELEM>(type, shape);

		int pos = 0;
		for (int i = 0; i < count; i++) {
			list.get(i).elems.copy(0, tensor.elems, pos, size);
			pos += size;
		}

		return tensor;
	}

	public static <ELEM> Tensor<ELEM> stack(List<Tensor<ELEM>> list) {
		assert list.size() >= 1;
		return stack(list.get(0).type, list.get(0).shape, list);
	}

	public static <ELEM> List<Tensor<ELEM>> unstack(Tensor<ELEM> tensor) {
		assert tensor.getOrder() >= 1;

		int[] shape = new int[tensor.getOrder() - 1];
		System.arraycopy(tensor.getShape(), 0, shape, 0, shape.length);
		int size = getSize(shape);
		int last = tensor.getDim(shape.length);

		List<Tensor<ELEM>> list = new ArrayList<Tensor<ELEM>>();
		for (int i = 0; i < last; i++) {
			Tensor<ELEM> t = new Tensor<ELEM>(tensor.type, shape);
			tensor.elems.copy(i * size, t.elems, 0, size);
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

		assert list.size() == tensor.elems.size();
		return list;
	}

	public static <ELEM> Tensor<ELEM> concat(Tensor<ELEM> arg1,
			Tensor<ELEM> arg2) {
		assert arg1.getOrder() == arg2.getOrder() && arg1.type == arg2.type;

		int a = arg1.getOrder() - 1;
		for (int i = 0; i < a; i++)
			assert arg1.getDim(i) == arg2.getDim(i);

		int[] shape = new int[arg1.getOrder()];
		System.arraycopy(arg1.getShape(), 0, shape, 0, a);
		shape[a] = arg1.getDim(a) + arg2.getDim(a);

		Tensor<ELEM> tensor = new Tensor<ELEM>(arg1.type, shape);
		assert arg1.elems.size() + arg2.elems.size() == tensor.elems.size();
		arg1.elems.copy(0, tensor.elems, 0, arg1.elems.size());
		arg2.elems.copy(0, tensor.elems, arg1.elems.size(), arg2.elems.size());

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
		for (int i = 0; i < elems.size(); i++) {
			if (i != 0)
				str.append(',');
			str.append(elems.get(i));
		}
		str.append("]");

		return str.toString();
	}

	@Override
	public boolean equals(Object other) {
		Tensor<?> tensor = (Tensor<?>) other;

		return Arrays.equals(shape, tensor.shape) && elems.equals(tensor.elems);
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
