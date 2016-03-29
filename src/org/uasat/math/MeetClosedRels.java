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
	private final List<Relation<Boolean>> covs;
	private PartialOrder<Boolean> comp;
	private Relation<Boolean> compcovs;

	public MeetClosedRels(int size, int arity) {
		assert size >= 1 && arity >= 1;

		this.size = size;
		this.arity = arity;

		perms = Permutation.symmetricGroup(arity);
		gens = new ArrayList<Relation<Boolean>>();
		covs = new ArrayList<Relation<Boolean>>();
		comp = PartialOrder.chain(0);
		compcovs = null;
	}

	public int getSize() {
		return size;
	}

	public int getArity() {
		return arity;
	}

	public void addGenerator(final Relation<Boolean> rel) {
		assert rel.getSize() == size && rel.getArity() == arity;

		if (gens.contains(rel))
			return;

		Relation<Boolean> cov = Relation.full(size, arity);
		for (int i = 0; i < gens.size(); i++) {
			Relation<Boolean> gen = gens.get(i);
			if (gen.isSubsetOf(rel)) {
				assert !rel.isSubsetOf(gen);
				covs.set(i, covs.get(i).intersect(rel));
			} else if (rel.isSubsetOf(gen))
				cov = cov.intersect(gen);
		}

		gens.add(rel);
		covs.add(cov);

		final int last = gens.size() - 1;
		Tensor<Boolean> poset = Tensor.generate(gens.size(), gens.size(),
				new Func2<Boolean, Integer, Integer>() {
					@Override
					public Boolean call(Integer elem1, Integer elem2) {
						if (elem2 == last)
							return gens.get(elem1).isSubsetOf(rel);
						else if (elem1 == last)
							return rel.isSubsetOf(gens.get(elem2));
						else
							return comp.getValue(elem1, elem2);
					}
				});

		comp = PartialOrder.wrap(poset);
		compcovs = null;
	}

	public void addPermutedGen(Relation<Boolean> rel) {
		assert rel.getSize() == size && rel.getArity() <= arity;

		if (rel.getArity() < arity)
			rel = rel.cartesian(Relation.full(size, arity - rel.getArity()));

		for (Permutation<Boolean> p : perms)
			addGenerator(rel.permute(p));
	}

	public void removeMeetReducibles() {
		boolean[] mask = new boolean[gens.size()];

		for (int i = gens.size() - 1; i >= 0; i--) {
			if (covs.get(i).isEqualTo(gens.get(i))) {

				mask[i] = true;
				gens.remove(i);
				covs.remove(i);
			}
		}

		if (gens.size() != mask.length) {
			final int[] m = new int[gens.size()];

			int j = 0;
			for (int i = 0; i < mask.length; i++)
				if (!mask[i])
					m[j++] = i;
			assert j == gens.size();

			Tensor<Boolean> poset = Tensor.generate(gens.size(), gens.size(),
					new Func2<Boolean, Integer, Integer>() {
						@Override
						public Boolean call(Integer elem1, Integer elem2) {
							return comp.getValue(m[elem1], m[elem2]);
						}
					});

			comp = PartialOrder.wrap(poset);
			compcovs = null;
		}
	}

	public <BOOL> Relation<BOOL> getGeneratorMask(Relation<BOOL> rel) {
		assert rel.getSize() == size && rel.getArity() == arity;

		BoolAlgebra<BOOL> alg = rel.getAlg();
		Tensor<BOOL> tensor = Tensor.constant(new int[] { gens.size() }, null);

		int[] order = PartialOrder.linearize(comp);
		if (compcovs == null)
			compcovs = comp.covers();

		for (int i = order.length - 1; i >= 0; i--) {
			int a = order[i];
			assert tensor.getElem(a) == null;

			BOOL x = alg.TRUE;
			for (int b = 0; b < order.length; b++) {
				if (compcovs.getValue(a, b)) {
					assert tensor.getElem(b) != null;
					x = alg.and(x, tensor.getElem(b));
				}
			}

			Relation<BOOL> r = Relation.lift(alg,
					covs.get(a).subtract(gens.get(a)));
			x = alg.and(x, rel.isDisjointOf(r));

			tensor.setElem(x, a);
		}

		return new Relation<BOOL>(alg, tensor);
	}

	public <BOOL> BOOL isMaskUpset(Relation<BOOL> mask) {
		assert mask.getSize() == gens.size() && mask.getArity() == 1;

		BoolAlgebra<BOOL> alg = mask.getAlg();
		if (compcovs == null)
			compcovs = comp.covers();

		BOOL b = alg.TRUE;
		for (int i = 0; i < gens.size(); i++)
			for (int j = 0; j < gens.size(); j++)
				if (compcovs.getValue(i, j))
					b = alg.and(b, alg.leq(mask.getValue(i), mask.getValue(j)));

		return b;
	}

	public <BOOL> Relation<BOOL> getClosureFromMask(Relation<BOOL> mask) {
		assert mask.getSize() == gens.size() && mask.getArity() == 1;

		BoolAlgebra<BOOL> alg = mask.getAlg();
		Relation<BOOL> rel = Relation.constant(alg, size, arity, alg.TRUE);

		for (int i = 0; i < gens.size(); i++) {
			Relation<Boolean> c = covs.get(i).subtract(gens.get(i));
			Relation<BOOL> d = Relation.constant(alg, size, arity,
					mask.getValue(i));
			Relation<BOOL> e = d.intersect(Relation.lift(alg, c)).complement();
			rel = rel.intersect(e);
		}

		return rel;
	}

	public <BOOL> Relation<BOOL> getClosure(Relation<BOOL> rel) {
		return getClosureFromMask(getGeneratorMask(rel));
	}

	public <BOOL> BOOL isClosed(Relation<BOOL> rel) {
		assert rel.getSize() == size && rel.getArity() == arity;

		return getClosure(rel).isEqualTo(rel);
	}

	public List<Relation<Boolean>> findAllGenerated(SatSolver<?> solver,
			int limit) {
		SatProblem problem = new SatProblem(Util.createShape(size, arity)) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> rel = new Relation<BOOL>(alg, tensors.get(0));

				return isClosed(rel);
			}
		};

		Tensor<Boolean> sol = problem.solveAll(solver, limit).get(0);
		return Relation.wrap(Tensor.unstack(sol));
	}

	public List<Relation<Boolean>> findAllGenerated() {
		return findAllGenerated(SatSolver.getDefault(), -1);
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
		return covs;
	}

	public PartialOrder<Boolean> getComparability() {
		return comp;
	}

	public int getGeneratorCount() {
		return gens.size();
	}

	public <BOOL> BOOL isMemberOf(BoolAlgebra<BOOL> alg, Relation<BOOL> rel) {
		return null;
	}

	public static void main(String[] args) {
		MeetClosedRels mcr = new MeetClosedRels(3, 1);
		mcr.addGenerator(Relation.parse(3, 1, "0"));
		mcr.addGenerator(Relation.parse(3, 1, "1"));
		mcr.addGenerator(Relation.parse(3, 1, "2"));
		mcr.addGenerator(Relation.parse(3, 1, "0 1"));
		// mcr.addGenerator(Relation.parse(3, 1, "0 2"));
		// mcr.addGenerator(Relation.parse(3, 1, "1 2"));
		// System.out.println(PartialOrder.format(mcr.getComparability()));

		System.out.println(Relation.format(mcr.getGeneratorMask(Relation.parse(
				3, 1, "0"))));
		System.out.println(mcr.isMaskUpset(Relation.parse(4, 1, "0 1 3")));
		System.out.println(Relation.format(mcr.getClosureFromMask(Relation
				.parse(4, 1, "0 1 3"))));
		Relation.print("generated", mcr.findAllGenerated());
	}
}
