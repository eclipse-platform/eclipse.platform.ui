package org.eclipse.ui.externaltools.dialog;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.ui.externaltools.group.ExternalToolOptionGroup;
import org.eclipse.ui.externaltools.internal.model.IHelpContextIds;

/**
 * Property page to show the main group.
 */
public class ExternalToolOptionGroupPropertyPage extends ExternalToolGroupPropertyPage {

	/**
	 * Creates the main group property page.
	 */
	public ExternalToolOptionGroupPropertyPage() {
		super(new ExternalToolOptionGroup(), IHelpContextIds.TOOL_OPTION_PROPERTY_PAGE);
	}
}
