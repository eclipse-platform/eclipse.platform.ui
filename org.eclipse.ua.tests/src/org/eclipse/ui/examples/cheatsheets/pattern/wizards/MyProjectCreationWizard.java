/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.examples.cheatsheets.pattern.wizards;

import org.eclipse.jdt.internal.ui.wizards.JavaProjectWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.cheatsheets.ICheatSheetManager;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

public class MyProjectCreationWizard extends JavaProjectWizard {
	private ICheatSheetManager csmanager;

	public MyProjectCreationWizard(ICheatSheetManager csm) {
		super();
		csmanager = csm;
	}

	public MyProjectCreationWizard() {
		super();
	}

	/*
	 * @see Wizard#addPages
	 */
	public void addPages() {
		super.addPages();
		IWizardPage[] pagesf = getPages();
		if (pagesf[0] instanceof WizardNewProjectCreationPage) {
			WizardNewProjectCreationPage page1 = (WizardNewProjectCreationPage) pagesf[0];
			String project = csmanager.getData("project"); //$NON-NLS-1$
			if (project == null)
				project = "PatternProject"; //$NON-NLS-1$
			page1.setInitialProjectName(project);
		}
	}

}