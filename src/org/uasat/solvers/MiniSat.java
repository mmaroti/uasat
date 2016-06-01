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

package org.uasat.solvers;

import java.io.*;
import java.text.*;
import java.util.*;

import org.uasat.core.*;

public class MiniSat extends SatSolver<Integer> {
	protected int variables;
	protected List<int[]> clauses = new ArrayList<int[]>();

	public String options = "-no-pre";
	public String logfile = null;

	public MiniSat() {
		super(Integer.TYPE, -1, 1);
		clear();
	}

	@Override
	public void clear() {
		variables = 1;
		clauses.clear();
		clauses.add(new int[] { 1 });
	}

	@Override
	public final Integer variable() {
		totalLiterals += 1;
		return ++variables;
	}

	@Override
	public void clause(List<Integer> clause) {
		totalClauses += 1;
		int[] c = new int[clause.size()];
		for (int i = 0; i < clause.size(); i++)
			c[i] = clause.get(i);

		clauses.add(c);
	}

	@Override
	public void clause(Integer lit1) {
		totalClauses += 1;
		clauses.add(new int[] { lit1 });
	}

	@Override
	public void clause(Integer lit1, Integer lit2) {
		totalClauses += 1;
		clauses.add(new int[] { lit1, lit2 });
	}

	@Override
	public void clause(Integer lit1, Integer lit2, Integer lit3) {
		totalClauses += 1;
		clauses.add(new int[] { lit1, lit2, lit3 });
	}

	@Override
	public Integer not(Integer b) {
		return -b;
	}

	public void dimacs(PrintStream stream) {
		stream.println("p cnf " + variables + " " + clauses.size());
		for (int[] clause : clauses) {
			for (int i : clause) {
				assert i != 0 && Math.abs(i) <= variables;

				stream.print(i);
				stream.print(' ');
			}
			stream.println('0');
		}
	}

	// variable indices in clauses and solution start at 1
	protected boolean[] solution;

	protected static final DateFormat DATEFORMAT = new SimpleDateFormat(
			"HH-mm-ss-SSS");

	@Override
	public boolean solve() {
		totalSolves += 1;
		solution = null;

		File input = null;
		PrintStream stream = null;

		BufferedReader reader = null;
		File output = null;

		Thread shutdown = null;

		try {
			if (logfile == null) {
				input = File.createTempFile("minisat_input_", ".tmp");
				output = File.createTempFile("minisat_output_", ".tmp");
			} else {
				String base = logfile + "-" + DATEFORMAT.format(new Date());
				input = new File(base + ".cnf");
				output = new File(base + ".out");
			}

			stream = new PrintStream(new BufferedOutputStream(
					new FileOutputStream(input, false), 4096));

			dimacs(stream);
			stream.close();
			stream = null;

			List<String> args = new ArrayList<String>();
			args.add("cominisatps");
			if (options != null)
				args.addAll(Arrays.asList(options.split(" ")));
			args.add(input.getAbsolutePath());
			args.add(output.getAbsolutePath());

			final Process proc = Runtime.getRuntime().exec(
					args.toArray(new String[args.size()]));

			shutdown = new Thread() {
				@Override
				public void run() {
					proc.destroy();
				}
			};

			// TODO: close small window when process is started but hook is not
			Runtime.getRuntime().addShutdownHook(shutdown);

			BufferedReader eater = new BufferedReader(new InputStreamReader(
					proc.getInputStream()));

			// eat the standard output
			while (eater.readLine() != null)
				;

			eater.close();

			int result = -1;
			try {
				result = proc.waitFor();
			} catch (InterruptedException e) {
				throw new RuntimeException(e.getMessage());
			}

			if (result != 10 && result != 20)
				throw new RuntimeException("MiniSat failed with error code "
						+ result);

			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(output)));

			String line = reader.readLine();

			if (result == 20) {
				if (line == null || !line.equals("UNSAT"))
					throw new RuntimeException("MiniSat failed with UNSAT");

				return false;
			}

			if (line == null || !line.equals("SAT"))
				throw new RuntimeException("MiniSat failed with SAT");

			line = reader.readLine();
			assert line != null;

			String[] sol = line.split("\\s+");
			if (sol.length > variables + 1 || !sol[sol.length - 1].equals("0"))
				throw new RuntimeException("MiniSat produced unexpected output");

			solution = new boolean[variables + 1];

			for (int i = 0; i < sol.length; i++) {
				int n = Integer.parseInt(sol[i]);
				if (!sol[i].equals(Integer.toString(n))
						|| Math.abs(n) > variables)
					throw new RuntimeException(
							"MiniSat produced unexpected literal");

				if (n > 0)
					solution[n] = true;
				else
					solution[-n] = false;
			}

			return true;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (stream != null)
				stream.close();

			if (input != null && logfile == null)
				input.delete();

			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
				}

			if (output != null && logfile == null)
				output.delete();

			if (shutdown != null)
				Runtime.getRuntime().removeShutdownHook(shutdown);
		}
	}

	@Override
	public boolean decode(Integer term) {
		return solution[term];
	}
}
