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
package org.eclipse.ui.texteditor.spelling;

import org.eclipse.core.runtime.content.IContentType;


/**
 * A spelling context allows a {@link ISpellingEngine} to retrieve information
 * about the spelling check it has to perform.
 * <p>
 * This class is not intended to be subclassed by clients. The amount of
 * information provided in this context may grow over time.
 * </p>
 *
 * @since 3.1
 * @noextend This class is not intended to be subclassed by clients.
 */
public class SpellingContext {

	/** Content type of the document */
	private IContentType fContentType;

	/**
	 * Creates a new, un-initialized spelling context.
	 */
	public SpellingContext() {
	}

	/**
	 * Sets the content type of the document.
	 *
	 * @param contentType the content type of the document or <code>null</code> if unknown
	 */
	public void setContentType(IContentType contentType) {
		fContentType= contentType;
	}

	/**
	 * Returns the content type of the document.
	 *
	 * @return the content type of the document or <code>null</code> if unknown
	 */
	public IContentType getContentType() {
		return fContentType;
	}
}
