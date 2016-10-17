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
	private final int opArity;
	private final int relArity;
	private final SatSolver<?> solver;

	private final List<Operation<Boolean>> generators;
	private final List<Relation<Boolean>> upperLimit;

	public MinimalClones(String type, int size, int opArity, int relArity) {
		this(type, size, opArity, relArity, SatSolver.getDefault());
	}

	public MinimalClones(String type, int size, int opArity, int relArity,
			SatSolver<?> solver) {
		assert size >= 1 && solver != null;

		this.type = type;
		this.size = size;
		this.opArity = opArity;
		this.relArity = relArity;
		this.solver = solver;

		this.generators = new ArrayList<Operation<Boolean>>();
		this.upperLimit = new ArrayList<Relation<Boolean>>();
	}

	public int getSize() {
		return size;
	}

	public void addUpperLimit(Relation<Boolean> rel) {
		upperLimit.add(rel);
	}

	public List<Relation<Boolean>> getUpperLimit() {
		return upperLimit;
	}

	public void addGenerator(Operation<Boolean> op) {
		generators.add(op);
	}

	public List<Operation<Boolean>> getGenerators() {
		return generators;
	}

	protected <BOOL> BOOL isValidGenerator(Operation<BOOL> op) {
		assert op.getSize() == size;

		BoolAlgebra<BOOL> alg = op.getAlg();
		BOOL b = op.isOperation();

		if (type == "nontrivial")
			b = alg.and(b, alg.not(op.isProjection()));
		else if (type == "majority")
			b = alg.and(b, op.isMajority());
		else if (type == "maltsev")
			b = alg.and(b, op.isMaltsev());
		else if (type == "minority")
			b = alg.and(b, op.isMinority());
		else
			throw new IllegalStateException();

		return b;
	}

	protected static <BOOL> BOOL isNotAboveClone(Operation<BOOL> op,
			Relation<BOOL> witness, Operation<Boolean> clone) {
		BoolAlgebra<BOOL> alg = op.getAlg();
		Operation<BOOL> clo = Operation.lift(alg, clone);

		BOOL b = alg.not(clo.preserves(witness));
		b = alg.and(b, op.preserves(witness));

		return b;
	}

	protected static <BOOL> BOOL isBelowClone(Operation<BOOL> op,
			List<Relation<Boolean>> rels) {
		BoolAlgebra<BOOL> alg = op.getAlg();

		BOOL b = alg.TRUE;
		for (Relation<Boolean> rel : rels)
			b = alg.and(b, op.preserves(Relation.lift(alg, rel)));

		return b;
	}

	public Operation<Boolean> findOne() {
		int[][] shapes = new int[1 + generators.size()][];

		shapes[0] = Util.createShape(size, opArity + 1);
		for (int i = 1; i < shapes.length; i++)
			shapes[i] = Util.createShape(size, relArity);

		SatProblem prob = new SatProblem(shapes) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Operation<BOOL> op = new Operation<BOOL>(alg, tensors.get(0));

				BOOL b = isValidGenerator(op);
				b = alg.and(b, isBelowClone(op, upperLimit));

				for (int i = 1; i < tensors.size(); i++) {
					Relation<BOOL> witness = new Relation<BOOL>(alg,
							tensors.get(i));
					Operation<Boolean> c = generators.get(i - 1);
					b = alg.and(b, isNotAboveClone(op, witness, c));
				}

				return b;
			}
		};

		List<Tensor<Boolean>> sol = prob.solveOne(solver);
		if (sol == null)
			return null;

		Operation<Boolean> op = Operation.wrap(sol.get(0));
		final List<Relation<Boolean>> relations = new ArrayList<Relation<Boolean>>();
		relations.addAll(upperLimit);
		for (int i = 1; i < sol.size(); i++)
			relations.add(Relation.wrap(sol.get(i)));

		for (;;) {
			final Operation<Boolean> op1 = op;

			prob = new SatProblem(Util.createShape(size, opArity + 1),
					Util.createShape(size, relArity)) {
				@Override
				public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
						List<Tensor<BOOL>> tensors) {

					Operation<BOOL> op2 = new Operation<BOOL>(alg,
							tensors.get(0));
					Relation<BOOL> witness = new Relation<BOOL>(alg,
							tensors.get(1));

					BOOL b = isValidGenerator(op2);
					b = alg.and(b, isBelowClone(op2, relations));
					b = alg.and(b, isNotAboveClone(op2, witness, op1));

					return b;
				}
			};

			sol = prob.solveOne(solver);
			if (sol == null)
				break;

			op = Operation.wrap(sol.get(0));
			relations.add(Relation.wrap(sol.get(1)));
		}

		generators.add(op);
		return op;
	}

	public void findAll() {
		while (findOne() != null)
			;
	}

	public void print() {
		System.out.println(type + " minimal clones on universe " + size
				+ " separated by " + relArity + "-ary relations");

		System.out.println("generators: " + generators.size());
		int c = 0;
		for (Operation<Boolean> op : generators)
			System.out.println((c++) + ":\t" + Operation.format(op));

		if (!upperLimit.isEmpty()) {

			System.out.println("below the clone with relations: "
					+ upperLimit.size());
			c = 0;
			for (Relation<Boolean> rel : upperLimit)
				System.out.println((c++) + ":\t" + Relation.format(rel));

			System.out.println();
		}
	}
}
