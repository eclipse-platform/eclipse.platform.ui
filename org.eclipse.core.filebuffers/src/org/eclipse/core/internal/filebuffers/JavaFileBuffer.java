/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.filebuffers;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import org.eclipse.core.resources.IResource;

import org.eclipse.core.filebuffers.FileBuffers;

/**
 * @since 3.0
 */
public abstract class JavaFileBuffer extends AbstractFileBuffer  {

	/** The location */
	protected IPath fLocation;
	/** The element for which the info is stored */
	protected IFileStore fFileStore;
	/** How often the element has been connected */
	protected int fReferenceCount;
	/** Can the element be saved */
	protected boolean fCanBeSaved= false;
	/** The status of this element */
	protected IStatus fStatus;
	/** The time stamp at which this buffer synchronized with the underlying file. */
	protected long fSynchronizationStamp= IResource.NULL_STAMP;
	/** How often the synchronization context has been requested */
	protected int fSynchronizationContextCount;
	/** The text file buffer manager */
	protected TextFileBufferManager fManager;


	public JavaFileBuffer(TextFileBufferManager manager) {
		super();
		fManager= manager;
	}

	abstract protected void addFileBufferContentListeners();

	abstract protected void removeFileBufferContentListeners();

	abstract protected void initializeFileBufferContent(IProgressMonitor monitor) throws CoreException;

	abstract protected void commitFileBufferContent(IProgressMonitor monitor, boolean overwrite) throws CoreException;

	public void create(IPath location, IProgressMonitor monitor) throws CoreException {
		fLocation= location;
		IFileStore fileStore= FileBuffers.getFileStoreAtLocation(location);
		IFileInfo info= fileStore.fetchInfo();
		if (info.exists())
			fFileStore= fileStore;
		initializeFileBufferContent(monitor);
		if (fFileStore != null)
			fSynchronizationStamp= info.getLastModified();

		addFileBufferContentListeners();
	}

	public void connect() {
		++ fReferenceCount;
		if (fReferenceCount == 1)
			connected();
	}

	/**
	 * Called when this file buffer has been connected. This is the case when
	 * there is exactly one connection.
	 * <p>
	 * Clients may extend this method.
	 */
	protected void connected() {
	}

	public void disconnect() throws CoreException {
		-- fReferenceCount;
		if (fReferenceCount == 0)
			disconnected();
	}

	/**
	 * Called when this file buffer has been disconnected. This is the case when
	 * the number of connections drops to <code>0</code>.
	 * <p>
	 * Clients may extend this method.
	 */
	protected void disconnected() {
	}

	/*
	 * @see org.eclipse.core.internal.filebuffers.AbstractFileBuffer#isDisconnected()
	 * @since 3.1
	 */
	protected boolean isDisconnected() {
		return fReferenceCount <= 0;
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBuffer#getLocation()
	 */
	public IPath getLocation() {
		return fLocation;
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBuffer#commit(org.eclipse.core.runtime.IProgressMonitor, boolean)
	 */
	public void commit(IProgressMonitor monitor, boolean overwrite) throws CoreException {
		if (!isDisconnected() && fCanBeSaved) {

			fManager.fireStateChanging(this);

			try {
				commitFileBufferContent(monitor, overwrite);
			} catch (CoreException x) {
				fManager.fireStateChangeFailed(this);
				throw x;
			} catch (RuntimeException x) {
				fManager.fireStateChangeFailed(this);
				throw x;
			}

			fCanBeSaved= false;
			addFileBufferContentListeners();
			fManager.fireDirtyStateChanged(this, fCanBeSaved);
		}
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBuffer#computeCommitRule()
	 */
	public ISchedulingRule computeCommitRule() {
		return null;
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBuffer#isDirty()
	 */
	public boolean isDirty() {
		return fCanBeSaved;
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBuffer#setDirty(boolean)
	 */
	public void setDirty(boolean isDirty) {
		fCanBeSaved= isDirty;
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBuffer#isShared()
	 */
	public boolean isShared() {
		return fReferenceCount > 1;
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBuffer#computeValidateStateRule()
	 */
	public ISchedulingRule computeValidateStateRule() {
		return null;
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBuffer#validateState(org.eclipse.core.runtime.IProgressMonitor, java.lang.Object)
	 */
	public void validateState(IProgressMonitor monitor, Object computationContext) throws CoreException {
		// nop
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBuffer#isStateValidated()
	 */
	public boolean isStateValidated() {
		return true;
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBuffer#resetStateValidation()
	 */
	public void resetStateValidation() {
		// nop
	}

	/**
	 * Sends out the notification that the file serving as document input has been moved.
	 *
	 * @param newLocation the path of the new location of the file
	 */
	protected void handleFileMoved(IPath newLocation) {
		fManager.fireUnderlyingFileMoved(this, newLocation);
	}

	/**
	 * Defines the standard procedure to handle <code>CoreExceptions</code>. Exceptions
	 * are written to the plug-in log.
	 *
	 * @param exception the exception to be logged
	 */
	protected void handleCoreException(CoreException exception) {
		ILog log= FileBuffersPlugin.getDefault().getLog();
		log.log(exception.getStatus());
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBuffer#isSynchronized()
	 */
	public boolean isSynchronized() {
		return fSynchronizationStamp == getModificationStamp();
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBuffer#getModificationStamp()
	 */
	public long getModificationStamp() {
		return fFileStore != null ? fFileStore.fetchInfo().getLastModified() : IResource.NULL_STAMP;
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBuffer#requestSynchronizationContext()
	 */
	public void requestSynchronizationContext() {
		++ fSynchronizationContextCount;
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBuffer#releaseSynchronizationContext()
	 */
	public void releaseSynchronizationContext() {
		-- fSynchronizationContextCount;
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBuffer#isSynchronizationContextRequested()
	 */
	public boolean isSynchronizationContextRequested() {
		return fSynchronizationContextCount > 0;
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBuffer#isCommitable()
	 */
	public boolean isCommitable() {
		IFileInfo info= fFileStore.fetchInfo();
		return info.exists() && !info.isReadOnly();
	}

	/*
	 * @see org.eclipse.core.filebuffers.IStateValidationSupport#validationStateChanged(boolean, org.eclipse.core.runtime.IStatus)
	 */
	public void validationStateChanged(boolean validationState, IStatus status) {
		//nop
	}
}
