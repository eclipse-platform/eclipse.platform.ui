/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.editors.text;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IPath;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.manipulation.ConvertLineDelimitersOperation;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;

import org.eclipse.jface.text.ITextSelection;

import org.eclipse.ui.internal.editors.text.SelectResourcesDialog.IFilter;

import org.eclipse.ui.editors.text.FileBufferOperationAction;


/**
 * A file buffer operation action that changes the line delimiters to a specified
 * line delimiter.
 *
 * @since 3.1
 */
public class ConvertLineDelimitersAction extends FileBufferOperationAction {

	private String fLabel;
	private boolean fStrictCheckIfTextLocation;

	protected ConvertLineDelimitersAction(String lineDelimiter, String label) {
		super(new ConvertLineDelimitersOperation(lineDelimiter));
		setText(constructLabel(label, lineDelimiter, System.getProperty("line.separator"))); //$NON-NLS-1$
		fLabel= Action.removeMnemonics(label);
	}

	private static String constructLabel(String label, String lineDelimiter, String platformLineDelimiter) {
		if (lineDelimiter.equals(platformLineDelimiter))
			return label + TextEditorMessages.ConvertLineDelimitersAction_default_label;
		return label;
	}

	/*
	 * @see org.eclipse.ui.internal.editors.text.FileBufferOperationAction#isAcceptableLocation(org.eclipse.core.runtime.IPath)
	 */
	protected boolean isAcceptableLocation(IPath location) {
		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		return location != null && manager.isTextFileLocation(location, fStrictCheckIfTextLocation);
	}

	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		fStrictCheckIfTextLocation= !(selection instanceof ITextSelection);
	}

	/*
	 * @see org.eclipse.ui.internal.editors.text.FileBufferOperationAction#collectFiles(org.eclipse.core.resources.IResource[])
	 */
	protected IFile[] collectFiles(final IResource[] resources) {
		fStrictCheckIfTextLocation= fStrictCheckIfTextLocation || resources.length != 1 || resources[0].getType() != IResource.FILE;
		IFile[] files= super.collectFiles(resources);
		files= filterUnacceptableFiles(files);
		if (containsOnlyFiles(resources))
			return files;

    	final IFilter filter= new IFilter() {
			public boolean accept(IResource resource) {
				return resource != null && isAcceptableLocation(resource.getFullPath());
			}
		};

		SelectResourcesDialog dialog= new SelectResourcesDialog(getShell(), NLSUtility.format(TextEditorMessages.ConvertLineDelimitersAction_dialog_title, fLabel), TextEditorMessages.ConvertLineDelimitersAction_dialog_description, filter);
		dialog.setInput(resources);
		int result= dialog.open();
		if (Window.OK == result) {
			IResource[] selectedResources= dialog.getSelectedResources();
			return super.collectFiles(selectedResources);
		}
		return null;
	}

	/**
	 * Checks whether the given resources array contains
	 * only files.
	 *
	 * @param resources the array with the resources
	 * @return <code>true</code> if there array only contains <code>IFiles</code>s
	 * @since 3.2
	 */
	private boolean containsOnlyFiles(IResource[] resources) {
		for (int i= 0; i < resources.length; i++) {
			IResource resource= resources[i];
			if ((IResource.FILE & resource.getType()) == 0)
				return false;
		}
		return true;
	}

	/**
	 * Filters the unacceptable files.
	 *
	 * @param files the files to filter
	 * @return an array of files
	 * @since 3.2
	 */
	private IFile[] filterUnacceptableFiles(IFile[] files) {
		Set filtered= new HashSet();
		for (int i= 0; i < files.length; i++) {
			IFile file= files[i];
			if (isAcceptableLocation(file.getFullPath()))
				filtered.add(file);
		}
		return (IFile[]) filtered.toArray(new IFile[filtered.size()]);
	}

}
