package org.eclipse.jface.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


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
 	 * Finds and selects a string starting at the given offset using the specified search
 	 * directives.
 	 *
 	 * @param offset the offset at which searching starts
 	 * @param findString the string which should be found
 	 * @param searchForward <code>true</code> searches forward, <code>false</code> backwards
  	 * @param caseSensitive <code>true</code> performes a case sensitve search, <code>false</code> an insensitive search
  	 * @param wholeWord if <code>true</code> only occurences are reported in which the findString stands as a word by itself 
  	 * @return the position of the specified string, or -1 if the string has not been found
	 */
	int findAndSelect(int offset, String findString, boolean searchForward, boolean caseSensitive, boolean wholeWord);
	
	/**
	 * Returns the currently selected range of characters as a offset and length.
	 *
	 * @return the currently selected character range
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
