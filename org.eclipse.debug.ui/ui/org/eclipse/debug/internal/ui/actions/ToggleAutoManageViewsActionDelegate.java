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

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.internal.ui.views.launch.LaunchView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewPart;

/**
 * 
 */
public class ToggleAutoManageViewsActionDelegate extends AbstractDebugActionDelegate {
	private LaunchView launchView= null;
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.AbstractDebugActionDelegate#doAction(java.lang.Object)
	 */
	protected void doAction(Object element) throws DebugException {
		IAction action= getAction();
		if (action != null && launchView != null) {
			launchView.setAutoManageViews(action.isChecked());
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {
		super.init(view);
		if (view instanceof LaunchView) {
			launchView= (LaunchView) view;
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.AbstractDebugActionDelegate#initialize(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	protected boolean initialize(IAction action, ISelection selection) {
		if (launchView != null) {
			action.setChecked(launchView.isAutoManageViews());
		}
		return super.initialize(action, selection);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.AbstractDebugActionDelegate#update(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	protected void update(IAction action, ISelection s) {
		action.setEnabled(true);
	}
}
