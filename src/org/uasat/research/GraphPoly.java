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

public class GraphPoly {
	private SatSolver<?> solver;
	private Relation<Boolean> relation;
	private int MAX_SOLUTIONS = 10;

	public GraphPoly(SatSolver<?> solver, Relation<Boolean> relation) {
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

				String[] tokens = options.split(" ");
				for (String token : tokens) {
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
					else
						throw new IllegalArgumentException("invalid option");
				}

				return res;
			}
		};

		prob.verbose = false;
		Tensor<Boolean> tensor = prob.solveAll(solver, MAX_SOLUTIONS).get(0);

		printCount(options, "binary ops", tensor.getLastDim());

		if (1 <= tensor.getLastDim()) {
			Tensor<Boolean> first = Tensor.unstack(tensor).get(0);
			System.out.println("  " + Operation.format(Operation.wrap(first)));
		}

		return 1 <= tensor.getLastDim();
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

				String[] tokens = options.split(" ");
				for (String token : tokens) {
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
					else
						throw new IllegalArgumentException("invalid option");
				}

				return res;
			}
		};

		prob.verbose = false;
		Tensor<Boolean> tensor = prob.solveAll(solver, MAX_SOLUTIONS).get(0);

		printCount(options, "ternary ops", tensor.getLastDim());

		if (1 <= tensor.getLastDim()) {
			Tensor<Boolean> first = Tensor.unstack(tensor).get(0);
			System.out.println("  " + Operation.format(Operation.wrap(first)));
		}

		return 1 <= tensor.getLastDim();
	}

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		PartialOrder<Boolean> p1 = PartialOrder.antiChain(1);
		PartialOrder<Boolean> p2 = PartialOrder.antiChain(2);
		PartialOrder<Boolean> c4 = PartialOrder.crown(4);
		PartialOrder<Boolean> c6 = PartialOrder.crown(6);

		Relation<Boolean> rel = c4.asRelation();

		GraphPoly poly = new GraphPoly(new Sat4J(), rel);
		poly.printMembers();
		poly.printBinaryOps("surjective essential");
		poly.printBinaryOps("idempotent essential");
		poly.printBinaryOps("idempotent commutative");
		poly.printBinaryOps("semilattice");
		poly.printBinaryOps("two-semilat");
		poly.printTernaryOps("surjective essential");
		poly.printTernaryOps("idempotent essential");
		poly.printTernaryOps("majority");
		poly.printTernaryOps("weak-nu");
	}
}
