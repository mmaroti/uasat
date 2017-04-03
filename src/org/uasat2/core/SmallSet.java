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

// TODO: This is not good, we need terms and create the instance later!
public class SmallSet {
	private final SmallDomain domain;
	private final int[] members;
	private final Instance instance;

	public SmallDomain getDomain() {
		return domain;
	}

	private SmallSet(SmallDomain domain, int[] members, Instance instance) {
		assert domain.getSize() == members.length;

		this.domain = domain;
		this.members = members;
		this.instance = instance;
	}

	public SmallSet complement() {
		int[] memb = new int[members.length];

		for (int i = 0; i < memb.length; i++)
			memb[i] = Instance.not(members[i]);

		return new SmallSet(domain, memb, instance);
	}

	public SmallSet intersect(SmallSet set) {
		assert domain == set.domain;

		int[] memb = new int[members.length];
		Instance inst = Instance.join(instance, set.instance);

		for (int i = 0; i < memb.length; i++)
			memb[i] = inst.and(members[i], set.members[i]);

		return new SmallSet(domain, memb, inst);
	}

	public SmallSet union(SmallSet set) {
		assert domain == set.domain;

		int[] memb = new int[members.length];
		Instance inst = Instance.join(instance, set.instance);

		for (int i = 0; i < memb.length; i++)
			memb[i] = inst.and(members[i], set.members[i]);

		return new SmallSet(domain, memb, inst);
	}
}
