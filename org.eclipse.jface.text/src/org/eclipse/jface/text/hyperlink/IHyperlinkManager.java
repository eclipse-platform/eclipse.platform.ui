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

package org.eclipse.jface.text.hyperlink;

import org.eclipse.jface.text.ITextViewer;

/**
 * A hyperlink manager for a text viewer detects
 * {@link org.eclipse.jface.text.hyperlink.IHyperlink hyperlinks} with the given
 * {@link org.eclipse.jface.text.hyperlink.IHyperlinkDetector hyperlink detectors}
 * and displays the links using a {@link org.eclipse.jface.text.hyperlink.IHyperlinkController hyperlink controller}.
 * <p>
 * NOTE: This API is work in progress and may change before the final API freeze.
 * </p>
 * 
 * @since 3.1
 */
public interface IHyperlinkManager {

	/**
	 * Installs this hyperlink manager on the given text viewer.
	 * 
	 * @param textViewer the text viewer
	 * @param hyperlinkController the hyperlink controller
	 * @param hyperlinkDetectors the hyperlink detectors
	 * @param keyModifierMask the modifier mask which in combination with the left mouse button triggers the hyperlink mode
	 */
	void install(ITextViewer textViewer, IHyperlinkController hyperlinkController, IHyperlinkDetector[] hyperlinkDetectors, int keyModifierMask);

	/**
	 * Uninstalls this hyperlink manager from the text viewer. 
	 */
	void uninstall();
	
	/**
	 * Sets the hyperlink detectors for this hyperlink manager.
	 * <p>
	 * It is allowed to called this method after this
	 * hyperlink manger has been installed.
	 * </p>
	 * 
	 * @param hyperlinkDetectors
	 */
	void setHyperlinkDetectors(IHyperlinkDetector[] hyperlinkDetectors);

	/**
	 * Sets the SWT key modifier mask which in combination
	 * with the left mouse button triggers the hyperlink mode.
	 * <p>
	 * It is allowed to call this method after this
	 * hyperlink manger has been installed.
	 * </p>
	 *  
	 * @param modifierMask the modifier mask
	 */
	void setKeyModifierMask(int modifierMask);
}