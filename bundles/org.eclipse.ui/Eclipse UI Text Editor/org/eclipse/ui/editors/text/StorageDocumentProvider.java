/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.ui.editors.text;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AbstractDocumentProvider;



/**
 * Shareable document provider specialized for <code>IStorage</code>s.
 */
public class StorageDocumentProvider extends AbstractDocumentProvider implements IStorageDocumentProvider {
	
	
	/**
	 * Bundle of all required information to allow <code>IStorage</code> as underlying document resources. 
	 * @since 2.0
	 */
	protected class StorageInfo extends ElementInfo {
		
		/** The flag representing the cached state whether the storage is modifiable */
		public boolean fIsModifiable= false;
		/** The flag representing the cached state whether the storage is read-only */
		public boolean fIsReadOnly= true;
		/** The flag representing the need to update the cached flag  */
		public boolean fUpdateCache= true;
		/** The encoding used to create the document from the storage or <code>null</code> for workbench encoding */
		public String fEncoding;
		
		/**
		 * Creates a new storage info.
		 * 
		 * @param document the document
		 * @param model the annotation model
		 */
		public StorageInfo(IDocument document, IAnnotationModel model) {
			super(document, model);
			fEncoding= null;
		}
	};
	
	/**
	 * Creates a new document provider.
	 * @since 2.0
	 */
	public StorageDocumentProvider() {
		super();
	}
	
	/**
	 * Intitializes the given document with the given stream.
	 *
	 * @param document the document to be initialized
	 * @param contentStream the stream which delivers the document content
	 * @exception CoreException if the given stream can not be read
	 * 
	 * @deprecated use encoding based version instead
	 */
	protected void setDocumentContent(IDocument document, InputStream contentStream) throws CoreException {
		setDocumentContent(document, contentStream, null);
	}
	
	/**
	 * Intitializes the given document with the given stream using the given encoding.
	 *
	 * @param document the document to be initialized
	 * @param contentStream the stream which delivers the document content
	 * @param encoding the character encoding for reading the given stream
	 * @exception CoreException if the given stream can not be read
	 * @since 2.0
	 */
	protected void setDocumentContent(IDocument document, InputStream contentStream, String encoding) throws CoreException {
		
		Reader in= null;
		
		try {
			
			if (encoding == null)
				encoding= getDefaultEncoding();
				
			in= new InputStreamReader(new BufferedInputStream(contentStream), encoding);
			StringBuffer buffer= new StringBuffer();
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
	 * Intitializes the given document from the given editor input using the default character encoding.
	 *
	 * @param document the document to be initialized
	 * @param editorInput the input from which to derive the content of the document
	 * @return <code>true</code> if the document content could be set, <code>false</code> otherwise
	 * @exception CoreException if the given editor input cannot be accessed
	 * 
	 * @deprecated use the encoding based version instead
	 * @since 2.0
	 */
	protected boolean setDocumentContent(IDocument document, IEditorInput editorInput) throws CoreException {
		return setDocumentContent(document, editorInput, null);
	}
	
	/**
	 * Intitializes the given document from the given editor input using the given character encoding.
	 *
	 * @param document the document to be initialized
	 * @param editorInput the input from which to derive the content of the document
	 * @param encoding the character encoding used to read the editor input
	 * @return <code>true</code> if the document content could be set, <code>false</code> otherwise
	 * @exception CoreException if the given editor input cannot be accessed
	 * @since 2.0
	 */
	protected boolean setDocumentContent(IDocument document, IEditorInput editorInput, String encoding) throws CoreException {
		if (editorInput instanceof IStorageEditorInput) {
			IStorage storage= ((IStorageEditorInput) editorInput).getStorage();
			setDocumentContent(document, storage.getContents(), encoding);
			return true;
		}
		return false;
	}
	
	/*
	 * @see AbstractDocumentProvider#createAnnotationModel(Object)
	 */
	protected IAnnotationModel createAnnotationModel(Object element) throws CoreException {
		return null;
	}
	
	/*
	 * @see AbstractDocumentProvider#createDocument(Object)
	 */
	protected IDocument createDocument(Object element) throws CoreException {
		
		if (element instanceof IEditorInput) {
			Document document= new Document();
			if (setDocumentContent(document, (IEditorInput) element, getEncoding(element)))
				return document;
		}
		
		return null;
	}
	
	/*
	 * @see AbstractDocumentProvider#createElementInfo(Object)
	 * @since 2.0
	 */
	protected ElementInfo createElementInfo(Object element) throws CoreException {
		if (element instanceof IStorageEditorInput) {
			
			IDocument document= null;
			IStatus status= null;
			
			try {
				document= createDocument(element);
			} catch (CoreException x) {
				status= x.getStatus();
				document= new Document();
			}
			
			ElementInfo info= new StorageInfo(document, createAnnotationModel(element));
			info.fStatus= status;
			
			return info;
		}
		
		return super.createElementInfo(element);
	}
	
	/*
	 * @see AbstractDocumentProvider#doSaveDocument(IProgressMonitor, Object, IDocument, boolean)
	 */
	protected void doSaveDocument(IProgressMonitor monitor, Object element, IDocument document, boolean overwrite) throws CoreException {
	}
	
	/**
	 * Defines the standard procedure to handle <code>CoreExceptions</code>. Exceptions
	 * are written to the plugin log.
	 *
	 * @param exception the exception to be logged
	 * @param message the message to be logged
	 * @since 2.0
	 */
	protected void handleCoreException(CoreException exception, String message) {
		ILog log= Platform.getPlugin(PlatformUI.PLUGIN_ID).getLog();
		
		if (message != null)
			log.log(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, message, exception));
		
		log.log(exception.getStatus());
	}
	
	/**
	 * Updates the internal cache for the given input.
	 * 
	 * @param input the input whose cache will be updated
	 * @since 2.0
	 */
	protected void updateCache(IStorageEditorInput input) throws CoreException {
		StorageInfo info= (StorageInfo) getElementInfo(input);
		if (info != null) {
			try {
				IStorage storage= input.getStorage();
				if (storage != null) {
					boolean readOnly= storage.isReadOnly();
					info.fIsReadOnly=  readOnly;
					info.fIsModifiable= !readOnly;
				}
			} catch (CoreException x) {
				handleCoreException(x, TextEditorMessages.getString("StorageDocumentProvider.updateCache")); //$NON-NLS-1$
			}
			info.fUpdateCache= false;
		}
	}
	
	/*
	 * @see IDocumentProviderExtension#isReadOnly(Object)
	 * @since 2.0
	 */
	public boolean isReadOnly(Object element) {
		if (element instanceof IStorageEditorInput) {
			StorageInfo info= (StorageInfo) getElementInfo(element);
			if (info != null) {
				if (info.fUpdateCache) {
					try {
						updateCache((IStorageEditorInput) element);
					} catch (CoreException x) {
						handleCoreException(x, TextEditorMessages.getString("StorageDocumentProvider.isReadOnly")); //$NON-NLS-1$
					}
				}
				return info.fIsReadOnly;
			}
		}
		return super.isReadOnly(element);
	}
	
	/*
	 * @see IDocumentProviderExtension#isModifiable(Object)
	 * @since 2.0
	 */
	public boolean isModifiable(Object element) {
		if (element instanceof IStorageEditorInput) {
			StorageInfo info= (StorageInfo) getElementInfo(element);
			if (info != null) {
				if (info.fUpdateCache) {
					try {
						updateCache((IStorageEditorInput) element);
					} catch (CoreException x) {
						handleCoreException(x, TextEditorMessages.getString("StorageDocumentProvider.isModifiable")); //$NON-NLS-1$
					}
				}
				return info.fIsModifiable;
			}
		}
		return super.isModifiable(element);
	}
	
	/*
	 * @see AbstractDocumentProvider#doUpdateStateCache(Object)
	 * @since 2.0
	 */
	protected void doUpdateStateCache(Object element) throws CoreException {
		if (element instanceof IStorageEditorInput) {
			StorageInfo info= (StorageInfo) getElementInfo(element);
			if (info != null)
				info.fUpdateCache= true;
		}
		super.doUpdateStateCache(element);
	}
	
	/*
	 * @see IStorageDocumentProvider#getDefaultEncoding()
	 * @since 2.0
	 */
	public String getDefaultEncoding() {
		return ResourcesPlugin.getEncoding();
	}
	
	/* 
	 * @see IStorageDocumentProvider#getEncoding(Object)
	 * @since 2.0
	 */
	public String getEncoding(Object element) {
		if (element instanceof IStorageEditorInput) {
			StorageInfo info= (StorageInfo) getElementInfo(element);
			if (info != null)
				return info.fEncoding;
		}
		return null;
	}
	
	/*
	 * @see IStorageDocumentProvider#setEncoding(Object, String)
	 * @since 2.0
	 */
	public void setEncoding(Object element, String encoding) {
		if (element instanceof IStorageEditorInput) {
			StorageInfo info= (StorageInfo) getElementInfo(element);
			if (info != null)
				info.fEncoding= encoding;
		}
	}
}