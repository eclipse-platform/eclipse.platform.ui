/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

package org.eclipse.ui.internal.keybindings;

import java.util.Iterator;

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
			for (int i = 0; i < left.length && i < right.length; i++) {
				int compareTo = compare(left[i], right[i]);
				
				if (compareTo != 0)
					return compareTo;
			}
			
			return left.length - right.length;
		}
	}

	public static int compare(Iterator left, Iterator right)
		throws ClassCastException {
		if (left == null && right == null)
			return 0;	
		else if (left == null)
			return -1;	
		else if (right == null)
			return +1;
		else {
			while (left.hasNext() && right.hasNext()) {
				int compareTo = ((Comparable) left.next()).compareTo(
					(Comparable) right.next());
			
				if (compareTo != 0)
					return compareTo;
			}
	
			return left.hasNext() ? +1 : right.hasNext() ? -1 : 0;
		}
	}

	private Util() {
		super();
	}
}
