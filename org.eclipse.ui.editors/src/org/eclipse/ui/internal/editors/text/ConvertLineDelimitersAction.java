/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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
package org.eclipse.ui.internal.editors.text;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.IPath;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.manipulation.ConvertLineDelimitersOperation;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
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

	@Override
	protected boolean isAcceptableLocation(IPath location) {
		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		return location != null && manager.isTextFileLocation(location, fStrictCheckIfTextLocation);
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		fStrictCheckIfTextLocation= !(selection instanceof ITextSelection);
	}

	@Override
	protected IFile[] collectFiles(final IResource[] resources) {
		fStrictCheckIfTextLocation= fStrictCheckIfTextLocation || resources.length != 1 || resources[0].getType() != IResource.FILE;
		if (containsOnlyFiles(resources)) {
			IFile[] files= super.collectFiles(resources);
			return filterUnacceptableFiles(files);
		}

		final IFilter filter= new IFilter() {
			@Override
			public boolean accept(IResource resource) {
				return resource != null && isAcceptableLocation(resource.getFullPath());
			}
		};

		SelectResourcesDialog dialog= new SelectResourcesDialog(getShell(), getDialogTitle(), TextEditorMessages.ConvertLineDelimitersAction_dialog_description, filter) {
			@Override
			protected Composite createSelectionButtonGroup(Composite parent) {
				Composite buttonGroup= super.createSelectionButtonGroup(parent);
				
				final Button button = new Button(buttonGroup, SWT.CHECK);
				((GridLayout) buttonGroup.getLayout()).numColumns++;
				button.setText(TextEditorMessages.ConvertLineDelimitersAction_show_only_text_files);
				button.setFont(JFaceResources.getDialogFont());
				button.setSelection(fStrictCheckIfTextLocation);
				button.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						fStrictCheckIfTextLocation= button.getSelection();
						refresh();
					}
				});
				
				return buttonGroup;
			}
		};
		dialog.setInput(resources);
		int result= dialog.open();
		if (Window.OK == result) {
			IResource[] selectedResources= dialog.getSelectedResources();
			return super.collectFiles(selectedResources);
		}
		return null;
	}
	
	private String getDialogTitle() {
		return NLSUtility.format(TextEditorMessages.ConvertLineDelimitersAction_dialog_title, fLabel);
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
		boolean askForBinary= true;
		Set<IFile> filtered= new HashSet<>();
		for (int i= 0; i < files.length; i++) {
			IFile file= files[i];
			if (isAcceptableLocation(file.getFullPath())) {
				filtered.add(file);
			} else if (askForBinary) {
				int result= new MessageDialog(getShell(), getDialogTitle(), null,
						TextEditorMessages.ConvertLineDelimitersAction_nontext_selection,
						MessageDialog.WARNING,
						new String[] { TextEditorMessages.ConvertLineDelimitersAction_convert_all , TextEditorMessages.ConvertLineDelimitersAction_convert_text, IDialogConstants.CANCEL_LABEL}, 1).open();
				if (result == 0) {
					fStrictCheckIfTextLocation= false;
					filtered.add(file);
				} else if (result == 1) {
					askForBinary= false;
				} else {
					return null;
				}
			}
		}
		return filtered.toArray(new IFile[filtered.size()]);
	}

}
