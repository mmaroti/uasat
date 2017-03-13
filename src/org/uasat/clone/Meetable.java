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

public abstract class Meetable {
	protected final int size;

	public Meetable(int size) {
		this.size = size;
	}

	public int getSize() {
		return size;
	}

	/**
	 * You should call verify on the operation to make sure that it is endeed
	 * member of this functional clone.
	 */
	public abstract <BOOL> BOOL member(BoolAlgebra<BOOL> alg, Operation<BOOL> op);

	/**
	 * If this method returns <code>false</code>, then the result returned by
	 * member is not valid and you should call it again.
	 */
	public abstract boolean verify(Operation<Boolean> op);
}
