/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.editors.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.osgi.framework.Bundle;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;

import org.eclipse.core.resources.IEncodedStorage;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jface.operation.IRunnableContext;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.internal.editors.text.NLSUtility;

import org.eclipse.ui.texteditor.AbstractDocumentProvider;


/**
 * Shared document provider specialized for {@link org.eclipse.core.resources.IStorage}s.
 */
public class StorageDocumentProvider extends AbstractDocumentProvider implements IStorageDocumentProvider {

	/**
	 * Default file size.
	 *
	 * @since 2.1
	 */
	protected static final int DEFAULT_FILE_SIZE= 15 * 1024;

	/**
	 * Constant denoting an empty set of properties
	 * @since 3.1
	 */
	private static final QualifiedName[] NO_PROPERTIES= new QualifiedName[0];


	/**
	 * Bundle of all required information to allow {@link org.eclipse.core.resources.IStorage} as underlying document resources.
	 * @since 2.0
	 */
	protected class StorageInfo extends ElementInfo {

		/** The flag representing the cached state whether the storage is modifiable. */
		public boolean fIsModifiable= false;
		/** The flag representing the cached state whether the storage is read-only. */
		public boolean fIsReadOnly= true;
		/** The flag representing the need to update the cached flag.  */
		public boolean fUpdateCache= true;
		/** The encoding used to create the document from the storage or <code>null</code> for workbench encoding. */
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
	}

	/**
	 * Creates a new document provider.
	 *
	 * @since 2.0
	 */
	public StorageDocumentProvider() {
		super();
	}

	/**
	 * Initializes the given document with the given stream.
	 *
	 * @param document the document to be initialized
	 * @param contentStream the stream which delivers the document content
	 * @throws CoreException if the given stream can not be read
	 *
	 * @deprecated use encoding based version instead
	 */
	protected void setDocumentContent(IDocument document, InputStream contentStream) throws CoreException {
		setDocumentContent(document, contentStream, null);
	}

	/**
	 * Initializes the given document with the given stream using the given encoding.
	 *
	 * @param document the document to be initialized
	 * @param contentStream the stream which delivers the document content
	 * @param encoding the character encoding for reading the given stream
	 * @throws CoreException if the given stream can not be read
	 * @since 2.0
	 */
	protected void setDocumentContent(IDocument document, InputStream contentStream, String encoding) throws CoreException {

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
			IStatus s= new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.OK, message, x);
			throw new CoreException(s);
		} finally {
			try {
				if (in != null)
					in.close();
				else
					contentStream.close();
			} catch (IOException x) {
			}
		}
	}

	/**
	 * Initializes the given document from the given editor input using the default character encoding.
	 *
	 * @param document the document to be initialized
	 * @param editorInput the input from which to derive the content of the document
	 * @return <code>true</code> if the document content could be set, <code>false</code> otherwise
	 * @throws CoreException if the given editor input cannot be accessed
	 * @deprecated use the encoding based version instead
	 * @since 2.0
	 */
	protected boolean setDocumentContent(IDocument document, IEditorInput editorInput) throws CoreException {
		return setDocumentContent(document, editorInput, null);
	}

	/**
	 * Initializes the given document from the given editor input using the given character encoding.
	 *
	 * @param document the document to be initialized
	 * @param editorInput the input from which to derive the content of the document
	 * @param encoding the character encoding used to read the editor input
	 * @return <code>true</code> if the document content could be set, <code>false</code> otherwise
	 * @throws CoreException if the given editor input cannot be accessed
	 * @since 2.0
	 */
	protected boolean setDocumentContent(IDocument document, IEditorInput editorInput, String encoding) throws CoreException {
		if (editorInput instanceof IStorageEditorInput) {
			IStorage storage= ((IStorageEditorInput) editorInput).getStorage();
			InputStream stream= storage.getContents();
			try {
				setDocumentContent(document, stream, encoding);
			} finally {
				try {
					stream.close();
				} catch (IOException x) {
				}
			}
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

	/**
	 * Factory method for creating empty documents.
	 * @return the newly created document
	 * @since 2.1
	 */
	protected IDocument createEmptyDocument() {
		return new Document();
	}

	/*
	 * @see AbstractDocumentProvider#createDocument(Object)
	 */
	protected IDocument createDocument(Object element) throws CoreException {

		if (element instanceof IEditorInput) {
			IDocument document= createEmptyDocument();
			if (setDocumentContent(document, (IEditorInput) element, getEncoding(element))) {
				setupDocument(element, document);
				return document;
			}
		}

		return null;
	}

	/**
	 * Sets up the given document as it would be provided for the given element. The
	 * content of the document is not changed. This default implementation is empty.
	 * Subclasses may reimplement.
	 *
	 * @param element the blue-print element
	 * @param document the document to set up
	 * @since 3.0
	 */
	protected void setupDocument(Object element, IDocument document) {
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
				document= createEmptyDocument();
			}

			ElementInfo info= new StorageInfo(document, createAnnotationModel(element));
			info.fStatus= status;
			((StorageInfo)info).fEncoding= getPersistedEncoding(element);

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
	 * are written to the plug-in log.
	 *
	 * @param exception the exception to be logged
	 * @param message the message to be logged
	 * @since 2.0
	 */
	protected void handleCoreException(CoreException exception, String message) {

		Bundle bundle = Platform.getBundle(PlatformUI.PLUGIN_ID);
		ILog log= Platform.getLog(bundle);

		if (message != null)
			log.log(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.OK, message, exception));
		else
			log.log(exception.getStatus());
	}

	/**
	 * Updates the internal cache for the given input.
	 *
	 * @param input the input whose cache will be updated
	 * @throws CoreException if the storage cannot be retrieved from the input
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
				handleCoreException(x, TextEditorMessages.StorageDocumentProvider_updateCache);
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
						handleCoreException(x, TextEditorMessages.StorageDocumentProvider_isReadOnly);
					}
				}
				return info.fIsReadOnly;
			}
		}
		return super.isReadOnly(element);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProviderExtension5#isNotSynchronizedException(Object, CoreException)
	 * @since 3.2
	 */
	public boolean isNotSynchronizedException(Object element, CoreException ex) {
		IStatus status= ex.getStatus();
		if (status == null || status instanceof MultiStatus)
			return false;

		if (status.getException() != null)
			return false;

		return status.getCode() == IResourceStatus.OUT_OF_SYNC_LOCAL;
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
						handleCoreException(x, TextEditorMessages.StorageDocumentProvider_isModifiable);
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
			return getPersistedEncoding(element);
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
			if (info != null) {
				info.fEncoding= encoding;
				try {
					persistEncoding(element, encoding);
				} catch (CoreException ex) {
					EditorsPlugin.log(ex.getStatus());
				}
			}
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.IDocumentProviderExtension4#getContentType(java.lang.Object)
	 * @since 3.1
	 */
	public IContentType getContentType(Object element) throws CoreException {
		if (element instanceof IStorageEditorInput) {
			IStorage storage= ((IStorageEditorInput) element).getStorage();
			Reader reader= null;
			InputStream stream= null;
			try {
				IContentDescription desc;
				IDocument document= getDocument(element);
				if (document != null) {
					reader= new DocumentReader(document);
					desc= Platform.getContentTypeManager().getDescriptionFor(reader, storage.getName(), NO_PROPERTIES);
				} else {
					stream= storage.getContents();
					desc= Platform.getContentTypeManager().getDescriptionFor(stream, storage.getName(), NO_PROPERTIES);
				}
				if (desc != null && desc.getContentType() != null)
					return desc.getContentType();
			} catch (IOException x) {
				IPath path= storage.getFullPath();
				String name;
				if (path != null)
					name= path.toOSString();
				else
					name= storage.getName();
				String message;
				if (name != null)
					message= NLSUtility.format(TextEditorMessages.StorageDocumentProvider_getContentDescriptionFor, name);
				else
					message= TextEditorMessages.StorageDocumentProvider_getContentDescription;
				throw new CoreException(new Status(IStatus.ERROR, EditorsUI.PLUGIN_ID, IStatus.OK, message, x));
			} finally {
				try {
					// Note: either 'reader' or 'stream' is null
					if (reader != null)
						reader.close();
					if (stream != null)
						stream.close();
				} catch (IOException x) {
				}
			}
		}
		return super.getContentType(element);
	}

	/**
	 * Returns the persisted encoding for the given element.
	 *
	 * @param element the element for which to get the persisted encoding
	 * @return the persisted encoding
	 * @since 2.1
	 */
	protected String getPersistedEncoding(Object element) {
		if (element instanceof IStorageEditorInput) {
			IStorage storage;
			try {
				storage= ((IStorageEditorInput)element).getStorage();
				if (storage instanceof IEncodedStorage)
					return ((IEncodedStorage)storage).getCharset();
			} catch (CoreException e) {
				return null;
			}
		}
		return null;
	}

	/**
	 * Persists the given encoding for the given element.
	 *
	 * @param element the element for which to store the persisted encoding
	 * @param encoding the encoding
	 * @throws CoreException if the operation fails
	 * @since 2.1
	 */
	protected void persistEncoding(Object element, String encoding) throws CoreException {
		// Default is to do nothing
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#getOperationRunner(org.eclipse.core.runtime.IProgressMonitor)
	 * @since 3.0
	 */
	protected IRunnableContext getOperationRunner(IProgressMonitor monitor) {
		return null;
	}
}
