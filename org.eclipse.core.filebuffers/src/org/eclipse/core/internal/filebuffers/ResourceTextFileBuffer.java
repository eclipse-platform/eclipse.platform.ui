/**********************************************************************
Copyright (c) 2000, 2003 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.core.internal.filebuffers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.resources.IFile;
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
	/**
	 * The encoding which has explicitly been set on the file.
	 */
	private String fExplicitEncoding;
	/**
	 * BOM if encoding is UTF-8.
	 * <p>
	 * XXX:
	 * This is a workaround for a corresponding bug in Java readers and writer,
	 * see: http://developer.java.sun.com/developer/bugParade/bugs/4508058.html
	 * </p>
	 */
	private byte[] fUTF8BOM;


	public ResourceTextFileBuffer(TextFileBufferManager manager) {
		super(manager);
	}

	/*
	 * @see org.eclipse.core.buffer.text.IBufferedTextFile#getDocument()
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
	 * @see org.eclipse.core.buffer.text.IBufferedTextFile#getEncoding()
	 */
	public String getEncoding() {
		return fEncoding;
	}

	/*
	 * @see org.eclipse.core.buffer.text.IBufferedTextFile#setEncoding(java.lang.String)
	 */
	public void setEncoding(String encoding) {
		fEncoding= encoding;
		fExplicitEncoding= encoding;
		try {
			fFile.setCharset(encoding);
			if (encoding == null)
				fEncoding= fFile.getCharset();
			readUTF8BOM();
		} catch (CoreException x) {
			handleCoreException(x);
		}
	}
	
	/*
	 * @see org.eclipse.core.buffer.text.IBufferedFile#getStatus()
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
	 * @see org.eclipse.core.filebuffers.IFileBuffer#revert(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void revert(IProgressMonitor monitor) throws CoreException {
		if (isDisposed())
			return;
		
		refreshFile(monitor);
				
		IDocument original= null;
		IStatus status= null;
		
		try {
			original= fManager.createEmptyDocument(fFile.getLocation());
			setDocumentContent(original, fFile.getContents(), fEncoding);
		} catch (CoreException x) {
			status= x.getStatus();
		}
			
		fStatus= status;			
			
		if (original != null) {
			
			String originalContents= original.get();
			boolean replaceContents= !originalContents.equals(fDocument.get());
			
			if (replaceContents)  {
				fManager.fireBufferContentAboutToBeReplaced(this);
				fDocument.set(original.get());
			}
			
			if (fCanBeSaved) {
				fCanBeSaved= false;
				addFileBufferContentListeners();
			}
			
			if (replaceContents)
				fManager.fireBufferContentReplaced(this);
			
			if (fAnnotationModel instanceof IPersistableAnnotationModel) {
				IPersistableAnnotationModel persistableModel= (IPersistableAnnotationModel) fAnnotationModel;
				persistableModel.revert(fDocument);
			}
			
			fManager.fireDirtyStateChanged(this, fCanBeSaved);
		}
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
					fFile.setCharset(fEncoding);
					// if successful delete old property
					fFile.setPersistentProperty(ENCODING_KEY, null);
				} catch (CoreException ex) {
					// log problem because we could not migrate the property successfully
					handleCoreException(ex);
				}
			} else {
				fExplicitEncoding= fFile.getCharset(false);
				if (fExplicitEncoding != null)
					fEncoding= fExplicitEncoding;
				else
					fEncoding= fFile.getCharset();
			}
			
			readUTF8BOM();
			
			fDocument= fManager.createEmptyDocument(fFile.getLocation());
			setDocumentContent(fDocument, fFile.getContents(), fEncoding);
			
			fAnnotationModel= fManager.createAnnotationModel(fFile.getLocation());
						
		} catch (CoreException x) {
			fDocument= fManager.createEmptyDocument(fFile.getLocation());
			fStatus= x.getStatus();
		}
	}
	
	/**
	 * Reads the file's UTF-8 BOM if any and stores it.
	 * <p>
	 * XXX:
	 * This is a workaround for a corresponding bug in Java readers and writer,
	 * see: http://developer.java.sun.com/developer/bugParade/bugs/4508058.html
	 * </p>
	 * @throws CoreException if
	 * 			- reading of file's content description fails
	 * 			- byte order mark is not valid for UTF-8
	 */
	protected void readUTF8BOM() throws CoreException {
		if (CHARSET_UTF_8.equals(fEncoding)) {
			IContentDescription description= fFile.getContentDescription();
			if (description != null) {
				fUTF8BOM= (byte[]) description.getProperty(IContentDescription.BYTE_ORDER_MARK);
				if (fUTF8BOM != null && fUTF8BOM != IContentDescription.BOM_UTF_8)
					throw new CoreException(new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IStatus.OK, FileBuffersMessages.getString("FileBuffer.error.wrongByteOrderMark"), null)); //$NON-NLS-1$
			}
		}
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
			if (fUTF8BOM != null && CHARSET_UTF_8.equals(encoding)) {
				byte[] bytesWithBOM= new byte[bytes.length + 3];
				System.arraycopy(fUTF8BOM, 0, bytesWithBOM, 0, 3);
				System.arraycopy(bytes, 0, bytesWithBOM, 3, bytes.length);
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

				try {
					monitor.beginTask("Saving", 2000); //$NON-NLS-1$
					ContainerGenerator generator = new ContainerGenerator(fFile.getWorkspace(), fFile.getParent().getFullPath());
					generator.generateContainer(new SubProgressMonitor(monitor, 1000));
					fFile.create(stream, false, new SubProgressMonitor(monitor, 1000));
				}
				finally {
					monitor.done();
				}
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
		try {
		/*
		 * FIXME
		 * Check whether explicit encoding has been set via properties dialog.
		 * This is needed because no notification is sent when this property
		 * changes, see: https://bugs.eclipse.org/bugs/show_bug.cgi?id=64077
		 */ 
		fExplicitEncoding= fFile.getCharset(false);
		if (fExplicitEncoding != null)
			return fExplicitEncoding;
		} catch (CoreException e) {
		}
		
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
		
		// Use UTF-8 BOM if there was any
		if (fUTF8BOM != null)
			return CHARSET_UTF_8;

		// Use parent chain
		try {
			return fFile.getParent().getDefaultCharset();
		} catch (CoreException ex) {
			// Use global default
			return fManager.getDefaultEncoding();
		}
	}
	
	/**
	 * Updates the element info to a change of the file content and sends out appropriate notifications.
	 */
	protected void handleFileContentChanged() {
		if (isDisposed())
			return;
		
		IDocument document= fManager.createEmptyDocument(fFile.getLocation());
		IStatus status= null;
		
		try {
			setDocumentContent(document, fFile.getContents(false), fEncoding);
		} catch (CoreException x) {
			status= x.getStatus();
		}
		
		String newContent= document.get();
		
		if ( !newContent.equals(fDocument.get())) {
			
			fManager.fireBufferContentAboutToBeReplaced(this);
			
			removeFileBufferContentListeners();
			fDocument.set(newContent);
			fCanBeSaved= false;
			fSynchronizationStamp= fFile.getModificationStamp();
			fStatus= status;
			addFileBufferContentListeners();
			
			fManager.fireBufferContentReplaced(this);
			
			if (fAnnotationModel instanceof IPersistableAnnotationModel) {
				IPersistableAnnotationModel persistableModel= (IPersistableAnnotationModel) fAnnotationModel;
				try {
					persistableModel.reinitialize(fDocument);
				} catch (CoreException x) {
					fStatus= status;
				}
			}
			
		} else {
			
			removeFileBufferContentListeners();
			fCanBeSaved= false;
			fSynchronizationStamp= fFile.getModificationStamp();
			fStatus= status;
			addFileBufferContentListeners();
			
			fManager.fireDirtyStateChanged(this, fCanBeSaved);
		}
	}
	
	/**
	 * Intitializes the given document with the given stream using the given encoding.
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
			if (fUTF8BOM != null && CHARSET_UTF_8.equals(encoding)) {
				contentStream.read();
				contentStream.read();
				contentStream.read();
			}
			
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
	 * @param file the file to check
	 * @exception CoreException if file has been changed on the file system
	 */
	private void checkSynchronizationState() throws CoreException {
		if (!fFile.isSynchronized(IFile.DEPTH_ZERO)) {
			Status status= new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IResourceStatus.OUT_OF_SYNC_LOCAL, FileBuffersMessages.getString("FileBuffer.error.outOfSync"), null);  //$NON-NLS-1$
			throw new CoreException(status);
		}
	}
}
