/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.commands.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.SortedMap;
import java.util.TreeMap;

public final class Util {

	public final static String ZERO_LENGTH_STRING = ""; //$NON-NLS-1$

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

	public static String getString(ResourceBundle resourceBundle, String key)
		throws IllegalArgumentException {
		if (resourceBundle == null || key == null)
			throw new IllegalArgumentException();

		String value = key;

		try {
			value = resourceBundle.getString(key);
		} catch (MissingResourceException eMissingResource) {
			System.err.println(eMissingResource);
		}
		
		return value != null ? value.trim() : null;
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

	public static List safeCopy(List list, Class c)
		throws IllegalArgumentException {
		if (list == null || c == null)
			throw new IllegalArgumentException();
			
		list = Collections.unmodifiableList(new ArrayList(list));
		Iterator iterator = list.iterator();
	
		while (iterator.hasNext())
			if (!c.isInstance(iterator.next()))
				throw new IllegalArgumentException();		

		return list;
	}

	public static Map safeCopy(Map map, Class keyClass, Class valueClass)
		throws IllegalArgumentException {
		if (map == null || keyClass == null || valueClass == null)
			throw new IllegalArgumentException();
			
		map = Collections.unmodifiableMap(new HashMap(map));
		Iterator iterator = map.entrySet().iterator();
	
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();			
			
			if (!keyClass.isInstance(entry.getKey()) || !valueClass.isInstance(entry.getValue()))
				throw new IllegalArgumentException();
		}
		
		return map;
	}

	public static SortedMap safeCopy(SortedMap sortedMap, Class keyClass, Class valueClass)
		throws IllegalArgumentException {
		if (sortedMap == null || keyClass == null || valueClass == null)
			throw new IllegalArgumentException();
			
		sortedMap = Collections.unmodifiableSortedMap(new TreeMap(sortedMap));
		Iterator iterator = sortedMap.entrySet().iterator();
	
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();			
			
			if (!keyClass.isInstance(entry.getKey()) || !valueClass.isInstance(entry.getValue()))
				throw new IllegalArgumentException();
		}
		
		return sortedMap;
	}	
	
	private Util() {
		super();
	}
}
