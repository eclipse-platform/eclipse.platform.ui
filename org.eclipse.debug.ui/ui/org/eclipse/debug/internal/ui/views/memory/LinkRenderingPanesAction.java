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
package org.eclipse.debug.internal.ui.views.memory;

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * Action to link/unlink rendering view panes
 */
public class LinkRenderingPanesAction implements IViewActionDelegate{

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		IMemoryBlockViewSynchronizer sync = DebugUIPlugin.getDefault().getMemoryBlockViewSynchronizer();
		sync.setEnabled(!sync.isEnabled());
		updateActionState(action);
	}

	/**
	 * @param action
	 */
	private void updateActionState(IAction action) {
		IMemoryBlockViewSynchronizer sync = DebugUIPlugin.getDefault().getMemoryBlockViewSynchronizer();
		if (sync.isEnabled())
			action.setChecked(true);
		else
			action.setChecked(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		updateActionState(action);		
	}
}
