/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.actions;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.team.internal.ccvs.core.client.Command.KSubstOption;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.ResizableWizardDialog;
import org.eclipse.team.internal.ccvs.ui.wizards.KSubstWizard;

/**
 * TagAction tags the selected resources with a version tag specified by the user.
 */
public class SetKeywordSubstitutionAction extends WorkspaceAction {
	private KSubstOption previousOption = null; // automatic

	/*
	 * @see IActionDelegate#run(IAction)
	 */
	public void execute(IAction action) {
		final IResource[] resources = getSelectedResources();
		KSubstWizard wizard = new KSubstWizard(resources, IResource.DEPTH_INFINITE, previousOption);
		WizardDialog dialog = new ResizableWizardDialog(getShell(), wizard);
		wizard.setParentDialog(dialog);
		dialog.setMinimumPageSize(350, 250);
		dialog.open();
		previousOption = wizard.getKSubstOption();
	}
	
	public String getId() {
		return ICVSUIConstants.CMD_SETFILETYPE;
	}
}
