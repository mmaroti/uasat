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

public class GeneratedOps implements Iterable<Operation<Boolean>> {
	private final int size;
	private final int arity;
	private final TreeSet<Operation<Boolean>> operations;
	private final SatSolver<?> solver;

	public GeneratedOps(int size, int arity) {
		this(size, arity, SatSolver.getDefault());
	}

	public GeneratedOps(int size, int arity, SatSolver<?> solver) {
		assert size >= 1 && arity >= 0 && solver != null;

		this.size = size;
		this.arity = arity;
		this.operations = new TreeSet<Operation<Boolean>>(Operation.COMPARATOR);
		this.solver = solver;
	}

	public SatSolver<?> getSolver() {
		return solver;
	}

	public int getSize() {
		return size;
	}

	public int getArity() {
		return arity;
	}

	public List<Operation<Boolean>> getOperations() {
		return new ArrayList<Operation<Boolean>>(operations);
	}

	public int getCount() {
		return operations.size();
	}

	public void clear() {
		operations.clear();
	}

	public void add(Operation<Boolean> op) {
		assert op.getArity() == arity && op.getSize() == size;
		operations.add(op);
	}

	public void addAll(Iterable<Operation<Boolean>> ops) {
		for (Operation<Boolean> op : ops)
			add(op);
	}

	public void addProjections() {
		for (int i = 0; i < arity; i++)
			operations.add(Operation.projection(size, arity, i));
	}

	public void addCompositions() {
		for (;;) {
			int a = operations.size();

			List<Operation<Boolean>> ops = new ArrayList<Operation<Boolean>>(
					operations);

			@SuppressWarnings("unchecked")
			Operation<Boolean>[] args = new Operation[arity];

			for (int radius = 0; radius < ops.size(); radius++) {
				Iterator<int[]> iter = Util.hullIterator(radius, arity + 1);
				while (iter.hasNext()) {
					int[] index = iter.next();

					Operation<Boolean> op = ops.get(index[0]);
					for (int i = 0; i < args.length; i++)
						args[i] = ops.get(index[i + 1]);

					op = op.compose(args);
					if (operations.add(op))
						ops.add(op);
				}
			}

			if (a == operations.size())
				break;
		}
	}

	public boolean isCompositionClosed() {
		List<Operation<Boolean>> ops = new ArrayList<Operation<Boolean>>(
				operations);

		@SuppressWarnings("unchecked")
		Operation<Boolean>[] args = new Operation[arity];

		Iterator<int[]> iter = Util.cubeIterator(size, arity + 1);
		while (iter.hasNext()) {
			int[] index = iter.next();

			Operation<Boolean> op = ops.get(index[0]);
			for (int i = 0; i < args.length; i++)
				args[i] = ops.get(index[i + 1]);

			op = op.compose(args);
			if (!operations.contains(op))
				return false;
		}

		return true;
	}

	public void print() {
		System.out.println("generated ops of size " + size + " arity " + arity
				+ " count " + operations.size());

		int c = 0;
		for (Operation<Boolean> op : operations)
			System.out.println((c++) + ":\t" + Operation.formatTable(op));

		System.out.println();
	}

	@Override
	public Iterator<Operation<Boolean>> iterator() {
		return operations.iterator();
	}
}
