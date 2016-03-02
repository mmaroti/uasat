/**
 *	Copyright (C) Miklos Maroti, 2015-2016
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

package org.uasat.math;

import java.util.*;

import org.uasat.core.*;

public class MeetClosedRels {
	private final int size;
	private final int arity;
	private final List<Permutation<Boolean>> perms;

	private final List<Relation<Boolean>> gens;
	private final List<Relation<Boolean>> covers;

	public MeetClosedRels(int size, int arity) {
		assert size >= 1 && arity >= 1;

		this.size = size;
		this.arity = arity;

		perms = Permutation.symmetricGroup(arity);
		gens = new ArrayList<Relation<Boolean>>();
		covers = new ArrayList<Relation<Boolean>>();
	}

	public int getSize() {
		return size;
	}

	public int getArity() {
		return arity;
	}

	public void addGenerator(Relation<Boolean> rel) {
		assert rel.getSize() == size && rel.getArity() == arity;

		if (gens.contains(rel))
			return;

		Relation<Boolean> cov = Relation.full(size, arity);
		for (int i = 0; i < gens.size(); i++) {
			Relation<Boolean> gen = gens.get(i);
			if (gen.isSubsetOf(rel)) {
				assert !rel.isSubsetOf(gen);
				covers.set(i, covers.get(i).intersect(rel));
			} else if (rel.isSubsetOf(gen))
				cov = cov.intersect(gen);
		}

		gens.add(rel);
		covers.add(cov);
	}

	public void addCriticalGen(Relation<Boolean> rel) {
		assert rel.getSize() == size && rel.getArity() <= arity;

		if (rel.getArity() < arity)
			rel = rel.cartesian(Relation.full(size, arity - rel.getArity()));

		for (Permutation<Boolean> p : perms)
			addGenerator(rel.permute(p));
	}

	public void removeMeetReducibles() {
		for (int i = gens.size() - 1; i >= 0; i--) {
			if (covers.get(i).isEqualTo(gens.get(i))) {
				gens.remove(i);
				covers.remove(i);
			}
		}
	}

	public Relation<Boolean> getCoverRel(Relation<Boolean> rel) {
		assert rel.getSize() == size && rel.getArity() == arity;

		Relation<Boolean> cov = Relation.full(size, arity);
		for (Relation<Boolean> gen : gens) {
			if (rel.isSubsetOf(gen))
				cov = cov.intersect(gen);
		}

		return cov;
	}

	public boolean isMeetIrreducible(Relation<Boolean> rel) {
		return !getCoverRel(rel).isEqualTo(rel);
	}

	public List<Relation<Boolean>> getGenerators() {
		return gens;
	}

	public List<Relation<Boolean>> getCoverRels() {
		return covers;
	}

	public int getGeneratorCount() {
		return gens.size();
	}

	public <BOOL> BOOL isMemberOf(BoolAlgebra<BOOL> alg, Relation<BOOL> rel) {
		return null;
	}
}
