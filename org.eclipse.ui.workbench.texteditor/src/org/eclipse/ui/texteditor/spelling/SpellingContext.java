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

package org.eclipse.ui.texteditor.spelling;

import org.eclipse.core.runtime.content.IContentType;

/**
 * Spelling context that allows a {@link ISpellingEngine} to retrieve
 * information about the spelling check it has to perform.
 * <p>
 * This class is not intended to be subclassed by clients.
 * </p><p>
 * Not yet for public use. API under construction.
 * </p>
 * 
 * @since 3.1
 */
public class SpellingContext {
	
	/** Content type of the document */
	private IContentType fContentType;
	
	/**
	 * Sets the content type of the document.
	 * 
	 * @param contentType the content type of the document
	 */
	public void setContentType(IContentType contentType) {
		fContentType= contentType;
	}
	
	/**
	 * Returns the content type of the document.
	 * 
	 * @return the content type of the document
	 */
	public IContentType getContentType() {
		return fContentType;
	}
}
