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

		digraph_mask = Tensor.constant(new int[] { size, size }, Boolean.FALSE);
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

	public void addUniOccur(final int minority) {
		assert 0 <= minority && minority < size;

		cases.add(new Case() {
			@Override
			public boolean matches(int[] tuple) {
				int a = 0;

				for (int i = 0; i < tuple.length; i++) {
					if (tuple[i] == minority)
						a++;
				}

				return a == 1;
			}

			@Override
			public String toString() {
				return "unioccur " + minority;
			}
		});
	}

	public void addAllUniOccur() {
		for (int minority = 0; minority < size; minority++)
			addUniOccur(minority);
	}

	public void addDiagAndPair(final int majority, final int first, final int second) {
		assert 0 <= majority && majority < size;
		assert first != majority && second != majority;

		cases.add(new Case() {
			@Override
			public boolean matches(int[] tuple) {
				int a = 0;
				int b = 0;
				int c = 0;

				int last = tuple[tuple.length - 1];
				for (int i = 0; i < tuple.length; i++) {
					if (tuple[i] == majority)
						a++;
					else if (tuple[i] == first && last == majority)
						b++;
					else if (tuple[i] == second && last == first)
						c++;
					else
						return false;

					last = tuple[i];
				}

				return a == tuple.length - 2 && b == 1 && c == 1;
			}

			@Override
			public String toString() {
				return "diagandpair " + majority + " " + first + " " + second;
			}
		});
	}

	public void addAllDiagAndPair() {
		for (int majority = 0; majority < size; majority++) {
			for (int first = 0; first < size; first++) {
				if (first == majority)
					continue;
				for (int second = 0; second < size; second++) {
					if (second == majority)
						continue;

					addDiagAndPair(majority, first, second);
				}
			}
		}
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

	private Tensor<Boolean> digraph_mask;

	public void addDigraph(final Relation<Boolean> graph) {
		assert graph.getArity() == 2 && graph.getSize() == size;

		cases.add(new Case() {
			@Override
			public boolean matches(int[] tuple) {
				digraph_mask.fillElems(Boolean.FALSE);

				digraph_mask.setElem(Boolean.TRUE, tuple[tuple.length - 1], tuple[0]);
				for (int i = 1; i < tuple.length; i++)
					digraph_mask.setElem(Boolean.TRUE, tuple[i - 1], tuple[i]);

				return graph.getTensor().equals(digraph_mask);
			}

			@Override
			public String toString() {
				return "digraph " + Relation.format(graph);
			}
		});
	}

	public void addAllDigarphs() {
		SatProblem prob = new SatProblem(digraph_mask.getShape()) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg, List<Tensor<BOOL>> tensors) {
				Relation<BOOL> rel = new Relation<BOOL>(alg, tensors.get(0));

				BOOL b = rel.isNotEmpty();
				b = alg.and(b, rel.project(0).isEqualTo(rel.project(1)));
				return b;
			}
		};

		List<Tensor<Boolean>> digraphs = Tensor.unstack(prob.solveAll(SatSolver.getDefault()).get(0));
		for (Tensor<Boolean> digraph : digraphs)
			addDigraph(Relation.wrap(digraph));
	}

	public <BOOL> Relation<BOOL> generateRelation(final Relation<BOOL> input, final int arity) {
		assert input.getArity() == 1 && input.getSize() == cases.size();

		Tensor<BOOL> tensor = Tensor.generate(input.getTensor().getType(), Util.createShape(size, arity),
				new Func1<BOOL, int[]>() {
					@Override
					public BOOL call(int[] elem) {
						return input.getValue(getCaseIndex(elem));
					}
				});

		return new Relation<BOOL>(input.getAlg(), tensor);
	}

	public <BOOL> Operation<BOOL> generateOperation(final Function<BOOL> input, final int arity) {
		assert input.getDomain() == cases.size() && input.getCodomain() == size;

		final int[] tuple = new int[arity];

		Tensor<BOOL> tensor = Tensor.generate(input.getTensor().getType(), Util.createShape(size, arity + 1),
				new Func1<BOOL, int[]>() {
					@Override
					public BOOL call(int[] elem) {
						System.arraycopy(elem, 1, tuple, 0, arity);
						return input.hasValue(elem[0], getCaseIndex(tuple));
					}
				});

		return new Operation<BOOL>(input.getAlg(), tensor);
	}
}
