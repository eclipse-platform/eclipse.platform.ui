/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.dynamic;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * @since 3.1
 */
public class DynamicWizard implements INewWizard {

	/**
	 * 
	 */
	public DynamicWizard() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#addPages()
	 */
	public void addPages() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#canFinish()
	 */
	public boolean canFinish() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#createPageControls(org.eclipse.swt.widgets.Composite)
	 */
	public void createPageControls(Composite pageContainer) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#getContainer()
	 */
	public IWizardContainer getContainer() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#getDefaultPageImage()
	 */
	public Image getDefaultPageImage() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#getDialogSettings()
	 */
	public IDialogSettings getDialogSettings() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#getNextPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	public IWizardPage getNextPage(IWizardPage page) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#getPage(java.lang.String)
	 */
	public IWizardPage getPage(String pageName) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#getPageCount()
	 */
	public int getPageCount() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#getPages()
	 */
	public IWizardPage[] getPages() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#getPreviousPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	public IWizardPage getPreviousPage(IWizardPage page) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#getStartingPage()
	 */
	public IWizardPage getStartingPage() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#getTitleBarColor()
	 */
	public RGB getTitleBarColor() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#getWindowTitle()
	 */
	public String getWindowTitle() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#isHelpAvailable()
	 */
	public boolean isHelpAvailable() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#needsPreviousAndNextButtons()
	 */
	public boolean needsPreviousAndNextButtons() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#needsProgressMonitor()
	 */
	public boolean needsProgressMonitor() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#performCancel()
	 */
	public boolean performCancel() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	public boolean performFinish() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#setContainer(org.eclipse.jface.wizard.IWizardContainer)
	 */
	public void setContainer(IWizardContainer wizardContainer) {
	}

}
