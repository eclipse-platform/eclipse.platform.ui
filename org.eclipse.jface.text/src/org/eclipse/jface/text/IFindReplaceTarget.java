/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;



import org.eclipse.swt.graphics.Point;


/**
 * Defines the interface for finding and replacing strings.
 */
public interface IFindReplaceTarget {
	
	/**
	 * Returns whether a find operation can be performed.
	 *
	 * @return whether a find operation can be performed
	 */
	boolean canPerformFind();
	
 	/**
 	 * Searches for a string starting at the given widget offset and using the specified search
 	 * directives. If a string has been found it is selected and its start offset is 
 	 * returned.
 	 *
 	 * @param widgetOffset the widget offset at which searching starts
 	 * @param findString the string which should be found
 	 * @param searchForward <code>true</code> searches forward, <code>false</code> backwards
  	 * @param caseSensitive <code>true</code> performs a case sensitive search, <code>false</code> an insensitive search
  	 * @param wholeWord if <code>true</code> only occurrences are reported in which the findString stands as a word by itself 
  	 * @return the position of the specified string, or -1 if the string has not been found
	 */
	int findAndSelect(int widgetOffset, String findString, boolean searchForward, boolean caseSensitive, boolean wholeWord);
	
	/**
	 * Returns the currently selected range of characters as a offset and length in widget coordinates.
	 *
	 * @return the currently selected character range in widget coordinates
	 */
	Point getSelection();
	
	/**
	 * Returns the currently selected characters as a string.
	 *
	 * @return the currently selected characters
	 */
	String getSelectionText();
	
	/**
	 * Returns whether this target can be modified.
	 *
	 * @return <code>true</code> if target can be modified
	 */
	boolean isEditable();
	
	/**
	 * Replaces the currently selected range of characters with the given text.
	 * This target must be editable. Otherwise nothing happens.
	 *
	 * @param text the substitution text
	 */
	void replaceSelection(String text);
}
