/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.wizards.ModeWizard;

/**
 * TagAction tags the selected resources with a version tag specified by the user.
 */
public class SetKeywordSubstitutionAction extends WorkspaceTraversalAction {

	/*
	 * @see IActionDelegate#run(IAction)
	 */
	public void execute(IAction action) {
	    ModeWizard.run(getShell(), getSelectedResources());
	}
	
	public String getId() {
		return ICVSUIConstants.CMD_SETFILETYPE;
	}
}
