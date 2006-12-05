/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.internal.databinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @since 1.0
 *
 */
public class ClassLookupSupport {
	
	/*
	 * code copied from AdapterManager.java
	 */
	private static HashMap classSearchOrderLookup;

	/**
	 * For a given class or interface, return an array containing the given type and all its direct and indirect supertypes.
	 * @param type
	 * @return an array containing the given type and all its direct and indirect supertypes
	 */
	public static Class[] getTypeHierarchyFlattened(Class type) {
		List classes = null;
		//cache reference to lookup to protect against concurrent flush
		HashMap lookup = classSearchOrderLookup;
		if (lookup != null)
			classes = (List) lookup.get(type);
		// compute class order only if it hasn't been cached before
		if (classes == null) {
			classes = new ArrayList();
			computeClassOrder(type, classes);
			if (lookup == null)
				classSearchOrderLookup = lookup = new HashMap();
			lookup.put(type, classes);
		}
		return (Class[]) classes.toArray(new Class[classes.size()]);
	}

	/**
	 * Builds and returns a table of adapters for the given adaptable type.
	 * The table is keyed by adapter class name. The
	 * value is the <b>sole<b> factory that defines that adapter. Note that
	 * if multiple adapters technically define the same property, only the
	 * first found in the search order is considered.
	 * 
	 * Note that it is important to maintain a consistent class and interface
	 * lookup order. See the class comment for more details.
	 */
	private static void computeClassOrder(Class adaptable, Collection classes) {
		Class clazz = adaptable;
		Set seen = new HashSet(4);
		while (clazz != null) {
			classes.add(clazz);
			computeInterfaceOrder(clazz.getInterfaces(), classes, seen);
			clazz = clazz.isInterface() ? Object.class : clazz.getSuperclass();
		}
	}

	private static void computeInterfaceOrder(Class[] interfaces, Collection classes, Set seen) {
		List newInterfaces = new ArrayList(interfaces.length);
		for (int i = 0; i < interfaces.length; i++) {
			Class interfac = interfaces[i];
			if (seen.add(interfac)) {
				//note we cannot recurse here without changing the resulting interface order
				classes.add(interfac);
				newInterfaces.add(interfac);
			}
		}
		for (Iterator it = newInterfaces.iterator(); it.hasNext();)
			computeInterfaceOrder(((Class) it.next()).getInterfaces(), classes, seen);
	}


}
