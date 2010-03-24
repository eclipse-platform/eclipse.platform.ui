/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.util;


/**
 * Utility class for string-related functions.
 * 
 * @since 3.0
 */
public final class Strings {

	public static String removeNewLine(String message) {
		final StringBuffer result= new StringBuffer();
		int current= 0;
		int index= message.indexOf('\n', 0);
		while (index != -1) {
			result.append(message.substring(current, index));
			if (current < index && index != 0)
				result.append(' ');
			current= index + 1;
			index= message.indexOf('\n', current);
		}
		result.append(message.substring(current));
		return result.toString();
	}

	private Strings() {
		// Not for instantiation
	}
}