/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.importexport.breakpoints;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

/**
 * <p>
 * Wizard for Importing breakpoints.
 * It serves a dual purpose, in that it is used by the platform import/export wizard,
 * but it can also be used as a standalone wizard.
 * </p>
 * <p>
 * Example:
 * </p>
 * <pre>
 * IWizard wiz = new WizardImportBreakpoints();
 * wiz.init(workbench, selection);
 * WizardDialog wizdialog = new WizardDialog(shell, wiz);
 * wizdialog.open();
 * </pre>
 * 
 * This class uses <code>WizardImportBreakpointsPage</code>
 * 
 * @since 3.2
 *
 */
public class WizardImportBreakpoints extends Wizard implements IImportWizard {

	/*
	 * The main page
	 */
	private WizardImportBreakpointsPage fMainPage = null;
	
	/**
	 * This is the default constructor
	 */
	public WizardImportBreakpoints() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#addPages()
	 */
	public void addPages() {
		super.addPages();
		fMainPage = new WizardImportBreakpointsPage(ImportExportMessages.WizardImportBreakpoints_0);
		addPage(fMainPage);
	}//end addPages
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#dispose()
	 */
	public void dispose() {
		super.dispose();
		fMainPage = null;
	}//end dispose

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	public boolean performFinish() {
		return fMainPage.finish();
	}//end performFinish

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle(ImportExportMessages.WizardImportBreakpoints_0);
        setNeedsProgressMonitor(true);
	}//end init
}//end class
