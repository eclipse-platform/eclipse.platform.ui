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
 * A document rewrite session.
 * <p>
 * This class is not yet for public use. API under construction.
 * 
 * @see org.eclipse.jface.text.IDocument
 * @see org.eclipse.jface.text.IDocumentExtension4
 * @see org.eclipse.jface.text.IDocumentRewriteSessionListener
 * @since 3.1
 */
public class DocumentRewriteSession {
	
	private DocumentRewriteSessionType fSessionType;
	
	/**
	 * Prohibit package external object creation.
	 * 
	 * @param sessionType the type of this session
	 */
	protected DocumentRewriteSession(DocumentRewriteSessionType sessionType) {
		fSessionType= sessionType;
	}
	
	/**
	 * Returns the type of this session.
	 * 
	 * @return the type of this session
	 */
	public DocumentRewriteSessionType getSessionType() {
		return fSessionType;
	}
}
