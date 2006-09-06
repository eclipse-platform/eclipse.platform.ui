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
package org.eclipse.compare.internal;

import java.io.*;

import org.eclipse.compare.ITypedElement;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.texteditor.AbstractDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * An {@link IDocumentProvider} that can be used to share the
 * document associated with an {@link ITypedElement}
 */
public class TypedElementDocumentProvider extends AbstractDocumentProvider {

	/**
	 * Default file size.
	 */
	private static final int DEFAULT_FILE_SIZE= 15 * 1024;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#createAnnotationModel(java.lang.Object)
	 */
	protected IAnnotationModel createAnnotationModel(Object element)
			throws CoreException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#createDocument(java.lang.Object)
	 */
	protected IDocument createDocument(Object element) throws CoreException {
		if (element instanceof AbstractTypedElementEditorInput) {
			IDocument document= createEmptyDocument();
			if (setDocumentContent(document, (AbstractTypedElementEditorInput) element)) {
				setupDocument(element, document);
				return document;
			}
		}

		return null;
	}

	protected void doSaveDocument(IProgressMonitor monitor, Object element,
			IDocument document, boolean overwrite) throws CoreException {
		if (element instanceof AbstractTypedElementEditorInput) {
			AbstractTypedElementEditorInput editorInput = (AbstractTypedElementEditorInput) element;
			editorInput.doSave(document, monitor);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#getOperationRunner(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IRunnableContext getOperationRunner(IProgressMonitor monitor) {
		return null;
	}
	
	private String getEncoding(Object element) {
		if (element instanceof AbstractTypedElementEditorInput) {
			AbstractTypedElementEditorInput editorInput = (AbstractTypedElementEditorInput) element;
			String encoding = editorInput.getEncoding();
			if (encoding != null)
				return encoding;
		}
		return getDefaultEncoding();
	}
	
	private void setupDocument(Object element, IDocument document) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Factory method for creating empty documents.
	 * @return the newly created document
	 */
	private IDocument createEmptyDocument() {
		return new Document();
	}
	
	/**
	 * Initializes the given document from the given editor input using the given character encoding.
	 *
	 * @param document the document to be initialized
	 * @param editorInput the input from which to derive the content of the document
	 * @return <code>true</code> if the document content could be set, <code>false</code> otherwise
	 * @throws CoreException if the given editor input cannot be accessed
	 */
	private boolean setDocumentContent(IDocument document, AbstractTypedElementEditorInput editorInput) throws CoreException {
		InputStream stream= editorInput.getContents();
		if (stream == null)
			return false;
		try {
			setDocumentContent(document, stream, getEncoding(editorInput));
		} finally {
			try {
				stream.close();
			} catch (IOException x) {
				// Ignore exception on close
			}
		}
		return true;
		
	}
	
	/**
	 * Initializes the given document with the given stream using the given encoding.
	 *
	 * @param document the document to be initialized
	 * @param contentStream the stream which delivers the document content
	 * @param encoding the character encoding for reading the given stream
	 * @throws CoreException if the given stream can not be read
	 */
	private void setDocumentContent(IDocument document, InputStream contentStream, String encoding) throws CoreException {

		Reader in= null;

		try {

			if (encoding == null)
				encoding= getDefaultEncoding();

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
			String message= (x.getMessage() != null ? x.getMessage() : ""); //$NON-NLS-1$
			IStatus s= new Status(IStatus.ERROR, CompareUIPlugin.PLUGIN_ID, IStatus.OK, message, x);
			throw new CoreException(s);
		} finally {
			try {
				if (in != null)
					in.close();
				else
					contentStream.close();
			} catch (IOException x) {
				// Ignore exceptions on close
			}
		}
	}
	
	private String getDefaultEncoding() {
		return ResourcesPlugin.getEncoding();
	}

}
