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
 * Extension interface for {@link org.eclipse.jface.text.IDocumentPartitioner}. Adds the
 * concept of rewrite sessions. A rewrite session is a sequence of replace
 * operations that form a semantic unit.
 * <p>
 * This class is not yet for public use. API under construction.
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
	 * @throws IllegalStateException in case there is already an active rewrite
	 *             session
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
}
