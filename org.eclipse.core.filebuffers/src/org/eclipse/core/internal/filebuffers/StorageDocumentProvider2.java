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

package org.eclipse.core.internal.filebuffers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;


/**
 * Shareable document provider specialized for <code>IStorage</code>s.
 */
public class StorageDocumentProvider2 extends AbstractDocumentProvider2 implements IStorageDocumentProvider2 {
	
	/**
	 * Reader chunk size.
	 */
	protected final static int READER_CHUNK_SIZE= 2048;

	/**
	 * Buffer size.
	 */
	protected final static int BUFFER_SIZE= 8 * READER_CHUNK_SIZE;
	
	
	/**
	 * Bundle of all required information to allow <code>IStorage</code> as underlying document resources. 
	 */
	protected class StorageInfo extends ElementInfo {
		
		/** The encoding used to create the document from the storage or <code>null</code> for workbench encoding. */
		public String fEncoding= null;
		
		/**
		 * Creates a new storage info.
		 * 
		 * @param document the document
		 */
		public StorageInfo(IDocument document) {
			super(document);
		}
	};
	
	/**
	 * Creates a new document provider.
	 */
	public StorageDocumentProvider2() {
		super();
	}
	
	/**
	 * Intitializes the given document with the given stream using the given encoding.
	 *
	 * @param document the document to be initialized
	 * @param contentStream the stream which delivers the document content
	 * @param encoding the character encoding for reading the given stream
	 * @exception CoreException if the given stream can not be read
	 */
	protected void setDocumentContent(IDocument document, InputStream contentStream, String encoding) throws CoreException {
		
		Reader in= null;
		
		try {
			
			if (encoding == null)
				encoding= getDefaultEncoding();
				
			in= new BufferedReader(new InputStreamReader(contentStream, encoding), BUFFER_SIZE);
			StringBuffer buffer= new StringBuffer(BUFFER_SIZE);
			char[] readBuffer= new char[READER_CHUNK_SIZE];
			int n= in.read(readBuffer);
			while (n > 0) {
				buffer.append(readBuffer, 0, n);
				n= in.read(readBuffer);
			}
			
			document.set(buffer.toString());
		
		} catch (IOException x) {
			String msg= x.getMessage() == null ? "" : x.getMessage(); //$NON-NLS-1$
			IStatus s= new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IStatus.OK, msg, x);
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
	
	/*
	 * @see org.eclipse.core.internal.filebuffers.IDocumentProvider2#createEmptyDocument(java.lang.Object)
	 */
	public IDocument createEmptyDocument(Object element) {
		return new Document();
	}
		
	/*
	 * @see AbstractDocumentProvider#createDocument(Object)
	 */
	protected IDocument createDocument(Object element) throws CoreException {
		
		if (element instanceof IStorage) {
			IStorage storage= (IStorage) element;
			IDocument document= createEmptyDocument(element);
			setDocumentContent(document, storage.getContents(), getEncoding(element));
			return document;
		}
		
		return null;
	}
	
	/*
	 * @see AbstractDocumentProvider#createElementInfo(Object)
	 */
	protected ElementInfo createElementInfo(Object element) throws CoreException {
		if (element instanceof IStorage) {
			
			IDocument document= null;
			IStatus status= null;
			
			try {
				document= createDocument(element);
			} catch (CoreException x) {
				status= x.getStatus();
				document= createEmptyDocument(element);
			}
			
			StorageInfo info= new StorageInfo(document);
			info.fStatus= status;
			info.fEncoding= getPersistedEncoding(element);
			
			return info;
		}
		
		return super.createElementInfo(element);
	}
	
	/*
	 * @see AbstractDocumentProvider#doSaveDocument(IProgressMonitor, Object, IDocument, boolean)
	 */
	protected void doSaveDocument(Object element, IDocument document, boolean overwrite) throws CoreException {
	}
	
	/**
	 * Defines the standard procedure to handle <code>CoreExceptions</code>. Exceptions
	 * are written to the plug-in log.
	 *
	 * @param exception the exception to be logged
	 * @param message the message to be logged
	 * @since 2.0
	 */
	protected void handleCoreException(CoreException exception, String message) {
		ILog log= Platform.getPlugin(FileBuffersPlugin.PLUGIN_ID).getLog();
		
		if (message != null)
			log.log(new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, 0, message, exception));
		else
			log.log(exception.getStatus());
	}
	
	/*
	 * @see IStorageDocumentProvider#getDefaultEncoding()
	 */
	public String getDefaultEncoding() {
		return ResourcesPlugin.getEncoding();
	}
	
	/*
	 * @see IStorageDocumentProvider#getEncoding(Object)
	 */
	public String getEncoding(Object element) {
		if (element instanceof IStorage) {
			StorageInfo info= (StorageInfo) getElementInfo(element);
			if (info != null)
				return info.fEncoding;
			else
				return getPersistedEncoding(element);
		}
		return null;
	}
	
	/*
	 * @see IStorageDocumentProvider#setEncoding(Object, String)
	 */
	public void setEncoding(Object element, String encoding) {
		if (element instanceof IStorage) {
			StorageInfo info= (StorageInfo) getElementInfo(element);
			if (info != null) {
				info.fEncoding= encoding;
				try {
					persistEncoding(element, encoding);
				} catch (CoreException x) {
				}
			}
		}
	}

	/**
	 * Returns the persited encoding for the given element.
	 * 
	 * @param element the element for which to get the persisted encoding
	 */
	protected String getPersistedEncoding(Object element) {
		return null;
	}

	/**
	 * Persists the given encoding for the given element.
	 * 
	 * @param element the element for which to store the persisted encoding
	 * @param encoding the encoding
	 */
	protected void persistEncoding(Object element, String encoding) throws CoreException {
	}
}
