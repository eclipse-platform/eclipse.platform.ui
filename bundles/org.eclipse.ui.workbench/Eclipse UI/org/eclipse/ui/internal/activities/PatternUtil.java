/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Bredex GmbH - Creator of this class.
 ******************************************************************************/

package org.eclipse.ui.internal.activities;

/**
 * Utility helper class for regular expression string patterns. 
 * 
 * @since 3.4
 * @author Jan Diederich
 */
public class PatternUtil {
	
	/**
	 * Quotes a string pattern as non-regular expression string.
	 * That means: no regular expresion instructions in the given string 
	 * won't be taken into account.<br/>
	 * Example:
	 * <code>String searchString = "xy[^a]";<br/>
	 * Pattern.compile(quotePattern(searchString)).matcher(searchString)
	 * 		.matches();
	 * <br/> </code> will return true.
	 * @param pattern pattern to quote
	 * @return the quoted pattern
	 */
	public static String quotePattern(String pattern) {
		final String START = "\\Q";  //$NON-NLS-1$
		final String STOP = "\\E"; //$NON-NLS-1$
		final int STOP_LENGTH = 2;  // STOP.length()
		
		int stopIndex = pattern.indexOf(STOP);
		if (stopIndex < 0) {
			return START + pattern + STOP;
		}
		
		String result = START;
		for (int position=0; ;) {
			stopIndex = pattern.indexOf(STOP, position);
			if (stopIndex >= 0) {
				result += pattern.substring(position, stopIndex + 2)
						+ "\\" + STOP + START; //$NON-NLS-1$
				position = stopIndex + STOP_LENGTH;
			} else {
				result += pattern.substring(position) + STOP;  
				break;
			}
		}
		
		return result;
	}	
}
