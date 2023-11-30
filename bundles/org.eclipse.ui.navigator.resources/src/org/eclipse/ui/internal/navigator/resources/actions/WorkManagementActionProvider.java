/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
package org.eclipse.ui.internal.navigator.resources.actions;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.AddBookmarkAction;
import org.eclipse.ui.actions.AddTaskAction;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;

/**
 * Supports Add Task and Add Bookmark actions.
 *
 * @since 3.2
 */
public class WorkManagementActionProvider extends CommonActionProvider {

	private AddTaskAction addTaskAction;

	private AddBookmarkAction addBookmarkAction;

	@Override
	public void init(ICommonActionExtensionSite aSite) {
		final Shell shell = aSite.getViewSite().getShell();
		IShellProvider sp = () -> shell;
		addBookmarkAction = new AddBookmarkAction(sp, true);
		addTaskAction = new AddTaskAction(sp);
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);
		actionBars.setGlobalActionHandler(IDEActionFactory.BOOKMARK.getId(), addBookmarkAction);
		actionBars.setGlobalActionHandler(IDEActionFactory.ADD_TASK.getId(), addTaskAction);
	}

	@Override
	public void setContext(ActionContext context) {
		super.setContext(context);
		if (context != null && context.getSelection() instanceof IStructuredSelection) {
			IStructuredSelection sSel = (IStructuredSelection) context.getSelection();
			addBookmarkAction.selectionChanged(sSel);
			addTaskAction.selectionChanged(sSel);
		} else {
			addBookmarkAction.selectionChanged(StructuredSelection.EMPTY);
			addTaskAction.selectionChanged(StructuredSelection.EMPTY);
		}
	}

}
