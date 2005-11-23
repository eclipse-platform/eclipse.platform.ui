/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.context;

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.internal.ui.viewers.AsynchronousTreeViewer;
import org.eclipse.debug.internal.ui.viewers.AsynchronousTreeNavigationDialog;
import org.eclipse.debug.internal.ui.viewers.AsynchronousTreeNavigationModel;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IUpdate;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;

/**
 * Action which prompts the user to help them find a variable in the variables
 * view.
 */
public class FindVariableAction extends Action implements IUpdate {
	
	private AsynchronousTreeViewer fViewer;

	public FindVariableAction(AsynchronousTreeViewer viewer) {
		setText(ActionMessages.FindVariableAction_0);
		setId(DebugUIPlugin.getUniqueIdentifier() + ".FindVariableAction"); //$NON-NLS-1$
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugHelpContextIds.FIND_VARIABLE_ACTION);
		setActionDefinitionId(IWorkbenchActionDefinitionIds.FIND_REPLACE);
		fViewer = viewer;
	}

	public void run() {
		AsynchronousTreeNavigationModel model = new AsynchronousTreeNavigationModel(fViewer);
		AsynchronousTreeNavigationDialog dialog = new AsynchronousTreeNavigationDialog(model); 
		dialog.setTitle(ActionMessages.FindVariableDialog_3);
		dialog.setMessage(ActionMessages.FindVariableDialog_1);
		dialog.open();
		model.dispose();
	}

	public void update() {
		setEnabled(fViewer.getInput() != null);
	}
	
}
