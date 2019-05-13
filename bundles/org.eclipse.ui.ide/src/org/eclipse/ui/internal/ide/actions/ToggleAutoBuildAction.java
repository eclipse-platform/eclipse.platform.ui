/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.actions;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;

/**
 * Action for toggling autobuild on or off.
 */
public class ToggleAutoBuildAction extends Action implements
		ActionFactory.IWorkbenchAction {
	private IWorkbenchWindow window;

	/**
	 * Creates a new ToggleAutoBuildAction
	 * @param window The window for parenting dialogs associated with this action
	 */
	public ToggleAutoBuildAction(IWorkbenchWindow window) {
		super(IDEWorkbenchMessages.Workbench_buildAutomatically);
		this.window = window;
		setChecked(ResourcesPlugin.getWorkspace().isAutoBuilding());
	}

	@Override
	public void dispose() {
		window = null;
	}

	@Override
	public void run() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceDescription description = workspace.getDescription();
		description.setAutoBuilding(!description.isAutoBuilding());
		try {
			workspace.setDescription(description);
		} catch (CoreException e) {
			ErrorDialog.openError(window.getShell(), null, null, e.getStatus());
		}
	}
}
