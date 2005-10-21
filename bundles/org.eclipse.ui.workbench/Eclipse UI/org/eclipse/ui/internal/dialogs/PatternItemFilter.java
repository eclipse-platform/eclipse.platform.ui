/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.text.BreakIterator;

/**
 * Abstract class that handles filtering items based on a supplied
 * matching string.
 * 
 * @since 3.1
 * 
 */
public abstract class PatternItemFilter extends PatternFilter {
	protected boolean matchItem;

	/**
	 * Create a new instance of a PatternItemFilter
	 * 
	 * @param isMatchItem
	 */
	public PatternItemFilter(boolean isMatchItem) {
		super();
		matchItem = isMatchItem;
	}
	
	/**
	 * Return whether or not if any of the words in text satisfy the
	 * match critera.
	 * @param text
	 * @return boolean <code>true</code> if one of the words in text 
	 * satisifes the match criteria.
	 */
	protected boolean wordMatches(String text) {
		if (text == null)
			return false;
		
		//If the whole text matches we are all set
		if(match(text))
			return true;
		
		// Break the text up into words, separating based on whitespace and
		// common punctuation.
		// Previously used String.split(..., "\\W"), where "\W" is a regular
		// expression (see the Javadoc for class Pattern).
		// Need to avoid both String.split and regular expressions, in order to
		// compile against JCL Foundation (bug 80053).
		// Also need to do this in an NL-sensitive way. The use of BreakIterator
		// was suggested in bug 90579.
		BreakIterator iter = BreakIterator.getWordInstance();
		iter.setText(text);
		int i = iter.first();
		while (i != java.text.BreakIterator.DONE && i < text.length()) {
			int j = iter.following(i);
			if (j == java.text.BreakIterator.DONE)
				j = text.length();
			if (Character.isLetterOrDigit(text.charAt(i))) {
				String word = text.substring(i, j);
				if (match(word))
					return true;
			}
			i = j;
		}
		return false;
	}

}
