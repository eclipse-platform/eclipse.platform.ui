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

import java.io.UnsupportedEncodingException;

import org.eclipse.compare.*;
import org.eclipse.compare.contentmergeviewer.IDocumentRange;
import org.eclipse.compare.internal.CompareUIPlugin;
import org.eclipse.compare.internal.Utilities;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.*;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.services.IDisposable;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * An {@link IStructureCreator2} that attempts to use an {@link IDocumentProvider}
 * to obtain a shared document for an {@link ITypedElement}.
 * <p>
 * Clients may subclass this class.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @since 3.3
 */
public abstract class StructureCreator implements IStructureCreator2 {

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
			return createStructureComparator(input, doc, null, null);
		} catch (CoreException e) {
			CompareUIPlugin.log(e);
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.structuremergeviewer.IStructureCreator2#createStructure(java.lang.Object, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStructureComparator createStructure(Object element, IProgressMonitor monitor) throws CoreException {
		IDocument document = null;
		final ISharedDocumentAdapter sda = SharedDocumentAdapterWrapper.getAdapter(element);
		if (sda != null) {
			final IEditorInput input = sda.getDocumentKey(element);
			if (input != null) {
				final IDocumentProvider provider = SharedDocumentAdapter.getDocumentProvider(input);
				if (provider != null) {
					try {
						sda.connect(provider, input);
						document = provider.getDocument(input);
						setupDocument(document);
						return createStructureComparator(element, document, wrapSharedDocumentAdapter(sda, element, document), monitor);
					} catch (CoreException e) {
						// Connection to the document provider failed.
						// Log and fall through to use simple structure
						CompareUIPlugin.log(e);
					}
				}
			}
		}
		return getStructure(element);
	}
	
	/**
	 * Create an {@link IStructureComparator} for the given element using the
	 * contents available in the given document. If the provided
	 * {@link ISharedDocumentAdapter} is not <code>null</code> then the
	 * {@link IStructureComparator} returned by this method must implement the
	 * {@link IDisposable} interface and disconnect from the adapter when the
	 * comparator is disposed. The {@link StructureDiffViewer} class will call
	 * dispose if the {@link IStructureComparator} also implements
	 * {@link IDisposable}. Other clients must do the same.
	 * <p>
	 * It should be noted that the provided {@link ISharedDocumentAdapter}
	 * will provide the key associated with the given element when
	 * {@link ISharedDocumentAdapter#getDocumentKey(Object)} is called
	 * for any {@link IDocumentRange} node whose document matches the
	 * provided document. Thus, this adapter should also be returned
	 * by the structure comparator and its children when they are adapted
	 * to an {@link ISharedDocumentAdapter}.
	 * @param element the element
	 * @param document the document that has the contents for the element
	 * @param sharedDocumentAdapter the shared document adapter from which the
	 *            document was obtained or <code>null</code> if the document
	 *            is not shared.
	 * @param monitor a progress monitor or <code>null</code> if progress is not required
	 * 
	 * @return a structure comparator
	 * @throws CoreException
	 */
	protected IStructureComparator createStructureComparator(final Object element, IDocument document, final ISharedDocumentAdapter sharedDocumentAdapter, IProgressMonitor monitor) throws CoreException {
		IDisposable disposable = null;
		if (sharedDocumentAdapter != null) {
			disposable = new IDisposable() {
				public void dispose() {
					sharedDocumentAdapter.disconnect(element);
				}
			};
		}
		return createStructureComparator(element, document, disposable);
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
	 * @deprecated Subclasses should implement {@link #createStructureComparator(Object, IDocument, ISharedDocumentAdapter, IProgressMonitor)} instead
	 */
	protected IStructureComparator createStructureComparator(Object element,
			IDocument document, IDisposable disposable) throws CoreException {
		return null;
	}

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
	
	/**
	 * Default implementation of save that extracts the contents from 
	 * the document of an {@link IDocumentRange} and sets it on the
	 * input. If the input is an {@link IEncodedStreamContentAccessor},
	 * the charset of the input is used to extract the contents from the
	 * document. If the input adapts to {@link ISharedDocumentAdapter} and
	 * the document of the {@link IDocumentRange} matches that of the
	 * input, then the save is issued through the shared document adapter.
	 * @see org.eclipse.compare.structuremergeviewer.IStructureCreator#save(org.eclipse.compare.structuremergeviewer.IStructureComparator, java.lang.Object)
	 */
	public void save(IStructureComparator node, Object input) {
		if (node instanceof IDocumentRange && input instanceof IEditableContent) {
			IDocument document= ((IDocumentRange)node).getDocument();
			// First check to see if we have a shared document
			final ISharedDocumentAdapter sda = SharedDocumentAdapterWrapper.getAdapter(input);
			if (sda != null) {
				IEditorInput key = sda.getDocumentKey(input);
				if (key != null) {
					IDocumentProvider provider = SharedDocumentAdapter.getDocumentProvider(key);
					if (provider != null) {
						IDocument providerDoc = provider.getDocument(key);
						// We have to make sure that the document we are saving is the same as the shared document
						if (providerDoc != null && providerDoc == document) {
							if (save(provider, document, input, sda, key))
								return;
						}
					}
				}
			}
			IEditableContent bca= (IEditableContent) input;
			String contents= document.get();
			String encoding= null;
			if (input instanceof IEncodedStreamContentAccessor) {
				try {
					encoding= ((IEncodedStreamContentAccessor)input).getCharset();
				} catch (CoreException e1) {
					// ignore
				}
			}
			if (encoding == null)
				encoding= ResourcesPlugin.getEncoding();
			byte[] bytes;				
			try {
				bytes= contents.getBytes(encoding);
			} catch (UnsupportedEncodingException e) {
				bytes= contents.getBytes();	
			}
			bca.setContent(bytes);
		}
	}

	private boolean save(final IDocumentProvider provider, final IDocument document,
			final Object input, final ISharedDocumentAdapter sda, final IEditorInput key) {
		try {
			sda.flushDocument(provider, key, document, false);
			return true;
		} catch (CoreException e) {
			CompareUIPlugin.log(e);
		}
		return false;
	}
	
	/**
	 * Create an {@link ISharedDocumentAdapter} that will provide the document key for the given input
	 * object for any {@link DocumentRangeNode} instances whose document is the same as the 
	 * provided document.
	 * @param input the input element
	 * @param document the document associated with the input element
	 * @return a shared document adapter that provides the proper document key for document range nodes
	 */
	private final ISharedDocumentAdapter wrapSharedDocumentAdapter(ISharedDocumentAdapter elementAdapter, final Object input, final IDocument document) {
		// We need to wrap the adapter so that the proper document key gets returned
		return new SharedDocumentAdapterWrapper(elementAdapter) {
			public IEditorInput getDocumentKey(Object element) {
				if (hasSameDocument(element)) {
					return super.getDocumentKey(input);
				}
				return super.getDocumentKey(element);
			}
			private boolean hasSameDocument(Object element) {
				if (element instanceof DocumentRangeNode) {
					DocumentRangeNode drn = (DocumentRangeNode) element;
					return drn.getDocument() == document;
				}
				return false;
			}
		};
	}
}
