/**
 * Copyright (C) Miklos Maroti, 2017
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

package org.uasat2.core;

import java.util.*;

public abstract class Domain {
	public abstract boolean isSmall();

	/**
	 * @return the size of the small closed domain
	 * @throws InvalidStateException
	 *             if the domain it not small or not closed
	 */
	public abstract int getSize();

	/**
	 * @return true if the domain contains no domain variables
	 */
	public abstract boolean isClosed();

	/**
	 * @return the list of domain variable names
	 */
	public List<String> getVariables() {
		List<String> names = new ArrayList<String>();
		addVariables(names);
		return names;
	}

	protected abstract void addVariables(List<String> names);

	/**
	 * @return true if the two domains are structurally the same and the domain
	 *         variables are named the same
	 */
	@Override
	public abstract boolean equals(Object other);

	public static class Primitive extends Domain {
		public final int size;

		public Primitive(int size) {
			assert size >= 0;
			this.size = size;
		}

		@Override
		public boolean isSmall() {
			return true;
		}

		@Override
		public int getSize() {
			return size;
		}

		@Override
		public boolean isClosed() {
			return true;
		}

		@Override
		protected void addVariables(List<String> names) {
		}

		@Override
		public boolean equals(Object other) {
			if (other instanceof Primitive)
				return size == ((Primitive) other).size;
			else
				return false;
		}
	}

	public static class Product extends Domain {
		public final Domain[] factors;

		public Product(Domain... factors) {
			this.factors = factors;
		}

		@Override
		public boolean isSmall() {
			for (Domain factor : factors)
				if (!factor.isSmall())
					return false;
			return true;
		}

		@Override
		public int getSize() {
			int s = 1;
			for (Domain factor : factors)
				s *= factor.getSize();
			return s;
		}

		@Override
		protected void addVariables(List<String> names) {
			for (Domain factor : factors)
				factor.addVariables(names);
		}

		@Override
		public boolean isClosed() {
			for (Domain factor : factors)
				if (!factor.isClosed())
					return false;
			return true;
		}

		@Override
		public boolean equals(Object other) {
			if (other instanceof Product) {
				Domain[] f = ((Product) other).factors;
				if (factors.length == f.length) {
					for (int i = 0; i < factors.length; i++)
						if (!factors[i].equals(f[i]))
							return false;
					return true;
				}
			}
			return false;
		}
	}

	public static class Union extends Domain {
		public final Domain[] factors;

		public Union(Domain... factors) {
			this.factors = factors;
		}

		@Override
		public boolean isSmall() {
			for (Domain factor : factors)
				if (!factor.isSmall())
					return false;
			return true;
		}

		@Override
		public int getSize() {
			int s = 0;
			for (Domain factor : factors)
				s += factor.getSize();
			return s;
		}

		@Override
		public boolean isClosed() {
			for (Domain factor : factors)
				if (!factor.isClosed())
					return false;
			return true;
		}

		@Override
		protected void addVariables(List<String> names) {
			for (Domain factor : factors)
				factor.addVariables(names);
		}

		@Override
		public boolean equals(Object other) {
			if (other instanceof Union) {
				Domain[] f = ((Union) other).factors;
				if (factors.length == f.length) {
					for (int i = 0; i < factors.length; i++)
						if (!factors[i].equals(f[i]))
							return false;
					return true;
				}
			}
			return false;
		}
	}

	public static class Power extends Domain {
		public final Domain domain;

		public Power(Domain domain) {
			this.domain = domain;
		}

		@Override
		public boolean isSmall() {
			return false;
		}

		@Override
		public int getSize() {
			throw new IllegalStateException();
		}

		@Override
		public boolean isClosed() {
			return domain.isClosed();
		}

		@Override
		protected void addVariables(List<String> names) {
			domain.addVariables(names);
		}

		@Override
		public boolean equals(Object other) {
			if (other instanceof Power)
				return domain.equals(((Power) other).domain);
			else
				return false;
		}
	}

	public static class Variable extends Domain {
		public final String name;

		public Variable(String name) {
			this.name = name;
		}

		@Override
		public boolean isSmall() {
			return true;
		}

		@Override
		public int getSize() {
			throw new IllegalStateException();
		}

		@Override
		public boolean isClosed() {
			return true;
		}

		@Override
		protected void addVariables(List<String> names) {
			if (!names.contains(name))
				names.add(name);
		}

		@Override
		public boolean equals(Object other) {
			if (other instanceof Variable)
				return name.equals(((Variable) other).name);
			else
				return false;
		}
	}
}
