package org.eclipse.ui.externaltools.internal.program.dialog;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.externaltools.dialog.ExternalToolNewWizard;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;

/**
 * Wizard that will create a new external tool of type program.
 */
public class ProgramExternalToolNewWizard extends ExternalToolNewWizard {
	
	/**
	 * Creates the wizard for a new external tool
	 */
	public ProgramExternalToolNewWizard() {
		super(IExternalToolConstants.TOOL_TYPE_PROGRAM);
	}
	
	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public void addPages() {
		addMainPage();
		addOptionPage();
		addRefreshPage();
	}

	/* (non-Javadoc)
	 * Method declared on ExternalToolNewWizard.
	 */
	protected ImageDescriptor getDefaultImageDescriptor() {
		return ExternalToolsPlugin.getDefault().getImageDescriptor("icons/full/wizban/program_wiz.gif"); //$NON-NLS-1$
	}
}
