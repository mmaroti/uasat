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

public class MinimalClones {
	private final SatSolver<?> solver;
	private final int size;
	private final List<Operation<Boolean>> generators;

	public final static int TYPE_NONTRIV = 0;
	public final static int TYPE_MALTSEV = 1;
	public final static int TYPE_MAJORITY = 2;
	private final int type;

	public MinimalClones(int size, int relArity, int type) {
		this(size, type, SatSolver.getDefault());
	}

	public MinimalClones(int size, int type, SatSolver<?> solver) {
		assert size >= 1 && solver != null;

		this.solver = solver;
		this.size = size;
		this.type = type;
		this.generators = new ArrayList<Operation<Boolean>>();
	}

	public int getSize() {
		return size;
	}

	public List<Operation<Boolean>> getGenerators() {
		return generators;
	}

	public <BOOL> BOOL isValid(Operation<BOOL> op) {
		assert op.getSize() == size;

		switch (type) {
		case TYPE_NONTRIV:
			return op.getAlg().not(op.isProjection());

		case TYPE_MALTSEV:
			return op.isMaltsev();

		case TYPE_MAJORITY:
			return op.isMajority();

		default:
			throw new IllegalStateException();
		}
	}
}
