/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.source;

import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextSelection;

/**
 * Extension to <code>IAnnotationHover</code> for
 * <ul>
 * <li>providing its own information control creator</li>
 * <li>providing the range of lines for which the hover for a given line is valid
 * </ul>
 * 
 * @see org.eclipse.jface.text.IInformationControlCreator
 * @see org.eclipse.jface.text.source.IAnnotationHover
 * @since 3.0
 */
public interface IAnnotationHoverExtension {

	/**
	 * Returns the information control creator of this annotation hover.
	 * 
	 * @return the information control creator
	 */
	IInformationControlCreator getInformationControlCreator();
	
	/**
	 * Returns the text which should be presented in the a
	 * hover popup window. This information is requested based on
	 * the specified line number.
	 *
	 * @param sourceViewer the source viewer this hover is registered with
	 * @param lineNumber the line number for which information is requested
	 * @param first the first line in <code>viewer</code>'s document to consider
	 * @param number the number of lines in <code>viewer</code>'s document to consider
	 * @return the requested information or <code>null</code> if no such information exists
	 */
	String getHoverInfo(ISourceViewer sourceViewer, int lineNumber, int first, int number);

	/**
	 * Returns the range of lines that are covered by this hover for the given
	 * <code>ISourceViewer</code> at model line <code>line</code>.
	 * 
	 * @param viewer the viewer which the hover is queried for
	 * @param line the line which a hover is displayed for
	 * @param first the first line in <code>viewer</code>'s document to consider
	 * @param number the number of lines in <code>viewer</code>'s document to consider
	 * @return the selection in the document displayed in <code>viewer</code> containing <code>line</code> 
	 * that is covered by the hover information returned by the receiver.
	 */
	ITextSelection getLineRange(ISourceViewer viewer, int line, int first, int number);
}