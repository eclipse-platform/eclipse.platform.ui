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
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferListener;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import org.eclipse.jface.text.Assert;

/**
 *
 */
public class FileBuffer implements IFileBuffer {
	
	private IFile fFile;
	private FileDocumentProvider2 fDocumentProvider;
	private List fElementStateListeners= new ArrayList();
	
	/**
	 * 
	 */
	public FileBuffer(IFile file, FileDocumentProvider2 documentProvider) {
		super();
		fFile= file;
		fDocumentProvider= documentProvider;
	}

	protected FileDocumentProvider2 getDocumentProvider() {
		return fDocumentProvider;
	}

	/*
	 * @see org.eclipse.core.buffer.text.IBufferedFile#getUnderlyingFile()
	 */
	public IFile getUnderlyingFile() {
		return fFile;
	}

	private ElementStateListener getStateListener(IFileBufferListener listener) {
		Iterator e= fElementStateListeners.iterator();
		while (e.hasNext()) {
			ElementStateListener s= (ElementStateListener) e.next();
			if (s.getBufferedListener() == listener)
				return s;
		}
		return null;
	}

	/*
	 * @see org.eclipse.core.buffer.text.IBufferedFile#addBufferedFileListener(org.eclipse.core.buffer.text.IBufferedFileListener)
	 */
	public void addFileBufferListener(IFileBufferListener listener) {
		Assert.isNotNull(listener);
		ElementStateListener stateListener= getStateListener(listener);
		if (stateListener == null)  {
			stateListener= new ElementStateListener(listener, this);
			fElementStateListeners.add(stateListener);
		}
		fDocumentProvider.addElementStateListener(stateListener);
	}
	
	/*
	 * @see org.eclipse.core.buffer.text.IBufferedFile#removeBufferedFileListener(org.eclipse.core.buffer.text.IBufferedFileListener)
	 */
	public void removeFileBufferListener(IFileBufferListener listener) {
		Assert.isNotNull(listener);
		ElementStateListener stateListener= getStateListener(listener);
		if (stateListener != null)  {
			fDocumentProvider.removeElementStateListener(stateListener);
			fElementStateListeners.remove(stateListener);
		}
	}

	/*
	 * @see org.eclipse.core.buffer.text.IBufferedFile#revert(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void revert(IProgressMonitor monitor) throws CoreException {
		IProgressMonitor previous= fDocumentProvider.getProgressMonitor();
		fDocumentProvider.setProgressMonitor(monitor);
		fDocumentProvider.restoreDocument(fFile);
		fDocumentProvider.setProgressMonitor(previous);
	}

	/*
	 * @see org.eclipse.core.buffer.text.IBufferedFile#commit(org.eclipse.core.runtime.IProgressMonitor, boolean)
	 */
	public void commit(IProgressMonitor monitor, boolean overwrite) throws CoreException {
		IProgressMonitor previous= fDocumentProvider.getProgressMonitor();
		fDocumentProvider.setProgressMonitor(monitor);
		fDocumentProvider.saveDocument(fFile, overwrite);
		fDocumentProvider.setProgressMonitor(previous);
	}

	/*
	 * @see org.eclipse.core.buffer.text.IBufferedFile#isDirty()
	 */
	public boolean isDirty() {
		return fDocumentProvider.canSaveDocument(fFile);
	}

	/*
	 * @see org.eclipse.core.buffer.text.IBufferedFile#validateState(org.eclipse.core.runtime.IProgressMonitor, java.lang.Object)
	 */
	public void validateState(IProgressMonitor monitor, Object computationContext) throws CoreException {
		IProgressMonitor previous= fDocumentProvider.getProgressMonitor();
		fDocumentProvider.setProgressMonitor(monitor);
		fDocumentProvider.validateState(fFile, computationContext);
		fDocumentProvider.setProgressMonitor(previous);
	}

	/*
	 * @see org.eclipse.core.buffer.text.IBufferedFile#isStateValidated()
	 */
	public boolean isStateValidated() {
		return fDocumentProvider.isStateValidated(fFile);
	}

	/*
	 * @see org.eclipse.core.buffer.text.IBufferedFile#getStatus()
	 */
	public IStatus getStatus() {
		return fDocumentProvider.getStatus(fFile);
	}
}
