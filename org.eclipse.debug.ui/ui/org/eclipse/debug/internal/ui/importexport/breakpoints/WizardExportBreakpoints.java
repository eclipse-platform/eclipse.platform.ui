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
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

/**
 * <p>
 * This class provides a wizard for exporting breakpoints.
 * It serves dual purpose, in that it is used by the platform import/export wizard,
 * but it can also be used as a standalone wizard.
 * </p>
 * <p>
 * Example:
 * </p>
 * <pre>
 * IWizard wiz = new WizardExportBreakpoints();
 * wiz.init(workbench, selection);
 * WizardDialog wizdialog = new WizardDialog(shell, wiz);
 * wizdialog.open();
 * </pre>
 * 
 * This class uses <code>WizardExportBreakpointsPage</code>
 * 
 * @since 3.2
 *
 */
public class WizardExportBreakpoints extends Wizard implements IExportWizard {

	/*
	 * The main page
	 */
	private WizardExportBreakpointsPage fMainPage = null;
	
	/**
	 * The existing selection
	 */
	private IStructuredSelection fSelection = null;
	
	/**
	 * This is the default constructor
	 */
	public WizardExportBreakpoints() {
		super();
	}//end constructor

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#addPages()
	 */
	public void addPages() {
		super.addPages();
		fMainPage = new WizardExportBreakpointsPage(ImportExportMessages.WizardExportBreakpoints_0, fSelection); 
		addPage(fMainPage);
	}//end addPages

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#dispose()
	 */
	public void dispose() {
		super.dispose();
		fMainPage = null;
		fSelection = null;
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
		fSelection = selection;
		setWindowTitle(ImportExportMessages.WizardExportBreakpoints_0);
        setNeedsProgressMonitor(true);
	}//end init
}//end class
