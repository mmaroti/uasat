/**
 * Copyright (C) Miklos Maroti, 2015
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

import org.uasat.core.*;

public class CachedOps extends SatSolver<Integer> {
	private final SatSolver<Integer> solver;
	private final int FALSE;
	private final int TRUE;

	private final static int CACHE_SIZE = 99991;
	private final static int ELEM1_STEP = 311;
	private final int[] andCache;
	private final int[] addCache;

	public CachedOps(SatSolver<Integer> solver) {
		super(solver.getType(), solver.FALSE, solver.TRUE);
		this.solver = solver;

		this.FALSE = solver.FALSE;
		this.TRUE = solver.TRUE;
		andCache = new int[3 * CACHE_SIZE];
		addCache = new int[3 * CACHE_SIZE];

		clear();
	}

	@Override
	public int getTotalVariables() {
		return solver.getTotalVariables();
	}

	@Override
	public int getTotalClauses() {
		return solver.getTotalClauses();
	}

	@Override
	public int getTotalSolves() {
		return solver.getTotalSolves();
	}

	@Override
	public void clear() {
		Arrays.fill(andCache, FALSE);
		Arrays.fill(addCache, FALSE);
		solver.clear();
	}

	@Override
	public Integer variable() {
		return solver.variable();
	}

	@Override
	public void clause(List<Integer> clause) {
		solver.clause(clause);
	}

	@Override
	public void clause(Integer lit1) {
		solver.clause(lit1);
	}

	@Override
	public void clause(Integer lit1, Integer lit2) {
		solver.clause(lit1, lit2);
	}

	@Override
	public void clause(Integer lit1, Integer lit2, Integer lit3) {
		solver.clause(lit1, lit2, lit3);
	}

	@Override
	public boolean solve() {
		return solver.solve();
	}

	@Override
	public boolean decode(Integer term) {
		return solver.decode(term);
	}

	@Override
	public Integer not(Integer elem) {
		return solver.not(elem);
	}

	@Override
	public Integer and(Integer elem1, Integer elem2) {
		int a = elem1;
		int b = elem2;

		if (a == FALSE || b == FALSE)
			return FALSE;
		else if (a == TRUE)
			return b;
		else if (b == TRUE)
			return a;
		else if (a == b)
			return a;
		else if (a == not(b))
			return FALSE;

		if (a > b) {
			int c = a;
			a = b;
			b = c;
		}

		int pos = (a * ELEM1_STEP + b) % CACHE_SIZE;
		if (pos < 0)
			pos += CACHE_SIZE;
		pos *= 3;

		if (andCache[pos] == a && andCache[pos + 1] == b)
			return andCache[pos + 2];

		int var = variable();
		andCache[pos] = a;
		andCache[pos + 1] = b;
		andCache[pos + 2] = var;

		clause(a, not(var));
		clause(b, not(var));
		clause(not(a), not(b), var);
		return var;
	}

	@Override
	public Integer or(Integer elem1, Integer elem2) {
		return not(and(not(elem1), not(elem2)));
	}

	@Override
	public Integer leq(Integer elem1, Integer elem2) {
		return not(and(elem1, not(elem2)));
	}

	@Override
	public Integer add(Integer elem1, Integer elem2) {
		int a = elem1;
		int b = elem2;

		if (a == TRUE)
			return not(b);
		else if (a == FALSE)
			return b;
		else if (b == TRUE)
			return not(a);
		else if (b == FALSE)
			return a;

		if (a > b) {
			int c = a;
			a = b;
			b = c;
		}

		int pos = (a * ELEM1_STEP + b) % CACHE_SIZE;
		if (pos < 0)
			pos += CACHE_SIZE;
		pos *= 3;

		if (addCache[pos] == a && addCache[pos + 1] == b)
			return addCache[pos + 2];

		int var = variable();
		addCache[pos] = a;
		addCache[pos + 1] = b;
		addCache[pos + 2] = var;

		clause(a, b, not(var));
		clause(a, not(b), var);
		clause(not(a), b, var);
		clause(not(a), not(b), not(var));
		return var;
	}

	@Override
	public Integer equ(Integer elem1, Integer elem2) {
		return not(add(elem1, elem2));
	}
}
