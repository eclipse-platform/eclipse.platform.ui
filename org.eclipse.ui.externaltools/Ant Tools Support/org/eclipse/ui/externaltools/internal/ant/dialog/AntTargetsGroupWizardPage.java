package org.eclipse.ui.externaltools.internal.ant.dialog;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.ui.externaltools.dialog.ExternalToolGroupWizardPage;
import org.eclipse.ui.externaltools.group.ExternalToolMainGroup;

/**
 * Wizard page that will display an <code>AntTargetsGroup</code>
 * as its content.
 */
public class AntTargetsGroupWizardPage extends ExternalToolGroupWizardPage {
	private ExternalToolMainGroup mainGroup;
	
	/**
	 * Creates a wizard page for the specified group.
	 * 
	 * @param pageName name given to the page
	 * @param group the ant targets group to display
	 * @param mainGroup the main group for access to the location
	 * @param helpContextId the help context id for this page
	 */
	public AntTargetsGroupWizardPage(String pageName, AntTargetsGroup group, ExternalToolMainGroup mainGroup, String helpContextId) {
		super(pageName, group, helpContextId);
		this.mainGroup = mainGroup;
	}

	/*
	 * (non-Javadoc)
	 * Method declared on DialogPage.
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		AntTargetsGroup group = (AntTargetsGroup) getGroup();
		if (group != null && mainGroup != null) {
			group.setFileLocation(mainGroup.getLocationFieldValue());
		}
	}
}
