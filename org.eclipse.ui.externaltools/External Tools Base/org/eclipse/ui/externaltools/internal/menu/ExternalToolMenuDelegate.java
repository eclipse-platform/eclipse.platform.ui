package org.eclipse.ui.externaltools.internal.menu;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.debug.ui.actions.AbstractLaunchHistoryAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;

/**
 * This action delegate is responsible for producing the
 * Run > External Tools sub menu contents, which includes
 * an items to run last tool, favorite tools, and show the
 * external tools view. Default action is to run the last tool
 * if one exist.
 */
public class ExternalToolMenuDelegate extends AbstractLaunchHistoryAction implements IWorkbenchWindowPulldownDelegate2, IMenuCreator {
	
	/**
	 * Creates the action delegate
	 */
	public ExternalToolMenuDelegate() {
		super(IExternalToolConstants.ID_EXTERNAL_TOOLS_LAUNCH_GROUP);
	}

}
