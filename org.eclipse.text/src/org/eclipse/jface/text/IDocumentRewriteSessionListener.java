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


/**
 * Interface for objects which are interested in getting informed about document
 * rewrite sessions.
 * <p>
 * Clients may implement this interface.
 * </p>
 *
 * @see org.eclipse.jface.text.IDocument
 * @see org.eclipse.jface.text.IDocumentExtension4
 * @since 3.1
 */
public interface IDocumentRewriteSessionListener {

	/**
	 * Signals a change in a document's rewrite session state.
	 *
	 * @param event the event describing the document rewrite session state change
	 */
	void documentRewriteSessionChanged(DocumentRewriteSessionEvent event);
}
