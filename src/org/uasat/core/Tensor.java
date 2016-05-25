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

public abstract class Tensor<ELEM> implements Iterable<ELEM> {
	private final int[] shape;

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

	public ELEM getElem(int... index) {
		assert index.length == shape.length;

		int pos = 0;
		int size = 1;
		for (int i = 0; i < shape.length; i++) {
			assert 0 <= index[i] && index[i] < shape[i];
			pos += size * index[i];
			size *= shape[i];
		}

		return getElemAt(pos);
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

		setElemAt(pos, elem);
	}

	public abstract Class<ELEM> getType();

	public abstract int getElemCount();

	public abstract ELEM getElemAt(int pos);

	public abstract void setElemAt(int pos, ELEM elem);

	protected abstract void fillElems(ELEM elem);

	protected abstract void copyElems(int srcPos, Tensor<ELEM> dst, int dstPos,
			int length);

	protected abstract boolean equalElems(Tensor<?> dst);

	private class Iter implements Iterator<ELEM> {
		private int pos;
		private final int end;

		Iter(int start, int length) {
			assert 0 <= start && 0 <= length
					&& start + length <= getElemCount();
			pos = start;
			end = start + length;
		}

		@Override
		public boolean hasNext() {
			return pos < end;
		}

		@Override
		public ELEM next() {
			return getElemAt(pos++);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Iterator<ELEM> iterator() {
		return new Iter(0, getElemCount());
	}

	public Iterable<ELEM> sliceElems(final int start, final int length) {
		return new Iterable<ELEM>() {
			@Override
			public Iterator<ELEM> iterator() {
				return new Iter(start, length);
			}
		};
	}

	public ELEM get() {
		assert getElemCount() == 1;
		return getElemAt(0);
	}

	@SuppressWarnings("unchecked")
	private static <ELEM> Tensor<ELEM> create(Class<ELEM> type, int[] shape) {
		if (type == Integer.TYPE)
			return (Tensor<ELEM>) new IntTensor(shape);
		else if (type == Boolean.TYPE)
			return (Tensor<ELEM>) new BoolTensor(shape);
		else
			return new ObjTensor<ELEM>(type, shape);
	}

	private Tensor(int[] shape) {
		this.shape = shape;
	}

	protected static class IntTensor extends Tensor<Integer> {
		private final int[] array;

		public IntTensor(int[] shape) {
			super(shape);
			this.array = new int[getSize(shape)];
		}

		@Override
		public Class<Integer> getType() {
			return Integer.TYPE;
		}

		@Override
		public int getElemCount() {
			return array.length;
		}

		@Override
		public Integer getElemAt(int index) {
			return array[index];
		}

		@Override
		public void setElemAt(int index, Integer elem) {
			array[index] = elem;
		}

		@Override
		protected void fillElems(Integer elem) {
			Arrays.fill(array, elem);
		}

		@Override
		protected void copyElems(int srcPos, Tensor<Integer> dst, int dstPos,
				int length) {
			System.arraycopy(array, srcPos, ((IntTensor) dst).array, dstPos,
					length);
		}

		@Override
		protected boolean equalElems(Tensor<?> tensor) {
			if (tensor instanceof IntTensor) {
				IntTensor t = (IntTensor) tensor;
				return Arrays.equals(array, t.array);
			} else
				return false;
		}
	}

	protected static class BoolTensor extends Tensor<Boolean> {
		private final boolean[] array;

		public BoolTensor(int[] shape) {
			super(shape);
			this.array = new boolean[getSize(shape)];
		}

		@Override
		public Class<Boolean> getType() {
			return Boolean.TYPE;
		}

		@Override
		public int getElemCount() {
			return array.length;
		}

		@Override
		public Boolean getElemAt(int index) {
			return array[index];
		}

		@Override
		public void setElemAt(int index, Boolean elem) {
			array[index] = elem;
		}

		@Override
		protected void fillElems(Boolean elem) {
			Arrays.fill(array, elem);
		}

		@Override
		protected void copyElems(int srcPos, Tensor<Boolean> dst, int dstPos,
				int length) {
			System.arraycopy(array, srcPos, ((BoolTensor) dst).array, dstPos,
					length);
		}

		@Override
		protected boolean equalElems(Tensor<?> tensor) {
			if (tensor instanceof BoolTensor) {
				BoolTensor t = (BoolTensor) tensor;
				return Arrays.equals(array, t.array);
			} else
				return false;
		}
	}

	@SuppressWarnings("unchecked")
	protected static class ObjTensor<ELEM> extends Tensor<ELEM> {
		private final Class<ELEM> type;
		private final Object[] array;

		public ObjTensor(Class<ELEM> type, int[] shape) {
			super(shape);
			this.type = type;
			this.array = new Object[getSize(shape)];
		}

		@Override
		public Class<ELEM> getType() {
			return type;
		}

		@Override
		public int getElemCount() {
			return array.length;
		}

		@Override
		public ELEM getElemAt(int index) {
			return (ELEM) array[index];
		}

		@Override
		public void setElemAt(int index, ELEM elem) {
			array[index] = elem;
		}

		@Override
		protected void fillElems(ELEM elem) {
			Arrays.fill(array, elem);
		}

		@Override
		protected void copyElems(int srcPos, Tensor<ELEM> dst, int dstPos,
				int length) {
			System.arraycopy(array, srcPos, ((ObjTensor<ELEM>) dst).array,
					dstPos, length);
		}

		@Override
		protected boolean equalElems(Tensor<?> tensor) {
			if (tensor instanceof ObjTensor) {
				ObjTensor<ELEM> t = (ObjTensor<ELEM>) tensor;
				return type == t.type && Arrays.equals(array, t.array);
			} else
				return false;
		}
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
		Tensor<ELEM> tensor = create(type, shape);

		for (int i = 0; i < tensor.getElemCount(); i++)
			tensor.setElemAt(i, func.call());

		return tensor;
	}

	public static Tensor<Boolean> generate(int[] shape, Func0<Boolean> func) {
		return generate(Boolean.TYPE, shape, func);
	}

	public static <ELEM> Tensor<ELEM> generate(Class<ELEM> type, int[] shape,
			Func1<ELEM, int[]> func) {
		Tensor<ELEM> tensor = create(type, shape);

		if (tensor.getElemCount() > 0) {
			int[] index = new int[shape.length];
			int pos = 0;
			outer: for (;;) {
				tensor.setElemAt(pos++, func.call(index));
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
		Tensor<ELEM> tensor = create(type, new int[] { dim });

		for (int i = 0; i < dim; i++)
			tensor.setElemAt(i, func.call(i));

		return tensor;
	}

	public static Tensor<Boolean> generate(int dim, Func1<Boolean, Integer> func) {
		return generate(Boolean.TYPE, dim, func);
	}

	public static <ELEM> Tensor<ELEM> generate(Class<ELEM> type, int dim1,
			int dim2, Func2<ELEM, Integer, Integer> func) {
		Tensor<ELEM> tensor = create(type, new int[] { dim1, dim2 });

		int pos = 0;
		for (int j = 0; j < dim2; j++)
			for (int i = 0; i < dim1; i++)
				tensor.setElemAt(pos++, func.call(i, j));

		return tensor;
	}

	public static Tensor<Boolean> generate(int dim1, int dim2,
			Func2<Boolean, Integer, Integer> func) {
		return generate(Boolean.TYPE, dim1, dim2, func);
	}

	public static <ELEM> Tensor<ELEM> constant(Class<ELEM> type, int[] shape,
			ELEM elem) {
		Tensor<ELEM> tensor = create(type, shape);
		tensor.fillElems(elem);

		return tensor;
	}

	public static Tensor<Boolean> constant(int[] shape, Boolean elem) {
		return constant(Boolean.TYPE, shape, elem);
	}

	public static <ELEM> Tensor<ELEM> scalar(Class<ELEM> type, ELEM elem) {
		Tensor<ELEM> tensor = create(type, new int[0]);
		tensor.setElemAt(0, elem);

		return tensor;
	}

	public static Tensor<Boolean> scalar(Boolean elem) {
		return scalar(Boolean.TYPE, elem);
	}

	public static <ELEM> Tensor<ELEM> vector(Class<ELEM> type, List<ELEM> elems) {
		Tensor<ELEM> tensor = create(type, new int[] { elems.size() });

		int pos = 0;
		for (ELEM elem : elems)
			tensor.setElemAt(pos++, elem);

		return tensor;
	}

	public static Tensor<Boolean> vector(List<Boolean> elems) {
		return vector(Boolean.TYPE, elems);
	}

	public static <ELEM> Tensor<ELEM> matrix(Class<ELEM> type, int dim1,
			int dim2, List<ELEM> elems) {
		Tensor<ELEM> tensor = create(type, new int[] { dim1, dim2 });
		assert tensor.getElemCount() == elems.size();

		int pos = 0;
		for (ELEM elem : elems)
			tensor.setElemAt(pos++, elem);

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

		Tensor<ELEM> tensor = create(arg.getType(), shape);

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

		if (tensor.getElemCount() > 0) {
			int pos = 0;
			int idx = 0;
			outer: for (;;) {
				tensor.setElemAt(idx++, arg.getElemAt(pos));

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
			assert idx == tensor.getElemCount();
		}

		return tensor;
	}

	public static <ELEM, ELEM1> Tensor<ELEM> map(Class<ELEM> type,
			Func1<ELEM, ELEM1> func, Tensor<ELEM1> arg) {
		Tensor<ELEM> tensor = create(type, arg.shape);

		for (int i = 0; i < tensor.getElemCount(); i++)
			tensor.setElemAt(i, func.call(arg.getElemAt(i)));

		return tensor;
	}

	public static <ELEM> Tensor<ELEM> map(Func1<ELEM, ELEM> func,
			Tensor<ELEM> arg) {
		return map(arg.getType(), func, arg);
	}

	public static <ELEM, ELEM1, ELEM2> Tensor<ELEM> map2(Class<ELEM> type,
			Func2<ELEM, ELEM1, ELEM2> func, Tensor<ELEM1> arg1,
			Tensor<ELEM2> arg2) {
		assert Arrays.equals(arg1.shape, arg2.shape);
		Tensor<ELEM> tensor = create(type, arg1.shape);

		for (int i = 0; i < tensor.getElemCount(); i++)
			tensor.setElemAt(i, func.call(arg1.getElemAt(i), arg2.getElemAt(i)));

		return tensor;
	}

	public static <ELEM> Tensor<ELEM> map2(Func2<ELEM, ELEM, ELEM> func,
			Tensor<ELEM> arg1, Tensor<ELEM> arg2) {
		assert arg1.getType() == arg2.getType();
		return map2(arg1.getType(), func, arg1, arg2);
	}

	public static <ELEM1, ELEM2> Tensor<ELEM2> fold(Class<ELEM2> type,
			Func1<ELEM2, Iterable<ELEM1>> func, int proj, Tensor<ELEM1> arg) {
		int[] shape = new int[proj];
		System.arraycopy(arg.shape, 0, shape, 0, proj);
		int step = getSize(shape);

		shape = new int[arg.getOrder() - proj];
		System.arraycopy(arg.shape, proj, shape, 0, shape.length);
		Tensor<ELEM2> tensor = create(type, shape);

		int pos = 0;
		for (int i = 0; i < tensor.getElemCount(); i++) {
			tensor.setElemAt(i, func.call(arg.sliceElems(pos, step)));
			pos += step;
		}

		return tensor;
	}

	public static <ELEM> Tensor<ELEM> fold(Func1<ELEM, Iterable<ELEM>> func,
			int proj, Tensor<ELEM> arg) {
		return fold(arg.getType(), func, proj, arg);
	}

	public static <ELEM> Tensor<ELEM> stack(Class<ELEM> type,
			int[] commonShape, List<Tensor<ELEM>> list) {
		for (Tensor<ELEM> tensor : list)
			assert Arrays.equals(commonShape, tensor.shape)
					&& tensor.getType() == type;

		int count = list.size();
		int size = getSize(commonShape);

		int[] shape = new int[commonShape.length + 1];
		System.arraycopy(commonShape, 0, shape, 0, commonShape.length);
		shape[commonShape.length] = list.size();

		Tensor<ELEM> tensor = create(type, shape);

		int pos = 0;
		for (int i = 0; i < count; i++) {
			list.get(i).copyElems(0, tensor, pos, size);
			pos += size;
		}

		return tensor;
	}

	public static <ELEM> Tensor<ELEM> stack(List<Tensor<ELEM>> list) {
		assert list.size() >= 1;
		return stack(list.get(0).getType(), list.get(0).shape, list);
	}

	public static <ELEM> List<Tensor<ELEM>> unstack(Tensor<ELEM> tensor) {
		assert tensor.getOrder() >= 1;

		int[] shape = new int[tensor.getOrder() - 1];
		System.arraycopy(tensor.getShape(), 0, shape, 0, shape.length);
		int size = getSize(shape);
		int last = tensor.getDim(shape.length);

		List<Tensor<ELEM>> list = new ArrayList<Tensor<ELEM>>();
		for (int i = 0; i < last; i++) {
			Tensor<ELEM> t = create(tensor.getType(), shape);
			tensor.copyElems(i * size, t, 0, size);
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

		assert list.size() == tensor.getElemCount();
		return list;
	}

	public static <ELEM> Tensor<ELEM> concat(Tensor<ELEM> arg1,
			Tensor<ELEM> arg2) {
		assert arg1.getOrder() == arg2.getOrder()
				&& arg1.getType() == arg2.getType();

		int a = arg1.getOrder() - 1;
		for (int i = 0; i < a; i++)
			assert arg1.getDim(i) == arg2.getDim(i);

		int[] shape = new int[arg1.getOrder()];
		System.arraycopy(arg1.getShape(), 0, shape, 0, a);
		shape[a] = arg1.getDim(a) + arg2.getDim(a);

		Tensor<ELEM> tensor = create(arg1.getType(), shape);
		assert arg1.getElemCount() + arg2.getElemCount() == tensor
				.getElemCount();
		arg1.copyElems(0, tensor, 0, arg1.getElemCount());
		arg2.copyElems(0, tensor, arg1.getElemCount(), arg2.getElemCount());

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
		for (int i = 0; i < getElemCount(); i++) {
			if (i != 0)
				str.append(',');
			str.append(getElemAt(i));
		}
		str.append("]");

		return str.toString();
	}

	@Override
	public boolean equals(Object other) {
		@SuppressWarnings("unchecked")
		Tensor<ELEM> tensor = (Tensor<ELEM>) other;

		return Arrays.equals(shape, tensor.shape) && equalElems(tensor);
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
