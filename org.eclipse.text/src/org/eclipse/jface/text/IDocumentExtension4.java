/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
 * Extension interface for {@link org.eclipse.jface.text.IDocument}. It adds the
 * following concepts:
 * <ul>
 *   <li>Rewrite sessions. A rewrite session is a sequence of replace operations
 *       that form a semantic unit.</li>
 *   <li>A modification stamp on the document</li>
 *   <li>The ability to set the initial line delimiter and to query the default
 *       line delimiter</li>
 * </ul>
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

	/**
	 * Substitutes the given text for the specified document range.
	 * Sends a <code>DocumentEvent</code> to all registered <code>IDocumentListener</code>.
	 *
	 * @param offset the document offset
	 * @param length the length of the specified range
	 * @param text the substitution text
	 * @param modificationStamp of the document after replacing
	 * @exception BadLocationException if the offset is invalid in this document
	 *
	 * @see DocumentEvent
	 * @see IDocumentListener
	 */
	void replace(int offset, int length, String text, long modificationStamp) throws BadLocationException;

	/**
	 * Replaces the content of the document with the given text.
	 * Sends a <code>DocumentEvent</code> to all registered <code>IDocumentListener</code>.
	 * This method is a convenience method for <code>replace(0, getLength(), text)</code>.
	 *
	 * @param text the new content of the document
	 * @param modificationStamp of the document after setting the content
	 *
	 * @see DocumentEvent
	 * @see IDocumentListener
	 */
	void set(String text, long modificationStamp);

	/**
	 * The unknown modification stamp.
	 */
	long UNKNOWN_MODIFICATION_STAMP= -1;

	/**
	 * Returns the modification stamp of this document. The modification stamp
	 * is updated each time a modifying operation is called on this document. If
	 * two modification stamps of the same document are identical then the document
	 * content is too, however, same content does not imply same modification stamp.
	 * <p>
	 * The magnitude or sign of the numerical difference between two modification stamps
	 * is not significant.
	 * </p>
	 *
	 * @return the modification stamp of this document or <code>UNKNOWN_MODIFICATION_STAMP</code>
	 */
	long getModificationStamp();

	/**
	 * Returns this document's default line delimiter.
	 * <p>
	 * This default line delimiter should be used by clients who
	 * want unique delimiters (e.g. 'CR's) in the document.</p>
	 *
	 * @return the default line delimiter or <code>null</code> if none
	 */
	String getDefaultLineDelimiter();

	/**
	 * Sets this document's initial line delimiter i.e. the one
	 * which is returned by <code>getDefaultLineDelimiter</code>
	 * if the document does not yet contain any line delimiter.
	 *
	 * @param lineDelimiter the default line delimiter
	 */
	void setInitialLineDelimiter(String lineDelimiter);
}
