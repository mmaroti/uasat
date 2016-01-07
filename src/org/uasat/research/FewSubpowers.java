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
import org.uasat.solvers.*;

public class FewSubpowers {
	private SatSolver<?> solver = new Sat4J();
	private static DecimalFormat TIME_FORMAT = new DecimalFormat("0.00");

	public static Operation<Boolean> IMPLICATION = Operation.parseTable(2, 2,
			"11 01");

	public void printSubpowers(final Operation<Boolean> op, int arity) {

		BoolProblem problem = new BoolProblem(Util.createShape(op.getSize(),
				arity)) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Operation<BOOL> op2 = Operation.lift(alg, op);
				Relation<BOOL> rel = new Relation<BOOL>(alg, tensors.get(0));

				return op2.preserves(rel);
			}
		};

		Tensor<Boolean> tensor = problem.solveAll(solver).get(0);

		int i = 0;
		for (Tensor<Boolean> t : Tensor.unstack(tensor)) {
			System.out.println("" + i + ": "
					+ Relation.formatMembers(Relation.wrap(t)));
			i += 1;
		}
	}

	public static void main(String[] args) {
		long time = System.currentTimeMillis();
		FewSubpowers test = new FewSubpowers();

		test.printSubpowers(IMPLICATION, 2);

		time = System.currentTimeMillis() - time;
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time)
				+ " seconds.");
	}
}
