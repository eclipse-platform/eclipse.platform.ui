/*******************************************************************************
 * Copyright (c) 2012 Tensilica Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Abeer Bagul (Tensilica Inc) - initial API and implementation (Bug 372181)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.expressions;

import org.eclipse.debug.internal.ui.views.expression.ExpressionView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;

/**
 * Opens the Working set wizard where user can define expression working sets.
 * 
 * @since 3.9
 */
public class ExpressionWorkingSetsAction implements IViewActionDelegate,
		IActionDelegate2 {

	private ExpressionView fView;
	
	public void run(IAction action) {
		IWorkingSetSelectionDialog selectionDialog = PlatformUI.getWorkbench().getWorkingSetManager().createWorkingSetSelectionDialog(
        		PlatformUI.getWorkbench().getDisplay().getActiveShell(), 
        		true, 
        		new String[] {IDebugUIConstants.EXPRESSION_WORKINGSET_ID});
		
		selectionDialog.setSelection(fView.getWorkingSets());
		
        if (selectionDialog.open() != Window.OK)
        	return;
        
        IWorkingSet[] selectedWorkingSets = selectionDialog.getSelection();
        if (selectedWorkingSets == null)
        	return;
        fView.applyWorkingSets(selectedWorkingSets);
	}

	public void selectionChanged(IAction action, ISelection selection) {

	}

	public void init(IAction action) {

	}

	public void dispose() {
		fView = null;
	}

	public void runWithEvent(IAction action, Event event) {
		run(action);
	}

	public void init(IViewPart view) {
		fView = (ExpressionView) view;
	}

}
