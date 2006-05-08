/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.readmetool;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * This class implements the interface required by the workbench
 * for all 'New' wizards.  This wizard creates readme files.
 */
public class ReadmeCreationWizard extends Wizard implements INewWizard {
    private IStructuredSelection selection;

    private IWorkbench workbench;

    private ReadmeCreationPage mainPage;

    /** (non-Javadoc)
     * Method declared on Wizard.
     */
    public void addPages() {
        mainPage = new ReadmeCreationPage(workbench, selection);
        addPage(mainPage);
    }

    /** (non-Javadoc)
     * Method declared on IWorkbenchWizard
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.workbench = workbench;
        this.selection = selection;
        setWindowTitle(MessageUtil.getString("New_Readme_File")); //$NON-NLS-1$
        setDefaultPageImageDescriptor(ReadmeImages.README_WIZARD_BANNER);
    }

    /** (non-Javadoc)
     * Method declared on IWizard
     */
    public boolean performFinish() {
        return mainPage.finish();
    }
}
