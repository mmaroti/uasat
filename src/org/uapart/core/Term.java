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
	public abstract int evaluate();

	/**
	 * Returns the minimum rank of bound variables within this expression.
	 * 
	 * @return the minimum rank of bound variables within this expression.
	 */
	public abstract int getBound();

	public Term and(Term term) {
		return new BoolAnd(this, term);
	}

	public Term equ(Term term) {
		return new BoolEqu(this, term);
	}

	public Term leq(Term term) {
		return new BoolLeq(this, term);
	}

	public Term neq(Term term) {
		return new BoolNeq(this, term);
	}

	public Term or(Term term) {
		return new BoolOr(this, term);
	}

	public Term not() {
		return new BoolNot(this);
	}

	public Term forall(Table table) {
		return new ForAll(table, this);
	}

	public Term exists(Table table) {
		return new Exists(table, this);
	}

	public Term count(Table table) {
		return new Counter(table, this);
	}

	public Term print(int trigger, Table... tables) {
		return new Printer(this, trigger, tables);
	}
}
