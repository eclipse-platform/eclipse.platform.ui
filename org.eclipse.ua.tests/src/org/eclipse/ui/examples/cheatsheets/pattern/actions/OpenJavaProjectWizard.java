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

import org.eclipse.jdt.internal.ui.wizards.JavaProjectWizard;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;

public class OpenJavaProjectWizard extends Action {

	public OpenJavaProjectWizard() {
		super();
	}

	public OpenJavaProjectWizard(String text) {
		super(text);
	}

	public OpenJavaProjectWizard(String text, ImageDescriptor image) {
		super(text, image);
	}

	public OpenJavaProjectWizard(String text, int style) {
		super(text, style);
	}

	public void run() {
		try {
			IWorkbench workbench = PlatformUI.getWorkbench();
			IStructuredSelection selection = new StructuredSelection();
			JavaProjectWizard projectWizard = new JavaProjectWizard();
			projectWizard.init(workbench, selection);

			Shell shell = Display.getCurrent().getActiveShell();
			WizardDialog wizardDialog = new WizardDialog(shell, projectWizard);
			wizardDialog.create();
			int result = wizardDialog.open();
			
			notifyResult(result == Window.OK ? true : false);
		} catch (Exception e) {
		}
	}
}
