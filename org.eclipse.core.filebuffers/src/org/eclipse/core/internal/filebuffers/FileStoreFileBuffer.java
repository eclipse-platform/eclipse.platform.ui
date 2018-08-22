/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.filebuffers;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import org.eclipse.jface.text.IDocumentExtension4;

/**
 * @since 3.3 (previously available as JavaFileBuffer since 3.0)
 */
public abstract class FileStoreFileBuffer extends AbstractFileBuffer  {

	/** The location */
	protected IPath fLocation;
	/** How often the element has been connected */
	protected int fReferenceCount;
	/** Can the element be saved */
	protected boolean fCanBeSaved= false;
	/** The status of this element */
	protected IStatus fStatus;
	/** The time stamp at which this buffer synchronized with the underlying file. */
	protected long fSynchronizationStamp= IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP;
	/** How often the synchronization context has been requested */
	protected int fSynchronizationContextCount;


	public FileStoreFileBuffer(TextFileBufferManager manager) {
		super(manager);
	}

	abstract protected void addFileBufferContentListeners();

	abstract protected void removeFileBufferContentListeners();

	abstract protected void initializeFileBufferContent(IProgressMonitor monitor) throws CoreException;

	abstract protected void commitFileBufferContent(IProgressMonitor monitor, boolean overwrite) throws CoreException;

	public void create(IFileStore fileStore, IProgressMonitor monitor) throws CoreException {
		IFileInfo info= fileStore.fetchInfo();
		fFileStore= fileStore;
		if (fLocation == null)
			fLocation= URIUtil.toPath(fileStore.toURI());

		initializeFileBufferContent(monitor);
		if (info.exists())
			fSynchronizationStamp= info.getLastModified();

		addFileBufferContentListeners();
	}

	@Override
	public void create(IPath location, IProgressMonitor monitor) throws CoreException {
		fLocation= location;
		create(EFS.getStore(URIUtil.toURI(getLocation())), monitor);
	}

	@Override
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

	@Override
	public void disconnect() throws CoreException {
		--fReferenceCount;
		if (fReferenceCount <= 0)
			disconnected();
	}

	/**
	 * Called when this file buffer has been disconnected. This is the case when
	 * the number of connections drops below <code>1</code>.
	 * <p>
	 * Clients may extend this method.
	 */
	protected void disconnected() {
	}

	@Override
	protected boolean isDisconnected() {
		return fReferenceCount <= 0;
	}

	@Override
	public IPath getLocation() {
		return fLocation;
	}

	@Override
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

	@Override
	public ISchedulingRule computeCommitRule() {
		return null;
	}

	@Override
	public boolean isDirty() {
		return fCanBeSaved;
	}

	@Override
	public void setDirty(boolean isDirty) {
		fCanBeSaved= isDirty;
	}

	@Override
	public boolean isShared() {
		return fReferenceCount > 1;
	}

	@Override
	public ISchedulingRule computeValidateStateRule() {
		return null;
	}

	@Override
	public void validateState(IProgressMonitor monitor, Object computationContext) throws CoreException {
		// nop
	}

	@Override
	public boolean isStateValidated() {
		return true;
	}

	@Override
	public void resetStateValidation() {
		// nop
	}

	@Override
	public boolean isSynchronized() {
		return fSynchronizationStamp == getModificationStamp();
	}

	@Override
	public void requestSynchronizationContext() {
		++ fSynchronizationContextCount;
	}

	@Override
	public void releaseSynchronizationContext() {
		-- fSynchronizationContextCount;
	}

	@Override
	public boolean isSynchronizationContextRequested() {
		return fSynchronizationContextCount > 0;
	}

	@Override
	public boolean isCommitable() {
		IFileInfo info= fFileStore.fetchInfo();
		return info.exists() && !info.getAttribute(EFS.ATTRIBUTE_READ_ONLY);
	}

	@Override
	public void validationStateChanged(boolean validationState, IStatus status) {
		//nop
	}
}
