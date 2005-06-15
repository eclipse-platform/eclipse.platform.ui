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
import org.eclipse.ui.ICapabilityUninstallWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.registry.Capability;
import org.eclipse.ui.internal.ide.registry.CapabilityRegistry;

/**
 * Represents the removal of a capability step in a multi-step
 * wizard.
 */
public class RemoveCapabilityStep extends WizardStep {
    private Capability capability;

    private String[] natureIds;

    private IProject project;

    private ICapabilityUninstallWizard wizard;

    /**
     * Creates the remove capability step
     * 
     * @param number step order number
     * @param capability the capability to be removed
     * @param natureIds the list of nature ids to remove on the project
     * @param project the project to remove the capability from
     */
    public RemoveCapabilityStep(int number, Capability capability,
            String[] natureIds, IProject project) {
        super(number);
        this.capability = capability;
        this.natureIds = natureIds;
        this.project = project;
    }

    /* (non-Javadoc)
     * Method declared on WizardStep.
     */
    public String getLabel() {
        return NLS.bind(IDEWorkbenchMessages.RemoveCapabilityStep_label, capability.getName());
    }

    /* (non-Javadoc)
     * Method declared on WizardStep.
     */
    public String getDetails() {
        String details = capability.getUninstallDetails();
        if (details == null) {
            if (natureIds.length == 1) {
                details = NLS.bind(IDEWorkbenchMessages.RemoveCapabilityStep_defaultDescription0, capability.getName());
            } else if (natureIds.length == 2) {
                CapabilityRegistry reg = IDEWorkbenchPlugin.getDefault()
                        .getCapabilityRegistry();
                Capability otherCapability = reg
                        .getCapabilityForNature(natureIds[1]);
                if (otherCapability == capability)
                    otherCapability = reg.getCapabilityForNature(natureIds[0]);
                details = NLS.bind(IDEWorkbenchMessages.RemoveCapabilityStep_defaultDescription1, capability.getName(), otherCapability.getName());
            } else {
                StringBuffer msg = new StringBuffer();
                CapabilityRegistry reg = IDEWorkbenchPlugin.getDefault()
                        .getCapabilityRegistry();
                for (int i = 0; i < natureIds.length; i++) {
                    Capability cap = reg.getCapabilityForNature(natureIds[i]);
                    if (cap != capability) {
                        msg.append("\n    "); //$NON-NLS-1$
                        msg.append(cap.getName());
                    }
                }
                details = NLS.bind(IDEWorkbenchMessages.RemoveCapabilityStep_defaultDescription2, capability.getName(), msg);
            }
        }

        return details;
    }

    /* (non-Javadoc)
     * Method declared on WizardStep.
     */
    public IWizard getWizard() {
        if (wizard == null) {
            wizard = capability.getUninstallWizard();
            if (wizard == null)
                wizard = new RemoveCapabilityWizard();
            if (wizard != null) {
                wizard.init(PlatformUI.getWorkbench(),
                        StructuredSelection.EMPTY, project, natureIds);
                wizard.addPages();
            }
        }

        return wizard;
    }

}
