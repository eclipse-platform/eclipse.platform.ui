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
package org.eclipse.ui.externaltools.internal.launchConfigurations;

import java.util.Comparator;

public class IgnoreWhiteSpaceComparator implements Comparator {

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object o1, Object o2) {
		String one= (String)o1;
		String two= (String)o2;
		int i1 = 0;
		int i2 = 0;
		int l1 = one.length();
		int l2 = two.length();
		char ch1 = ' ';
		char ch2 = ' ';
		while (i1 < l1 && i2 < l2) {
			while (i1 < l1 && Character.isWhitespace(ch1 = one.charAt(i1))) {
				i1++;
			}
			while (i2 < l2 && Character.isWhitespace(ch2 = two.charAt(i2))) {
				i2++;
			}
			if (i1 == l1 && i2 == l2) {
				return 0;
			}
			if (ch1 != ch2) {
				return -1;
			}			
			i1++;
			i2++;
		}
		return 0;
	}
}
