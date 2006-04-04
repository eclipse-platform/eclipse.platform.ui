/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources;

import java.util.*;
import org.eclipse.core.resources.ICommand;

/**
 * This class provides static comparison methods for various core
 * objects that do not implement equals.
 */
public class Comparator {
	public static boolean equals(byte[] b0, byte[] b1) {
		if (b0 == b1) {
			return true;
		}

		if (b0 == null || b1 == null) {
			return false;
		}

		if (b0.length != b1.length) {
			return false;
		}

		for (int i = 0; i < b0.length; ++i) {
			if (b0[i] != b1[i]) {
				return false;
			}
		}

		return true;
	}

	public static boolean equals(Object[] obj0, Object[] obj1) {
		return equals(obj0, obj1, true);
	}

	public static boolean equals(Object[] obj0, Object[] obj1, boolean isOrderImportant) {
		if (obj0 == obj1) {
			return true;
		}

		if (obj0 == null || obj1 == null) {
			return false;
		}

		if (obj0.length != obj1.length) {
			return false;
		}

		if (isOrderImportant) {
			for (int i = 0; i < obj0.length; ++i) {
				if (!equals(obj0[i], obj1[i])) {
					return false;
				}
			}
		} else {
			boolean[] checkList = new boolean[obj0.length];
			for (int i = 0; i < checkList.length; ++i) {
				checkList[i] = false;
			}

			for (int i = 0; i < obj0.length; ++i) {
				boolean found = false;
				for (int j = 0; !found && j < obj1.length; ++j) {
					if (!checkList[j] && equals(obj0[i], obj1[j])) {
						checkList[j] = true;
						found = true;
					}
				}
				if (!found) {
					return false;
				}
			}
		}

		return true;
	}

	public static boolean equals(ICommand[] cs0, ICommand[] cs1) {
		if (cs0 == cs1)
			return true;
		if (cs0 == null || cs1 == null)
			return false;
		if (cs0.length != cs1.length)
			return false;
		for (int i = 0; i < cs0.length; ++i) {
			if (!cs0[i].getBuilderName().equals(cs1[i].getBuilderName()))
				return false;
			if (!equals(cs0[i].getArguments(), cs1[i].getArguments()))
				return false;
		}
		return true;
	}

	public static boolean equals(Object obj0, Object obj1) {
		if (obj0 == obj1) {
			return true;
		}

		if (obj0 == null || obj1 == null) {
			return false;
		}

		if (obj0 instanceof byte[] && obj1 instanceof byte[]) {
			return equals((byte[]) obj0, (byte[]) obj1);
		} else if (obj0 instanceof Enumeration && obj1 instanceof Enumeration) {
			return equals((Enumeration) obj0, (Enumeration) obj1);
		} else if (obj0 instanceof Hashtable && obj1 instanceof Hashtable) {
			return equals((Hashtable) obj0, (Hashtable) obj1);
		} else if (obj0 instanceof ICommand[] && obj1 instanceof ICommand[]) {
			return equals((ICommand[]) obj0, (ICommand[]) obj1);
		} else if (obj0 instanceof Object[] && obj1 instanceof Object[]) {
			return equals((Object[]) obj0, (Object[]) obj1);
		} else if (obj0 instanceof Vector && obj1 instanceof Vector) {
			return equals((Vector) obj0, (Vector) obj1);
		}

		return obj0.equals(obj1);
	}

	public static boolean equals(Enumeration e, Object[] obj) {
		return equals(e, obj, true);
	}

	public static boolean equals(Enumeration e, Object[] obj1, boolean isOrderImportant) {
		Vector v = new Vector();

		while (e.hasMoreElements()) {
			v.addElement(e.nextElement());
		}

		Object[] obj0 = new Object[v.size()];
		v.copyInto(obj0);

		return equals(obj0, obj1, isOrderImportant);
	}

	public static boolean equals(Enumeration e0, Enumeration e1) {
		if (e0 == e1) {
			return true;
		}

		if (e0 == null || e1 == null) {
			return false;
		}

		while (e0.hasMoreElements() && e1.hasMoreElements()) {
			if (!equals(e0.nextElement(), e1.nextElement())) {
				return false;
			}
		}

		if (e0.hasMoreElements() || e1.hasMoreElements()) {
			return false;
		}

		return true;
	}

	public static boolean equals(Hashtable ht0, Hashtable ht1) {
		if (ht0 == ht1) {
			return true;
		}

		if (ht0 == null || ht1 == null) {
			return false;
		}

		if (ht0.size() != ht1.size()) {
			return false;
		}

		Enumeration keys0 = ht0.keys();
		while (keys0.hasMoreElements()) {
			Object key = keys0.nextElement();
			if (!equals(ht0.get(key), ht1.get(key))) {
				return false;
			}
		}

		return true;
	}

	public static boolean equals(Vector c0, Vector c1) {
		if (c0 == c1) {
			return true;
		}

		if (c0 == null || c1 == null) {
			return false;
		}

		if (!equals(c0.elements(), c1.elements())) {
			return false;
		}

		return true;
	}
}
