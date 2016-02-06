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

public abstract class Function {
	protected final Domain codomain;
	protected final Domain domains[];

	protected Function(Domain codomain, Domain... domains) {
		if (codomain == null || domains == null)
			throw new IllegalArgumentException();

		for (int i = 0; i < domains.length; i++)
			if (domains[i] == null)
				throw new IllegalArgumentException();

		this.codomain = codomain;
		this.domains = domains;
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

	abstract public Term of(Term... subterms);
}
