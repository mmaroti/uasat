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

package org.uasat.math;

import java.text.*;
import java.util.*;

import org.uasat.core.*;

public class Validation {
	boolean failed = false;
	SatSolver<?> solver = SatSolver.getDefault();

	void verify(String msg, int count, int expected) {
		System.out.println(msg + " is " + count + ".");
		if (count != expected) {
			System.out
					.println("FAILED, the correct value is " + expected + ".");
			failed = true;
		}
	}

	void checkEquivalences() {
		SatProblem problem = new SatProblem(new int[] { 7, 7 }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> rel = new Relation<BOOL>(alg, tensors.get(0));
				return rel.isEquivalence();
			}
		};

		int count = problem.solveAll(solver).get(0).getLastDim();
		verify("A000110 the number of equivalences on a 7-element set", count,
				877);
	}

	void checkPartialOrders() {
		SatProblem problem = new SatProblem(new int[] { 5, 5 }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> rel = new Relation<BOOL>(alg, tensors.get(0));
				return rel.isPartialOrder();
			}
		};

		int count = problem.solveAll(solver).get(0).getLastDim();
		verify("A001035 the number of partial orders on a 5-element set",
				count, 4231);
	}

	void checkLinearExtensions() {
		PartialOrder<Boolean> c2 = PartialOrder.chain(2);
		PartialOrder<Boolean> c3 = PartialOrder.chain(3);
		final PartialOrder<Boolean> ord = c3.product(c2).product(c2);

		SatProblem problem = new SatProblem(new int[] { ord.getSize(),
				ord.getSize() }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> rel = new Relation<BOOL>(alg, tensors.get(0));
				Relation<BOOL> ord2 = Relation.lift(alg, ord.asRelation());
				return alg.and(ord2.isSubsetOf(rel), rel.isTotalOrder());
			}
		};

		int count = problem.solveAll(solver).get(0).getLastDim();
		verify("A114714 the number of linear extensions of 2x2x3", count, 2452);
	}

	void checkAlternations() {
		SatProblem problem = new SatProblem(new int[] { 7, 7 }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Permutation<BOOL> perm = new Permutation<BOOL>(alg,
						tensors.get(0));
				return alg.and(perm.isPermutation(), perm.isEven());
			}
		};

		int count = problem.solveAll(solver).get(0).getLastDim();
		verify("A001710 the number of even permutations on a 7-element set",
				count, 2520);
	}

	void checkAntiChains() {
		final PartialOrder<Boolean> ord = PartialOrder.powerset(4);

		SatProblem problem = new SatProblem(new int[] { ord.getSize() }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> rel = new Relation<BOOL>(alg, tensors.get(0));
				PartialOrder<BOOL> ord2 = PartialOrder.lift(alg, ord);
				return ord2.isAntiChain(rel);
			}
		};

		int count = problem.solveAll(solver).get(0).getLastDim();
		verify("A000372 the number of antichains of 2^4", count, 168);
	}

	void checkNonIsomorphicDigraphs() {
		final List<Permutation<Boolean>> perms = Permutation.nontrivialPerms(4);

		SatProblem problem = new SatProblem(new int[] { 4, 4 }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> rel = new Relation<BOOL>(alg, tensors.get(0));
				BOOL b = rel.isAntiReflexive();
				for (Permutation<Boolean> p : perms) {
					Permutation<BOOL> perm = Permutation.lift(alg, p);
					b = alg.and(b, rel.isLexLeq(rel.conjugate(perm)));
				}
				return b;
			}
		};

		int count = problem.solveAll(solver).get(0).getLastDim();
		verify("A000273 the number of non-isomorphic 4-element digraphs",
				count, 218);
	}

	void checkNonIsomorphicGroupoids() {
		final List<Permutation<Boolean>> perms = Permutation.nontrivialPerms(3);

		SatProblem problem = new SatProblem(new int[] { 3, 3, 3 }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Operation<BOOL> op = new Operation<BOOL>(alg, tensors.get(0));
				BOOL b = op.isOperation();
				for (Permutation<Boolean> p : perms) {
					Permutation<BOOL> perm = Permutation.lift(alg, p);
					b = alg.and(b, op.isLexLeq(op.conjugate(perm)));
				}
				return b;
			}
		};

		int count = problem.solveAll(solver).get(0).getLastDim();
		verify("A001329 the number of non-isomorphic 3-element groupoids",
				count, 3330);
	}

	void checkThreeColorableGraphs() {
		final Relation<Boolean> cycle = Relation.parse(3, 2, "01 12 20")
				.symmetricClosure();

		SatProblem problem = new SatProblem(Tensor.constant(new int[] { 5, 5 },
				Boolean.TRUE), Tensor.constant(new int[] { 3, 5 },
				Boolean.FALSE)) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> cyc = Relation.lift(alg, cycle);
				Relation<BOOL> rel = new Relation<BOOL>(alg, tensors.get(0));
				Function<BOOL> fun = new Function<BOOL>(alg, tensors.get(1));

				BOOL b = rel.isAntiReflexive();
				b = alg.and(b, rel.isSymmetric());
				b = alg.and(b, fun.isFunction());
				b = alg.and(b, fun.preserves(rel, cyc));

				return b;
			}
		};

		int count = problem.solveAll(solver).get(0).getLastDim();
		verify("A084279 the number of 3-colorable 5-element simple graphs",
				count, 958);
	}

	void checkPartialInjections() {
		SatProblem problem = new SatProblem(new int[] { 5, 5 }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Function<BOOL> fun = new Function<BOOL>(alg, tensors.get(0));
				return alg.and(fun.isPartialFunction(), fun.isInjective());
			}
		};

		int count = problem.solveAll(solver).get(0).getLastDim();
		verify("A002720 the number of partial permutations on 5", count, 1546);
	}

	void checkCommutativeSemigroups() {
		SatProblem problem = new SatProblem(new int[] { 4, 4, 4 }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Operation<BOOL> op = new Operation<BOOL>(alg, tensors.get(0));
				BOOL b = op.isOperation();
				b = alg.and(b, op.isAssociative());
				b = alg.and(b, op.isCommutative());
				return b;
			}
		};

		int count = problem.solveAll(solver).get(0).getLastDim();
		verify("A023815 the number of commutative semigroups on 4", count, 1140);
	}

	void checkLatinSquares() {
		SatProblem problem = new SatProblem(new int[] { 4, 4, 4 }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> rel = new Relation<BOOL>(alg, tensors.get(0));
				BOOL b = rel.isOperation();
				b = alg.and(b, rel.rotate(1).isOperation());
				b = alg.and(b, rel.rotate(2).isOperation());
				return b;
			}
		};

		int count = problem.solveAll(solver).get(0).getLastDim();
		verify("A002860 the number of labelled quasigroups of order 4", count,
				576);
	}

	void checkFiniteGroups() {
		final List<Permutation<Boolean>> perms = Permutation.nontrivialPerms(4);

		SatProblem problem = new SatProblem(new int[] { 4, 4, 4 }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> rel = new Relation<BOOL>(alg, tensors.get(0));
				BOOL b = rel.isOperation();
				b = alg.and(b, rel.rotate(1).isOperation());
				b = alg.and(b, rel.rotate(2).isOperation());

				Operation<BOOL> op = rel.asOperation();
				b = alg.and(b, op.isAssociative());

				for (Permutation<Boolean> p : perms) {
					Permutation<BOOL> perm = Permutation.lift(alg, p);
					b = alg.and(b, op.isLexLeq(op.conjugate(perm)));
				}

				return b;
			}
		};

		int count = problem.solveAll(solver).get(0).getLastDim();
		verify("A000001 the number of groups of order 4", count, 2);
	}

	void checkSubspaces() {
		Algebra<Boolean> z3 = Algebra.wrap(
				Operation.parse(3, 2, "012 120 201"),
				Operation.parse(3, 0, "0"));

		CompatibleRels subs = new CompatibleRels(z3);

		int count = subs.findAllRels(4, -1).size();
		verify("A006117 the number of subspaces of Z_3^4", count, 212);
	}

	private static DecimalFormat TIME_FORMAT = new DecimalFormat("0.00");

	void validate() {
		failed = false;
		long time = System.currentTimeMillis();
		System.out.println("Validating UASAT:");

		checkFiniteGroups();
		checkEquivalences();
		checkNonIsomorphicDigraphs();
		checkAntiChains();
		checkPartialOrders();
		checkNonIsomorphicGroupoids();
		checkAlternations();
		checkPartialInjections();
		checkLatinSquares();
		checkSubspaces();
		checkCommutativeSemigroups();
		checkThreeColorableGraphs();
		checkLinearExtensions();

		time = System.currentTimeMillis() - time;
		System.out.println("Total variables: " + solver.getTotalVariables()
				+ ", clauses: " + solver.getTotalClauses() + ".");

		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time)
				+ " seconds.");

		if (failed)
			System.out.println("*** SOME TESTS HAVE FAILED ***");
	}

	public static void main(String[] args) {
		new Validation().validate();
	}
}
