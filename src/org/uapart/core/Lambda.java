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

public class Lambda extends Function {
	private final Variable[] vars;
	private final Term term;

	public Lambda(Variable[] vars, Term term) {
		super(calculateCodomain(term), calculateDomains(vars));

		this.term = term;
		this.vars = vars;
	}

	private static Domain calculateCodomain(Term term) {
		if (term == null)
			throw new IllegalArgumentException();

		return term.getDomain();
	}

	private static Domain[] calculateDomains(Variable[] vars) {
		if (vars == null)
			throw new IllegalArgumentException();

		Domain[] domains = new Domain[vars.length];
		for (int i = 0; i < vars.length; i++) {
			if (vars[i].$evaluate() != Integer.MIN_VALUE)
				throw new IllegalArgumentException();

			domains[i] = vars[i].getDomain();
		}

		for (int i = 0; i < vars.length; i++)
			vars[i].$set(Integer.MAX_VALUE);

		return domains;
	}

	@Override
	public Term of(Term... subterms) {
		return new Eval(this, subterms);
	}

	static class Eval extends Term {
		private final Variable[] vars;
		private final Term[] args;
		private final Term term;

		Eval(Lambda lambda, Term[] subterms) {
			assert lambda != null;
			vars = lambda.vars;
			term = lambda.term;

			if (subterms == null || subterms.length != vars.length)
				throw new IllegalArgumentException();

			for (int i = 0; i < subterms.length; i++)
				if (subterms[i].getDomain() != vars[i].getDomain())
					throw new IllegalArgumentException();

			this.args = subterms;
		}

		@Override
		public Domain getDomain() {
			return term.getDomain();
		}

		@Override
		public int $evaluate() {
			for (int i = 0; i < vars.length; i++)
				vars[i].$set(args[i].$evaluate());

			return term.$evaluate();
		}

		@Override
		public int getBound() {
			int a = term.getBound();
			for (int i = 0; i < args.length; i++) {
				int b = args[i].getBound();
				if (b < a)
					a = b;
			}
			return a;
		}
	}
}
