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

public abstract class Apply extends Term {
	public static Term create(Table table, Term... subterms) {
		if (table == null || subterms == null)
			throw new IllegalArgumentException();

		for (int i = 0; i < subterms.length; i++)
			if (subterms[i] == null)
				throw new IllegalArgumentException();

		int a = table.getArity();
		if (a == 0)
			return new Nullary((Table.Nullary) table, subterms);
		else if (a == 1)
			return new Unary((Table.Unary) table, subterms);
		else if (a == 2)
			return new Binary((Table.Binary) table, subterms);
		else if (a == 3)
			return new Ternary((Table.Ternary) table, subterms);
		else
			return new Moreary((Table.Moreary) table, subterms);
	}

	static class Nullary extends Apply {
		private final Table.Nullary table;

		public Nullary(Table.Nullary table, Term[] subterms) {
			if (subterms.length != 0)
				throw new IllegalArgumentException();

			this.table = table;
		}

		@Override
		public Domain getDomain() {
			return table.getCodomain();
		}

		@Override
		public int evaluate() {
			return table.evaluate();
		}

		@Override
		public int getBound() {
			return 0;
		}
	}

	static class Unary extends Apply {
		private final Table.Unary table;
		private final Term subterm;

		public Unary(Table.Unary table, Term[] subterms) {
			if (subterms.length != 1)
				throw new IllegalArgumentException();

			this.table = table;
			this.subterm = subterms[0];
		}

		@Override
		public Domain getDomain() {
			return table.getCodomain();
		}

		@Override
		public int evaluate() {
			int a = subterm.evaluate();
			if (a < 0)
				return a;

			return table.evaluate(a);
		}

		@Override
		public int getBound() {
			return subterm.getBound();
		}
	}

	static class Binary extends Apply {
		private final Table.Binary table;
		private final Term subterm0;
		private final Term subterm1;

		public Binary(Table.Binary table, Term[] subterms) {
			if (subterms.length != 2)
				throw new IllegalArgumentException();

			this.table = table;
			this.subterm0 = subterms[0];
			this.subterm1 = subterms[1];
		}

		@Override
		public Domain getDomain() {
			return table.getCodomain();
		}

		@Override
		public int evaluate() {
			int a = subterm0.evaluate();
			if (a < 0)
				return a;

			int b = subterm1.evaluate();
			if (b < 0)
				return b;

			return table.evaluate(a, b);
		}

		@Override
		public int getBound() {
			int a = subterm0.getBound();
			int b = subterm1.getBound();
			return a <= b ? a : b;
		}
	}

	static class Ternary extends Apply {
		private final Table.Ternary table;
		private final Term subterm0;
		private final Term subterm1;
		private final Term subterm2;

		public Ternary(Table.Ternary table, Term[] subterms) {
			if (subterms.length != 3)
				throw new IllegalArgumentException();

			this.table = table;
			this.subterm0 = subterms[0];
			this.subterm1 = subterms[1];
			this.subterm2 = subterms[2];
		}

		@Override
		public Domain getDomain() {
			return table.getCodomain();
		}

		@Override
		public int evaluate() {
			int a = subterm0.evaluate();
			if (a < 0)
				return a;

			int b = subterm1.evaluate();
			if (b < 0)
				return b;

			int c = subterm2.evaluate();
			if (c < 0)
				return c;

			return table.evaluate(a, b, c);
		}

		@Override
		public int getBound() {
			int a = subterm0.getBound();

			int b = subterm1.getBound();
			if (b < a)
				a = b;

			b = subterm2.getBound();
			if (b < 0)
				a = b;

			return a;
		}
	}

	static class Moreary extends Apply {
		private final Table.Moreary table;
		private final Term[] subterms;
		private final int[] args;

		public Moreary(Table.Moreary table, Term[] subterms) {
			if (subterms.length < 4)
				throw new IllegalArgumentException();

			this.table = table;
			this.subterms = subterms;
			this.args = new int[subterms.length];
		}

		@Override
		public Domain getDomain() {
			return table.getCodomain();
		}

		@Override
		public int evaluate() {
			for (int i = 0; i < args.length; i++) {
				int a = subterms[i].evaluate();
				if (a < 0)
					return a;

				args[i] = a;
			}

			return table.evaluate(args);
		}

		@Override
		public int getBound() {
			int b = 0;

			for (int i = 0; i < subterms.length; i++) {
				int a = subterms[i].getBound();
				if (a < b)
					b = a;
			}

			return b;
		}
	}
}
