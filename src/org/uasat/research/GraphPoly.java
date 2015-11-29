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

	public GraphPoly(SatSolver<?> solver, Relation<Boolean> relation) {
		this.solver = solver;
		this.relation = relation;
	}

	private static List<Operation<Boolean>> wrapOperations(
			Tensor<Boolean> tensor) {
		List<Operation<Boolean>> result = new ArrayList<Operation<Boolean>>();
		for (Tensor<Boolean> t : Tensor.unstack(tensor))
			result.add(Operation.wrap(t));

		return result;
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

	public void printBinaryOps() {
		int size = relation.getSize();
		BoolProblem prob = new BoolProblem(new int[] { size, size, size }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {

				Operation<BOOL> op = new Operation<BOOL>(alg, tensors.get(0));
				Relation<BOOL> rel = Relation.lift(alg, relation);

				BOOL res = op.isOperation();
				res = alg.and(res, op.preserves(rel));

				return res;
			}
		};

		List<Operation<Boolean>> ops = wrapOperations(prob.solveAll(solver)
				.get(0));
		System.out.println("binary ops: " + ops.size());
	}

	public void printBinaryIdempotentOps() {
		int size = relation.getSize();
		BoolProblem prob = new BoolProblem(new int[] { size, size, size }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {

				Operation<BOOL> op = new Operation<BOOL>(alg, tensors.get(0));
				Relation<BOOL> rel = Relation.lift(alg, relation);

				BOOL res = op.isOperation();
				// res = alg.and(res, op.isEssential());
				res = alg.and(res, op.isIdempotent());
				res = alg.and(res, op.preserves(rel));

				return res;
			}
		};

		List<Operation<Boolean>> ops = wrapOperations(prob.solveAll(solver)
				.get(0));
		System.out.println("binary idempotent ops: " + ops.size());
	}

	public static void main(String[] args) {
		Relation<Boolean> rel = Relation.parseMembers(5, 2, "02 03 12 13 24 34");
		rel = Relation.transitiveClosure(rel.reflexiveClosure());
		assert rel.isPartialOrder();

		GraphPoly poly = new GraphPoly(new Sat4J(), rel);
		poly.printMembers();
		poly.printBinaryIdempotentOps();
	}
}
