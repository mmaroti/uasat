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

	private static class Pair {
		final Operation<Boolean> op;
		final Relation<Boolean> rel;

		Pair(Operation<Boolean> op, Relation<Boolean> rel) {
			this.op = op;
			this.rel = rel;
		}
	}

	private Pair getCriticalPair(int opArity, int relArity) {
		assert opArity >= 0 && relArity >= 1;

		SatProblem problem = new SatProblem(new int[] { operations.size() },
				new int[] { relations.size() }, Util.createShape(size,
						opArity + 1), Util.createShape(size, relArity)) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> ops = new Relation<BOOL>(alg, tensors.get(0));
				Relation<BOOL> rels = new Relation<BOOL>(alg, tensors.get(1));
				Operation<BOOL> op = new Operation<BOOL>(alg, tensors.get(2));
				Relation<BOOL> rel = new Relation<BOOL>(alg, tensors.get(3));
				GaloisConn<BOOL> gal = GaloisConn.lift(alg, galois);

				BOOL b = op.isOperation();
				b = alg.and(b, alg.not(op.preserves(rel)));

				Relation<BOOL> p1 = preservedOps(alg, rel);
				Relation<BOOL> c1 = gal.leftClosure(rels);
				b = alg.and(b, p1.intersect(c1).isEqualTo(ops));

				Relation<BOOL> p2 = preservedRels(alg, op);
				Relation<BOOL> c2 = gal.rightClosure(ops);
				b = alg.and(b, p2.intersect(c2).isEqualTo(rels));

				Relation<BOOL> d1 = gal.leftClosure(c2);
				// Relation<BOOL> d2 = gal.rightClosure(c1);
				b = alg.and(b, alg.not(d1.isEqualTo(ops)));

				return b;
			}
		};

		List<Tensor<Boolean>> sol = problem.solveOne(solver);
		if (sol == null)
			return null;

		System.out.println(sol.get(0));
		System.out.println(sol.get(1));
		System.out.println(sol.get(2));
		System.out.println(sol.get(3));

		return new Pair(Operation.wrap(sol.get(2)), Relation.wrap(sol.get(3)));
	}

	public boolean addCriticalPair(int opArity, int relArity) {
		Pair pair = getCriticalPair(opArity, relArity);
		if (pair == null)
			return false;

		add(pair.op);
		add(pair.rel);
		return true;
	}

	public boolean addCriticalOp(int opArity, int relArity) {
		Pair pair = getCriticalPair(opArity, relArity);
		if (pair == null)
			return false;

		add(pair.op);
		return true;
	}

	public boolean addCriticalRel(int opArity, int relArity) {
		Pair pair = getCriticalPair(opArity, relArity);
		if (pair == null)
			return false;

		add(pair.rel);
		return true;
	}

	public boolean hasCriticalPair(int opArity, int relArity) {
		return getCriticalPair(opArity, relArity) != null;
	}

	List<Relation<Boolean>> getClosedOpSets(int limit) {
		return GaloisConn.findLeftClosedSets(solver, galois, limit);
	}

	List<Relation<Boolean>> getClosedRelSets(int limit) {
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
		for (int i = 0; i < 10; i++) {
			if (!clone.addCriticalPair(1, 1))
				break;
			clone.print();
			clone.printClosedRelSets(-1);

			System.out.println();
		}
	}
}
