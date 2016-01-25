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

public abstract class Table extends Function {
	protected final int[] table;

	public static Table create(Domain codomain, Domain... domains) {
		if (domains == null)
			throw new IllegalArgumentException();

		if (domains.length == 0)
			return new Table0(codomain);
		else if (domains.length == 1)
			return new Table1(codomain, domains);
		else if (domains.length == 2)
			return new Table2(codomain, domains);
		else if (domains.length == 3)
			return new Table3(codomain, domains);
		else
			return new TableN(codomain, domains);
	}

	public Table copy() {
		Table t = create(codomain, domains);
		assert table.length == t.table.length;
		System.arraycopy(table, 0, t.table, 0, table.length);

		return t;
	}

	protected Table(Domain codomain, Domain[] domains) {
		super(codomain, domains);

		long length = 1;
		for (int i = 0; i < domains.length; i++) {
			length *= domains[i].getSize();
			if (length >= Integer.MAX_VALUE)
				throw new IllegalArgumentException();
		}

		this.table = new int[(int) length];
		Arrays.fill(table, Integer.MIN_VALUE);
	}

	public int[] getTable() {
		return table;
	}

	public Term get(int... coords) {
		int p = getIndex(coords);
		return new Peek(this, p);
	}

	public int getValue(int... coords) {
		return table[getIndex(coords)];
	}

	protected int getIndex(int... coords) {
		if (coords.length != domains.length)
			throw new IllegalArgumentException();

		int s = 1, a = 0;
		for (int i = 0; i < domains.length; i++) {
			int b = domains[i].getSize();
			int c = coords[i];
			if (c < 0 || c >= b)
				throw new IllegalArgumentException();

			a += s * c;
			s *= b;
		}

		return a;
	}

	@Override
	public String toString() {
		String s = "";

		for (int i = 0; i < table.length; i++)
			s += " " + table[i];

		return s;
	}

	static class Peek extends Term {
		private Table table;
		private final int index;

		Peek(Table table, int index) {
			this.table = table;
			this.index = index;
		}

		@Override
		public int $evaluate() {
			return table.table[index];
		}

		@Override
		public Domain getDomain() {
			return table.getCodomain();
		}

		@Override
		public int getBound() {
			return 0;
		}
	}

	static class Table0 extends Table {
		private static Domain[] DOMAINS = new Domain[0];

		private Table0(Domain codomain) {
			super(codomain, DOMAINS);
		}

		public int $evaluate() {
			return table[0];
		}

		@Override
		public Term of(Term... subterms) {
			if (subterms.length != 0)
				throw new IllegalArgumentException();

			return new Term0(this);
		}
	}

	private static class Term0 extends Term {
		private final Table0 table;

		public Term0(Table0 table) {
			this.table = table;
		}

		@Override
		public Domain getDomain() {
			return table.getCodomain();
		}

		@Override
		public int $evaluate() {
			return table.$evaluate();
		}

		@Override
		public int getBound() {
			return 0;
		}
	}

	static class Table1 extends Table {
		private Table1(Domain codomain, Domain[] domains) {
			super(codomain, domains);
			assert domains.length == 1;
		}

		public int $evaluate(int arg) {
			return table[arg];
		}

		@Override
		public Term of(Term... subterms) {
			if (subterms.length != 1)
				throw new IllegalArgumentException();

			return new Term1(this, subterms[0]);
		}
	}

	static class Term1 extends Term {
		private final Table1 table;
		private final Term subterm;

		public Term1(Table1 table, Term subterm) {
			if (subterm == null)
				throw new IllegalArgumentException();

			this.table = table;
			this.subterm = subterm;
		}

		@Override
		public Domain getDomain() {
			return table.getCodomain();
		}

		@Override
		public int $evaluate() {
			int a = subterm.$evaluate();
			if (a < 0)
				return a;

			return table.$evaluate(a);
		}

		@Override
		public int getBound() {
			return subterm.getBound();
		}
	}

	static class Table2 extends Table {
		private int step1;

		private Table2(Domain codomain, Domain[] domains) {
			super(codomain, domains);
			assert domains.length == 2;

			step1 = domains[0].getSize();
		}

		public int $evaluate(int arg0, int arg1) {
			return table[arg0 + arg1 * step1];
		}

		@Override
		public Term of(Term... subterms) {
			if (subterms.length != 2)
				throw new IllegalArgumentException();

			return new Term2(this, subterms[0], subterms[1]);
		}
	}

	static class Term2 extends Term {
		private final Table2 table;
		private final Term subterm0;
		private final Term subterm1;

		public Term2(Table2 table, Term subterm0, Term subterm1) {
			if (subterm0 == null || subterm1 == null)
				throw new IllegalArgumentException();

			this.table = table;
			this.subterm0 = subterm0;
			this.subterm1 = subterm1;
		}

		@Override
		public Domain getDomain() {
			return table.getCodomain();
		}

		@Override
		public int $evaluate() {
			int a = subterm0.$evaluate();
			if (a < 0)
				return a;

			int b = subterm1.$evaluate();
			if (b < 0)
				return b;

			return table.$evaluate(a, b);
		}

		@Override
		public int getBound() {
			int a = subterm0.getBound();
			int b = subterm1.getBound();
			return a <= b ? a : b;
		}
	}

	static class Table3 extends Table {
		private int step1;
		private int step2;

		private Table3(Domain codomain, Domain[] domains) {
			super(codomain, domains);
			assert domains.length == 3;

			step1 = domains[0].getSize();
			step2 = step1 * domains[1].getSize();
		}

		public int $evaluate(int arg0, int arg1, int arg2) {
			return table[arg0 + arg1 * step1 + arg2 * step2];
		}

		@Override
		public Term of(Term... subterms) {
			if (subterms.length != 3)
				throw new IllegalArgumentException();

			return new Term3(this, subterms[0], subterms[1], subterms[2]);
		}
	}

	static class Term3 extends Term {
		private final Table3 table;
		private final Term subterm0;
		private final Term subterm1;
		private final Term subterm2;

		public Term3(Table3 table, Term subterm0, Term subterm1, Term subterm2) {
			if (subterm0 == null || subterm1 == null || subterm2 == null)
				throw new IllegalArgumentException();

			this.table = table;
			this.subterm0 = subterm0;
			this.subterm1 = subterm1;
			this.subterm2 = subterm2;
		}

		@Override
		public Domain getDomain() {
			return table.getCodomain();
		}

		@Override
		public int $evaluate() {
			int a = subterm0.$evaluate();
			if (a < 0)
				return a;

			int b = subterm1.$evaluate();
			if (b < 0)
				return b;

			int c = subterm2.$evaluate();
			if (c < 0)
				return c;

			return table.$evaluate(a, b, c);
		}

		@Override
		public int getBound() {
			int a = subterm0.getBound();

			int b = subterm1.getBound();
			if (b < a)
				a = b;

			b = subterm2.getBound();
			if (b < a)
				a = b;

			return a;
		}
	}

	static class TableN extends Table {
		private final int[] steps;

		private TableN(Domain codomain, Domain[] domains) {
			super(codomain, domains);
			assert domains.length >= 4;

			steps = new int[domains.length];

			int s = 1;
			for (int i = 0; i < domains.length; i++) {
				steps[i] = s;
				s *= domains[i].getSize();
			}
		}

		public int $evaluate(int[] args) {
			assert args.length == steps.length;

			int p = 0;
			for (int i = 0; i < args.length; i++)
				p += args[i] * steps[i];

			return table[p];
		}

		@Override
		public Term of(Term... subterms) {
			if (subterms == null)
				throw new IllegalArgumentException();

			return new TermN(this, subterms);
		}
	}

	static class TermN extends Term {
		private final TableN table;
		private final Term[] subterms;
		private final int[] args;

		public TermN(TableN table, Term[] subterms) {
			for (int i = 0; i < subterms.length; i++)
				if (subterms[i] == null)
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
		public int $evaluate() {
			for (int i = 0; i < args.length; i++) {
				int a = subterms[i].$evaluate();
				if (a < 0)
					return a;

				args[i] = a;
			}

			return table.$evaluate(args);
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
