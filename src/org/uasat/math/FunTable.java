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

import org.uasat.core.*;

public final class FunTable<BOOL> {
	private final BoolAlgebra<BOOL> alg;
	private final Tensor<BOOL> tensor;

	public FunTable(BoolAlgebra<BOOL> alg, Tensor<BOOL> tensor) {
		assert tensor.getOrder() == 3;

		this.alg = alg;
		this.tensor = tensor;
	}

	public FunTable(BoolAlgebra<BOOL> alg, int codomain, int domain) {
		assert codomain >= 1 && domain >= 1;

		this.alg = alg;
		this.tensor = Tensor.constant(new int[] { codomain, domain, 0 },
				alg.FALSE);
	}

	public BoolAlgebra<BOOL> getAlg() {
		return alg;
	}

	public Tensor<BOOL> getTensor() {
		return tensor;
	}

	public int getCodomain() {
		return tensor.getDim(0);
	}

	public int getDomain() {
		return tensor.getDim(1);
	}

	public int getCount() {
		return tensor.getDim(2);
	}

	public BOOL isValid() {
		Tensor<BOOL> t = Tensor.fold(alg.ONE, 1, tensor);
		return Tensor.fold(alg.ALL, 2, t).get();
	}

	public FunTable<BOOL> concat(FunTable<BOOL> funs) {
		assert funs.getAlg() == alg && funs.getCodomain() == getCodomain()
				&& funs.getDomain() == getDomain();

		return new FunTable<BOOL>(alg, Tensor.concat(tensor, funs.tensor));
	}
}
