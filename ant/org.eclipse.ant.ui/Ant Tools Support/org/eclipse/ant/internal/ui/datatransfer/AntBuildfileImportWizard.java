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
package org.eclipse.ant.internal.ui.datatransfer;

import org.eclipse.ant.internal.ui.AntUIImages;
import org.eclipse.ant.internal.ui.IAntUIConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

public class AntBuildfileImportWizard extends Wizard implements IImportWizard {

	private ExternalAntBuildfileImportPage mainPage;

	public AntBuildfileImportWizard() {
		super();
	}

	/* (non-Javadoc)
	* Method declared on IWizard.
	*/
	public void addPages() {
		super.addPages();
		mainPage = new ExternalAntBuildfileImportPage();
		addPage(mainPage);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		setWindowTitle(DataTransferMessages.getString("AntBuildfileImportWizard.0")); //$NON-NLS-1$
		setDefaultPageImageDescriptor(AntUIImages.getImageDescriptor(IAntUIConstants.IMG_IMPORT_WIZARD_BANNER));
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#performCancel()
	 */
	public boolean performCancel() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	public boolean performFinish() {
		return (mainPage.createProject() != null);
	}
}