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

import org.uasat.math.*;

public class HousePoset {
	private static DecimalFormat TIME_FORMAT = new DecimalFormat("0.00");

	public final Relation<Boolean> poset;
	public final List<Relation<Boolean>> crit1;

	public final List<Relation<Boolean>> crit2_gen3;
	public final List<Relation<Boolean>> crit2_op2;
	public final List<Operation<Boolean>> crit2_ops2;

	public final List<Relation<Boolean>> crit3_gen4;

	// public final List<Relation<Boolean>> crit2_op3;
	// public final List<Operation<Boolean>> crit2_ops3;

	public final List<Relation<Boolean>> ncrit2;
	public final List<Relation<Boolean>> ncrit3;
	public final List<Relation<Boolean>> ncrit4;

	public HousePoset() {
		poset = PartialOrder.crown(4).plus(PartialOrder.antiChain(1))
				.asRelation();

		// crit1 = findUnaryCriticals();
		crit1 = parseRels(1, "2 4", "3 4", "0 1 2", "0 1 3", "0 2 3 4",
				"1 2 3 4");

		// crit2_gen3 = findBinaryCriticals();
		crit2_gen3 = parseRels(2, "21 41 02 22 32 42 23 43 04 24 34 44",
				"21 41 22 42 03 23 33 43 04 24 34 44",
				"31 41 02 22 32 42 33 43 04 24 34 44",
				"31 41 32 42 03 23 33 43 04 24 34 44",
				"02 12 22 32 42 23 43 04 14 24 34 44",
				"02 12 22 32 42 33 43 04 14 24 34 44",
				"22 42 03 13 23 33 43 04 14 24 34 44",
				"32 42 03 13 23 33 43 04 14 24 34 44",
				"00 11 02 12 22 03 13 33 04 14 24 34 44",
				"00 20 30 40 02 12 22 32 42 03 23 33 43 04 14 24 34 44",
				"00 20 30 40 02 22 32 42 03 13 23 33 43 04 14 24 34 44",
				"11 21 31 41 02 12 22 32 42 13 23 33 43 04 14 24 34 44",
				"11 21 31 41 12 22 32 42 03 13 23 33 43 04 14 24 34 44",
				"00 20 30 40 11 21 31 41 02 12 22 32 42 03 13 23 33 43 04 14 24 34 44");

		// crit2_ops2 = findBinaryCritOp2();
		crit2_op2 = parseRels(2, "21 41 02 22 32 42 23 43 04 24 34 44",
				"21 41 22 42 03 23 33 43 04 24 34 44",
				"31 41 02 22 32 42 33 43 04 24 34 44",
				"31 41 32 42 03 23 33 43 04 24 34 44",
				"02 12 22 32 42 23 43 04 14 24 34 44",
				"02 12 22 32 42 33 43 04 14 24 34 44",
				"22 42 03 13 23 33 43 04 14 24 34 44",
				"32 42 03 13 23 33 43 04 14 24 34 44",
				"00 11 02 12 22 03 13 33 04 14 24 34 44",
				"00 20 30 40 02 12 22 32 42 03 23 33 43 04 14 24 34 44",
				"00 20 30 40 02 22 32 42 03 13 23 33 43 04 14 24 34 44",
				"11 21 31 41 02 12 22 32 42 13 23 33 43 04 14 24 34 44",
				"11 21 31 41 12 22 32 42 03 13 23 33 43 04 14 24 34 44",
				"00 20 30 40 01 11 21 31 41 02 12 22 32 42 03 13 23 33 43 04 14 24 34 44");

		crit2_ops2 = parseOps(2, "01234 11111 22244 33434 44444",
				"01234 11112 22244 33434 44444",
				"00234 11111 22244 33434 44444",
				"01234 11111 21244 33434 44444",
				"01234 11212 21244 33434 44444");

		crit3_gen4 = parseRels(
				3,
				"211 411 021 221 321 421 231 431 041 241 341 441 002 202 302 402 212 412 022 222 322 422 032 232 332 432 042 242 342 442 213 413 023 223 323 423 233 433 043 243 343 443 004 204 304 404 214 414 024 224 324 424 034 234 334 434 044 244 344 444",
				"211 411 021 221 321 421 231 431 041 241 341 441 212 412 022 222 322 422 232 432 042 242 342 442 003 203 303 403 213 413 023 223 323 423 033 233 333 433 043 243 343 443 004 204 304 404 214 414 024 224 324 424 034 234 334 434 044 244 344 444",
				"211 411 221 421 031 231 331 431 041 241 341 441 002 202 302 402 212 412 022 222 322 422 032 232 332 432 042 242 342 442 213 413 223 423 033 233 333 433 043 243 343 443 004 204 304 404 214 414 024 224 324 424 034 234 334 434 044 244 344 444",
				"211 411 221 421 031 231 331 431 041 241 341 441 212 412 222 422 032 232 332 432 042 242 342 442 003 203 303 403 213 413 023 223 323 423 033 233 333 433 043 243 343 443 004 204 304 404 214 414 024 224 324 424 034 234 334 434 044 244 344 444",
				"311 411 021 221 321 421 331 431 041 241 341 441 002 202 302 402 312 412 022 222 322 422 032 232 332 432 042 242 342 442 313 413 023 223 323 423 333 433 043 243 343 443 004 204 304 404 314 414 024 224 324 424 034 234 334 434 044 244 344 444",
				"311 411 021 221 321 421 331 431 041 241 341 441 312 412 022 222 322 422 332 432 042 242 342 442 003 203 303 403 313 413 023 223 323 423 033 233 333 433 043 243 343 443 004 204 304 404 314 414 024 224 324 424 034 234 334 434 044 244 344 444",
				"311 411 321 421 031 231 331 431 041 241 341 441 002 202 302 402 312 412 022 222 322 422 032 232 332 432 042 242 342 442 313 413 323 423 033 233 333 433 043 243 343 443 004 204 304 404 314 414 024 224 324 424 034 234 334 434 044 244 344 444",
				"311 411 321 421 031 231 331 431 041 241 341 441 312 412 322 422 032 232 332 432 042 242 342 442 003 203 303 403 313 413 023 223 323 423 033 233 333 433 043 243 343 443 004 204 304 404 314 414 024 224 324 424 034 234 334 434 044 244 344 444",
				"021 121 221 321 421 231 431 041 141 241 341 441 002 202 302 402 022 122 222 322 422 032 232 332 432 042 142 242 342 442 023 123 223 323 423 233 433 043 143 243 343 443 004 204 304 404 024 124 224 324 424 034 234 334 434 044 144 244 344 444",
				"021 121 221 321 421 231 431 041 141 241 341 441 022 122 222 322 422 232 432 042 142 242 342 442 003 203 303 403 023 123 223 323 423 033 233 333 433 043 143 243 343 443 004 204 304 404 024 124 224 324 424 034 234 334 434 044 144 244 344 444",
				"021 121 221 321 421 331 431 041 141 241 341 441 002 202 302 402 022 122 222 322 422 032 232 332 432 042 142 242 342 442 023 123 223 323 423 333 433 043 143 243 343 443 004 204 304 404 024 124 224 324 424 034 234 334 434 044 144 244 344 444",
				"021 121 221 321 421 331 431 041 141 241 341 441 022 122 222 322 422 332 432 042 142 242 342 442 003 203 303 403 023 123 223 323 423 033 233 333 433 043 143 243 343 443 004 204 304 404 024 124 224 324 424 034 234 334 434 044 144 244 344 444",
				"021 121 221 321 421 041 141 241 341 441 002 202 302 402 022 122 222 322 422 032 132 232 332 432 042 142 242 342 442 023 123 223 323 423 233 433 043 143 243 343 443 004 204 304 404 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"021 121 221 321 421 041 141 241 341 441 002 202 302 402 022 122 222 322 422 032 132 232 332 432 042 142 242 342 442 023 123 223 323 423 333 433 043 143 243 343 443 004 204 304 404 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"021 121 221 321 421 041 141 241 341 441 022 122 222 322 422 232 432 042 142 242 342 442 003 203 303 403 023 123 223 323 423 033 133 233 333 433 043 143 243 343 443 004 204 304 404 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"021 121 221 321 421 041 141 241 341 441 022 122 222 322 422 332 432 042 142 242 342 442 003 203 303 403 023 123 223 323 423 033 133 233 333 433 043 143 243 343 443 004 204 304 404 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"121 221 321 421 141 241 341 441 002 102 202 302 402 022 122 222 322 422 032 132 232 332 432 042 142 242 342 442 203 403 123 223 323 423 233 433 143 243 343 443 004 104 204 304 404 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"121 221 321 421 141 241 341 441 002 102 202 302 402 022 122 222 322 422 032 132 232 332 432 042 142 242 342 442 303 403 123 223 323 423 333 433 143 243 343 443 004 104 204 304 404 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"121 221 321 421 141 241 341 441 002 102 202 302 402 022 122 222 322 422 032 132 232 332 432 042 142 242 342 442 023 123 223 323 423 233 433 043 143 243 343 443 004 104 204 304 404 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"121 221 321 421 141 241 341 441 002 102 202 302 402 022 122 222 322 422 032 132 232 332 432 042 142 242 342 442 023 123 223 323 423 333 433 043 143 243 343 443 004 104 204 304 404 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"121 221 321 421 141 241 341 441 202 402 122 222 322 422 232 432 142 242 342 442 003 103 203 303 403 023 123 223 323 423 033 133 233 333 433 043 143 243 343 443 004 104 204 304 404 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"121 221 321 421 141 241 341 441 302 402 122 222 322 422 332 432 142 242 342 442 003 103 203 303 403 023 123 223 323 423 033 133 233 333 433 043 143 243 343 443 004 104 204 304 404 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"121 221 321 421 141 241 341 441 022 122 222 322 422 232 432 042 142 242 342 442 003 103 203 303 403 023 123 223 323 423 033 133 233 333 433 043 143 243 343 443 004 104 204 304 404 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"121 221 321 421 141 241 341 441 022 122 222 322 422 332 432 042 142 242 342 442 003 103 203 303 403 023 123 223 323 423 033 133 233 333 433 043 143 243 343 443 004 104 204 304 404 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"221 421 031 131 231 331 431 041 141 241 341 441 002 202 302 402 022 222 322 422 032 132 232 332 432 042 142 242 342 442 223 423 033 133 233 333 433 043 143 243 343 443 004 204 304 404 024 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"221 421 031 131 231 331 431 041 141 241 341 441 222 422 032 132 232 332 432 042 142 242 342 442 003 203 303 403 023 223 323 423 033 133 233 333 433 043 143 243 343 443 004 204 304 404 024 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"321 421 031 131 231 331 431 041 141 241 341 441 002 202 302 402 022 222 322 422 032 132 232 332 432 042 142 242 342 442 323 423 033 133 233 333 433 043 143 243 343 443 004 204 304 404 024 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"321 421 031 131 231 331 431 041 141 241 341 441 322 422 032 132 232 332 432 042 142 242 342 442 003 203 303 403 023 223 323 423 033 133 233 333 433 043 143 243 343 443 004 204 304 404 024 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"031 131 231 331 431 041 141 241 341 441 002 202 302 402 022 122 222 322 422 032 132 232 332 432 042 142 242 342 442 223 423 033 133 233 333 433 043 143 243 343 443 004 204 304 404 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"031 131 231 331 431 041 141 241 341 441 002 202 302 402 022 122 222 322 422 032 132 232 332 432 042 142 242 342 442 323 423 033 133 233 333 433 043 143 243 343 443 004 204 304 404 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"031 131 231 331 431 041 141 241 341 441 222 422 032 132 232 332 432 042 142 242 342 442 003 203 303 403 023 123 223 323 423 033 133 233 333 433 043 143 243 343 443 004 204 304 404 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"031 131 231 331 431 041 141 241 341 441 322 422 032 132 232 332 432 042 142 242 342 442 003 203 303 403 023 123 223 323 423 033 133 233 333 433 043 143 243 343 443 004 204 304 404 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"131 231 331 431 141 241 341 441 002 102 202 302 402 022 122 222 322 422 032 132 232 332 432 042 142 242 342 442 203 403 223 423 133 233 333 433 143 243 343 443 004 104 204 304 404 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"131 231 331 431 141 241 341 441 002 102 202 302 402 022 122 222 322 422 032 132 232 332 432 042 142 242 342 442 303 403 323 423 133 233 333 433 143 243 343 443 004 104 204 304 404 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"131 231 331 431 141 241 341 441 002 102 202 302 402 022 122 222 322 422 032 132 232 332 432 042 142 242 342 442 223 423 033 133 233 333 433 043 143 243 343 443 004 104 204 304 404 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"131 231 331 431 141 241 341 441 002 102 202 302 402 022 122 222 322 422 032 132 232 332 432 042 142 242 342 442 323 423 033 133 233 333 433 043 143 243 343 443 004 104 204 304 404 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"131 231 331 431 141 241 341 441 202 402 222 422 132 232 332 432 142 242 342 442 003 103 203 303 403 023 123 223 323 423 033 133 233 333 433 043 143 243 343 443 004 104 204 304 404 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"131 231 331 431 141 241 341 441 302 402 322 422 132 232 332 432 142 242 342 442 003 103 203 303 403 023 123 223 323 423 033 133 233 333 433 043 143 243 343 443 004 104 204 304 404 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"131 231 331 431 141 241 341 441 222 422 032 132 232 332 432 042 142 242 342 442 003 103 203 303 403 023 123 223 323 423 033 133 233 333 433 043 143 243 343 443 004 104 204 304 404 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"131 231 331 431 141 241 341 441 322 422 032 132 232 332 432 042 142 242 342 442 003 103 203 303 403 023 123 223 323 423 033 133 233 333 433 043 143 243 343 443 004 104 204 304 404 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"002 102 202 302 402 012 112 212 312 412 022 122 222 322 422 032 132 232 332 432 042 142 242 342 442 213 413 023 223 323 423 233 433 043 243 343 443 004 104 204 304 404 014 114 214 314 414 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"002 102 202 302 402 012 112 212 312 412 022 122 222 322 422 032 132 232 332 432 042 142 242 342 442 213 413 223 423 033 233 333 433 043 243 343 443 004 104 204 304 404 014 114 214 314 414 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"002 102 202 302 402 012 112 212 312 412 022 122 222 322 422 032 132 232 332 432 042 142 242 342 442 313 413 023 223 323 423 333 433 043 243 343 443 004 104 204 304 404 014 114 214 314 414 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"002 102 202 302 402 012 112 212 312 412 022 122 222 322 422 032 132 232 332 432 042 142 242 342 442 313 413 323 423 033 233 333 433 043 243 343 443 004 104 204 304 404 014 114 214 314 414 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"002 102 202 302 402 012 112 212 312 412 022 122 222 322 422 032 132 232 332 432 042 142 242 342 442 023 123 223 323 423 233 433 043 143 243 343 443 004 104 204 304 404 014 114 214 314 414 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"002 102 202 302 402 012 112 212 312 412 022 122 222 322 422 032 132 232 332 432 042 142 242 342 442 023 123 223 323 423 333 433 043 143 243 343 443 004 104 204 304 404 014 114 214 314 414 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"002 102 202 302 402 012 112 212 312 412 022 122 222 322 422 032 132 232 332 432 042 142 242 342 442 223 423 033 133 233 333 433 043 143 243 343 443 004 104 204 304 404 014 114 214 314 414 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"002 102 202 302 402 012 112 212 312 412 022 122 222 322 422 032 132 232 332 432 042 142 242 342 442 323 423 033 133 233 333 433 043 143 243 343 443 004 104 204 304 404 014 114 214 314 414 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"212 412 022 222 322 422 232 432 042 242 342 442 003 103 203 303 403 013 113 213 313 413 023 123 223 323 423 033 133 233 333 433 043 143 243 343 443 004 104 204 304 404 014 114 214 314 414 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"212 412 222 422 032 232 332 432 042 242 342 442 003 103 203 303 403 013 113 213 313 413 023 123 223 323 423 033 133 233 333 433 043 143 243 343 443 004 104 204 304 404 014 114 214 314 414 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"312 412 022 222 322 422 332 432 042 242 342 442 003 103 203 303 403 013 113 213 313 413 023 123 223 323 423 033 133 233 333 433 043 143 243 343 443 004 104 204 304 404 014 114 214 314 414 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"312 412 322 422 032 232 332 432 042 242 342 442 003 103 203 303 403 013 113 213 313 413 023 123 223 323 423 033 133 233 333 433 043 143 243 343 443 004 104 204 304 404 014 114 214 314 414 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"022 122 222 322 422 232 432 042 142 242 342 442 003 103 203 303 403 013 113 213 313 413 023 123 223 323 423 033 133 233 333 433 043 143 243 343 443 004 104 204 304 404 014 114 214 314 414 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"022 122 222 322 422 332 432 042 142 242 342 442 003 103 203 303 403 013 113 213 313 413 023 123 223 323 423 033 133 233 333 433 043 143 243 343 443 004 104 204 304 404 014 114 214 314 414 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"222 422 032 132 232 332 432 042 142 242 342 442 003 103 203 303 403 013 113 213 313 413 023 123 223 323 423 033 133 233 333 433 043 143 243 343 443 004 104 204 304 404 014 114 214 314 414 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"322 422 032 132 232 332 432 042 142 242 342 442 003 103 203 303 403 013 113 213 313 413 023 123 223 323 423 033 133 233 333 433 043 143 243 343 443 004 104 204 304 404 014 114 214 314 414 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"000 200 300 400 020 220 320 420 030 230 330 430 040 240 340 440 121 221 321 421 141 241 341 441 002 102 202 302 402 022 122 222 322 422 032 132 232 332 432 042 142 242 342 442 003 203 303 403 023 123 223 323 423 033 233 333 433 043 143 243 343 443 004 104 204 304 404 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"000 200 300 400 020 220 320 420 030 230 330 430 040 240 340 440 121 221 321 421 141 241 341 441 002 202 302 402 022 122 222 322 422 032 232 332 432 042 142 242 342 442 003 103 203 303 403 023 123 223 323 423 033 133 233 333 433 043 143 243 343 443 004 104 204 304 404 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"000 200 300 400 020 220 320 420 030 230 330 430 040 240 340 440 131 231 331 431 141 241 341 441 002 102 202 302 402 022 122 222 322 422 032 132 232 332 432 042 142 242 342 442 003 203 303 403 023 223 323 423 033 133 233 333 433 043 143 243 343 443 004 104 204 304 404 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"000 200 300 400 020 220 320 420 030 230 330 430 040 240 340 440 131 231 331 431 141 241 341 441 002 202 302 402 022 222 322 422 032 132 232 332 432 042 142 242 342 442 003 103 203 303 403 023 123 223 323 423 033 133 233 333 433 043 143 243 343 443 004 104 204 304 404 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"111 211 311 411 021 121 221 321 421 131 231 331 431 041 141 241 341 441 002 202 302 402 112 212 312 412 022 122 222 322 422 032 132 232 332 432 042 142 242 342 442 113 213 313 413 023 123 223 323 423 133 233 333 433 043 143 243 343 443 004 204 304 404 114 214 314 414 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"111 211 311 411 021 121 221 321 421 131 231 331 431 041 141 241 341 441 112 212 312 412 022 122 222 322 422 132 232 332 432 042 142 242 342 442 003 203 303 403 113 213 313 413 023 123 223 323 423 033 133 233 333 433 043 143 243 343 443 004 204 304 404 114 214 314 414 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"111 211 311 411 121 221 321 421 031 131 231 331 431 041 141 241 341 441 002 202 302 402 112 212 312 412 022 122 222 322 422 032 132 232 332 432 042 142 242 342 442 113 213 313 413 123 223 323 423 033 133 233 333 433 043 143 243 343 443 004 204 304 404 114 214 314 414 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"111 211 311 411 121 221 321 421 031 131 231 331 431 041 141 241 341 441 112 212 312 412 122 222 322 422 032 132 232 332 432 042 142 242 342 442 003 203 303 403 113 213 313 413 023 123 223 323 423 033 133 233 333 433 043 143 243 343 443 004 204 304 404 114 214 314 414 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"000 200 300 400 020 220 320 420 030 230 330 430 040 240 340 440 002 102 202 302 402 012 112 212 312 412 022 122 222 322 422 032 132 232 332 432 042 142 242 342 442 003 203 303 403 023 123 223 323 423 033 233 333 433 043 143 243 343 443 004 104 204 304 404 014 114 214 314 414 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"000 200 300 400 020 220 320 420 030 230 330 430 040 240 340 440 002 102 202 302 402 012 112 212 312 412 022 122 222 322 422 032 132 232 332 432 042 142 242 342 442 003 203 303 403 023 223 323 423 033 133 233 333 433 043 143 243 343 443 004 104 204 304 404 014 114 214 314 414 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"000 200 300 400 020 220 320 420 030 230 330 430 040 240 340 440 002 202 302 402 022 122 222 322 422 032 232 332 432 042 142 242 342 442 003 103 203 303 403 013 113 213 313 413 023 123 223 323 423 033 133 233 333 433 043 143 243 343 443 004 104 204 304 404 014 114 214 314 414 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"000 200 300 400 020 220 320 420 030 230 330 430 040 240 340 440 002 202 302 402 022 222 322 422 032 132 232 332 432 042 142 242 342 442 003 103 203 303 403 013 113 213 313 413 023 123 223 323 423 033 133 233 333 433 043 143 243 343 443 004 104 204 304 404 014 114 214 314 414 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"111 211 311 411 121 221 321 421 131 231 331 431 141 241 341 441 002 102 202 302 402 012 112 212 312 412 022 122 222 322 422 032 132 232 332 432 042 142 242 342 442 113 213 313 413 023 123 223 323 423 133 233 333 433 043 143 243 343 443 004 104 204 304 404 014 114 214 314 414 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"111 211 311 411 121 221 321 421 131 231 331 431 141 241 341 441 002 102 202 302 402 012 112 212 312 412 022 122 222 322 422 032 132 232 332 432 042 142 242 342 442 113 213 313 413 123 223 323 423 033 133 233 333 433 043 143 243 343 443 004 104 204 304 404 014 114 214 314 414 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"111 211 311 411 121 221 321 421 131 231 331 431 141 241 341 441 112 212 312 412 022 122 222 322 422 132 232 332 432 042 142 242 342 442 003 103 203 303 403 013 113 213 313 413 023 123 223 323 423 033 133 233 333 433 043 143 243 343 443 004 104 204 304 404 014 114 214 314 414 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"111 211 311 411 121 221 321 421 131 231 331 431 141 241 341 441 112 212 312 412 122 222 322 422 032 132 232 332 432 042 142 242 342 442 003 103 203 303 403 013 113 213 313 413 023 123 223 323 423 033 133 233 333 433 043 143 243 343 443 004 104 204 304 404 014 114 214 314 414 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"000 200 300 400 020 220 320 420 030 230 330 430 040 240 340 440 111 211 311 411 121 221 321 421 131 231 331 431 141 241 341 441 002 102 202 302 402 012 112 212 312 412 022 122 222 322 422 032 132 232 332 432 042 142 242 342 442 003 203 303 403 113 213 313 413 023 123 223 323 423 033 133 233 333 433 043 143 243 343 443 004 104 204 304 404 014 114 214 314 414 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444",
				"000 200 300 400 020 220 320 420 030 230 330 430 040 240 340 440 111 211 311 411 121 221 321 421 131 231 331 431 141 241 341 441 002 202 302 402 112 212 312 412 022 122 222 322 422 032 132 232 332 432 042 142 242 342 442 003 103 203 303 403 013 113 213 313 413 023 123 223 323 423 033 133 233 333 433 043 143 243 343 443 004 104 204 304 404 014 114 214 314 414 024 124 224 324 424 034 134 234 334 434 044 144 244 344 444");

		ncrit2 = parseRels(2, "", "00 11 02 12 22 03 13 33 04 14 24 34 44",
				"00 20 30 40 11 21 31 41 02 12 22 32 42 03 13 23 33 43 04 14 24 34 44");

		ncrit3 = parseRels(3);

		ncrit4 = parseRels(
				4,
				"0000 2000 3000 4000 0200 2200 3200 4200 0300 2300 3300 4300 0400 2400 3400 4400 0020 2020 3020 4020 0220 2220 3220 4220 0320 2320 3320 4320 0420 2420 3420 4420 0030 2030 3030 4030 0230 2230 3230 4230 0330 2330 3330 4330 0430 2430 3430 4430 0040 2040 3040 4040 0240 2240 3240 4240 0340 2340 3340 4340 0440 2440 3440 4440 1111 2111 3111 4111 1211 2211 3211 4211 1311 2311 3311 4311 1411 2411 3411 4411 1121 2121 3121 4121 1221 2221 3221 4221 1321 2321 3321 4321 1421 2421 3421 4421 1131 2131 3131 4131 1231 2231 3231 4231 1331 2331 3331 4331 1431 2431 3431 4431 1141 2141 3141 4141 1241 2241 3241 4241 1341 2341 3341 4341 1441 2441 3441 4441 0002 2002 3002 4002 0202 2202 3202 4202 0302 2302 3302 4302 0402 2402 3402 4402 1112 2112 3112 4112 1212 2212 3212 4212 1312 2312 3312 4312 1412 2412 3412 4412 0022 1022 2022 3022 4022 0122 1122 2122 3122 4122 0222 1222 2222 3222 4222 0322 1322 2322 3322 4322 0422 1422 2422 3422 4422 0032 2032 3032 4032 1132 2132 3132 4132 0232 1232 2232 3232 4232 0332 1332 2332 3332 4332 0432 1432 2432 3432 4432 0042 1042 2042 3042 4042 0142 1142 2142 3142 4142 0242 1242 2242 3242 4242 0342 1342 2342 3342 4342 0442 1442 2442 3442 4442 0003 2003 3003 4003 0203 2203 3203 4203 0303 2303 3303 4303 0403 2403 3403 4403 1113 2113 3113 4113 1213 2213 3213 4213 1313 2313 3313 4313 1413 2413 3413 4413 0023 2023 3023 4023 1123 2123 3123 4123 0223 1223 2223 3223 4223 0323 1323 2323 3323 4323 0423 1423 2423 3423 4423 0033 1033 2033 3033 4033 0133 1133 2133 3133 4133 0233 1233 2233 3233 4233 0333 1333 2333 3333 4333 0433 1433 2433 3433 4433 0043 1043 2043 3043 4043 0143 1143 2143 3143 4143 0243 1243 2243 3243 4243 0343 1343 2343 3343 4343 0443 1443 2443 3443 4443 0004 2004 3004 4004 0204 2204 3204 4204 0304 2304 3304 4304 0404 2404 3404 4404 1114 2114 3114 4114 1214 2214 3214 4214 1314 2314 3314 4314 1414 2414 3414 4414 0024 1024 2024 3024 4024 0124 1124 2124 3124 4124 0224 1224 2224 3224 4224 0324 1324 2324 3324 4324 0424 1424 2424 3424 4424 0034 1034 2034 3034 4034 0134 1134 2134 3134 4134 0234 1234 2234 3234 4234 0334 1334 2334 3334 4334 0434 1434 2434 3434 4434 0044 1044 2044 3044 4044 0144 1144 2144 3144 4144 0244 1244 2244 3244 4244 0344 1344 2344 3344 4344 0444 1444 2444 3444 4444");
	}

	public List<Relation<Boolean>> parseRels(int arity, String... rels) {
		List<Relation<Boolean>> list = new ArrayList<Relation<Boolean>>();
		for (String rel : rels)
			list.add(Relation.parse(poset.getSize(), arity, rel));
		return list;
	}

	public List<Operation<Boolean>> parseOps(int arity, String... ops) {
		List<Operation<Boolean>> list = new ArrayList<Operation<Boolean>>();
		for (String op : ops)
			list.add(Operation.parse(poset.getSize(), arity, op));
		return list;
	}

	public List<Relation<Boolean>> findUnaryCriticals() {
		CriticalRelsGen gen = new CriticalRelsGen(poset.getSize(), 1, 2);

		gen.addGenerator(poset);
		gen.addSingletons();
		gen.generate1();
		gen.printUniCriticals1();

		return gen.getUniCriticals1();
	}

	public List<Relation<Boolean>> findBinaryCriticals() {
		CriticalRelsGen gen = new CriticalRelsGen(poset.getSize(), 2, 3);

		gen.addGenerator(poset);
		gen.addGenerators(crit1);
		gen.generate2();
		gen.printUniCriticals1();
		// gen.printStats();

		return gen.getUniCriticals1();
	}

	public List<Relation<Boolean>> findBinaryCritOp2() {
		ClonePair clone = new ClonePair(poset.getSize());
		// clone.trace = true;
		clone.addRelation(poset);
		clone.addSingletons();
		clone.addCriticalOps(2, 2);
		clone.print();

		CompatibleRels comp = new CompatibleRels(clone.getAlgebra());
		List<Relation<Boolean>> rels = comp.findUniCriticalRels(2);
		Relation.print("crit2 op2", rels);

		return rels;
	}

	public List<Relation<Boolean>> findBinaryCritOp3() {
		ClonePair clone = new ClonePair(poset.getSize());
		clone.addRelation(poset);
		clone.addSingletons();
		clone.addCriticalOps(2, 2);
		clone.addCriticalOps(3, 2);
		clone.print();

		CompatibleRels comp = new CompatibleRels(clone.getAlgebra());
		List<Relation<Boolean>> rels = comp.findUniCriticalRels(2);
		Relation.print("crit2 op3", rels);

		return rels;
	}

	public List<Relation<Boolean>> findBinaryCritOpX() {
		CriticalRelsGen2 gen = new CriticalRelsGen2(poset.getSize(), 2);
		gen.addRelations(crit1);
		gen.addRelations(crit2_gen3);
		gen.trace = true;

		gen.generate(2);
		gen.printUniCriticals();

		return gen.getUniCriticals();
	}

	public List<Relation<Boolean>> findTernaryCriticals() {
		CriticalRelsGen gen = new CriticalRelsGen(poset.getSize(), 3, 4);
		gen.trace = true;

		gen.addGenerators(crit1);
		gen.addGenerators(crit2_gen3);
		gen.generate2();
		gen.printUniCriticals1();

		return gen.getUniCriticals1();
	}

	public List<Relation<Boolean>> findTernaryCritOp2() {
		ClonePair clone = new ClonePair(poset.getSize());
		clone.trace = true;
		clone.addRelation(poset);
		clone.addSingletons();
		clone.addCriticalOps(2, 2);
		clone.print();
		// clone.addCriticalOps(2, 3);
		// clone.print();

		CriticalRelsComp comp = new CriticalRelsComp(poset.getSize(), 3);
		comp.trace = true;
		comp.addRelations(crit1);
		comp.addRelations(crit2_gen3);
		comp.addRelations(crit3_gen4);
		comp.addOperations(clone.getAlgebra());
		comp.generate();
		List<Relation<Boolean>> rels = comp.getUniCriticals();
		Relation.print("crit3 op2", rels);

		return rels;
	}

	public List<Relation<Boolean>> findTernaryCritOp3() {
		ClonePair clone = new ClonePair(poset.getSize());
		clone.addRelation(poset);
		clone.addSingletons();
		clone.addOperations(crit2_ops2);
		clone.addCriticalOps(2, 2);
		clone.addCriticalOps(3, 2);
		clone.print();
		clone.trace = true;
		clone.addCriticalOps(3, 3);
		clone.print();

		CriticalRelsComp comp = new CriticalRelsComp(poset.getSize(), 3);
		comp.trace = true;
		comp.addRelations(crit1);
		comp.addRelations(crit2_gen3);
		comp.addRelations(crit3_gen4);
		comp.addOperations(clone.getAlgebra());
		comp.generate();
		List<Relation<Boolean>> rels = comp.getUniCriticals();
		Relation.print("crit3 op3", rels);

		return rels;
	}

	public List<Relation<Boolean>> findTernaryCritOpX() {
		CriticalRelsGen2 gen = new CriticalRelsGen2(poset.getSize(), 3);
		gen.addRelations(crit1);
		gen.addRelations(crit2_gen3);
		gen.addRelations(crit3_gen4);
		gen.trace = true;

		gen.generate(2);
		gen.printUniCriticals();

		return gen.getUniCriticals();
	}

	public List<Relation<Boolean>> findQuaternaryCriticals() {
		CriticalRelsGen gen = new CriticalRelsGen(poset.getSize(), 4, 5);
		gen.trace = true;

		gen.addGenerators(crit1);
		gen.addGenerators(crit2_gen3);
		System.out.println("done adding 2-criticals");
		gen.addGenerators(crit3_gen4);
		System.out.println("done adding 3-criticals");
		gen.generate1();
		gen.printUniCriticals1();

		return gen.getUniCriticals1();
	}

	public void explain(Relation<Boolean> rel, int arity) {
		assert rel.getArity() <= arity && 2 <= arity;

		CriticalRelsGen gen = new CriticalRelsGen(poset.getSize(),
				rel.getArity(), arity);
		gen.addGenerator(poset);
		gen.addGenerators(crit1);
		gen.printRepresentation(rel);
		gen.printStats();
	}

	public List<Relation<Boolean>> findUnaryNCriticals() {
		CriticalRelsGen gen = new CriticalRelsGen(poset.getSize(), 1, 2);

		gen.addGenerator(poset);
		gen.addGenerator(Relation.empty(poset.getSize(), 1));
		gen.generate2();
		gen.printUniCriticals1();

		return gen.getUniCriticals1();
	}

	public List<Relation<Boolean>> findBinaryNCriticals() {
		CriticalRelsGen gen = new CriticalRelsGen(poset.getSize(), 2, 3);

		gen.addGenerator(poset);
		gen.addGenerator(Relation.empty(poset.getSize(), 2));
		gen.generate2();
		gen.printUniCriticals1();

		return gen.getUniCriticals1();
	}

	public List<Relation<Boolean>> findTernaryNCriticals() {
		CriticalRelsGen gen = new CriticalRelsGen(poset.getSize(), 3, 4);

		gen.addGenerators(ncrit2);
		gen.generate2();
		gen.printUniCriticals1();

		return gen.getUniCriticals1();
	}

	public List<Relation<Boolean>> findQuaternaryNCriticals() {
		CriticalRelsGen gen = new CriticalRelsGen(poset.getSize(), 4, 5);

		gen.addGenerators(ncrit2);
		gen.generate2();
		gen.printUniCriticals1();

		return gen.getUniCriticals1();
	}

	public List<Relation<Boolean>> findPentaryNCriticals() {
		CriticalRelsGen gen = new CriticalRelsGen(poset.getSize(), 5, 6);
		gen.trace = true;

		gen.addGenerators(ncrit2);
		gen.generate2();
		gen.printUniCriticals1();

		return gen.getUniCriticals1();
	}

	public static void main(String[] args) {
		long time = System.currentTimeMillis();

		HousePoset h = new HousePoset();
		h.findPentaryNCriticals();
		// h.findBinaryCriticals();
		// h.findBinaryCritOp2();
		// h.findTernaryCriticals();
		// h.findTernaryCritOp3();
		// h.findTernaryCritOpX();
		// h.explain(h.crit2_gen3.get(13), 3);
		// h.findQuaternaryCriticals();

		time = System.currentTimeMillis() - time;
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time)
				+ " seconds.");
	}
}
