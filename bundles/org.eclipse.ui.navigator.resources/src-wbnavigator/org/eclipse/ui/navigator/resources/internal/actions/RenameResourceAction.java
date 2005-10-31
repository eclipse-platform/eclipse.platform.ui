/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator.resources.internal.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.PlatformUI;


/**
 * Standard action for renaming the selected resource. The resource may be a file, folder or
 * project. An INavigatorSiteEditor will be used to let the user type a new name for the selected
 * resource.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p> 
 * 
 * Derived from org.eclipse.ui.actions.RenameResourceAction
 * 
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * @since 3.2
 */
public class RenameResourceAction implements IActionDelegate {
	private org.eclipse.ui.actions.RenameResourceAction renameAction = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		if (this.renameAction == null)
			this.renameAction = new org.eclipse.ui.actions.RenameResourceAction(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		this.renameAction.run();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (this.renameAction == null)
			this.renameAction = new org.eclipse.ui.actions.RenameResourceAction(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());

		this.renameAction.selectionChanged((IStructuredSelection) selection);
	}
}