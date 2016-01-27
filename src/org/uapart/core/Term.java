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

public abstract class Term {
	public abstract Domain getDomain();

	/**
	 * Attempts to evaluate this term based on already set variables. If the
	 * term can be fully evaluated, then the returned value is between
	 * <code>0</code> and <code>domain.getSize() - 1</code>. If the term cannot
	 * be evaluated because of an unset variable, then it returns a negative
	 * number, which is the most negative rank of unset variables.
	 * 
	 * @return the evaluated result if positive, or the rank of an unset
	 *         variable.
	 */
	public abstract int $evaluate();

	/**
	 * Returns the minimum rank of bound variables within this expression.
	 * 
	 * @return the minimum rank of bound variables within this expression.
	 */
	public abstract int getBound();

	public Term and(Term term) {
		return new BoolAnd(this, term);
	}

	public Term andThen(Term term) {
		return new AndThen(this, term);
	}

	public Term equ(Term term) {
		return new BoolEqu(this, term);
	}

	public Term neq(Term term) {
		return equ(term).not();
	}

	public Term leq(Term term) {
		return new BoolLeq(this, term);
	}

	public Term or(Term term) {
		return not().and(term.not()).not();
	}

	public Term not() {
		return new BoolNot(this);
	}

	public static Term and(Term[] subterms) {
		if (subterms == null)
			throw new IllegalArgumentException();

		if (subterms.length == 0)
			return new Constant(Domain.BOOL, 1);
		else {
			Term t = subterms[subterms.length - 1];
			for (int i = subterms.length - 2; i >= 0; i--)
				t = new BoolAnd(subterms[i], t);

			return t;
		}
	}

	public static Term forall(Table table, Term term) {
		return new ForAll(table, term);
	}

	public static Term exists(Table table, Term term) {
		// return new Exists(table, term);
		return new BoolNot(new ForAll(table, new BoolNot(term)));
	}

	public static Term count(Table table, Term term) {
		return new Counter(table, term);
	}

	public Term print(int trigger, Table... tables) {
		return new Printer(this, trigger, tables);
	}
}
