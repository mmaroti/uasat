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

package org.mmaroti.satset;

import java.util.*;

public abstract class Matrix<ELEM> {
	public final int[] shape;

	public Matrix(int[] shape) {
		if (shape == null)
			throw new IllegalArgumentException();

		this.shape = shape;
	}

	public abstract ELEM get(int[] index);

	public int getSize() {
		return getSize(shape);
	}

	public static int getSize(int[] shape) {
		long a = 1;

		for (int i = 0; i < shape.length; i++) {
			assert shape[i] >= 0;
			a *= shape[i];

			if (a > Integer.MAX_VALUE)
				throw new IllegalArgumentException();
		}

		return (int) a;
	}

	public boolean isEmpty() {
		for (int i = 0; i < shape.length; i++)
			if (shape[i] == 0)
				return true;

		return false;
	}

	public String toString() {
		if (shape.length == 0)
			return get(shape).toString();
		else if (shape.length == 1) {
			String s = "[";
			int[] index = new int[1];
			for (index[0] = 0; index[0] < shape[0]; index[0]++) {
				if (index[0] != 0)
					s += " ";
				s += get(index).toString();
			}
			s += "]";
			return s;
		} else
			return null;
	}

	public static <ELEM> Matrix<ELEM> scalar(final ELEM elem) {
		final int[] shape = new int[0];

		return new Matrix<ELEM>(shape) {
			@Override
			public ELEM get(int[] index) {
				assert index.length == 0;
				return elem;
			}
		};
	}

	public static <ELEM> Matrix<ELEM> vector(final List<ELEM> elems) {
		final int[] shape = new int[] { elems.size() };

		return new Matrix<ELEM>(shape) {
			@Override
			public ELEM get(int[] index) {
				assert index.length == 1;
				return elems.get(index[0]);
			}
		};
	}

	public static <ELEM> Matrix<ELEM> collect(final List<Matrix<ELEM>> rows,
			int[] rowShape) {
		for (Matrix<ELEM> elem : rows)
			assert Arrays.equals(elem.shape, rowShape);

		final int[] shape = new int[1 + rowShape.length];
		shape[0] = rows.size();
		System.arraycopy(rowShape, 0, shape, 1, rowShape.length);

		final int[] subidx = new int[rowShape.length];

		return new Matrix<ELEM>(shape) {
			@Override
			public ELEM get(int[] index) {
				assert index.length == shape.length;

				System.arraycopy(index, 1, subidx, 0, subidx.length);
				return rows.get(index[0]).get(subidx);
			}
		};
	}

	public Matrix<ELEM> row(int row) {
		assert shape.length >= 1 && 0 <= row && row < shape[0];

		int[] shape2 = new int[shape.length - 1];
		System.arraycopy(shape, 1, shape2, 0, shape2.length);

		final int[] index1 = new int[shape.length];
		index1[0] = row;

		final Matrix<ELEM> that = this;

		return new Matrix<ELEM>(shape2) {
			@Override
			public ELEM get(int[] index) {
				assert index.length + 1 == index1.length;

				System.arraycopy(index, 0, index1, 1, index.length);
				return that.get(index1);
			}
		};
	}

	public static Matrix<Integer> range(final int start, int end) {
		if (end < start)
			throw new IllegalArgumentException();

		final int[] shape = new int[] { end - start };

		return new Matrix<Integer>(shape) {
			@Override
			public Integer get(int[] index) {
				assert index.length == 1 && 0 <= index[0]
						&& index[0] < shape[0];

				return start + index[0];
			}
		};
	}

	public static Matrix<Integer> range(int end) {
		return range(0, end);
	}

	public int getPosition(int[] index) {
		assert index.length == shape.length;

		int pos = 0;
		for (int i = 0; i < index.length; i++) {
			assert 0 <= index[i] && index[i] < shape[i];
			pos = pos * shape[i] + index[i];
		}

		return pos;
	}

	public boolean nextIndex(int[] index) {
		assert index.length == shape.length;

		int i = index.length;
		while (--i >= 0 && ++index[i] >= shape[i])
			index[i] = 0;

		return i >= 0;
	}

	public static <ELEM> Matrix<ELEM> matrix(int[] shape, final List<ELEM> elems) {
		if (getSize(shape) != elems.size())
			throw new IllegalArgumentException();

		return new Matrix<ELEM>(shape) {
			@Override
			public ELEM get(int[] index) {
				return elems.get(getPosition(index));
			}
		};
	}

	public static <ELEM> Matrix<ELEM> matrix(int[] shape, final ELEM[] elems) {
		if (getSize(shape) != elems.length)
			throw new IllegalArgumentException();

		return new Matrix<ELEM>(shape) {
			@Override
			public ELEM get(int[] index) {
				return elems[getPosition(index)];
			}
		};
	}

	public static Matrix<Integer> matrix(int[] shape, final int[] elems) {
		if (getSize(shape) != elems.length)
			throw new IllegalArgumentException();

		return new Matrix<Integer>(shape) {
			@Override
			public Integer get(int[] index) {
				return elems[getPosition(index)];
			}
		};
	}

	public static Matrix<Boolean> matrix(int[] shape, final boolean[] elems) {
		if (getSize(shape) != elems.length)
			throw new IllegalArgumentException();

		return new Matrix<Boolean>(shape) {
			@Override
			public Boolean get(int[] index) {
				return elems[getPosition(index)];
			}
		};
	}

	public static Matrix<BoolTerm> matrix(int[] shape, final BoolTerm[] elems) {
		if (getSize(shape) != elems.length)
			throw new IllegalArgumentException();

		return new Matrix<BoolTerm>(shape) {
			@Override
			public BoolTerm get(int[] index) {
				return elems[getPosition(index)];
			}
		};
	}

	public List<ELEM> toList() {
		List<ELEM> list = new ArrayList<ELEM>();

		if (isEmpty())
			return list;

		int[] index = new int[shape.length];
		do {
			list.add(get(index));
		} while (nextIndex(index));

		return list;
	}

	public static <ELEM> Matrix<ELEM> cache(Matrix<ELEM> matrix) {
		return matrix(matrix.shape, matrix.toList());
	}

	public static <ARG, RET> Matrix<RET> apply(
			final Matrix<Func1<ARG, RET>> func, final Matrix<ARG> arg) {

		final int[] idx = new int[Math.min(func.shape.length, arg.shape.length)];

		for (int i = 0; i < idx.length; i++)
			if (func.shape[i] != arg.shape[i])
				throw new IllegalArgumentException();

		if (func.shape.length <= arg.shape.length) {
			return new Matrix<RET>(arg.shape) {
				@Override
				public RET get(int[] index) {
					System.arraycopy(index, 0, idx, 0, idx.length);

					return func.get(idx).apply1(arg.get(index));
				}
			};
		} else {
			return new Matrix<RET>(func.shape) {
				@Override
				public RET get(int[] index) {
					System.arraycopy(index, 0, idx, 0, idx.length);

					return func.get(index).apply1(arg.get(idx));
				}
			};
		}
	}

	public static <ARG, RET> Matrix<RET> apply(final Func1<ARG, RET> func,
			final Matrix<ARG> arg) {
		return new Matrix<RET>(arg.shape) {
			@Override
			public RET get(int[] index) {
				return func.apply1(arg.get(index));
			}
		};
	}
}