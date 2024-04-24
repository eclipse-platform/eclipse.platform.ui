/*******************************************************************************
 * Copyright (c) 2013, 2015 IBM Corporation and others.
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
package org.eclipse.e4.ui.internal.workbench;

import java.util.Collections;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * <p>
 * A static class providing utility methods to all of JFace.
 * </p>
 *
 * @since 3.1
 */
public final class Util {

	/**
	 * An unmodifiable, empty, sorted set. This value is guaranteed to never change and never be
	 * <code>null</code>.
	 */
	public static final SortedSet<?> EMPTY_SORTED_SET = Collections.unmodifiableSortedSet(new TreeSet<>());

	/**
	 * A common zero-length string. It avoids needing write <code>NON-NLS</code> next to code
	 * fragments. It's also a bit clearer to read.
	 */
	public static final String ZERO_LENGTH_STRING = ""; //$NON-NLS-1$

	/**
	 * Verifies that the given object is an instance of the given class.
	 *
	 * @param object
	 *            The object to check; may be <code>null</code>.
	 * @param c
	 *            The class which the object should be; must not be <code>null</code>.
	 */
	public static void assertInstance(final Object object, final Class<?> c) {
		assertInstance(object, c, false);
	}

	/**
	 * Verifies the given object is an instance of the given class. It is possible to specify
	 * whether the object is permitted to be <code>null</code>.
	 *
	 * @param object
	 *            The object to check; may be <code>null</code>.
	 * @param c
	 *            The class which the object should be; must not be <code>null</code>.
	 * @param allowNull
	 *            Whether the object is allowed to be <code>null</code>.
	 */
	private static void assertInstance(final Object object, final Class<?> c,
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
	 * Compares two boolean values. <code>false</code> is considered to be "less than"
	 * <code>true</code>.
	 *
	 * @param left
	 *            The left value to compare
	 * @param right
	 *            The right value to compare
	 * @return <code>-1</code> if the left is <code>false</code> and the right is <code>true</code>.
	 *         <code>1</code> if the opposite is true. If they are equal, then it returns
	 *         <code>0</code>.
	 */
	public static int compare(final boolean left, final boolean right) {
		return !left ? (right ? -1 : 0) : 1;
	}

	/**
	 * Compares two integer values.
	 *
	 * @param left
	 *            The left value to compare
	 * @param right
	 *            The right value to compare
	 * @return <code>left - right</code>
	 */
	public static int compare(final int left, final int right) {
		return left - right;
	}

	/**
	 * Compares to comparable objects -- defending against <code>null</code>.
	 *
	 * @param left
	 *            The left object to compare; may be <code>null</code>.
	 * @param right
	 *            The right object to compare; may be <code>null</code>.
	 * @return The result of the comparison. <code>null</code> is considered to be the least
	 *         possible value.
	 */
	public static <T extends Comparable<T>> int compare(final T left, final T right) {
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
	 * Compares two arrays of comparable objects -- accounting for <code>null</code>.
	 *
	 * @param left
	 *            The left array to be compared; may be <code>null</code>.
	 * @param right
	 *            The right array to be compared; may be <code>null</code>.
	 * @return The result of the comparison. <code>null</code> is considered to be the least
	 *         possible value. A shorter array is considered less than a longer array.
	 */
	public static <T extends Comparable<T>> int compare(final T[] left, final T[] right) {
		if (left == null && right == null) {
			return 0;
		} else if (left == null) {
			return -1;
		} else if (right == null) {
			return 1;
		} else {
			int l = left.length;
			int r = right.length;

			if (l != r) {
				return l - r;
			}

			for (int i = 0; i < l; i++) {
				int compareTo = compare(left[i], right[i]);

				if (compareTo != 0) {
					return compareTo;
				}
			}

			return 0;
		}
	}

	/**
	 * Compares two lists -- account for <code>null</code>. The lists must contain comparable
	 * objects.
	 *
	 * @param left
	 *            The left list to compare; may be <code>null</code>. This list must only contain
	 *            instances of <code>Comparable</code>.
	 * @param right
	 *            The right list to compare; may be <code>null</code>. This list must only contain
	 *            instances of <code>Comparable</code>.
	 * @return The result of the comparison. <code>null</code> is considered to be the least
	 *         possible value. A shorter list is considered less than a longer list.
	 */
	public static <T extends Comparable<T>> int compare(final List<T> left, final List<T> right) {
		if (left == null && right == null) {
			return 0;
		} else if (left == null) {
			return -1;
		} else if (right == null) {
			return 1;
		} else {
			int l = left.size();
			int r = right.size();

			if (l != r) {
				return l - r;
			}

			for (int i = 0; i < l; i++) {
				int compareTo = compare(left.get(i), right.get(i));

				if (compareTo != 0) {
					return compareTo;
				}
			}

			return 0;
		}
	}

	/**
	 * Tests whether the first array ends with the second array.
	 *
	 * @param left
	 *            The array to check (larger); may be <code>null</code>.
	 * @param right
	 *            The array that should be a subsequence (smaller); may be <code>null</code>.
	 * @param equals
	 *            Whether the two array are allowed to be equal.
	 * @return <code>true</code> if the second array is a subsequence of the array list, and they
	 *         share end elements.
	 */
	public static boolean endsWith(final Object[] left, final Object[] right,
			final boolean equals) {
		if (left == null || right == null) {
			return false;
		}

		int l = left.length;
		int r = right.length;

		if (r > l || !equals && r == l) {
			return false;
		}

		for (int i = 0; i < r; i++) {
			if (!Objects.equals(left[l - i - 1], right[r - i - 1])) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Checks whether the second array is a subsequence of the first array, and that they share
	 * common starting elements.
	 *
	 * @param left
	 *            The first array to compare (large); may be <code>null</code>.
	 * @param right
	 *            The second array to compare (small); may be <code>null</code>.
	 * @param equals
	 *            Whether it is allowed for the two arrays to be equivalent.
	 * @return <code>true</code> if the first arrays starts with the second list; <code>false</code>
	 *         otherwise.
	 */
	public static boolean startsWith(final Object[] left, final Object[] right,
			final boolean equals) {
		if (left == null || right == null) {
			return false;
		}

		int l = left.length;
		int r = right.length;

		if (r > l || !equals && r == l) {
			return false;
		}

		for (int i = 0; i < r; i++) {
			if (!Objects.equals(left[i], right[i])) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Converts an array into a string representation that is suitable for debugging.
	 *
	 * @param array
	 *            The array to convert; may be <code>null</code>.
	 * @return The string representation of the array; never <code>null</code>.
	 */
	public static String toString(final Object[] array) {
		if (array == null) {
			return "null"; //$NON-NLS-1$
		}

		final StringBuilder buffer = new StringBuilder();
		buffer.append('[');

		final int length = array.length;
		for (int i = 0; i < length; i++) {
			if (i != 0) {
				buffer.append(',');
			}
			final Object object = array[i];
			final String element = String.valueOf(object);
			buffer.append(element);
		}
		buffer.append(']');

		return buffer.toString();
	}

	/**
	 * Provides a translation of a particular key from the resource bundle.
	 *
	 * @param resourceBundle
	 *            The key to look up in the resource bundle; should not be <code>null</code>.
	 * @param key
	 *            The key to look up in the resource bundle; should not be <code>null</code>.
	 * @param defaultString
	 *            The value to return if the resource cannot be found; may be <code>null</code>.
	 * @return The value of the translated resource at <code>key</code>. If the key cannot be found,
	 *         then it is simply the <code>defaultString</code>.
	 */
	public static String translateString(final ResourceBundle resourceBundle,
			final String key, final String defaultString) {
		if (resourceBundle != null && key != null) {
			try {
				final String translatedString = resourceBundle.getString(key);

				if (translatedString != null) {
					return translatedString;
				}
			} catch (MissingResourceException eMissingResource) {
				// Such is life. We'll return the key
			}
		}

		return defaultString;
	}

	/**
	 * Foundation replacement for <code>String#replaceAll(String,
	 * String)</code>, but <strong>without support for regular expressions</strong>.
	 *
	 * @param src
	 *            the original string
	 * @param find
	 *            the string to find
	 * @param replacement
	 *            the replacement string
	 * @return the new string, with all occurrences of <code>find</code> replaced by
	 *         <code>replacement</code> (not using regular expressions)
	 * @since 3.4
	 */
	public static String replaceAll(String src, String find, String replacement) {
		final int len = src.length();
		final int findLen = find.length();

		int idx = src.indexOf(find);
		if (idx < 0) {
			return src;
		}

		StringBuilder buf = new StringBuilder();
		int beginIndex = 0;
		while (idx != -1 && idx < len) {
			buf.append(src.substring(beginIndex, idx));
			buf.append(replacement);

			beginIndex = idx + findLen;
			if (beginIndex < len) {
				idx = src.indexOf(find, beginIndex);
			} else {
				idx = -1;
			}
		}
		if (beginIndex < len) {
			buf.append(src.substring(beginIndex, (idx == -1 ? len : idx)));
		}
		return buf.toString();
	}

	/**
	 * This class should never be constructed.
	 */
	private Util() {
		// Not allowed.
	}
}
