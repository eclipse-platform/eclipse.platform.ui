/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
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
 *     Sebastian Davids <sdavids@gmx.de> - Images for menu items (27481)
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.resources.actions;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.actions.MoveResourceAction;
import org.eclipse.ui.actions.RenameResourceAction;
import org.eclipse.ui.actions.TextActionHandler;
import org.eclipse.ui.ide.ResourceSelectionUtil;
import org.eclipse.ui.navigator.ICommonMenuConstants;

/**
 * This is the action group for refactor actions.
 *
 * @since 2.0
 */
public class RefactorActionGroup extends ActionGroup {

	private RenameResourceAction renameAction;

	private MoveResourceAction moveAction;

	private Shell shell;

	private Tree tree;

	private TextActionHandler textActionHandler;

	public RefactorActionGroup(Shell aShell, Tree aTree) {
		shell = aShell;
		tree = aTree;
		makeActions();
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();

		boolean anyResourceSelected = !selection.isEmpty()
				&& ResourceSelectionUtil.allResourcesAreOfType(selection, IResource.PROJECT | IResource.FOLDER | IResource.FILE);

		if (anyResourceSelected) {
			moveAction.selectionChanged(selection);
			menu.appendToGroup(ICommonMenuConstants.GROUP_REORGANIZE, moveAction);
			renameAction.selectionChanged(selection);
			menu.insertAfter(moveAction.getId(), renameAction);
		}
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		if (textActionHandler != null) {
			textActionHandler.dispose();
		}
		textActionHandler = new TextActionHandler(actionBars, true);
		renameAction.setTextActionHandler(textActionHandler);

		updateActionBars();

		actionBars.setGlobalActionHandler(ActionFactory.MOVE.getId(), moveAction);
		actionBars.setGlobalActionHandler(ActionFactory.RENAME.getId(), renameAction);
	}

	/**
	 * Handles a key pressed event by invoking the appropriate action.
	 *
	 * @param event
	 *            The Key Event
	 */
	public void handleKeyPressed(KeyEvent event) {

		if (event.keyCode == SWT.F2 && event.stateMask == 0) {
			if (renameAction.isEnabled()) {
				renameAction.run();
			}

			// Swallow the event.
			event.doit = false;
		}
	}

	protected void makeActions() {
		IShellProvider sp = () -> shell;

		moveAction = new MoveResourceAction(sp);
		moveAction.setActionDefinitionId(IWorkbenchCommandConstants.FILE_MOVE);

		renameAction = new RenameResourceAction(sp, tree);
		renameAction.setActionDefinitionId(IWorkbenchCommandConstants.FILE_RENAME);
	}

	@Override
	public void updateActionBars() {
		IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();

		moveAction.selectionChanged(selection);
		renameAction.selectionChanged(selection);
	}

	@Override
	public void dispose() {
		if (textActionHandler != null) {
			textActionHandler.dispose();
		}
		textActionHandler = null;
		super.dispose();
	}
}
