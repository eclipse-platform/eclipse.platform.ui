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
 * Extension interface for {@link org.eclipse.jface.text.ILineTracker}. Adds the
 * concept of rewrite sessions. A rewrite session is a sequence of replace
 * operations that form a semantic unit.
 * <p>
 * This class is not yet for public use. API under construction.
 * 
 * @since 3.1
 */
public interface ILineTrackerExtension {

	/**
	 * Tells the line tracker that a rewrite session is about to start. A
	 * rewrite session is a sequence of replace operations that form a semantic
	 * unit.
	 * <p>
	 * The line tracker is considered being in rewrite mode as long as
	 * <code>stopRewriteSession</code> has not been called.
	 * 
	 * @param sessionId the session id
	 */
	void startRewriteSession(Object sessionId);

	/**
	 * Tells the line tracker that the rewrite session has finished. This method
	 * has only any effect if <code>startRewriteSession</code> has been called
	 * before.
	 * 
	 * @param sessionId the session id
	 * @param text the text with which to re-initialize the line tracker
	 */
	void stopRewriteSession(Object sessionId, String text);
}
