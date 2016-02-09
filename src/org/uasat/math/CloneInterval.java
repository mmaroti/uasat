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

public class CloneInterval {
	private final SatSolver<?> solver;
	private final int size;
	private final List<Operation<Boolean>> operations;
	private final List<Relation<Boolean>> relations;
	private GaloisConn<Boolean> galois;

	public CloneInterval(int size) {
		this(size, SatSolver.getDefault());
	}

	public CloneInterval(int size, SatSolver<?> solver) {
		assert size >= 1 && solver != null;

		this.size = size;
		this.solver = solver;

		operations = new ArrayList<Operation<Boolean>>();
		relations = new ArrayList<Relation<Boolean>>();
		galois = GaloisConn.wrap(Tensor.constant(new int[] { 0, 0 },
				Boolean.FALSE));
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

	public GaloisConn<Boolean> getGaloisConn() {
		return galois;
	}

	public int getOperationCount() {
		return operations.size();
	}

	public int getRelationCount() {
		return relations.size();
	}

	public int getTotalCount() {
		return operations.size() + relations.size();
	}

	public void clear() {
		relations.clear();
	}

	public void add(final Relation<Boolean> rel) {
		assert rel.getSize() == size;

		final Tensor<Boolean> t1 = galois.getTensor();
		Tensor<Boolean> t2 = Tensor.generate(operations.size(),
				relations.size() + 1, new Func2<Boolean, Integer, Integer>() {
					@Override
					public Boolean call(Integer elem1, Integer elem2) {
						if (elem2 < relations.size())
							return t1.getElem(elem1, elem2);
						else
							return operations.get(elem1).preserves(rel);
					}
				});

		relations.add(rel);
		galois = GaloisConn.wrap(t2);
	}

	public void add(final Operation<Boolean> op) {
		assert op.getSize() == size;

		final Tensor<Boolean> t1 = galois.getTensor();
		Tensor<Boolean> t2 = Tensor.generate(operations.size() + 1,
				relations.size(), new Func2<Boolean, Integer, Integer>() {
					@Override
					public Boolean call(Integer elem1, Integer elem2) {
						if (elem1 < operations.size())
							return t1.getElem(elem1, elem2);
						else
							return op.preserves(relations.get(elem2));
					}
				});

		operations.add(op);
		galois = GaloisConn.wrap(t2);
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

	public boolean addCriticalPair(int opArity, int relArity) {
		assert opArity >= 0 && relArity >= 1;

		SatProblem problem = new SatProblem(new int[] { operations.size() },
				Util.createShape(size, opArity + 1), Util.createShape(size,
						relArity)) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> oset = new Relation<BOOL>(alg, tensors.get(0));
				Operation<BOOL> op = new Operation<BOOL>(alg, tensors.get(1));
				Relation<BOOL> rel = new Relation<BOOL>(alg, tensors.get(2));

				GaloisConn<BOOL> gal = GaloisConn.lift(alg, galois);
				Relation<BOOL> rset = gal.rightClosure(oset);

				BOOL b = op.isOperation();
				b = alg.and(b, alg.not(op.preserves(rel)));
				b = alg.and(b, oset.isSubsetOf(preservedOps(alg, rel)));
				b = alg.and(b, rset.isSubsetOf(preservedRels(alg, op)));
				b = alg.and(b, gal.leftClosure(rset).isEqualTo(oset));

				return b;
			}
		};

		List<Tensor<Boolean>> sol = problem.solveOne(solver);
		if (sol == null)
			return false;

		add(Operation.wrap(sol.get(1)));
		add(Relation.wrap(sol.get(2)));
		return true;
	}

	public boolean addCriticalOp(int opArity, int relArity) {
		assert opArity >= 0 && relArity >= 1;

		SatProblem problem = new SatProblem(
				Util.createShape(size, opArity + 1), Util.createShape(size,
						relArity)) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Operation<BOOL> op = new Operation<BOOL>(alg, tensors.get(0));
				Relation<BOOL> rel = new Relation<BOOL>(alg, tensors.get(1));
				GaloisConn<BOOL> gal = GaloisConn.lift(alg, galois);

				BOOL b = op.isOperation();
				b = alg.and(b, alg.not(op.preserves(rel)));

				Relation<BOOL> rset = preservedRels(alg, op);
				Relation<BOOL> oset = gal.leftClosure(rset);
				b = alg.and(b, oset.isSubsetOf(preservedOps(alg, rel)));

				return b;
			}
		};

		List<Tensor<Boolean>> sol = problem.solveOne(solver);
		if (sol == null)
			return false;

		add(Operation.wrap(sol.get(0)));
		return true;
	}

	public boolean addCriticalRel(int opArity, int relArity) {
		assert opArity >= 0 && relArity >= 1;

		SatProblem problem = new SatProblem(
				Util.createShape(size, opArity + 1), Util.createShape(size,
						relArity)) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Operation<BOOL> op = new Operation<BOOL>(alg, tensors.get(0));
				Relation<BOOL> rel = new Relation<BOOL>(alg, tensors.get(1));
				GaloisConn<BOOL> gal = GaloisConn.lift(alg, galois);

				BOOL b = op.isOperation();
				b = alg.and(b, alg.not(op.preserves(rel)));

				Relation<BOOL> oset = preservedOps(alg, rel);
				Relation<BOOL> rset = gal.rightClosure(oset);
				b = alg.and(b, rset.isSubsetOf(preservedRels(alg, op)));

				return b;
			}
		};

		List<Tensor<Boolean>> sol = problem.solveOne(solver);
		if (sol == null)
			return false;

		add(Relation.wrap(sol.get(1)));
		return true;
	}

	public void addCriticalAll(int opArity, int relArity) {
		for (int i = 1; i <= Math.max(opArity, relArity); i++) {
			if (i <= relArity)
				for (; addCriticalRel(Math.min(opArity, i), i);)
					;

			if (i <= opArity)
				for (; addCriticalOp(i, Math.min(relArity, i));)
					;

			for (; addCriticalPair(Math.min(opArity, i), Math.min(relArity, i));)
				;
		}
	}

	public List<Relation<Boolean>> getClosedOpSets(int limit) {
		return GaloisConn.findLeftClosedSets(solver, galois, limit);
	}

	public List<Relation<Boolean>> getClosedRelSets(int limit) {
		return GaloisConn.findRightClosedSets(solver, galois, limit);
	}

	public void print() {
		System.out.println("clone interval on universe " + size);

		System.out.println("operations: " + operations.size());
		int c = 0;
		for (Operation<Boolean> op : operations)
			System.out.println((c++) + ":\t" + Operation.formatTable(op));

		System.out.println("relations: " + relations.size());
		c = 0;
		for (Relation<Boolean> rel : relations)
			System.out.println((c++) + ":\t" + Relation.formatMembers(rel));

		GaloisConn.print(galois);
	}

	public void printClosedOpSets(int limit) {
		List<Relation<Boolean>> sets = getClosedOpSets(limit);
		System.out.println("closed sets of ops: "
				+ (sets.size() == limit ? ">=" : "") + sets.size());

		for (int i = 0; i < sets.size(); i++)
			System.out.println(i + ":\t" + Relation.formatMembers(sets.get(i)));
	}

	public void printClosedRelSets(int limit) {
		List<Relation<Boolean>> sets = getClosedRelSets(limit);
		System.out.println("closed sets of rels: "
				+ (sets.size() == limit ? ">=" : "") + sets.size());

		for (int i = 0; i < sets.size(); i++)
			System.out.println(i + ":\t" + Relation.formatMembers(sets.get(i)));
	}

	public static void main2(String[] args) {
		CloneInterval clone = new CloneInterval(2);
		clone.add(Relation.parseMembers(2, 1, ""));
		clone.add(Relation.parseMembers(2, 1, "0"));
		clone.add(Relation.parseMembers(2, 1, "1"));
		clone.add(Relation.parseMembers(2, 1, "0 1"));
		clone.add(Operation.parseTable(2, 1, "00"));
		clone.add(Operation.parseTable(2, 1, "01"));
		clone.add(Operation.parseTable(2, 1, "10"));
		clone.add(Operation.parseTable(2, 1, "11"));
		clone.print();
		clone.printClosedRelSets(-1);
	}

	public static void main(String[] args) {
		CloneInterval clone = new CloneInterval(2);
		clone.addCriticalAll(3, 4);
		clone.print();
		clone.printClosedOpSets(-1);
		clone.printClosedRelSets(-1);
	}

	public static void main3(String[] args) {
		CloneInterval clone = new CloneInterval(2);

		for (int i = 0; i < 8; i++) {
			clone.addCriticalPair(2, 2);
			clone.addCriticalOp(2, 2);
			clone.addCriticalRel(2, 2);
			clone.print();
			clone.printClosedOpSets(-1);
			clone.printClosedRelSets(-1);
			System.out.println();
		}
	}
}
