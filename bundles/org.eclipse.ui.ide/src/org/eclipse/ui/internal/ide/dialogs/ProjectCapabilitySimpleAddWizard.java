/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.dialogs;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

public class ProjectCapabilitySimpleAddWizard extends Wizard {
    private ProjectCapabilitySimpleSelectionPage mainPage;

    private IWorkbench workbench;

    private IStructuredSelection selection;

    private IProject project;

    public ProjectCapabilitySimpleAddWizard(IWorkbench workbench,
            IStructuredSelection selection, IProject project) {
        super();
        this.workbench = workbench;
        this.selection = selection;
        this.project = project;
        setForcePreviousAndNextButtons(true);
        setNeedsProgressMonitor(true);
        initializeDefaultPageImageDescriptor();
        setWindowTitle(IDEWorkbenchMessages.ProjectCapabilitySimpleSelectionPage_windowTitle);
    }

    /* (non-Javadoc)
     * Method declared on IWizard
     */
    public void addPages() {
        mainPage = new ProjectCapabilitySimpleSelectionPage(
                "projectCapabilitySimpleSelectionPage", //$NON-NLS-1$
                workbench, selection, project);
        mainPage.setTitle(IDEWorkbenchMessages.ProjectCapabilitySimpleSelectionPage_title);
        mainPage.setDescription(IDEWorkbenchMessages.ProjectCapabilitySimpleSelectionPage_description);
        addPage(mainPage);
    }

    /* (non-Javadoc)
     * Method declared on IWizard.
     */
    public boolean canFinish() {
        return false;
    }

    /**
     * Sets the image banner for the wizard
     */
    protected void initializeDefaultPageImageDescriptor() {
		ImageDescriptor desc = IDEWorkbenchPlugin.getIDEImageDescriptor("wizban/newprj_wiz.gif");//$NON-NLS-1$
        setDefaultPageImageDescriptor(desc);
    }

    /* (non-Javadoc)
     * Method declared on IWizard
     */
    public boolean performFinish() {
        return true;
    }
}
