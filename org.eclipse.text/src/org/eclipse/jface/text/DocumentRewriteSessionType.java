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
 * A document rewrite session type. Allowed values are
 * {@link DocumentRewriteSessionType#UNRESTRICTED},
 * {@link DocumentRewriteSessionType#SEQUENTIAL},
 * {@link DocumentRewriteSessionType#STRICTLY_SEQUENTIAL}.
 * <p>
 * This class is not yet for public use. API under construction.
 * 
 * @see org.eclipse.jface.text.IDocument
 * @see org.eclipse.jface.text.IDocumentExtension4
 * @see org.eclipse.jface.text.IDocumentRewriteSessionListener
 * @since 3.1
 */
public class DocumentRewriteSessionType {
	
	/**
	 * A unrestricted rewrite session is a sequence of unrestricted replace
	 * operations.
	 */
	public final static DocumentRewriteSessionType UNRESTRICTED= new DocumentRewriteSessionType();
	/**
	 * A sequential rewrite session is a sequence of non-overlapping replace
	 * operations starting at an arbitrary document offset.
	 */
	public final static DocumentRewriteSessionType SEQUENTIAL= new DocumentRewriteSessionType();
	/**
	 * A strictly sequential rewrite session is a sequence of non-overlapping
	 * replace operations from the start of the document to its end.
	 */
	public final static DocumentRewriteSessionType STRICTLY_SEQUENTIAL= new DocumentRewriteSessionType();


	/**
	 * Prohibit external object creation.
	 */
	private DocumentRewriteSessionType() {
	}
}
