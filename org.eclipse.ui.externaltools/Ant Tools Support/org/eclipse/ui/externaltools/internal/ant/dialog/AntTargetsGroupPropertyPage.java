package org.eclipse.ui.externaltools.internal.ant.dialog;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.ui.externaltools.dialog.ExternalToolGroupPropertyPage;
import org.eclipse.ui.externaltools.internal.model.IHelpContextIds;
import org.eclipse.ui.externaltools.model.ExternalTool;

/**
 * Property page to show the Ant targets group.
 */
public class AntTargetsGroupPropertyPage extends ExternalToolGroupPropertyPage {

	/**
	 * Creates the Ant targets group property page.
	 */
	public AntTargetsGroupPropertyPage() {
		super(new AntTargetsGroup(), IHelpContextIds.ANT_TARGETS_PROPERTY_PAGE);
	}

	/*
	 * (non-Javadoc)
	 * Method declared on DialogPage.
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		
		ExternalTool tool = getExternalTool();
		if (tool != null) {
			AntTargetsGroup group = (AntTargetsGroup) getGroup();
			if (group != null)
				group.setFileLocation(tool.getLocation());
		}
	}
}
