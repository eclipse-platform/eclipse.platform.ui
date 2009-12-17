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
package org.eclipse.ui.actions;

/**
 * Implements an algorithm for very simple pattern matching in a string.
 * There is only one feature: "*" may be used at the start or the end of the
 * pattern to represent "one or more characters".
 */
public final class SimpleWildcardTester {
    /**
     * Returns whether a string matches a particular pattern.
     *
     * @param pattern the input pattern
     * @param str the string to test
     * @return <code>true</code> if a match occurs; <code>false</code>otherwise.
     */
    public static boolean testWildcard(String pattern, String str) {
        if (pattern.equals("*")) {//$NON-NLS-1$
            return true;
        } else if (pattern.startsWith("*")) {//$NON-NLS-1$
            if (pattern.endsWith("*")) {//$NON-NLS-1$
                if (pattern.length() == 2) {
					return true;
				}
                return str.indexOf(pattern.substring(1, pattern.length() - 1)) >= 0;
            }
            return str.endsWith(pattern.substring(1));
        } else if (pattern.endsWith("*")) {//$NON-NLS-1$
            return str.startsWith(pattern.substring(0, pattern.length() - 1));
        } else {
            return str.equals(pattern);
        }
    }

    /**
     * Returns whether a string matches a particular pattern.  Both string and
     * pattern are converted to lower case before pattern matching occurs.
     *
     * @param pattern the input pattern
     * @param str the string to test
     * @return <code>true</code> if a match occurs; <code>false</code>otherwise.
     */
    public static boolean testWildcardIgnoreCase(String pattern, String str) {

        //If str is null there was no extension to test
        if (str == null) {
			return false;
		}
        pattern = pattern.toLowerCase();
        str = str.toLowerCase();
        return testWildcard(pattern, str);
    }
}
