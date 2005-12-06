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
package org.eclipse.ui.editors.text;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.content.IContentType;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.source.IAnnotationModel;

import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProviderExtension;
import org.eclipse.ui.texteditor.IDocumentProviderExtension2;
import org.eclipse.ui.texteditor.IDocumentProviderExtension3;
import org.eclipse.ui.texteditor.IDocumentProviderExtension4;
import org.eclipse.ui.texteditor.IDocumentProviderExtension5;
import org.eclipse.ui.texteditor.IElementStateListener;


/**
 * A forwarding document provider is a document provider that forwards all requests
 * to a known parent document provider. The only functional addition of the
 * forwarding document provider is that it ensures that all documents provided
 * to clients are appropriately set up.
 * <p>
 * This document provider should be used by editors that directly or indirectly
 * work with a {@link org.eclipse.ui.editors.text.TextFileDocumentProvider} and do not
 * accept that they may be provided with documents which do not contain the partitioning
 * they work on. This can happen either because of a plug-in configuration error
 * or when a user associates a file name or file extension with an existing text
 * editor and the file buffer infrastructure does not recognize that file name
 * or file extension to be of the same file type the editor works on. Thus, the
 * document provided for the files with that name or extension may not be set up
 * in the way the editor expects it. The <code>ForwardingDocumentProvider</code>
 * compensates for that situation.
 * </p>
 * <p>
 * Editors that directly work with a {@link org.eclipse.ui.editors.text.TextFileDocumentProvider} can
 * now use a <code>ForwardingDocumentProvider</code> instead and configure a
 * {@link org.eclipse.ui.editors.text.TextFileDocumentProvider} as its parent provider. Editors that
 * indirectly work with a {@link org.eclipse.ui.editors.text.TextFileDocumentProvider}, e.g. never
 * set a document provider explicitly, should explicitly set a
 * <code>ForwardingDocumentProvider</code> as document provider. In this case
 * the forwarding document provider may not be shared between editors.
 * </p>
 *
 * @since 3.0
 */
public class ForwardingDocumentProvider implements IDocumentProvider, IDocumentProviderExtension, IDocumentProviderExtension2, IDocumentProviderExtension3, IDocumentProviderExtension4, IDocumentProviderExtension5, IStorageDocumentProvider {

	private IDocumentProvider fParentProvider;
	private String fPartitioning;
	private IDocumentSetupParticipant fDocumentSetupParticipant;
	private boolean fAllowSetParentProvider;


	/**
	 * Creates a new forwarding document provider with a fixed parent document provider. Calling
	 * {@link #setParentProvider(IDocumentProvider)} does not have any effect on this object.
	 *
	 * @param partitioning the partitioning
	 * @param documentSetupParticipant the document setup participant
	 * @param parentProvider the parent document provider
	 */
	public ForwardingDocumentProvider(String partitioning, IDocumentSetupParticipant documentSetupParticipant, IDocumentProvider parentProvider) {
		fPartitioning= partitioning;
		fDocumentSetupParticipant= documentSetupParticipant;
		fParentProvider= parentProvider;
		fAllowSetParentProvider= false;
	}

	/**
	 * Creates a new forwarding document provider with a dynamically changeable
	 * parent provider. Forwarding document providers created with that method
	 * are not allowed to be shared by multiple editors.
	 *
	 * @param partitioning the partitioning
	 * @param documentSetupParticipant the document setup participant
	 */
	public ForwardingDocumentProvider(String partitioning, IDocumentSetupParticipant documentSetupParticipant) {
		fPartitioning= partitioning;
		fDocumentSetupParticipant= documentSetupParticipant;
		fAllowSetParentProvider= true;
	}

	/**
	 * Sets the parent document provider. This method has only an effect if the
	 * forwarding document provider has accordingly be created.
	 *
	 * @param parentProvider the new parent document provider
	 */
	public void setParentProvider(IDocumentProvider parentProvider) {
		if (fAllowSetParentProvider)
			fParentProvider= parentProvider;
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#connect(java.lang.Object)
	 */
	public void connect(Object element) throws CoreException {
		fParentProvider.connect(element);
		IDocument document= fParentProvider.getDocument(element);
		if (document instanceof IDocumentExtension3) {
			IDocumentExtension3 extension= (IDocumentExtension3) document;
			if (extension.getDocumentPartitioner(fPartitioning) == null)
				fDocumentSetupParticipant.setup(document);
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#disconnect(java.lang.Object)
	 */
	public void disconnect(Object element) {
		fParentProvider.disconnect(element);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#getDocument(java.lang.Object)
	 */
	public IDocument getDocument(Object element) {
		return fParentProvider.getDocument(element);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#resetDocument(java.lang.Object)
	 */
	public void resetDocument(Object element) throws CoreException {
		fParentProvider.resetDocument(element);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#saveDocument(org.eclipse.core.runtime.IProgressMonitor, java.lang.Object, org.eclipse.jface.text.IDocument, boolean)
	 */
	public void saveDocument(IProgressMonitor monitor, Object element, IDocument document, boolean overwrite) throws CoreException {
		fParentProvider.saveDocument(monitor, element, document, overwrite);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#getModificationStamp(java.lang.Object)
	 */
	public long getModificationStamp(Object element) {
		return fParentProvider.getModificationStamp(element);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#getSynchronizationStamp(java.lang.Object)
	 */
	public long getSynchronizationStamp(Object element) {
		return fParentProvider.getSynchronizationStamp(element);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#isDeleted(java.lang.Object)
	 */
	public boolean isDeleted(Object element) {
		return fParentProvider.isDeleted(element);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#mustSaveDocument(java.lang.Object)
	 */
	public boolean mustSaveDocument(Object element) {
		return fParentProvider.mustSaveDocument(element);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#canSaveDocument(java.lang.Object)
	 */
	public boolean canSaveDocument(Object element) {
		return fParentProvider.canSaveDocument(element);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#getAnnotationModel(java.lang.Object)
	 */
	public IAnnotationModel getAnnotationModel(Object element) {
		return fParentProvider.getAnnotationModel(element);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#aboutToChange(java.lang.Object)
	 */
	public void aboutToChange(Object element) {
		fParentProvider.aboutToChange(element);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#changed(java.lang.Object)
	 */
	public void changed(Object element) {
		fParentProvider.changed(element);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#addElementStateListener(org.eclipse.ui.texteditor.IElementStateListener)
	 */
	public void addElementStateListener(IElementStateListener listener) {
		fParentProvider.addElementStateListener(listener);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#removeElementStateListener(org.eclipse.ui.texteditor.IElementStateListener)
	 */
	public void removeElementStateListener(IElementStateListener listener) {
		fParentProvider.removeElementStateListener(listener);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProviderExtension#isReadOnly(java.lang.Object)
	 */
	public boolean isReadOnly(Object element) {
		if (fParentProvider instanceof IDocumentProviderExtension) {
			IDocumentProviderExtension extension= (IDocumentProviderExtension)fParentProvider;
			return extension.isReadOnly(element);
		}
		return false;
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProviderExtension#isModifiable(java.lang.Object)
	 */
	public boolean isModifiable(Object element) {
		if (fParentProvider instanceof IDocumentProviderExtension) {
			IDocumentProviderExtension extension= (IDocumentProviderExtension)fParentProvider;
			return extension.isModifiable(element);
		}
		return true;
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProviderExtension#validateState(java.lang.Object, java.lang.Object)
	 */
	public void validateState(Object element, Object computationContext) throws CoreException {
		if (fParentProvider instanceof IDocumentProviderExtension) {
			IDocumentProviderExtension extension= (IDocumentProviderExtension)fParentProvider;
			extension.validateState(element, computationContext);
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProviderExtension#isStateValidated(java.lang.Object)
	 */
	public boolean isStateValidated(Object element) {
		if (fParentProvider instanceof IDocumentProviderExtension) {
			IDocumentProviderExtension extension= (IDocumentProviderExtension)fParentProvider;
			return extension.isStateValidated(element);
		}
		return true;
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProviderExtension#updateStateCache(java.lang.Object)
	 */
	public void updateStateCache(Object element) throws CoreException {
		if (fParentProvider instanceof IDocumentProviderExtension) {
			IDocumentProviderExtension extension= (IDocumentProviderExtension)fParentProvider;
			extension.updateStateCache(element);
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProviderExtension#setCanSaveDocument(java.lang.Object)
	 */
	public void setCanSaveDocument(Object element) {
		if (fParentProvider instanceof IDocumentProviderExtension) {
			IDocumentProviderExtension extension= (IDocumentProviderExtension)fParentProvider;
			extension.setCanSaveDocument(element);
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProviderExtension#getStatus(java.lang.Object)
	 */
	public IStatus getStatus(Object element) {
		if (fParentProvider instanceof IDocumentProviderExtension) {
			IDocumentProviderExtension extension= (IDocumentProviderExtension)fParentProvider;
			return extension.getStatus(element);
		}
		return null;
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProviderExtension#synchronize(java.lang.Object)
	 */
	public void synchronize(Object element) throws CoreException {
		if (fParentProvider instanceof IDocumentProviderExtension) {
			IDocumentProviderExtension extension= (IDocumentProviderExtension)fParentProvider;
			extension.synchronize(element);
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProviderExtension2#setProgressMonitor(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void setProgressMonitor(IProgressMonitor progressMonitor) {
		if (fParentProvider instanceof IDocumentProviderExtension2) {
			IDocumentProviderExtension2 extension= (IDocumentProviderExtension2)fParentProvider;
			extension.setProgressMonitor(progressMonitor);
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProviderExtension2#getProgressMonitor()
	 */
	public IProgressMonitor getProgressMonitor() {
		if (fParentProvider instanceof IDocumentProviderExtension2) {
			IDocumentProviderExtension2 extension= (IDocumentProviderExtension2)fParentProvider;
			return extension.getProgressMonitor();
		}
		return null;
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProviderExtension3#isSynchronized(java.lang.Object)
	 */
	public boolean isSynchronized(Object element) {
		if (fParentProvider instanceof IDocumentProviderExtension3) {
			IDocumentProviderExtension3 extension= (IDocumentProviderExtension3)fParentProvider;
			return extension.isSynchronized(element);
		}
		return true;
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProviderExtension5#isNotSynchronizedException(Object, CoreException)
	 * @since 3.2
	 */
	public boolean isNotSynchronizedException(Object element, CoreException ex) {
		if (fParentProvider instanceof IDocumentProviderExtension5) {
			IDocumentProviderExtension5 extension= (IDocumentProviderExtension5)fParentProvider;
			return extension.isNotSynchronizedException(element, ex);
		}
		return false;
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProviderExtension4#getContentType(java.lang.Object)
	 * @since 3.1
	 */
	public IContentType getContentType(Object element) throws CoreException {
		if (fParentProvider instanceof IDocumentProviderExtension4) {
			IDocumentProviderExtension4 extension= (IDocumentProviderExtension4)fParentProvider;
			return extension.getContentType(element);
		}
		return null;
	}

	/*
	 * @see org.eclipse.ui.editors.text.IStorageDocumentProvider#getDefaultEncoding()
	 */
	public String getDefaultEncoding() {
		if (fParentProvider instanceof IStorageDocumentProvider) {
			IStorageDocumentProvider provider= (IStorageDocumentProvider)fParentProvider;
			return provider.getDefaultEncoding();
		}
		return null;
	}

	/*
	 * @see org.eclipse.ui.editors.text.IStorageDocumentProvider#getEncoding(java.lang.Object)
	 */
	public String getEncoding(Object element) {
		if (fParentProvider instanceof IStorageDocumentProvider) {
			IStorageDocumentProvider provider= (IStorageDocumentProvider)fParentProvider;
			return provider.getEncoding(element);
		}
		return null;
	}

	/*
	 * @see org.eclipse.ui.editors.text.IStorageDocumentProvider#setEncoding(java.lang.Object, java.lang.String)
	 */
	public void setEncoding(Object element, String encoding) {
		if (fParentProvider instanceof IStorageDocumentProvider) {
			IStorageDocumentProvider provider= (IStorageDocumentProvider)fParentProvider;
			provider.setEncoding(element, encoding);
		}
	}
}
