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

import org.eclipse.compare.structuremergeviewer.SharedDocumentAdapterWrapper;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * An <code>ISharedDocumentAdapter</code> is used to map an {@link ITypedElement} to
 * a shared document for the purposes of editing.
 * <p>
 * Clients are not expected to implement this interface but instead should subclass 
 * {@link SharedDocumentAdapter} or {@link SharedDocumentAdapterWrapper}.
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
	 * <code>DocumentProviderRegistry</code> and for obtaining the shared
	 * {@link IDocument} from the document provider. Returns <code>null</code>
	 * if the element does not have a shared document.
	 * 
	 * @param element
	 *            the element being queried for a shared document
	 * @return the object that acts as the key to obtain a document provider and
	 *         document or <code>null</code>
	 */
	IEditorInput getDocumentKey(Object element);
	
	/**
	 * Connect the given element to its document provider. All connections must be performed
	 * through this adapter so that the adapter can track whether it is connected or not.
	 * @param provider the document provider
	 * @param documentKey the element's key returned from {@link #getDocumentKey(Object)}
	 * @throws CoreException if connection was not possible
	 * @see IDocumentProvider#connect(Object)
	 */
	void connect(IDocumentProvider provider, IEditorInput documentKey) throws CoreException;
	
	/**
	 * Disconnect the element from the document provider. All connects and
	 * disconnects must occur through the adapter so that the adapter can 
	 * track whether it is connected or not.
	 * @param provider the document provider
	 * @param documentKey the element's key returned from {@link #getDocumentKey(Object)}
	 * @see IDocumentProvider#disconnect(Object)
	 */
	void disconnect(IDocumentProvider provider, IEditorInput documentKey);
	
	/**
	 * Saves the given document provided for the given element from the given
	 * document provider. Saves should be performed through the adapter to allow
	 * the provider of the compare input to perform any additional processing on save.
	 * 
	 * @param provider the document provider
	 * @param documentKey the element's key returned from {@link #getDocumentKey(Object)}
	 * @param document the document
	 * @param overwrite indicates whether overwrite should be performed
	 * 			while saving the given element if necessary
	 * @param monitor a progress monitor to report progress and request cancelation
	 * @exception CoreException if document could not be stored to the given element
	 * @see IDocumentProvider#saveDocument(IProgressMonitor, Object, IDocument, boolean)
	 */
	void saveDocument(IDocumentProvider provider, IEditorInput documentKey, IDocument document, boolean overwrite, IProgressMonitor monitor) throws CoreException;

}
