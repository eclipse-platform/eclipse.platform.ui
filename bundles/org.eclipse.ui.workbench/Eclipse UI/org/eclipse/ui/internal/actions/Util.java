/************************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal.actions;

import java.util.List;

public final class Util {

	public static int compare(Comparable left, Comparable right) {
		if (left == null && right == null)
			return 0;	
		else if (left == null)
			return -1;	
		else if (right == null)
			return +1;
		else
			return left.compareTo(right);
	}

	public static int compare(Comparable[] left, Comparable[] right) {
		if (left == null && right == null)
			return 0;	
		else if (left == null)
			return -1;	
		else if (right == null)
			return +1;
		else {
			int l = left.length;
			int r = right.length;

			if (l != r)
				return l - r;
			else {
				for (int i = 0; i < l; i++) {
					int compareTo = compare(left[i], right[i]);

					if (compareTo != 0)
						return compareTo;
				}

				return 0;
			}
		}
	}

	public static int compare(List left, List right)
		throws ClassCastException {
		if (left == null && right == null)
			return 0;
		else if (left == null)
			return -1;
		else if (right == null)
			return +1;
		else {
			int l = left.size();
			int r = right.size();

			if (l != r)
				return l - r;
			else {
				for (int i = 0; i < l; i++) {
					int compareTo = ((Comparable) left.get(i)).compareTo((Comparable) right.get(i));

					if (compareTo != 0)
						return compareTo;
				}

				return 0;
			}
		}
	}

	public static boolean equals(Object left, Object right) {
		return left == null ? right == null : left.equals(right);
	}

	public static boolean isChildOf(Object[] left, Object[] right, boolean equals) {
		if (left == null || right == null)
			return false;
		else {
			int l = left.length;
			int r = right.length;

			if (r > l || !equals && r == l)
				return false;

			for (int i = 0; i < r; i++)
				if (!equals(left[i], right[i]))
					return false;

			return true;
		}
	}

	public static boolean isChildOf(List left, List right, boolean equals) {
		if (left == null || right == null)
			return false;
		else {
			int l = left.size();
			int r = right.size();

			if (r > l || !equals && r == l)
				return false;

			for (int i = 0; i < r; i++)
				if (!equals(left.get(i), right.get(i)))
					return false;

			return true;
		}
	}

	public static int hashCode(Object object) {
		return object != null ? object.hashCode() : 0;	
	}

	private Util() {
		super();
	}
}
