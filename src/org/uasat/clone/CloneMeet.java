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

import org.uasat.core.*;
import org.uasat.math.*;

public class CloneMeet extends FinitelyRel {
	protected final FinitelyRel[] clones;

	public CloneMeet(FinitelyRel... clones) {
		super(clones[0].getSize());

		for (int i = 1; i < clones.length; i++)
			assert clones[i].getSize() == size;

		this.clones = clones;
	}

	@Override
	public <BOOL> BOOL isPossibleMember(BoolAlgebra<BOOL> alg, Operation<BOOL> op) {
		BOOL b = alg.TRUE;

		for (int i = 0; i < clones.length; i++)
			b = alg.and(b, clones[i].isPossibleMember(alg, op));

		return b;
	}

	@Override
	public boolean isMember(Operation<Boolean> op) {
		for (int i = 0; i < clones.length; i++)
			if (!clones[i].isMember(op))
				return false;

		return true;
	}
}
