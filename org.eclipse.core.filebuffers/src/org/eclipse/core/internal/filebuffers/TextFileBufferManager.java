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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filebuffers.IDocumentFactory;
import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferListener;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

/**
 *
 */
public class TextFileBufferManager implements ITextFileBufferManager {	
		
	private Map fFilesBuffers= new HashMap();
	private List fFileBufferListeners= new ArrayList();
	private ExtensionsRegistry fRegistry;


	public TextFileBufferManager()  {
		fRegistry= new ExtensionsRegistry();
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBufferManager#connect(org.eclipse.core.resources.IFile, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void connect(IFile file, IProgressMonitor monitor) throws CoreException {
		Assert.isNotNull(file);
		FileBuffer fileBuffer= (FileBuffer) fFilesBuffers.get(file);
		if (fileBuffer == null)  {
			fileBuffer= createFileBuffer(file);
			if (fileBuffer == null)
				throw new CoreException(new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, 0, "Cannot create file buffer.", null));
			fileBuffer.create(file, monitor);
			fFilesBuffers.put(file, fileBuffer);
		}
		fileBuffer.connect();
	}
	

	/*
	 * @see org.eclipse.core.filebuffers.IFileBufferManager#disconnect(org.eclipse.core.resources.IFile, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void disconnect(IFile file, IProgressMonitor monitor) throws CoreException {
		Assert.isNotNull(file);
		FileBuffer fileBuffer= (FileBuffer) fFilesBuffers.get(file);
		if (fileBuffer != null) {
			fileBuffer.disconnect();
			fFilesBuffers.remove(file);
		}
	}
	
	private FileBuffer createFileBuffer(IFile file) {
		if (isTextFile(file))
			return new TextFileBuffer(this);
		return null;
	}
	
	private boolean isTextFile(IFile file) {
		return true;
	}
	
	/*
	 * @see org.eclipse.core.filebuffers.IFileBufferManager#getFileBuffer(org.eclipse.core.resources.IFile)
	 */
	public IFileBuffer getFileBuffer(IFile file) {
		return (IFileBuffer) fFilesBuffers.get(file);
	}
	
	/*
	 * @see org.eclipse.core.filebuffers.ITextFileBufferManager#getTextFileBuffer(org.eclipse.core.resources.IFile)
	 */
	public ITextFileBuffer getTextFileBuffer(IFile file) {
		return (ITextFileBuffer) fFilesBuffers.get(file);
	}

	/*
	 * @see org.eclipse.core.buffer.text.IBufferedFileManager#getDefaultEncoding()
	 */
	public String getDefaultEncoding() {
		return ResourcesPlugin.getEncoding();
	}

	/*
	 * @see org.eclipse.core.filebuffers.ITextFileBufferManager#createEmptyDocument(org.eclipse.core.resources.IFile)
	 */
	public IDocument createEmptyDocument(IFile file) {
		IDocumentFactory factory= fRegistry.getDocumentFactory(file);
		
		IDocument document= null;
		if (factory != null)
			document= factory.createDocument();
		else
			document= new Document();
			
		IDocumentSetupParticipant[] participants= fRegistry.getDocumentSetupParticipants((IFile) file);
		if (participants != null) {
			for (int i= 0; i < participants.length; i++)
				participants[i].setup(document);
		}
		
		return document;
	}
	
	/*
	 * @see org.eclipse.core.filebuffers.IFileBufferManager#addFileBufferListener(org.eclipse.core.filebuffers.IFileBufferListener)
	 */
	public void addFileBufferListener(IFileBufferListener listener) {
		Assert.isNotNull(listener);
		if (!fFileBufferListeners.contains(listener))
		fFileBufferListeners.add(listener);
	}
	
	/*
	 * @see org.eclipse.core.filebuffers.IFileBufferManager#removeFileBufferListener(org.eclipse.core.filebuffers.IFileBufferListener)
	 */
	public void removeFileBufferListener(IFileBufferListener listener) {
		Assert.isNotNull(listener);
		fFileBufferListeners.remove(listener);
	}
	
	protected void fireDirtyStateChanged(IFileBuffer buffer, boolean isDirty) {
		Iterator e= new ArrayList(fFileBufferListeners).iterator();
		while (e.hasNext()) {
			IFileBufferListener l= (IFileBufferListener) e.next();
			l.dirtyStateChanged(buffer, isDirty);
		}
	}
	
	protected void fireBufferContentAboutToBeReplaced(IFileBuffer buffer) {
		Iterator e= new ArrayList(fFileBufferListeners).iterator();
		while (e.hasNext()) {
			IFileBufferListener l= (IFileBufferListener) e.next();
			l.bufferContentAboutToBeReplaced(buffer);
		}
	}

	protected void fireBufferContentReplaced(IFileBuffer buffer) {
		Iterator e= new ArrayList(fFileBufferListeners).iterator();
		while (e.hasNext()) {
			IFileBufferListener l= (IFileBufferListener) e.next();
			l.bufferContentReplaced(buffer);
		}
	}

	protected void fireUnderlyingFileMoved(IFileBuffer buffer, IFile target) {
		Iterator e= new ArrayList(fFileBufferListeners).iterator();
		while (e.hasNext()) {
			IFileBufferListener l= (IFileBufferListener) e.next();
			l.underlyingFileMoved(buffer, target);
		}
	}

	protected void fireUnderlyingFileDeleted(IFileBuffer buffer) {
		Iterator e= new ArrayList(fFileBufferListeners).iterator();
		while (e.hasNext()) {
			IFileBufferListener l= (IFileBufferListener) e.next();
			l.underlyingFileDeleted(buffer);
		}
	}

	protected void fireStateValidationChanged(IFileBuffer buffer, boolean isStateValidated) {
		Iterator e= new ArrayList(fFileBufferListeners).iterator();
		while (e.hasNext()) {
			IFileBufferListener l= (IFileBufferListener) e.next();
			l.stateValidationChanged(buffer, isStateValidated);
		}
	}

	protected void fireStateChanging(IFileBuffer buffer) {
		Iterator e= new ArrayList(fFileBufferListeners).iterator();
		while (e.hasNext()) {
			IFileBufferListener l= (IFileBufferListener) e.next();
			l.stateChanging(buffer);
		}
	}

	protected void fireStateChangeFailed(IFileBuffer buffer) {
		Iterator e= new ArrayList(fFileBufferListeners).iterator();
		while (e.hasNext()) {
			IFileBufferListener l= (IFileBufferListener) e.next();
			l.stateChangeFailed(buffer);
		}
	}
}
