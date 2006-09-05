/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchWindow;

/**
 * Toggle the visibility of the coolbar/perspective bar.
 * 
 * @since 3.3
 */
public class ToggleCoolbarVisibilityAction extends Action implements
		IWorkbenchAction {

	private WorkbenchWindow window;

	/**
	 * Create a new instance of this action.
	 * 
	 * @param window
	 */
	public ToggleCoolbarVisibilityAction(IWorkbenchWindow window) {
		super();
		this.window = (WorkbenchWindow) window;
		setActionDefinitionId("org.eclipse.ui.ToggleCoolbarAction"); //$NON-NLS-1$
		setText(isVisible() ? WorkbenchMessages.ToggleCoolbarVisibilityAction_hide_text
				: WorkbenchMessages.ToggleCoolbarVisibilityAction_show_text);
		setToolTipText(WorkbenchMessages.ToggleCoolbarVisibilityAction_toolTip);
		
		window.getWorkbench().getHelpSystem().setHelp(this,
				IWorkbenchHelpContextIds.TOGGLE_COOLBAR_ACTION);
	}
	
	

	/**
	 * Return whether either the coolbar or perspective bar is visible.
	 * 
	 * @return whether either the coolbar or perspective bar is visible
	 */
	private boolean isVisible() {
		return window.getCoolBarVisible() || window.getPerspectiveBarVisible();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		window.toggleToolbarVisibility();
		setText(isVisible() ? WorkbenchMessages.ToggleCoolbarVisibilityAction_hide_text
				: WorkbenchMessages.ToggleCoolbarVisibilityAction_show_text);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.ActionFactory.IWorkbenchAction#dispose()
	 */
	public void dispose() {
		window = null;
	}

}
