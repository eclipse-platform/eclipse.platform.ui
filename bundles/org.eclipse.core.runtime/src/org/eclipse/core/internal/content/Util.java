/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.content;

import java.util.*;

public class Util {
	public static String[] parseItems(String string) {
		return parseItems(string, ","); //$NON-NLS-1$
	}

	public static String[] parseItems(String string, String separator) {
		if (string == null)
			return new String[0];
		StringTokenizer tokenizer = new StringTokenizer(string, separator); //$NON-NLS-1$
		if (!tokenizer.hasMoreTokens())
			return new String[0];
		String first = tokenizer.nextToken().trim();
		if (!tokenizer.hasMoreTokens())
			return new String[] {first};
		ArrayList items = new ArrayList();
		items.add(first);
		do {
			items.add(tokenizer.nextToken().trim());
		} while (tokenizer.hasMoreTokens());
		return (String[]) items.toArray(new String[items.size()]);
	}

	public static String toListString(List list) {
		return toListString(list, ","); //$NON-NLS-1$
	}

	public static String toListString(List list, String separator) {
		if (list.isEmpty())
			return ""; //$NON-NLS-1$
		StringBuffer result = new StringBuffer();
		for (Iterator i = list.iterator(); i.hasNext();) {
			result.append(i.next());
			result.append(separator);
		}
		// ignore last comma
		return result.substring(0, result.length() - 1);
	}

	public static String toListString(Object[] list) {
		return toListString(list, ","); //$NON-NLS-1$
	}

	public static String toListString(Object[] list, String separator) {
		if (list.length == 0)
			return ""; //$NON-NLS-1$
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < list.length; i++) {
			result.append(list[i]);
			result.append(separator);
		}
		// ignore last comma
		return result.substring(0, result.length() - 1);
	}

}
