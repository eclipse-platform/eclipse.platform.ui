/**********************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.ant.internal.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;


public class RunAntActionDelegate implements IWorkbenchWindowActionDelegate {

	private IFile selection;

/*
 * @see IWorkbenchWindowActionDelegate
 */
public void dispose() {
}

/*
 * @see IWorkbenchWindowActionDelegate
 */
public void init(IWorkbenchWindow window) {
}
/*
 * @see IActionDelegate
 */
public void run(IAction action) {
	
	new AntAction(selection).run();
}
/*
 * @see IWorkbenchActionDelegate
 */
public void selectionChanged(IAction action, ISelection selection) {
	this.selection = null;
	if (selection instanceof IStructuredSelection) {
		IStructuredSelection structuredSelection = (IStructuredSelection)selection;
		if (structuredSelection.size() == 1) {
			Object selectedResource = structuredSelection.getFirstElement();
			if (selectedResource instanceof IFile)
				this.selection = (IFile)selectedResource;
		}
	}
}

/**
 * Sets the file for the wizard.
 * 
 * @param the file to parse
 */
public void setFile(IFile file) {
	selection = file;
}

}
