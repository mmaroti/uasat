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

import java.text.*;
import java.util.*;

import org.uasat.core.*;
import org.uasat.math.*;

public class PermPrime {
	public final static Relation<Boolean> poset = PartialOrder.antiChain(1).plus(PartialOrder.antiChain(2))
			.plus(PartialOrder.antiChain(2)).plus(PartialOrder.antiChain(1)).asRelation();

	public static void main0(String[] args) {
		Relation.print(poset);
		int size = poset.getSize();

		SatProblem problem = new SatProblem(new int[] { size, size, size }, new int[] { size, size, size }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg, List<Tensor<BOOL>> tensors) {
				Operation<BOOL> meet1 = new Operation<BOOL>(alg, tensors.get(0));
				Operation<BOOL> join1 = new Operation<BOOL>(alg, tensors.get(1));

				Relation<BOOL> rel = Relation.lift(alg, poset);

				BOOL b = meet1.isOperation();
				b = alg.and(b, join1.isOperation());

				b = alg.and(b, meet1.preserves(rel));
				// b = alg.and(b, join1.preserves(rel));

				b = alg.and(b, meet1.isSemilattice());
				b = alg.and(b, join1.isSemilattice());
				b = alg.and(b, meet1.getOrderFromMeet().isEqualTo(join1.getOrderFromJoin()));

				return b;
			}
		};

		SatSolver<?> solver = SatSolver.getDefault();
		List<Tensor<Boolean>> sol = problem.solveOne(solver);
		if (sol != null) {
			Operation<Boolean> meet1 = Operation.wrap(sol.get(0));
			Operation<Boolean> join1 = Operation.wrap(sol.get(1));
			Operation.print(meet1);
			Operation.print(join1);
		} else {
			System.out.println("null");
		}
	}

	public static <BOOL> BOOL preserves_lat(Operation<BOOL> op1, Operation<BOOL> op2, Relation<BOOL> rel) {
		Contract<BOOL> c = Contract.logical(rel.getAlg());
		c.add(op1.getTensor(), "abc");
		c.add(op2.getTensor(), "def");
		c.add(rel.getTensor(), "be");
		c.add(rel.getTensor(), "cf");
		Relation<BOOL> tmp = new Relation<BOOL>(rel.getAlg(), c.get("ad"));
		return tmp.isSubsetOf(rel);
	}

	public static <BOOL> BOOL presunary_lat(Operation<BOOL> op1, Operation<BOOL> op2, Relation<BOOL> rel) {
		Contract<BOOL> c = Contract.logical(rel.getAlg());
		c.add(op1.getTensor(), "abc");
		c.add(op2.getTensor(), "dec");
		c.add(rel.getTensor(), "be");
		Relation<BOOL> tmp = new Relation<BOOL>(rel.getAlg(), c.get("ad"));
		return tmp.isSubsetOf(rel);
	}

	public static <BOOL> BOOL presedge1_lat(Operation<BOOL> op1, Relation<BOOL> rel) {
		Contract<BOOL> c = Contract.logical(rel.getAlg());
		c.add(op1.getTensor(), "abc");
		c.add(rel.getTensor(), "bc");
		Relation<BOOL> tmp = new Relation<BOOL>(rel.getAlg(), c.get("ac"));
		return tmp.isSubsetOf(rel);
	}

	public static <BOOL> BOOL presedge2_lat(Operation<BOOL> op2, Relation<BOOL> rel) {
		Contract<BOOL> c = Contract.logical(rel.getAlg());
		c.add(op2.getTensor(), "abc");
		c.add(rel.getTensor(), "bc");
		Relation<BOOL> tmp = new Relation<BOOL>(rel.getAlg(), c.get("ba"));
		return tmp.isSubsetOf(rel);
	}

	public static <BOOL> BOOL ordered_lat(Operation<BOOL> op1, Operation<BOOL> op2, Relation<BOOL> rel) {
		Contract<BOOL> c = Contract.logical(rel.getAlg());
		c.add(op1.getTensor(), "xab");
		c.add(op2.getTensor(), "yab");
		Relation<BOOL> tmp = new Relation<BOOL>(rel.getAlg(), c.get("xy"));
		return tmp.isSubsetOf(rel);
	}

	public static <BOOL> BOOL preserves_maj(Operation<BOOL> op1, Operation<BOOL> op2, Relation<BOOL> rel) {
		Contract<BOOL> c = Contract.logical(rel.getAlg());
		c.add(op1.getTensor(), "abcd");
		c.add(op2.getTensor(), "efgh");
		c.add(rel.getTensor(), "bf");
		c.add(rel.getTensor(), "cg");
		c.add(rel.getTensor(), "dh");
		Relation<BOOL> tmp = new Relation<BOOL>(rel.getAlg(), c.get("ae"));
		return tmp.isSubsetOf(rel);
	}

	public static void main(String[] args) {
		Relation.print(poset);
		int size = poset.getSize();

		SatProblem problem = new SatProblem(new int[] { size, size, size }, new int[] { size, size, size },
				new int[] { size, size, size }, new int[] { size, size, size }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg, List<Tensor<BOOL>> tensors) {
				Operation<BOOL> meet1 = new Operation<BOOL>(alg, tensors.get(0));
				Operation<BOOL> join1 = new Operation<BOOL>(alg, tensors.get(1));
				Operation<BOOL> meet2 = new Operation<BOOL>(alg, tensors.get(2));
				Operation<BOOL> join2 = new Operation<BOOL>(alg, tensors.get(3));

				Relation<BOOL> rel = Relation.lift(alg, poset);

				BOOL b = meet1.isOperation();
				b = alg.and(b, join1.isOperation());
				b = alg.and(b, meet2.isOperation());
				b = alg.and(b, join2.isOperation());

				b = alg.and(b, presunary_lat(meet1, meet2, rel));
				b = alg.and(b, presunary_lat(join1, join2, rel));
				// b = alg.and(b, presedge1_lat(join1, rel));
				// b = alg.and(b, presedge2_lat(join2, rel));
				// b = alg.and(b, ordered_lat(join1, join2, rel));
				// b = alg.and(b, alg.not(join1.isEqualTo(join2)));

				b = alg.and(b, meet1.isSemilattice());
				b = alg.and(b, join1.isSemilattice());
				b = alg.and(b, meet1.getOrderFromMeet().isEqualTo(join1.getOrderFromJoin()));

				b = alg.and(b, meet2.isSemilattice());
				b = alg.and(b, join2.isSemilattice());
				b = alg.and(b, meet2.getOrderFromMeet().isEqualTo(join2.getOrderFromJoin()));

				return b;
			}
		};

		SatSolver<?> solver = SatSolver.getDefault();
		List<Tensor<Boolean>> sol = problem.solveOne(solver);
		if (sol != null) {
			Operation<Boolean> meet1 = Operation.wrap(sol.get(0));
			Operation<Boolean> join1 = Operation.wrap(sol.get(1));
			Operation<Boolean> meet2 = Operation.wrap(sol.get(2));
			Operation<Boolean> join2 = Operation.wrap(sol.get(3));
			Operation.print(meet1);
			Operation.print(join1);
			Operation.print(meet2);
			Operation.print(join2);
			Relation.print(meet1.getOrderFromMeet());
			// Relation.print(join1.getOrderFromJoin());
			Relation.print(meet2.getOrderFromMeet());
			// Relation.print(join2.getOrderFromJoin());
		} else {
			System.out.println("null");
		}
	}

	public static void main2(String[] args) {
		Relation.print(poset);
		int size = poset.getSize();

		SatProblem problem = new SatProblem(new int[] { size, size, size, size },
				new int[] { size, size, size, size }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg, List<Tensor<BOOL>> tensors) {
				Operation<BOOL> maj1 = new Operation<BOOL>(alg, tensors.get(0));
				Operation<BOOL> maj2 = new Operation<BOOL>(alg, tensors.get(1));

				Relation<BOOL> rel = Relation.lift(alg, poset);

				BOOL b = maj1.isOperation();
				b = alg.and(b, maj2.isOperation());

				b = alg.and(b, preserves_maj(maj1, maj2, rel));

				b = alg.and(b, maj1.isMajority());
				b = alg.and(b, maj2.isMajority());

				return b;
			}
		};

		SatSolver<?> solver = SatSolver.getDefault();
		List<Tensor<Boolean>> sol = problem.solveOne(solver);
		if (sol != null) {
			Operation<Boolean> maj1 = Operation.wrap(sol.get(0));
			Operation<Boolean> maj2 = Operation.wrap(sol.get(1));
			Operation.print(maj1);
			Operation.print(maj2);
		} else {
			System.out.println("null");
		}
	}
}
