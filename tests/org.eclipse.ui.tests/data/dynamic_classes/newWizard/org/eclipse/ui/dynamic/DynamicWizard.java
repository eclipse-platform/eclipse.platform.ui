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

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	@Override
	public void addPages() {
	}

	@Override
	public boolean canFinish() {
		return false;
	}

	@Override
	public void createPageControls(Composite pageContainer) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public IWizardContainer getContainer() {
		return null;
	}

	@Override
	public Image getDefaultPageImage() {
		return null;
	}

	@Override
	public IDialogSettings getDialogSettings() {
		return null;
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		return null;
	}

	@Override
	public IWizardPage getPage(String pageName) {
		return null;
	}

	@Override
	public int getPageCount() {
		return 0;
	}

	@Override
	public IWizardPage[] getPages() {
		return null;
	}

	@Override
	public IWizardPage getPreviousPage(IWizardPage page) {
		return null;
	}

	@Override
	public IWizardPage getStartingPage() {
		return null;
	}

	@Override
	public RGB getTitleBarColor() {
		return null;
	}

	@Override
	public String getWindowTitle() {
		return null;
	}

	@Override
	public boolean isHelpAvailable() {
		return false;
	}

	@Override
	public boolean needsPreviousAndNextButtons() {
		return false;
	}

	@Override
	public boolean needsProgressMonitor() {
		return false;
	}

	@Override
	public boolean performCancel() {
		return false;
	}

	@Override
	public boolean performFinish() {
		return false;
	}

	@Override
	public void setContainer(IWizardContainer wizardContainer) {
	}

}
