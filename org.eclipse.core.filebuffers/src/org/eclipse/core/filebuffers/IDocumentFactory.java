/**********************************************************************
Copyright (c) 2000, 2004 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.core.filebuffers;

import org.eclipse.jface.text.IDocument;

/**
 * Factory for text file buffer documents. Used by the text file buffer manager
 * to create the document for a new text file buffer.
 * <p>
 * The expected interface of extensions provided for the
 * <code>"org.eclipse.core.filebuffers.documentCreation"</code> extension
 * point.
 * 
 * @since 3.0
 */
public interface IDocumentFactory {
	
	/**
	 * Creates and returns a new, empty document.
	 * 
	 * @return a new, empty document
	 */
	IDocument createDocument();
}
