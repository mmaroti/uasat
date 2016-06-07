/**
 *	Copyright (C) Miklos Maroti, 2016
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

package org.uasat.math;

import java.util.*;

import org.uasat.core.*;

public class DefByCases {
	protected final int size;
	protected final List<Case> cases;

	public DefByCases(int size) {
		assert size >= 2;

		this.size = size;
		cases = new ArrayList<Case>();
	}

	public int getSize() {
		return size;
	}

	public int getCases() {
		return cases.size();
	}

	private abstract class Case {
		public abstract boolean matches(int[] tuple);

		@Override
		public abstract String toString();
	}

	public int getCaseIndex(int[] tuple) {
		for (int i = 0; i < cases.size(); i++)
			if (cases.get(i).matches(tuple))
				return i;

		throw new IllegalArgumentException("tuple not matched");
	}

	public void printCases() {
		System.out.println("definition by cases: " + cases.size());
		for (int i = 0; i < cases.size(); i++)
			System.out.println(i + ":\t" + cases.get(i));
		System.out.println();
	}

	public void addDiagonal(final int elem) {
		assert 0 <= elem && elem < size;

		cases.add(new Case() {
			@Override
			public boolean matches(int[] tuple) {
				for (int i = 0; i < tuple.length; i++)
					if (tuple[i] != elem)
						return false;
				return true;
			}

			@Override
			public String toString() {
				return "diagonal " + elem;
			}
		});
	}

	public void addAllDiagonals() {
		for (int elem = 0; elem < size; elem++)
			addDiagonal(elem);
	}

	public void addNearUnanimous(final int majority, final int minority) {
		assert 0 <= majority && majority < size;
		assert 0 <= minority && minority < size;
		assert majority != minority;

		cases.add(new Case() {
			@Override
			public boolean matches(int[] tuple) {
				int a = 0;
				int b = 0;

				for (int i = 0; i < tuple.length; i++) {
					if (tuple[i] == majority)
						a++;
					else if (tuple[i] == minority)
						b++;
					else
						return false;
				}

				return a == tuple.length - 1 && b == 1;
			}

			@Override
			public String toString() {
				return "nearunanimous " + majority + " " + minority;
			}
		});
	}

	public void addAllNearUnaMajor(int majority) {
		assert 0 <= majority && majority < size;
		for (int minority = 0; minority < size; minority++)
			if (minority != majority)
				addNearUnanimous(majority, minority);
	}

	public void addAllNearUnaMinor(int minority) {
		assert 0 <= minority && minority < size;
		for (int majority = 0; majority < size; majority++)
			if (minority != majority)
				addNearUnanimous(minority, majority);
	}

	public void addAllNearUnanimous() {
		for (int majority = 0; majority < size; majority++)
			addAllNearUnaMajor(majority);
	}

	public void addDiagButTwo(final int majority, final int minority1,
			final int minority2) {
		assert 0 <= majority && majority < size;
		assert 0 <= minority1 && minority1 < size;
		assert 0 <= minority2 && minority2 < size;
		assert majority != minority1 && majority != minority2
				&& minority1 < minority2;

		cases.add(new Case() {
			@Override
			public boolean matches(int[] tuple) {
				int a = 0;
				int b = 0;
				int c = 0;

				for (int i = 0; i < tuple.length; i++) {
					if (tuple[i] == majority)
						a++;
					else if (tuple[i] == minority1)
						b++;
					else if (tuple[i] == minority2)
						c++;
					else
						return false;
				}

				return a == tuple.length - 2 && b == 1 && c == 1;
			}

			@Override
			public String toString() {
				return "diagbuttwo " + majority + " " + minority1 + " "
						+ minority2;
			}
		});
	}

	public void addRangeTwo(final int elem1, final int elem2) {
		assert 0 <= elem1 && elem1 < elem2 && elem2 < size;

		cases.add(new Case() {
			@Override
			public boolean matches(int[] tuple) {
				int a = 0;
				int b = 0;

				for (int i = 0; i < tuple.length; i++) {
					if (tuple[i] == elem1)
						a++;
					else if (tuple[i] == elem2)
						b++;
					else
						return false;
				}

				return a > 0 && b > 0;
			}

			@Override
			public String toString() {
				return "rangetwo " + elem1 + " " + elem2;
			}
		});
	}

	public void addAllRangeTwo() {
		for (int elem1 = 0; elem1 < size - 1; elem1++)
			for (int elem2 = elem1 + 1; elem2 < size; elem2++)
				addRangeTwo(elem1, elem2);
	}

	public void addOtherwise() {
		cases.add(new Case() {
			@Override
			public boolean matches(int[] tuple) {
				return true;
			}

			@Override
			public String toString() {
				return "otherwise";
			}
		});
	}

	public void addFirstProj(final int elem) {
		assert 0 <= elem && elem < size;

		cases.add(new Case() {
			@Override
			public boolean matches(int[] tuple) {
				return tuple[0] == elem;
			}

			@Override
			public String toString() {
				return "firstproj " + elem;
			}
		});
	}

	public void addAllFirstProj() {
		for (int elem = 0; elem < size; elem++)
			addFirstProj(elem);
	}

	public <BOOL> Relation<BOOL> generateRelation(final Relation<BOOL> input,
			final int arity) {
		assert input.getArity() == 1 && input.getSize() == cases.size();

		Tensor<BOOL> tensor = Tensor.generate(input.getTensor().getType(),
				Util.createShape(size, arity), new Func1<BOOL, int[]>() {
					@Override
					public BOOL call(int[] elem) {
						return input.getValue(getCaseIndex(elem));
					}
				});

		return new Relation<BOOL>(input.getAlg(), tensor);
	}

	public <BOOL> Operation<BOOL> generateOperation(final Function<BOOL> input,
			final int arity) {
		assert input.getDomain() == cases.size() && input.getCodomain() == size;

		final int[] tuple = new int[arity];

		Tensor<BOOL> tensor = Tensor.generate(input.getTensor().getType(),
				Util.createShape(size, arity + 1), new Func1<BOOL, int[]>() {
					@Override
					public BOOL call(int[] elem) {
						System.arraycopy(elem, 1, tuple, 0, arity);
						return input.hasValue(elem[0], getCaseIndex(tuple));
					}
				});

		return new Operation<BOOL>(input.getAlg(), tensor);
	}
}
