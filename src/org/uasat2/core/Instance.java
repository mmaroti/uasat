/**
 * Copyright (C) Miklos Maroti, 2017
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

package org.uasat2.core;

public abstract class Instance {
	public static final int TRUE = 1;
	public static final int FALSE = -1;

	public static int not(int val) {
		return -val;
	}

	public int and(int val0, int val1) {
		if (val0 == TRUE)
			return val1;
		else if (val0 == FALSE)
			return FALSE;
		else if (val1 == TRUE)
			return val0;
		else if (val1 == FALSE)
			return FALSE;
		else
			throw new IllegalArgumentException();
	}

	static Instance join(Instance... instances) {
		return null;
	}
}
