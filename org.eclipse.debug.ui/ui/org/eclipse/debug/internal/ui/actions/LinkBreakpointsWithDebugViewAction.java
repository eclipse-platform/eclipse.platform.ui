/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
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
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointsView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * An action which toggles the "Link with Debug View" preference on a
 * breakpoints view.
 */
public class LinkBreakpointsWithDebugViewAction extends Action {
	
	private BreakpointsView fView;
	
	public LinkBreakpointsWithDebugViewAction(BreakpointsView view) {
		super(ActionMessages.getString("LinkBreakpointsWithDebugViewAction.0")); //$NON-NLS-1$
		setToolTipText(ActionMessages.getString("LinkBreakpointsWithDebugViewAction.1")); //$NON-NLS-1$
		setDescription(ActionMessages.getString("LinkBreakpointsWithDebugViewAction.2")); //$NON-NLS-1$
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_ACT_SYNCED));
		WorkbenchHelp.setHelp(this, IDebugHelpContextIds.LINK_BREAKPOINTS_WITH_DEBUG_ACTION);
		fView= view;
		setChecked(view.isTrackingSelection());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		fView.setTrackSelection(isChecked());
	}

}
