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

import org.eclipse.jdt.internal.ui.wizards.NewClassCreationWizard;
import org.eclipse.jdt.ui.wizards.NewClassWizardPage;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.*;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;
import org.eclipse.ui.IWorkbench;

public class OpenJavaClassWizardWithMain extends Action {

	public OpenJavaClassWizardWithMain() {
		super();
		 
	}

	public OpenJavaClassWizardWithMain(String text) {
		super(text);
		
	}

	public OpenJavaClassWizardWithMain(String text, ImageDescriptor image) {
		super(text, image);
		
	}

	public OpenJavaClassWizardWithMain(String text, int style) {
		super(text, style);
		
	}

	public void run() {
		try {
			IWorkbench workbench = PlatformUI.getWorkbench();
			IStructuredSelection selection = new StructuredSelection();
			NewClassCreationWizard classWizard = new NewClassCreationWizard();
			classWizard.init(workbench, selection);

			Shell shell = Display.getCurrent().getActiveShell();
			WizardDialog wizardDialog = new WizardDialog(shell, classWizard);
			wizardDialog.create();

			IWizardPage[] pages = classWizard.getPages();
			String className = "HelloWorld"; //$NON-NLS-1$

			if (pages[0] instanceof NewClassWizardPage) {
				NewClassWizardPage page1 = (NewClassWizardPage) pages[0];
				page1.setTypeName(className, true);
				page1.setMethodStubSelection(true, false,false,true);
			}

			int result = wizardDialog.open();
			
			notifyResult(result == Window.OK ? true : false);
		} catch (Exception e) {

		}
	}

}
