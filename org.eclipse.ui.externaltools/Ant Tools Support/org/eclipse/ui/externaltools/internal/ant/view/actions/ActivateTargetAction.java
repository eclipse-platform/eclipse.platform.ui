package org.eclipse.ui.externaltools.internal.ant.view.actions;

/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/

import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.externaltools.internal.ant.view.AntView;
import org.eclipse.ui.externaltools.internal.ant.view.elements.TargetNode;
import org.eclipse.ui.externaltools.internal.model.IExternalToolsHelpContextIds;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Action for activating a target node selected in the ant view
 */
public class ActivateTargetAction extends Action implements IUpdate {
	private AntView view;
	
	public ActivateTargetAction(AntView view) {
		super(AntViewActionMessages.getString("ActivateTargetAction.Activate_Target_1")); //$NON-NLS-1$
		setDescription(AntViewActionMessages.getString("ActivateTargetAction.Activate_selected")); //$NON-NLS-1$
		this.view= view;
		WorkbenchHelp.setHelp(this, IExternalToolsHelpContextIds.ACTIVATE_TARGET_ACTION);
	}
	
	public void run() {
		view.activateSelectedTargets();
	}
	
	/**
	 * Updates the enablement of this action based on the user's selection
	 */
	public void update() {
		boolean enabled= true;
		IStructuredSelection selection= (IStructuredSelection) view.getProjectViewer().getSelection();
		if (selection.isEmpty()) {
			enabled= false;
		}
		Iterator iter= selection.iterator();
		while (iter.hasNext()) {
			Object data= iter.next();
			if (!(data instanceof TargetNode) || ((TargetNode) data).isErrorNode()) {
				enabled= false;
				break;
			}
		}
		setEnabled(enabled);
	}
	
}
