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

/**
 * Extension interface for {@link org.eclipse.jface.text.IDocument}. Adds the
 * concept of rewrite sessions. A rewrite session is a sequence of replace
 * operations that form a semantic unit.
 * <p>
 * This class is not yet for public use. API under construction.
 * 
 * @since 3.1
 */
public interface IDocumentExtension4 {
	
	/**
	 * Tells the document that it is about to be rewritten. That is a sequence
	 * of replace operations that form a semantic unit will be performed on this
	 * document.
	 * <p>
	 * The document is considered being in rewrite mode as long as
	 * <code>stopRewriteSession</code> has not been called.
	 * 
	 * @param sessionId the session id
	 */
	void startRewriteSession(Object sessionId);

	/**
	 * Tells the document that the rewrite mode has been finished. This method
	 * has only any effect if <code>startRewriteSession</code> has been called
	 * before.
	 * 
	 * @param sessionId the session id
	 */
	void stopRewriteSession(Object sessionId);
	
	/**
	 * Registers the document rewrite session listener with the document. After
	 * registration the <code>IDocumentRewriteSessionListener</code> is
	 * informed about each state change of rewrite sessions performed on this
	 * document.
	 * <p>
	 * If the listener is already registered nothing happens.
	 * <p>
	 * An <code>IRewriteSessionDocumentListener</code> may call back to this
	 * document when being inside a document notification.
	 * 
	 * @param listener the listener to be registered
	 */
	void addDocumentRewriteSessionListener(IDocumentRewriteSessionListener listener);
	
	/**
	 * Removes the listener from the document's list of document rewrite session
	 * listeners. If the listener is not registered with the document nothing
	 * happens.
	 * <p>
	 * An <code>IDocumentRewriteSessionListener</code> may call back to this
	 * document when being inside a document notification.
	 * 
	 * @param listener the listener to be removed
	 */
	void removeDocumentRewriteSessionListener(IDocumentRewriteSessionListener listener);
}
