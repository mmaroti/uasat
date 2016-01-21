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

public final class Algebra<BOOL> {
	private final BoolAlgebra<BOOL> alg;
	private final int size;
	private final List<Operation<BOOL>> ops;

	public BoolAlgebra<BOOL> getAlg() {
		return alg;
	}

	public int getSize() {
		return size;
	}

	public List<Operation<BOOL>> getOperations() {
		return ops;
	}

	public Algebra(BoolAlgebra<BOOL> alg, int size, List<Operation<BOOL>> ops) {
		assert alg != null && !ops.isEmpty();

		for (Operation<BOOL> op : ops)
			assert op.getSize() == size;

		this.alg = alg;
		this.size = size;
		this.ops = ops;
	}

	@SafeVarargs
	public Algebra(BoolAlgebra<BOOL> alg, Operation<BOOL>... ops) {
		assert alg != null && ops.length > 0;

		size = ops[0].getSize();
		for (int i = 1; i < ops.length; i++)
			assert ops[i].getSize() == size;

		this.alg = alg;
		this.ops = Arrays.asList(ops);
	}

	@SafeVarargs
	final public Algebra<BOOL> extend(Operation<BOOL>... ops) {
		List<Operation<BOOL>> list = new ArrayList<Operation<BOOL>>(this.ops);

		for (int i = 0; i < ops.length; i++) {
			assert ops[i].getSize() == size && ops[i].getAlg() == alg;
			list.add(ops[i]);
		}

		return new Algebra<BOOL>(alg, size, list);
	}

	public BOOL isSubuniverse(Relation<BOOL> rel) {
		assert alg == rel.getAlg() && size == rel.getSize();

		BOOL b = alg.TRUE;
		for (Operation<BOOL> op : ops)
			b = alg.and(b, op.preserves(rel));

		return b;
	}

	public BOOL isCompatibleWith(Structure<BOOL> str) {
		assert alg == str.getAlg() && size == str.getSize();

		BOOL b = alg.TRUE;
		for (Relation<BOOL> rel : str.getRelations())
			b = alg.and(b, isSubuniverse(rel));

		return b;
	}

	public boolean hasConstants() {
		for (Operation<BOOL> op : ops)
			if (op.getArity() == 0)
				return true;

		return false;
	}

	public static <BOOL> Algebra<BOOL> lift(BoolAlgebra<BOOL> alg,
			Algebra<Boolean> ua) {
		List<Operation<BOOL>> ops = new ArrayList<Operation<BOOL>>();
		for (Operation<Boolean> op : ua.ops)
			ops.add(Operation.lift(alg, op));

		return new Algebra<BOOL>(alg, ua.getSize(), ops);
	}

	@SafeVarargs
	public static Algebra<Boolean> wrap(Operation<Boolean>... ops) {
		return new Algebra<Boolean>(BoolAlgebra.INSTANCE, ops);
	}

	public static void print(Algebra<Boolean> ua) {
		List<Operation<Boolean>> ops = ua.getOperations();
		System.out.println("algebra of size " + ua.getSize() + " with "
				+ ops.size() + " ops");
		for (int i = 0; i < ops.size(); i++)
			Operation.print(ops.get(i));
		System.out.println();
	}
}
