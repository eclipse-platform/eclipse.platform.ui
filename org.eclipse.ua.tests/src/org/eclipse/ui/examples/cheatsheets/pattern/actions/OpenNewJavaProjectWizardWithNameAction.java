/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.cheatsheets.pattern.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.cheatsheets.*;
import org.eclipse.ui.examples.cheatsheets.pattern.wizards.MyProjectCreationWizard;

public class OpenNewJavaProjectWizardWithNameAction extends Action implements ICheatSheetAction {
	private ICheatSheetManager csmanager;

	/**
	 * Create a new <code>OpenFileImportWizard</code> action.
	 */
	public OpenNewJavaProjectWizardWithNameAction() {
	}
	
	public void run() {
		try {
			IWorkbench workbench = PlatformUI.getWorkbench();

			IStructuredSelection selection = new StructuredSelection();

			MyProjectCreationWizard projectWizard = new MyProjectCreationWizard(csmanager);
			projectWizard.init(workbench, selection);

			Shell shell = Display.getCurrent().getActiveShell();
			WizardDialog wizardDialog = new WizardDialog(shell, projectWizard);

			wizardDialog.create();
			
			wizardDialog.open();

		} catch (Exception e) {

		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.cheatsheets.ICheatsheetAction#run(java.lang.String[], org.eclipse.ui.cheatsheets.ICheatsheetManager)
	 */
	public void run(String[] params, ICheatSheetManager csm) {
		csmanager = csm;
		run();
	}

}
