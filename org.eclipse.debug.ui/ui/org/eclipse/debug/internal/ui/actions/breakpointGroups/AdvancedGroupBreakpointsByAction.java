/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.breakpointGroups;

import java.util.List;

import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointsView;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;

/**
 * Action that opens a dialog to select which breakpoint
 * container factories should be applies to the breakpoints
 * view.
 */
public class AdvancedGroupBreakpointsByAction extends Action {
	
	private BreakpointsView fView;
	
	public AdvancedGroupBreakpointsByAction(BreakpointsView view) {
		fView= view;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
	    GroupBreakpointsByDialog dialog = new GroupBreakpointsByDialog(fView);
		if (dialog.open() == Dialog.OK) {
			List selectedContainers = dialog.getSelectedContainers();
			fView.setBreakpointContainerFactories(selectedContainers);
		}
	}

}
