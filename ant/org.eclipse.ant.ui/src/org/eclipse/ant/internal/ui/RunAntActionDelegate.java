package org.eclipse.ant.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import org.apache.tools.ant.*;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ant.core.*;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.*;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.*;import org.eclipse.jface.wizard.WizardDialog;


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
