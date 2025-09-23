/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
	private final IShellProvider shellProvider;

	/**
	 * Creates a new bookmark action. By default, prompts the user for the
	 * bookmark name.
	 *
	 * @param shell
	 *            the shell for any dialogs
	 * @deprecated see {@link #AddBookmarkAction(IShellProvider, boolean)}
	 */
	@Deprecated
	public AddBookmarkAction(Shell shell) {
		this(shell, true);
	}

	/**
	 * Creates a new bookmark action.
	 *
	 * @param shell         the shell for any dialogs
	 * @param promptForName whether to ask the user for the bookmark name (IGNORED)
	 * @deprecated see {@link #AddBookmarkAction(IShellProvider, boolean)}
	 */
	@Deprecated
	public AddBookmarkAction(final Shell shell, boolean promptForName) {
		super(IDEWorkbenchMessages.AddBookmarkLabel);
		Assert.isNotNull(shell);
		shellProvider = () -> shell;

		initAction();
	}

	/**
	 * Creates a new bookmark action.
	 *
	 * @param provider      the shell provider for any dialogs. Must not be
	 *                      <code>null</code>
	 * @param promptForName whether to ask the user for the bookmark name (IGNORED)
	 * @since 3.4
	 */
	public AddBookmarkAction(IShellProvider provider, boolean promptForName) {
		super(IDEWorkbenchMessages.AddBookmarkLabel);
		Assert.isNotNull(provider);
		shellProvider = provider;
		initAction();
	}

	private void initAction() {
		setToolTipText(IDEWorkbenchMessages.AddBookmarkToolTip);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				IIDEHelpContextIds.ADD_BOOKMARK_ACTION);
		setId(ID);
	}

	@Override
	public void run() {
		if (getSelectedResources().isEmpty()) {
			return;
		}

		IResource resource= getSelectedResources().get(0);
		if (resource != null) {
			Map<String, String> attrs = new HashMap<>();
			attrs.put(IMarker.MESSAGE, resource.getName());
			CreateMarkersOperation op = new CreateMarkersOperation(IMarker.BOOKMARK, attrs, resource,
					BookmarkMessages.CreateBookmark_undoText);
			try {
				PlatformUI.getWorkbench().getOperationSupport().getOperationHistory().execute(op, null,
						WorkspaceUndoUtil.getUIInfoAdapter(shellProvider.getShell()));
			} catch (ExecutionException e) {
				IDEWorkbenchPlugin.log(null, e); // We don't care
			}
		}

	}

	/**
	 * The <code>AddBookmarkAction</code> implementation of this
	 * <code>SelectionListenerAction</code> method enables the action only if
	 * the selection is not empty and contains just file resources.
	 */
	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		// @issue typed selections
		return super.updateSelection(selection) && getSelectedResources().size() == 1;
	}
}
