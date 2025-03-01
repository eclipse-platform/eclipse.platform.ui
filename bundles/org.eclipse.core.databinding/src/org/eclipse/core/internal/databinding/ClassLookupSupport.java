/*******************************************************************************
 * Copyright (c) 2006, 2025 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.internal.databinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @since 1.0
 */
public class ClassLookupSupport {

	/*
	 * code copied from AdapterManager.java
	 */
	private static HashMap<Class<?>, List<Class<?>>> classSearchOrderLookup;

	/**
	 * For a given class or interface, return an array containing the given type and
	 * all its direct and indirect supertypes.
	 *
	 * @param type the requested type
	 * @return an array containing the given type and all its direct and indirect
	 *         supertypes
	 */
	public static Class<?>[] getTypeHierarchyFlattened(Class<?> type) {
		List<Class<?>> classes = null;
		// cache reference to lookup to protect against concurrent flush
		HashMap<Class<?>, List<Class<?>>> lookup = classSearchOrderLookup;
		if (lookup != null)
			classes = lookup.get(type);
		// compute class order only if it hasn't been cached before
		if (classes == null) {
			classes = new ArrayList<>();
			computeClassOrder(type, classes);
			if (lookup == null)
				classSearchOrderLookup = lookup = new HashMap<>();
			lookup.put(type, classes);
		}
		return classes.toArray(Class[]::new);
	}

	/**
	 * Builds and returns a table of adapters for the given adaptable type. The
	 * table is keyed by adapter class name. The value is the <b>sole</b> factory
	 * that defines that adapter. Note that if multiple adapters technically
	 * define the same property, only the first found in the search order is
	 * considered.
	 *
	 * Note that it is important to maintain a consistent class and interface
	 * lookup order. See the class comment for more details.
	 */
	private static void computeClassOrder(Class<?> adaptable,
			Collection<Class<?>> classes) {
		Class<?> clazz = adaptable;
		Set<Class<?>> seen = new HashSet<>(4);
		while (clazz != null) {
			classes.add(clazz);
			computeInterfaceOrder(clazz.getInterfaces(), classes, seen);
			clazz = clazz.isInterface() ? Object.class : clazz.getSuperclass();
		}
	}

	private static void computeInterfaceOrder(Class<?>[] interfaces, Collection<Class<?>> classes, Set<Class<?>> seen) {
		List<Class<?>> newInterfaces = new ArrayList<>(interfaces.length);
		for (Class<?> interfaze : interfaces) {
			if (seen.add(interfaze)) {
				//note we cannot recurse here without changing the resulting interface order
				classes.add(interfaze);
				newInterfaces.add(interfaze);
			}
		}
		for (Class<?> interfaze : newInterfaces)
			computeInterfaceOrder(interfaze.getInterfaces(), classes, seen);
	}

}
