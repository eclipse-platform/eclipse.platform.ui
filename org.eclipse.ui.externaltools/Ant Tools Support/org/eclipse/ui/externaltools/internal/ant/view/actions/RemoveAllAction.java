package org.eclipse.ui.externaltools.internal.ant.view.actions;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.jface.action.Action;
import org.eclipse.ui.externaltools.internal.ant.view.AntView;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsImages;
import org.eclipse.ui.externaltools.internal.model.IExternalToolsHelpContextIds;
import org.eclipse.ui.externaltools.internal.ui.IExternalToolsUIConstants;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.texteditor.IUpdate;

public class RemoveAllAction extends Action implements IUpdate {
	private AntView view;
	
	public RemoveAllAction(AntView view) {
		super(AntViewActionMessages.getString("RemoveAllAction.Remove_All"), ExternalToolsImages.getImageDescriptor(IExternalToolsUIConstants.IMG_REMOVE_ALL)); //$NON-NLS-1$
		setDescription(AntViewActionMessages.getString("RemoveAllAction.Remove_All")); //$NON-NLS-1$
		setToolTipText(AntViewActionMessages.getString("RemoveAllAction.Remove_All")); //$NON-NLS-1$
		this.view= view;
		WorkbenchHelp.setHelp(this, IExternalToolsHelpContextIds.REMOVE_ALL_ACTION);
	}
	
	public void run() {
		view.removeAllProjects();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		setEnabled(view.getProjectViewer().getTree().getItemCount() != 0);
	}
}