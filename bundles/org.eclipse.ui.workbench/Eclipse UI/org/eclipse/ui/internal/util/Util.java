/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;

public final class Util {

    public final static SortedMap EMPTY_SORTED_MAP = Collections
            .unmodifiableSortedMap(new TreeMap());

    public final static SortedSet EMPTY_SORTED_SET = Collections
            .unmodifiableSortedSet(new TreeSet());

    public final static String ZERO_LENGTH_STRING = ""; //$NON-NLS-1$

    /**
     * Ensures that a string is not null. Converts null strings into empty
     * strings, and leaves any other string unmodified. Use this to help
     * wrap calls to methods that return null instead of the empty string.
     * Can also help protect against implementation errors in methods that
     * are not supposed to return null. 
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
    
    /**
     * If it is possible to adapt the given object to the given type, this
     * returns the adapter. Performs the following checks:
     * 
     * <ol>
     * <li>Returns <code>sourceObject</code> if it is an instance of the
     * adapter type.</li>
     * <li>If sourceObject implements IAdaptable, it is queried for adapters.</li>
     * <li>If sourceObject is not an instance of PlatformObject (which would have
     * already done so), the adapter manager is queried for adapters</li>
     * </ol>
     * 
     * Otherwise returns null.
     * 
     * @param sourceObject
     *            object to adapt, or null
     * @param adapterType
     *            type to adapt to
     * @return a representation of sourceObject that is assignable to the
     *         adapter type, or null if no such representation exists
     */
    public static Object getAdapter(Object sourceObject, Class adapterType) {
    	Assert.isNotNull(adapterType);
        if (sourceObject == null) {
            return null;
        }
        if (adapterType.isInstance(sourceObject)) {
            return sourceObject;
        }

        if (sourceObject instanceof IAdaptable) {
            IAdaptable adaptable = (IAdaptable) sourceObject;

            Object result = adaptable.getAdapter(adapterType);
            if (result != null) {
                // Sanity-check
                Assert.isTrue(adapterType.isInstance(result));
                return result;
            }
        } 
        
        if (!(sourceObject instanceof PlatformObject)) {
            Object result = Platform.getAdapterManager().getAdapter(sourceObject, adapterType);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    public static void assertInstance(Object object, Class c) {
        assertInstance(object, c, false);
    }

    public static void assertInstance(Object object, Class c, boolean allowNull) {
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
        return left == false ? (right == true ? -1 : 0) : 1;
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
			} else {
                for (int i = 0; i < l; i++) {
                    int compareTo = compare(left[i], right[i]);

                    if (compareTo != 0) {
						return compareTo;
					}
                }

                return 0;
            }
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
			} else {
                for (int i = 0; i < l; i++) {
                    int compareTo = compare((Comparable) left.get(i),
                            (Comparable) right.get(i));

                    if (compareTo != 0) {
						return compareTo;
					}
                }

                return 0;
            }
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
			return compare(System.identityHashCode(left), System
                    .identityHashCode(right));
		}
    }

    /**
     * An optimized comparison that uses identity hash codes to perform the
     * comparison between non- <code>null</code> objects.
     * 
     * @param left
     *            The left-hand side of the comparison; may be <code>null</code>.
     * @param right
     *            The right-hand side of the comparison; may be
     *            <code>null</code>.
     * @return <code>0</code> if they are the same, <code>-1</code> if left
     *         is <code>null</code>;<code>1</code> if right is
     *         <code>null</code>. Otherwise, the left identity hash code
     *         minus the right identity hash code.
     */
    public static final int compareIdentity(Object left, Object right) {
        if (left == null && right == null) {
			return 0;
		} else if (left == null) {
			return -1;
		} else if (right == null) {
			return 1;
		} else {
			return System.identityHashCode(left)
                    - System.identityHashCode(right);
		}
    }

    public static void diff(Map left, Map right, Set leftOnly, Set different,
            Set rightOnly) {
        if (left == null || right == null || leftOnly == null
                || different == null || rightOnly == null) {
			throw new NullPointerException();
		}

        Iterator iterator = left.keySet().iterator();

        while (iterator.hasNext()) {
            Object key = iterator.next();

            if (!right.containsKey(key)) {
				leftOnly.add(key);
			} else if (!Util.equals(left.get(key), right.get(key))) {
				different.add(key);
			}
        }

        iterator = right.keySet().iterator();

        while (iterator.hasNext()) {
            Object key = iterator.next();

            if (!left.containsKey(key)) {
				rightOnly.add(key);
			}
        }
    }

    public static void diff(Set left, Set right, Set leftOnly, Set rightOnly) {
        if (left == null || right == null || leftOnly == null
                || rightOnly == null) {
			throw new NullPointerException();
		}

        Iterator iterator = left.iterator();

        while (iterator.hasNext()) {
            Object object = iterator.next();

            if (!right.contains(object)) {
				leftOnly.add(object);
			}
        }

        iterator = right.iterator();

        while (iterator.hasNext()) {
            Object object = iterator.next();

            if (!left.contains(object)) {
				rightOnly.add(object);
			}
        }
    }

    public static boolean endsWith(List left, List right, boolean equals) {
        if (left == null || right == null) {
			return false;
		} else {
            int l = left.size();
            int r = right.size();

            if (r > l || !equals && r == l) {
				return false;
			}

            for (int i = 0; i < r; i++) {
				if (!equals(left.get(l - i - 1), right.get(r - i - 1))) {
					return false;
				}
			}

            return true;
        }
    }

    public static boolean endsWith(Object[] left, Object[] right, boolean equals) {
        if (left == null || right == null) {
			return false;
		} else {
            int l = left.length;
            int r = right.length;

            if (r > l || !equals && r == l) {
				return false;
			}

            for (int i = 0; i < r; i++) {
				if (!equals(left[l - i - 1], right[r - i - 1])) {
					return false;
				}
			}

            return true;
        }
    }

    public static boolean equals(boolean left, boolean right) {
        return left == right;
    }

    public static boolean equals(int left, int right) {
        return left == right;
    }

    public static boolean equals(Object left, Object right) {
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
		if (leftArray == rightArray) {
			return true;
		}

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

    public static int hashCode(boolean b) {
        return b ? Boolean.TRUE.hashCode() : Boolean.FALSE.hashCode();
    }

    public static int hashCode(int i) {
        return i;
    }

    public static int hashCode(Object object) {
        return object != null ? object.hashCode() : 0;
    }

    public static Collection safeCopy(Collection collection, Class c) {
        return safeCopy(collection, c, false);
    }

    public static Collection safeCopy(Collection collection, Class c,
            boolean allowNullElements) {
        if (collection == null || c == null) {
			throw new NullPointerException();
		}

        collection = Collections.unmodifiableCollection(new ArrayList(
                collection));
        Iterator iterator = collection.iterator();

        while (iterator.hasNext()) {
			assertInstance(iterator.next(), c, allowNullElements);
		}

        return collection;
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

    public static Map safeCopy(Map map, Class keyClass, Class valueClass) {
        return safeCopy(map, keyClass, valueClass, false, false);
    }

    public static Map safeCopy(Map map, Class keyClass, Class valueClass,
            boolean allowNullKeys, boolean allowNullValues) {
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

    public static SortedMap safeCopy(SortedMap sortedMap, Class keyClass,
            Class valueClass) {
        return safeCopy(sortedMap, keyClass, valueClass, false, false);
    }

    public static SortedMap safeCopy(SortedMap sortedMap, Class keyClass,
            Class valueClass, boolean allowNullKeys, boolean allowNullValues) {
        if (sortedMap == null || keyClass == null || valueClass == null) {
			throw new NullPointerException();
		}

        sortedMap = Collections.unmodifiableSortedMap(new TreeMap(sortedMap));
        Iterator iterator = sortedMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            assertInstance(entry.getKey(), keyClass, allowNullKeys);
            assertInstance(entry.getValue(), valueClass, allowNullValues);
        }

        return sortedMap;
    }

    public static SortedSet safeCopy(SortedSet sortedSet, Class c) {
        return safeCopy(sortedSet, c, false);
    }

    public static SortedSet safeCopy(SortedSet sortedSet, Class c,
            boolean allowNullElements) {
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
		} else {
            int l = left.size();
            int r = right.size();

            if (r > l || !equals && r == l) {
				return false;
			}

            for (int i = 0; i < r; i++) {
				if (!equals(left.get(i), right.get(i))) {
					return false;
				}
			}

            return true;
        }
    }

    public static boolean startsWith(Object[] left, Object[] right,
            boolean equals) {
        if (left == null || right == null) {
			return false;
		} else {
            int l = left.length;
            int r = right.length;

            if (r > l || !equals && r == l) {
				return false;
			}

            for (int i = 0; i < r; i++) {
				if (!equals(left[i], right[i])) {
					return false;
				}
			}

            return true;
        }
    }

    public static String translateString(ResourceBundle resourceBundle,
            String key) {
        return Util.translateString(resourceBundle, key, key, true, true);
    }

    public static String translateString(ResourceBundle resourceBundle,
            String key, String string, boolean signal, boolean trim) {
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
    
    public static void arrayCopyWithRemoval(Object [] src, Object [] dst, int idxToRemove) {
    	if (src == null || dst == null || src.length - 1 != dst.length || idxToRemove < 0 || idxToRemove >= src.length) {
			throw new IllegalArgumentException();
		}
    	
    	if (idxToRemove == 0) {
    		System.arraycopy(src, 1, dst, 0, src.length - 1);    		
    	}
    	else if (idxToRemove == src.length - 1) {
    		System.arraycopy(src, 0, dst, 0, src.length - 1);
    	}
    	else {
    		System.arraycopy(src, 0, dst, 0, idxToRemove);
    		System.arraycopy(src, idxToRemove + 1, dst, idxToRemove, src.length - idxToRemove - 1);
    	}    	
    }

    /**
     * Appends array2 to the end of array1 and returns the result
     * 
     * @param array1
     * @param array2
     * @return
     * @since 3.1
     */
    public static Object[] appendArray(Object[] array1, Object[] array2) {        
        Object[] result = new Object[array1.length + array2.length];
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;               
    }
    
    private Util() {
    }

	/**
	 * Returns an interned representation of the given string
	 * @param string The string to intern
	 * @return The interned string
	 */
	public static String intern(String string) {
		return string == null ? null : string.intern();
	}
	
	/**
	 * Returns the result of converting a list of comma-separated tokens into an array.
	 * Used as a replacement for <code>String.split(String)</code>, to allow compilation
	 * against JCL Foundation (bug 80053).
	 * 
	 * @param prop the initial comma-separated string
	 * @param separator the separator characters
	 * @return the array of string tokens
	 * @since 3.1
	 */
	public static String[] getArrayFromList(String prop, String separator) {
		if (prop == null || prop.trim().equals("")) { //$NON-NLS-1$
			return new String[0];
		}
		ArrayList list = new ArrayList();
		StringTokenizer tokens = new StringTokenizer(prop, separator); 
		while (tokens.hasMoreTokens()) {
			String token = tokens.nextToken().trim();
			if (!token.equals("")) { //$NON-NLS-1$
				list.add(token);
			}
		}
		return list.isEmpty() ? new String[0] : (String[]) list.toArray(new String[list.size()]);
	}

	/**
	 * Return the window for the given shell or the currently active window if
	 * one could not be determined.
	 * 
	 * @param shellToCheck
	 *            the shell to search on
	 * @return the window for the given shell or the currently active window if
	 *         one could not be determined
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
			workbenchWindow = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow();
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
		IWorkbenchWindow windowToParentOn = activeWindow == null ? (workbench
				.getWorkbenchWindowCount() > 0 ? workbench
				.getWorkbenchWindows()[0] : null) : activeWindow;
		return windowToParentOn == null ? null : activeWindow.getShell();
	}
	
	/**
	 * Splits a string at the first occurance of the delimiting char.
	 * If the source string is null then so is the result. If the source
	 * string is badly formatted then it is returned in the first array
	 * entry and the second entry is an empty string.
	 * 
	 * @param src The string to be split
	 * @param delim The character to split on
	 * @return A two entry string array containing the left/right pair.
	 */
	public static String[] split(String src, char delim) {
		if (src == null)
			return null;
		
		String[] splitStr = new String[2];
		int delimIndex = src.indexOf(delim);
		if (delimIndex == -1 || delimIndex == 0 || delimIndex == src.length()-1) {
			splitStr[0] = src;
			splitStr[1] = ZERO_LENGTH_STRING;
			return splitStr;
		}
		
		splitStr[0] = src.substring(0, delimIndex);
		splitStr[1] = src.substring(delimIndex+1);
		
		return splitStr;
	}
	
	/**
	 * Foundation replacement for String.replaceAll(*).
	 * 
	 * @param src the starting string.
	 * @param find the string to find.
	 * @param replacement the string to replace.
	 * @return The new string.
	 * @since 3.3
	 */
	public static String replaceAll(String src, String find, String replacement) {
		return org.eclipse.jface.util.Util.replaceAll(src, find, replacement);
	}
	
	/**
	 * Attempt to load the executable extension from the element/attName. If
	 * the load fails or the resulting object is not castable to the
	 * provided classSpec (if any) an error is logged and a null is returned.
	 * 
	 * @param element The {@link IConfigurationElement} containing the
	 * executable extension's specification 
	 * @param attName The attribute name of the executable extension
	 * @param classSpec An optional <code>Class</code> defining the type
	 * that the loaded Object must be castable to. This is optional to support
	 * code where the client has a choice of mutually non-castable types to
	 * choose from.
	 * 
	 * @return The loaded object which is guaranteed to be
	 * castable to the given classSpec or null if a failure occurred 
	 */
	public static Object safeLoadExecutableExtension(IConfigurationElement element, 
			String attName, Class classSpec) {
		Object loadedEE = null;
		
		// Load the handler.
		try {
			loadedEE = element.createExecutableExtension(attName);
		} catch (final CoreException e) {
			// TODO: give more info (eg plugin id)....
			// Gather formatting info
			final String classDef = element.getAttribute(attName);			

			final String message = "Class load Failure: '" + classDef + "'";  //$NON-NLS-1$//$NON-NLS-2$
			IStatus status = new Status(IStatus.ERROR,
					WorkbenchPlugin.PI_WORKBENCH, 0, message, e);
			WorkbenchPlugin.log(message, status);
		}
		
		// Check the loaded object's type
		if (classSpec != null && loadedEE != null && !classSpec.isInstance(loadedEE)) {
			// ooops, the loaded class is not castable to the given type
			final String message = "Loaded class is of incorrect type: expected(" + //$NON-NLS-1$
				classSpec.getName() + ") got (" + loadedEE.getClass().getName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$

			IllegalArgumentException e = new IllegalArgumentException(message);			
			final IStatus status = new Status(IStatus.ERROR,
					WorkbenchPlugin.PI_WORKBENCH, 0, message, e);
			WorkbenchPlugin.log(message, status);
			
			// This 'failed'
			loadedEE = null;
		}
		
		return loadedEE;
	}
	
}
