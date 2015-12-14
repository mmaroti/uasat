/**
 *	Copyright (C) Miklos Maroti, 2015
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

public final class RelTable {
	private int size;
	private int arity;
	private final List<Tensor<Boolean>> tensors;

	public RelTable(int size, int arity) {
		this.size = size;
		this.arity = arity;
		this.tensors = new ArrayList<Tensor<Boolean>>();
	}

	public int getSize() {
		return size;
	}

	public int getArity() {
		return arity;
	}

	public int getCount() {
		return tensors.size();
	}

	public void add(Relation<Boolean> rel) {
		assert rel.getAlg() == BoolAlgebra.INSTANCE && rel.getSize() == size
				&& rel.getArity() == arity;

		if (!tensors.contains(rel.getTensor()))
			tensors.add(rel.getTensor());
	}

	public Relation<Boolean> get(int index) {
		return Relation.wrap(tensors.get(index));
	}

	public <BOOL> BOOL contains(Relation<BOOL> rel) {
		assert rel.getSize() == size && rel.getArity() == arity;
		BoolAlgebra<BOOL> alg = rel.getAlg();

		BOOL b = alg.TRUE;
		for (Tensor<Boolean> tensor : tensors) {
			Relation<BOOL> tmp = Relation.lift(alg, Relation.wrap(tensor));
			b = alg.and(b, tmp.isEqualTo(rel));
		}

		return b;
	}

	public void addSingletons() {
		
		add(Relation.full(size, arity));
	}
}
