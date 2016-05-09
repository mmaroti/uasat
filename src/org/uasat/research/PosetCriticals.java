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

public class PosetCriticals {
	public final Relation<Boolean> poset;
	public List<Relation<Boolean>> crit1 = null;
	public List<Relation<Boolean>> crit2 = null;
	public List<Relation<Boolean>> crit3 = null;
	public List<Relation<Boolean>> crit4 = null;
	public List<Relation<Boolean>> crit5 = null;
	public List<Relation<Boolean>> crit6 = null;

	public PosetCriticals(Relation<Boolean> poset) {
		this.poset = poset;
		System.out.println("Poset " + Relation.format(poset));
		System.out.println();
	}

	public void findUnaryCriticals() {
		System.out.println("Finding unary critical relations");
		CriticalRelsGen gen = new CriticalRelsGen(poset.getSize(), 1, 2);

		gen.addGenerator(poset);
		gen.addGenerator(Relation.empty(poset.getSize(), 1));
		gen.generate2();
		gen.printUniCriticals1();

		List<Relation<Boolean>> list = gen.getUniCriticals1();
		if (crit1 == null)
			crit1 = Relation.sort(list);
		else
			assert Relation.sort(list).equals(crit1);

		gen = new CriticalRelsGen(poset.getSize(), 1, 3);
		gen.addGenerators(list);
		gen.generate1();
		assert list.equals(gen.getUniCriticals1());
	}

	public void findBinaryCriticals() {
		System.out.println("Finding binary critical relations");
		CriticalRelsGen gen = new CriticalRelsGen(poset.getSize(), 2, 3);

		gen.addGenerator(poset);
		gen.addGenerators(crit1);
		gen.generate2();
		gen.printUniCriticals1();

		List<Relation<Boolean>> list = gen.getUniCriticals1();
		if (crit2 == null)
			crit2 = Relation.sort(list);
		else
			assert Relation.sort(list).equals(crit2);

		gen = new CriticalRelsGen(poset.getSize(), 2, 4);
		gen.addGenerators(list);
		gen.generate1();
		assert list.equals(gen.getUniCriticals1());
	}

	public void findTernaryCriticals() {
		System.out.println("Finding ternary critical relations");
		CriticalRelsGen gen = new CriticalRelsGen(poset.getSize(), 3, 4);

		gen.addGenerators(crit2);
		gen.generate2();
		gen.printUniCriticals1();

		List<Relation<Boolean>> list = gen.getUniCriticals1();
		if (crit3 == null)
			crit3 = Relation.sort(list);
		else
			assert Relation.sort(list).equals(crit3);

		gen = new CriticalRelsGen(poset.getSize(), 3, 5);
		gen.addGenerators(list);
		gen.generate1();
		assert list.equals(gen.getUniCriticals1());
	}

	public void findQuaternaryCriticals(boolean check) {
		System.out.println("Finding 4-ary critical relations");
		CriticalRelsGen gen = new CriticalRelsGen(poset.getSize(), 4, 5);

		gen.addGenerators(crit2);
		gen.generate2();
		gen.printUniCriticals1();

		List<Relation<Boolean>> list = gen.getUniCriticals1();
		if (crit4 == null)
			crit4 = Relation.sort(list);
		else
			assert Relation.sort(list).equals(crit4);

		if (check) {
			gen = new CriticalRelsGen(poset.getSize(), 4, 6);
			gen.addGenerators(list);
			gen.generate1();
			assert list.equals(gen.getUniCriticals1());
		}
	}

	public void findPentaryCriticals(boolean check) {
		System.out.println("Finding 5-ary critical relations");
		CriticalRelsGen gen = new CriticalRelsGen(poset.getSize(), 5, 6);
		gen.trace = true;

		gen.addGenerators(crit2);
		gen.generate2();
		gen.printUniCriticals1();

		List<Relation<Boolean>> list = gen.getUniCriticals1();
		if (crit5 == null)
			crit5 = Relation.sort(list);
		else
			assert Relation.sort(list).equals(crit5);

		if (check) {
			gen = new CriticalRelsGen(poset.getSize(), 5, 7);
			gen.addGenerators(list);
			gen.generate1();
			assert list.equals(gen.getUniCriticals1());
		}
	}

	public void findSixaryCriticals(boolean check) {
		System.out.println("Finding 6-ary critical relations");
		CriticalRelsGen gen = new CriticalRelsGen(poset.getSize(), 6, 7);
		gen.trace = true;

		gen.addGenerators(crit2);
		gen.generate2();
		gen.printUniCriticals1();

		List<Relation<Boolean>> list = gen.getUniCriticals1();
		if (crit5 == null)
			crit5 = Relation.sort(list);
		else
			assert Relation.sort(list).equals(crit6);

		if (check) {
			gen = new CriticalRelsGen(poset.getSize(), 6, 8);
			gen.addGenerators(list);
			gen.generate1();
			assert list.equals(gen.getUniCriticals1());
		}
	}

	public boolean explain1(Relation<Boolean> rel, int arity) {
		assert rel.getArity() <= arity && 2 <= arity;

		CriticalRelsGen gen = new CriticalRelsGen(poset.getSize(),
				rel.getArity(), arity);
		gen.addGenerator(poset);
		return gen.printRepresentation(rel);
	}

	public boolean explain2(Relation<Boolean> rel, int arity) {
		assert rel.getArity() <= arity && 2 <= arity;

		CriticalRelsGen gen = new CriticalRelsGen(poset.getSize(),
				rel.getArity(), arity);
		gen.addGenerators(crit2);
		return gen.printRepresentation(rel);
	}

	private static DecimalFormat TIME_FORMAT = new DecimalFormat("0.00");

	public static List<Relation<Boolean>> parseRels(int size, int arity,
			String... rels) {
		List<Relation<Boolean>> list = new ArrayList<Relation<Boolean>>();
		for (String rel : rels)
			list.add(Relation.parse(size, arity, rel));
		return list;
	}

	public static List<Relation<Boolean>> parseRelComps(int size, int arity,
			String... rels) {
		List<Relation<Boolean>> list = new ArrayList<Relation<Boolean>>();
		for (String rel : rels)
			list.add(Relation.parse(size, arity, rel).complement());
		return list;
	}

	@SafeVarargs
	public static List<Relation<Boolean>> concat(
			List<Relation<Boolean>>... lists) {
		List<Relation<Boolean>> list = new ArrayList<Relation<Boolean>>();
		for (List<Relation<Boolean>> l : lists)
			list.addAll(l);
		return list;
	}

	public static void crown4() {
		PosetCriticals p = new PosetCriticals(CROWN4);
		p.crit1 = CROWN4_CRIT1;
		p.crit2 = CROWN4_CRIT2;
		p.crit3 = CROWN4_CRIT3;
		p.crit4 = CROWN4_CRIT4;
		p.crit5 = CROWN4_CRIT5;
		p.crit6 = CROWN4_CRIT6;

		// p.findUnaryCriticals();
		// p.findBinaryCriticals();
		// p.findTernaryCriticals();
		// p.findQuaternaryCriticals(true);
		// p.findPentaryCriticals(false);
		p.explain2(p.crit5.get(4), 7);
		// p.findSixaryCriticals(false);
	}

	public static final Relation<Boolean> CROWN4 = PartialOrder.crown(4)
			.asRelation();

	public static final List<Relation<Boolean>> CROWN4_CRIT1 = parseRels(4, 1,
			"");

	public static final List<Relation<Boolean>> CROWN4_CRIT2 = concat(
			CROWN4_CRIT1,
			parseRels(4, 2, "00 11 02 12 22 03 13 33",
					"00 10 20 30 01 11 21 31 02 12 22 03 13 33",
					"00 20 30 11 21 31 02 12 22 32 03 13 23 33"));

	public static final List<Relation<Boolean>> CROWN4_CRIT3 = CROWN4_CRIT2;

	public static final List<Relation<Boolean>> CROWN4_CRIT4 = concat(
			CROWN4_CRIT2, parseRelComps(4, 4, "3210 2310 3201 2301"));

	public static final List<Relation<Boolean>> CROWN4_CRIT5 = concat(
			CROWN4_CRIT2,
			parseRelComps(4, 5, "32210 23310 32201 23301",
					"32100 23100 32011 23011"));

	public static final List<Relation<Boolean>> CROWN4_CRIT6 = concat(
			CROWN4_CRIT2,
			parseRelComps(4, 6, "332210 223310 332201 223301",
					"322210 233310 322201 233301",
					"322100 233100 322011 233011",
					"321100 231100 320011 230011",
					"321000 231000 320111 230111"));

	public static void crown6() {
		PosetCriticals p = new PosetCriticals(CROWN6);
		p.crit1 = CROWN6_CRIT1;
		p.crit2 = CROWN6_CRIT2;
		p.crit3 = CROWN6_CRIT3;
		p.crit4 = CROWN6_CRIT4;

		p.findUnaryCriticals();
		p.findBinaryCriticals();
		p.explain1(p.crit2.get(4), 4);
		p.findTernaryCriticals();
		p.findQuaternaryCriticals(true);
	}

	public static final Relation<Boolean> CROWN6 = PartialOrder.crown(6)
			.asRelation();

	public static final List<Relation<Boolean>> CROWN6_CRIT1 = parseRels(6, 1,
			"");

	public static final List<Relation<Boolean>> CROWN6_CRIT2 = concat(
			CROWN6_CRIT1,
			parseRels(
					6,
					2,
					"00 11 22 03 23 33 04 14 44 15 25 55",
					"00 10 20 30 40 01 11 21 41 51 02 12 22 32 52 03 23 33 04 14 44 15 25 55",
					"00 30 40 11 41 51 22 32 52 03 23 33 43 53 04 14 34 44 54 15 25 35 45 55",
					"00 10 20 30 40 01 11 21 41 51 02 12 22 32 52 03 13 23 33 43 53 04 14 24 34 44 54 05 15 25 35 45 55"));

	public static final List<Relation<Boolean>> CROWN6_CRIT3 = concat(
			CROWN6_CRIT2,
			parseRelComps(6, 3, "543 453 534 354 435 345",
					"210 120 201 021 102 012"));

	public static final List<Relation<Boolean>> CROWN6_CRIT4 = concat(
			CROWN6_CRIT2,
			parseRelComps(6, 4, "5433 4533 5344 3544 4355 3455",
					"5430 5340 3541 3451 4532 4352",
					"5310 5420 3501 3421 4502 4312",
					"3210 4120 5201 4021 5102 3012",
					"2100 1200 2011 0211 1022 0122"));

	public static void house5() {
		PosetCriticals p = new PosetCriticals(HOUSE5);
		p.crit1 = HOUSE5_CRIT1;
		p.crit2 = HOUSE5_CRIT2;
		p.crit3 = HOUSE5_CRIT3;
		p.crit4 = HOUSE5_CRIT4;
		p.crit5 = HOUSE5_CRIT5;

		// p.findUnaryCriticals();
		// p.findBinaryCriticals();
		// p.findTernaryCriticals();
		p.findQuaternaryCriticals(true);
		p.explain1(p.crit4.get(3), 8);
		p.explain2(p.crit4.get(3), 5);
		p.findPentaryCriticals(false);
		p.explain2(p.crit5.get(4), 7);
		p.explain2(p.crit5.get(5), 8);
		// p.findSixaryCriticals(false);
	}

	public static final Relation<Boolean> HOUSE5 = PartialOrder.crown(4)
			.plus(PartialOrder.antiChain(1)).asRelation();

	public static final List<Relation<Boolean>> HOUSE5_CRIT1 = parseRels(5, 1,
			"");

	public static final List<Relation<Boolean>> HOUSE5_CRIT2 = concat(
			HOUSE5_CRIT1,
			parseRels(5, 2, "00 11 02 12 22 03 13 33 04 14 24 34 44",
					"00 20 30 40 11 21 31 41 02 12 22 32 42 03 13 23 33 43 04 14 24 34 44"));

	public static final List<Relation<Boolean>> HOUSE5_CRIT3 = HOUSE5_CRIT2;

	public static final List<Relation<Boolean>> HOUSE5_CRIT4 = concat(
			HOUSE5_CRIT2,
			parseRels(
					5,
					4,
					"0000 2000 3000 4000 0200 2200 3200 4200 0300 2300 3300 4300 0400 2400 3400 4400 0020 2020 3020 4020 0220 2220 3220 4220 0320 2320 3320 4320 0420 2420 3420 4420 0030 2030 3030 4030 0230 2230 3230 4230 0330 2330 3330 4330 0430 2430 3430 4430 0040 2040 3040 4040 0240 2240 3240 4240 0340 2340 3340 4340 0440 2440 3440 4440 1111 2111 3111 4111 1211 2211 3211 4211 1311 2311 3311 4311 1411 2411 3411 4411 1121 2121 3121 4121 1221 2221 3221 4221 1321 2321 3321 4321 1421 2421 3421 4421 1131 2131 3131 4131 1231 2231 3231 4231 1331 2331 3331 4331 1431 2431 3431 4431 1141 2141 3141 4141 1241 2241 3241 4241 1341 2341 3341 4341 1441 2441 3441 4441 0002 2002 3002 4002 0202 2202 3202 4202 0302 2302 3302 4302 0402 2402 3402 4402 1112 2112 3112 4112 1212 2212 3212 4212 1312 2312 3312 4312 1412 2412 3412 4412 0022 1022 2022 3022 4022 0122 1122 2122 3122 4122 0222 1222 2222 3222 4222 0322 1322 2322 3322 4322 0422 1422 2422 3422 4422 0032 2032 3032 4032 1132 2132 3132 4132 0232 1232 2232 3232 4232 0332 1332 2332 3332 4332 0432 1432 2432 3432 4432 0042 1042 2042 3042 4042 0142 1142 2142 3142 4142 0242 1242 2242 3242 4242 0342 1342 2342 3342 4342 0442 1442 2442 3442 4442 0003 2003 3003 4003 0203 2203 3203 4203 0303 2303 3303 4303 0403 2403 3403 4403 1113 2113 3113 4113 1213 2213 3213 4213 1313 2313 3313 4313 1413 2413 3413 4413 0023 2023 3023 4023 1123 2123 3123 4123 0223 1223 2223 3223 4223 0323 1323 2323 3323 4323 0423 1423 2423 3423 4423 0033 1033 2033 3033 4033 0133 1133 2133 3133 4133 0233 1233 2233 3233 4233 0333 1333 2333 3333 4333 0433 1433 2433 3433 4433 0043 1043 2043 3043 4043 0143 1143 2143 3143 4143 0243 1243 2243 3243 4243 0343 1343 2343 3343 4343 0443 1443 2443 3443 4443 0004 2004 3004 4004 0204 2204 3204 4204 0304 2304 3304 4304 0404 2404 3404 4404 1114 2114 3114 4114 1214 2214 3214 4214 1314 2314 3314 4314 1414 2414 3414 4414 0024 1024 2024 3024 4024 0124 1124 2124 3124 4124 0224 1224 2224 3224 4224 0324 1324 2324 3324 4324 0424 1424 2424 3424 4424 0034 1034 2034 3034 4034 0134 1134 2134 3134 4134 0234 1234 2234 3234 4234 0334 1334 2334 3334 4334 0434 1434 2434 3434 4434 0044 1044 2044 3044 4044 0144 1144 2144 3144 4144 0244 1244 2244 3244 4244 0344 1344 2344 3344 4344 0444 1444 2444 3444 4444"));

	public static final List<Relation<Boolean>> HOUSE5_CRIT5 = concat(
			HOUSE5_CRIT4,
			parseRels(
					5,
					5,
					"00000 20000 30000 40000 02000 22000 32000 42000 03000 23000 33000 43000 04000 24000 34000 44000 00200 20200 30200 40200 02200 22200 32200 42200 03200 23200 33200 43200 04200 24200 34200 44200 00300 20300 30300 40300 02300 22300 32300 42300 03300 23300 33300 43300 04300 24300 34300 44300 00400 20400 30400 40400 02400 22400 32400 42400 03400 23400 33400 43400 04400 24400 34400 44400 00020 20020 30020 40020 02020 22020 32020 42020 03020 23020 33020 43020 04020 24020 34020 44020 00220 20220 30220 40220 02220 22220 32220 42220 03220 23220 33220 43220 04220 24220 34220 44220 00320 20320 30320 40320 02320 22320 32320 42320 03320 23320 33320 43320 04320 24320 34320 44320 00420 20420 30420 40420 02420 22420 32420 42420 03420 23420 33420 43420 04420 24420 34420 44420 00030 20030 30030 40030 02030 22030 32030 42030 03030 23030 33030 43030 04030 24030 34030 44030 00230 20230 30230 40230 02230 22230 32230 42230 03230 23230 33230 43230 04230 24230 34230 44230 00330 20330 30330 40330 02330 22330 32330 42330 03330 23330 33330 43330 04330 24330 34330 44330 00430 20430 30430 40430 02430 22430 32430 42430 03430 23430 33430 43430 04430 24430 34430 44430 00040 20040 30040 40040 02040 22040 32040 42040 03040 23040 33040 43040 04040 24040 34040 44040 00240 20240 30240 40240 02240 22240 32240 42240 03240 23240 33240 43240 04240 24240 34240 44240 00340 20340 30340 40340 02340 22340 32340 42340 03340 23340 33340 43340 04340 24340 34340 44340 00440 20440 30440 40440 02440 22440 32440 42440 03440 23440 33440 43440 04440 24440 34440 44440 11111 21111 31111 41111 12111 22111 32111 42111 13111 23111 33111 43111 14111 24111 34111 44111 11211 21211 31211 41211 12211 22211 32211 42211 13211 23211 33211 43211 14211 24211 34211 44211 11311 21311 31311 41311 12311 22311 32311 42311 13311 23311 33311 43311 14311 24311 34311 44311 11411 21411 31411 41411 12411 22411 32411 42411 13411 23411 33411 43411 14411 24411 34411 44411 11121 21121 31121 41121 12121 22121 32121 42121 13121 23121 33121 43121 14121 24121 34121 44121 11221 21221 31221 41221 12221 22221 32221 42221 13221 23221 33221 43221 14221 24221 34221 44221 11321 21321 31321 41321 12321 22321 32321 42321 13321 23321 33321 43321 14321 24321 34321 44321 11421 21421 31421 41421 12421 22421 32421 42421 13421 23421 33421 43421 14421 24421 34421 44421 11131 21131 31131 41131 12131 22131 32131 42131 13131 23131 33131 43131 14131 24131 34131 44131 11231 21231 31231 41231 12231 22231 32231 42231 13231 23231 33231 43231 14231 24231 34231 44231 11331 21331 31331 41331 12331 22331 32331 42331 13331 23331 33331 43331 14331 24331 34331 44331 11431 21431 31431 41431 12431 22431 32431 42431 13431 23431 33431 43431 14431 24431 34431 44431 11141 21141 31141 41141 12141 22141 32141 42141 13141 23141 33141 43141 14141 24141 34141 44141 11241 21241 31241 41241 12241 22241 32241 42241 13241 23241 33241 43241 14241 24241 34241 44241 11341 21341 31341 41341 12341 22341 32341 42341 13341 23341 33341 43341 14341 24341 34341 44341 11441 21441 31441 41441 12441 22441 32441 42441 13441 23441 33441 43441 14441 24441 34441 44441 00002 20002 30002 40002 02002 22002 32002 42002 03002 23002 33002 43002 04002 24002 34002 44002 00202 20202 30202 40202 01202 21202 31202 41202 02202 22202 32202 42202 03202 23202 33202 43202 04202 24202 34202 44202 00302 20302 30302 40302 02302 22302 32302 42302 03302 23302 33302 43302 04302 24302 34302 44302 00402 20402 30402 40402 01402 21402 31402 41402 02402 22402 32402 42402 03402 23402 33402 43402 04402 24402 34402 44402 11112 21112 31112 41112 12112 22112 32112 42112 13112 23112 33112 43112 14112 24112 34112 44112 10212 20212 30212 40212 11212 21212 31212 41212 12212 22212 32212 42212 13212 23212 33212 43212 14212 24212 34212 44212 11312 21312 31312 41312 12312 22312 32312 42312 13312 23312 33312 43312 14312 24312 34312 44312 10412 20412 30412 40412 11412 21412 31412 41412 12412 22412 32412 42412 13412 23412 33412 43412 14412 24412 34412 44412 00022 10022 20022 30022 40022 02022 12022 22022 32022 42022 03022 13022 23022 33022 43022 04022 14022 24022 34022 44022 01122 11122 21122 31122 41122 02122 12122 22122 32122 42122 03122 13122 23122 33122 43122 04122 14122 24122 34122 44122 00222 10222 20222 30222 40222 01222 11222 21222 31222 41222 02222 12222 22222 32222 42222 03222 13222 23222 33222 43222 04222 14222 24222 34222 44222 00322 10322 20322 30322 40322 01322 11322 21322 31322 41322 02322 12322 22322 32322 42322 03322 13322 23322 33322 43322 04322 14322 24322 34322 44322 00422 10422 20422 30422 40422 01422 11422 21422 31422 41422 02422 12422 22422 32422 42422 03422 13422 23422 33422 43422 04422 14422 24422 34422 44422 00032 20032 30032 40032 02032 22032 32032 42032 03032 23032 33032 43032 04032 24032 34032 44032 11132 21132 31132 41132 12132 22132 32132 42132 13132 23132 33132 43132 14132 24132 34132 44132 00232 10232 20232 30232 40232 01232 11232 21232 31232 41232 02232 12232 22232 32232 42232 03232 13232 23232 33232 43232 04232 14232 24232 34232 44232 00332 20332 30332 40332 11332 21332 31332 41332 02332 12332 22332 32332 42332 03332 13332 23332 33332 43332 04332 14332 24332 34332 44332 00432 10432 20432 30432 40432 01432 11432 21432 31432 41432 02432 12432 22432 32432 42432 03432 13432 23432 33432 43432 04432 14432 24432 34432 44432 00042 10042 20042 30042 40042 02042 12042 22042 32042 42042 03042 13042 23042 33042 43042 04042 14042 24042 34042 44042 01142 11142 21142 31142 41142 02142 12142 22142 32142 42142 03142 13142 23142 33142 43142 04142 14142 24142 34142 44142 00242 10242 20242 30242 40242 01242 11242 21242 31242 41242 02242 12242 22242 32242 42242 03242 13242 23242 33242 43242 04242 14242 24242 34242 44242 00342 10342 20342 30342 40342 01342 11342 21342 31342 41342 02342 12342 22342 32342 42342 03342 13342 23342 33342 43342 04342 14342 24342 34342 44342 00442 10442 20442 30442 40442 01442 11442 21442 31442 41442 02442 12442 22442 32442 42442 03442 13442 23442 33442 43442 04442 14442 24442 34442 44442 00003 20003 30003 40003 02003 22003 32003 42003 03003 23003 33003 43003 04003 24003 34003 44003 00203 20203 30203 40203 02203 22203 32203 42203 03203 23203 33203 43203 04203 24203 34203 44203 00303 20303 30303 40303 01303 21303 31303 41303 02303 22303 32303 42303 03303 23303 33303 43303 04303 24303 34303 44303 00403 20403 30403 40403 01403 21403 31403 41403 02403 22403 32403 42403 03403 23403 33403 43403 04403 24403 34403 44403 11113 21113 31113 41113 12113 22113 32113 42113 13113 23113 33113 43113 14113 24113 34113 44113 11213 21213 31213 41213 12213 22213 32213 42213 13213 23213 33213 43213 14213 24213 34213 44213 10313 20313 30313 40313 11313 21313 31313 41313 12313 22313 32313 42313 13313 23313 33313 43313 14313 24313 34313 44313 10413 20413 30413 40413 11413 21413 31413 41413 12413 22413 32413 42413 13413 23413 33413 43413 14413 24413 34413 44413 00023 20023 30023 40023 02023 22023 32023 42023 03023 23023 33023 43023 04023 24023 34023 44023 11123 21123 31123 41123 12123 22123 32123 42123 13123 23123 33123 43123 14123 24123 34123 44123 00223 20223 30223 40223 11223 21223 31223 41223 02223 12223 22223 32223 42223 03223 13223 23223 33223 43223 04223 14223 24223 34223 44223 00323 10323 20323 30323 40323 01323 11323 21323 31323 41323 02323 12323 22323 32323 42323 03323 13323 23323 33323 43323 04323 14323 24323 34323 44323 00423 10423 20423 30423 40423 01423 11423 21423 31423 41423 02423 12423 22423 32423 42423 03423 13423 23423 33423 43423 04423 14423 24423 34423 44423 00033 10033 20033 30033 40033 02033 12033 22033 32033 42033 03033 13033 23033 33033 43033 04033 14033 24033 34033 44033 01133 11133 21133 31133 41133 02133 12133 22133 32133 42133 03133 13133 23133 33133 43133 04133 14133 24133 34133 44133 00233 10233 20233 30233 40233 01233 11233 21233 31233 41233 02233 12233 22233 32233 42233 03233 13233 23233 33233 43233 04233 14233 24233 34233 44233 00333 10333 20333 30333 40333 01333 11333 21333 31333 41333 02333 12333 22333 32333 42333 03333 13333 23333 33333 43333 04333 14333 24333 34333 44333 00433 10433 20433 30433 40433 01433 11433 21433 31433 41433 02433 12433 22433 32433 42433 03433 13433 23433 33433 43433 04433 14433 24433 34433 44433 00043 10043 20043 30043 40043 02043 12043 22043 32043 42043 03043 13043 23043 33043 43043 04043 14043 24043 34043 44043 01143 11143 21143 31143 41143 02143 12143 22143 32143 42143 03143 13143 23143 33143 43143 04143 14143 24143 34143 44143 00243 10243 20243 30243 40243 01243 11243 21243 31243 41243 02243 12243 22243 32243 42243 03243 13243 23243 33243 43243 04243 14243 24243 34243 44243 00343 10343 20343 30343 40343 01343 11343 21343 31343 41343 02343 12343 22343 32343 42343 03343 13343 23343 33343 43343 04343 14343 24343 34343 44343 00443 10443 20443 30443 40443 01443 11443 21443 31443 41443 02443 12443 22443 32443 42443 03443 13443 23443 33443 43443 04443 14443 24443 34443 44443 00004 20004 30004 40004 02004 22004 32004 42004 03004 23004 33004 43004 04004 24004 34004 44004 00204 20204 30204 40204 01204 21204 31204 41204 02204 22204 32204 42204 03204 23204 33204 43204 04204 24204 34204 44204 00304 20304 30304 40304 01304 21304 31304 41304 02304 22304 32304 42304 03304 23304 33304 43304 04304 24304 34304 44304 00404 20404 30404 40404 01404 21404 31404 41404 02404 22404 32404 42404 03404 23404 33404 43404 04404 24404 34404 44404 11114 21114 31114 41114 12114 22114 32114 42114 13114 23114 33114 43114 14114 24114 34114 44114 10214 20214 30214 40214 11214 21214 31214 41214 12214 22214 32214 42214 13214 23214 33214 43214 14214 24214 34214 44214 10314 20314 30314 40314 11314 21314 31314 41314 12314 22314 32314 42314 13314 23314 33314 43314 14314 24314 34314 44314 10414 20414 30414 40414 11414 21414 31414 41414 12414 22414 32414 42414 13414 23414 33414 43414 14414 24414 34414 44414 00024 10024 20024 30024 40024 02024 12024 22024 32024 42024 03024 13024 23024 33024 43024 04024 14024 24024 34024 44024 01124 11124 21124 31124 41124 02124 12124 22124 32124 42124 03124 13124 23124 33124 43124 04124 14124 24124 34124 44124 00224 10224 20224 30224 40224 01224 11224 21224 31224 41224 02224 12224 22224 32224 42224 03224 13224 23224 33224 43224 04224 14224 24224 34224 44224 00324 10324 20324 30324 40324 01324 11324 21324 31324 41324 02324 12324 22324 32324 42324 03324 13324 23324 33324 43324 04324 14324 24324 34324 44324 00424 10424 20424 30424 40424 01424 11424 21424 31424 41424 02424 12424 22424 32424 42424 03424 13424 23424 33424 43424 04424 14424 24424 34424 44424 00034 10034 20034 30034 40034 02034 12034 22034 32034 42034 03034 13034 23034 33034 43034 04034 14034 24034 34034 44034 01134 11134 21134 31134 41134 02134 12134 22134 32134 42134 03134 13134 23134 33134 43134 04134 14134 24134 34134 44134 00234 10234 20234 30234 40234 01234 11234 21234 31234 41234 02234 12234 22234 32234 42234 03234 13234 23234 33234 43234 04234 14234 24234 34234 44234 00334 10334 20334 30334 40334 01334 11334 21334 31334 41334 02334 12334 22334 32334 42334 03334 13334 23334 33334 43334 04334 14334 24334 34334 44334 00434 10434 20434 30434 40434 01434 11434 21434 31434 41434 02434 12434 22434 32434 42434 03434 13434 23434 33434 43434 04434 14434 24434 34434 44434 00044 10044 20044 30044 40044 02044 12044 22044 32044 42044 03044 13044 23044 33044 43044 04044 14044 24044 34044 44044 01144 11144 21144 31144 41144 02144 12144 22144 32144 42144 03144 13144 23144 33144 43144 04144 14144 24144 34144 44144 00244 10244 20244 30244 40244 01244 11244 21244 31244 41244 02244 12244 22244 32244 42244 03244 13244 23244 33244 43244 04244 14244 24244 34244 44244 00344 10344 20344 30344 40344 01344 11344 21344 31344 41344 02344 12344 22344 32344 42344 03344 13344 23344 33344 43344 04344 14344 24344 34344 44344 00444 10444 20444 30444 40444 01444 11444 21444 31444 41444 02444 12444 22444 32444 42444 03444 13444 23444 33444 43444 04444 14444 24444 34444 44444",
					"00000 20000 30000 40000 02000 22000 32000 42000 03000 23000 33000 43000 04000 24000 34000 44000 00200 20200 30200 40200 02200 22200 32200 42200 03200 23200 33200 43200 04200 24200 34200 44200 00300 20300 30300 40300 02300 22300 32300 42300 03300 23300 33300 43300 04300 24300 34300 44300 00400 20400 30400 40400 02400 22400 32400 42400 03400 23400 33400 43400 04400 24400 34400 44400 00020 20020 30020 40020 02020 22020 32020 42020 03020 23020 33020 43020 04020 24020 34020 44020 00220 20220 30220 40220 02220 22220 32220 42220 03220 23220 33220 43220 04220 24220 34220 44220 00320 20320 30320 40320 02320 22320 32320 42320 03320 23320 33320 43320 04320 24320 34320 44320 00420 20420 30420 40420 02420 22420 32420 42420 03420 23420 33420 43420 04420 24420 34420 44420 00030 20030 30030 40030 02030 22030 32030 42030 03030 23030 33030 43030 04030 24030 34030 44030 00230 20230 30230 40230 02230 22230 32230 42230 03230 23230 33230 43230 04230 24230 34230 44230 00330 20330 30330 40330 02330 22330 32330 42330 03330 23330 33330 43330 04330 24330 34330 44330 00430 20430 30430 40430 02430 22430 32430 42430 03430 23430 33430 43430 04430 24430 34430 44430 00040 20040 30040 40040 02040 22040 32040 42040 03040 23040 33040 43040 04040 24040 34040 44040 00240 20240 30240 40240 02240 22240 32240 42240 03240 23240 33240 43240 04240 24240 34240 44240 00340 20340 30340 40340 02340 22340 32340 42340 03340 23340 33340 43340 04340 24340 34340 44340 00440 20440 30440 40440 02440 22440 32440 42440 03440 23440 33440 43440 04440 24440 34440 44440 11111 21111 31111 41111 12111 22111 32111 42111 13111 23111 33111 43111 14111 24111 34111 44111 11211 21211 31211 41211 12211 22211 32211 42211 13211 23211 33211 43211 14211 24211 34211 44211 11311 21311 31311 41311 12311 22311 32311 42311 13311 23311 33311 43311 14311 24311 34311 44311 11411 21411 31411 41411 12411 22411 32411 42411 13411 23411 33411 43411 14411 24411 34411 44411 11121 21121 31121 41121 12121 22121 32121 42121 13121 23121 33121 43121 14121 24121 34121 44121 11221 21221 31221 41221 12221 22221 32221 42221 13221 23221 33221 43221 14221 24221 34221 44221 11321 21321 31321 41321 12321 22321 32321 42321 13321 23321 33321 43321 14321 24321 34321 44321 11421 21421 31421 41421 12421 22421 32421 42421 13421 23421 33421 43421 14421 24421 34421 44421 11131 21131 31131 41131 12131 22131 32131 42131 13131 23131 33131 43131 14131 24131 34131 44131 11231 21231 31231 41231 12231 22231 32231 42231 13231 23231 33231 43231 14231 24231 34231 44231 11331 21331 31331 41331 12331 22331 32331 42331 13331 23331 33331 43331 14331 24331 34331 44331 11431 21431 31431 41431 12431 22431 32431 42431 13431 23431 33431 43431 14431 24431 34431 44431 11141 21141 31141 41141 12141 22141 32141 42141 13141 23141 33141 43141 14141 24141 34141 44141 11241 21241 31241 41241 12241 22241 32241 42241 13241 23241 33241 43241 14241 24241 34241 44241 11341 21341 31341 41341 12341 22341 32341 42341 13341 23341 33341 43341 14341 24341 34341 44341 11441 21441 31441 41441 12441 22441 32441 42441 13441 23441 33441 43441 14441 24441 34441 44441 00002 20002 30002 40002 02002 22002 32002 42002 03002 23002 33002 43002 04002 24002 34002 44002 00202 20202 30202 40202 02202 22202 32202 42202 03202 23202 33202 43202 04202 24202 34202 44202 00302 20302 30302 40302 02302 22302 32302 42302 03302 23302 33302 43302 04302 24302 34302 44302 00402 20402 30402 40402 02402 22402 32402 42402 03402 23402 33402 43402 04402 24402 34402 44402 11112 21112 31112 41112 12112 22112 32112 42112 13112 23112 33112 43112 14112 24112 34112 44112 11212 21212 31212 41212 12212 22212 32212 42212 13212 23212 33212 43212 14212 24212 34212 44212 11312 21312 31312 41312 12312 22312 32312 42312 13312 23312 33312 43312 14312 24312 34312 44312 11412 21412 31412 41412 12412 22412 32412 42412 13412 23412 33412 43412 14412 24412 34412 44412 00022 10022 20022 30022 40022 01022 11022 21022 31022 41022 02022 12022 22022 32022 42022 03022 13022 23022 33022 43022 04022 14022 24022 34022 44022 00122 10122 20122 30122 40122 01122 11122 21122 31122 41122 02122 12122 22122 32122 42122 03122 13122 23122 33122 43122 04122 14122 24122 34122 44122 00222 10222 20222 30222 40222 01222 11222 21222 31222 41222 02222 12222 22222 32222 42222 03222 13222 23222 33222 43222 04222 14222 24222 34222 44222 00322 10322 20322 30322 40322 01322 11322 21322 31322 41322 02322 12322 22322 32322 42322 03322 13322 23322 33322 43322 04322 14322 24322 34322 44322 00422 10422 20422 30422 40422 01422 11422 21422 31422 41422 02422 12422 22422 32422 42422 03422 13422 23422 33422 43422 04422 14422 24422 34422 44422 00032 20032 30032 40032 02032 22032 32032 42032 03032 23032 33032 43032 04032 24032 34032 44032 11132 21132 31132 41132 12132 22132 32132 42132 13132 23132 33132 43132 14132 24132 34132 44132 00232 20232 30232 40232 11232 21232 31232 41232 02232 12232 22232 32232 42232 03232 13232 23232 33232 43232 04232 14232 24232 34232 44232 00332 10332 20332 30332 40332 01332 11332 21332 31332 41332 02332 12332 22332 32332 42332 03332 13332 23332 33332 43332 04332 14332 24332 34332 44332 00432 10432 20432 30432 40432 01432 11432 21432 31432 41432 02432 12432 22432 32432 42432 03432 13432 23432 33432 43432 04432 14432 24432 34432 44432 00042 10042 20042 30042 40042 01042 11042 21042 31042 41042 02042 12042 22042 32042 42042 03042 13042 23042 33042 43042 04042 14042 24042 34042 44042 00142 10142 20142 30142 40142 01142 11142 21142 31142 41142 02142 12142 22142 32142 42142 03142 13142 23142 33142 43142 04142 14142 24142 34142 44142 00242 10242 20242 30242 40242 01242 11242 21242 31242 41242 02242 12242 22242 32242 42242 03242 13242 23242 33242 43242 04242 14242 24242 34242 44242 00342 10342 20342 30342 40342 01342 11342 21342 31342 41342 02342 12342 22342 32342 42342 03342 13342 23342 33342 43342 04342 14342 24342 34342 44342 00442 10442 20442 30442 40442 01442 11442 21442 31442 41442 02442 12442 22442 32442 42442 03442 13442 23442 33442 43442 04442 14442 24442 34442 44442 00003 20003 30003 40003 02003 22003 32003 42003 03003 23003 33003 43003 04003 24003 34003 44003 00203 20203 30203 40203 02203 22203 32203 42203 03203 23203 33203 43203 04203 24203 34203 44203 00303 20303 30303 40303 02303 22303 32303 42303 03303 23303 33303 43303 04303 24303 34303 44303 00403 20403 30403 40403 02403 22403 32403 42403 03403 23403 33403 43403 04403 24403 34403 44403 11113 21113 31113 41113 12113 22113 32113 42113 13113 23113 33113 43113 14113 24113 34113 44113 11213 21213 31213 41213 12213 22213 32213 42213 13213 23213 33213 43213 14213 24213 34213 44213 11313 21313 31313 41313 12313 22313 32313 42313 13313 23313 33313 43313 14313 24313 34313 44313 11413 21413 31413 41413 12413 22413 32413 42413 13413 23413 33413 43413 14413 24413 34413 44413 00023 20023 30023 40023 02023 22023 32023 42023 03023 23023 33023 43023 04023 24023 34023 44023 11123 21123 31123 41123 12123 22123 32123 42123 13123 23123 33123 43123 14123 24123 34123 44123 00223 10223 20223 30223 40223 01223 11223 21223 31223 41223 02223 12223 22223 32223 42223 03223 13223 23223 33223 43223 04223 14223 24223 34223 44223 00323 20323 30323 40323 11323 21323 31323 41323 02323 12323 22323 32323 42323 03323 13323 23323 33323 43323 04323 14323 24323 34323 44323 00423 10423 20423 30423 40423 01423 11423 21423 31423 41423 02423 12423 22423 32423 42423 03423 13423 23423 33423 43423 04423 14423 24423 34423 44423 00033 10033 20033 30033 40033 01033 11033 21033 31033 41033 02033 12033 22033 32033 42033 03033 13033 23033 33033 43033 04033 14033 24033 34033 44033 00133 10133 20133 30133 40133 01133 11133 21133 31133 41133 02133 12133 22133 32133 42133 03133 13133 23133 33133 43133 04133 14133 24133 34133 44133 00233 10233 20233 30233 40233 01233 11233 21233 31233 41233 02233 12233 22233 32233 42233 03233 13233 23233 33233 43233 04233 14233 24233 34233 44233 00333 10333 20333 30333 40333 01333 11333 21333 31333 41333 02333 12333 22333 32333 42333 03333 13333 23333 33333 43333 04333 14333 24333 34333 44333 00433 10433 20433 30433 40433 01433 11433 21433 31433 41433 02433 12433 22433 32433 42433 03433 13433 23433 33433 43433 04433 14433 24433 34433 44433 00043 10043 20043 30043 40043 01043 11043 21043 31043 41043 02043 12043 22043 32043 42043 03043 13043 23043 33043 43043 04043 14043 24043 34043 44043 00143 10143 20143 30143 40143 01143 11143 21143 31143 41143 02143 12143 22143 32143 42143 03143 13143 23143 33143 43143 04143 14143 24143 34143 44143 00243 10243 20243 30243 40243 01243 11243 21243 31243 41243 02243 12243 22243 32243 42243 03243 13243 23243 33243 43243 04243 14243 24243 34243 44243 00343 10343 20343 30343 40343 01343 11343 21343 31343 41343 02343 12343 22343 32343 42343 03343 13343 23343 33343 43343 04343 14343 24343 34343 44343 00443 10443 20443 30443 40443 01443 11443 21443 31443 41443 02443 12443 22443 32443 42443 03443 13443 23443 33443 43443 04443 14443 24443 34443 44443 00004 20004 30004 40004 02004 22004 32004 42004 03004 23004 33004 43004 04004 24004 34004 44004 00204 20204 30204 40204 02204 22204 32204 42204 03204 23204 33204 43204 04204 24204 34204 44204 00304 20304 30304 40304 02304 22304 32304 42304 03304 23304 33304 43304 04304 24304 34304 44304 00404 20404 30404 40404 02404 22404 32404 42404 03404 23404 33404 43404 04404 24404 34404 44404 11114 21114 31114 41114 12114 22114 32114 42114 13114 23114 33114 43114 14114 24114 34114 44114 11214 21214 31214 41214 12214 22214 32214 42214 13214 23214 33214 43214 14214 24214 34214 44214 11314 21314 31314 41314 12314 22314 32314 42314 13314 23314 33314 43314 14314 24314 34314 44314 11414 21414 31414 41414 12414 22414 32414 42414 13414 23414 33414 43414 14414 24414 34414 44414 00024 10024 20024 30024 40024 01024 11024 21024 31024 41024 02024 12024 22024 32024 42024 03024 13024 23024 33024 43024 04024 14024 24024 34024 44024 00124 10124 20124 30124 40124 01124 11124 21124 31124 41124 02124 12124 22124 32124 42124 03124 13124 23124 33124 43124 04124 14124 24124 34124 44124 00224 10224 20224 30224 40224 01224 11224 21224 31224 41224 02224 12224 22224 32224 42224 03224 13224 23224 33224 43224 04224 14224 24224 34224 44224 00324 10324 20324 30324 40324 01324 11324 21324 31324 41324 02324 12324 22324 32324 42324 03324 13324 23324 33324 43324 04324 14324 24324 34324 44324 00424 10424 20424 30424 40424 01424 11424 21424 31424 41424 02424 12424 22424 32424 42424 03424 13424 23424 33424 43424 04424 14424 24424 34424 44424 00034 10034 20034 30034 40034 01034 11034 21034 31034 41034 02034 12034 22034 32034 42034 03034 13034 23034 33034 43034 04034 14034 24034 34034 44034 00134 10134 20134 30134 40134 01134 11134 21134 31134 41134 02134 12134 22134 32134 42134 03134 13134 23134 33134 43134 04134 14134 24134 34134 44134 00234 10234 20234 30234 40234 01234 11234 21234 31234 41234 02234 12234 22234 32234 42234 03234 13234 23234 33234 43234 04234 14234 24234 34234 44234 00334 10334 20334 30334 40334 01334 11334 21334 31334 41334 02334 12334 22334 32334 42334 03334 13334 23334 33334 43334 04334 14334 24334 34334 44334 00434 10434 20434 30434 40434 01434 11434 21434 31434 41434 02434 12434 22434 32434 42434 03434 13434 23434 33434 43434 04434 14434 24434 34434 44434 00044 10044 20044 30044 40044 01044 11044 21044 31044 41044 02044 12044 22044 32044 42044 03044 13044 23044 33044 43044 04044 14044 24044 34044 44044 00144 10144 20144 30144 40144 01144 11144 21144 31144 41144 02144 12144 22144 32144 42144 03144 13144 23144 33144 43144 04144 14144 24144 34144 44144 00244 10244 20244 30244 40244 01244 11244 21244 31244 41244 02244 12244 22244 32244 42244 03244 13244 23244 33244 43244 04244 14244 24244 34244 44244 00344 10344 20344 30344 40344 01344 11344 21344 31344 41344 02344 12344 22344 32344 42344 03344 13344 23344 33344 43344 04344 14344 24344 34344 44344 00444 10444 20444 30444 40444 01444 11444 21444 31444 41444 02444 12444 22444 32444 42444 03444 13444 23444 33444 43444 04444 14444 24444 34444 44444"));

	public static void main(String[] args) {
		long time = System.currentTimeMillis();
		SatSolver.setDefault("logging");

		// crown4();
		// crown6();
		house5();

		time = System.currentTimeMillis() - time;
		System.out.println("Finished in " + TIME_FORMAT.format(0.001 * time)
				+ " seconds.");
	}
}
