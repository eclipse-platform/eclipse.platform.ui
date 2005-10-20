/***************************************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/
/*
 * Created on Jul 8, 2004
 * 
 * TODO To change the template for this generated file go to Window - Preferences - Java - Code
 * Style - Code Templates
 */
package org.eclipse.ui.navigator.resources.internal.actions;


import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.PlatformUI;

public class MoveResourceAction implements IActionDelegate {

	private org.eclipse.ui.actions.MoveResourceAction moveAction = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		if (this.moveAction == null)
			this.moveAction = new org.eclipse.ui.actions.MoveResourceAction(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());

		this.moveAction.run();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (this.moveAction == null)
			this.moveAction = new org.eclipse.ui.actions.MoveResourceAction(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());

		this.moveAction.selectionChanged((IStructuredSelection) selection);
	}

}

