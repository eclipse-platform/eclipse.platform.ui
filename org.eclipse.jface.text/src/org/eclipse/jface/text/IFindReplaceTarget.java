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
package org.eclipse.jface.text;


import org.eclipse.swt.graphics.Point;


/**
 * Defines the target for finding and replacing strings.
 * <p>
 * The two main methods are <code>findAndSelect</code> and
 * <code>replaceSelection</code>. The target does not provide any way to
 * modify the content other than replacing the selection.
 * <p>
 *
 * In order to provide backward compatibility for clients of
 * <code>IFindReplaceTarget</code>, extension interfaces are used as a means
 * of evolution. The following extension interfaces exist:
 * <ul>
 * <li>{@link org.eclipse.jface.text.IFindReplaceTargetExtension} since version
 *     2.0 introducing the notion of find/replace session and of a find/replace
 *     scope. In additions, in allows clients to replace all occurrences of a given
 *     find query.</li>
 * <li>{@link org.eclipse.jface.text.IFindReplaceTargetExtension3} since
 *     version 3.0 allowing clients to specify search queries as regular
 *     expressions.</li>
 * </ul>
 * <p>
 * Clients of a <code>IFindReplaceTarget</code> that also implements the
 * <code>IFindReplaceTargetExtension</code> have to indicate the start of a find/replace
 * session before using the target and to indicate the end of the session when the
 * target is no longer used.
 *
 * @see org.eclipse.jface.text.IFindReplaceTargetExtension
 * @see org.eclipse.jface.text.IFindReplaceTargetExtension3
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
 	 * <p>
 	 * Replaced by {@link IFindReplaceTargetExtension3#findAndSelect(int, String, boolean, boolean, boolean, boolean)}.
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
	 * <p>
	 * Replaced by {@link IFindReplaceTargetExtension3#replaceSelection(String, boolean)}.
	 *
	 * @param text the substitution text
	 */
	void replaceSelection(String text);
}
