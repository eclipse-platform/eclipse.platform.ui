/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import java.io.SequenceInputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnmappableCharacterException;
import java.nio.charset.UnsupportedCharsetException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;

import org.eclipse.core.resources.IFile;

import org.eclipse.core.filebuffers.IFileBufferStatusCodes;
import org.eclipse.core.filebuffers.IPersistableAnnotationModel;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.manipulation.ContainerCreator;

import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
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
			if (fCanBeSaved && fSynchronizationStamp == event.getModificationStamp()) {
				fCanBeSaved= false;
				fManager.fireDirtyStateChanged(ResourceTextFileBuffer.this, fCanBeSaved);
			} else if (!fCanBeSaved) {
				fCanBeSaved= true;
				fManager.fireDirtyStateChanged(ResourceTextFileBuffer.this, fCanBeSaved);
			}
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
	 * Constant for representing the error status. This is considered a value object.
	 */
	static final private IStatus STATUS_ERROR= new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IStatus.OK, FileBuffersMessages.FileBuffer_status_error, null);
	/**
	 * Constant denoting UTF-8 encoding.
	 */
	private static final String CHARSET_UTF_8= "UTF-8"; //$NON-NLS-1$
	/**
	 * Constant denoting UTF-16 encoding.
	 * @since 3.4
	 */
	private static final String CHARSET_UTF_16= "UTF-16"; //$NON-NLS-1$
	/**
	 * Constant denoting UTF-16LE encoding.
	 * @since 3.4
	 */
	private static final String CHARSET_UTF_16LE= "UTF-16LE"; //$NON-NLS-1$

	/**
	 * Constant denoting an empty set of properties
	 * @since 3.1
	 */
	private static final QualifiedName[] NO_PROPERTIES= new QualifiedName[0];


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
	/** The BOM that needs to get written. */
	private byte[] fBOM;
	/**
	 * Lock for lazy creation of annotation model.
	 * @since 3.2
	 */
	private final Object fAnnotationModelCreationLock= new Object();


	public ResourceTextFileBuffer(ResourceTextFileBufferManager manager) {
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
		synchronized (fAnnotationModelCreationLock) {
			if (fAnnotationModel == null && !isDisconnected()) {
				fAnnotationModel= getManager().createAnnotationModel(fFile);
				if (fAnnotationModel != null)
					fAnnotationModel.connect(fDocument);
			}
		}
		return fAnnotationModel;
	}

	/**
	 * Returns the file buffer manager.
	 *
	 * @return the file buffer manager
	 * @since 3.4
	 */
	private ResourceTextFileBufferManager getManager() {
		return (ResourceTextFileBufferManager)fManager;
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
		fBOM= null;
		try {
			fFile.setCharset(encoding, null);
			if (encoding == null)
				fEncoding= fFile.getCharset();
			cacheBOM();
		} catch (CoreException x) {
			handleCoreException(x);
		}
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBuffer#getStatus()
	 */
	public IStatus getStatus() {
		if (!isDisconnected()) {
			if (fStatus != null)
				return fStatus;
			return (fDocument == null ? STATUS_ERROR : Status.OK_STATUS);
		}
		return STATUS_ERROR;
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBuffer#getContentType()
	 * @since 3.1
	 */
	public IContentType getContentType() throws CoreException {
		try {
			if (isDirty()) {
				Reader reader= null;
				try {
					reader= new DocumentReader(getDocument());
					IContentDescription desc= Platform.getContentTypeManager().getDescriptionFor(reader, fFile.getName(), NO_PROPERTIES);
					if (desc != null && desc.getContentType() != null)
						return desc.getContentType();
				} finally {
					try {
						if (reader != null)
							reader.close();
					} catch (IOException x) {
					}
				}
			}
			IContentDescription desc= fFile.getContentDescription();
			if (desc != null && desc.getContentType() != null)
				return desc.getContentType();
			return null;
		} catch (IOException x) {
			throw new CoreException(new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IStatus.OK, NLSUtility.format(FileBuffersMessages.FileBuffer_error_queryContentDescription, fFile.getFullPath().toOSString()), x));
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
					fFile.setCharset(fEncoding, monitor);
					// if successful delete old property
					fFile.setPersistentProperty(ENCODING_KEY, null);
				} catch (CoreException ex) {
					// log problem because we could not migrate the property successfully
					handleCoreException(ex);
				}
				cacheBOM();
			} else {
				cacheEncodingState();
			}


			fDocument= getManager().createEmptyDocument(fFile);
			setDocumentContent(fDocument, fFile, fEncoding);

		} catch (CoreException x) {
			fDocument= getManager().createEmptyDocument(fFile);
			fStatus= x.getStatus();
		}
	}

	/**
	 * Caches the BOM of the underlying file.
	 *
	 * @throws CoreException if reading of file's content description fails
	 */
	protected void cacheBOM() throws CoreException {
		fBOM= null;

		IContentDescription description= fFile.getContentDescription();
		if (description != null)
			fBOM= (byte[])description.getProperty(IContentDescription.BYTE_ORDER_MARK);
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
	protected void dispose() {
		try {
			fDocument.removePositionCategory(IDocument.DEFAULT_CATEGORY);
		} catch (BadPositionCategoryException ex) {
			// Category is already gone - no problem.
		}
		if (fAnnotationModel != null)
			fAnnotationModel.disconnect(fDocument);
		fDocument= null;
		super.dispose();
	}

	/*
	 * @see org.eclipse.core.internal.filebuffers.FileBuffer#commitFileBufferContent(org.eclipse.core.runtime.IProgressMonitor, boolean)
	 */
	protected void commitFileBufferContent(IProgressMonitor monitor, boolean overwrite) throws CoreException {
		String encoding= computeEncoding();

		if (fBOM == IContentDescription.BOM_UTF_16LE && CHARSET_UTF_16.equals(encoding))
			encoding= CHARSET_UTF_16LE;

		Charset charset;
		try {
			charset= Charset.forName(encoding);
		} catch (UnsupportedCharsetException ex) {
			String message= NLSUtility.format(FileBuffersMessages.ResourceTextFileBuffer_error_unsupported_encoding_message_arg, encoding);
			IStatus s= new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IStatus.OK, message, ex);
			throw new CoreException(s);
		} catch (IllegalCharsetNameException ex) {
			String message= NLSUtility.format(FileBuffersMessages.ResourceTextFileBuffer_error_illegal_encoding_message_arg, encoding);
			IStatus s= new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IStatus.OK, message, ex);
			throw new CoreException(s);
		}

		CharsetEncoder encoder= charset.newEncoder();
		encoder.onMalformedInput(CodingErrorAction.REPLACE);
		encoder.onUnmappableCharacter(CodingErrorAction.REPORT);

		InputStream stream;

		try {
			byte[] bytes;
			ByteBuffer byteBuffer= encoder.encode(CharBuffer.wrap(fDocument.get()));
			if (byteBuffer.hasArray())
				bytes= byteBuffer.array();
			else {
				bytes= new byte[byteBuffer.limit()];
				byteBuffer.get(bytes);
			}
			stream= new ByteArrayInputStream(bytes, 0, byteBuffer.limit());
		} catch (CharacterCodingException ex) {
			Assert.isTrue(ex instanceof UnmappableCharacterException);
			String message= NLSUtility.format(FileBuffersMessages.ResourceTextFileBuffer_error_charset_mapping_failed_message_arg, encoding);
			IStatus s= new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IFileBufferStatusCodes.CHARSET_MAPPING_FAILED, message, ex);
			throw new CoreException(s);
		}

		/*
		 * XXX:
		 * This is a workaround for a corresponding bug in Java readers and writer,
		 * see http://developer.java.sun.com/developer/bugParade/bugs/4508058.html
		 */
		if (fBOM == IContentDescription.BOM_UTF_8 && CHARSET_UTF_8.equals(encoding))
			stream= new SequenceInputStream(new ByteArrayInputStream(IContentDescription.BOM_UTF_8), stream);

		if (fBOM == IContentDescription.BOM_UTF_16LE && CHARSET_UTF_16LE.equals(encoding))
			stream= new SequenceInputStream(new ByteArrayInputStream(IContentDescription.BOM_UTF_16LE), stream);

		if (fFile.exists()) {

			// here the file synchronizer should actually be removed and afterwards added again. However,
			// we are already inside an operation, so the delta is sent AFTER we have added the listener
			fFile.setContents(stream, overwrite, true, monitor);
			// set synchronization stamp to know whether the file synchronizer must become active

			if (fDocument instanceof IDocumentExtension4) {
				fSynchronizationStamp= ((IDocumentExtension4)fDocument).getModificationStamp();
				fFile.revertModificationStamp(fSynchronizationStamp);
			} else
				fSynchronizationStamp= fFile.getModificationStamp();

			if (fAnnotationModel instanceof IPersistableAnnotationModel) {
				IPersistableAnnotationModel persistableModel= (IPersistableAnnotationModel) fAnnotationModel;
				persistableModel.commit(fDocument);
			}

		} else {

			monitor= Progress.getMonitor(monitor);
			try {
				monitor.beginTask(FileBuffersMessages.ResourceTextFileBuffer_task_saving, 2);
				ContainerCreator creator = new ContainerCreator(fFile.getWorkspace(), fFile.getParent().getFullPath());
				IProgressMonitor subMonitor= new SubProgressMonitor(monitor, 1);
				creator.createContainer(subMonitor);
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

	}

	private String computeEncoding() {
		// User-defined encoding has first priority
		if (fExplicitEncoding != null)
			return fExplicitEncoding;

		// Probe content
		Reader reader= new DocumentReader(fDocument);
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
			} catch (IOException x) {
			}
		}

		// Use file's encoding if the file has a BOM
		if (fBOM != null)
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
		cacheBOM();
	}

	/*
	 * @see org.eclipse.core.internal.filebuffers.ResourceFileBuffer#handleFileContentChanged()
	 */
	protected void handleFileContentChanged(boolean revert, boolean updateModificationStamp) throws CoreException {

		IDocument document= getManager().createEmptyDocument(fFile);
		IStatus status= null;

		try {
			cacheEncodingState();
			setDocumentContent(document, fFile, fEncoding);
		} catch (CoreException x) {
			status= x.getStatus();
		}

		String newContent= document.get();
		boolean replaceContent= updateModificationStamp || !newContent.equals(fDocument.get());

		if (replaceContent)
			fManager.fireBufferContentAboutToBeReplaced(this);

		removeFileBufferContentListeners();
		fSynchronizationStamp= fFile.getModificationStamp();
		if (replaceContent) {
			if (fDocument instanceof IDocumentExtension4)
				((IDocumentExtension4)fDocument).set(newContent, fSynchronizationStamp);
			else
				fDocument.set(newContent);
		}
		fCanBeSaved= false;
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
	 * @param file the file which delivers the document content
	 * @param encoding the character encoding for reading the given stream
	 * @exception CoreException if the given stream can not be read
	 */
	private void setDocumentContent(IDocument document, IFile file, String encoding) throws CoreException {
		InputStream contentStream= file.getContents();
		Reader in= null;
		try {

			if (encoding == null)
				encoding= fManager.getDefaultEncoding();

			/*
			 * XXX:
			 * This is a workaround for a corresponding bug in Java readers and writer,
			 * see http://developer.java.sun.com/developer/bugParade/bugs/4508058.html
			 */
			if (fBOM != null && CHARSET_UTF_8.equals(encoding)) {
				int n= 0;
				do {
					int bytes= contentStream.read(new byte[IContentDescription.BOM_UTF_8.length]);
					if (bytes == -1)
						throw new IOException();
					n += bytes;
				} while (n < IContentDescription.BOM_UTF_8.length);
			}

			in= new BufferedReader(new InputStreamReader(contentStream, encoding), BUFFER_SIZE);
			StringBuffer buffer= new StringBuffer(BUFFER_SIZE);
			char[] readBuffer= new char[READER_CHUNK_SIZE];
			int n= in.read(readBuffer);
			while (n > 0) {
				buffer.append(readBuffer, 0, n);
				n= in.read(readBuffer);
			}

			if (document instanceof IDocumentExtension4)
				((IDocumentExtension4)document).set(buffer.toString(), fFile.getModificationStamp());
			else
				document.set(buffer.toString());

		} catch (IOException x) {
			String message= (x.getMessage() != null ? x.getMessage() : ""); //$NON-NLS-1$
			IStatus s= new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IStatus.OK, message, x);
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
}
