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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.IStorageDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IElementStateListener;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.quickdiff.IQuickDiffProviderImplementation;

/**
 * Default provider for the quickdiff display - the saved document is taken as the reference.
 * 
 * @since 3.0
 */
public class LastSaveReferenceProvider implements IQuickDiffProviderImplementation, IElementStateListener {

	/** <code>true</code> if the document has been read. */
	private boolean fDocumentRead= false;
	/** The reference document - might be <code>null</code> even if <code>fDocumentRead</code> is <code>true</code>. */
	private IDocument fReference= null;
	/** Our unique id that makes us comparable to another instance of the same provider. See extension point reference. */
	private String fId;
	/** The current document provider. */
	private IDocumentProvider fDocumentProvider;
	/** The current editor input. */
	private IEditorInput fEditorInput;

	/*
	 * @see org.eclipse.ui.editors.quickdiff.IQuickDiffReferenceProvider#getReference()
	 */
	public IDocument getReference(IProgressMonitor monitor) {
		if (!fDocumentRead)
			readDocument();
		return fReference;
	}

	/*
	 * @see org.eclipse.ui.editors.quickdiff.IQuickDiffReferenceProvider#dispose()
	 */
	public void dispose() {
		if (fDocumentProvider != null)
			fDocumentProvider.removeElementStateListener(this);
		
		fEditorInput= null;
		fDocumentProvider= null;
		fReference= null;
		fDocumentRead= false;
	}

	/*
	 * @see org.eclipse.ui.editors.quickdiff.IQuickDiffReferenceProvider#getId()
	 */
	public String getId() {
		return fId;
	}

	/*
	 * @see org.eclipse.ui.editors.quickdiff.IQuickDiffProviderImplementation#setActiveEditor(org.eclipse.ui.texteditor.ITextEditor)
	 */
	public void setActiveEditor(ITextEditor targetEditor) {
		
		IDocumentProvider provider= null;
		IEditorInput input= null;
		if (targetEditor != null) { 
			provider= targetEditor.getDocumentProvider();
			input= targetEditor.getEditorInput();
		}
			
		// dispose if the editor input or document provider have changed
		// note that they may serve multiple editors	
		if (provider != fDocumentProvider || input != fEditorInput) {
			dispose();
			fDocumentProvider= provider;
			fEditorInput= input;
		}
	}

	/*
	 * @see org.eclipse.ui.editors.quickdiff.IQuickDiffProviderImplementation#isEnabled()
	 */
	public boolean isEnabled() {
		return fEditorInput != null && fDocumentProvider != null;
	}

	/*
	 * @see org.eclipse.ui.editors.quickdiff.IQuickDiffProviderImplementation#setId(java.lang.String)
	 */
	public void setId(String id) {
		fId= id;
	}

	/**
	 * Reads in the saved document into <code>fReference</code>.
	 */
	private void readDocument() {
		
		if (fDocumentProvider instanceof IStorageDocumentProvider && fEditorInput instanceof IFileEditorInput) {
			
			IFileEditorInput input= (IFileEditorInput) fEditorInput;
			IStorageDocumentProvider provider= (IStorageDocumentProvider) fDocumentProvider;
			
			if (fReference == null)
				fReference= new Document();
	
			// addElementStateListener adds at most once - no problem to call repeatedly
			((IDocumentProvider)provider).addElementStateListener(this);
			
			InputStream stream= getFileContents(input);
			if (stream == null)
				return;
			
			String encoding= getEncoding(input, provider);
			
			try {
				setDocumentContent(fReference, stream, encoding);
				fDocumentRead= true;
			} catch (IOException e) {
				return;
			}
			
		}
	}

	/**
	 * Gets the contents of the file referred to by <code>input</code> as an input stream.
	 * 
	 * @param input the <code>IFileEditorInput</code> which we want the content for
	 * @return an input stream for the file's content
	 */
	private InputStream getFileContents(IFileEditorInput input) {
		InputStream stream= null;
		try {
			IFile file= input.getFile();
			if (file != null)
				stream= file.getContents();
				
		} catch (CoreException e) {
		}
		return stream;
	}

	/**
	 * Returns the encoding of the file corresponding to <code>input</code>. If no encoding can
	 * be found, the default encoding as returned by <code>provider.getDefaultEncoding()</code> is
	 * returned. 
	 * 
	 * @param input the current editor input
	 * @param provider the current document provider
	 * @return the encoding for the file corresponding to <code>input</code>, or the default encoding
	 */
	private String getEncoding(IFileEditorInput input, IStorageDocumentProvider provider) {
		String encoding= provider.getEncoding(input);
		if (encoding == null)
			encoding= provider.getDefaultEncoding();
		return encoding;
	}

	/**
	 * Intitializes the given document with the given stream using the given encoding.
	 *
	 * @param document the document to be initialized
	 * @param contentStream the stream which delivers the document content
	 * @param encoding the character encoding for reading the given stream
	 * @exception CoreException if the given stream can not be read
	 */
	private static void setDocumentContent(IDocument document, InputStream contentStream, String encoding) throws IOException {
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
			
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException x) {
				}
			}
		}
	}
	
	/*
	 * @see org.eclipse.ui.texteditor.IElementStateListener#elementDirtyStateChanged(java.lang.Object, boolean)
	 */
	public void elementDirtyStateChanged(Object element, boolean isDirty) {
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
		if (element == fEditorInput) {
			// document has been reverted or replaced
			readDocument();
		}
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
}
