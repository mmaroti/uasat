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

import java.util.Arrays;

public class Lambda extends Function {
	private final Term subterm;
	private final Table vars;

	public Lambda(Term term, Table vars) {
		super(calculateCodomain(term), calculateDomains(vars));

		this.subterm = term;
		this.vars = vars;
	}

	private static Domain calculateCodomain(Term term) {
		if (term == null)
			throw new IllegalArgumentException();

		return term.getDomain();
	}

	private static Domain[] calculateDomains(Table vars) {
		if (vars == null || vars.getArity() >= 2)
			throw new IllegalArgumentException();

		int size = vars.getTable().length;
		Domain[] domains = new Domain[size];
		Arrays.fill(domains, vars.getCodomain());

		return domains;
	}

	@Override
	public Term of(Term... subterms) {
		if (subterms == null || subterms.length != vars.getTable().length)
			throw new IllegalArgumentException();
	}

	static class Eval extends Term {

		@Override
		public Domain getDomain() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int $evaluate() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getBound() {
			// TODO Auto-generated method stub
			return 0;
		}

	}
}
