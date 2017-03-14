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

public class CloneRel extends FinitelyRel {
	protected List<Relation<Boolean>> relations = new ArrayList<Relation<Boolean>>();

	public CloneRel(int size) {
		super(size);
	}

	@SafeVarargs
	public CloneRel(Relation<Boolean>... relations) {
		super(relations[0].getSize());
		for (int i = 1; i < relations.length; i++)
			assert relations[i].getSize() == size;

		this.relations.addAll(Arrays.asList(relations));
	}

	public List<Relation<Boolean>> getRelations() {
		return relations;
	}

	public void addRelation(Relation<Boolean> rel) {
		relations.add(rel);
	}

	public static CloneRel idempotentClone(int size) {
		CloneRel clone = new CloneRel(size);
		for (int i = 0; i < size; i++)
			clone.addRelation(Relation.singleton(size, i));
		return clone;
	}

	public <BOOL> BOOL isPossibleMember(BoolAlgebra<BOOL> alg, Operation<BOOL> op) {
		BOOL b = alg.TRUE;
		for (Relation<Boolean> rel : relations)
			b = alg.and(b, op.preserves(Relation.lift(alg, rel)));

		return b;
	}

	public boolean isMember(Operation<Boolean> op) {
		return true;
	}
}
