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
package org.eclipse.ant.ui.internal.launchConfigurations;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionDelegate;

/**
 * Action delegate to launch Ant on a build file.
 */
public class AntRunActionDelegate extends ActionDelegate implements IObjectActionDelegate {
	private IFile selectedFile;
	private IWorkbenchPart part;

	/* (non-Javadoc)
	 * Method declared on IActionDelegate.
	 */
	public void run(IAction action) {
		if (part != null && selectedFile != null) {
			AntLaunchShortcut shortcut = new AntLaunchShortcut();
			shortcut.setShowDialog(true);
			shortcut.launch(selectedFile, ILaunchManager.RUN_MODE);
		}
	}
	
	/* (non-Javadoc)
	 * Method declared on IActionDelegate.
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		selectedFile = null;
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			if (structuredSelection.size() == 1) {
				Object selectedResource = structuredSelection.getFirstElement();
				if (selectedResource instanceof IFile) {
					selectedFile = (IFile) selectedResource;
				} else if (selectedResource instanceof IAdaptable) {
					selectedResource= ((IAdaptable) selectedResource).getAdapter(IResource.class);
					if (selectedResource instanceof IFile) {
						selectedFile= (IFile) selectedResource;
					}
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * Method declared on IObjectActionDelegate.
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.part = targetPart;
	}
}
