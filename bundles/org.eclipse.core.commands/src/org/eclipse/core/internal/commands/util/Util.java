/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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

package org.eclipse.core.internal.commands.util;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.commands.Command;

/**
 * A class providing utility functions for the commands plug-in.
 *
 * @since 3.1
 */
public final class Util {

	/**
	 * A shared, zero-length string -- for avoiding non-externalized string
	 * tags. This value is guaranteed to always be the same.
	 */
	public static final String ZERO_LENGTH_STRING = ""; //$NON-NLS-1$

	/**
	 * Asserts the the given object is an instance of the given class --
	 * optionally allowing the object to be <code>null</code>.
	 *
	 * @param object
	 *            The object for which the type should be checked.
	 * @param c
	 *            The class that the object must be; fails if the class is
	 *            <code>null</code>.
	 * @param allowNull
	 *            Whether the object being <code>null</code> will not cause a
	 *            failure.
	 */
	public static final void assertInstance(final Object object, final Class<?> c, final boolean allowNull) {
		if (object == null && allowNull) {
			return;
		}

		if (object == null || c == null) {
			throw new NullPointerException();
		} else if (!c.isInstance(object)) {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Compares two boolean values. <code>false</code> is considered to be
	 * less than <code>true</code>.
	 *
	 * @param left
	 *            The left value to compare.
	 * @param right
	 *            The right value to compare.
	 * @return <code>-1</code> if <code>left</code> is <code>false</code>
	 *         and <code>right</code> is <code>true</code>;<code>0</code>
	 *         if they are equal; <code>1</code> if <code>left</code> is
	 *         <code>true</code> and <code>right</code> is
	 *         <code>false</code>
	 */
	public static final int compare(final boolean left, final boolean right) {
		return left == false ? (right == true ? -1 : 0) : (right == true ? 0 : 1);
	}

	/**
	 * Compares two comparable objects, but with protection against
	 * <code>null</code>.
	 *
	 * @param left
	 *            The left value to compare; may be <code>null</code>.
	 * @param right
	 *            The right value to compare; may be <code>null</code>.
	 * @return <code>-1</code> if <code>left</code> is <code>null</code>
	 *         and <code>right</code> is not <code>null</code>;
	 *         <code>0</code> if they are both <code>null</code>;
	 *         <code>1</code> if <code>left</code> is not <code>null</code>
	 *         and <code>right</code> is <code>null</code>. Otherwise, the
	 *         result of <code>left.compareTo(right)</code>.
	 */
	public static final <T extends Comparable<? super T>> int compare(final T left, final T right) {
		if (left == null && right == null) {
			return 0;
		} else if (left == null) {
			return -1;
		} else if (right == null) {
			return 1;
		} else {
			return left.compareTo(right);
		}
	}

	/**
	 * Compares two integer values. This method fails if the distance between
	 * <code>left</code> and <code>right</code> is greater than
	 * <code>Integer.MAX_VALUE</code>.
	 *
	 * @param left
	 *            The left value to compare.
	 * @param right
	 *            The right value to compare.
	 * @return <code>left - right</code>
	 */
	public static final int compare(final int left, final int right) {
		return left - right;
	}

	/**
	 * Compares two objects that are not otherwise comparable. If neither object
	 * is <code>null</code>, then the string representation of each object is
	 * used.
	 *
	 * @param left
	 *            The left value to compare. The string representation of this
	 *            value must not be <code>null</code>.
	 * @param right
	 *            The right value to compare. The string representation of this
	 *            value must not be <code>null</code>.
	 * @return <code>-1</code> if <code>left</code> is <code>null</code>
	 *         and <code>right</code> is not <code>null</code>;
	 *         <code>0</code> if they are both <code>null</code>;
	 *         <code>1</code> if <code>left</code> is not <code>null</code>
	 *         and <code>right</code> is <code>null</code>. Otherwise, the
	 *         result of
	 *         <code>left.toString().compareTo(right.toString())</code>.
	 */
	public static final int compare(final Object left, final Object right) {
		if (left == null && right == null) {
			return 0;
		} else if (left == null) {
			return -1;
		} else if (right == null) {
			return 1;
		} else {
			return left.toString().compareTo(right.toString());
		}
	}

	/**
	 * Makes a type-safe copy of the given map. This method should be used when
	 * a map is crossing an API boundary (i.e., from a hostile plug-in into
	 * internal code, or vice versa).
	 *
	 * @param map
	 *            The map which should be copied; must not be <code>null</code>.
	 * @param keyClass
	 *            The class that all the keys must be; must not be
	 *            <code>null</code>.
	 * @param valueClass
	 *            The class that all the values must be; must not be
	 *            <code>null</code>.
	 * @param allowNullKeys
	 *            Whether <code>null</code> keys should be allowed.
	 * @param allowNullValues
	 *            Whether <code>null</code> values should be allowed.
	 * @return A copy of the map; may be empty, but never <code>null</code>.
	 */
	public static final <K, V> Map<K, V> safeCopy(final Map<K, V> map, final Class<K> keyClass,
			final Class<V> valueClass, final boolean allowNullKeys, final boolean allowNullValues) {
		if (map == null || keyClass == null || valueClass == null) {
			throw new NullPointerException();
		}

		final Map<K, V> copy = Collections.unmodifiableMap(new HashMap<>(map));
		final Iterator<Entry<K, V>> iterator = copy.entrySet().iterator();

		while (iterator.hasNext()) {
			final Entry<K, V> entry = iterator.next();
			assertInstance(entry.getKey(), keyClass, allowNullKeys);
			assertInstance(entry.getValue(), valueClass, allowNullValues);
		}
		return copy;
	}

	/**
	 * Makes a type-safe copy of the given set. This method should be used when
	 * a set is crossing an API boundary (i.e., from a hostile plug-in into
	 * internal code, or vice versa).
	 *
	 * @param set
	 *            The set which should be copied; must not be <code>null</code>.
	 * @param c
	 *            The class that all the values must be; must not be
	 *            <code>null</code>.
	 * @return A copy of the set; may be empty, but never <code>null</code>.
	 *         None of its element will be <code>null</code>.
	 */
	public static final <T> Set<T> safeCopy(final Set<T> set, final Class<T> c) {
		return safeCopy(set, c, false);
	}

	/**
	 * Makes a type-safe copy of the given set. This method should be used when
	 * a set is crossing an API boundary (i.e., from a hostile plug-in into
	 * internal code, or vice versa).
	 *
	 * @param set
	 *            The set which should be copied; must not be <code>null</code>.
	 * @param c
	 *            The class that all the values must be; must not be
	 *            <code>null</code>.
	 * @param allowNullElements
	 *            Whether null values should be allowed.
	 * @return A copy of the set; may be empty, but never <code>null</code>.
	 */
	public static final <T> Set<T> safeCopy(final Set<T> set, final Class<T> c, final boolean allowNullElements) {
		if (set == null || c == null) {
			throw new NullPointerException();
		}

		final Set<T> copy = Collections.unmodifiableSet(new HashSet<>(set));
		final Iterator<T> iterator = copy.iterator();

		while (iterator.hasNext()) {
			assertInstance(iterator.next(), c, allowNullElements);
		}

		return copy;
	}

	/**
	 * Returns context help ID which is directly assigned to the command.
	 * Context help IDs assigned to related handlers are ignored.
	 *
	 * @param command
	 *            The command from which the context help ID is retrieved.
	 * @return The help context ID assigned to the command; may be
	 *         <code>null</code>.
	 */
	public static final String getHelpContextId(Command command) {
		Method method = null;
		try {
			method = Command.class.getDeclaredMethod("getHelpContextId"); //$NON-NLS-1$
		} catch (Exception e) {
			// do nothing
		}

		String contextId = null;
		if (method != null) {
			boolean accessible = method.isAccessible();
			method.setAccessible(true);
			try {
				contextId = (String) method.invoke(command);
			} catch (Exception e) {
				// do nothing
			}
			method.setAccessible(accessible);
		}
		return contextId;
	}

	/**
	 * The utility class is meant to just provide static members.
	 */
	private Util() {
		// Should not be called.
	}
}
