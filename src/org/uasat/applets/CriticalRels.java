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

package org.uasat.applets;

import java.text.*;
import java.util.*;
import java.applet.*;
import java.io.*;

import org.uasat.core.*;
import org.uasat.math.*;

public class CriticalRels extends Applet {
	protected static final DecimalFormat TIME_FORMAT = new DecimalFormat("0.00");
	private static final long serialVersionUID = -1611572326328944751L;

	ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	PrintStream out = new PrintStream(buffer);

	public CriticalRels() {
		SatSolver.setDefault("sat4j");
	}

	public void calculate(Structure<Boolean> structure, int arity, int extra) {
		if (structure.getMaxArity() > arity + extra)
			throw new IllegalArgumentException(
					"maximal arity of structure cannot be larger than arity plus extra");

		out.println("finding unique critical rels of arity " + arity);
		Structure.print(structure, out);

		CriticalRelsGen gen = new CriticalRelsGen(structure.getSize(), arity,
				arity + extra);

		gen.addGenerators(structure);
		gen.generate2();
		gen.printUniCriticals1(out);
	}

	public String solve(String input) {
		buffer.reset();

		try {
			long time = System.currentTimeMillis();

			Structure<Boolean> structure = null;
			int arity = -1;
			int extra = 1;

			StringTokenizer tokenizer = new StringTokenizer(input, "\n\r");
			while (tokenizer.hasMoreTokens()) {
				String line = tokenizer.nextToken().trim();

				if (line.startsWith("size ")) {
					int size = Integer.parseInt(line.substring(5));
					if (size <= 0 || structure != null)
						throw new IllegalArgumentException("invalid size");
					structure = new Structure<Boolean>(BoolAlgebra.INSTANCE,
							size);
				} else if (line.startsWith("arity ")) {
					arity = Integer.parseInt(line.substring(6));
					if (arity <= 0)
						throw new IllegalArgumentException("invalid arity");
				} else if (line.startsWith("extra ")) {
					extra = Integer.parseInt(line.substring(6));
					if (extra <= 0)
						throw new IllegalArgumentException("invalid extra");
				} else if (structure == null) {
					break;
				} else if (line.startsWith("rel ")) {
					structure.add(Relation.parse(structure.getSize(),
							line.substring(4)));
				} else if (line.equals("idempotent")) {
					structure.addAll(Relation.subsets(structure.getSize(), 1));
				} else
					throw new IllegalArgumentException("invalid line " + line);
			}

			if (structure == null)
				throw new IllegalArgumentException("size is not set");

			calculate(structure, arity, extra);

			time = System.currentTimeMillis() - time;
			out.println("Finished in " + TIME_FORMAT.format(0.001 * time)
					+ " seconds.");

			return buffer.toString();
		} catch (Throwable e) {
			return e.toString();
		}
	}

	public static void main(String[] args) {
		String test = "size 2\nrel 00 01 10\narity 2";
		test = new CriticalRels().solve(test);
		System.out.println("-------");
		System.out.println(test);
	}
}
