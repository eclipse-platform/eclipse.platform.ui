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
 * A hyperlink controller displays the given hyperlinks on
 * the installed text viewer and allows to select one
 * on of the hyperlinks.
 * <p>
 * NOTE: This API is work in progress and may change before the final API freeze.
 * </p>
 * 
 * @since 3.1
 */
public interface IHyperlinkController {

	/**
	 * Tells whether this controller is able to handle
	 * more than one hyperlink.
	 * 
	 * @return <code>true</code> if this controller can handle more than one hyperlink
	 */
	boolean canShowMultipleHyperlinks();
	
	/**
	 * Activates the this hyperlink controller on the installed
	 * text viewer for the given hyperlinks.
	 * 
	 * @param hyperlinks the hyperlinks to show
	 * @throws IllegalArgumentException if
	 * 			<ul>
	 * 				<li><code>hyperlinks</code> is empty</li>
	 * 				<li>{@link #canShowMultipleHyperlinks()} returns <code>false</code> and <code>hyperlinks</code> contains more than one element</li>
	 * 			</ul>  
	 */
	void activate(IHyperlink[] hyperlinks) throws IllegalArgumentException;

	/**
	 * Deactivates this hyperlink controller.
	 */
	void deactivate();

	/**
	 * Installs this hyperlink controller on the given text viewer.
	 * 
	 * @param textViewer the text viewer
	 */
	void install(ITextViewer textViewer);

	/**
	 * Uninstalls this hyperlink controller.
	 */
	void uninstall();
}