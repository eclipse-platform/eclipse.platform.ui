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
 * Extension interface for {@link org.eclipse.jface.text.IDocumentPartitioner}. Adds the
 * concept of rewrite sessions. A rewrite session is a sequence of replace
 * operations that form a semantic unit.
 *
 * @since 3.1
 */
public interface IDocumentPartitionerExtension3 {

	/**
	 * Tells the document partitioner that a rewrite session started. A rewrite
	 * session is a sequence of replace operations that form a semantic unit.
	 * The document partitioner is allowed to use that information for internal
	 * optimization.
	 *
	 * @param session the rewrite session
	 * @throws IllegalStateException in case there is already an active rewrite session
	 */
	void startRewriteSession(DocumentRewriteSession session) throws IllegalStateException;

	/**
	 * Tells the document partitioner that the rewrite session has finished.
	 * This method is only called when <code>startRewriteSession</code> has
	 * been called before.
	 *
	 * @param session the rewrite session
	 */
	void stopRewriteSession(DocumentRewriteSession session);

	/**
	 * Returns the active rewrite session of this document or <code>null</code>.
	 *
	 * @return the active rewrite session or <code>null</code>
	 */
	DocumentRewriteSession getActiveRewriteSession();

	/**
	 * Connects this partitioner to a document. Connect indicates the begin of
	 * the usage of the receiver as partitioner of the given document. Thus,
	 * resources the partitioner needs to be operational for this document
	 * should be allocated.
	 * <p>
	 * The caller of this method must ensure that this partitioner is also set
	 * as the document's document partitioner.
	 * <p>
	 * <code>delayInitialization</code> indicates whether the partitioner is
	 * allowed to delay it initial computation of the document's partitioning
	 * until it has to answer the first query.
	 *
	 * Replaces {@link IDocumentPartitioner#connect(IDocument)}.
	 *
	 * @param document the document to be connected to
	 * @param delayInitialization <code>true</code> if initialization can be delayed, <code>false</code> otherwise
	 */
	void connect(IDocument document, boolean delayInitialization);
}
