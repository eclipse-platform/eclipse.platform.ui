package org.eclipse.ui.externaltools.internal.ant.dialog;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.ui.externaltools.dialog.ExternalToolOptionGroupPropertyPage;
import org.eclipse.ui.externaltools.group.ExternalToolOptionGroup;
import org.eclipse.ui.externaltools.internal.model.ToolMessages;

/**
 * Property page to show the options group. Sets the
 * appropriate label to prompt for targets and arguments.
 */
public class AntOptionGroupPropertyPage extends ExternalToolOptionGroupPropertyPage {

	/**
	 * Creates the option group property page.
	 */
	public AntOptionGroupPropertyPage() {
		super();
		((ExternalToolOptionGroup) getGroup()).setPromptForArgumentLabel(ToolMessages.getString("AntOptionGroupPropertyPage.promptForArgumentLabel")); //$NON-NLS-1$
	}
}
