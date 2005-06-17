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
package org.eclipse.core.filebuffers.tests;

import org.eclipse.core.runtime.IPath;

import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferListener;

/**
 * FileBufferListener
 */
public class FileBufferListener implements IFileBufferListener {
	
	/*
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#bufferCreated(org.eclipse.core.filebuffers.IFileBuffer)
	 */
	public void bufferCreated(IFileBuffer buffer) {
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#bufferDisposed(org.eclipse.core.filebuffers.IFileBuffer)
	 */
	public void bufferDisposed(IFileBuffer buffer) {
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#bufferContentAboutToBeReplaced(org.eclipse.core.filebuffers.IFileBuffer)
	 */
	public void bufferContentAboutToBeReplaced(IFileBuffer buffer) {
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#bufferContentReplaced(org.eclipse.core.filebuffers.IFileBuffer)
	 */
	public void bufferContentReplaced(IFileBuffer buffer) {
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#stateChanging(org.eclipse.core.filebuffers.IFileBuffer)
	 */
	public void stateChanging(IFileBuffer buffer) {
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#dirtyStateChanged(org.eclipse.core.filebuffers.IFileBuffer, boolean)
	 */
	public void dirtyStateChanged(IFileBuffer buffer, boolean isDirty) {
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#stateValidationChanged(org.eclipse.core.filebuffers.IFileBuffer, boolean)
	 */
	public void stateValidationChanged(IFileBuffer buffer, boolean isStateValidated) {
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#underlyingFileMoved(org.eclipse.core.filebuffers.IFileBuffer, org.eclipse.core.runtime.IPath)
	 */
	public void underlyingFileMoved(IFileBuffer buffer, IPath path) {
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#underlyingFileDeleted(org.eclipse.core.filebuffers.IFileBuffer)
	 */
	public void underlyingFileDeleted(IFileBuffer buffer) {
	}

	/*
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#stateChangeFailed(org.eclipse.core.filebuffers.IFileBuffer)
	 */
	public void stateChangeFailed(IFileBuffer buffer) {
	}
}
