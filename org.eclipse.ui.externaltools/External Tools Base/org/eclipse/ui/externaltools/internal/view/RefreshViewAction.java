package org.eclipse.ui.externaltools.internal.view;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.IHelpContextIds;
import org.eclipse.ui.externaltools.internal.model.ToolMessages;
import org.eclipse.ui.externaltools.model.ExternalToolStorage;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Action that will refresh the contents of the
 * external tool view.
 */
public class RefreshViewAction extends Action {
	private IWorkbenchPage page;

	/**
	 * Create an action to refresh the view
	 */
	public RefreshViewAction(IWorkbenchPage page) {
		super();
		this.page = page;
		setText(ToolMessages.getString("RefreshViewAction.text")); //$NON-NLS-1$
		setToolTipText(ToolMessages.getString("RefreshViewAction.toolTip")); //$NON-NLS-1$
		setHoverImageDescriptor(ExternalToolsPlugin.getDefault().getImageDescriptor("icons/full/clcl16/refresh.gif")); //$NON-NLS-1$
		setImageDescriptor(ExternalToolsPlugin.getDefault().getImageDescriptor("icons/full/elcl16/refresh.gif")); //$NON-NLS-1$
		setDisabledImageDescriptor(ExternalToolsPlugin.getDefault().getImageDescriptor("icons/full/dlcl16/refresh.gif")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, IHelpContextIds.REFRESH_VIEW_ACTION);
	}

	/* (non-Javadoc)
	 * Method declared on Action.
	 */
	public void run() {
		Shell shell = page.getWorkbenchWindow().getShell();
		ExternalToolStorage.refreshTools(shell);
	}
}
