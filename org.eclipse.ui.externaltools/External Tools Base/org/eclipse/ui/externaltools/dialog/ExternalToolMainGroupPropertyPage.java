package org.eclipse.ui.externaltools.dialog;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.ui.externaltools.group.ExternalToolMainGroup;
import org.eclipse.ui.externaltools.internal.model.IHelpContextIds;

/**
 * Property page to show the main group.
 */
public class ExternalToolMainGroupPropertyPage extends ExternalToolGroupPropertyPage {

	/**
	 * Creates the main group property page.
	 */
	public ExternalToolMainGroupPropertyPage() {
		super(new ExternalToolMainGroup(), IHelpContextIds.TOOL_MAIN_PROPERTY_PAGE);
	}
}
