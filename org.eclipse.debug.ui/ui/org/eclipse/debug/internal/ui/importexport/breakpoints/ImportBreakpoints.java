/*******************************************************************************
 * Copyright (c)2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.importexport.breakpoints;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.actions.AbstractDebugActionDelegate;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;

/**
 * This class provides the aciton event for both the context menu in breakpoints view
 * and the drop down menu in the breakpoints view. 
 * <p>
 * The action simply calls the wizard to import breakpoints.
 * </p>
 *  
 *  @see WizardImportBreakpoints
 *  @see WizardImportBreakpointsPage
 *  
 *  @since 3.2
 */
public class ImportBreakpoints extends AbstractDebugActionDelegate {
	
	/**
	 * Opens import wizard
	 * 
	 * @param action IAction the action
	 */
	public void run(IAction action) {
		WizardImportBreakpoints wiz = new WizardImportBreakpoints();
		wiz.init(DebugUIPlugin.getDefault().getWorkbench(), null);
		WizardDialog wizdialog = new WizardDialog(DebugUIPlugin.getShell(), wiz);
		wizdialog.setBlockOnOpen(true);
		wizdialog.open();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.AbstractDebugActionDelegate#doAction(java.lang.Object)
	 */
	protected void doAction(Object element) throws DebugException {}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.AbstractDebugActionDelegate#update(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	protected void update(IAction action, ISelection s) {
		getAction().setEnabled(true);
	}
}
