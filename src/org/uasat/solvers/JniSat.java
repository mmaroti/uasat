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

package org.uasat.solvers;

import java.util.*;

import org.jnisat.*;

import org.uasat.core.*;

public class JniSat extends SatSolver<Integer> {
	public int totalVariables = 0;
	public int totalClauses = 0;
	public int totalSolves = 0;

	@Override
	public int getTotalVariables() {
		return totalVariables;
	}

	@Override
	public int getTotalClauses() {
		return totalClauses;
	}

	@Override
	public int getTotalSolves() {
		return totalSolves;
	}

	private final Solver solver;

	public JniSat(String what) {
		super(Integer.TYPE, -1, 1);
		solver = Solver.create(what);
		int a = solver.addVariable();
		if (a != 1)
			throw new IllegalStateException();
		solver.addClause(1);
	}

	@Override
	public void clear() {
		solver.reset();
		int a = solver.addVariable();
		if (a != 1)
			throw new IllegalStateException();
		solver.addClause(1);
	}

	@Override
	public Integer variable() {
		totalVariables += 1;
		return solver.addVariable();
	}

	@Override
	public void clause(List<Integer> clause) {
		totalClauses += 1;
		int[] c = new int[clause.size()];
		for (int i = 0; i < c.length; i++)
			c[i] = clause.get(i);
		solver.addClause(c);
	}

	@Override
	public void clause(Integer lit1) {
		totalClauses += 1;
		solver.addClause(lit1);
	}

	@Override
	public void clause(Integer lit1, Integer lit2) {
		totalClauses += 1;
		solver.addClause(lit1, lit2);
	}

	@Override
	public void clause(Integer lit1, Integer lit2, Integer lit3) {
		totalClauses += 1;
		solver.addClause(lit1, lit2, lit3);
	}

	@Override
	public boolean solve() {
		totalSolves += 1;
		return solver.solve();
	}

	@Override
	public boolean decode(Integer term) {
		return solver.getValue(term) > 0;
	}

	@Override
	public Integer not(Integer elem) {
		assert elem != 0;
		return -elem;
	}

	private static final int AUXILIARY_FLAGS = Solver.FLAG_ELIMINATE;

	@Override
	public Integer and(Integer a, Integer b) {
		if (a == -1 || b == -1)
			return -1;
		else if (a == 1)
			return b;
		else if (b == 1)
			return a;
		else if (a == b)
			return a;
		else if (a == -b)
			return -1;

		int var = solver.addVariable(AUXILIARY_FLAGS);
		solver.addClause(a, -var);
		solver.addClause(b, -var);
		solver.addClause(-a, -b, var);

		totalVariables += 1;
		totalClauses += 3;
		return var;
	}

	@Override
	public Integer or(Integer a, Integer b) {
		if (a == 1 || b == 1)
			return 1;
		else if (a == -1)
			return b;
		else if (b == -1)
			return a;
		else if (a == b)
			return a;
		else if (a == -b)
			return 1;

		int var = solver.addVariable(AUXILIARY_FLAGS);
		solver.addClause(-a, var);
		solver.addClause(-b, var);
		solver.addClause(a, b, -var);

		totalVariables += 1;
		totalClauses += 3;
		return var;
	}

	@Override
	public Integer add(Integer a, Integer b) {
		if (a == 1)
			return -b;
		else if (a == -1)
			return b;
		else if (b == 1)
			return -a;
		else if (b == -1)
			return a;

		int var = solver.addVariable(AUXILIARY_FLAGS);
		solver.addClause(a, b, -var);
		solver.addClause(a, -b, var);
		solver.addClause(-a, b, var);
		solver.addClause(-a, -b, -var);

		totalVariables += 1;
		totalClauses += 4;
		return var;
	}
}
