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

/**
 * Extension interface for {@link org.eclipse.jface.text.IDocument}.<p>
 *
 * It introduces the notion of sequentially rewriting a document. This is to tell a
 * document that a sequence of non-overlapping replace operation is about to be
 * performed. Implementers can use this knowledge for internal optimization.<p>
 *
 * Is also introduces the concept of post notification replaces. This is, a document
 * listener who is informed about a document change can cause a derived document
 * change. As the listener is not allowed to directly modify the document, it can
 * register a replace operation that is performed directly after all document listeners
 * have been notified.
 *
 * @since 2.0
 */
public interface IDocumentExtension {

	/**
	 * Interface for a post notification replace operation.
	 */
	public interface IReplace {

		/**
		 * Executes the replace operation on the given document.
		 *
		 * @param document the document to be changed
		 * @param owner the owner of this replace operation
		 */
		void perform(IDocument document, IDocumentListener owner);
	}

	/**
	 * Callback for document listeners to be used inside <code>documentChanged</code>
	 * to register a post notification replace operation on the document notifying them.
	 *
	 * @param owner the owner of the replace operation
	 * @param replace the replace operation to be executed
	 * @exception UnsupportedOperationException if <code>registerPostNotificationReplace</code>
	 * 	is not supported by this document
	 */
	void registerPostNotificationReplace(IDocumentListener owner, IReplace replace) throws UnsupportedOperationException;

	/**
	 * Stops the processing of registered post notification replace operations until
	 * <code>resumePostNotificationProcessing</code> is called.
	 */
	void stopPostNotificationProcessing();

	/**
	 * Resumes the processing of post notification replace operations. If the queue of registered
	 * <code>IDocumentExtension.IReplace</code> objects is not empty, they are immediately processed if the
	 * document is not inside a replace operation. If the document is inside a replace operation,
	 * they are processed directly after the replace operation has finished.
	 */
	void resumePostNotificationProcessing();

	/**
	 * Tells the document that it is about to be sequentially rewritten. That is a
	 * sequence of non-overlapping replace operations will be performed on it. The
	 * <code>normalize</code> flag indicates whether the rewrite is performed from
	 * the start of the document to its end or from an arbitrary start offset. <p>
	 *
	 * The document is considered being in sequential rewrite mode as long as
	 * <code>stopSequentialRewrite</code> has not been called.
	 *
	 * @param normalize <code>true</code> if performed from the start to the end of the document
	 * @deprecated since 3.1. Use {@link IDocumentExtension4#startRewriteSession(DocumentRewriteSessionType)} instead.
	 */
	@Deprecated
	void startSequentialRewrite(boolean normalize);

	/**
	 * Tells the document that the sequential rewrite has been finished. This method
	 * has only any effect if <code>startSequentialRewrite</code> has been called before.
	 * @deprecated since 3.1. Use {@link IDocumentExtension4#stopRewriteSession(DocumentRewriteSession)} instead.
	 */
	@Deprecated
	void stopSequentialRewrite();
}
