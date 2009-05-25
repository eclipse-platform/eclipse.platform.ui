/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * 
 */
public class WorkManagementActionProvider extends CommonActionProvider {

	private AddTaskAction addTaskAction;

	private AddBookmarkAction addBookmarkAction;

	public void init(ICommonActionExtensionSite aSite) {
		final Shell shell = aSite.getViewSite().getShell();
		IShellProvider sp = new IShellProvider() {
			public Shell getShell() {
				return shell;
			}
		};
		addBookmarkAction = new AddBookmarkAction(sp, true);
		addTaskAction = new AddTaskAction(sp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.actions.ActionGroup#fillActionBars(org.eclipse.ui.IActionBars
	 * )
	 */
	public void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);
		actionBars.setGlobalActionHandler(IDEActionFactory.BOOKMARK.getId(), addBookmarkAction);
		actionBars.setGlobalActionHandler(IDEActionFactory.ADD_TASK.getId(), addTaskAction);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.actions.ActionGroup#setContext(org.eclipse.ui.actions.
	 * ActionContext)
	 */
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
