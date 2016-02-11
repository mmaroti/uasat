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

package org.uasat.research;

import java.text.*;
import java.util.*;

import org.uasat.core.*;
import org.uasat.math.*;

public class DigraphPoly {
	private Relation<Boolean> relation;
	private int MAX_SOLUTIONS = 1;

	public DigraphPoly(Relation<Boolean> relation) {
		assert relation.getArity() == 2;
		this.relation = relation;
	}

	private void printCount(String options, String what, int count) {
		String s = options.trim();
		if (!s.isEmpty())
			s += ' ';
		s += what;
		s += ": ";
		if (count >= MAX_SOLUTIONS)
			s += ">= ";
		s += count;
		System.out.println(s);
	}

	public List<Relation<Boolean>> printDefinableSubalgs(String options,
			boolean print) {
		int size = relation.getSize();

		final Set<Relation<Boolean>> set = new TreeSet<Relation<Boolean>>(
				Relation.COMPARATOR);
		set.add(Relation.full(size, 1));

		List<SatProblem> problems = new ArrayList<SatProblem>();
		Tensor<Boolean> full = Relation.full(size, 1).getTensor();
		Tensor<Boolean> empty = Relation.empty(size, 1).getTensor();

		SatProblem intersect = new SatProblem(full, empty, empty) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> s0 = new Relation<BOOL>(alg, tensors.get(0));
				Relation<BOOL> s1 = new Relation<BOOL>(alg, tensors.get(1));
				Relation<BOOL> s2 = new Relation<BOOL>(alg, tensors.get(2));

				BOOL b = alg.not(s0.isMemberOf(set));
				b = alg.and(b, s1.isMemberOf(set));
				b = alg.and(b, s2.isMemberOf(set));
				b = alg.and(b, s0.isEqualTo(s1.intersect(s2)));

				return b;
			}
		};

		SatProblem union = new SatProblem(full, empty, empty) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> s0 = new Relation<BOOL>(alg, tensors.get(0));
				Relation<BOOL> s1 = new Relation<BOOL>(alg, tensors.get(1));
				Relation<BOOL> s2 = new Relation<BOOL>(alg, tensors.get(2));

				BOOL b = alg.not(s0.isMemberOf(set));
				b = alg.and(b, s1.isMemberOf(set));
				b = alg.and(b, s2.isMemberOf(set));
				b = alg.and(b, s0.isEqualTo(s1.union(s2)));

				return b;
			}
		};

		SatProblem upset = new SatProblem(full, empty) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> rel = Relation.lift(alg, relation);
				Relation<BOOL> s0 = new Relation<BOOL>(alg, tensors.get(0));
				Relation<BOOL> s1 = new Relation<BOOL>(alg, tensors.get(1));

				BOOL b = alg.not(s0.isMemberOf(set));
				b = alg.and(b, s1.isMemberOf(set));
				b = alg.and(b, s0.isEqualTo(s1.compose(rel)));

				return b;
			}
		};

		SatProblem downset = new SatProblem(full, empty) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> rel = Relation.lift(alg, relation);
				Relation<BOOL> s0 = new Relation<BOOL>(alg, tensors.get(0));
				Relation<BOOL> s1 = new Relation<BOOL>(alg, tensors.get(1));

				BOOL b = alg.not(s0.isMemberOf(set));
				b = alg.and(b, s1.isMemberOf(set));
				b = alg.and(b, s0.isEqualTo(rel.compose(s1)));

				return b;
			}
		};

		SatProblem union_upset = new SatProblem(full, empty, empty) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> rel = Relation.lift(alg, relation);
				Relation<BOOL> s0 = new Relation<BOOL>(alg, tensors.get(0));
				Relation<BOOL> s1 = new Relation<BOOL>(alg, tensors.get(1));
				Relation<BOOL> s2 = new Relation<BOOL>(alg, tensors.get(2));

				BOOL b = alg.not(s0.isMemberOf(set));
				b = alg.and(b, s1.isMemberOf(set));
				b = alg.and(b, s2.isMemberOf(set));
				b = alg.and(b, s0.isEqualTo(s1.union(s2).compose(rel)));

				return b;
			}
		};

		SatProblem union_downset = new SatProblem(full, empty, empty) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> rel = Relation.lift(alg, relation);
				Relation<BOOL> s0 = new Relation<BOOL>(alg, tensors.get(0));
				Relation<BOOL> s1 = new Relation<BOOL>(alg, tensors.get(1));
				Relation<BOOL> s2 = new Relation<BOOL>(alg, tensors.get(2));

				BOOL b = alg.not(s0.isMemberOf(set));
				b = alg.and(b, s1.isMemberOf(set));
				b = alg.and(b, s2.isMemberOf(set));
				b = alg.and(b, s0.isEqualTo(rel.compose(s1.union(s2))));

				return b;
			}
		};

		boolean nonempty = false;
		for (String token : options.split(" ")) {
			if (token.equals("emptyset"))
				set.add(Relation.empty(size, 1));
			else if (token.equals("singletons")) {
				for (int i = 0; i < size; i++)
					set.add(Relation.singleton(size, i));
			} else if (token.equals("primesets")) {
				for (int i = 0; i < size; i++) {
					set.add(Relation.singleton(size, i).compose(relation));
					set.add(relation.compose(Relation.singleton(size, i)));
				}
			} else if (token.equals("prime-upsets")) {
				for (int i = 0; i < size; i++)
					set.add(Relation.singleton(size, i).compose(relation));
			} else if (token.equals("prime-downsets")) {
				for (int i = 0; i < size; i++)
					set.add(relation.compose(Relation.singleton(size, i)));
			} else if (token.equals("intersect"))
				problems.add(intersect);
			else if (token.equals("union"))
				problems.add(union);
			else if (token.equals("upset"))
				problems.add(upset);
			else if (token.equals("downset"))
				problems.add(downset);
			else if (token.equals("union-upset"))
				problems.add(union_upset);
			else if (token.equals("union-downset"))
				problems.add(union_downset);
			else if (token.equals("treedef")) {
				problems.add(intersect);
				problems.add(upset);
				problems.add(downset);
			} else if (token.equals("convex")) {
				problems.add(intersect);
				problems.add(union_upset);
				problems.add(union_downset);
			} else if (token.equals("nonempty"))
				nonempty = true;
			else if (!token.isEmpty())
				throw new IllegalArgumentException("invalid option");
		}

		int count = 0;
		while (set.size() != count) {
			count = set.size();
			for (SatProblem prob : problems) {
				prob.verbose = false;

				Tensor<Boolean> tensor = prob.solveAll(solver).get(0);
				for (Tensor<Boolean> t : Tensor.unstack(tensor)) {
					assert !Relation.wrap(t).isMemberOf(set);
					set.add(Relation.wrap(t));
				}
			}
		}

		if (nonempty)
			set.remove(Relation.wrap(empty));

		System.out.println(options + (options.isEmpty() ? "" : " ")
				+ "definable subalgs: " + set.size());

		List<Relation<Boolean>> list = new ArrayList<Relation<Boolean>>(set);

		if (print)
			for (int i = 0; i < list.size(); i++)
				System.out.println(i + ":\t" + Relation.format(list.get(i)));

		return list;
	}

	public void printHomomorphismExt(final Relation<Boolean> target,
			String pairs) {
		assert target.getArity() == 2;

		final List<Integer> p = new ArrayList<Integer>();
		for (String pair : pairs.split(" ")) {
			if (pair.length() == 2) {
				p.add(Util.parseElement(relation.getSize(),
						pair.substring(0, 1)));
				p.add(Util.parseElement(target.getSize(), pair.substring(1, 2)));
			} else if (!pair.isEmpty())
				throw new IllegalArgumentException();
		}

		SatProblem prob = new SatProblem(new int[] { target.getSize(),
				relation.getSize() }) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Function<BOOL> fun = new Function<BOOL>(alg, tensors.get(0));
				Relation<BOOL> trg = Relation.lift(alg, target);
				Relation<BOOL> src = Relation.lift(alg, relation);

				BOOL b = fun.isFunction();
				b = alg.and(b, fun.preserves(src, trg));

				for (int i = 0; i < p.size(); i += 2)
					b = alg.and(b, fun.hasValue(p.get(i + 1), p.get(i)));

				return b;
			}
		};

		prob.verbose = false;
		Tensor<Boolean> tensor = prob.solveAll(solver, MAX_SOLUTIONS).get(0);

		printCount(pairs, "extending homs", tensor.getLastDim());
		for (Tensor<Boolean> t : Tensor.unstack(tensor))
			System.out.println(" " + Function.format(Function.wrap(t)));
	}

	public boolean printOperationExt(int arity, String tuples, String notuples) {
		final List<int[]> ts = new ArrayList<int[]>();
		for (String tuple : tuples.split(" ")) {
			if (tuple.length() == arity + 1) {
				int[] index = new int[arity + 1];
				for (int i = 0; i <= arity; i++)
					index[i] = Util.parseElement(relation.getSize(),
							tuple.substring(i, i + 1));
				ts.add(index);
			} else if (!tuple.isEmpty())
				throw new IllegalArgumentException();
		}
		final List<int[]> nts = new ArrayList<int[]>();
		for (String tuple : notuples.split(" ")) {
			if (tuple.length() == arity + 1) {
				int[] index = new int[arity + 1];
				for (int i = 0; i <= arity; i++)
					index[i] = Util.parseElement(relation.getSize(),
							tuple.substring(i, i + 1));
				nts.add(index);
			} else if (!tuple.isEmpty())
				throw new IllegalArgumentException();
		}

		SatProblem prob = new SatProblem(Util.createShape(relation.getSize(),
				arity + 1)) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> rel = Relation.lift(alg, relation);
				Operation<BOOL> op = new Operation<BOOL>(alg, tensors.get(0));

				BOOL b = op.isOperation();
				b = alg.and(b, op.preserves(rel));

				// b = alg.and(b, op.isIdempotent());
				for (int[] tuple : ts)
					b = alg.and(b, op.hasValue(tuple));
				for (int[] tuple : nts)
					b = alg.and(b, alg.not(op.hasValue(tuple)));

				return b;
			}
		};

		prob.verbose = false;
		Tensor<Boolean> tensor = prob.solveAll(solver, MAX_SOLUTIONS).get(0);

		printCount(tuples, "extending ops", tensor.getLastDim());
		for (Tensor<Boolean> a : Tensor.unstack(tensor))
			System.out.println(" " + Operation.format(Operation.wrap(a)));

		return tensor.getLastDim() > 0;
	}

	public static List<Relation<Boolean>> findDigraphs(SatSolver<?> solver,
			final int size, final String what) {
		List<Tensor<Boolean>> masks = new ArrayList<Tensor<Boolean>>();
		masks.add(Relation.full(size, 2).getTensor());

		for (String token : what.split(" ")) {
			if (token.equals("non-core"))
				masks.add(Relation.empty(size, 2).getTensor());
			else if (token.equals("two-semilattice"))
				masks.add(Relation.empty(size, 3).getTensor());
			else if (token.equals("maltsev"))
				masks.add(Relation.empty(size, 4).getTensor());
			else if (token.equals("majority"))
				masks.add(Relation.empty(size, 4).getTensor());
			else if (token.equals("sd-meet")) {
				masks.add(Relation.empty(size, 4).getTensor());
				masks.add(Relation.empty(size, 4).getTensor());
			} else if (token.equals("taylor")) {
				masks.add(Relation.empty(size, 4).getTensor());
				masks.add(Relation.empty(size, 4).getTensor());
			}
		}

		SatProblem problem = new SatProblem(masks) {
			@Override
			public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
					List<Tensor<BOOL>> tensors) {
				Relation<BOOL> rel = new Relation<BOOL>(alg, tensors.get(0));
				int pos = 1;

				BOOL b = alg.TRUE;
				for (String token : what.split(" ")) {
					if (token.equals("reflexive"))
						b = alg.and(b, rel.isReflexive());
					else if (token.equals("non-reflexive"))
						b = alg.and(b, alg.not(rel.isReflexive()));
					else if (token.equals("antireflexive"))
						b = alg.and(b, rel.isAntiReflexive());
					else if (token.equals("non-antireflexive"))
						b = alg.and(b, alg.not(rel.isAntiReflexive()));
					else if (token.equals("symmetric"))
						b = alg.and(b, rel.isSymmetric());
					else if (token.equals("non-symmetric"))
						b = alg.and(b, alg.not(rel.isSymmetric()));
					else if (token.equals("antisymmetric"))
						b = alg.and(b, rel.isAntiSymmetric());
					else if (token.equals("non-antisymmetric"))
						b = alg.and(b, alg.not(rel.isAntiSymmetric()));
					else if (token.equals("transitive"))
						b = alg.and(b, rel.isTransitive());
					else if (token.equals("non-transitive"))
						b = alg.and(b, alg.not(rel.isTransitive()));
					else if (token.equals("trichotome"))
						b = alg.and(b, rel.isTrichotome());
					else if (token.equals("non-trichotome"))
						b = alg.and(b, alg.not(rel.isTrichotome()));
					else if (token.equals("non-isomorphic")) {
						List<Permutation<Boolean>> perms = Permutation
								.symmetricGroup(size);
						for (Permutation<Boolean> p : perms) {
							Permutation<BOOL> perm = Permutation.lift(alg, p);
							b = alg.and(b, rel.isLexLeq(rel.conjugate(perm)));
						}
					} else if (token.equals("non-isomorphic2")) {
						List<Permutation<Boolean>> perms = Permutation
								.transpositions(size);
						for (Permutation<Boolean> p : perms) {
							Permutation<BOOL> perm = Permutation.lift(alg, p);
							b = alg.and(b, rel.isLexLeq(rel.conjugate(perm)));
						}
					} else if (token.equals("non-isomorphic3")) {
						List<Permutation<Boolean>> perms = Permutation
								.threeCycles(size);
						for (Permutation<Boolean> p : perms) {
							Permutation<BOOL> perm = Permutation.lift(alg, p);
							b = alg.and(b, rel.isLexLeq(rel.conjugate(perm)));
						}
					} else if (token.equals("non-core")) {
						Operation<BOOL> op = new Operation<BOOL>(alg,
								tensors.get(pos++));
						b = alg.and(b, op.isOperation());
						b = alg.and(b, op.preserves(rel));
						b = alg.and(b, op.compose(op).isEqualTo(op));
						b = alg.and(b, alg.not(op.isIdempotent()));
					} else if (token.equals("two-semilattice")) {
						Operation<BOOL> op = new Operation<BOOL>(alg,
								tensors.get(pos++));
						b = alg.and(b, op.isOperation());
						b = alg.and(b, op.preserves(rel));
						b = alg.and(b, op.isTwoSemilattice());
					} else if (token.equals("maltsev")) {
						Operation<BOOL> op = new Operation<BOOL>(alg,
								tensors.get(pos++));
						b = alg.and(b, op.isOperation());
						b = alg.and(b, op.preserves(rel));
						b = alg.and(b, op.isMaltsev());
					} else if (token.equals("majority")) {
						Operation<BOOL> op = new Operation<BOOL>(alg,
								tensors.get(pos++));
						b = alg.and(b, op.isOperation());
						b = alg.and(b, op.preserves(rel));
						b = alg.and(b, op.isMajority());
					} else if (token.equals("sd-meet")) {
						Operation<BOOL> op1 = new Operation<BOOL>(alg,
								tensors.get(pos++));
						Operation<BOOL> op2 = new Operation<BOOL>(alg,
								tensors.get(pos++));
						b = alg.and(b, op1.isOperation());
						b = alg.and(b, op1.preserves(rel));
						b = alg.and(b, op2.isOperation());
						b = alg.and(b, op2.preserves(rel));
						b = alg.and(b, Operation.areJovanovicTerms(op1, op2));
					} else if (token.equals("taylor")) {
						Operation<BOOL> op1 = new Operation<BOOL>(alg,
								tensors.get(pos++));
						Operation<BOOL> op2 = new Operation<BOOL>(alg,
								tensors.get(pos++));
						b = alg.and(b, op1.isOperation());
						b = alg.and(b, op1.preserves(rel));
						b = alg.and(b, op2.isOperation());
						b = alg.and(b, op2.preserves(rel));
						b = alg.and(b, Operation.areSiggersTerms(op1, op2));
					} else if (token.length() > 0)
						throw new IllegalArgumentException("unknown option: "
								+ token);
				}

				return b;
			}
		};

		List<Relation<Boolean>> found = new ArrayList<Relation<Boolean>>();
		List<Tensor<Boolean>> solution = Tensor.unstack(problem
				.solveAll(solver).get(0));

		for (Tensor<Boolean> ten : solution) {
			Relation<Boolean> rel = Relation.wrap(ten);
			found.add(rel);
		}

		return found;
	}

	public static boolean isSpecial(SatSolver<?> solver,
			final Structure<Boolean> structure) {
		int size = structure.getSize();
		List<Relation<Boolean>> subsets = Relation.subsets(size, 1, size);
		final Structure<Boolean> complex = structure
				.makeComplexStructure(subsets);

		for (int x = 0; x < subsets.size(); x++) {
			final int fx = x;
			Relation<Boolean> subset = subsets.get(x);
			for (int y = 0; y < size; y++) {
				final int fy = y;
				if (!subset.getTensor().getElem(y))
					continue;

				SatProblem problem = new SatProblem(new int[] { size,
						subsets.size() }) {
					@Override
					public <BOOL> BOOL compute(BoolAlgebra<BOOL> alg,
							List<Tensor<BOOL>> tensors) {
						Function<BOOL> fun = new Function<BOOL>(alg,
								tensors.get(0));
						Structure<BOOL> str1 = Structure.lift(alg, complex);
						Structure<BOOL> str2 = Structure.lift(alg, structure);

						BOOL b = fun.isFunction();
						b = alg.and(b, fun.preserve(str1, str2));
						for (int i = 0; i < fun.getCodomain(); i++)
							b = alg.and(b, fun.hasValue(i, i));

						b = alg.and(b, fun.hasValue(fy, fx));

						return b;
					}
				};

				if (problem.solveOne(solver) == null) {
					System.out.println("special:");
					System.out.println(Relation.format(subset));
					System.out.println(y);
					return true;
				}
			}
		}

		return false;
	}

	private SatSolver<?> solver = SatSolver.getDefault();
	private static DecimalFormat TIME_FORMAT = new DecimalFormat("0.00");

	public static Relation<Boolean> importPartial(org.uapart.math.Relation rel) {
		assert rel.getArity() >= 1;

		int a = rel.getArity();
		int s = rel.getDomain().getSize();
		final org.uapart.core.Table t = rel.getTable();

		Tensor<Boolean> tensor = Tensor.generate(Util.createShape(s, a),
				new Func1<Boolean, int[]>() {
					@Override
					public Boolean call(int[] elem) {
						int a = t.getValue(elem);
						assert 0 <= a && a < 2;
						return a != 0;
					}
				});

		return Relation.wrap(tensor);
	}

	public static List<Relation<Boolean>> generatePosets(int size) {
		assert size >= 1;
		System.out.println("generating posets of size " + size);

		org.uapart.core.Domain dom = new org.uapart.core.Domain(size);
		org.uapart.math.Relation rel = new org.uapart.math.Relation(dom, 2);

		org.uapart.core.Term t = rel.isLexMinimal().and(rel.isPartialOrder());
		org.uapart.core.Collector col = new org.uapart.core.Collector(t, 1,
				rel.getTable());
		t = new org.uapart.core.Counter(rel.getTable(), col);

		int c = t.$evaluate();
		System.out.println("found: " + c);

		List<Relation<Boolean>> list = new ArrayList<Relation<Boolean>>();
		for (org.uapart.core.Table[] ts : col.getCollected())
			list.add(importPartial(new org.uapart.math.Relation(ts[0])));

		return list;
	}

	public static void main2(String[] args) {
		SatSolver<?> solver = SatSolver.getDefault();
		int size = 5;

		List<Relation<Boolean>> list = generatePosets(size);
		for (Relation<Boolean> rel : list) {
			Structure<Boolean> str = Structure.wrap(rel);
			CompatibleOps ops = new CompatibleOps(str, solver);
			if (!ops.hasTotallySymmetricIdempotentTerms())
				continue;

			if (isSpecial(solver, str))
				Structure.print(str);
		}

		System.out.println("done");
	}

	public static void main3(String[] args) {
		SatSolver<?> solver = SatSolver.getDefault();
		int size = 6;

		if (args.length >= 1)
			size = Integer.parseInt(args[0]);

		long time = System.currentTimeMillis();

		// System.out.println("finding posets of size " + size);
		// List<Relation<Boolean>> list = findDigraphs(solver, size,
		// "reflexive antisymmetric transitive non-isomorphic");
		// System.out.println("count: " + list.size());

		List<Relation<Boolean>> list = generatePosets(size);

		System.out.println("filtering for taylor but no tsi");
		for (Relation<Boolean> rel : list) {
			CompatibleOps ops = new CompatibleOps(Structure.wrap(rel), solver);
			if (ops.hasSiggersTerms()
					&& !ops.hasTotallySymmetricIdempotentTerms())
				System.out.println(Relation.format(rel));
		}

		time = System.currentTimeMillis() - time;
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time)
				+ " seconds.");

		// 377543 two-semilat
		// 377559 sd-meet
		// 55853 majority
		// 377559 taylor
	}

	@SuppressWarnings("unused")
	public static void main5(String[] args) {
		PartialOrder<Boolean> a1 = PartialOrder.antiChain(1);
		PartialOrder<Boolean> a2 = PartialOrder.antiChain(2);
		PartialOrder<Boolean> c4 = PartialOrder.crown(4);
		PartialOrder<Boolean> c6 = PartialOrder.crown(6);

		Structure<Boolean> str = Structure.wrap(a2.plus(a2).plus(a2)
				.asRelation());
		Structure.print(str);

		CompatibleOps ops = new CompatibleOps(str);
		ops.printUnaryOps();
		ops.printBinaryOps();
		ops.printTernaryOps();
		ops.printSpecialOps();

		GeneratedRels gen = GeneratedRels.getTreeDefUnary(str);
		// def.keepMeetIrreducibles();
		gen.print();

		Structure<Boolean> pow = str.makeComplexStructure(gen.getRelations());
		Structure.print(pow);
		System.out.println();

		GeneratedRels path2 = new GeneratedRels(str.getSize(), 2);
		path2.addPathDefBinary(gen.getRelations(), str);
		System.out.println(path2.getCount());
		path2.addIntersections();
		System.out.println(path2.getCount());
	}

	public static void main4(String[] args) {
		for (;;) {
			PartialOrder<Boolean> po = PartialOrder.random(14, 0.1);

			Structure<Boolean> str = Structure.wrap(po.asRelation());
			CompatibleOps comp = new CompatibleOps(str);
			if (!comp.hasSiggersTerms())
				continue;

			System.out.println(PartialOrder.format(po));

			if (!comp.hasTotallySymmetricIdempotentTerms()) {
				System.out.println("BINGO");
				break;
			}
		}
	}

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		String[] benoit = new String[] {
				"00 03 04 11 12 15 22 23 24 25 31 32 33 35 40 42 43 44 50 51 52 54 55",
				"00 03 04 11 12 14 15 22 23 24 25 31 32 33 35 40 42 43 44 50 51 52 54 55",
				"00 03 04 11 14 15 22 23 24 25 31 32 33 35 40 42 43 44 50 51 52 54 55",
				"00 03 04 11 12 22 23 24 25 31 32 33 35 40 42 43 44 50 51 52 54 55",
				"00 03 04 11 12 14 22 23 24 25 31 32 33 35 40 42 43 44 50 51 52 54 55",
				"00 04 05 11 13 15 22 23 24 25 31 32 33 35 41 42 43 44 50 52 54 55",
				"00 02 03 05 11 13 14 15 22 23 24 31 32 33 34 40 41 44 45 50 51 52 53 55",
				"00 02 03 05 11 12 13 14 15 22 23 24 31 32 33 34 40 41 44 45 50 51 52 53 55",
				"00 03 05 11 12 13 14 15 22 23 24 31 32 33 34 40 41 44 45 50 51 52 53 55",
				"00 03 05 11 12 13 14 15 22 23 24 31 32 33 34 40 41 44 45 50 51 53 55",
				"00 05 11 13 14 22 23 24 25 31 32 33 34 40 42 44 45 50 51 52 53 55",
				"00 01 02 03 05 11 13 14 22 23 24 25 31 32 33 34 40 42 44 45 50 51 52 53 55",
				"00 11 13 14 22 23 24 25 31 32 33 34 40 42 44 45 50 51 52 53 55",
				"00 03 04 11 13 15 22 23 24 25 32 33 34 41 42 44 45 51 52 53 55",
				"00 02 03 11 12 15 22 23 24 31 33 34 35 40 42 43 44 45 50 51 52 54 55",
				"00 02 03 11 12 14 22 23 25 31 33 34 35 41 42 44 45 50 52 53 54 55" };

		String cycle = "00 11 22 01 12 20";

		for (int i = 0; i < 1; i++) {
			System.out.println("digraph #" + i);
			Relation<Boolean> rel = Relation.parse(3, 2, cycle);

			Structure<Boolean> str = Structure.wrap(rel);
			Structure.print(str);

			CompatibleOps ops = new CompatibleOps(str);
			ops.printUnaryOps();
			ops.printBinaryOps();
			ops.printTernaryOps();
			ops.printSpecialOps();

			GeneratedRels def = GeneratedRels.getTreeDefUnary(str);
			def.removeEmpty();
			def.print();

			Structure<Boolean> pow = str.makeComplexStructure(def
					.getRelations());
			for (int j = 0; j < rel.getSize(); j++)
				pow.add(Relation.singleton(pow.getSize(), j));
			Structure.print(pow);

			ops = new CompatibleOps(pow);
			ops.printUnaryOps();
			ops.printBinaryOps();
			ops.printTernaryOps();
			ops.printSpecialOps();
		}
	}
}
