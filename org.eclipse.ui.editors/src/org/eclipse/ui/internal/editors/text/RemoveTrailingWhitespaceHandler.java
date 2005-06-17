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
package org.eclipse.ui.internal.editors.text;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.manipulation.RemoveTrailingWhitespaceOperation;

import org.eclipse.jface.window.Window;

import org.eclipse.ui.editors.text.FileBufferOperationHandler;


/**
 * A file buffer operation action that removes trailing whitespace.
 *
 * @since 3.1
 */
public class RemoveTrailingWhitespaceHandler extends FileBufferOperationHandler {

	public RemoveTrailingWhitespaceHandler() {
		super(new RemoveTrailingWhitespaceOperation());
	}

	/*
	 * @see org.eclipse.ui.editors.text.FileBufferOperationHandler#isAcceptableLocation(org.eclipse.core.runtime.IPath)
	 */
	protected boolean isAcceptableLocation(IPath location) {
		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		return manager.isTextFileLocation(location);
	}

	/*
	 * @see org.eclipse.ui.editors.text.FileBufferOperationHandler#collectFiles(org.eclipse.core.resources.IResource[])
	 */
	protected IFile[] collectFiles(IResource[] resources) {

		IFile[] files= super.collectFiles(resources);
		if (files != null && resources != null && files.length == resources.length)
			return files;

		SelectResourcesDialog dialog= new SelectResourcesDialog(getShell(), TextEditorMessages.RemoveTrailingWhitespaceHandler_dialog_title, TextEditorMessages.RemoveTrailingWhitespaceHandler_dialog_description);
		dialog.setInput(resources);
		int result= dialog.open();
		if (Window.OK == result) {
			IResource[] selectedResources= dialog.getSelectedResources();
			return super.collectFiles(selectedResources);
		}
		return null;
	}
}
