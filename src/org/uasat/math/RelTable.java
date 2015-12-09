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

public final class RelTable<BOOL> {
	private final BoolAlgebra<BOOL> alg;
	private int size;
	private int arity;
	private final List<Tensor<BOOL>> tensors;

	public RelTable(BoolAlgebra<BOOL> alg, int size, int arity) {
		this.size = size;
		this.arity = arity;
		this.alg = alg;
		this.tensors = new ArrayList<Tensor<BOOL>>();
	}

	public BoolAlgebra<BOOL> getAlg() {
		return alg;
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

	public void add(Relation<BOOL> rel) {
		assert rel.getAlg() == alg && rel.getSize() == size
				&& rel.getArity() == arity;

		tensors.add(rel.getTensor());
	}

	public Relation<BOOL> get(int index) {
		return new Relation<BOOL>(alg, tensors.get(index));
	}

	public BOOL contains(Relation<BOOL> rel) {
		assert rel.getAlg() == alg && rel.getSize() == size
				&& rel.getArity() == arity;

		BOOL b = alg.TRUE;
		for (Tensor<BOOL> tensor : tensors) {
			Tensor<BOOL> tmp = Tensor.map2(alg.EQU, tensor, rel.getTensor());
			b = alg.and(b, Tensor.fold(alg.ALL, arity, tmp).get());
		}

		return b;
	}
}
