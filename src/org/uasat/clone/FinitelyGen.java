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

public abstract class FinitelyGen {
	protected final int size;

	public FinitelyGen(int size) {
		this.size = size;
	}

	public int getSize() {
		return size;
	}

	/**
	 * This method can return a false positive (but no false negative), so you
	 * should call <code>isMember</code> on the relation to make sure that it is
	 * indeed member of this functional clone.
	 */
	public abstract <BOOL> BOOL isPossibleMember(BoolAlgebra<BOOL> alg, Relation<BOOL> rel);

	/**
	 * Checks if the given concrete relation is a member of the clone.
	 */
	public abstract boolean isMember(Relation<Boolean> rel);
}
