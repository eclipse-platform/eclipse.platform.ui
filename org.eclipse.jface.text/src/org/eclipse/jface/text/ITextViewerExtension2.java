/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.jface.text;


/**
 * Extension interface for <code>ITextViewer</code>. Extends <code>ITextViewer</code> with
 * <ul>
 * <li> a replacement of the invalidateTextPresentation method
 * </ul>
 * 
 * @since 2.1
 */
public interface ITextViewerExtension2 {
	 
	/**
	 * Invalidates the viewer's text presentation for the given range.
	 * 	 * @param offset the offset of the first character to be redrawn	 * @param length the length of the range to be redrawn	 */
	void invalidateTextPresentation(int offset, int length);
}
