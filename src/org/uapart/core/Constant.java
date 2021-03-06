/**
 *	Copyright (C) Miklos Maroti, 2016
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

package org.uapart.core;

public class Constant extends Term {
	private final Domain domain;
	private final int value;

	public Constant(Domain domain, int value) {
		if (domain == null || value < 0 || value >= domain.getSize())
			throw new IllegalArgumentException();

		this.domain = domain;
		this.value = value;
	}

	@Override
	public Domain getDomain() {
		return domain;
	}

	@Override
	public int $evaluate() {
		return value;
	}

	@Override
	public int getBound() {
		return 0;
	}

	public static final Constant FALSE = new Constant(Domain.BOOL, 0);
	public static final Constant TRUE = new Constant(Domain.BOOL, 1);

	public static final Constant LT = new Constant(Domain.ORD, 0);
	public static final Constant EQ = new Constant(Domain.ORD, 1);
	public static final Constant GT = new Constant(Domain.ORD, 2);
}
