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

public final class Structure<BOOL> {
	private final BoolAlgebra<BOOL> alg;
	private final int size;
	private final List<Relation<BOOL>> relations;

	public BoolAlgebra<BOOL> getAlg() {
		return alg;
	}

	public int getSize() {
		return size;
	}

	public List<Relation<BOOL>> getRelations() {
		return relations;
	}

	public Relation<BOOL> getRelation(int index) {
		return relations.get(index);
	}

	public void add(Relation<BOOL> rel) {
		assert rel.getSize() == size;
		relations.add(rel);
	}

	public Structure(BoolAlgebra<BOOL> alg, int size, List<Relation<BOOL>> rels) {
		assert alg != null;

		for (Relation<BOOL> rel : rels)
			assert rel.getSize() == size;

		this.alg = alg;
		this.size = size;
		this.relations = rels;
	}

	@SafeVarargs
	public Structure(BoolAlgebra<BOOL> alg, Relation<BOOL>... rels) {
		assert alg != null && rels.length > 0;

		size = rels[0].getSize();
		for (int i = 1; i < rels.length; i++)
			assert rels[i].getSize() == size;

		this.alg = alg;
		this.relations = Arrays.asList(rels);
	}

	@SafeVarargs
	final public Structure<BOOL> extend(Relation<BOOL>... rels) {
		List<Relation<BOOL>> list = new ArrayList<Relation<BOOL>>(
				this.relations);

		for (int i = 0; i < rels.length; i++) {
			assert rels[i].getSize() == size && rels[i].getAlg() == alg;
			list.add(rels[i]);
		}

		return new Structure<BOOL>(alg, size, list);
	}

	public BOOL isCompatibleWith(Operation<BOOL> op) {
		assert alg == op.getAlg() && size == op.getSize();

		BOOL b = alg.TRUE;
		for (Relation<BOOL> rel : relations)
			b = alg.and(b, op.preserves(rel));

		return b;
	}

	public BOOL isCompatibleWith(Algebra<BOOL> ua) {
		return ua.isCompatibleWith(this);
	}

	public Structure<BOOL> makeComplexStructure(List<Relation<BOOL>> subsets) {
		List<Relation<BOOL>> rels = new ArrayList<Relation<BOOL>>();
		for (Relation<BOOL> rel : relations)
			rels.add(rel.makeComplexRelation(subsets));

		return new Structure<BOOL>(alg, subsets.size(), rels);
	}

	public static <BOOL> Structure<BOOL> lift(BoolAlgebra<BOOL> alg,
			Structure<Boolean> str) {
		List<Relation<BOOL>> ops = new ArrayList<Relation<BOOL>>();
		for (Relation<Boolean> op : str.relations)
			ops.add(Relation.lift(alg, op));

		return new Structure<BOOL>(alg, str.getSize(), ops);
	}

	@SafeVarargs
	public static Structure<Boolean> wrap(Relation<Boolean>... rels) {
		return new Structure<Boolean>(BoolAlgebra.INSTANCE, rels);
	}

	public static Structure<Boolean> trivial(int size) {
		return new Structure<Boolean>(BoolAlgebra.INSTANCE, size,
				new ArrayList<Relation<Boolean>>());
	}

	public static void print(Structure<Boolean> str) {
		List<Relation<Boolean>> rels = str.relations;
		System.out.println("structure of size " + str.getSize() + " with "
				+ rels.size() + " rels");
		for (int i = 0; i < rels.size(); i++)
			Relation.print(rels.get(i));
		System.out.println();
	}
}
