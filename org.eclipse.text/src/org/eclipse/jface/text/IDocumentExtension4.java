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
	 * Tells the document that it is about to be rewritten. That is, a sequence
	 * of replace operations that form a semantic unit will be performed on this
	 * document. A specification of the nature of the operation sequence is
	 * given in form of the session type.
	 * <p>
	 * The document is considered being in rewrite mode as long as
	 * <code>stopRewriteSession</code> has not been called.
	 * 
	 * @param sessionType the session type
	 * @return the started rewrite session
	 * @throws IllegalStateException in case there is already an active rewrite session
	 */
	DocumentRewriteSession startRewriteSession(DocumentRewriteSessionType sessionType) throws IllegalStateException;

	/**
	 * Tells the document to stop the rewrite session. This method has only any
	 * effect if <code>startRewriteSession</code> has been called before.
	 * <p>
	 * This method does not have any effect if the given session is not the
	 * active rewrite session.
	 * 
	 * @param session the session to stop
	 */
	void stopRewriteSession(DocumentRewriteSession session);
	
	/**
	 * Returns the active rewrite session of this document or <code>null</code>.
	 * 
	 * @return the active rewrite session or <code>null</code>
	 */
	DocumentRewriteSession getActiveRewriteSession();
	
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
