/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help.internal.webapp.data;

import java.util.StringTokenizer;

import org.eclipse.core.runtime.Platform;

/**
 * Utility class for parsing the CSS preferences
 */

public class CssUtil {
	
	private static String replaceParameters(String input) {
		final String OS = "${os}"; //$NON-NLS-1$
		int index = input.indexOf(OS);
		if (index < 0) {
			return input;
		}
		String result = input.substring(0, index) + Platform.getOS() + input.substring(index + OS.length());
		return replaceParameters(result); 
	}
	
	/**
	 * @param filenames 
	 * @return
	 */
	public static String[] getCssFilenames(String filenames ) {
		if (filenames  == null) {
			return new String[0];
		}
		StringTokenizer tok = new StringTokenizer(filenames , ","); //$NON-NLS-1$
		String[] result = new String[tok.countTokens()];
		for (int i = 0; tok.hasMoreTokens(); i++) {
			result[i] = replaceParameters(tok.nextToken().trim());
		}
		return result;
	}

}
