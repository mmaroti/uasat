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
import org.uasat.solvers.*;

public class CompatibleOps {
	private final Structure<Boolean> structure;
	private final SatSolver<?> solver;
	private int maxSolutions = 100;

	public CompatibleOps(Structure<Boolean> structure) {
		assert structure != null;

		this.structure = structure;
		solver = new Sat4J();
	}

	public CompatibleOps(Structure<Boolean> structure, SatSolver<?> solver) {
		assert structure != null && solver != null;

		this.structure = structure;
		this.solver = solver;
	}

	public Structure<Boolean> getStructure() {
		return structure;
	}

	public SatSolver<?> getSolver() {
		return solver;
	}

	public List<Operation<Boolean>> findUnaryOps(final String options) {
		int size = structure.getSize();
		BoolProblem prob = new BoolProblem(new int[] { size, size }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {

				Operation<BOOL> op = new Operation<BOOL>(alg, tensors.get(0));
				Structure<BOOL> str = Structure.lift(alg, structure);

				BOOL res = op.isOperation();
				res = alg.and(res, str.isCompatibleWith(op));

				for (String token : options.split(" ")) {
					if (token.equals("automorphism"))
						res = alg.and(res, op.isSurjective());
					else if (token.equals("endomorphism"))
						res = alg.and(res, alg.not(op.isSurjective()));
					else if (token.equals("retraction"))
						res = alg.and(res, op.isRetraction());
					else if (!token.isEmpty())
						throw new IllegalArgumentException("invalid option: "
								+ token);
				}

				return res;
			}
		};

		prob.verbose = false;
		Tensor<Boolean> sol = prob.solveAll(solver, maxSolutions).get(0);
		return Operation.wrap(Tensor.unstack(sol));
	}

	public List<Operation<Boolean>> findBinaryOps(final String options) {
		int size = structure.getSize();
		BoolProblem prob = new BoolProblem(new int[] { size, size, size }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {

				Operation<BOOL> op = new Operation<BOOL>(alg, tensors.get(0));
				Structure<BOOL> str = Structure.lift(alg, structure);

				BOOL res = op.isOperation();
				res = alg.and(res, str.isCompatibleWith(op));

				for (String token : options.split(" ")) {
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
					else if (!token.isEmpty())
						throw new IllegalArgumentException("invalid option: "
								+ token);
				}

				return res;
			}
		};

		prob.verbose = false;
		Tensor<Boolean> sol = prob.solveAll(solver, maxSolutions).get(0);
		return Operation.wrap(Tensor.unstack(sol));
	}

	public List<Operation<Boolean>> findTernaryOps(final String options) {
		int size = structure.getSize();
		BoolProblem prob = new BoolProblem(new int[] { size, size, size, size }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {

				Operation<BOOL> op = new Operation<BOOL>(alg, tensors.get(0));
				Structure<BOOL> str = Structure.lift(alg, structure);

				BOOL res = op.isOperation();
				res = alg.and(res, str.isCompatibleWith(op));

				for (String token : options.split(" ")) {
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
					else if (!token.isEmpty())
						throw new IllegalArgumentException("invalid option: "
								+ token);
				}

				return res;
			}
		};

		prob.verbose = false;
		Tensor<Boolean> sol = prob.solveAll(solver, maxSolutions).get(0);
		return Operation.wrap(Tensor.unstack(sol));
	}

	private boolean printOps(String what, List<Operation<Boolean>> list,
			int limit) {
		System.out.println(what.trim() + " ops: "
				+ (list.size() < maxSolutions ? "" : ">= ") + list.size());

		for (int i = 0; i < Math.min(list.size(), limit); i++)
			System.out.println(Operation.formatTable(list.get(i)));

		return !list.isEmpty();
	}

	public boolean printUnaryOps(String options, int limit) {
		return printOps("unary " + options, findUnaryOps(options), limit);
	}

	public void printUnaryOps() {
		printUnaryOps("automorphism", 1);
		printUnaryOps("retraction endomorphism", 1);
		System.out.println();
	}

	public boolean printBinaryOps(String options, int limit) {
		return printOps("binary " + options, findBinaryOps(options), limit);
	}

	@SuppressWarnings("unused")
	public void printBinaryOps() {
		boolean semilattice = printBinaryOps("semilattice", 1);
		boolean two_semilat = semilattice || printBinaryOps("two-semilat", 1);
		boolean commutidemp = two_semilat
				|| printBinaryOps("idempotent commutative", 1);
		boolean commutative = commutidemp
				|| printBinaryOps("commutative essential", 1);
		boolean associative = semilattice
				|| printBinaryOps("associative essential", 1);
		boolean idempotent = commutidemp
				|| printBinaryOps("idempotent essential", 1);
		boolean surjective = idempotent
				|| printBinaryOps("surjective essential", 1);
		boolean essential = associative || idempotent || surjective
				|| commutative || printBinaryOps("essential", 1);
		System.out.println();
	}

	public boolean printTernaryOps(String options, int limit) {
		return printOps("ternary " + options, findTernaryOps(options), limit);
	}

	@SuppressWarnings("unused")
	public void printTernaryOps() {
		boolean majority = printTernaryOps("majority", 1);
		boolean minority = printTernaryOps("minority", 1);
		boolean maltsev = minority || printTernaryOps("maltsev", 1);
		boolean weaknu = majority || minority || printTernaryOps("weak-nu", 1);
		boolean idempotent = weaknu
				|| printTernaryOps("idempotent essential", 1);
		boolean surjective = idempotent
				|| printTernaryOps("surjective essential", 1);
		boolean essential = idempotent || surjective
				|| printTernaryOps("essential", 1);
	}
}
