/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.preferences;

import java.util.HashMap;

/**
 * A string pool is used for sharing strings in a way that eliminates duplicate
 * equal strings.  A string pool instance can be maintained over a long period
 * of time, or used as a temporary structure during a string sharing pass over
 * a data structure.
 * <p>
 * This class is not intended to be subclassed by clients.
 * </p>
 * 
 * Note: This class is copied from org.eclipse.core.resources
 * 
 * @since 3.1
 */
public final class StringPool {
	private final HashMap map = new HashMap();

	/**
	 * Adds a <code>String</code> to the pool.  Returns a <code>String</code>
	 * that is equal to the argument but that is unique within this pool.
	 * @param string The string to add to the pool
	 * @return A string that is equal to the argument.
	 */
	public String add(String string) {
		if (string == null)
			return string;
		Object result = map.get(string);
		if (result != null)
			return (String) result;
		map.put(string, string);
		return string;
	}
}
