/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core.sourcelookup;

import java.util.Comparator;

/**
 * Comparator for source locator mementors. Ignores whitespace differences.
 * 
 * @since 3.0
 */
public class SourceLocatorMementoComparator implements Comparator {
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object o1, Object o2) {
		String m1 = (String)o1;
		String m2 = (String)o2;
		int i1 = 0, i2 = 0;
		while (i1 < m1.length()) {
			i1 = skipWhitespace(m1, i1);
			i2 = skipWhitespace(m2, i2);
			if (i1 < m1.length() && i2 < m2.length()) {
				if (m1.charAt(i1) != m2.charAt(i2)) {
					return -1;
				}
				i1++;
				i2++;
			} else {
				if (i2 < m2.length()) {
					return -1;
				} 
				return 0;
			}
		}
		return 0;
	}
	
	private int skipWhitespace(String string, int offset) {
		while (offset < string.length() && Character.isWhitespace(string.charAt(offset))) {
			offset++;
		}
		return offset;
	}
}
