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

package org.uasat.research;

import java.text.DecimalFormat;
import java.util.*;

import org.uasat.core.*;
import org.uasat.math.*;
import org.uasat.solvers.*;

public class DigraphPoly {
	private Relation<Boolean> relation;
	private int MAX_SOLUTIONS = 1;

	public DigraphPoly(Relation<Boolean> relation) {
		assert relation.getArity() == 2;
		this.relation = relation;
	}

	private void printCount(String options, String what, int count) {
		String s = options.trim();
		if (!s.isEmpty())
			s += ' ';
		s += what;
		s += ": ";
		if (count >= MAX_SOLUTIONS)
			s += ">= ";
		s += count;
		System.out.println(s);
	}

	public List<Relation<Boolean>> printDefinableSubalgs(String options,
			boolean print) {
		int size = relation.getSize();

		final Set<Relation<Boolean>> set = new TreeSet<Relation<Boolean>>(
				Relation.COMPARATOR);
		set.add(Relation.full(size, 1));

		List<BoolProblem> problems = new ArrayList<BoolProblem>();
		Tensor<Boolean> full = Relation.full(size, 1).getTensor();
		Tensor<Boolean> empty = Relation.empty(size, 1).getTensor();

		BoolProblem intersect = new BoolProblem(full, empty, empty) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> s0 = new Relation<BOOL>(alg, tensors.get(0));
				Relation<BOOL> s1 = new Relation<BOOL>(alg, tensors.get(1));
				Relation<BOOL> s2 = new Relation<BOOL>(alg, tensors.get(2));

				BOOL b = alg.not(s0.isMemberOf(set));
				b = alg.and(b, s1.isMemberOf(set));
				b = alg.and(b, s2.isMemberOf(set));
				b = alg.and(b, s0.isEqualTo(s1.intersect(s2)));

				return b;
			}
		};

		BoolProblem union = new BoolProblem(full, empty, empty) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> s0 = new Relation<BOOL>(alg, tensors.get(0));
				Relation<BOOL> s1 = new Relation<BOOL>(alg, tensors.get(1));
				Relation<BOOL> s2 = new Relation<BOOL>(alg, tensors.get(2));

				BOOL b = alg.not(s0.isMemberOf(set));
				b = alg.and(b, s1.isMemberOf(set));
				b = alg.and(b, s2.isMemberOf(set));
				b = alg.and(b, s0.isEqualTo(s1.union(s2)));

				return b;
			}
		};

		BoolProblem upset = new BoolProblem(full, empty) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> rel = Relation.lift(alg, relation);
				Relation<BOOL> s0 = new Relation<BOOL>(alg, tensors.get(0));
				Relation<BOOL> s1 = new Relation<BOOL>(alg, tensors.get(1));

				BOOL b = alg.not(s0.isMemberOf(set));
				b = alg.and(b, s1.isMemberOf(set));
				b = alg.and(b, s0.isEqualTo(s1.compose(rel)));

				return b;
			}
		};

		BoolProblem downset = new BoolProblem(full, empty) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> rel = Relation.lift(alg, relation);
				Relation<BOOL> s0 = new Relation<BOOL>(alg, tensors.get(0));
				Relation<BOOL> s1 = new Relation<BOOL>(alg, tensors.get(1));

				BOOL b = alg.not(s0.isMemberOf(set));
				b = alg.and(b, s1.isMemberOf(set));
				b = alg.and(b, s0.isEqualTo(rel.compose(s1)));

				return b;
			}
		};

		BoolProblem union_upset = new BoolProblem(full, empty, empty) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> rel = Relation.lift(alg, relation);
				Relation<BOOL> s0 = new Relation<BOOL>(alg, tensors.get(0));
				Relation<BOOL> s1 = new Relation<BOOL>(alg, tensors.get(1));
				Relation<BOOL> s2 = new Relation<BOOL>(alg, tensors.get(2));

				BOOL b = alg.not(s0.isMemberOf(set));
				b = alg.and(b, s1.isMemberOf(set));
				b = alg.and(b, s2.isMemberOf(set));
				b = alg.and(b, s0.isEqualTo(s1.union(s2).compose(rel)));

				return b;
			}
		};

		BoolProblem union_downset = new BoolProblem(full, empty, empty) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> rel = Relation.lift(alg, relation);
				Relation<BOOL> s0 = new Relation<BOOL>(alg, tensors.get(0));
				Relation<BOOL> s1 = new Relation<BOOL>(alg, tensors.get(1));
				Relation<BOOL> s2 = new Relation<BOOL>(alg, tensors.get(2));

				BOOL b = alg.not(s0.isMemberOf(set));
				b = alg.and(b, s1.isMemberOf(set));
				b = alg.and(b, s2.isMemberOf(set));
				b = alg.and(b, s0.isEqualTo(rel.compose(s1.union(s2))));

				return b;
			}
		};

		boolean nonempty = false;
		for (String token : options.split(" ")) {
			if (token.equals("emptyset"))
				set.add(Relation.empty(size, 1));
			else if (token.equals("singletons")) {
				for (int i = 0; i < size; i++)
					set.add(Relation.singleton(size, i));
			} else if (token.equals("primesets")) {
				for (int i = 0; i < size; i++) {
					set.add(Relation.singleton(size, i).compose(relation));
					set.add(relation.compose(Relation.singleton(size, i)));
				}
			} else if (token.equals("prime-upsets")) {
				for (int i = 0; i < size; i++)
					set.add(Relation.singleton(size, i).compose(relation));
			} else if (token.equals("prime-downsets")) {
				for (int i = 0; i < size; i++)
					set.add(relation.compose(Relation.singleton(size, i)));
			} else if (token.equals("intersect"))
				problems.add(intersect);
			else if (token.equals("union"))
				problems.add(union);
			else if (token.equals("upset"))
				problems.add(upset);
			else if (token.equals("downset"))
				problems.add(downset);
			else if (token.equals("union-upset"))
				problems.add(union_upset);
			else if (token.equals("union-downset"))
				problems.add(union_downset);
			else if (token.equals("treedef")) {
				problems.add(intersect);
				problems.add(upset);
				problems.add(downset);
			} else if (token.equals("convex")) {
				problems.add(intersect);
				problems.add(union_upset);
				problems.add(union_downset);
			} else if (token.equals("nonempty"))
				nonempty = true;
			else if (!token.isEmpty())
				throw new IllegalArgumentException("invalid option");
		}

		int count = 0;
		while (set.size() != count) {
			count = set.size();
			for (BoolProblem prob : problems) {
				prob.verbose = false;

				Tensor<Boolean> tensor = prob.solveAll(solver).get(0);
				for (Tensor<Boolean> t : Tensor.unstack(tensor)) {
					assert !Relation.wrap(t).isMemberOf(set);
					set.add(Relation.wrap(t));
				}
			}
		}

		if (nonempty)
			set.remove(Relation.wrap(empty));

		System.out.println(options + (options.isEmpty() ? "" : " ")
				+ "definable subalgs: " + set.size());

		List<Relation<Boolean>> list = new ArrayList<Relation<Boolean>>(set);

		if (print)
			for (int i = 0; i < list.size(); i++)
				System.out.println(Util.formatIndex(i) + ": "
						+ Relation.formatMembers(list.get(i)));

		return list;
	}

	public void printHomomorphismExt(final Relation<Boolean> target,
			String pairs) {
		assert target.getArity() == 2;

		final List<Integer> p = new ArrayList<Integer>();
		for (String pair : pairs.split(" ")) {
			if (pair.length() == 2) {
				p.add(Util.parseIndex(relation.getSize(), pair.charAt(0)));
				p.add(Util.parseIndex(target.getSize(), pair.charAt(1)));
			} else if (!pair.isEmpty())
				throw new IllegalArgumentException();
		}

		BoolProblem prob = new BoolProblem(new int[] { target.getSize(),
				relation.getSize() }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Function<BOOL> fun = new Function<BOOL>(alg, tensors.get(0));
				Relation<BOOL> trg = Relation.lift(alg, target);
				Relation<BOOL> src = Relation.lift(alg, relation);

				BOOL b = fun.isFunction();
				b = alg.and(b, fun.preserves(src, trg));

				for (int i = 0; i < p.size(); i += 2)
					b = alg.and(b, fun.hasValue(p.get(i + 1), p.get(i)));

				return b;
			}
		};

		prob.verbose = false;
		Tensor<Boolean> tensor = prob.solveAll(solver, MAX_SOLUTIONS).get(0);

		printCount(pairs, "extending homs", tensor.getLastDim());
		for (Tensor<Boolean> t : Tensor.unstack(tensor))
			System.out.println(" " + Function.format(Function.wrap(t)));
	}

	public boolean printOperationExt(int arity, String tuples, String notuples) {
		final List<int[]> ts = new ArrayList<int[]>();
		for (String tuple : tuples.split(" ")) {
			if (tuple.length() == arity + 1) {
				int[] index = new int[arity + 1];
				for (int i = 0; i <= arity; i++)
					index[i] = Util.parseIndex(relation.getSize(),
							tuple.charAt(i));
				ts.add(index);
			} else if (!tuple.isEmpty())
				throw new IllegalArgumentException();
		}
		final List<int[]> nts = new ArrayList<int[]>();
		for (String tuple : notuples.split(" ")) {
			if (tuple.length() == arity + 1) {
				int[] index = new int[arity + 1];
				for (int i = 0; i <= arity; i++)
					index[i] = Util.parseIndex(relation.getSize(),
							tuple.charAt(i));
				nts.add(index);
			} else if (!tuple.isEmpty())
				throw new IllegalArgumentException();
		}

		BoolProblem prob = new BoolProblem(Util.createShape(relation.getSize(),
				arity + 1)) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> rel = Relation.lift(alg, relation);
				Operation<BOOL> op = new Operation<BOOL>(alg, tensors.get(0));

				BOOL b = op.isOperation();
				b = alg.and(b, op.preserves(rel));

				// b = alg.and(b, op.isIdempotent());
				for (int[] tuple : ts)
					b = alg.and(b, op.hasValue(tuple));
				for (int[] tuple : nts)
					b = alg.and(b, alg.not(op.hasValue(tuple)));

				return b;
			}
		};

		prob.verbose = false;
		Tensor<Boolean> tensor = prob.solveAll(solver, MAX_SOLUTIONS).get(0);

		printCount(tuples, "extending ops", tensor.getLastDim());
		for (Tensor<Boolean> a : Tensor.unstack(tensor))
			System.out.println(" " + Operation.formatTable(Operation.wrap(a)));

		return tensor.getLastDim() > 0;
	}

	public static List<Relation<Boolean>> findDigraphs(SatSolver<?> solver,
			final int size, final String what) {
		List<Tensor<Boolean>> masks = new ArrayList<Tensor<Boolean>>();
		masks.add(Relation.full(size, 2).getTensor());

		for (String token : what.split(" ")) {
			if (token.equals("non-core"))
				masks.add(Relation.empty(size, 2).getTensor());
			else if (token.equals("two-semilattice"))
				masks.add(Relation.empty(size, 3).getTensor());
			else if (token.equals("maltsev"))
				masks.add(Relation.empty(size, 4).getTensor());
			else if (token.equals("majority"))
				masks.add(Relation.empty(size, 4).getTensor());
			else if (token.equals("sd-meet")) {
				masks.add(Relation.empty(size, 4).getTensor());
				masks.add(Relation.empty(size, 4).getTensor());
			} else if (token.equals("taylor")) {
				masks.add(Relation.empty(size, 4).getTensor());
				masks.add(Relation.empty(size, 4).getTensor());
			}
		}

		BoolProblem problem = new BoolProblem(masks) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> rel = new Relation<BOOL>(alg, tensors.get(0));
				int pos = 1;

				BOOL b = alg.TRUE;
				for (String token : what.split(" ")) {
					if (token.equals("reflexive"))
						b = alg.and(b, rel.isReflexive());
					else if (token.equals("non-reflexive"))
						b = alg.and(b, alg.not(rel.isReflexive()));
					else if (token.equals("antireflexive"))
						b = alg.and(b, rel.isAntiReflexive());
					else if (token.equals("non-antireflexive"))
						b = alg.and(b, alg.not(rel.isAntiReflexive()));
					else if (token.equals("symmetric"))
						b = alg.and(b, rel.isSymmetric());
					else if (token.equals("non-symmetric"))
						b = alg.and(b, alg.not(rel.isSymmetric()));
					else if (token.equals("antisymmetric"))
						b = alg.and(b, rel.isAntiSymmetric());
					else if (token.equals("non-antisymmetric"))
						b = alg.and(b, alg.not(rel.isAntiSymmetric()));
					else if (token.equals("transitive"))
						b = alg.and(b, rel.isTransitive());
					else if (token.equals("non-transitive"))
						b = alg.and(b, alg.not(rel.isTransitive()));
					else if (token.equals("trichotome"))
						b = alg.and(b, rel.isTrichotome());
					else if (token.equals("non-trichotome"))
						b = alg.and(b, alg.not(rel.isTrichotome()));
					else if (token.equals("non-isomorphic")) {
						List<Permutation<Boolean>> perms = Permutation
								.symmetricGroup(size);
						for (Permutation<Boolean> p : perms) {
							Permutation<BOOL> perm = Permutation.lift(alg, p);
							b = alg.and(b, rel.isLexLeq(rel.conjugate(perm)));
						}
					} else if (token.equals("non-isomorphic2")) {
						List<Permutation<Boolean>> perms = Permutation
								.transpositions(size);
						for (Permutation<Boolean> p : perms) {
							Permutation<BOOL> perm = Permutation.lift(alg, p);
							b = alg.and(b, rel.isLexLeq(rel.conjugate(perm)));
						}
					} else if (token.equals("non-isomorphic3")) {
						List<Permutation<Boolean>> perms = Permutation
								.threeCycles(size);
						for (Permutation<Boolean> p : perms) {
							Permutation<BOOL> perm = Permutation.lift(alg, p);
							b = alg.and(b, rel.isLexLeq(rel.conjugate(perm)));
						}
					} else if (token.equals("non-core")) {
						Operation<BOOL> op = new Operation<BOOL>(alg,
								tensors.get(pos++));
						b = alg.and(b, op.isOperation());
						b = alg.and(b, op.preserves(rel));
						b = alg.and(b, op.compose(op).isEqualTo(op));
						b = alg.and(b, alg.not(op.isIdempotent()));
					} else if (token.equals("two-semilattice")) {
						Operation<BOOL> op = new Operation<BOOL>(alg,
								tensors.get(pos++));
						b = alg.and(b, op.isOperation());
						b = alg.and(b, op.preserves(rel));
						b = alg.and(b, op.isTwoSemilattice());
					} else if (token.equals("maltsev")) {
						Operation<BOOL> op = new Operation<BOOL>(alg,
								tensors.get(pos++));
						b = alg.and(b, op.isOperation());
						b = alg.and(b, op.preserves(rel));
						b = alg.and(b, op.isMaltsev());
					} else if (token.equals("majority")) {
						Operation<BOOL> op = new Operation<BOOL>(alg,
								tensors.get(pos++));
						b = alg.and(b, op.isOperation());
						b = alg.and(b, op.preserves(rel));
						b = alg.and(b, op.isMajority());
					} else if (token.equals("sd-meet")) {
						Operation<BOOL> op1 = new Operation<BOOL>(alg,
								tensors.get(pos++));
						Operation<BOOL> op2 = new Operation<BOOL>(alg,
								tensors.get(pos++));
						b = alg.and(b, op1.isOperation());
						b = alg.and(b, op1.preserves(rel));
						b = alg.and(b, op2.isOperation());
						b = alg.and(b, op2.preserves(rel));
						b = alg.and(b, Operation.areJovanovicTerms(op1, op2));
					} else if (token.equals("taylor")) {
						Operation<BOOL> op1 = new Operation<BOOL>(alg,
								tensors.get(pos++));
						Operation<BOOL> op2 = new Operation<BOOL>(alg,
								tensors.get(pos++));
						b = alg.and(b, op1.isOperation());
						b = alg.and(b, op1.preserves(rel));
						b = alg.and(b, op2.isOperation());
						b = alg.and(b, op2.preserves(rel));
						b = alg.and(b, Operation.areSiggersTerms(op1, op2));
					} else if (token.length() > 0)
						throw new IllegalArgumentException("unknown option: "
								+ token);
				}

				return b;
			}
		};

		List<Relation<Boolean>> found = new ArrayList<Relation<Boolean>>();
		List<Tensor<Boolean>> solution = Tensor.unstack(problem
				.solveAll(solver).get(0));

		for (Tensor<Boolean> ten : solution) {
			Relation<Boolean> rel = Relation.wrap(ten);
			found.add(rel);
		}

		return found;
	}

	private SatSolver<?> solver = new Sat4J();
	private static DecimalFormat TIME_FORMAT = new DecimalFormat("0.00");

	@SuppressWarnings("unused")
	public static void main2(String[] args) {
		SatSolver<?> solver = new Sat4J();
		int size = 6;
		String options;

		long time = System.currentTimeMillis();

		System.out.println("finding taylors for size " + size);
		List<Relation<Boolean>> list = findDigraphs(solver, size,
				"reflexive antisymmetric transitive taylor non-isomorphic");
		System.out.println("count: " + list.size());

		System.out.println("filtering for no 2sl");
		for (Relation<Boolean> rel : list) {
			CompatibleOps ops = new CompatibleOps(Structure.wrap(rel), solver);
			if (!ops.hasBinaryOp("two-semilat"))
				System.out.println("no 2sl: " + Relation.formatMembers(rel));
		}

		time = System.currentTimeMillis() - time;
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time)
				+ " seconds.");

		// 377543 two-semilat
		// 377559 sd-meet
		// 55853 majority
		// 377559 taylor
	}

	@SuppressWarnings("unused")
	public static void main3(String[] args) {
		PartialOrder<Boolean> a1 = PartialOrder.antiChain(1);
		PartialOrder<Boolean> a2 = PartialOrder.antiChain(2);
		PartialOrder<Boolean> c4 = PartialOrder.crown(4);
		PartialOrder<Boolean> c6 = PartialOrder.crown(6);

		Structure<Boolean> str = Structure.wrap(c4.plus(a1).asRelation());
		Structure.print(str);

		// CompatibleOps ops = new CompatibleOps(str);
		// ops.printUnaryOps();
		// ops.printBinaryOps();
		// ops.printTernaryOps();

		DigraphPoly poly = new DigraphPoly(str.getRelation(0));
		poly.printOperationExt(7,
				"00100001 11001101 22232301 33322301 44442301 "
						+ "00100000 10101111 20102322 30102333 40102344 "
						+ "01010000 11011111 21012322 31012333 41012344 "
						+ "02230000 12231111 22232322 32232333 42232344 "
						+ "03320000 13321111 23322322 33322333 43322344 "
						+ "04440000 14441111 24442322 34442333 44442344 ", "");
	}

	public static void main(String[] args) {
		String[] benoit = new String[] {
				"00 03 04 11 12 15 22 23 24 25 31 32 33 35 40 42 43 44 50 51 52 54 55",
				"00 03 04 11 12 14 15 22 23 24 25 31 32 33 35 40 42 43 44 50 51 52 54 55",
				"00 03 04 11 14 15 22 23 24 25 31 32 33 35 40 42 43 44 50 51 52 54 55",
				"00 03 04 11 12 22 23 24 25 31 32 33 35 40 42 43 44 50 51 52 54 55",
				"00 03 04 11 12 14 22 23 24 25 31 32 33 35 40 42 43 44 50 51 52 54 55",
				"00 04 05 11 13 15 22 23 24 25 31 32 33 35 41 42 43 44 50 52 54 55",
				"00 02 03 05 11 13 14 15 22 23 24 31 32 33 34 40 41 44 45 50 51 52 53 55",
				"00 02 03 05 11 12 13 14 15 22 23 24 31 32 33 34 40 41 44 45 50 51 52 53 55",
				"00 03 05 11 12 13 14 15 22 23 24 31 32 33 34 40 41 44 45 50 51 52 53 55",
				"00 03 05 11 12 13 14 15 22 23 24 31 32 33 34 40 41 44 45 50 51 53 55",
				"00 05 11 13 14 22 23 24 25 31 32 33 34 40 42 44 45 50 51 52 53 55",
				"00 01 02 03 05 11 13 14 22 23 24 25 31 32 33 34 40 42 44 45 50 51 52 53 55",
				"00 11 13 14 22 23 24 25 31 32 33 34 40 42 44 45 50 51 52 53 55",
				"00 03 04 11 13 15 22 23 24 25 32 33 34 41 42 44 45 51 52 53 55",
				"00 02 03 11 12 15 22 23 24 31 33 34 35 40 42 43 44 45 50 51 52 54 55",
				"00 02 03 11 12 14 22 23 25 31 33 34 35 41 42 44 45 50 52 53 54 55" };

		for (int i = 0; i < 1; i++) {
			System.out.println("digraph #" + i);
			Relation<Boolean> rel = Relation.parseMembers(6, 2, benoit[i]);

			Structure<Boolean> str = Structure.wrap(rel);
			Structure.print(str);

			CompatibleOps ops = new CompatibleOps(str);
			ops.printUnaryOps();
			ops.printBinaryOps();
			ops.printTernaryOps();
			ops.printSpecialOps();
		}
	}
}
