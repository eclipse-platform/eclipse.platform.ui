/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.tasklist;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * This is the base class of all the local actions used in the task list view.
 */
abstract class TaskAction extends Action {

	private TaskList taskList;

	/**
	 * TaskAction constructor.
	 */
	protected TaskAction(TaskList tasklist, String id) {
		super();
		this.taskList = tasklist;
		setId(id);
	}

	/**
	 * Returns the shell to use within actions.
	 */
	protected Shell getShell() {
		return taskList.getSite().getShell();
	}

	/**
	 * Returns the task list viewer.
	 */
	protected TaskList getTaskList() {
		return taskList;
	}

	/**
	 * Stores the current state value of this toggle action into the dialog
	 * store using action ID as a key.
	 */
	protected void storeValue() {
		IDialogSettings workbenchSettings = TaskList.getPlugin()
				.getDialogSettings();
		IDialogSettings settings = workbenchSettings.getSection("TaskAction");//$NON-NLS-1$
		if (settings == null) {
			settings = workbenchSettings.addNewSection("TaskAction");//$NON-NLS-1$
		}
		settings.put(getId(), isChecked());
	}

	/**
	 * Execute the specified undoable operation
	 */
	void execute(IUndoableOperation operation, String title,
			IProgressMonitor monitor, IAdaptable uiInfo) {
		try {
			PlatformUI.getWorkbench().getOperationSupport()
					.getOperationHistory().execute(operation, monitor, uiInfo);
		} catch (ExecutionException e) {
			if (e.getCause() instanceof CoreException) {
				ErrorDialog
						.openError(WorkspaceUndoUtil.getShell(uiInfo), title,
								null, ((CoreException) e.getCause())
										.getStatus());
			} else {
				IDEWorkbenchPlugin.log(title, e);
			}
		}
	}

}
