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

import java.util.*;

import org.uasat.core.*;

public class ClonePair {
	private final SatSolver<?> solver;
	private final int size;
	private final List<Operation<Boolean>> operations;
	private final List<Relation<Boolean>> relations;

	public ClonePair(int size) {
		this(size, SatSolver.getDefault());
	}

	public ClonePair(int size, SatSolver<?> solver) {
		assert size >= 1 && solver != null;

		this.size = size;
		this.solver = solver;

		operations = new ArrayList<Operation<Boolean>>();
		relations = new ArrayList<Relation<Boolean>>();
	}

	public int getSize() {
		return size;
	}

	public List<Operation<Boolean>> getOperations() {
		return operations;
	}

	public List<Relation<Boolean>> getRelations() {
		return relations;
	}

	public Algebra<Boolean> getAlgebra() {
		return Algebra.wrap(operations);
	}

	public Structure<Boolean> getStructure() {
		return Structure.wrap(relations);
	}

	public int getTotalCount() {
		return operations.size() + relations.size();
	}

	public void clear() {
		relations.clear();
	}

	public void add(final Operation<Boolean> op) {
		assert op.getSize() == size;
		for (Relation<Boolean> rel : relations)
			assert op.preserves(rel);

		operations.add(op);
	}

	public void add(final Relation<Boolean> rel) {
		assert rel.getSize() == size;
		for (Operation<Boolean> op : operations)
			assert op.preserves(rel);

		relations.add(rel);
	}

	public void addSingletons() {
		for (int i = 0; i < size; i++)
			add(Relation.singleton(size, i));
	}

	public <BOOL> Relation<BOOL> preservedOps(final BoolAlgebra<BOOL> alg,
			final Relation<BOOL> rel) {
		Tensor<BOOL> tensor = Tensor.generate(operations.size(),
				new Func1<BOOL, Integer>() {
					@Override
					public BOOL call(Integer elem) {
						Operation<BOOL> op = Operation.lift(alg,
								operations.get(elem));
						return op.preserves(rel);
					}
				});
		return new Relation<BOOL>(alg, tensor);
	}

	public <BOOL> Relation<BOOL> preservedRels(final BoolAlgebra<BOOL> alg,
			final Operation<BOOL> op) {
		Tensor<BOOL> tensor = Tensor.generate(relations.size(),
				new Func1<BOOL, Integer>() {
					@Override
					public BOOL call(Integer elem) {
						Relation<BOOL> rel = Relation.lift(alg,
								relations.get(elem));
						return op.preserves(rel);
					}
				});
		return new Relation<BOOL>(alg, tensor);
	}

	public static class Pair {
		private final Operation<Boolean> op;
		private final Relation<Boolean> rel;

		public Pair(Operation<Boolean> op, Relation<Boolean> rel) {
			this.op = op;
			this.rel = rel;
		}

		public Operation<Boolean> getOperation() {
			return op;
		}

		public Relation<Boolean> getRelation() {
			return rel;
		}
	}

	public Pair findCriticalPair(int opArity, int relArity) {
		assert opArity >= 0 && relArity >= 1;

		SatProblem problem = new SatProblem(
				Util.createShape(size, opArity + 1), Util.createShape(size,
						relArity)) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Operation<BOOL> op = new Operation<BOOL>(alg, tensors.get(0));
				Relation<BOOL> rel = new Relation<BOOL>(alg, tensors.get(1));

				BOOL b = op.isOperation();
				b = alg.and(b, alg.not(op.preserves(rel)));

				for (Operation<Boolean> o : operations)
					b = alg.and(b, Operation.lift(alg, o).preserves(rel));

				for (Relation<Boolean> r : relations)
					b = alg.and(b, op.preserves(Relation.lift(alg, r)));

				return b;
			}
		};

		List<Tensor<Boolean>> sol = problem.solveOne(solver);
		if (sol == null)
			return null;

		return new Pair(Operation.wrap(sol.get(0)), Relation.wrap(sol.get(1)));
	}

	public boolean addCriticalOp(int opArity, int relArity) {
		Pair pair = findCriticalPair(opArity, relArity);
		if (pair == null)
			return false;

		add(pair.getOperation());
		return true;
	}

	public boolean addCriticalRel(int opArity, int relArity) {
		Pair pair = findCriticalPair(opArity, relArity);
		if (pair == null)
			return false;

		add(pair.getRelation());
		return true;
	}

	public void addCriticalOps(int opArity, int relArity) {
		while (addCriticalOp(opArity, relArity))
			;
	}

	public void addCriticalRels(int opArity, int relArity) {
		while (addCriticalRel(opArity, relArity))
			;
	}

	public void print() {
		System.out.println("clone pair on universe " + size);

		System.out.println("operations: " + operations.size());
		int c = 0;
		for (Operation<Boolean> op : operations)
			System.out.println((c++) + ":\t" + Operation.formatTable(op));

		System.out.println("relations: " + relations.size());
		c = 0;
		for (Relation<Boolean> rel : relations)
			System.out.println((c++) + ":\t" + Relation.formatMembers(rel));
	}
}
