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

import org.eclipse.swt.graphics.Point;

/**
 * Extension interface for <code>ITextViewer</code>. Extends <code>ITextViewer</code> with
 * <ul>
 * <li> a replacement of the invalidateTextPresentation method
 * <li> a replacement of the setTextHover method now accepting state masks
 * </ul>
 * 
 * @since 2.1
 */
public interface ITextViewerExtension2 {
	 
	 /**
	  * The state mask of the default hover (value <code>0xff</code>).
	  * @since 2.1
	  */
	 final int DEFAULT_HOVER_STATE_MASK= 0xff;
	 
	/**
	 * Invalidates the viewer's text presentation for the given range.
	 * 
	 * @param offset the offset of the first character to be redrawn
	 * @param length the length of the range to be redrawn
	 */
	void invalidateTextPresentation(int offset, int length);

	/**
	 * Sets this viewer's text hover for the given content type. 
	 *
	 * @param textViewerHover the new hover. <code>null</code> uninstalls the hover for the
	 * 			given content type and state mask.
	 * @param contentType the type for which the hover is registered
	 * @param stateMask the SWT event state mask; <code>DEFAULT_HOVER_STATE_MASK</code> indicates that
	 * 			the hover is installed as the default hover.
	 * @since 2.1
	 */
	void setTextHover(ITextHover textViewerHover, String contentType, int stateMask);
	
	/**
	 * Returns the currently displayed text hover if any, <code>null</code>
	 * otherwise.	 */
	ITextHover getCurrentTextHover();
	
	/**
	 * Returns the location at which the most recent mouse hover event
	 * has been issued.
	 * 
	 * @return the location of the most recent mouse hover event
	 */
	Point getHoverEventLocation();
	
}
