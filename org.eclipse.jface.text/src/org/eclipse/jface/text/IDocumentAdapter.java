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



import org.eclipse.swt.custom.StyledTextContent;


/**
 * Adapts an <code>IDocument</code> to the <code>StyledTextContent</code> interface.
 * The document adapter is used by <code>TextViewer</code> to translate document changes
 * into styled text content changes and vice versa.
 * Clients may implement this interface and override <code>TextViewer.createDocumentAdapter</code>
 * if they want to intercept the communication between the viewer's text widget and
 * the viewer's document.
 * 
 * @see IDocument
 * @see StyledTextContent
 */
public interface IDocumentAdapter extends StyledTextContent {
	
	/**
	 * Sets the adapters document.
	 * 
	 * @param document the document to be adapted
	 */
	void setDocument(IDocument document);
}
