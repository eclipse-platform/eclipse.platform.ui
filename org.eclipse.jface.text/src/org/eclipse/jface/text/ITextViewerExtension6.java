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

import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;

/**
 * Extension interface for {@link org.eclipse.jface.text.ITextViewer}. 
 * Introduces the concept of text hyperlinks.
 * <p>
 * NOTE: This API is work in progress and may change before the final API freeze. (FIXME)
 * </p>
 *
 * @see org.eclipse.jface.text.hyperlink.IHyperlink
 * @see org.eclipse.jface.text.hyperlink.IHyperlinkDetector
 * @since 3.1
 */
public interface ITextViewerExtension6 {
	
	/**
	 * Sets this viewer's hyperlinkDetectors for the given content type.
	 *
	 * @param hyperlinkDetectors the new list of hyperlink detectors, must not be empty
	 * @throws IllegalArgumentException if hyperlinkDetectors is <code>null</code> or empty 
	 */
	void setHyperlinkDetectors(IHyperlinkDetector[] hyperlinkDetectors) throws IllegalArgumentException;

	/**
	 * Sets whether hyperlinking is enabled or not.
	 * 
	 * @param state <code>true</code> if enabled 
	 */
	void setHyperlinksEnabled(boolean state);
	
	/**
	 * Sets the hyperlink state mask.
	 * 
	 * @param hyperlinkStateMask the hyperlink state mask
	 */
	public void setHyperlinkStateMask(int hyperlinkStateMask);
}
