/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
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

package org.eclipse.ui.internal.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;

public final class Util {

	public static final String ZERO_LENGTH_STRING = ""; //$NON-NLS-1$

	public static final String[] EMPTY_STRING_ARRAY = new String[0];

	/**
	 * Ensures that a string is not null. Converts null strings into empty strings,
	 * and leaves any other string unmodified. Use this to help wrap calls to
	 * methods that return null instead of the empty string. Can also help protect
	 * against implementation errors in methods that are not supposed to return
	 * null.
	 *
	 * @param input input string (may be null)
	 * @return input if not null, or the empty string if input is null
	 */
	public static String safeString(String input) {
		if (input != null) {
			return input;
		}

		return ZERO_LENGTH_STRING;
	}

	public static void assertInstance(Object object, Class<?> c) {
		assertInstance(object, c, false);
	}

	public static void assertInstance(Object object, Class<?> c, boolean allowNull) {
		if (object == null && allowNull) {
			return;
		}

		if (object == null || c == null) {
			throw new NullPointerException();
		} else if (!c.isInstance(object)) {
			throw new IllegalArgumentException();
		}
	}

	public static int compare(boolean left, boolean right) {
		return !left ? (right ? -1 : 0) : 1;
	}

	public static int compare(Comparable left, Comparable right) {
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

	public static int compare(Comparable[] left, Comparable[] right) {
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

	public static int compare(int left, int right) {
		return left - right;
	}

	public static int compare(List left, List right) {
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
				int compareTo = compare((Comparable) left.get(i), (Comparable) right.get(i));

				if (compareTo != 0) {
					return compareTo;
				}
			}

			return 0;
		}
	}

	public static int compare(Object left, Object right) {
		if (left == null && right == null) {
			return 0;
		} else if (left == null) {
			return -1;
		} else if (right == null) {
			return 1;
		} else if (left == right) {
			return 0;
		} else {
			return compare(System.identityHashCode(left), System.identityHashCode(right));
		}
	}

	/**
	 * An optimized comparison that uses identity hash codes to perform the
	 * comparison between non- <code>null</code> objects.
	 *
	 * @param left  The left-hand side of the comparison; may be <code>null</code>.
	 * @param right The right-hand side of the comparison; may be <code>null</code>.
	 * @return <code>0</code> if they are the same, <code>-1</code> if left is
	 *         <code>null</code>;<code>1</code> if right is <code>null</code>.
	 *         Otherwise, the left identity hash code minus the right identity hash
	 *         code.
	 */
	public static int compareIdentity(Object left, Object right) {
		if (left == null && right == null) {
			return 0;
		} else if (left == null) {
			return -1;
		} else if (right == null) {
			return 1;
		} else {
			return System.identityHashCode(left) - System.identityHashCode(right);
		}
	}

	public static boolean endsWith(List left, List right, boolean equals) {
		if (left == null || right == null) {
			return false;
		}
		int l = left.size();
		int r = right.size();

		if (r > l || !equals && r == l) {
			return false;
		}

		for (int i = 0; i < r; i++) {
			if (!Objects.equals(left.get(l - i - 1), right.get(r - i - 1))) {
				return false;
			}
		}

		return true;
	}

	public static List safeCopy(List list, Class c) {
		return safeCopy(list, c, false);
	}

	public static List safeCopy(List list, Class c, boolean allowNullElements) {
		if (list == null || c == null) {
			throw new NullPointerException();
		}

		list = Collections.unmodifiableList(new ArrayList(list));
		Iterator iterator = list.iterator();

		while (iterator.hasNext()) {
			assertInstance(iterator.next(), c, allowNullElements);
		}

		return list;
	}

	public static Map safeCopy(Map map, Class keyClass, Class valueClass, boolean allowNullKeys,
			boolean allowNullValues) {
		if (map == null || keyClass == null || valueClass == null) {
			throw new NullPointerException();
		}

		map = Collections.unmodifiableMap(new HashMap(map));
		Iterator iterator = map.entrySet().iterator();

		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			assertInstance(entry.getKey(), keyClass, allowNullKeys);
			assertInstance(entry.getValue(), valueClass, allowNullValues);
		}

		return map;
	}

	public static Set safeCopy(Set set, Class c) {
		return safeCopy(set, c, false);
	}

	public static Set safeCopy(Set set, Class c, boolean allowNullElements) {
		if (set == null || c == null) {
			throw new NullPointerException();
		}

		set = Collections.unmodifiableSet(new HashSet(set));
		Iterator iterator = set.iterator();

		while (iterator.hasNext()) {
			assertInstance(iterator.next(), c, allowNullElements);
		}

		return set;
	}

	public static SortedSet safeCopy(SortedSet sortedSet, Class c) {
		return safeCopy(sortedSet, c, false);
	}

	public static SortedSet safeCopy(SortedSet sortedSet, Class c, boolean allowNullElements) {
		if (sortedSet == null || c == null) {
			throw new NullPointerException();
		}

		sortedSet = Collections.unmodifiableSortedSet(new TreeSet(sortedSet));
		Iterator iterator = sortedSet.iterator();

		while (iterator.hasNext()) {
			assertInstance(iterator.next(), c, allowNullElements);
		}

		return sortedSet;
	}

	public static boolean startsWith(List left, List right, boolean equals) {
		if (left == null || right == null) {
			return false;
		}
		int l = left.size();
		int r = right.size();

		if (r > l || !equals && r == l) {
			return false;
		}

		for (int i = 0; i < r; i++) {
			if (!Objects.equals(left.get(i), right.get(i))) {
				return false;
			}
		}

		return true;
	}

	public static String translateString(ResourceBundle resourceBundle, String key) {
		return Util.translateString(resourceBundle, key, key, true, true);
	}

	public static String translateString(ResourceBundle resourceBundle, String key, String string, boolean signal,
			boolean trim) {
		if (resourceBundle != null && key != null) {
			try {
				final String translatedString = resourceBundle.getString(key);

				if (translatedString != null) {
					return trim ? translatedString.trim() : translatedString;
				}
			} catch (MissingResourceException eMissingResource) {
				if (signal) {
					WorkbenchPlugin.log(eMissingResource);
				}
			}
		}

		return trim ? string.trim() : string;
	}

	public static void arrayCopyWithRemoval(Object[] src, Object[] dst, int idxToRemove) {
		if (src == null || dst == null || src.length - 1 != dst.length || idxToRemove < 0
				|| idxToRemove >= src.length) {
			throw new IllegalArgumentException();
		}

		if (idxToRemove == 0) {
			System.arraycopy(src, 1, dst, 0, src.length - 1);
		} else if (idxToRemove == src.length - 1) {
			System.arraycopy(src, 0, dst, 0, src.length - 1);
		} else {
			System.arraycopy(src, 0, dst, 0, idxToRemove);
			System.arraycopy(src, idxToRemove + 1, dst, idxToRemove, src.length - idxToRemove - 1);
		}
	}

	private Util() {
	}

	/**
	 * Returns an interned representation of the given string
	 *
	 * @param string The string to intern
	 * @return The interned string
	 */
	public static String intern(String string) {
		return string == null ? null : string.intern();
	}

	/**
	 * Two {@link String}s presented in a list form. This method can be used to form
	 * a longer list by providing a list for <code>item1</code> and an item to
	 * append to the list for <code>item2</code>.
	 *
	 * @param item1 a string
	 * @param item2 a string
	 * @return a string which presents <code>item1</code> and <code>item2</code> in
	 *         a list form.
	 */
	public static String createList(String item1, String item2) {
		return NLS.bind(WorkbenchMessages.Util_List, item1, item2);
	}

	/**
	 * Return the window for the given shell or the currently active window if one
	 * could not be determined.
	 *
	 * @param shellToCheck the shell to search on
	 * @return the window for the given shell or the currently active window if one
	 *         could not be determined
	 * @since 3.2
	 */
	public static IWorkbenchWindow getWorkbenchWindowForShell(Shell shellToCheck) {
		IWorkbenchWindow workbenchWindow = null;
		while (workbenchWindow == null && shellToCheck != null) {
			if (shellToCheck.getData() instanceof IWorkbenchWindow) {
				workbenchWindow = (IWorkbenchWindow) shellToCheck.getData();
			} else {
				shellToCheck = (Shell) shellToCheck.getParent();
			}
		}

		if (workbenchWindow == null) {
			workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		}

		return workbenchWindow;
	}

	/**
	 * Return an appropriate shell to parent dialogs on. This will be one of the
	 * workbench windows (the active one) should any exist. Otherwise
	 * <code>null</code> is returned.
	 *
	 * @return the shell to parent on or <code>null</code> if there is no
	 *         appropriate shell
	 * @since 3.3
	 */
	public static Shell getShellToParentOn() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow activeWindow = workbench.getActiveWorkbenchWindow();
		IWorkbenchWindow windowToParentOn = activeWindow == null
				? (workbench.getWorkbenchWindowCount() > 0 ? workbench.getWorkbenchWindows()[0] : null)
				: activeWindow;
		return windowToParentOn == null ? null : windowToParentOn.getShell();
	}

	/**
	 * A String#split(*) replacement that splits on the provided char. No Regex
	 * involved.
	 *
	 * @param src   The string to be split
	 * @param delim The character to split on
	 * @return An array containing the split. Might be empty, but will not be
	 *         <code>null</code>.
	 */
	public static String[] split(String src, char delim) {
		if (src == null) {
			return EMPTY_STRING_ARRAY;
		}

		if (src.isEmpty()) {
			return new String[] { ZERO_LENGTH_STRING };
		}

		ArrayList result = new ArrayList();
		int idx = src.indexOf(delim);
		int lastIdx = 0;
		while (idx != -1) {
			result.add(src.substring(lastIdx, idx));
			lastIdx = idx + 1;
			if (lastIdx == src.length()) {
				idx = -1;
			} else {
				idx = src.indexOf(delim, lastIdx);
			}
		}
		if (lastIdx < src.length()) {
			result.add(src.substring(lastIdx));
		}
		String[] resultArray = (String[]) result.toArray(new String[result.size()]);
		boolean allEmpty = true;
		for (int i = 0; i < resultArray.length && allEmpty; i++) {
			if (resultArray[i].length() > 0) {
				allEmpty = false;
			}
		}
		if (allEmpty) {
			return EMPTY_STRING_ARRAY;
		}
		return resultArray;
	}

	/**
	 * Foundation replacement for String.replaceAll(*).
	 *
	 * @param src         the starting string.
	 * @param find        the string to find.
	 * @param replacement the string to replace.
	 * @return The new string.
	 * @since 3.3
	 */
	public static String replaceAll(String src, String find, String replacement) {
		return org.eclipse.jface.util.Util.replaceAll(src, find, replacement);
	}

	/**
	 * Attempt to load the executable extension from the element/attName. If the
	 * load fails or the resulting object is not castable to the provided classSpec
	 * (if any) an error is logged and a null is returned.
	 *
	 * @param element   The {@link IConfigurationElement} containing the executable
	 *                  extension's specification
	 * @param attName   The attribute name of the executable extension
	 * @param classSpec An optional <code>Class</code> defining the type that the
	 *                  loaded Object must be castable to. This is optional to
	 *                  support code where the client has a choice of mutually
	 *                  non-castable types to choose from.
	 *
	 * @return The loaded object which is guaranteed to be castable to the given
	 *         classSpec or null if a failure occurred
	 */
	public static Object safeLoadExecutableExtension(IConfigurationElement element, String attName, Class classSpec) {
		Object loadedEE = null;

		// Load the handler.
		try {
			loadedEE = element.createExecutableExtension(attName);
		} catch (final CoreException e) {
			// TODO: give more info (eg plugin id)....
			// Gather formatting info
			final String classDef = element.getAttribute(attName);

			final String message = "Class load Failure: '" + classDef + "'"; //$NON-NLS-1$//$NON-NLS-2$
			IStatus status = new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, 0, message, e);
			WorkbenchPlugin.log(message, status);
		}

		// Check the loaded object's type
		if (classSpec != null && loadedEE != null && !classSpec.isInstance(loadedEE)) {
			// ooops, the loaded class is not castable to the given type
			final String message = "Loaded class is of incorrect type: expected(" + //$NON-NLS-1$
					classSpec.getName() + ") got (" + loadedEE.getClass().getName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$

			IllegalArgumentException e = new IllegalArgumentException(message);
			final IStatus status = new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, 0, message, e);
			WorkbenchPlugin.log(message, status);

			// This 'failed'
			loadedEE = null;
		}

		return loadedEE;
	}

}
