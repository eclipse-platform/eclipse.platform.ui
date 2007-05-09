/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;

import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;

/**
 * Extension interface for {@link org.eclipse.jface.text.ITextViewer}.
 * Introduces the concept of text hyperlinks and adds access to the undo manager.
 *
 * @see org.eclipse.jface.text.hyperlink.IHyperlink
 * @see org.eclipse.jface.text.hyperlink.IHyperlinkDetector
 * @since 3.1
 */
public interface ITextViewerExtension6 {

	/**
	 * Sets this viewer's hyperlink detectors for the given state mask.
	 *
	 * @param hyperlinkDetectors	the new array of hyperlink detectors, <code>null</code>
	 * 									or an empty array to disable hyperlinking
	 * @param eventStateMask		the SWT event state mask to activate hyperlink mode
	 */
	void setHyperlinkDetectors(IHyperlinkDetector[] hyperlinkDetectors, int eventStateMask);

	/**
	 * Returns this viewer's undo manager.
	 *
	 * @return the undo manager or <code>null</code> if it has not been plugged-in
	 * @since 3.1
	 */
	IUndoManager getUndoManager();

}
