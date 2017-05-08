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

package org.uasat.research;

import org.uasat.core.*;
import org.uasat.math.*;

public class InfiniteSemiLat {
	public final int width;
	public final int halfHeight;
	public final int height;

	public final Relation<Boolean> outside;
	public final Relation<Boolean> downside;
	public final Relation<Boolean> upside;

	public InfiniteSemiLat(final int width, final int halfHeight) {
		assert width >= 1 && halfHeight >= 1;
		this.width = width;
		this.halfHeight = halfHeight;

		height = halfHeight + 1 + halfHeight;

		outside = Relation.wrap(Tensor.generate(width * height, new Func1<Boolean, Integer>() {
			@Override
			public Boolean call(Integer elem) {
				return elem < width * halfHeight || elem >= width * (1 + halfHeight);
			}
		}));

		downside = Relation.wrap(Tensor.generate(width * height, new Func1<Boolean, Integer>() {
			@Override
			public Boolean call(Integer elem) {
				return elem < width * 2 * halfHeight;
			}
		}));

		upside = Relation.wrap(Tensor.generate(width * height, new Func1<Boolean, Integer>() {
			@Override
			public Boolean call(Integer elem) {
				return elem >= width;
			}
		}));
	}

	public <BOOL> BOOL isTranslationInvariant(PartialOperation<BOOL> op) {
		BoolAlgebra<BOOL> alg = op.getAlg();

		Relation<BOOL> o = Relation.lift(alg, outside);
		BOOL b = op.evaluate(o).isSubsetOf(o);

		return b;
	}
}
