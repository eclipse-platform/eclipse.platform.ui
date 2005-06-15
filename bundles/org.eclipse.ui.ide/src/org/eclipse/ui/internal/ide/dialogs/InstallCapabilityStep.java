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
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.ICapabilityInstallWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.registry.Capability;

/**
 * Represents a capability install step in a multi-step
 * wizard.
 */
public class InstallCapabilityStep extends WizardStep {
    private Capability capability;

    private ICapabilityInstallWizard wizard;

    private IWorkbench workbench;

    private IProjectProvider projectProvider;

    /**
     * Creates the capability install step
     * 
     * @param number step order number
     * @param capability the capability to install
     */
    public InstallCapabilityStep(int number, Capability capability,
            IWorkbench workbench, IProjectProvider projectProvider) {
        super(number);
        this.capability = capability;
        this.workbench = workbench;
        this.projectProvider = projectProvider;
    }

    /* (non-Javadoc)
     * Method declared on WizardStep.
     */
    public String getLabel() {
        return NLS.bind(IDEWorkbenchMessages.InstallCapabilityStep_label, capability.getName());
    }

    /* (non-Javadoc)
     * Method declared on WizardStep.
     */
    public String getDetails() {
        return capability.getInstallDetails();
    }

    /* (non-Javadoc)
     * Method declared on WizardStep.
     */
    public IWizard getWizard() {
        if (wizard == null) {
            wizard = capability.getInstallWizard();
            if (wizard != null) {
                wizard.init(workbench, StructuredSelection.EMPTY,
                        projectProvider.getProject());
                wizard.addPages();
            }
        }

        return wizard;
    }

    interface IProjectProvider {
        /**
         * Returns the project to which the capability
         * is to be configured against.
         */
        public IProject getProject();
    }
}
