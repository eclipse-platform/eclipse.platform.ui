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
package org.eclipse.ui.texteditor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.content.IContentType;


/**
 * Extension interface for {@link org.eclipse.ui.texteditor.IDocumentProvider}.
 * Extends a document provider with the ability to query the content type
 * of a given element.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 *
 * @see org.eclipse.ui.texteditor.IDocumentProvider
 * @since 3.1
 */
public interface IDocumentProviderExtension4 {

	/**
	 * Returns the content type of for the given element or
	 * <code>null</code> if none could be determined. If the element's
	 * document can be saved, the returned content type is determined by the
	 * document's current content.
	 *
	 * @param element the element
	 * @return the content type or <code>null</code>
	 * @throws CoreException if reading or accessing the underlying store
	 *                 fails
	 */
	IContentType getContentType(Object element) throws CoreException;
}
