/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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
import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.IShellProvider;
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
 * @noextend This class is not intended to be subclassed by clients.
 */
public class AddBookmarkAction extends SelectionListenerAction {

	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID + ".AddBookmarkAction"; //$NON-NLS-1$

	/**
	 * The IShellProvider in which to show any dialogs.
	 */
	private IShellProvider shellProvider;

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
	 * @deprecated see {@link #AddBookmarkAction(IShellProvider, boolean)} 
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
	 * @deprecated see {@link #AddBookmarkAction(IShellProvider, boolean)} 
	 */
	public AddBookmarkAction(final Shell shell, boolean promptForName) {
		super(IDEWorkbenchMessages.AddBookmarkLabel);
		Assert.isNotNull(shell);
		shellProvider = new IShellProvider() {
			public Shell getShell() {
				return shell;
			} };
			
		initAction(promptForName);
	}
	
	/**
	 * Creates a new bookmark action.
	 * 
	 * @param provider
	 *            the shell provider for any dialogs. Must not be
	 *            <code>null</code>
	 * @param promptForName
	 *            whether to ask the user for the bookmark name
	 * @since 3.4
	 */
	public AddBookmarkAction(IShellProvider provider, boolean promptForName) {
		super(IDEWorkbenchMessages.AddBookmarkLabel);
		Assert.isNotNull(provider);
		shellProvider = provider;
		initAction(promptForName);
	}

	/**
	 * @param promptForName
	 */
	private void initAction(boolean promptForName) {
		this.promptForName = promptForName;
		setToolTipText(IDEWorkbenchMessages.AddBookmarkToolTip);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				IIDEHelpContextIds.ADD_BOOKMARK_ACTION);
		setId(ID);
	}

	/*
	 * (non-Javadoc) Method declared on IAction.
	 */
	public void run() {
		if (getSelectedResources().isEmpty())
			return;

		IResource resource= (IResource)getSelectedResources().get(0);
		if (resource != null) {
			if (promptForName) {
				BookmarkPropertiesDialog dialog= new BookmarkPropertiesDialog(shellProvider.getShell());
				dialog.setResource(resource);
				dialog.open();
			} else {
				Map attrs= new HashMap();
				attrs.put(IMarker.MESSAGE, resource.getName());
				CreateMarkersOperation op= new CreateMarkersOperation(IMarker.BOOKMARK, attrs, resource, BookmarkMessages.CreateBookmark_undoText);
				try {
					PlatformUI.getWorkbench().getOperationSupport().getOperationHistory().execute(op, null, WorkspaceUndoUtil.getUIInfoAdapter(shellProvider.getShell()));
				} catch (ExecutionException e) {
					IDEWorkbenchPlugin.log(null, e); // We don't care
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
		return super.updateSelection(selection) && getSelectedResources().size() == 1;
	}
}
