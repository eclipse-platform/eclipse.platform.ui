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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentDescription;

import org.eclipse.core.filebuffers.ITextFileBuffer;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.source.IAnnotationModel;

/**
 * @since 3.0
 */
public class JavaTextFileBuffer extends JavaFileBuffer implements ITextFileBuffer {
	
	
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
			fManager.fireDirtyStateChanged(JavaTextFileBuffer.this, fCanBeSaved);
		}
	}
	
	/**
	 * Reader chunk size.
	 */
	private static final int READER_CHUNK_SIZE= 2048;
	/**
	 * Buffer size.
	 */
	private static final int BUFFER_SIZE= 8 * READER_CHUNK_SIZE;
	/**
	 * Constant for representing the OK status. This is considered a value object.
	 */
	private static final IStatus STATUS_OK= new Status(IStatus.OK, FileBuffersPlugin.PLUGIN_ID, IStatus.OK, FileBuffersMessages.getString("FileBuffer.status.ok"), null); //$NON-NLS-1$
	/**
	 * Constant for representing the error status. This is considered a value object.
	 */
	private static final IStatus STATUS_ERROR= new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IStatus.INFO, FileBuffersMessages.getString("FileBuffer.status.error"), null); //$NON-NLS-1$
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
	/**
	 * The encoding which has explicitly been set on the file.
	 */
	private String fExplicitEncoding;
	/**
	 * Tells whether the file on disk has a BOM.
	 */
	private boolean fHasBOM;

	
	public JavaTextFileBuffer(TextFileBufferManager manager) {
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
		return null;
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
	
	private InputStream getFileContents(IProgressMonitor monitor) {
		try {
			return new FileInputStream(fFile);
		} catch (FileNotFoundException e) {
		}
		return null;
	}
	
	private void setFileContents(InputStream stream, boolean overwrite, IProgressMonitor monitor) {
		try {
			OutputStream out= new FileOutputStream(fFile, false);
			
			try {
				byte[] buffer = new byte[8192];
				while (true) {
					int bytesRead = -1;
					try {
						bytesRead = stream.read(buffer);
					} catch (IOException e) {
					}
					if (bytesRead == -1)
						break;
					try {
						out.write(buffer, 0, bytesRead);
					} catch (IOException e) {
					}
					monitor.worked(1);
				}
			} finally {
				try {
					stream.close();
				} catch (IOException e) {
				} finally {
					try {
						out.close();
					} catch (IOException e) {
					}
				}
			}		
		} catch (FileNotFoundException e) {
		}
	}
	
	/*
	 * @see org.eclipse.core.filebuffers.IFileBuffer#revert(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void revert(IProgressMonitor monitor) throws CoreException {
		if (isDisposed())
			return;
		
		IDocument original= null;
		IStatus status= null;
		
		try {
			original= fManager.createEmptyDocument(getLocation());
			setDocumentContent(original, getFileContents(monitor), fEncoding);
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
			fDocument= fManager.createEmptyDocument(getLocation());
			fEncoding= null;
			fHasBOM= false;
			
			InputStream stream= getFileContents(monitor);
			try {
				QualifiedName[] options= new QualifiedName[] { IContentDescription.CHARSET, IContentDescription.BYTE_ORDER_MARK };
				IContentDescription description= Platform.getContentTypeManager().getDescriptionFor(stream, fFile.getName(), options);
				if (description != null) {
					fEncoding= description.getCharset();
					fHasBOM= description.getProperty(IContentDescription.BYTE_ORDER_MARK) != null;
				}
			} catch (IOException e) {
				// do nothing
			} finally {
				try {
					stream.close();
				} catch (IOException ex) {
					FileBuffersPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IStatus.OK, FileBuffersMessages.getString("JavaTextFileBuffer.error.closeStream"), ex)); //$NON-NLS-1$
				}
			}
			
			setDocumentContent(fDocument, getFileContents(monitor), fEncoding);
		} catch (CoreException x) {
			fDocument= fManager.createEmptyDocument(getLocation());
			fStatus= x.getStatus();
		}
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
				setFileContents(stream, overwrite, monitor);
				// set synchronization stamp to know whether the file synchronizer must become active
				fSynchronizationStamp= fFile.lastModified();
				
				// TODO if there is an annotation model update it here
				
			} else {

//				try {
//					monitor.beginTask("Saving", 2000); //$NON-NLS-1$
//					ContainerGenerator generator = new ContainerGenerator(fFile.getWorkspace(), fFile.getParent().getFullPath());
//					generator.generateContainer(new SubProgressMonitor(monitor, 1000));
//					fFile.create(stream, false, new SubProgressMonitor(monitor, 1000));
//				}
//				finally {
//					monitor.done();
//				}

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
		
		// Use global default
		return fManager.getDefaultEncoding();
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
			 * </p>
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
	
	/**
	 * Checks whether the given file is synchronized with the the local file system. 
	 * If the file has been changed, a <code>CoreException</code> is thrown.
	 * 
	 * @param file the file to check
	 * @exception CoreException if file has been changed on the file system
	 */
	private void checkSynchronizationState() throws CoreException {
		if (!isSynchronized()) {
			Status status= new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IResourceStatus.OUT_OF_SYNC_LOCAL, FileBuffersMessages.getString("FileBuffer.error.outOfSync"), null);  //$NON-NLS-1$
			throw new CoreException(status);
		}
	}
}
