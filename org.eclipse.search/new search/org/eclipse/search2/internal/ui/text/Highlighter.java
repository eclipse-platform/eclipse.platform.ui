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
package org.eclipse.search2.internal.ui.text;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferListener;
import org.eclipse.core.runtime.IPath;
import org.eclipse.search.ui.text.Match;

public class Highlighter {
	private IFileBufferListener fBufferListener;

	public Highlighter() {
		fBufferListener= new IFileBufferListener() {
			public void bufferCreated(IFileBuffer buffer) {
			}

			public void bufferDisposed(IFileBuffer buffer) {
			}

			public void bufferContentAboutToBeReplaced(IFileBuffer buffer) {
			}

			public void bufferContentReplaced(IFileBuffer buffer) {
				handleContentReplaced(buffer);
			}

			public void stateChanging(IFileBuffer buffer) {
			}

			public void dirtyStateChanged(IFileBuffer buffer, boolean isDirty) {
			}

			public void stateValidationChanged(IFileBuffer buffer, boolean isStateValidated) {
			}

			public void underlyingFileMoved(IFileBuffer buffer, IPath path) {
			}

			public void underlyingFileDeleted(IFileBuffer buffer) {
			}

			public void stateChangeFailed(IFileBuffer buffer) {
			}
		};
		FileBuffers.getTextFileBufferManager().addFileBufferListener(fBufferListener);
	}
	
	public void addHighlights(Match[] matches) {
	}

	public void removeHighlights(Match[] matches) {
	}
	
	public void removeAll() {
	}
	
	public void dispose() {
		FileBuffers.getTextFileBufferManager().removeFileBufferListener(fBufferListener);
	}
	
	protected void handleContentReplaced(IFileBuffer buffer) {
	}

}
