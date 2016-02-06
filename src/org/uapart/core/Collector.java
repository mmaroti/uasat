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

import java.util.*;

public class Collector extends Term {
	private final Term subterm;
	private final int trigger;
	private final Table[] tables;
	private final List<Table[]> collected;

	public Collector(Term subterm, int trigger, Table... tables) {
		if (subterm == null || tables == null || trigger < -1
				|| trigger >= subterm.getDomain().getSize())
			throw new IllegalArgumentException();

		this.subterm = subterm;
		this.trigger = trigger;
		this.tables = tables;
		collected = new ArrayList<Table[]>();
	}

	@Override
	public Domain getDomain() {
		return subterm.getDomain();
	}

	public List<Table[]> getCollected() {
		return collected;
	}

	@Override
	public int $evaluate() {
		int a = subterm.$evaluate();

		if ((trigger >= 0 && a == trigger) || trigger == -1) {
			Table[] ts = new Table[tables.length];
			for (int i = 0; i < ts.length; i++)
				ts[i] = tables[i].copy();

			collected.add(ts);
		}

		return a;
	}

	@Override
	public int getBound() {
		return subterm.getBound();
	}
}
