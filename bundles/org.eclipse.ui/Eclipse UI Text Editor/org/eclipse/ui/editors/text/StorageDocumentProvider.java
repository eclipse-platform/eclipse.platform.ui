package org.eclipse.ui.editors.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


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
public class StorageDocumentProvider extends AbstractDocumentProvider {
	
	
	/**
	 * Bundle of all required informations to allow <code>IStorage</code>
	 * as underlying document resources. 
	 */
	protected class StorageInfo extends ElementInfo {
		
		/** The flag representing the cached state whether the storage is modifiable */
		public boolean fIsModifiable= false;
		/** The flag representing the cached state whether the storage is read-only */
		public boolean fIsReadOnly= true;
		/** The flag representing the need to update the cached flag */
		public boolean fUpdateCache= true;
		
		public StorageInfo(IDocument document, IAnnotationModel model) {
			super(document, model);
		}
	};
	
	/**
	 * Intitializes the given document with the given stream.
	 *
	 * @param document the document to be initialized
	 * @param contentStream the stream which delivers the document content
	 * @exception CoreException if the given stream can not be read
	 */
	protected void setDocumentContent(IDocument document, InputStream contentStream) throws CoreException {
		
		Reader in= null;
		
		try {
			
			in= new InputStreamReader(new BufferedInputStream(contentStream));
//			in= new InputStreamReader(new BufferedInputStream(contentStream), ResourcesPlugin.getEncoding());
			StringBuffer buffer= new StringBuffer();
			char[] readBuffer= new char[2048];
			int n= in.read(readBuffer);
			while (n > 0) {
				buffer.append(readBuffer, 0, n);
				n= in.read(readBuffer);
			}
			
			document.set(buffer.toString());
		
		} catch (IOException x) {
			IStatus s= new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.OK, x.getMessage(), x);
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
	 * Intitializes the given document from the given editor input.
	 *
	 * @param document the document to be initialized
	 * @param editorInput the input from which to derive the content of the document
	 * @return <code>true</code> if the document content could be set, <code>false</code> otherwise
	 * @exception CoreException if the given editor input cannot be accessed
	 */
	protected boolean setDocumentContent(IDocument document, IEditorInput editorInput) throws CoreException {
		if (editorInput instanceof IStorageEditorInput) {
			IStorage storage= ((IStorageEditorInput) editorInput).getStorage();
			setDocumentContent(document, storage.getContents());
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
			if (setDocumentContent(document, (IEditorInput) element))
				return document;
		}
		
		return null;
	}
	
	/*
	 * @see AbstractDocumentProvider#createElementInfo(Object)
	 */
	protected ElementInfo createElementInfo(Object element) throws CoreException {
		if (element instanceof IStorageEditorInput)
			return new StorageInfo(createDocument(element), createAnnotationModel(element));
		return super.createElementInfo(element);
	}
	
	/*
	 * @see AbstractDocumentProvider#doSaveDocument(IProgressMonitor, Object, IDocument, boolean)
	 */
	protected void doSaveDocument(IProgressMonitor monitor, Object element, IDocument document, boolean overwrite) throws CoreException {
	}
	
	/**
	 * Defines the standard procedure to handle CoreExceptions.
	 *
	 * @param exception the exception to be logged
	 * @param message the message to be logged
	 */
	protected void handleCoreException(CoreException exception, String message) {
		ILog log= Platform.getPlugin(PlatformUI.PLUGIN_ID).getLog();
		
		if (message != null)
			log.log(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, message, null));
		
		log.log(exception.getStatus());
	}
	
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
	 */
	protected void doUpdateStateCache(Object element) throws CoreException {
		if (element instanceof IStorageEditorInput) {
			StorageInfo info= (StorageInfo) getElementInfo(element);
			if (info != null)
				info.fUpdateCache= true;
		}
		super.doUpdateStateCache(element);
	}
}