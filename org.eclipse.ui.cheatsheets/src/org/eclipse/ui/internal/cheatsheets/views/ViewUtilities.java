/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.cheatsheets.views;

/**
 * Contains static functions used in cheat sheet display
 */
public class ViewUtilities {

	/*
	* Ampersands need to be escaped before being passed to a Label
	*/
	public static String escapeForLabel(String message) {
		// Make the most common case - i.e. no ampersand the
		// most efficient
		if (message.indexOf('&') < 0) {
			return message;
		}
		
		int next = 0;
		StringBuffer result = new StringBuffer();
		int index = message.indexOf('&');
		while (index >= 0) {
			result.append(message.substring(next, index + 1));
			result.append('&');
			next = index + 1;
			index = message.indexOf('&', next);
		}
		result.append(message.substring(next));
		return result.toString();
	}
    
}
