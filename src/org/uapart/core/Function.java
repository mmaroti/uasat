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

public abstract class Function {
	protected final int[] table;
	private final Domain domains[];
	private final Domain codomain;
	private final String name;

	private Function(String name, Domain[] domains, Domain codomain) {
		if (name == null || domains == null || codomain == null)
			throw new IllegalArgumentException();

		long length = 1;
		for (int i = 0; i < domains.length; i++) {
			length *= domains[i].getSize();
			if (length >= Integer.MAX_VALUE)
				throw new IllegalArgumentException();
		}

		this.table = new int[(int) length];
		Arrays.fill(table, Integer.MIN_VALUE);

		this.domains = domains;
		this.codomain = codomain;
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

	public static class Nullary extends Function {
		private static Domain[] DOMAINS = new Domain[0];

		public Nullary(String name, Domain codomain) {
			super(name, DOMAINS, codomain);
		}

		public int evaluate() {
			return table[0];
		}
	}

	public static class Unary extends Function {
		public Unary(String name, Domain domain, Domain codomain) {
			super(name, new Domain[] { domain }, codomain);
		}

		public int evaluate(int arg) {
			return table[arg];
		}
	}

	public static class Binary extends Function {
		private int step1;

		public Binary(String name, Domain domain0, Domain domain1,
				Domain codomain) {
			super(name, new Domain[] { domain0, domain1 }, codomain);

			step1 = domain0.getSize();
		}

		public int evaluate(int arg0, int arg1) {
			return table[arg0 + arg1 * step1];
		}
	}

	public static class Ternary extends Function {
		private int step1;
		private int step2;

		public Ternary(String name, Domain domain0, Domain domain1,
				Domain domain2, Domain codomain) {
			super(name, new Domain[] { domain0, domain1, domain2 }, codomain);

			step1 = domain0.getSize();
			step2 = step1 * domain1.getSize();
		}

		public int evaluate(int arg0, int arg1, int arg2) {
			return table[arg0 + arg1 * step1 + arg2 * step2];
		}
	}

	public static class Moreary extends Function {
		private final int[] steps;

		public Moreary(String name, Domain[] domains, Domain codomain) {
			super(name, domains, codomain);

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
