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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.externaltools.internal.ant.view.AntView;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsImages;
import org.eclipse.ui.externaltools.internal.model.IExternalToolsHelpContextIds;
import org.eclipse.ui.externaltools.internal.ui.IExternalToolsUIConstants;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.texteditor.IUpdate;

public class DeactivateTargetAction extends Action implements IUpdate {
	
	private AntView view;

	public DeactivateTargetAction(AntView view) {
		super(AntViewActionMessages.getString("DeactivateTargetAction.Deactivate"), ExternalToolsImages.getImageDescriptor(IExternalToolsUIConstants.IMG_REMOVE)); //$NON-NLS-1$
		setDescription(AntViewActionMessages.getString("DeactivateTargetAction.Deactivate_selected")); //$NON-NLS-1$
		this.view= view;
		WorkbenchHelp.setHelp(this, IExternalToolsHelpContextIds.DEACTIVATE_TARGET_ACTION);
	}
	
	public void run() {
		view.deactivateSelectedTargets();
	}
	
	/**
	 * Updates the enablement of this action based on the user's selection
	 */
	public void update() {
		IStructuredSelection selection= (IStructuredSelection) view.getTargetViewer().getSelection();
		setEnabled(!selection.isEmpty());
	}
}
