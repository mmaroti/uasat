/**
 * Copyright (C) Miklos Maroti, 2015-2016
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

public class MinimalClones {
	private final String type;
	private final int size;
	private final int relArity;
	private final SatSolver<?> solver;
	public boolean trace = false;

	private final List<Operation<Boolean>> generators;
	private final List<Relation<Boolean>> upperLimit;

	public MinimalClones(String type, int size, int relArity) {
		this(type, size, relArity, SatSolver.getDefault());
	}

	public MinimalClones(String type, int size, int relArity, SatSolver<?> solver) {
		assert size >= 1 && solver != null;

		this.type = type;
		this.size = size;
		this.relArity = relArity;
		this.solver = solver;

		this.generators = new ArrayList<Operation<Boolean>>();
		this.upperLimit = new ArrayList<Relation<Boolean>>();
	}

	public int getSize() {
		return size;
	}

	public void addUpperLimit(Relation<Boolean> rel) {
		for (Operation<Boolean> op : generators)
			assert op.preserves(rel);

		upperLimit.add(rel);
	}

	public List<Relation<Boolean>> getUpperLimit() {
		return upperLimit;
	}

	public void addGenerator(Operation<Boolean> op) {
		for (Relation<Boolean> rel : upperLimit)
			assert op.preserves(rel);

		generators.add(op);
	}

	public List<Operation<Boolean>> getGenerators() {
		return generators;
	}

	public int getOpArity() {
		if (type.equals("nontriv1"))
			return 1;
		else if (type.equals("nontriv2"))
			return 2;
		else if (type.equals("nontriv3"))
			return 3;
		else if (type.equals("majority"))
			return 3;
		else if (type.equals("minority"))
			return 3;
		else if (type.equals("maltsev"))
			return 3;
		else
			throw new IllegalStateException();
	}

	protected <BOOL> BOOL isValidOp(Operation<BOOL> op) {
		assert op.getSize() == size;

		BoolAlgebra<BOOL> alg = op.getAlg();
		BOOL b = op.isOperation();

		if (type.equals("nontriv1") || type.equals("nontriv2") || type.equals("nontriv3"))
			b = alg.and(b, alg.not(op.isProjection()));
		else if (type.equals("majority"))
			b = alg.and(b, op.isMajority());
		else if (type.equals("maltsev"))
			b = alg.and(b, op.isMaltsev());
		else if (type.equals("minority"))
			b = alg.and(b, op.isMinority());
		else
			throw new IllegalStateException();

		return b;
	}

	public static <BOOL> BOOL isNotAboveClone(Operation<BOOL> op, Relation<BOOL> witness, Operation<Boolean> other) {
		BoolAlgebra<BOOL> alg = op.getAlg();
		Operation<BOOL> op2 = Operation.lift(alg, other);

		BOOL b = alg.not(op2.preserves(witness));
		b = alg.and(b, op.preserves(witness));

		return b;
	}

	public static <BOOL> BOOL isBelowClone(Operation<BOOL> op, List<Relation<Boolean>> rels) {
		BoolAlgebra<BOOL> alg = op.getAlg();

		BOOL b = alg.TRUE;
		for (Relation<Boolean> rel : rels)
			b = alg.and(b, op.preserves(Relation.lift(alg, rel)));

		return b;
	}

	public ClonePair findNotAboveGens() {
		int opArity = getOpArity();

		int[][] shapes = new int[1 + generators.size()][];
		shapes[0] = Util.createShape(size, opArity + 1);

		int[] shape = Util.createShape(size, relArity);
		for (int i = 1; i < shapes.length; i++)
			shapes[i] = shape;

		SatProblem prob = new SatProblem(shapes) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg, List<Tensor<BOOL>> tensors) {
				Operation<BOOL> op = new Operation<BOOL>(alg, tensors.get(0));

				BOOL b = isValidOp(op);
				b = alg.and(b, isBelowClone(op, upperLimit));

				for (int i = 1; i < tensors.size(); i++) {
					Relation<BOOL> witness = new Relation<BOOL>(alg, tensors.get(i));
					Operation<Boolean> c = generators.get(i - 1);
					b = alg.and(b, isNotAboveClone(op, witness, c));
				}

				return b;
			}
		};

		List<Tensor<Boolean>> sol = prob.solveOne(solver);
		if (sol == null)
			return null;

		ClonePair clone = new ClonePair(size, solver);
		clone.addOperation(Operation.wrap(sol.get(0)));
		clone.addRelations(upperLimit);
		for (int i = 1; i < sol.size(); i++)
			clone.addRelation(Relation.wrap(sol.get(i)));

		return clone;
	}

	public Operation<Boolean> findNotAboveAny(final ClonePair clone) {
		if (clone.getOperations().size() != 1)
			throw new IllegalArgumentException();
		int opArity = getOpArity();

		for (;;) {
			final Operation<Boolean> op = clone.getOperations().get(0);

			SatProblem prob = new SatProblem(Util.createShape(size, opArity + 1), Util.createShape(size, relArity)) {
				@Override
				public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg, List<Tensor<BOOL>> tensors) {

					Operation<BOOL> op2 = new Operation<BOOL>(alg, tensors.get(0));
					Relation<BOOL> witness = new Relation<BOOL>(alg, tensors.get(1));

					BOOL b = isValidOp(op2);
					b = alg.and(b, isBelowClone(op2, clone.getRelations()));
					b = alg.and(b, isNotAboveClone(op2, witness, op));

					return b;
				}
			};

			List<Tensor<Boolean>> sol = prob.solveOne(solver);
			if (sol == null)
				return op;

			clone.clearOperations();
			clone.addOperation(Operation.wrap(sol.get(0)));
			clone.addRelation(Relation.wrap(sol.get(1)));
		}
	}

	public List<Relation<Boolean>> findWitnesses(final Operation<Boolean> op) {
		List<Relation<Boolean>> rels = new ArrayList<Relation<Boolean>>();

		for (final Operation<Boolean> clone : generators) {
			SatProblem prob = new SatProblem(Util.createShape(size, relArity)) {
				@Override
				public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg, List<Tensor<BOOL>> tensors) {
					Relation<BOOL> rel = new Relation<BOOL>(alg, tensors.get(0));
					BOOL b = isNotAboveClone(Operation.lift(alg, op), rel, clone);
					b = alg.and(b, rel.isLexMinimal());

					return b;
				}
			};

			List<Tensor<Boolean>> sol = prob.solveOne(solver);
			if (sol == null)
				rels.add(null);
			else
				rels.add(Relation.wrap(sol.get(0)));
		}

		return rels;
	}

	public Operation<Boolean> findOne() {
		ClonePair clone = findNotAboveGens();
		if (clone == null)
			return null;

		Operation<Boolean> op = findNotAboveAny(clone);
		generators.add(op);
		if (trace)
			System.out.println(Operation.format(op));

		return op;
	}

	public void findAll() {
		while (findOne() != null)
			;
	}

	public void print() {
		System.out.println(type + " minimal clones on universe " + size + " separated by " + relArity
			+ "-ary relations");

		System.out.println("generators: " + generators.size());
		int c = 0;
		for (Operation<Boolean> op : generators)
			System.out.println((c++) + ":\t" + Operation.format(op));

		if (!upperLimit.isEmpty()) {

			System.out.println("below the clone with relations: " + upperLimit.size());
			c = 0;
			for (Relation<Boolean> rel : upperLimit)
				System.out.println((c++) + ":\t" + Relation.format(rel));
		}
		System.out.println();
	}

	public void printWitnesses() {
		for (int i = 0; i < generators.size(); i++) {
			Operation<Boolean> op = generators.get(i);
			System.out.println(i + ":\t witnesses for " + Operation.format(op));

			List<Relation<Boolean>> witnesses = findWitnesses(op);
			for (Relation<Boolean> rel : witnesses)
				System.out.println(rel == null ? "none" : Relation.format(rel));
		}
		System.out.println();
	}
}
