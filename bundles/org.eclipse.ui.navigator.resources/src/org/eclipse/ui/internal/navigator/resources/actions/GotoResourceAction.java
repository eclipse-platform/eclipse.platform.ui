/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.navigator.INavigatorHelpContextIds;

/**
 * Implements the go to resource action. Opens a dialog and set the navigator
 * selection with the resource selected by the user.
 */
public class GotoResourceAction extends Action {

	protected Shell shell;

	protected TreeViewer viewer;

	public GotoResourceAction(Shell shell, TreeViewer viewer) {
		this.shell = shell;
		this.viewer = viewer;
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				INavigatorHelpContextIds.GOTO_RESOURCE_ACTION);
	}

	/**
	 * Collect all resources in the workbench open a dialog asking the user to
	 * select a resource and change the selection in the navigator.
	 */
	@Override
	public void run() {
		GotoResourceDialog dialog = new GotoResourceDialog(shell,
				ResourcesPlugin.getWorkspace().getRoot(), IResource.FILE
						| IResource.FOLDER | IResource.PROJECT);
		dialog.open();
		Object[] result = dialog.getResult();
		if (result == null || result.length == 0
				|| !(result[0] instanceof IResource)) {
			return;
		}

		IResource selection = (IResource) result[0];
		viewer.setSelection(new StructuredSelection(selection), true);
	}
}
