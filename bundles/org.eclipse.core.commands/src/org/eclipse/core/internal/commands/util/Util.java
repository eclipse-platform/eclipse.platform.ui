/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.core.internal.commands.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A class providing utility functions for the commands plug-in.
 * 
 * @since 3.1
 */
public final class Util {

    /**
     * A shared, unmodifiable, empty, sorted map. This value is guaranteed to
     * always be the same.
     */
    public final static SortedMap EMPTY_SORTED_MAP = Collections
            .unmodifiableSortedMap(new TreeMap());

    /**
     * A shared, unmodifiable, empty, sorted set. This value is guaranteed to
     * always be the same.
     */
    public final static SortedSet EMPTY_SORTED_SET = Collections
            .unmodifiableSortedSet(new TreeSet());

    /**
     * A shared, zero-length string -- for avoiding non-externalized string
     * tags. This value is guaranteed to always be the same.
     */
    public final static String ZERO_LENGTH_STRING = ""; //$NON-NLS-1$

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
    public static final void assertInstance(final Object object, final Class c,
            final boolean allowNull) {
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
        return left == false ? (right == true ? -1 : 0) : (right == true ? 0
                : 1);
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
    public static final int compare(final Comparable left,
            final Comparable right) {
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
     * Decides whether two booleans are equal.
     * 
     * @param left
     *            The first boolean to compare; may be <code>null</code>.
     * @param right
     *            The second boolean to compare; may be <code>null</code>.
     * @return <code>true</code> if the booleans are equal; <code>false</code>
     *         otherwise.
     */
    public static final boolean equals(final boolean left, final boolean right) {
        return left == right;
    }

    /**
     * Decides whether two objects are equal -- defending against
     * <code>null</code>.
     * 
     * @param left
     *            The first object to compare; may be <code>null</code>.
     * @param right
     *            The second object to compare; may be <code>null</code>.
     * @return <code>true</code> if the objects are equals; <code>false</code>
     *         otherwise.
     */
    public static final boolean equals(final Object left, final Object right) {
        return left == null ? right == null : ((right != null) && left
                .equals(right));
    }

	/**
	 * Tests whether two arrays of objects are equal to each other. The arrays
	 * must not be <code>null</code>, but their elements may be
	 * <code>null</code>.
	 * 
	 * @param leftArray
	 *            The left array to compare; may be <code>null</code>, and
	 *            may be empty and may contain <code>null</code> elements.
	 * @param rightArray
	 *            The right array to compare; may be <code>null</code>, and
	 *            may be empty and may contain <code>null</code> elements.
	 * @return <code>true</code> if the arrays are equal length and the
	 *         elements at the same position are equal; <code>false</code>
	 *         otherwise.
	 */
	public static final boolean equals(final Object[] leftArray,
			final Object[] rightArray) {
		if (leftArray == null) {
			return (rightArray == null);
		} else if (rightArray == null) {
			return false;
		}

		if (leftArray.length != rightArray.length) {
			return false;
		}

		for (int i = 0; i < leftArray.length; i++) {
			final Object left = leftArray[i];
			final Object right = rightArray[i];
			final boolean equal = (left == null) ? (right == null) : (left
					.equals(right));
			if (!equal) {
				return false;
			}
		}

		return true;
	}

    /**
     * Computes the hash code for an integer.
     * 
     * @param i
     *            The integer for which a hash code should be computed.
     * @return <code>i</code>.
     */
    public static final int hashCode(final int i) {
        return i;
    }

    /**
     * Computes the hash code for an object, but with defense against
     * <code>null</code>.
     * 
     * @param object
     *            The object for which a hash code is needed; may be
     *            <code>null</code>.
     * @return The hash code for <code>object</code>; or <code>0</code> if
     *         <code>object</code> is <code>null</code>.
     */
    public static final int hashCode(final Object object) {
        return object != null ? object.hashCode() : 0;
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
    public static final Map safeCopy(final Map map, final Class keyClass,
            final Class valueClass, final boolean allowNullKeys,
            final boolean allowNullValues) {
        if (map == null || keyClass == null || valueClass == null) {
			throw new NullPointerException();
		}

        final Map copy = Collections.unmodifiableMap(new HashMap(map));
        final Iterator iterator = copy.entrySet().iterator();

        while (iterator.hasNext()) {
            final Map.Entry entry = (Map.Entry) iterator.next();
            assertInstance(entry.getKey(), keyClass, allowNullKeys);
            assertInstance(entry.getValue(), valueClass, allowNullValues);
        }

        return map;
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
    public static final Set safeCopy(final Set set, final Class c) {
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
    public static final Set safeCopy(final Set set, final Class c,
            final boolean allowNullElements) {
        if (set == null || c == null) {
			throw new NullPointerException();
		}

        final Set copy = Collections.unmodifiableSet(new HashSet(set));
        final Iterator iterator = copy.iterator();

        while (iterator.hasNext()) {
			assertInstance(iterator.next(), c, allowNullElements);
		}

        return set;
    }

    /**
     * The utility class is meant to just provide static members.
     */
    private Util() {
        // Should not be called.
    }
}
