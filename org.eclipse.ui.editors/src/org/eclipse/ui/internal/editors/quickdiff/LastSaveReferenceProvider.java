/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.editors.quickdiff;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.quickdiff.IQuickDiffProviderImplementation;
import org.eclipse.ui.editors.text.StorageDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IElementStateListener;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Default provider for the quickdiff display - the saved document is taken as the reference.
 * 
 * @since 3.0
 */
public class LastSaveReferenceProvider implements IQuickDiffProviderImplementation, IElementStateListener {

	/** <code>true</code> if the document has been read. */
	private boolean fDocumentRead= false;
	/** The text editor we work on. */
	private ITextEditor fEditor= null;
	/** The reference document - might be <code>null</code> even if <code>fDocumentRead</code> is <code>true</code>. */
	private IDocument fReference= null;
	/** Our unique id that makes us comparable to another instance of the same provider. See extension point reference. */
	private String fId;

	/*
	 * @see org.eclipse.ui.editors.quickdiff.IQuickDiffReferenceProvider#getReference()
	 */
	public IDocument getReference() {
		if (!fDocumentRead)
			readDocument();
		return fReference;
	}

	/**
	 * Intitializes the given document with the given stream using the given encoding.
	 *
	 * @param document the document to be initialized
	 * @param contentStream the stream which delivers the document content
	 * @param encoding the character encoding for reading the given stream
	 * @exception CoreException if the given stream can not be read
	 */
	private static void setDocumentContent(IDocument document, InputStream contentStream, String encoding) throws CoreException {
		Reader in= null;
		try {
			final int DEFAULT_FILE_SIZE= 15 * 1024;

			in= new BufferedReader(new InputStreamReader(contentStream, encoding), DEFAULT_FILE_SIZE);
			StringBuffer buffer= new StringBuffer(DEFAULT_FILE_SIZE);
			char[] readBuffer= new char[2048];
			int n= in.read(readBuffer);
			while (n > 0) {
				buffer.append(readBuffer, 0, n);
				n= in.read(readBuffer);
			}
			document.set(buffer.toString());
		} catch (IOException x) {
			String msg= x.getMessage() == null ? "" : x.getMessage(); //$NON-NLS-1$
			IStatus s= new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.OK, msg, x);
			throw new CoreException(s);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException x) {
				}
			}
		}
	}

	/**
	 * Reads in the saved document into <code>fReference</code>.
	 */
	private void readDocument() {
		if (!initialized())
			return;
		fEditor.getDocumentProvider().addElementStateListener(this);
		IDocumentProvider provider= fEditor.getDocumentProvider();
		IEditorInput input= fEditor.getEditorInput();
		if (!(input instanceof IFileEditorInput))
			return;
		if (fReference == null)
			fReference= new Document();

		if (provider instanceof StorageDocumentProvider) {
			InputStream stream= null;
			try {
				stream= ((IFileEditorInput)input).getFile().getContents();
			} catch (CoreException e) {
				return;
			}
			StorageDocumentProvider sProvider= (StorageDocumentProvider)provider;
			try {
				String encoding= sProvider.getEncoding(input);
				if (encoding == null)
					encoding= sProvider.getDefaultEncoding();
				setDocumentContent(fReference, stream, encoding);
			} catch (CoreException e1) {
				return;
			}
		}
		fDocumentRead= true;
	}

	/*
	 * @see org.eclipse.ui.texteditor.IElementStateListener#elementDirtyStateChanged(java.lang.Object, boolean)
	 */
	public void elementDirtyStateChanged(Object element, boolean isDirty) {
		if (!initialized())
			return;
		if (!isDirty && element == fEditor.getEditorInput()) {
			// document has been saved or reverted - recreate reference
			readDocument();
		}
	}

	/**
	 * Returns <code>true</code> if the receiver has been initialized, i.e. an editor has been set.
	 * 
	 * @return <code>true</code> if the receiver has been initialized.
	 */
	private boolean initialized() {
		return fEditor != null;
	}

	/*
	 * @see org.eclipse.ui.texteditor.IElementStateListener#elementContentAboutToBeReplaced(java.lang.Object)
	 */
	public void elementContentAboutToBeReplaced(Object element) {
	}

	/*
	 * @see org.eclipse.ui.texteditor.IElementStateListener#elementContentReplaced(java.lang.Object)
	 */
	public void elementContentReplaced(Object element) {
	}

	/*
	 * @see org.eclipse.ui.texteditor.IElementStateListener#elementDeleted(java.lang.Object)
	 */
	public void elementDeleted(Object element) {
	}

	/*
	 * @see org.eclipse.ui.texteditor.IElementStateListener#elementMoved(java.lang.Object, java.lang.Object)
	 */
	public void elementMoved(Object originalElement, Object movedElement) {
	}

	/*
	 * @see org.eclipse.ui.editors.quickdiff.IQuickDiffProviderImplementation#setActiveEditor(org.eclipse.ui.texteditor.ITextEditor)
	 */
	public void setActiveEditor(ITextEditor targetEditor) {
		if (targetEditor != fEditor) {
			dispose();
			fEditor= targetEditor;
		}
	}

	/*
	 * @see org.eclipse.ui.editors.quickdiff.IQuickDiffProviderImplementation#isEnabled()
	 */
	public boolean isEnabled() {
		return initialized();
	}

	/*
	 * @see org.eclipse.ui.editors.quickdiff.IQuickDiffReferenceProvider#dispose()
	 */
	public void dispose() {
		if (fEditor != null)
			fEditor.getDocumentProvider().removeElementStateListener(this);
		fReference= null;
		fDocumentRead= false;
	}

	/*
	 * @see org.eclipse.ui.editors.quickdiff.IQuickDiffProviderImplementation#setId(java.lang.String)
	 */
	public void setId(String id) {
		fId= id;
	}

	/*
	 * @see org.eclipse.ui.editors.quickdiff.IQuickDiffReferenceProvider#getId()
	 */
	public String getId() {
		return fId;
	}

}
