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
package org.eclipse.ui.internal;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * The <code>CloseAllPerspectivesAction</code> is used to close all of 
 * the opened perspectives in the workbench window's active page.
 */
public class CloseAllPerspectivesAction extends Action {
	private WorkbenchWindow window;
	
	/**
	 * Create a new instance of <code>CloseAllPerspectivesAction</code>
	 * 
	 * @param window the workbench window this action applies to
	 */
	public CloseAllPerspectivesAction(WorkbenchWindow window) {
		super(WorkbenchMessages.getString("CloseAllPerspectivesAction.text")); //$NON-NLS-1$
		setToolTipText(WorkbenchMessages.getString("CloseAllPerspectivesAction.toolTip")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, IHelpContextIds.CLOSE_ALL_PAGES_ACTION);
		setEnabled(false);
		this.window = window;
	}
	
	/* (non-Javadoc)
	 * Method declared on IAction.
	 */
	public void run() {
		WorkbenchPage page = (WorkbenchPage)window.getActivePage();
		if (page != null)
			page.closeAllPerspectives();
	}
}
