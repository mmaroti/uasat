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

public abstract class Table {
	protected final int[] table;
	private final Domain codomain;
	private final Domain domains[];
	private final String name;

	public static Table create(String name, Domain codomain, Domain... domains) {
		if (domains == null)
			throw new IllegalArgumentException();

		if (domains.length == 0)
			return new Nullary(name, codomain);
		else if (domains.length == 1)
			return new Unary(name, codomain, domains);
		else if (domains.length == 2)
			return new Binary(name, codomain, domains);
		else if (domains.length == 3)
			return new Ternary(name, codomain, domains);
		else
			return new Moreary(name, codomain, domains);
	}

	public Term of(Term... terms) {
		return Apply.create(this, terms);
	}

	private Table(String name, Domain codomain, Domain[] domains) {
		if (name == null || codomain == null)
			throw new IllegalArgumentException();
		assert domains != null;

		long length = 1;
		for (int i = 0; i < domains.length; i++) {
			length *= domains[i].getSize();
			if (length >= Integer.MAX_VALUE)
				throw new IllegalArgumentException();
		}

		this.table = new int[(int) length];
		Arrays.fill(table, Integer.MIN_VALUE);

		this.codomain = codomain;
		this.domains = domains;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public int getArity() {
		return domains.length;
	}

	public Domain getDomain(int index) {
		return domains[index];
	}

	public Domain getCodomain() {
		return codomain;
	}

	public int getSize() {
		return codomain.getSize();
	}

	public int[] getTable() {
		return table;
	}

	public int getLength() {
		return table.length;
	}

	@Override
	public String toString() {
		String s = name + ":";

		for (int i = 0; i < table.length; i++)
			s += " " + table[i];

		return s;
	}

	static class Nullary extends Table {
		private static Domain[] DOMAINS = new Domain[0];

		private Nullary(String name, Domain codomain) {
			super(name, codomain, DOMAINS);
		}

		public int evaluate() {
			return table[0];
		}
	}

	static class Unary extends Table {
		private Unary(String name, Domain codomain, Domain[] domains) {
			super(name, codomain, domains);
			assert domains.length == 1;
		}

		public int evaluate(int arg) {
			return table[arg];
		}
	}

	static class Binary extends Table {
		private int step1;

		private Binary(String name, Domain codomain, Domain[] domains) {
			super(name, codomain, domains);
			assert domains.length == 2;

			step1 = domains[0].getSize();
		}

		public int evaluate(int arg0, int arg1) {
			return table[arg0 + arg1 * step1];
		}
	}

	static class Ternary extends Table {
		private int step1;
		private int step2;

		private Ternary(String name, Domain codomain, Domain[] domains) {
			super(name, codomain, domains);
			assert domains.length == 3;

			step1 = domains[0].getSize();
			step2 = step1 * domains[1].getSize();
		}

		public int evaluate(int arg0, int arg1, int arg2) {
			return table[arg0 + arg1 * step1 + arg2 * step2];
		}
	}

	static class Moreary extends Table {
		private final int[] steps;

		private Moreary(String name, Domain codomain, Domain[] domains) {
			super(name, codomain, domains);
			assert domains.length >= 4;

			steps = new int[domains.length];

			int s = 1;
			for (int i = 0; i < domains.length; i++) {
				steps[i] = s;
				s *= domains[i].getSize();
			}
		}

		public int evaluate(int[] args) {
			assert args.length == steps.length;

			int p = 0;
			for (int i = 0; i < args.length; i++)
				p += args[i] * steps[i];

			return table[p];
		}
	}
}
