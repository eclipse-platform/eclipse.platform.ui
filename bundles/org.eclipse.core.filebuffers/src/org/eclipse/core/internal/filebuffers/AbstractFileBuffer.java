/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IStateValidationSupport;

import org.eclipse.jface.text.IDocumentExtension4;

/**
 * @since 3.0
 */
public abstract class AbstractFileBuffer implements IFileBuffer, IStateValidationSupport {

	/**
	 * The element for which the info is stored.
	 * @since 3.3
	 */
	protected IFileStore fFileStore;
	/**
	 * The text file buffer manager.
	 * @since 3.4 (pulled up from subclasses)
	 */
	final protected TextFileBufferManager fManager;


	public AbstractFileBuffer(TextFileBufferManager manager) {
		fManager= manager;
	}

	public abstract void create(IPath location, IProgressMonitor monitor) throws CoreException;

	public abstract void connect();

	public abstract void disconnect() throws CoreException;

	/**
	 * Returns whether this file buffer has been disconnected.
	 *
	 * @return <code>true</code> if already disposed, <code>false</code> otherwise
	 * @since 3.1
	 */
	protected abstract boolean isDisconnected();

	/**
	 * Disposes this file buffer.
	 * This implementation is always empty.
	 * <p>
	 * Subclasses may extend but must call <code>super.dispose()</code>.
	 * </p>
	 *
	 * @since 3.1
	 */
	protected void dispose() {
		// do nothing
	}

	@Override
	public void validationStateAboutToBeChanged() {
		fManager.fireStateChanging(this);
	}

	@Override
	public void validationStateChangeFailed() {
		fManager.fireStateChangeFailed(this);
	}

	@Override
	public long getModificationStamp() {
		IFileInfo info= fFileStore.fetchInfo();
		return info.exists() ? info.getLastModified() : IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP;
	}

	@Override
	public IFileStore getFileStore() {
		return fFileStore;
	}

}
