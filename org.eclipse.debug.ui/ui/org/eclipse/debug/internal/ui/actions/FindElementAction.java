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
package org.eclipse.debug.internal.ui.actions;

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.viewers.AsynchronousTreeNavigationDialog;
import org.eclipse.debug.internal.ui.viewers.AsynchronousTreeNavigationModel;
import org.eclipse.debug.internal.ui.viewers.AsynchronousTreeViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IUpdate;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;

/**
 * Action which prompts the user to find/navigate to an element in an async tree.
 */
public class FindElementAction extends Action implements IUpdate {
	
	private AsynchronousTreeViewer fViewer;

	public FindElementAction(AsynchronousTreeViewer viewer) {
		setText(ActionMessages.FindAction_0);
		setId(DebugUIPlugin.getUniqueIdentifier() + ".FindElementAction"); //$NON-NLS-1$
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugHelpContextIds.FIND_ELEMENT_ACTION);
		setActionDefinitionId(IWorkbenchActionDefinitionIds.FIND_REPLACE);
		fViewer = viewer;
	}

	public void run() {
		AsynchronousTreeNavigationModel model = new AsynchronousTreeNavigationModel(fViewer);
		AsynchronousTreeNavigationDialog dialog = new AsynchronousTreeNavigationDialog(model); 
		dialog.setTitle(ActionMessages.FindDialog_3);
		dialog.setMessage(ActionMessages.FindDialog_1);
		dialog.open();
		model.dispose();
	}

	public void update() {
		setEnabled(fViewer.getInput() != null);
	}
	
}
