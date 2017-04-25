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

public class SmallElement {
	public final Domain domain;
	public final int element;

	public SmallElement(Domain domain, int element) {
		assert domain.isSmall() && 0 <= element && element < domain.getSize();

		this.domain = domain;
		this.element = element;
	}

	public static SmallElement projection(Domain domain, int index) {
		return null;
	}

	public SmallElement apply(SmallElement argument) {
		assert domain instanceof Domain.Function;
		Domain.Function d = (Domain.Function) domain;
		assert d.argument.equals(argument.domain);

		int a = element;
		int b = d.result.getSize();
		for (int i = 1; i < argument.element; i++)
			a = a / b;
		a = a % b;

		return new SmallElement(d.result, a);
	}

	public SmallElement compose(SmallElement arguments) {
		return null;
	}

	public static SmallElement tuple(SmallElement... elements) {
		Domain[] factors = new Domain[elements.length];
		int element = 0;
		for (int i = elements.length - 1; i >= 0; i--) {
			SmallElement e = elements[i];
			Domain d = e.domain;

			factors[i] = d;
			element = element * d.getSize() + e.element;
		}

		Domain domain = new Domain.Product(factors);
		return new SmallElement(domain, element);
	}

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		Domain BOOLEAN = new Domain.Primitive(2);
		SmallElement FALSE = new SmallElement(BOOLEAN, 0);
		SmallElement TRUE = new SmallElement(BOOLEAN, 1);

		Domain UNARIES = new Domain.Function(new Domain.Function(
				new Domain.Primitive(1), BOOLEAN), BOOLEAN);
		SmallElement NEG = new SmallElement(UNARIES, 1);
		SmallElement ID = new SmallElement(UNARIES, 2);

		Domain BINARIES = new Domain.Function(new Domain.Function(
				new Domain.Primitive(2), BOOLEAN), BOOLEAN);
		SmallElement AND = new SmallElement(BINARIES, 8);
		SmallElement OR = new SmallElement(BINARIES, 14);
		SmallElement EQU = new SmallElement(BINARIES, 9);
	}
}
