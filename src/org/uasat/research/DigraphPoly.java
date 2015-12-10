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

package org.uasat.research;

import java.util.*;

import org.uasat.core.*;
import org.uasat.math.*;
import org.uasat.solvers.*;

public class DigraphPoly {
	private SatSolver<?> solver;
	private Relation<Boolean> relation;
	private int MAX_SOLUTIONS = 5;

	public DigraphPoly(SatSolver<?> solver, Relation<Boolean> relation) {
		assert relation.getArity() == 2;

		this.solver = solver;
		this.relation = relation;
	}

	public void printMembers() {
		System.out.println("relation: " + Relation.formatMembers(relation));

		String s = "properties:";
		if (relation.isReflexive())
			s += " reflexive";
		if (relation.isAntiReflexive())
			s += " antireflexive";
		if (relation.isSymmetric())
			s += " symmetric";
		if (relation.isAntiSymmetric())
			s += " antisymmetric";
		if (relation.isTransitive())
			s += " transitive";
		if (relation.isTrichotome())
			s += " trichotome";
		System.out.println(s);

		if (relation.isPartialOrder()) {
			Relation<Boolean> covers = relation.asPartialOrder().covers();
			System.out.println("covers: " + Relation.formatMembers(covers));
		}
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

	public boolean printUnaryOps(final String options) {
		int size = relation.getSize();
		BoolProblem prob = new BoolProblem(new int[] { size, size }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {

				Operation<BOOL> op = new Operation<BOOL>(alg, tensors.get(0));
				Relation<BOOL> rel = Relation.lift(alg, relation);

				BOOL res = op.isOperation();
				res = alg.and(res, op.preserves(rel));

				for (String token : options.split(" ")) {
					if (token.equals("automorphism"))
						res = alg.and(res, op.isSurjective());
					else if (token.equals("endomorphism"))
						res = alg.and(res, alg.not(op.isSurjective()));
					else if (token.equals("decreasing"))
						res = alg.and(res, op.asRelation().isSubsetOf(rel));
					else if (token.equals("increasing"))
						res = alg.and(res,
								op.asRelation().isSubsetOf(rel.rotate()));
					else if (token.equals("retraction"))
						res = alg.and(res, op.compose(op).isEqualTo(op));
					else if (!token.isEmpty())
						throw new IllegalArgumentException("invalid option");
				}

				return res;
			}
		};

		prob.verbose = false;
		Tensor<Boolean> tensor = prob.solveAll(solver, MAX_SOLUTIONS).get(0);

		printCount(options, "unary ops", tensor.getLastDim());
		for (Tensor<Boolean> t : Tensor.unstack(tensor))
			System.out.println(" " + Operation.format(Operation.wrap(t)));

		return 1 <= tensor.getLastDim();
	}

	@SuppressWarnings("unused")
	public void printUnaryOps() {
		boolean automorphism = printUnaryOps("automorphism");
		boolean dec_retraction = printUnaryOps("decreasing retraction endomorphism");
		boolean inc_retraction = printUnaryOps("increasing retraction endomorphism");
		boolean retraction = dec_retraction || inc_retraction
				|| printUnaryOps("retraction endomorphism");
		boolean decreasing = dec_retraction
				|| printUnaryOps("decreasing endomorphism");
		boolean increasing = inc_retraction
				|| printUnaryOps("increasing endomorphism");
	}

	public boolean printBinaryOps(final String options) {
		int size = relation.getSize();
		BoolProblem prob = new BoolProblem(new int[] { size, size, size }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {

				Operation<BOOL> op = new Operation<BOOL>(alg, tensors.get(0));
				Relation<BOOL> rel = Relation.lift(alg, relation);

				BOOL res = op.isOperation();
				res = alg.and(res, op.preserves(rel));

				for (String token : options.split(" ")) {
					if (token.equals("idempotent"))
						res = alg.and(res, op.isIdempotent());
					else if (token.equals("commutative"))
						res = alg.and(res, op.isCommutative());
					else if (token.equals("associative"))
						res = alg.and(res, op.isAssociative());
					else if (token.equals("surjective"))
						res = alg.and(res, op.isSurjective());
					else if (token.equals("essential"))
						res = alg.and(res, op.isEssential());
					else if (token.equals("semilattice"))
						res = alg.and(res, op.isSemilattice());
					else if (token.equals("two-semilat"))
						res = alg.and(res, op.isTwoSemilattice());
					else if (!token.isEmpty())
						throw new IllegalArgumentException("invalid option");
				}

				return res;
			}
		};

		prob.verbose = false;
		Tensor<Boolean> tensor = prob.solveAll(solver, MAX_SOLUTIONS).get(0);

		printCount(options, "binary ops", tensor.getLastDim());
		for (Tensor<Boolean> t : Tensor.unstack(tensor))
			System.out.println(" " + Operation.format(Operation.wrap(t)));

		return 1 <= tensor.getLastDim();
	}

	public void printBinaryOps() {
		boolean semilattice = printBinaryOps("semilattice");
		boolean two_semilat = semilattice || printBinaryOps("two-semilat");
		boolean associative = semilattice
				|| printBinaryOps("associative essential");
		boolean idempotent = two_semilat
				|| printBinaryOps("idempotent essential");
		boolean surjective = idempotent
				|| printBinaryOps("surjective essential");
		@SuppressWarnings("unused")
		boolean essential = associative || idempotent || surjective
				|| printTernaryOps("essential");
	}

	public boolean printTernaryOps(final String options) {
		int size = relation.getSize();
		BoolProblem prob = new BoolProblem(new int[] { size, size, size, size }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {

				Operation<BOOL> op = new Operation<BOOL>(alg, tensors.get(0));
				Relation<BOOL> rel = Relation.lift(alg, relation);

				BOOL res = op.isOperation();
				res = alg.and(res, op.preserves(rel));

				for (String token : options.split(" ")) {
					if (token.equals("idempotent"))
						res = alg.and(res, op.isIdempotent());
					else if (token.equals("essential"))
						res = alg.and(res, op.isEssential());
					else if (token.equals("surjective"))
						res = alg.and(res, op.isSurjective());
					else if (token.equals("majority"))
						res = alg.and(res, op.isMajority());
					else if (token.equals("minority"))
						res = alg.and(res, op.isMinority());
					else if (token.equals("maltsev"))
						res = alg.and(res, op.isMaltsev());
					else if (token.equals("weak-nu"))
						res = alg.and(res, op.isWeakNearUnanimity());
					else if (!token.isEmpty())
						throw new IllegalArgumentException("invalid option");
				}

				return res;
			}
		};

		prob.verbose = false;
		Tensor<Boolean> tensor = prob.solveAll(solver, MAX_SOLUTIONS).get(0);

		printCount(options, "ternary ops", tensor.getLastDim());
		for (Tensor<Boolean> t : Tensor.unstack(tensor))
			System.out.println(" " + Operation.format(Operation.wrap(t)));

		return 1 <= tensor.getLastDim();
	}

	public void printTernaryOps() {
		boolean majority = printTernaryOps("majority");
		boolean minority = printTernaryOps("minority");
		@SuppressWarnings("unused")
		boolean maltsev = minority || printTernaryOps("maltsev");
		boolean weaknu = majority || minority || printTernaryOps("weak-nu");
		boolean idempotent = weaknu || printTernaryOps("idempotent essential");
		boolean surjective = idempotent
				|| printTernaryOps("surjective essential");
		@SuppressWarnings("unused")
		boolean essential = idempotent || surjective
				|| printTernaryOps("essential");
	}

	public List<Relation<Boolean>> printDefinableSubalgs(String options,
			boolean print) {
		int size = relation.getSize();

		final List<Relation<Boolean>> list = new ArrayList<Relation<Boolean>>();
		list.add(Relation.full(size, 1));

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

				BOOL b = alg.not(s0.isMemberOf(list));
				b = alg.and(b, s1.isMemberOf(list));
				b = alg.and(b, s2.isMemberOf(list));
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

				BOOL b = alg.not(s0.isMemberOf(list));
				b = alg.and(b, s1.isMemberOf(list));
				b = alg.and(b, s2.isMemberOf(list));
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

				BOOL b = alg.not(s0.isMemberOf(list));
				b = alg.and(b, s1.isMemberOf(list));
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

				BOOL b = alg.not(s0.isMemberOf(list));
				b = alg.and(b, s1.isMemberOf(list));
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

				BOOL b = alg.not(s0.isMemberOf(list));
				b = alg.and(b, s1.isMemberOf(list));
				b = alg.and(b, s2.isMemberOf(list));
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

				BOOL b = alg.not(s0.isMemberOf(list));
				b = alg.and(b, s1.isMemberOf(list));
				b = alg.and(b, s2.isMemberOf(list));
				b = alg.and(b, s0.isEqualTo(rel.compose(s1.union(s2))));

				return b;
			}
		};

		boolean nonempty = false;
		for (String token : options.split(" ")) {
			if (token.equals("emptyset"))
				list.add(Relation.empty(size, 1));
			else if (token.equals("singletons")) {
				for (int i = 0; i < size; i++)
					list.add(Relation.singleton(size, i));
			} else if (token.equals("primesets")) {
				for (int i = 0; i < size; i++) {
					list.add(Relation.singleton(size, i).compose(relation));
					list.add(relation.compose(Relation.singleton(size, i)));
				}
			} else if (token.equals("prime-upsets")) {
				for (int i = 0; i < size; i++)
					list.add(Relation.singleton(size, i).compose(relation));
			} else if (token.equals("prime-downsets")) {
				for (int i = 0; i < size; i++)
					list.add(relation.compose(Relation.singleton(size, i)));
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
		while (list.size() != count) {
			count = list.size();
			for (BoolProblem prob : problems) {
				prob.verbose = false;

				Tensor<Boolean> tensor = prob.solveAll(solver).get(0);
				for (Tensor<Boolean> t : Tensor.unstack(tensor))
					list.add(Relation.wrap(t));
			}
		}

		if (nonempty)
			list.remove(Relation.wrap(empty));

		Relation.sort(list);

		System.out.println(options + (options.isEmpty() ? "" : " ")
				+ "definable subalgs: " + list.size());

		if (print)
			for (int i = 0; i < list.size(); i++)
				System.out.println(" " + Relation.formatMembers(list.get(i)));

		return list;
	}

	public List<Relation<Boolean>> makeSubsets(String... subsets) {
		List<Relation<Boolean>> subs = new ArrayList<Relation<Boolean>>();
		for (String str : subsets)
			subs.add(Relation.parseMembers(relation.getSize(), 1, str));

		return subs;
	}

	public Relation<Boolean> makeGlobal(final List<Relation<Boolean>> subsets) {
		int[] shape = new int[relation.getArity()];
		Arrays.fill(shape, subsets.size());

		Tensor<Boolean> tensor = Tensor.generate(shape,
				new Func1<Boolean, int[]>() {
					@Override
					public Boolean call(int[] elem) {
						Relation<Boolean> r = subsets.get(elem[0]);
						for (int i = 1; i < elem.length; i++)
							r = r.cartesian(subsets.get(elem[i]));

						r = r.intersect(relation);

						for (int i = 0; i < elem.length; i++)
							if (!subsets.get(elem[i]).isSubsetOf(r.project(i)))
								return false;

						return true;
					}
				});

		return Relation.wrap(tensor);
	}

	public boolean printSurjectiveFuntions(int exp, final Relation<Boolean> rel) {
		assert rel.getArity() == 2;

		final Relation<Boolean> pow = relation.power(exp);
		BoolProblem prob = new BoolProblem(new int[] { rel.getSize(),
				pow.getSize() }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Function<BOOL> fun = new Function<BOOL>(alg, tensors.get(0));
				Relation<BOOL> rel1 = Relation.lift(alg, pow);
				Relation<BOOL> rel2 = Relation.lift(alg, rel);

				BOOL b = fun.preserves(rel1, rel2);
				b = alg.and(b, fun.isFunction());
				return alg.and(b, fun.isSurjective());
			}
		};

		prob.verbose = false;
		Tensor<Boolean> tensor = prob.solveAll(solver, MAX_SOLUTIONS).get(0);

		printCount("surjective", "functions from power " + exp,
				tensor.getLastDim());

		if (1 <= tensor.getLastDim()) {
			Tensor<Boolean> first = Tensor.unstack(tensor).get(0);
			System.out.println("  " + Function.format(Function.wrap(first)));
		}

		return 1 <= tensor.getLastDim();
	}

	public boolean printSpecialOperation3(final int a, final int b,
			final int c, final int d) {
		int size = relation.getSize();
		BoolProblem prob = new BoolProblem(new int[] { size, size, size, size,
				size, size, size }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Operation<BOOL> op = new Operation<BOOL>(alg, tensors.get(0));
				BOOL t = alg.and(op.isOperation(), op.isIdempotent());

				t = alg.and(t, op.hasValue(a, a, a, a, d, d, d));
				t = alg.and(t, op.hasValue(b, b, b, b, d, d, d));
				t = alg.and(t, op.hasValue(c, d, c, c, d, a, b));
				t = alg.and(t, op.hasValue(c, c, d, c, b, d, a));
				t = alg.and(t, op.hasValue(c, c, c, d, a, b, d));

				return t;
			}
		};

		prob.verbose = false;
		boolean solvable = prob.isSolvable(solver);

		System.out.println("6-ary special function: "
				+ (solvable ? ">= 1" : "0"));
		return solvable;
	}

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		PartialOrder<Boolean> a1 = PartialOrder.antiChain(1);
		PartialOrder<Boolean> a2 = PartialOrder.antiChain(2);
		PartialOrder<Boolean> c4 = PartialOrder.crown(4);
		PartialOrder<Boolean> c6 = PartialOrder.crown(6);

		Relation<Boolean> rel1 = c4.plus(a1).asRelation();
		DigraphPoly pol1 = new DigraphPoly(new Sat4J(), rel1);
		pol1.printMembers();
		pol1.printUnaryOps();
		pol1.printBinaryOps();
		pol1.printTernaryOps();
		pol1.printDefinableSubalgs("intersect prime-upsets nonempty", true);
		pol1.printDefinableSubalgs("intersect prime-downsets nonempty", true);
		pol1.printDefinableSubalgs("singletons treedef nonempty", true);
		pol1.printDefinableSubalgs("singletons convex nonempty", true);
		pol1.printDefinableSubalgs("primesets convex nonempty", true);

		// poly.printSpecialOperation3(3, 4, 6, 8);
	}
}
