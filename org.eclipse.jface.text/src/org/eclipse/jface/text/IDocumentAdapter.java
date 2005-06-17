/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;


import org.eclipse.swt.custom.StyledTextContent;


/**
 * Adapts an {@link org.eclipse.jface.text.IDocument}to the
 * {@link org.eclipse.swt.custom.StyledTextContent} interface. The document
 * adapter is used by {@link org.eclipse.jface.text.TextViewer} to translate
 * document changes into styled text content changes and vice versa.
 * <p>
 * Clients may implement this interface and override
 * <code>TextViewer.createDocumentAdapter</code> if they want to intercept the
 * communication between the viewer's text widget and the viewer's document.
 * <p>
 * In order to provide backward compatibility for clients of
 * <code>IDocumentAdapter</code>, extension interfaces are used as a means of
 * evolution. The following extension interfaces exist:
 * <ul>
 * <li>{@link org.eclipse.jface.text.IDocumentAdapterExtension} since version
 *     2.0 introducing a way of batching a sequence of document changes into a
 *     single styled text content notification</li>
 * </ul>
 *
 * @see org.eclipse.jface.text.IDocumentAdapterExtension
 * @see org.eclipse.jface.text.IDocument
 */
public interface IDocumentAdapter extends StyledTextContent {

	/**
	 * Sets the adapters document.
	 *
	 * @param document the document to be adapted
	 */
	void setDocument(IDocument document);
}
