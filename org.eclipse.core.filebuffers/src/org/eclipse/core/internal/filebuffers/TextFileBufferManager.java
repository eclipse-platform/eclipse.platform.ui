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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.IAnnotationModelFactory;
import org.eclipse.core.filebuffers.IDocumentFactory;
import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferListener;
import org.eclipse.core.filebuffers.ISynchronizationContext;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;

/**
 * @since 3.0
 */
public class TextFileBufferManager implements ITextFileBufferManager {	
		
	private Map fFilesBuffers= new HashMap();
	private List fFileBufferListeners= new ArrayList();
	private ExtensionsRegistry fRegistry;
	private ISynchronizationContext fSynchronizationContext;


	public TextFileBufferManager()  {
		fRegistry= new ExtensionsRegistry();
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBufferManager#connect(org.eclipse.core.runtime.IPath, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void connect(IPath location, IProgressMonitor monitor) throws CoreException {
		Assert.isNotNull(location);
		location= FileBuffers.normalizeLocation(location);
		
		AbstractFileBuffer fileBuffer= (AbstractFileBuffer) fFilesBuffers.get(location);
		if (fileBuffer == null)  {
			
			fileBuffer= createFileBuffer(location);
			if (fileBuffer == null)
				throw new CoreException(new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, 0, FileBuffersMessages.getString("FileBufferManager.error.canNotCreateFilebuffer"), null)); //$NON-NLS-1$
			
			fileBuffer.create(location, monitor);
			fileBuffer.connect();
			fFilesBuffers.put(location, fileBuffer);
			fireBufferCreated(fileBuffer);
			
		} else {
			fileBuffer.connect();
		}
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBufferManager#disconnect(org.eclipse.core.runtime.IPath, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void disconnect(IPath location, IProgressMonitor monitor) throws CoreException {
		Assert.isNotNull(location);
		location= FileBuffers.normalizeLocation(location);
		
		AbstractFileBuffer fileBuffer= (AbstractFileBuffer) fFilesBuffers.get(location);
		if (fileBuffer != null) {
			fileBuffer.disconnect();
			if (fileBuffer.isDisposed()) {
				fFilesBuffers.remove(location);
				fireBufferDisposed(fileBuffer);
			}
		}
	}
	
	private AbstractFileBuffer createFileBuffer(IPath location) {
		if (!isTextFile(location))
			return null;
		
		if (isWorkspaceResource(location))
			return new ResourceTextFileBuffer(this);
		
		return new JavaTextFileBuffer(this);
	}
	
	private boolean isWorkspaceResource(IPath location) {
		return FileBuffers.getWorkspaceFileAtLocation(location) != null;
	}
	
	private boolean isTextFile(IPath location) {
		return true;
	}
	
	/*
	 * @see org.eclipse.core.filebuffers.IFileBufferManager#getFileBuffer(org.eclipse.core.runtime.IPath)
	 */
	public IFileBuffer getFileBuffer(IPath location) {
		location= FileBuffers.normalizeLocation(location);
		return (IFileBuffer) fFilesBuffers.get(location);
	}
	
	/*
	 * @see org.eclipse.core.filebuffers.ITextFileBufferManager#getTextFileBuffer(org.eclipse.core.runtime.IPath)
	 */
	public ITextFileBuffer getTextFileBuffer(IPath location) {
		location= FileBuffers.normalizeLocation(location);
		return (ITextFileBuffer) fFilesBuffers.get(location);
	}

	/*
	 * @see org.eclipse.core.buffer.text.IBufferedFileManager#getDefaultEncoding()
	 */
	public String getDefaultEncoding() {
		return ResourcesPlugin.getEncoding();
	}

	/*
	 * @see org.eclipse.core.filebuffers.ITextFileBufferManager#createEmptyDocument(org.eclipse.core.runtime.IPath)
	 */
	public IDocument createEmptyDocument(IPath location) {
		Assert.isNotNull(location);
		location= FileBuffers.normalizeLocation(location);
		
		IDocumentFactory factory= fRegistry.getDocumentFactory(location);
		
		IDocument document= null;
		if (factory != null)
			document= factory.createDocument();
		else
			document= new Document();
			
		IDocumentSetupParticipant[] participants= fRegistry.getDocumentSetupParticipants(location);
		if (participants != null) {
			for (int i= 0; i < participants.length; i++)
				participants[i].setup(document);
		}
		
		return document;
	}
	
	/*
	 * @see org.eclipse.core.filebuffers.ITextFileBufferManager#createAnnotationModel(org.eclipse.core.runtime.IPath)
	 */
	public IAnnotationModel createAnnotationModel(IPath location) {
		Assert.isNotNull(location);
		location= FileBuffers.normalizeLocation(location);
		IAnnotationModelFactory factory= fRegistry.getAnnotationModelFactory(location);
		if (factory != null)
			return factory.createAnnotationModel(location);
		return null;
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
	
	/*
	 * @see org.eclipse.core.filebuffers.IFileBufferManager#setSynchronizationContext(org.eclipse.core.filebuffers.ISynchronizationContext)
	 */
	public void setSynchronizationContext(ISynchronizationContext context) {
		fSynchronizationContext= context;
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBufferManager#requestSynchronizationContext(org.eclipse.core.runtime.IPath)
	 */
	public void requestSynchronizationContext(IPath location) {
		Assert.isNotNull(location);
		location= FileBuffers.normalizeLocation(location);
		
		AbstractFileBuffer fileBuffer= (AbstractFileBuffer) fFilesBuffers.get(location);
		if (fileBuffer != null)
			fileBuffer.requestSynchronizationContext();
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBufferManager#releaseSynchronizationContext(org.eclipse.core.runtime.IPath)
	 */
	public void releaseSynchronizationContext(IPath location) {
		Assert.isNotNull(location);
		location= FileBuffers.normalizeLocation(location);
		
		AbstractFileBuffer fileBuffer= (AbstractFileBuffer) fFilesBuffers.get(location);
		if (fileBuffer != null)
			fileBuffer.releaseSynchronizationContext();
	}
	
	/**
	 * Executes the given runnable in the synchronization context of this file buffer manager.
	 * If there is no synchronization context connected with this manager, the runnable is
	 * directly executed.
	 * 
	 * @param runnable the runnable to be executed
	 */
	public void execute(Runnable runnable, boolean requestSynchronizationContext) {
		if (requestSynchronizationContext && fSynchronizationContext != null)
			fSynchronizationContext.run(runnable);
		else
			runnable.run();
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

	protected void fireUnderlyingFileMoved(IFileBuffer buffer, IPath target) {
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
	
	protected void fireBufferCreated(IFileBuffer buffer) {
		Iterator e= new ArrayList(fFileBufferListeners).iterator();
		while (e.hasNext()) {
			IFileBufferListener l= (IFileBufferListener) e.next();
			l.bufferCreated(buffer);
		}
	}
	
	protected void fireBufferDisposed(IFileBuffer buffer) {
		Iterator e= new ArrayList(fFileBufferListeners).iterator();
		while (e.hasNext()) {
			IFileBufferListener l= (IFileBufferListener) e.next();
			l.bufferDisposed(buffer);
		}
	}
}
