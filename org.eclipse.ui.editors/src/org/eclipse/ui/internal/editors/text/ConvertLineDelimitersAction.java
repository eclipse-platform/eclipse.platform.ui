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
package org.eclipse.ui.internal.editors.text;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.manipulation.ConvertLineDelimitersOperation;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;

import org.eclipse.ui.editors.text.FileBufferOperationAction;


/**
 * Not yet for public use. API under construction.
 * 
 * @since 3.1
 */
public class ConvertLineDelimitersAction extends FileBufferOperationAction {
	
	private String fLabel;
	
	protected ConvertLineDelimitersAction(String lineDelimiter, String label) {
		super(new ConvertLineDelimitersOperation(lineDelimiter));
		setText(constructLabel(label, lineDelimiter, System.getProperty("line.separator")));
		fLabel= Action.removeMnemonics(label);
	}
	
	private static String constructLabel(String label, String lineDelimiter, String platformLineDelimiter) {
		if (lineDelimiter.equals(platformLineDelimiter))
			return label + " [default]";
		return label;
	}
	
	/*
	 * @see org.eclipse.ui.internal.editors.text.FileBufferOperationAction#isAcceptableLocation(org.eclipse.core.runtime.IPath)
	 */
	protected boolean isAcceptableLocation(IPath location) {
		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		return manager.isTextFileLocation(location);
	}
	
	/*
	 * @see org.eclipse.ui.internal.editors.text.FileBufferOperationAction#collectFiles(org.eclipse.core.resources.IResource[])
	 */
	protected IFile[] collectFiles(IResource[] resources) {
		
		IFile[] files= super.collectFiles(resources);
		if (files != null && resources != null && files.length == resources.length)
			return files;
		
		ConvertLineDelimitersDialog dialog= new ConvertLineDelimitersDialog(getShell(), "Convert Line Delimiters to " + fLabel);
		dialog.setInput(resources);
		int result= dialog.open();
		if (Window.OK == result) {
			IResource[] selectedResources= dialog.getSelectedResources();
			return super.collectFiles(selectedResources);
		}
		return null;
	}	
}
