/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;


/**
 * Defines the target for finding and replacing strings.
 * <p>
 * Clients can implements that interface or use the provided default
 * implementation {@link TextViewerDeleteLineTarget}.
 * </p>
 *
 * @since 3.4
 */
interface IDeleteLineTarget {

	/**
	 * Deletes the specified fraction of the line of the given offset.
	 *
	 * @param document the document
	 * @param offset the offset
	 * @param length the length
	 * @param type the line deletion type, must be one of
	 * 	<code>WHOLE_LINE</code>, <code>TO_BEGINNING</code> or <code>TO_END</code>
	 * @param copyToClipboard <code>true</code> if the deleted line should be copied to the clipboard
	 * @throws BadLocationException if position is not valid in the given document
	 */
	public abstract void deleteLine(IDocument document, int offset, int length, int type, boolean copyToClipboard) throws BadLocationException;

}