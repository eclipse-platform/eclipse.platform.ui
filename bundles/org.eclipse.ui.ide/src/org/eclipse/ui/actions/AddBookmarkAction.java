package org.eclipse.ui.actions;

/******************************************************************************* 
 * Copyright (c) 2000, 2003 IBM Corporation and others. 
 * All rights reserved. This program and the accompanying materials! 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 * 
 * Contributors: 
 *        IBM Corporation - initial API and implementation 
 *   Sebastian Davids <sdavids@gmx.de>
 *     - Fix for bug 20510 - Add Bookmark action has wrong label in navigator or packages view
*********************************************************************/

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;

import java.util.Iterator;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;

/**
 * Standard action for adding a bookmark to the currently selected file
 * resource(s).
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class AddBookmarkAction extends SelectionListenerAction {

	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID + ".AddBookmarkAction"; //$NON-NLS-1$

	/**
	 * The shell in which to show any dialogs.
	 */
	private Shell shell;
	
	/**
	 * Whether to prompt the user for the bookmark name.
	 */
	private boolean promptForName = true;
	
	/**
	 * Creates a new bookmark action. By default, prompts the user for the
	 * bookmark name.
	 *
	 * @param shell the shell for any dialogs
	 */
	public AddBookmarkAction(Shell shell) {
		this(shell, true);
	}
	
	/**
	 * Creates a new bookmark action.
	 *
	 * @param shell the shell for any dialogs
	 * @param promptForName whether to ask the user for the bookmark name
	 */
	public AddBookmarkAction(Shell shell, boolean promptForName) {
		super(WorkbenchMessages.getString("AddBookmarkLabel")); //$NON-NLS-1$
		setId(ID);
		Assert.isNotNull(shell);
		this.shell = shell;
		this.promptForName = promptForName;
		setToolTipText(WorkbenchMessages.getString("AddBookmarkToolTip")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, IHelpContextIds.ADD_BOOKMARK_ACTION);
	}

	/**
	 * Creates a marker of the given type on each of the files in the
	 * current selection.
	 *
	 * @param markerType the marker type
	 */
	void createMarker(String markerType) {
		IStructuredSelection selection = getStructuredSelection();
		for (Iterator enum = selection.iterator(); enum.hasNext();) {
			Object o = enum.next();
			if (o instanceof IFile) {
				createMarker((IFile) o, markerType);
			} else if (o instanceof IAdaptable) {
				Object resource = ((IAdaptable) o).getAdapter(IResource.class);
				if (resource instanceof IFile)
					createMarker((IFile) resource, markerType);
			}
		}
	}
	
	/**
	 * Creates a marker of the given type on the given file resource.
	 *
	 * @param file the file resource
	 * @param markerType the marker type
	 */
	void createMarker(final IFile file, final String markerType) {
		try {
			file.getWorkspace().run(
				new IWorkspaceRunnable() {
					public void run(IProgressMonitor monitor) throws CoreException {
						String markerMessage = file.getName();
						if (promptForName)
							markerMessage = askForLabel(markerMessage);
						if (markerMessage != null) {
							IMarker marker = file.createMarker(markerType);
							marker.setAttribute(IMarker.MESSAGE, markerMessage);
						}
					}
				},
				null);
		} catch (CoreException e) {
			WorkbenchPlugin.log(null, e.getStatus()); // We don't care
		}
	}
	
	/* (non-Javadoc)
	 * Method declared on IAction.
	 */
	public void run() {
		createMarker(IMarker.BOOKMARK);
	}
	
	/**
	 * The <code>AddBookmarkAction</code> implementation of this
	 * <code>SelectionListenerAction</code> method enables the action only
	 * if the selection is not empty and contains just file resources.
	 */
	protected boolean updateSelection(IStructuredSelection selection) {
		return super.updateSelection(selection) && !selection.isEmpty() && selectionIsOfType(IFile.FILE);
	}

	/**
	 * Asks the user for a bookmark name.
	 *
	 * @param proposal the suggested bookmark name
	 * @return the bookmark name or <code>null</code> if cancelled.
	 */
	String askForLabel(String proposal) {
		String title = WorkbenchMessages.getString("AddBookmarkDialog.title"); //$NON-NLS-1$
		String message = WorkbenchMessages.getString("AddBookmarkDialog.message"); //$NON-NLS-1$

		IInputValidator inputValidator = new IInputValidator() {
			public String isValid(String newText) {
				return (newText == null || newText.length() == 0) ? " " : null; //$NON-NLS-1$
			}
		};
		InputDialog dialog = new InputDialog(shell, title, message, proposal, inputValidator);

		if (dialog.open() != Window.CANCEL) {
			String name = dialog.getValue();
			if (name == null)
				return null;
			name = name.trim();
			return (name.length() == 0) ? null : name;
		} else {
			return null;
		}
	}
}
