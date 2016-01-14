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

package org.uapart.math;

import java.util.*;

import org.uapart.core.*;

public class Relation {
	private final Function fun;

	protected Relation(Function fun) {
		if (fun == null || fun.getCodomain() != Domain.BOOL
				|| fun.getArity() <= 0)
			throw new IllegalArgumentException();

		Domain d = fun.getDomain(0);
		for (int i = 1; i < fun.getArity(); i++)
			if (fun.getDomain(i) != d)
				throw new IllegalArgumentException();

		this.fun = fun;
	}

	public int getArity() {
		return fun.getArity();
	}

	public Domain getDomain() {
		return fun.getDomain(0);
	}

	public Term isFull() {
		Domain d = new Domain("i", getArity());
		Table x = Table.create("x", getDomain(), d);

		Term[] xs = new Term[getArity()];
		for (int i = 0; i < xs.length; i++)
			xs[i] = x.get(i);

		return Term.forall(x, fun.of(xs));
	}

	public Term isEmpty() {
		Domain d = new Domain("i", getArity());
		Table x = Table.create("x", getDomain(), d);

		Term[] xs = new Term[getArity()];
		for (int i = 0; i < xs.length; i++)
			xs[i] = x.get(i);

		return Term.exists(x, fun.of(xs)).not();
	}

	public Term isReflexive() {
		Table x = Table.create("x", getDomain());

		Term[] xs = new Term[getArity()];
		Arrays.fill(xs, x.get());

		return Term.forall(x, fun.of(xs));
	}
}
