/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jface.viewers.deferred;

import java.util.HashMap;

/**
 * Represents a map of objects onto ints. This is intended for future optimization:
 * using int primitives would allow for an implementation that doesn't require
 * additional object allocations for Integers. However, the current implementation
 * simply delegates to the Java HashMap class.
 *
 * @since 3.1
 */
/* package */ class IntHashMap {
	private HashMap map;

	public IntHashMap(int size, float loadFactor) {
		map = new HashMap(size, loadFactor);
	}

	public IntHashMap() {
		map = new HashMap();
	}

	public void remove(Object key) {
		map.remove(key);
	}

	public void put(Object key, int value) {
		map.put(key, Integer.valueOf(value));
	}

	/**
	 * @return the int value at the given key
	 */
	public int get(Object key) {
		return get(key, 0);
	}

	/**
	 * @return the int value at the given key, or the default value if this map does not contain the given key
	 */
	public int get(Object key, int defaultValue) {
		Integer result = (Integer)map.get(key);

		if (result != null) {
			return result.intValue();
		}

		return defaultValue;
	}

	/**
	 * @return <code>true</code> if this map contains the given key, <code>false</code> otherwise
	 */
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	/**
	 * @return the number of key/value pairs
	 */
	public int size() {
		return map.size();
	}
}
