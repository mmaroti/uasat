/**
 * Copyright (C) Miklos Maroti, 2015-2017
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

package org.uasat.clone;

import java.util.*;
import org.uasat.core.*;
import org.uasat.math.*;

public class CloneGen extends FinitelyGen {
	protected List<Operation<Boolean>> operations = new ArrayList<Operation<Boolean>>();

	public CloneGen(int size) {
		super(size);
	}

	@SafeVarargs
	public CloneGen(Operation<Boolean>... operations) {
		super(operations[0].getSize());
		for (int i = 1; i < operations.length; i++)
			assert operations[i].getSize() == size;

		this.operations.addAll(Arrays.asList(operations));
	}

	public List<Operation<Boolean>> getOperations() {
		return operations;
	}

	public void addOperation(Operation<Boolean> op) {
		operations.add(op);
	}

	public static CloneGen polynomialClone(int size) {
		CloneGen clone = new CloneGen(size);
		for (int i = 0; i < size; i++)
			clone.addOperation(Operation.unaryConstant(size, i));
		return clone;
	}

	@Override
	public <BOOL> BOOL isPossibleMember(BoolAlgebra<BOOL> alg, Relation<BOOL> rel) {
		BOOL b = alg.TRUE;
		for (Operation<Boolean> op : operations)
			b = alg.and(b, Operation.lift(alg, op).preserves(rel));

		return b;
	}

	@Override
	public boolean isMember(Relation<Boolean> rel) {
		return true;
	}
}
