/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms.examples.wizards;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.pde.internal.ui.wizards.plugin.NewPluginProjectWizard;
import org.eclipse.ui.*;
/**
 * @see IWorkbenchWindowActionDelegate
 */
public class OpenAssistedWizardAction implements IWorkbenchWindowActionDelegate {
	//private static final String EDITOR_ID = "org.eclipse.ui.forms.examples.wizard-editor";
	private IWorkbenchWindow window;

	public OpenAssistedWizardAction() {
	}
	
	public void run(IAction action) {
		NewPluginProjectWizard wizard = new NewPluginProjectWizard();
		AssistedWizardDialog dialog = new AssistedWizardDialog(window.getShell(), wizard);
		dialog.create();
		dialog.getShell().setText("Assisted Wizard");
		//dialog.getShell().setSize(600, 400);
		dialog.open();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}
}