/*
 * Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 */
package org.eclipse.compare.contentmergeviewer;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.IDocument;


/**
 * Defines a subrange in a document.
 * <p>
 * It is used by text viewers that can work on a subrange of a document. For example,
 * a text viewer for Java compilation units might use this to restrict the view
 * to a single method.
 * </p>
 * <p>
 * Clients may implement this interface.
 * </p>
 *
 * @see TextMergeViewer
 * @see org.eclipse.compare.structuremergeviewer.DocumentRangeNode
 */
public interface IDocumentRange {
	
	/**
	 * The position category typically used for an <code>IDocumentRange</code> position
	 * (value <code>"DocumentRangeCategory"</code>).
	 * @since 2.0
	 */
	public static final String RANGE_CATEGORY= "DocumentRangeCategory";	//$NON-NLS-1$

	/**
	 * Returns the underlying document.
	 * 
	 * @return the underlying document
	 */
	IDocument getDocument();
	
	/**
	 * Returns a position that specifies a subrange in the underlying document,
	 * or <code>null</code> if this document range spans the whole underlying document.
	 * 
	 * @return a position that specifies a subrange in the underlying document, or <code>null</code>
	 */
	Position getRange();
}
