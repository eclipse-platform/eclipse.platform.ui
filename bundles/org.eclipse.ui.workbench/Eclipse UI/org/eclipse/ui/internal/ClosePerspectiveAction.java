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
 * The <code>ClosePerspectiveAction</code> is used to close the
 * active perspective in the workbench window's active page.
 */
public class ClosePerspectiveAction extends Action {
	private WorkbenchWindow window;
	
	/**
	 * Create a new instance of <code>ClosePerspectiveAction</code>
	 * 
	 * @param window the workbench window this action applies to
	 */
	public ClosePerspectiveAction(WorkbenchWindow window) {
		super(WorkbenchMessages.getString("ClosePerspectiveAction.text")); //$NON-NLS-1$
		setToolTipText(WorkbenchMessages.getString("ClosePerspectiveAction.toolTip")); //$NON-NLS-1$
		setEnabled(false);
		this.window = window;
		WorkbenchHelp.setHelp(this, IHelpContextIds.CLOSE_PAGE_ACTION);
	}
	
	/* (non-Javadoc)
	 * Method declared on IAction.
	 */
	public void run() {
		WorkbenchPage page = (WorkbenchPage) window.getActivePage();
		if (page != null) {
			Perspective persp = page.getActivePerspective();
			if (persp != null)
				page.closePerspective(persp, true,true);
		}
	}
}
