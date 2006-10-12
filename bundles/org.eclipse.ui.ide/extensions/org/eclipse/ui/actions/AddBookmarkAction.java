/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *        IBM Corporation - initial API and implementation 
 *   Sebastian Davids <sdavids@gmx.de>
 *     - Fix for bug 20510 - Add Bookmark action has wrong label in navigator or
 *       packages view
 *******************************************************************************/
package org.eclipse.ui.actions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.undo.CreateMarkersOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.views.bookmarkexplorer.BookmarkMessages;
import org.eclipse.ui.views.bookmarkexplorer.BookmarkPropertiesDialog;

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
	 * @param shell
	 *            the shell for any dialogs
	 */
	public AddBookmarkAction(Shell shell) {
		this(shell, true);
	}

	/**
	 * Creates a new bookmark action.
	 * 
	 * @param shell
	 *            the shell for any dialogs
	 * @param promptForName
	 *            whether to ask the user for the bookmark name
	 */
	public AddBookmarkAction(Shell shell, boolean promptForName) {
		super(IDEWorkbenchMessages.AddBookmarkLabel);
		setId(ID);
		if (shell == null) {
			throw new IllegalArgumentException();
		}
		this.shell = shell;
		this.promptForName = promptForName;
		setToolTipText(IDEWorkbenchMessages.AddBookmarkToolTip);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				IIDEHelpContextIds.ADD_BOOKMARK_ACTION);
	}

	/*
	 * (non-Javadoc) Method declared on IAction.
	 */
	public void run() {
		IStructuredSelection selection = getStructuredSelection();
		for (Iterator i = selection.iterator(); i.hasNext();) {
			Object o = i.next();
			IFile file = null;
			if (o instanceof IFile) {
				file = (IFile) o;
			} else if (o instanceof IAdaptable) {
				Object resource = ((IAdaptable) o).getAdapter(IResource.class);
				if (resource instanceof IFile) {
					file = (IFile) resource;
				}
			}
			if (file != null) {
				if (promptForName) {
					BookmarkPropertiesDialog dialog = new BookmarkPropertiesDialog(
							shell);
					dialog.setResource(file);
					dialog.open();
				} else {
					Map attrs = new HashMap();
					attrs.put(IMarker.MESSAGE, file.getName());
					CreateMarkersOperation op = new CreateMarkersOperation(
							IMarker.BOOKMARK, attrs, file,
							BookmarkMessages.CreateBookmark_undoText);
					try {
						PlatformUI.getWorkbench().getOperationSupport()
								.getOperationHistory().execute(op, null,
										WorkspaceUndoUtil.getUiInfoAdapter(shell));
					} catch (ExecutionException e) {
						IDEWorkbenchPlugin.log(null, e); // We don't care
					}
				}
			}
		}

	}

	/**
	 * The <code>AddBookmarkAction</code> implementation of this
	 * <code>SelectionListenerAction</code> method enables the action only if
	 * the selection is not empty and contains just file resources.
	 */
	protected boolean updateSelection(IStructuredSelection selection) {
		// @issue typed selections
		return super.updateSelection(selection) && !selection.isEmpty()
				&& selectionIsOfType(IResource.FILE);
	}
}
