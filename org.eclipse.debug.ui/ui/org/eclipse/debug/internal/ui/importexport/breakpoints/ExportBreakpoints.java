/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
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
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.actions.AbstractDebugActionDelegate;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IViewPart;

/**
 * <p>
 * This class provides the aciton event for both the context menu in breakpoints view
 * and the drop down menu inn the breakpoints view.
 * </p> 
 * <p>
 *  The action simply calls the wizard to export breakpoints.
 *  </p>
 *  @see WizardExportBreakpoints
 *  @see WizardExportBreakpointsPage
 *  
 *  @since 3.2
 */
public class ExportBreakpoints extends AbstractDebugActionDelegate { 

	/**
	 * This method actually performs the execution of the action event
	 * 
	 * @param action IAction the action
	 */
	public void run(IAction action) {
		IViewPart fViewpart = DebugUIPlugin.getActiveWorkbenchWindow().getActivePage().findView(IDebugUIConstants.ID_BREAKPOINT_VIEW);
		WizardExportBreakpoints wiz = new WizardExportBreakpoints();
		wiz.init(DebugUIPlugin.getDefault().getWorkbench(), (IStructuredSelection)fViewpart.getViewSite().getSelectionProvider().getSelection());
		WizardDialog wizdialog = new WizardDialog(DebugUIPlugin.getShell(), wiz);
		wizdialog.setBlockOnOpen(true);
		wizdialog.open();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.AbstractDebugActionDelegate#update(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	protected void update(IAction action, ISelection s) {
		getAction().setEnabled(DebugPlugin.getDefault().getBreakpointManager().hasBreakpoints());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.AbstractDebugActionDelegate#doAction(java.lang.Object)
	 */
	protected void doAction(Object element) throws DebugException {}
}
