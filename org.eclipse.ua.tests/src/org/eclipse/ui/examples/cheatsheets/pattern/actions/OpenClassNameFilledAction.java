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

import java.util.StringTokenizer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.internal.ui.wizards.NewClassCreationWizard;
import org.eclipse.jdt.ui.wizards.NewClassWizardPage;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.cheatsheets.*;
import org.eclipse.ui.cheatsheets.ICheatSheetAction;


public class OpenClassNameFilledAction extends Action implements ICheatSheetAction {
	private ICheatSheetManager csmanager;
	private String className;
	private String projectName;

	/**
	 * Create a new <code>OpenFileImportWizard</code> action.
	 */
	public OpenClassNameFilledAction() {

	}

	public void run() {
		boolean extendBase = false;
		try {
			IWorkbench workbench = PlatformUI.getWorkbench();
			IStructuredSelection psel = null;
			if (projectName != null) {
				IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				if (p != null)
					psel = new StructuredSelection(p);
			}

			IStructuredSelection selection = null;
			//			ISelection s = workbench.getActiveWorkbenchWindow().getSelectionService().getSelection();

			if (psel != null)
				selection = psel;
			else
				selection = new StructuredSelection();

			NewClassCreationWizard classWizard = new NewClassCreationWizard();
			classWizard.init(workbench, selection);

			Shell shell = Display.getCurrent().getActiveShell();
			WizardDialog wizardDialog = new WizardDialog(shell, classWizard);
			wizardDialog.create();

			IWizardPage[] pages = classWizard.getPages();

			
			String values = csmanager.getData("files"); //$NON-NLS-1$

			StringTokenizer tokenizer = new StringTokenizer(values, ",");
			String[] files = new String[tokenizer.countTokens()];
			for(int i = 0; tokenizer.hasMoreTokens(); i++) {
				files[i] = tokenizer.nextToken();
			}
			String baseClassName = null;
			String derived = null;
			String secondderived = null;
			if(files.length == 4) {
				baseClassName = files[1];
				derived = files[2];
				secondderived = files[3];
			}
			
			
			
			if (className != null){
				if(className.equals(derived) ||  //$NON-NLS-1$
					className.equals(secondderived)){//$NON-NLS-1$
						extendBase = true;
				}
			}else {
				className = "NewClass"; //$NON-NLS-1$
			}

			if (pages[0] instanceof NewClassWizardPage) {
				NewClassWizardPage page1 = (NewClassWizardPage) pages[0];
				page1.setTypeName(className, true);
				page1.setMethodStubSelection(false, false, false, false);
				if(extendBase){
					page1.setSuperClass(baseClassName, false);	//$NON-NLS-1$
				}
			}

			wizardDialog.open();

			PatternGenerator.generate(csmanager);
		} catch (Exception e) {

		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.cheatsheets.ICheatSheetAction#run(java.lang.String[], org.eclipse.ui.cheatsheets.ICheatSheetManager)
	 */
	public void run(String[] params, ICheatSheetManager csm) {
		csmanager = csm;
		if (params != null && !(params.length < 2))
			if (params[0] != null)
				className = params[0];
		if (params[1] != null)
			projectName = params[1];
		run();
	}

}
