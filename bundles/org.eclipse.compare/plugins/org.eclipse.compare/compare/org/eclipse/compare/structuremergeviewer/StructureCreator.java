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
import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.*;
import org.eclipse.compare.contentmergeviewer.IDocumentRange;
import org.eclipse.compare.internal.*;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.*;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
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
			return createStructureComparator(input, doc, null);
		} catch (CoreException e) {
			CompareUIPlugin.log(e);
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.structuremergeviewer.ITextStructureCreator#getStructure(java.lang.Object, org.eclipse.compare.structuremergeviewer.IDocumentManager)
	 */
	public IStructureComparator createStructure(Object element) {
		IDocument document = null;
		final IEditorInput input = getDocumentKey(element);
		if (input != null) {
			final IDocumentProvider provider = SharedDocumentAdapter.getDocumentProvider(input);
			if (provider != null) {
				try {
					final ISharedDocumentAdapter sda = (ISharedDocumentAdapter) Utilities.getAdapter(input, ISharedDocumentAdapter.class);
					if (sda != null) {
						sda.connect(provider, input);
					} else {
						provider.connect(input);
					}
					document = provider.getDocument(input);
					IDisposable disposable = new IDisposable() {
						public void dispose() {
							if (sda != null) {
								sda.disconnect(provider, input);
							} else {
								provider.disconnect(input);
							}
						}
					};
					setupDocument(document);
					return createStructureComparator(element, document, disposable);
				} catch (CoreException e) {
					// Connection to the document provider failed.
					// Log and fall through to use simple structure
					CompareUIPlugin.log(e);
				}
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
	
	private IEditorInput getDocumentKey(Object element) {
		IEditorInput input = (IEditorInput)Utilities.getAdapter(element, IEditorInput.class);
		if (input != null)
			return input;
		ISharedDocumentAdapter sda = (ISharedDocumentAdapter)Utilities.getAdapter(element, ISharedDocumentAdapter.class, true);
		if (sda != null) {
			return sda.getDocumentKey(element);
		}
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
			final ISharedDocumentAdapter sda = (ISharedDocumentAdapter) Utilities.getAdapter(input, ISharedDocumentAdapter.class);
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
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						doSave(provider, document, input, sda, key, monitor);
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					}
				}
			};
			IProgressService progressService= PlatformUI.getWorkbench().getProgressService();
			progressService.run(false,false, runnable);
			return true;
		} catch (InvocationTargetException e) {
			// TODO: Should show error to the user
			Throwable t = e.getTargetException();
			CompareUIPlugin.log(t);
		} catch (InterruptedException e) {
			// Ignore
		}
		return false;
	}
	
	private void doSave(IDocumentProvider provider, IDocument document,
			Object input, final ISharedDocumentAdapter sda, IEditorInput key, IProgressMonitor monitor) throws CoreException {
		try {
			provider.aboutToChange(key);
			sda.saveDocument(provider, key, document, false, monitor);
		} finally {
			provider.changed(input);
		}
	}
}
