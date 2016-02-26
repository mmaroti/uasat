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

package org.uasat.script;

import java.io.*;
import java.text.*;
import java.util.*;

import org.uasat.math.*;

public abstract class Parser<ELEM> {
	public static class Result<ELEM> {
		public final ELEM elem;
		public final int line;

		public Result(ELEM elem, int line) {
			this.elem = elem;
			this.line = line;
		}
	}

	protected abstract Result<ELEM> parse(List<String> lines, int pos);

	public ELEM parse(InputStreamReader stream) throws IOException,
			ParseException {
		ArrayList<String> lines = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(stream);

		String line;
		while ((line = reader.readLine()) != null)
			lines.add(line);

		Result<ELEM> result = parse(lines, 0);
		if (result.elem == null)
			throw new ParseException("invalid input", result.line);
		else if (result.line < lines.size())
			throw new ParseException("unexpected input", result.line);
		else
			return result.elem;
	}

	public static Parser<String> string(final String line) {
		assert line != null && line.equals(line.trim());
		return new Parser<String>() {
			@Override
			protected Result<String> parse(List<String> lines, int pos) {
				String value = lines.get(pos);
				if (line.equals(value.trim()))
					return new Result<String>(value, pos + 1);
				else
					return new Result<String>(null, pos);
			}
		};
	}

	public static <ELEM> Parser<ELEM> either(List<Parser<ELEM>> parsers) {
		return null;
	}

	public static Parser<Relation<Boolean>> relation(final int size,
			final int arity) {
		return new Parser<Relation<Boolean>>() {
			@Override
			protected Result<Relation<Boolean>> parse(List<String> lines,
					int pos) {
				String line = lines.get(pos);
				try {
					Relation<Boolean> rel = Relation.parse(size, arity, line);
					assert rel != null;
					return new Result<Relation<Boolean>>(rel, pos + 1);
				} catch (IllegalArgumentException e) {
					return new Result<Relation<Boolean>>(null, pos);
				}
			}
		};
	}

	public static Parser<Relation<Boolean>> relation(final int size) {
		List<Parser<Relation<Boolean>>> parsers = new ArrayList<Parser<Relation<Boolean>>>();
		for (int i = 1; i <= 5; i++)
			parsers.add(relation(size, i));
		return either(parsers);
	}

	public static Parser<Structure<Boolean>> STRUCTURE = new Parser<Structure<Boolean>>() {
		@Override
		public Result<Structure<Boolean>> parse(List<String> lines, int pos) {
			// String line = lines.get(pos);
			return null;
		}
	};
}
