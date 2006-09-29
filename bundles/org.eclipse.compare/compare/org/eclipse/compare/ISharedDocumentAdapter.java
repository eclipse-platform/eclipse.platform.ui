/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare;

import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.DocumentProviderRegistry;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * An <code>ISharedDocumentAdapter</code> is used to map an {@link ITypedElement} to
 * a shared document for the purposes of editing.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * @since 3.3
 */
public interface ISharedDocumentAdapter {

	/**
	 * Return the object that is to be used as the key for retrieving the
	 * appropriate {@link IDocumentProvider} from the
	 * {@link DocumentProviderRegistry} and for obtaining the shared
	 * {@link IDocument} from the document provider. Returns <code>null</code>
	 * if the element does not have a shared document.
	 * 
	 * @param element
	 *            the element being queried for a shared document
	 * @return the object that acts as the key to obtain a document provider and
	 *         document or <code>null</code>
	 */
	IEditorInput getDocumentKey(Object element);

}
