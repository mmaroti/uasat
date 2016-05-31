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
	private final GeneratedOps interval;
	private final List<Operation<Boolean>> operations;
	private final List<Relation<Boolean>> relations;
	private GaloisConn<Boolean> galois;
	public boolean trace = false;

	public CloneInterval(int size) {
		this(size, SatSolver.getDefault());
	}

	public CloneInterval(int size, SatSolver<?> solver) {
		assert size >= 1 && solver != null;

		this.size = size;
		this.solver = solver;
		this.interval = null;

		operations = new ArrayList<Operation<Boolean>>();
		relations = new ArrayList<Relation<Boolean>>();
		galois = GaloisConn.wrap(Tensor.constant(new int[] { 0, 0 },
				Boolean.FALSE));
	}

	public CloneInterval(GeneratedOps interval) {
		this(interval, SatSolver.getDefault());
	}

	public CloneInterval(GeneratedOps interval, SatSolver<?> solver) {
		assert interval.getSize() >= 1 && solver != null;
		assert interval.isSelfClosed();

		this.size = interval.getSize();
		this.solver = solver;
		this.interval = interval;

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
		Tensor<BOOL> tensor = Tensor.generate(alg.type, operations.size(),
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
		Tensor<BOOL> tensor = Tensor.generate(alg.type, relations.size(),
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

	/*
	 * Ensures that all closed sets of the selected relations in a join
	 * irreducible partial clone (defined by the polarity of a single operation)
	 * is definable by the selected operations.
	 */
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

				b = alg.and(b, op.isPermuteMinimal());
				b = alg.and(b, rel.isPermuteMinimal());

				if (interval != null) {
					b = alg.and(b, interval.isClosedUnder(op));
					b = alg.and(b, interval.isCompatibleWith(rel));
				}

				Relation<BOOL> rset = preservedRels(alg, op);
				Relation<BOOL> oset = gal.leftClosure(rset);
				b = alg.and(b, oset.isSubsetOf(preservedOps(alg, rel)));

				return b;
			}
		};

		List<Tensor<Boolean>> sol = problem.solveOne(solver);
		if (sol == null)
			return false;

		Operation<Boolean> op = Operation.wrap(sol.get(0));
		if (trace)
			System.out.println(Operation.format(op));

		add(op);
		return true;
	}

	/*
	 * Ensures that all closed sets of selected operations in a meet irreducible
	 * partial clone (defined by the polarity of a single relation) is definable
	 * by the selected relations.
	 */
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

				b = alg.and(b, op.isPermuteMinimal());
				b = alg.and(b, rel.isPermuteMinimal());

				if (interval != null) {
					b = alg.and(b, interval.isClosedUnder(op));
					b = alg.and(b, interval.isCompatibleWith(rel));
				}

				Relation<BOOL> oset = preservedOps(alg, rel);
				Relation<BOOL> rset = gal.rightClosure(oset);
				b = alg.and(b, rset.isSubsetOf(preservedRels(alg, op)));

				return b;
			}
		};

		List<Tensor<Boolean>> sol = problem.solveOne(solver);
		if (sol == null)
			return false;

		Relation<Boolean> rel = Relation.wrap(sol.get(1));
		if (trace)
			System.out.println(Relation.format(rel));

		add(rel);
		return true;
	}

	/*
	 * Ensures, that every meet irreducible partial clone (the polarity of a
	 * single relation) is generated by some subset of selected operations.
	 */
	public boolean addCriticalOp2(int opArity, int relArity) {
		assert opArity >= 0 && relArity >= 1;

		SatProblem problem = new SatProblem(Util.createShape(size, relArity),
				Util.createShape(size, opArity + 1), Util.createShape(size,
						relArity)) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> rel1 = new Relation<BOOL>(alg, tensors.get(0));
				Operation<BOOL> op = new Operation<BOOL>(alg, tensors.get(1));
				Relation<BOOL> rel2 = new Relation<BOOL>(alg, tensors.get(2));

				BOOL b = op.isOperation();
				b = alg.and(b, op.preserves(rel1));
				b = alg.and(b, alg.not(op.preserves(rel2)));

				b = alg.and(b, op.isPermuteMinimal());
				b = alg.and(b, rel1.isPermuteMinimal());
				b = alg.and(b, rel2.isPermuteMinimal());

				if (interval != null) {
					b = alg.and(b, interval.isClosedUnder(op));
					b = alg.and(b, interval.isCompatibleWith(rel1));
					b = alg.and(b, interval.isCompatibleWith(rel2));
				}

				Relation<BOOL> set1 = preservedOps(alg, rel1);
				Relation<BOOL> set2 = preservedOps(alg, rel2);
				b = alg.and(b, set1.isSubsetOf(set2));

				return b;
			}
		};

		List<Tensor<Boolean>> sol = problem.solveOne(solver);
		if (sol == null)
			return false;

		Operation<Boolean> op = Operation.wrap(sol.get(1));
		if (trace)
			System.out.println(Operation.format(op));

		add(op);
		return true;
	}

	/*
	 * Ensures, that every join irreducible partial clone (the polarity of a
	 * single operation) is generated by some subset of selected relations.
	 */
	public boolean addCriticalRel2(int opArity, int relArity) {
		assert opArity >= 0 && relArity >= 1;

		SatProblem problem = new SatProblem(
				Util.createShape(size, opArity + 1), Util.createShape(size,
						relArity), Util.createShape(size, opArity + 1)) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Operation<BOOL> op1 = new Operation<BOOL>(alg, tensors.get(0));
				Relation<BOOL> rel = new Relation<BOOL>(alg, tensors.get(1));
				Operation<BOOL> op2 = new Operation<BOOL>(alg, tensors.get(2));

				BOOL b = alg.and(op1.isOperation(), op2.isOperation());
				b = alg.and(b, op1.preserves(rel));
				b = alg.and(b, alg.not(op2.preserves(rel)));

				b = alg.and(b, rel.isPermuteMinimal());
				b = alg.and(b, op1.isPermuteMinimal());
				b = alg.and(b, op2.isPermuteMinimal());

				if (interval != null) {
					b = alg.and(b, interval.isClosedUnder(op1));
					b = alg.and(b, interval.isClosedUnder(op2));
					b = alg.and(b, interval.isCompatibleWith(rel));
				}

				Relation<BOOL> set1 = preservedRels(alg, op1);
				Relation<BOOL> set2 = preservedRels(alg, op2);
				b = alg.and(b, set1.isSubsetOf(set2));

				return b;
			}
		};

		List<Tensor<Boolean>> sol = problem.solveOne(solver);
		if (sol == null)
			return false;

		Relation<Boolean> rel = Relation.wrap(sol.get(1));
		if (trace)
			System.out.println(Relation.format(rel));

		add(rel);
		return true;
	}

	public void generate(int opArity, int relArity) {
		assert opArity >= 1 && relArity >= 1;

		if (trace)
			System.out.println("critical ops of type 2:");
		for (int i = 1; i <= opArity; i++)
			while (addCriticalOp2(i, relArity))
				;

		if (trace)
			System.out.println("critical rels of type 2:");
		for (int i = 1; i <= relArity; i++)
			while (addCriticalRel2(opArity, i))
				;

		for (int i = 1; i <= Math.max(opArity, relArity); i++) {
			int a = 0;
			for (;;) {
				if (trace)
					System.out.println("critical ops of type 1:");
				while (addCriticalOp(Math.min(i, opArity),
						Math.min(i, relArity)))
					a = 0;
				if (++a >= 2)
					break;

				if (trace)
					System.out.println("critical rels of type 1:");
				while (addCriticalRel(Math.min(i, opArity),
						Math.min(i, relArity)))
					a = 0;
				if (++a >= 2)
					break;
			}
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
			System.out.println((c++) + ":\t" + Operation.format(op));

		System.out.println("relations: " + relations.size());
		c = 0;
		for (Relation<Boolean> rel : relations)
			System.out.println((c++) + ":\t" + Relation.format(rel));

		GaloisConn.print(galois);
	}

	public void printClosedOpSets(int limit) {
		List<Relation<Boolean>> sets = getClosedOpSets(limit);
		System.out.println("closed sets of ops: "
				+ (sets.size() == limit ? ">=" : "") + sets.size());

		for (int i = 0; i < sets.size(); i++)
			System.out.println(i + ":\t" + Relation.format(sets.get(i)));
	}

	public void printClosedRelSets(int limit) {
		List<Relation<Boolean>> sets = getClosedRelSets(limit);
		System.out.println("closed sets of rels: "
				+ (sets.size() == limit ? ">=" : "") + sets.size());

		for (int i = 0; i < sets.size(); i++)
			System.out.println(i + ":\t" + Relation.format(sets.get(i)));
	}
}
