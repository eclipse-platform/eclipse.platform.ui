/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.content.IContentDescription;

import org.eclipse.core.filebuffers.IPersistableAnnotationModel;
import org.eclipse.core.filebuffers.ITextFileBuffer;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.source.IAnnotationModel;

/**
 * @since 3.0
 */
public class ResourceTextFileBuffer extends ResourceFileBuffer implements ITextFileBuffer {
	
	
	private class DocumentListener implements IDocumentListener {

		/*
		 * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
		 */
		public void documentAboutToBeChanged(DocumentEvent event) {
		}

		/*
		 * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
		 */
		public void documentChanged(DocumentEvent event) {
			fCanBeSaved= true;
			removeFileBufferContentListeners();
			fManager.fireDirtyStateChanged(ResourceTextFileBuffer.this, fCanBeSaved);
		}
	}
	
	/**
	 * Reader chunk size.
	 */
	static final private int READER_CHUNK_SIZE= 2048;
	/**
	 * Buffer size.
	 */
	static final private int BUFFER_SIZE= 8 * READER_CHUNK_SIZE;
	/**
	 * Qualified name for the encoding key.
	 */
	static final private QualifiedName ENCODING_KEY= new QualifiedName(FileBuffersPlugin.PLUGIN_ID, "encoding");  //$NON-NLS-1$
	/**
	 * Constant for representing the OK status. This is considered a value object.
	 */
	static final private IStatus STATUS_OK= new Status(IStatus.OK, FileBuffersPlugin.PLUGIN_ID, IStatus.OK, FileBuffersMessages.getString("FileBuffer.status.ok"), null); //$NON-NLS-1$
	/**
	 * Constant for representing the error status. This is considered a value object.
	 */
	static final private IStatus STATUS_ERROR= new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IStatus.INFO, FileBuffersMessages.getString("FileBuffer.status.error"), null); //$NON-NLS-1$
	/** 
	 * Constant denoting UTF-8 encoding.
	 */
	private static final String CHARSET_UTF_8= "UTF-8"; //$NON-NLS-1$
	
	
	/** The element's document */
	protected IDocument fDocument;
	/** The encoding used to create the document from the storage or <code>null</code> for workbench encoding. */
	protected String fEncoding;
	/** Internal document listener */
	protected IDocumentListener fDocumentListener= new DocumentListener();
	/** The element's annotation model */
	protected IAnnotationModel fAnnotationModel;
	/** The encoding which has explicitly been set on the file. */
	private String fExplicitEncoding;
	/** Tells whether the file on disk has a BOM. */
	private boolean fHasBOM;


	public ResourceTextFileBuffer(TextFileBufferManager manager) {
		super(manager);
	}

	/*
	 * @see org.eclipse.core.filebuffers.ITextFileBuffer#getDocument()
	 */
	public IDocument getDocument() {
		return fDocument;
	}
	
	/*
	 * @see org.eclipse.core.filebuffers.ITextFileBuffer#getAnnotationModel()
	 */
	public IAnnotationModel getAnnotationModel() {
		return fAnnotationModel;
	}

	/*
	 * @see org.eclipse.core.filebuffers.ITextFileBuffer#getEncoding()
	 */
	public String getEncoding() {
		return fEncoding;
	}

	/*
	 * @see org.eclipse.core.filebuffers.ITextFileBuffer#setEncoding(java.lang.String)
	 */
	public void setEncoding(String encoding) {
		fEncoding= encoding;
		fExplicitEncoding= encoding;
		fHasBOM= false;
		try {
			fFile.setCharset(encoding, null);
			if (encoding == null)
				fEncoding= fFile.getCharset();
			setHasBOM();
		} catch (CoreException x) {
			handleCoreException(x);
		}
	}
	
	/*
	 * @see org.eclipse.core.filebuffers.IFileBuffer#getStatus()
	 */
	public IStatus getStatus() {
		if (!isDisposed()) {
			if (fStatus != null)
				return fStatus;
			return (fDocument == null ? STATUS_ERROR : STATUS_OK);
		}
		return STATUS_ERROR;	
	}
	
	/*
	 * @see org.eclipse.core.internal.filebuffers.FileBuffer#addFileBufferContentListeners()
	 */
	protected void addFileBufferContentListeners() {
		if (fDocument != null)
			fDocument.addDocumentListener(fDocumentListener);
	}
	
	/*
	 * @see org.eclipse.core.internal.filebuffers.FileBuffer#removeFileBufferContentListeners()
	 */
	protected void removeFileBufferContentListeners() {
		if (fDocument != null)
			fDocument.removeDocumentListener(fDocumentListener);
	}
	
	/*
	 * @see org.eclipse.core.internal.filebuffers.FileBuffer#initializeFileBufferContent(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void initializeFileBufferContent(IProgressMonitor monitor) throws CoreException {		
		try {
			fEncoding= null;
			fExplicitEncoding= null;
			try {
				fEncoding= fFile.getPersistentProperty(ENCODING_KEY);
			} catch (CoreException x) {
				// we ignore exceptions here because we support the ENCODING_KEY property only for compatibility reasons
			}
			if (fEncoding != null) {
				// if we found an old encoding property, we try to migrate it to the new core.resources encoding support
				try {
					fExplicitEncoding= fEncoding;
					fFile.setCharset(fEncoding, monitor);
					// if successful delete old property
					fFile.setPersistentProperty(ENCODING_KEY, null);
				} catch (CoreException ex) {
					// log problem because we could not migrate the property successfully
					handleCoreException(ex);
				}
				setHasBOM();
			} else {
				cacheEncodingState();
			}
			
			
			fDocument= fManager.createEmptyDocument(getLocation());
			setDocumentContent(fDocument, fFile.getContents(), fEncoding);
			
			fAnnotationModel= fManager.createAnnotationModel(getLocation());
						
		} catch (CoreException x) {
			fDocument= fManager.createEmptyDocument(getLocation());
			fStatus= x.getStatus();
		}
	}

	/**
	 * Sets whether the underlying file has a BOM.
	 * 
	 * @throws CoreException if reading of file's content description fails
	 */
	protected void setHasBOM() throws CoreException {
		fHasBOM= false;
		IContentDescription description= fFile.getContentDescription();
		fHasBOM= description != null && description.getProperty(IContentDescription.BYTE_ORDER_MARK) != null;
	}
	
	/*
	 * @see org.eclipse.core.internal.filebuffers.ResourceFileBuffer#connected()
	 */
	protected void connected() {
		super.connected();
		if (fAnnotationModel != null)
			fAnnotationModel.connect(fDocument);
	}
	
	/*
	 * @see org.eclipse.core.internal.filebuffers.ResourceFileBuffer#disconnected()
	 */
	protected void disconnected() {
		if (fAnnotationModel != null)
			fAnnotationModel.disconnect(fDocument);
		super.disconnected();
	}
	
	/*
	 * @see org.eclipse.core.internal.filebuffers.FileBuffer#commitFileBufferContent(org.eclipse.core.runtime.IProgressMonitor, boolean)
	 */
	protected void commitFileBufferContent(IProgressMonitor monitor, boolean overwrite) throws CoreException {
		String encoding= computeEncoding();
		try {
			
			byte[] bytes= fDocument.get().getBytes(encoding);
			
			/*
			 * XXX:
			 * This is a workaround for a corresponding bug in Java readers and writer,
			 * see: http://developer.java.sun.com/developer/bugParade/bugs/4508058.html
			 */
			if (fHasBOM && CHARSET_UTF_8.equals(encoding)) {
				int bomLength= IContentDescription.BOM_UTF_8.length;
				byte[] bytesWithBOM= new byte[bytes.length + bomLength];
				System.arraycopy(IContentDescription.BOM_UTF_8, 0, bytesWithBOM, 0, bomLength);
				System.arraycopy(bytes, 0, bytesWithBOM, bomLength, bytes.length);
				bytes= bytesWithBOM;
			}
			
			InputStream stream= new ByteArrayInputStream(bytes);
			if (fFile.exists()) {
								
				if (!overwrite)
					checkSynchronizationState();
							
					
				// here the file synchronizer should actually be removed and afterwards added again. However,
				// we are already inside an operation, so the delta is sent AFTER we have added the listener
				fFile.setContents(stream, overwrite, true, monitor);
				// set synchronization stamp to know whether the file synchronizer must become active
				fSynchronizationStamp= fFile.getModificationStamp();
				
				if (fAnnotationModel instanceof IPersistableAnnotationModel) {
					IPersistableAnnotationModel persistableModel= (IPersistableAnnotationModel) fAnnotationModel;
					persistableModel.commit(fDocument);
				}
				
			} else {

				monitor= Progress.getMonitor(monitor);
				try {
					monitor.beginTask("Saving", 2); //$NON-NLS-1$
					ContainerGenerator generator = new ContainerGenerator(fFile.getWorkspace(), fFile.getParent().getFullPath());
					IProgressMonitor subMonitor= new SubProgressMonitor(monitor, 1);
					generator.generateContainer(subMonitor);
					subMonitor.done();
					
					subMonitor= new SubProgressMonitor(monitor, 1);
					fFile.create(stream, false, subMonitor);
					subMonitor.done();
					
				} finally {
					monitor.done();
				}
				
				// set synchronization stamp to know whether the file synchronizer must become active
				fSynchronizationStamp= fFile.getModificationStamp();
				
				// TODO commit persistable annotation model
			}
			
		} catch (UnsupportedEncodingException x) {
			String message= FileBuffersMessages.getFormattedString("ResourceTextFileBuffer.error.unsupported_encoding.message_arg", encoding); //$NON-NLS-1$
			IStatus s= new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IStatus.OK, message, x);
			throw new CoreException(s);
		}	
	}
	
	private String computeEncoding() {
		// User-defined encoding has first priority
		if (fExplicitEncoding != null)
			return fExplicitEncoding;
		
		// Probe content
		Reader reader= new BufferedReader(new StringReader(fDocument.get()));
		try {
			QualifiedName[] options= new QualifiedName[] { IContentDescription.CHARSET, IContentDescription.BYTE_ORDER_MARK };
			IContentDescription description= Platform.getContentTypeManager().getDescriptionFor(reader, fFile.getName(), options);
			if (description != null) {
				String encoding= description.getCharset();
				if (encoding != null)
					return encoding;
			} 
		} catch (IOException ex) {
			// try next strategy
		} finally {
			try {
				reader.close();
			} catch (IOException ex) {
				FileBuffersPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IStatus.OK, FileBuffersMessages.getString("ResourceTextFileBuffer.error.closeReader"), ex)); //$NON-NLS-1$
			}
		}
		
		// Use file's encoding if the file has a BOM
		if (fHasBOM)
			return fEncoding;

		// Use parent chain
		try {
			return fFile.getParent().getDefaultCharset();
		} catch (CoreException ex) {
			// Use global default
			return fManager.getDefaultEncoding();
		}
	}
	
	/**
	 * Internally caches the text resource's encoding.
	 * 
	 * @throws CoreException if the encoding cannot be retrieved from the resource
	 * @since 3.1
	 */
	protected void cacheEncodingState() throws CoreException {
		fExplicitEncoding= fFile.getCharset(false);
		if (fExplicitEncoding != null)
			fEncoding= fExplicitEncoding;
		else
			fEncoding= fFile.getCharset();
		setHasBOM();
	}
	
	/*
	 * @see org.eclipse.core.internal.filebuffers.ResourceFileBuffer#handleFileContentChanged()
	 */
	protected void handleFileContentChanged(boolean revert) throws CoreException {
		
		IDocument document= fManager.createEmptyDocument(fFile.getLocation());
		IStatus status= null;
		
		try {
			cacheEncodingState();
			setDocumentContent(document, fFile.getContents(false), fEncoding);
		} catch (CoreException x) {
			status= x.getStatus();
		}
		
		String newContent= document.get();
		boolean replaceContent= !newContent.equals(fDocument.get());
			
		if (replaceContent)
			fManager.fireBufferContentAboutToBeReplaced(this);
		
		removeFileBufferContentListeners();
		if (replaceContent)
			fDocument.set(newContent);
		fCanBeSaved= false;
		fSynchronizationStamp= fFile.getModificationStamp();
		fStatus= status;
		addFileBufferContentListeners();
		
		if (replaceContent)
			fManager.fireBufferContentReplaced(this);			
		
		if (fAnnotationModel instanceof IPersistableAnnotationModel) {
			IPersistableAnnotationModel persistableModel= (IPersistableAnnotationModel) fAnnotationModel;
			try {
				if (revert)
					persistableModel.revert(fDocument);
				else
					persistableModel.reinitialize(fDocument);
			} catch (CoreException x) {
				fStatus= x.getStatus();
			}
		}
		
		fManager.fireDirtyStateChanged(this, fCanBeSaved);
	}
	
	/**
	 * Initializes the given document with the given stream using the given encoding.
	 *
	 * @param document the document to be initialized
	 * @param contentStream the stream which delivers the document content
	 * @param encoding the character encoding for reading the given stream
	 * @exception CoreException if the given stream can not be read
	 */
	private void setDocumentContent(IDocument document, InputStream contentStream, String encoding) throws CoreException {
		Reader in= null;
		try {
			
			if (encoding == null)
				encoding= fManager.getDefaultEncoding();

			/*
			 * XXX:
			 * This is a workaround for a corresponding bug in Java readers and writer,
			 * see: http://developer.java.sun.com/developer/bugParade/bugs/4508058.html
			 */
			if (fHasBOM && CHARSET_UTF_8.equals(encoding))
				contentStream.read(new byte[IContentDescription.BOM_UTF_8.length]);
			
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
			String message= (x.getMessage() != null ? x.getMessage() : ""); //$NON-NLS-1$
			IStatus s= new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IStatus.OK, message, x);
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
	 * Checks whether the given file is synchronized with the the local file system. 
	 * If the file has been changed, a <code>CoreException</code> is thrown.
	 * 
	 * @exception CoreException if file has been changed on the file system
	 */
	private void checkSynchronizationState() throws CoreException {
		if (!fFile.isSynchronized(IResource.DEPTH_ZERO)) {
			Status status= new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IResourceStatus.OUT_OF_SYNC_LOCAL, FileBuffersMessages.getString("FileBuffer.error.outOfSync"), null);  //$NON-NLS-1$
			throw new CoreException(status);
		}
	}
}
