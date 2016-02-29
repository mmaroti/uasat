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

import java.text.*;
import java.util.*;

import org.uasat.core.*;
import org.uasat.math.*;

public class TransMon {
	protected static DecimalFormat TIME_FORMAT = new DecimalFormat("0.00");

	private final int size;
	private final List<Operation<Boolean>> transforms;
	private final List<Operation<Boolean>> selected;

	public TransMon(int size, String selected) {
		this.size = size;

		Structure<Boolean> str = new Structure<Boolean>(BoolAlgebra.INSTANCE,
				size);
		CompatibleOps com = new CompatibleOps(str);
		transforms = com.findUnaryOps("", -1);
		this.selected = new ArrayList<Operation<Boolean>>();

		for (String s : selected.split(" ")) {
			Operation<Boolean> op = Operation.parse(size, 1, s);
			assert transforms.contains(op) && !this.selected.contains(op);
			this.selected.add(op);
		}
	}

	public void print() {
		System.out.println("transformation monoid on " + size);

		System.out.println("selected ops " + selected.size() + ":");
		for (int i = 0; i < selected.size(); i++)
			System.out.println(i + ":\t" + Operation.format(selected.get(i)));
	}

	public List<boolean[]> findMonoids(final boolean[] mask) {
		assert mask.length == selected.size();

		SatProblem problem = new SatProblem(new int[] { transforms.size() }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Tensor<BOOL> tensor = tensors.get(0);

				BOOL b = alg.TRUE;
				for (int i = 0; i < selected.size(); i++) {
					int j = transforms.indexOf(selected.get(i));
					BOOL c = tensor.getElem(j);

					b = alg.and(b, mask[i] ? c : alg.not(c));
					tensor.setElem(alg.lift(mask[i]), j);
				}

				for (int i = 0; i < transforms.size(); i++) {
					Operation<Boolean> op1 = transforms.get(i);

					BOOL c = alg.TRUE;
					for (int j = 0; j < transforms.size(); j++) {
						Operation<Boolean> op2 = transforms.get(j);
						int k = transforms.indexOf(op1.compose(op2));

						c = alg.and(c,
								alg.leq(tensor.getElem(j), tensor.getElem(k)));
					}

					b = alg.and(b, alg.leq(tensor.getElem(i), c));
				}

				return b;
			}
		};

		Tensor<Boolean> sols = problem.solveAll(SatSolver.getDefault(), 100).get(0);

		List<boolean[]> list = new ArrayList<boolean[]>();
		for (int i = 0; i < sols.getDim(1); i++) {
			boolean[] s = new boolean[sols.getDim(0)];
			for (int j = 0; j < sols.getDim(0); j++)
				s[j] = sols.getElem(j, i);
			list.add(s);
		}

		return list;
	}

	public static void main(String[] args) {
		TransMon m = new TransMon(4, "0123 0000 1111 2222 3333 0100 0020 0003 0111 1121 1131 0222 2122 2223 0333 3133 3323");
		m.print();
		System.out.println(m.findMonoids(new boolean[17]).size());
	}
}
