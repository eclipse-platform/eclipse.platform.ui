/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.structuremergeviewer;

import org.eclipse.compare.*;
import org.eclipse.compare.internal.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.*;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.services.IDisposable;
import org.eclipse.ui.texteditor.DocumentProviderRegistry;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * An {@link IStructureCreator2} that attempts to use an {@link IDocumentProvider}
 * to obtain a shared document for an {@link ITypedElement}.
 * <p>
 * Clients may subclass this class.
 * 
 * @since 3.3
 */
public abstract class DocumentStructureCreator implements IStructureCreator2 {

	/* (non-Javadoc)
	 * @see org.eclipse.compare.structuremergeviewer.IStructureCreator#getStructure(java.lang.Object)
	 */
	public IStructureComparator getStructure(Object input) {
		String contents= null;
		IDocument doc= CompareUI.getDocument(input);
		if (doc == null) {
			if (input instanceof IStreamContentAccessor) {
				IStreamContentAccessor sca= (IStreamContentAccessor) input;			
				try {
					contents= Utilities.readString(sca);
				} catch (CoreException e) {
					// return null indicates the error.
					CompareUIPlugin.log(e);
					return null;
				}			
			}
			
			if (contents != null) {
				doc= new Document(contents);
				setupDocument(doc);				
			}
		}
		
		try {
			return createStructureComparator(input, doc, null);
		} catch (CoreException e) {
			CompareUIPlugin.log(e);
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.structuremergeviewer.ITextStructureCreator#getStructure(java.lang.Object, org.eclipse.compare.structuremergeviewer.IDocumentManager)
	 */
	public IStructureComparator createStructure(Object element) throws CoreException {
		IDocument document = null;
		final IEditorInput input = getDocumentKey(element);
		if (input != null) {
			final IDocumentProvider provider = getDocumentProvider(input);
			if (provider != null) {
				provider.connect(input);
				document = provider.getDocument(input);
				IDisposable disposable = new IDisposable() {
					public void dispose() {
						provider.disconnect(input);
					}
				};
				setupDocument(document);
				return createStructureComparator(element, document, disposable);
			}
		}
		return getStructure(element);
		
	}
	
	/**
	 * Create an {@link IStructureComparator} for the given element using the
	 * contents available in the given document. If the provided disposable is
	 * not <code>null</code> then the {@link IStructureComparator} returned by
	 * this method should also be an {@link IDisposable} which delegates the
	 * dispose to the given disposable.
	 * 
	 * @param element
	 *            the element
	 * @param document
	 *            the document that has the contents for the element
	 * @param disposable
	 *            a disposable that must be disposed when the returned
	 *            {@link IStructureComparator} is no longer needed (or
	 *            <code>null</code> if disposal is not needed).
	 * @return a structure comparator
	 * @throws CoreException
	 */
	protected abstract IStructureComparator createStructureComparator(Object element,
			IDocument document, IDisposable disposable) throws CoreException;

	/**
	 * Setup the newly created document as appropriate. Any document partitioners
	 * should be added to a custom slot using the {@link IDocumentExtension3} interface
	 * in case the document is shared via a file buffer.
	 * @param document a document
	 */
	protected void setupDocument(IDocument document) {
		String partitioning = getDocumentPartitioning();
		if (partitioning == null || !(document instanceof IDocumentExtension3)) {
			if (document.getDocumentPartitioner() == null) {
				IDocumentPartitioner partitioner= getDocumentPartitioner();
				if (partitioner != null) {
					document.setDocumentPartitioner(partitioner);
					partitioner.connect(document);
				}
			}
		} else {
			IDocumentExtension3 ex3 = (IDocumentExtension3) document;
			if (ex3.getDocumentPartitioner(partitioning) == null) {
				IDocumentPartitioner partitioner= getDocumentPartitioner();
				if (partitioner != null) {
					ex3.setDocumentPartitioner(partitioning, partitioner);
					partitioner.connect(document);
				}
			}
		}
	}
	
	/**
	 * Return the partitioner to be associated with the document or
	 * <code>null</code> is partitioning is not needed or if the subclass
	 * overrode {@link #setupDocument(IDocument)} directly.
	 * @return a partitioner
	 */
	protected IDocumentPartitioner getDocumentPartitioner() {
		return null;
	}

	/**
	 * Return the partitioning to which the partitioner returned from
	 * {@link #getDocumentPartitioner()} is to be associated. Return <code>null</code>
	 * only if partitioning is not needed or if the subclass
	 * overrode {@link #setupDocument(IDocument)} directly.
	 * @see IDocumentExtension3
	 * @return a partitioning
	 */
	protected String getDocumentPartitioning() {
		return null;
	}

	private IDocumentProvider getDocumentProvider(IEditorInput input) {
		return DocumentProviderRegistry.getDefault().getDocumentProvider(input);
	}
	
	private IEditorInput getDocumentKey(Object element) {
		IEditorInput input = (IEditorInput)Utilities.getAdapter(element, IEditorInput.class);
		if (input != null)
			return input;
		ISharedDocumentAdapter sda = (ISharedDocumentAdapter)Utilities.getAdapter(element, ISharedDocumentAdapter.class, true);
		if (sda != null) {
			return sda.getDocumentKey(element);
		}
		if (element instanceof ITypedElement) {
			ITypedElement te = (ITypedElement) element;
			return new TypedElementEditorInput(te);
		}
		return null;
	}
}
