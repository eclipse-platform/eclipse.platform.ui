/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions;

import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.views.launch.LaunchView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * An action which toggles whether or not a launch view automatically manages views.
 */
public class AutoManageViewsAction extends Action implements IUpdate {
	private LaunchView launchView= null;
	
	public AutoManageViewsAction(LaunchView view) {
		super(ActionMessages.getString("AutoManageViewsAction.0")); //$NON-NLS-1$
		setToolTipText(ActionMessages.getString("AutoManageViewsAction.1")); //$NON-NLS-1$
		setDescription(ActionMessages.getString("AutoManageViewsAction.2")); //$NON-NLS-1$
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_ACT_SYNCED));
		WorkbenchHelp.setHelp(this, IDebugHelpContextIds.AUTO_MANAGE_VIEWS_ACTION);
		launchView= view;
		setChecked(launchView.isAutoManageViews());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		launchView.setAutoManageViews(isChecked());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		setChecked(launchView.isAutoManageViews());
	}
}
