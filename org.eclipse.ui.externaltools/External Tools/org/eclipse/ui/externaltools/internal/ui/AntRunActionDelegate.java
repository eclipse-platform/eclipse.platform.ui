package org.eclipse.ui.externaltools.internal.ui;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import org.eclipse.core.resources.IFile;
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
			new AntAction(selectedFile, part.getSite().getWorkbenchWindow()).run();
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
				if (selectedResource instanceof IFile)
					selectedFile = (IFile) selectedResource;
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