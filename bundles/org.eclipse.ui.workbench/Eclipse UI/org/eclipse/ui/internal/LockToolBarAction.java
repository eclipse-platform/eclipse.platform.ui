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
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * The <code>LockToolBarAction</code> is used to lock the toolbars for the
 * workbench.  The toolbar for all perspectives is locked.
 */
public class LockToolBarAction extends Action {
	private WorkbenchWindow window;
	
	/**
	 * Create a new instance of <code>LockToolBarAction</code>
	 * 
	 * @param window the workbench window this action applies to
	 */
	public LockToolBarAction(WorkbenchWindow window) {
		super(WorkbenchMessages.getString("LockToolBarAction.text")); //$NON-NLS-1$
		setToolTipText(WorkbenchMessages.getString("LockToolBarAction.toolTip")); //$NON-NLS-1$
		setEnabled(false);
		setChecked(false);
		this.window = window;
		WorkbenchHelp.setHelp(this, IHelpContextIds.LOCK_TOOLBAR_ACTION);
		// add window listener for updating checked state of this action when
		// workbench opened
		window.getWorkbench().addWindowListener(new org.eclipse.ui.IWindowListener() {
			public void windowActivated(IWorkbenchWindow window){
			}   
			public void windowDeactivated(IWorkbenchWindow window) {
			}   
			public void windowClosed(IWorkbenchWindow window) {
			}   
			public void windowOpened(IWorkbenchWindow window) {
    			setChecked(((WorkbenchWindow)window).isToolBarLocked());
			}   
		});
	}
	/* (non-Javadoc)
	 * Method declared on IAction.
	 */
	public void run() {
		boolean locked = isChecked();
		window.lockToolBar(locked);
	}
}
