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

public abstract class Element {
	/**
	 * @return the domain to which this element belongs
	 */
	public abstract Domain getDomain();

	/**
	 * @return true if the element contains no element variables
	 */
	public abstract boolean isClosed();

	/**
	 * @return the list of element variable names
	 */
	public List<String> getVariables() {
		List<String> names = new ArrayList<String>();
		addVariables(names);
		return names;
	}

	protected abstract void addVariables(List<String> names);

	public static class Constant extends Element {
		public final Domain domain;
		public final int element;

		public Constant(Domain domain, int element) {
			this.domain = domain;
			this.element = element;

			assert domain.isSmall() && domain.isClosed();
			assert 0 <= element && element < domain.getSize();
		}

		@Override
		public Domain getDomain() {
			return domain;
		}

		@Override
		public boolean isClosed() {
			return true;
		}

		@Override
		protected void addVariables(List<String> names) {
		}
	}

	public static class Tuple extends Element {
		public final Element[] elements;

		public Tuple(Element[] elements) {
			this.elements = elements;
		}

		@Override
		public Domain getDomain() {
			Domain[] factors = new Domain[elements.length];
			for (int i = 0; i < elements.length; i++)
				factors[i] = elements[i].getDomain();
			return new Domain.Product(factors);
		}

		@Override
		public boolean isClosed() {
			for (Element element : elements)
				if (!element.isClosed())
					return false;
			return true;
		}

		@Override
		protected void addVariables(List<String> names) {
			for (Element element : elements)
				element.addVariables(names);
		}
	}

	public static class Apply extends Element {
		public final Element function;
		public final Element argument;

		public Apply(Element function, Element argument) {
			this.function = function;
			this.argument = argument;

			Domain f = function.getDomain();
			Domain a = argument.getDomain();
			assert f instanceof Domain.Function && ((Domain.Function) f).argument == a;
		}

		@Override
		public Domain getDomain() {
			return ((Domain.Function) function.getDomain()).result;
		}

		@Override
		public boolean isClosed() {
			return function.isClosed() && argument.isClosed();
		}

		@Override
		protected void addVariables(List<String> names) {
			function.addVariables(names);
			argument.addVariables(names);
		}
	}

	public static class Variable extends Element {
		public final String name;
		public final Domain domain;

		public Variable(String name, Domain domain) {
			this.name = name;
			this.domain = domain;
		}

		@Override
		public Domain getDomain() {
			return domain;
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
	}
}
