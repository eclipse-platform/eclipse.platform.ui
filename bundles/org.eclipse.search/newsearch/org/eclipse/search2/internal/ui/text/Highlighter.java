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
package org.eclipse.search2.internal.ui.text;

import org.eclipse.core.runtime.IPath;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferListener;

import org.eclipse.search.ui.text.Match;

public class Highlighter {
	private IFileBufferListener fBufferListener;

	public Highlighter() {
		fBufferListener= new IFileBufferListener() {
			@Override
			public void bufferCreated(IFileBuffer buffer) {
			}

			@Override
			public void bufferDisposed(IFileBuffer buffer) {
			}

			@Override
			public void bufferContentAboutToBeReplaced(IFileBuffer buffer) {
			}

			@Override
			public void bufferContentReplaced(IFileBuffer buffer) {
				handleContentReplaced(buffer);
			}

			@Override
			public void stateChanging(IFileBuffer buffer) {
			}

			@Override
			public void dirtyStateChanged(IFileBuffer buffer, boolean isDirty) {
			}

			@Override
			public void stateValidationChanged(IFileBuffer buffer, boolean isStateValidated) {
			}

			@Override
			public void underlyingFileMoved(IFileBuffer buffer, IPath path) {
			}

			@Override
			public void underlyingFileDeleted(IFileBuffer buffer) {
			}

			@Override
			public void stateChangeFailed(IFileBuffer buffer) {
			}
		};
		FileBuffers.getTextFileBufferManager().addFileBufferListener(fBufferListener);
	}

	/**
	 * Adds highlighting for the given matches
	 *
	 * @param matches the matches to add highlighting
	 */
	public void addHighlights(Match[] matches) {
	}

	/**
	 * Removes highlighting for the given matches
	 *
	 * @param matches the matches to remove the highlighting
	 */
	public void removeHighlights(Match[] matches) {
	}

	/**
	 * Removes all highlighting
	 */
	public void removeAll() {
	}

	/**
	 * Called when the highlighter is disposed.
	 */
	public void dispose() {
		FileBuffers.getTextFileBufferManager().removeFileBufferListener(fBufferListener);
	}

	/**
	 * Notifies that a buffer has its content changed
	 *
	 * @param buffer the buffer
	 */
	protected void handleContentReplaced(IFileBuffer buffer) {
	}

}
