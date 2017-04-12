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

public class Test {
	public static final Domain BOOLEAN = new Domain.Primitive(2);

	public static final Element FALSE = new Element.Constant(BOOLEAN, 0);
	public static final Element TRUE = new Element.Constant(BOOLEAN, 1);

	public static final Domain BOOLEAN_UNARY_OP = new Domain.Function(new Domain.Function(new Domain.Primitive(1),
		BOOLEAN), BOOLEAN);
	public static final Domain BOOLEAN_BINARY_OP = new Domain.Function(new Domain.Function(new Domain.Primitive(2),
		BOOLEAN), BOOLEAN);
}
