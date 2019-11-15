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

package org.uasat.core;

import java.util.*;

public class Contract<ELEM> {
	private static boolean track = false;
	private static int track_reshape = 0;
	private static int track_map2 = 0;
	private static int track_fold = 0;

	private final Func1<ELEM, Iterable<ELEM>> sum;
	private final Func2<ELEM, ELEM, ELEM> prod;

	private LinkedList<Object> varOrder = new LinkedList<Object>();
	private LinkedList<Entry<ELEM>> entries = new LinkedList<Entry<ELEM>>();

	public Contract(Func1<ELEM, Iterable<ELEM>> sum,
			Func2<ELEM, ELEM, ELEM> prod) {
		this.sum = sum;
		this.prod = prod;
	}

	public static <ELEM> Contract<ELEM> logical(BoolAlgebra<ELEM> alg) {
		return new Contract<ELEM>(alg.ANY, alg.AND);
	}

	public static <ELEM> Contract<ELEM> linear(BoolAlgebra<ELEM> alg) {
		return new Contract<ELEM>(alg.SUM, alg.AND);
	}

	private static String format(List<?> vars) {
		String s = new String();
		for (Object var : vars) {
			if (s.length() != 0)
				s += ' ';
			s += var.toString();
		}
		return s;
	}

	private static class Entry<ELEM> {
		public final Tensor<ELEM> tensor;
		public final List<?> vars;

		public Entry(Tensor<ELEM> tensor, List<?> vars) {
			this.tensor = tensor;
			this.vars = vars;
		}

		public Class<ELEM> getType() {
			return tensor.getType();
		}

		@Override
		public String toString() {
			return tensor.info() + " vars " + format(vars);
		}
	}

	public static List<Integer> range(int start, int end) {
		assert start <= end;

		List<Integer> list = new ArrayList<Integer>();
		for (int i = start; i < end; i++)
			list.add(i);

		return list;
	}

	public static List<Integer> range(int first, int start, int end) {
		assert first < start && start <= end;

		List<Integer> list = new ArrayList<Integer>();
		list.add(first);
		for (int i = start; i < end; i++)
			list.add(i);

		return list;
	}

	public void add(Tensor<ELEM> tensor, List<?> vars) {
		if (tensor.getOrder() != vars.size())
			throw new IllegalArgumentException("invalid tensor");

		for (Object v : vars) {
			varOrder.remove(v);
			varOrder.add(v);
		}

		entries.add(new Entry<ELEM>(tensor, vars));
	}

	public void add(Tensor<ELEM> tensor, int... vars) {
		List<Object> list = new ArrayList<Object>();
		for (int v : vars)
			list.add(Integer.valueOf(v));

		add(tensor, list);
	}

	public void add(Tensor<ELEM> tensor, String vars) {
		List<Object> list = new ArrayList<Object>();
		for (int i = 0; i < vars.length(); i++)
			list.add(Character.valueOf(vars.charAt(i)));

		add(tensor, list);
	}

	private static <ELEM> int[] getReshapeMap(Entry<ELEM> entry,
			List<Object> vars, int[] shape) {
		assert shape.length == vars.size();
		int[] map = new int[entry.vars.size()];

		boolean triv = shape.length == map.length;
		int index = 0;
		for (Object v : entry.vars) {
			int pos = vars.indexOf(v);
			assert pos >= 0;

			int dim = entry.tensor.getDim(index);
			if (shape[pos] >= 0 && shape[pos] != dim)
				throw new IllegalStateException("variable dimension mismatch");

			triv &= index == pos;
			map[index] = pos;
			shape[pos] = dim;
			index += 1;
		}

		return triv ? null : map;
	}

	private Entry<ELEM> norm(Entry<ELEM> entry) {
		List<Object> vars = new ArrayList<Object>(varOrder);
		vars.retainAll(entry.vars);

		int[] shape = new int[vars.size()];
		Arrays.fill(shape, -1);
		int[] map = getReshapeMap(entry, vars, shape);

		if (map == null)
			return entry;

		if (track)
			track_reshape += Util.getShapeSize(shape);

		Tensor<ELEM> tensor = Tensor.reshape(entry.tensor, shape, map);
		return new Entry<ELEM>(tensor, vars);
	}

	private Entry<ELEM> join(Entry<ELEM> arg1, Entry<ELEM> arg2) {
		assert arg1.getType() == arg2.getType();

		Set<Object> set = new HashSet<Object>();
		set.addAll(arg1.vars);
		set.addAll(arg2.vars);

		List<Object> vars = new LinkedList<Object>(varOrder);
		vars.retainAll(set);

		int[] shape = new int[vars.size()];
		Arrays.fill(shape, -1);
		int[] map1 = getReshapeMap(arg1, vars, shape);
		int[] map2 = getReshapeMap(arg2, vars, shape);

		if (track) {
			int s = Util.getShapeSize(shape);
			if (map1 != null)
				track_reshape += s;
			if (map2 != null)
				track_reshape += s;
			track_map2 += s;
		}

		Tensor<ELEM> t1 = map1 != null ? Tensor.reshape(arg1.tensor, shape,
				map1) : arg1.tensor;
		Tensor<ELEM> t2 = map2 != null ? Tensor.reshape(arg2.tensor, shape,
				map2) : arg2.tensor;

		return new Entry<ELEM>(Tensor.map2(arg1.getType(), prod, t1, t2), vars);
	}

	private Entry<ELEM> fold(Entry<ELEM> entry) {
		List<Object> vars = new ArrayList<Object>();
		List<Object> rest = new ArrayList<Object>();

		outer: for (Object v : entry.vars) {
			for (Entry<ELEM> e : entries)
				if (e != entry && e.vars.contains(v)) {
					rest.add(v);
					continue outer;
				}
			vars.add(v);
		}

		if (vars.isEmpty())
			return entry;

		int count = vars.size();
		vars.addAll(rest);
		assert vars.size() == entry.vars.size();

		int[] shape = new int[vars.size()];
		Arrays.fill(shape, -1);
		int[] map = getReshapeMap(entry, vars, shape);

		if (track) {
			int s = Util.getShapeSize(shape);
			if (map != null)
				track_reshape += s;
			track_fold += s;
		}

		Tensor<ELEM> tensor = map != null ? Tensor.reshape(entry.tensor, shape,
				map) : entry.tensor;

		tensor = Tensor.fold(entry.getType(), sum, count, tensor);
		return new Entry<ELEM>(tensor, rest);
	}

	public Tensor<ELEM> get(List<?> vars) {
		if (entries.isEmpty())
			throw new IllegalStateException("no tensor added");

		int a = varOrder.size();
		for (Object v : vars) {
			if (!varOrder.remove(v))
				throw new IllegalArgumentException("unknown variable");

			varOrder.add(v);
		}
		if (a != varOrder.size())
			throw new IllegalArgumentException("repeated variable");

		entries.add(new Entry<ELEM>(null, vars));

		Entry<ELEM> entry = entries.removeFirst();
		entries.addFirst(fold(norm(entry)));
		while (entries.size() > 2) {
			entry = entries.removeFirst();
			entry = join(entry, norm(entries.removeFirst()));
			entries.addFirst(fold(entry));
		}

		entry = entries.removeFirst();
		assert entry.vars.equals(vars);
		assert entries.getFirst().tensor == null;

		entries.clear();
		varOrder.clear();
		return entry.tensor;
	}

	public Tensor<ELEM> get(int... vars) {
		List<Object> list = new ArrayList<Object>();
		for (int v : vars)
			list.add(Integer.valueOf(v));

		return get(list);
	}

	public Tensor<ELEM> get(String vars) {
		List<Object> list = new ArrayList<Object>();
		for (int i = 0; i < vars.length(); i++)
			list.add(Character.valueOf(vars.charAt(i)));

		return get(list);
	}

	public void debug(List<?> output) {
		System.out.println("variables: " + format(varOrder));
		for (Entry<ELEM> entry : entries)
			System.out.println(entry);

		if (output != null)
			System.out.println("output: " + format(output));

		System.out.println();
	}

	public static void main2(String[] args) {
		Tensor<Integer> a = Tensor.matrix(Integer.TYPE, 2, 2,
				Arrays.asList(1, 2, 3, 4));
		Tensor.print(a);

		Contract<Integer> contract = new Contract<Integer>(Func1.INT_SUM,
				Func2.INT_MUL);
		contract.add(a, "ij");
		contract.add(a, "jk");
		Tensor.print(contract.get("ijk"));

		contract.add(a, "ij");
		contract.add(a, "jk");
		Tensor.print(contract.get("ik"));

		contract.add(a, "ij");
		contract.add(a, "jk");
		Tensor.print(contract.get("j"));
	}

	public static void main(String[] args) {
		Contract.track = true;
		Contract<Boolean> c = Contract.logical(BoolAlgebra.INSTANCE);

		Tensor<Boolean> op = Tensor.constant(new int[] { 3, 3, 3, 3, 3, 3 },
				Boolean.FALSE);

		Tensor<Boolean> rel = Tensor.constant(new int[] { 3, 3, 3, 3 },
				Boolean.FALSE);

		c.add(rel, "abcd");
		c.add(op, "xaeimq");
		c.add(rel, "efgh");
		c.add(op, "ybfjnr");
		c.add(rel, "ijkl");
		c.add(rel, "mnop");
		c.add(op, "zcgkos");
		c.add(rel, "qrst");
		c.add(op, "udhlpt");
		c.get("xyzu");

		System.out.println("reshape: " + track_reshape);
		System.out.println("map2:    " + track_map2);
		System.out.println("fold:    " + track_fold);
		System.out.println("total:   "
				+ (track_reshape + track_map2 + track_fold));
	}
}
