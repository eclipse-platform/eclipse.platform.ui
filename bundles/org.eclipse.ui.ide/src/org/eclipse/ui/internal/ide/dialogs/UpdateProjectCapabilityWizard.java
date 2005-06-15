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

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.registry.Capability;
import org.eclipse.ui.internal.ide.registry.CapabilityRegistry;

/**
 * Standard workbench wizard that guides the user to supply
 * the necessary information to configure new capabilities
 * on an existing project.
 */
public class UpdateProjectCapabilityWizard extends MultiStepCapabilityWizard {
    private IProject project;

    private Capability[] addCapabilities;

    private Capability[] removeCapabilities;

    /**
     * Creates a wizard.
     * 
     * @param project the project to configure new capabilities
     * @param addCapabilities the new capabilities to configure on the project
     * @param removeCapabilities the old capabilities to remove from the project
     * 		in reverse order (first item last to be removed)
     */
    public UpdateProjectCapabilityWizard(IProject project,
            Capability[] addCapabilities, Capability[] removeCapabilities) {
        super();
        this.project = project;
        this.addCapabilities = addCapabilities;
        this.removeCapabilities = removeCapabilities;
        initializeDefaultPageImageDescriptor();
        setWindowTitle(IDEWorkbenchMessages.UpdateProjectCapabilityWizard_windowTitle);
    }

    /**
     * Builds the collection of steps
     */
    private void buildSteps() {
        int stepNumber = 1;
        ArrayList steps = new ArrayList(removeCapabilities.length
                + addCapabilities.length);

        // collect the minimum remove capability steps
        if (removeCapabilities.length > 0) {
            // Reserve the order so prereq aren't removed before dependents
            for (int i = removeCapabilities.length - 1; i >= 0; i--) {
                if (removeCapabilities[i] != null) {
                    // Collect all the nature ids this capability should
                    // remove. Includes itself and any ones that it
                    // handles the ui for.
                    ArrayList natureIds = new ArrayList();
                    natureIds.add(removeCapabilities[i].getNatureId());
                    ArrayList uiIds = removeCapabilities[i].getHandleUIs();
                    if (uiIds != null) {
                        Iterator it = uiIds.iterator();
                        while (it.hasNext()) {
                            String id = (String) it.next();
                            for (int j = 0; j < removeCapabilities.length; j++) {
                                if (removeCapabilities[j] != null) {
                                    if (removeCapabilities[j].getId()
                                            .equals(id)) {
                                        natureIds.add(removeCapabilities[j]
                                                .getNatureId());
                                        removeCapabilities[j] = null;
                                    }
                                }
                            }
                        }
                    }
                    // Create a step to remove this capability and prereq natures
                    String[] ids = new String[natureIds.size()];
                    natureIds.toArray(ids);
                    steps.add(new RemoveCapabilityStep(stepNumber,
                            removeCapabilities[i], ids, project));
                    stepNumber++;
                }
            }
        }

        // Collect the minimum add capability steps
        if (addCapabilities.length > 0) {
            IWorkbench workbench = PlatformUI.getWorkbench();
            CapabilityRegistry reg = IDEWorkbenchPlugin.getDefault()
                    .getCapabilityRegistry();
            Capability[] results = reg.pruneCapabilities(addCapabilities);
            for (int i = 0; i < results.length; i++, stepNumber++)
                steps.add(new InstallCapabilityStep(stepNumber, results[i],
                        workbench, this));
        }

        // Set the list of steps to do
        WizardStep[] results = new WizardStep[steps.size()];
        steps.toArray(results);
        setSteps(results);
    }

    /* (non-Javadoc)
     * Method declared on MultiStepWizard.
     */
    protected void addCustomPages() {
    }

    /* (non-Javadoc)
     * Method declared on MultiStepWizard.
     */
    protected boolean canFinishOnReviewPage() {
        WizardStep[] steps = getSteps();
        // yes if the only step is to remove capabilities
        return steps.length == 1 && steps[0] instanceof RemoveCapabilityStep;
    }

    /* (non-Javadoc)
     * Method declared on MultiStepWizard.
     */
    protected String getConfigurePageTitle() {
        return IDEWorkbenchMessages.UpdateProjectCapabilityWizard_title;
    }

    /* (non-Javadoc)
     * Method declared on MultiStepWizard.
     */
    protected String getConfigurePageDescription() {
        return IDEWorkbenchMessages.WizardProjectConfigurePage_description;
    }

    /* (non-Javadoc)
     * Method declared on MultiStepWizard.
     */
    protected String getFinishStepLabel(WizardStep[] steps) {
        int count = 0;
        for (int i = 0; i < steps.length; i++) {
            if (!(steps[i] instanceof RemoveCapabilityStep)) {
                count++;
                if (count > 1)
                    return super.getFinishStepLabel(steps);
            }
        }

        return null;
    }

    /* (non-Javadoc)
     * Method declared on MultiStepWizard.
     */
    protected String[] getPerspectiveChoices() {
        ArrayList results = new ArrayList();
        for (int i = 0; i < addCapabilities.length; i++) {
            ArrayList ids = addCapabilities[i].getPerspectiveChoices();
            if (ids != null) {
                Iterator it = ids.iterator();
                while (it.hasNext()) {
                    String id = (String) it.next();
                    if (!results.contains(id))
                        results.add(id);
                }
            }
        }
        String[] ids = new String[results.size()];
        results.toArray(ids);
        return ids;
    }

    /* (non-Javadoc)
     * Method declared on IProjectProvider.
     */
    public IProject getProject() {
        return project;
    }

    /* (non-Javadoc)
     * Method declared on MultiStepWizard.
     */
    protected String getReviewPageTitle() {
        return IDEWorkbenchMessages.UpdateProjectCapabilityWizard_title;
    }

    /* (non-Javadoc)
     * Method declared on MultiStepWizard.
     */
    protected String getReviewPageDescription() {
        return IDEWorkbenchMessages.WizardProjectReviewPage_description;
    }

    /* (non-Javadoc)
     * Method declared on IWizard.
     */
    public IWizardPage getStartingPage() {
        IWizardPage page = super.getStartingPage();
        buildSteps();
        return page;
    }

    /* (non-Javadoc)
     * Method declared on MultiStepWizard.
     */
    /* package */boolean handleMissingStepWizard(WizardStep step) {
        MessageDialog
                .openError(
                        getShell(),
                        IDEWorkbenchMessages.UpdateProjectCapabilityWizard_errorTitle,
                        NLS.bind(IDEWorkbenchMessages.UpdateProjectCapabilityWizard_noWizard, step.getLabel()));
        return false;
    }

    /**
     * Sets the image banner for the wizard
     */
    protected void initializeDefaultPageImageDescriptor() {
        ImageDescriptor desc = IDEWorkbenchPlugin.getIDEImageDescriptor("wizban/newprj_wiz.gif");//$NON-NLS-1$
     
        setDefaultPageImageDescriptor(desc);
      
    }
}
