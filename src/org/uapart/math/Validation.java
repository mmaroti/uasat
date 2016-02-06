/**
 *	Copyright (C) Miklos Maroti, 2016
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

package org.uapart.math;

import java.text.*;

import org.uapart.core.*;

public class Validation {
	boolean failed = false;

	void verify(String msg, int count, int expected) {
		System.out.println(msg + " is " + count + ".");
		if (count != expected) {
			System.out
					.println("FAILED, the correct value is " + expected + ".");
			failed = true;
		}
	}

	void checkEquivalences() {
		Domain dom = new Domain(10);

		Equivalence equ = new Equivalence(dom);
		Term term = Term.count(equ.getTable(), equ.isValid());

		int count = term.$evaluate();
		verify("A000110 the number of equivalences on 10", count, 115975);
	}

	void checkPermutations() {
		Domain dom = new Domain(8);

		Permutation perm = new Permutation(dom);
		Term term = Term.count(perm.getTable(), perm.isValid());

		int count = term.$evaluate();
		verify("A000142 the number of permutations on 8", count, 40320);
	}

	void checkPartialOrders1() {
		Domain dom = new Domain(5);

		Relation rel = new Relation(dom, 2);
		Term term = Term.count(rel.getTable(), rel.isPartialOrder());

		int count = term.$evaluate();
		verify("A001035 the number of labelled partial orders on 5", count,
				4231);
	}

	void checkPartialOrders2() {
		Domain dom = new Domain(6);

		Relation rel = new Relation(dom, 2);
		Term term = Term.count(rel.getTable(),
				rel.isLexMinimal().and(rel.isPartialOrder()));

		int count = term.$evaluate();
		verify("A000112 the number of non-isomorphic partial orders on 6",
				count, 318);
	}

	void checkSemigroups1() {
		Domain dom = new Domain(4);

		Operation op = new Operation(dom, 2);
		Term term = Term.count(op.getTable(), op.isAssociative());

		int count = term.$evaluate();
		verify("A023814 the number of labelled semigroups on 4", count, 3492);
	}

	void checkSemigroups2() {
		Domain dom = new Domain(5);

		Operation op = new Operation(dom, 2);
		Term term = Term.count(op.getTable(),
				op.isLexMinimal().and(op.isAssociative()));

		int count = term.$evaluate();
		verify("A027851 the number of non-isomorphic semigroups on 5", count,
				1915);
	}

	void checkSimpleGraphs() {
		Domain dom = new Domain(6);

		Relation rel = new Relation(dom, 2);
		Term term = rel.isReflexive().and(rel.isSymmetric());
		term = Term.count(rel.getTable(), rel.isLexMinimal().and(term));

		int count = term.$evaluate();
		verify("A000088 the number of non-isomorphic graphs on 6", count, 156);
	}

	void checkGroups() {
		Domain dom = new Domain(6);

		Operation inv = new Operation(dom, 1);
		Operation mul = new Operation(dom, 2);
		Term term = mul.isUnitElemen(0);
		term = term.and(mul.isInverse(inv, 0));
		term = Term.exists(inv.getTable(), term);
		term = term.and(mul.isAssociative());
		term = mul.isLexMinimal().and(term);
		term = Term.count(mul.getTable(), term);

		int count = term.$evaluate();
		verify("A000001 the number of non-isomorphic groups on 6", count, 2);
	}

	private static DecimalFormat TIME_FORMAT = new DecimalFormat("0.00");

	void validate() {
		failed = false;
		long time = System.currentTimeMillis();
		System.out.println("Validating UAPART:");

		checkGroups();
		checkSimpleGraphs();
		checkEquivalences();
		checkPartialOrders2();
		checkPermutations();
		checkPartialOrders1();
		checkSemigroups1();
		checkSemigroups2();

		time = System.currentTimeMillis() - time;
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time)
				+ " seconds.");

		if (failed)
			System.out.println("*** SOME TESTS HAVE FAILED ***");
	}

	public static void main(String[] arg) {
		new Validation().validate();
	}
}
