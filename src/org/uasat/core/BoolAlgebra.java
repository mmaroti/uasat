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

public abstract class BoolAlgebra<BOOL> {
	protected final Class<BOOL> type;
	
	public Class<BOOL> getType() {
		return type;
	}
	
	public final BOOL FALSE;
	public final BOOL TRUE;
	
	public BOOL lift(boolean elem) {
		return elem ? TRUE : FALSE;
	}

	public Tensor<BOOL> lift(Tensor<Boolean> tensor) {
		return Tensor.map(type, LIFT, tensor);
	}

	public abstract BOOL not(BOOL elem);

	public Tensor<BOOL> not(Tensor<BOOL> tensor) {
		return Tensor.map(type, NOT, tensor);
	}

	public BOOL or(BOOL elem1, BOOL elem2) {
		return not(and(not(elem1), not(elem2)));
	}

	public BOOL and(BOOL elem1, BOOL elem2) {
		return not(or(not(elem1), not(elem2)));
	}

	public BOOL leq(BOOL elem1, BOOL elem2) {
		return or(not(elem1), elem2);
	}

	public BOOL add(BOOL elem1, BOOL elem2) {
		return not(equ(elem1, elem2));
	}

	public BOOL equ(BOOL elem1, BOOL elem2) {
		return not(add(elem1, elem2));
	}

	public BOOL all(Iterable<BOOL> elems) {
		BOOL ret = TRUE;

		for (BOOL elem : elems) {
			if (elem == FALSE)
				return FALSE;

			ret = and(ret, elem);
		}

		return ret;
	}

	public BOOL any(Iterable<BOOL> elems) {
		BOOL ret = FALSE;

		for (BOOL elem : elems) {
			if (elem == TRUE)
				return TRUE;

			ret = or(ret, elem);
		}

		return ret;
	}

	public BOOL sum(Iterable<BOOL> elems) {
		BOOL ret = FALSE;

		for (BOOL elem : elems)
			ret = add(ret, elem);

		return ret;
	}

	public BOOL one(Iterable<BOOL> elems) {
		BOOL any = FALSE;
		BOOL err = FALSE;

		for (BOOL elem : elems) {
			err = or(err, and(any, elem));
			if (err == TRUE)
				return FALSE;

			any = or(any, elem);
		}

		return and(any, not(err));
	}

	public BOOL many(Iterable<BOOL> elems) {
		BOOL any = FALSE;
		BOOL err = FALSE;

		for (BOOL elem : elems) {
			err = or(err, and(any, elem));
			if (err == TRUE)
				return TRUE;

			any = or(any, elem);
		}

		return err;
	}

	public BOOL eqs(Iterable<BOOL> elems) {
		Iterator<BOOL> iter = elems.iterator();

		assert iter.hasNext();
		BOOL fst = iter.next();

		BOOL res = TRUE;
		while (iter.hasNext()) {
			res = and(res, equ(fst, iter.next()));
			if (res == FALSE)
				break;
		}

		return res;
	}

	public BOOL lexLess(Iterable<BOOL> elem1, Iterable<BOOL> elem2) {
		BOOL less = FALSE;
		BOOL equal = TRUE;

		Iterator<BOOL> iter1 = elem1.iterator();
		Iterator<BOOL> iter2 = elem2.iterator();
		while (iter1.hasNext()) {
			assert iter2.hasNext();

			BOOL a = iter1.next();
			BOOL b = iter2.next();

			less = or(less, and(equal, and(not(a), b)));
			equal = and(equal, equ(a, b));
			if (equal == FALSE)
				return less;
		}
		assert !iter2.hasNext();

		return less;
	}

	public BOOL lexLeq(Iterable<BOOL> elem1, Iterable<BOOL> elem2) {
		return not(lexLess(elem2, elem1));
	}

	protected final Func1<BOOL, BOOL> NOT = new Func1<BOOL, BOOL>() {
		@Override
		public BOOL call(BOOL elem) {
			assert elem != null;
			return not(elem);
		}
	};

	public final Func2<BOOL, BOOL, BOOL> OR = new Func2<BOOL, BOOL, BOOL>() {
		@Override
		public BOOL call(BOOL elem1, BOOL elem2) {
			assert elem1 != null && elem2 != null;
			return or(elem1, elem2);
		}
	};

	public final Func2<BOOL, BOOL, BOOL> AND = new Func2<BOOL, BOOL, BOOL>() {
		@Override
		public BOOL call(BOOL elem1, BOOL elem2) {
			assert elem1 != null && elem2 != null;
			return and(elem1, elem2);
		}
	};

	public final Func2<BOOL, BOOL, BOOL> LEQ = new Func2<BOOL, BOOL, BOOL>() {
		@Override
		public BOOL call(BOOL elem1, BOOL elem2) {
			assert elem1 != null && elem2 != null;
			return leq(elem1, elem2);
		}
	};

	public final Func2<BOOL, BOOL, BOOL> ADD = new Func2<BOOL, BOOL, BOOL>() {
		@Override
		public BOOL call(BOOL elem1, BOOL elem2) {
			assert elem1 != null && elem2 != null;
			return add(elem1, elem2);
		}
	};

	public final Func2<BOOL, BOOL, BOOL> EQU = new Func2<BOOL, BOOL, BOOL>() {
		@Override
		public BOOL call(BOOL elem1, BOOL elem2) {
			assert elem1 != null && elem2 != null;
			return equ(elem1, elem2);
		}
	};

	protected final Func1<BOOL, Boolean> LIFT = new Func1<BOOL, Boolean>() {
		@Override
		public BOOL call(Boolean elem) {
			return lift(elem);
		}
	};

	public final Func1<BOOL, Iterable<BOOL>> ALL = new Func1<BOOL, Iterable<BOOL>>() {
		@Override
		public BOOL call(Iterable<BOOL> elems) {
			return all(elems);
		}
	};

	public final Func1<BOOL, Iterable<BOOL>> ANY = new Func1<BOOL, Iterable<BOOL>>() {
		@Override
		public BOOL call(Iterable<BOOL> elems) {
			return any(elems);
		}
	};

	public final Func1<BOOL, Iterable<BOOL>> SUM = new Func1<BOOL, Iterable<BOOL>>() {
		@Override
		public BOOL call(Iterable<BOOL> elems) {
			return sum(elems);
		}
	};

	public final Func1<BOOL, Iterable<BOOL>> ONE = new Func1<BOOL, Iterable<BOOL>>() {
		@Override
		public BOOL call(Iterable<BOOL> elems) {
			return one(elems);
		}
	};

	public final Func1<BOOL, Iterable<BOOL>> MANY = new Func1<BOOL, Iterable<BOOL>>() {
		@Override
		public BOOL call(Iterable<BOOL> elems) {
			return many(elems);
		}
	};

	public final Func1<BOOL, Iterable<BOOL>> EQS = new Func1<BOOL, Iterable<BOOL>>() {
		@Override
		public BOOL call(Iterable<BOOL> elems) {
			return eqs(elems);
		}
	};

	public BoolAlgebra(final Class<BOOL> type, final BOOL FALSE, final BOOL TRUE) {
		this.type = type;
		this.FALSE = FALSE;
		this.TRUE = TRUE;

		assert TRUE != null && FALSE != null && TRUE != FALSE;
	}

	public static BoolAlgebra<Boolean> INSTANCE = new BoolAlgebra<Boolean>(
			Boolean.TYPE, Boolean.FALSE, Boolean.TRUE) {
		@Override
		public Boolean not(Boolean elem) {
			return !elem.booleanValue();
		}

		@Override
		public Boolean or(Boolean elem1, Boolean elem2) {
			return elem1.booleanValue() || elem2.booleanValue();
		}

		@Override
		public Boolean and(Boolean elem1, Boolean elem2) {
			return elem1.booleanValue() && elem2.booleanValue();
		}

		@Override
		public Boolean add(Boolean elem1, Boolean elem2) {
			return elem1.booleanValue() != elem2.booleanValue();
		}

		@Override
		public Boolean equ(Boolean elem1, Boolean elem2) {
			return elem1.booleanValue() == elem2.booleanValue();
		}

		@Override
		public Boolean leq(Boolean elem1, Boolean elem2) {
			return !elem1.booleanValue() || elem2.booleanValue();
		}

		@Override
		public Boolean all(Iterable<Boolean> elems) {
			for (boolean elem : elems)
				if (!elem)
					return false;

			return true;
		}

		@Override
		public Boolean any(Iterable<Boolean> elems) {
			for (boolean elem : elems)
				if (elem)
					return true;

			return false;
		}

		@Override
		public Boolean sum(Iterable<Boolean> elems) {
			boolean ret = FALSE;

			for (boolean elem : elems)
				ret ^= elem;

			return ret;
		}

		@Override
		public Boolean one(Iterable<Boolean> elems) {
			int count = 0;

			for (boolean elem : elems) {
				if (elem && ++count >= 2)
					return false;
			}

			return count == 1;
		}

		@Override
		public Boolean many(Iterable<Boolean> elems) {
			int count = 0;

			for (boolean elem : elems) {
				if (elem && ++count >= 2)
					return true;
			}

			return false;
		}

		@Override
		public Boolean eqs(Iterable<Boolean> elems) {
			Iterator<Boolean> iter = elems.iterator();

			assert iter.hasNext();
			boolean fst = iter.next();

			while (iter.hasNext()) {
				if (iter.next() != fst)
					return false;
			}

			return true;
		}
	};

	public static Comparator<Boolean> COMPARATOR = new Comparator<Boolean>() {
		@Override
		public int compare(Boolean o1, Boolean o2) {
			return o1.compareTo(o2);
		}
	};
}
