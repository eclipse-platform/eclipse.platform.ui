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

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;

/**
 * Represents the create project step in a multi-step
 * wizard.
 */
public class CreateProjectStep extends WizardStep {
    private CreateProjectWizard stepWizard;

    /**
     * Creates the project creation step
     * 
     * @param number the step order number
     * @param page the wizard page containing the new project name and location
     * @param wizard the multi-step wizard for collecting new project information
     */
    public CreateProjectStep(int number,
            WizardNewProjectNameAndLocationPage page, NewProjectWizard wizard) {
        super(number);
        stepWizard = new CreateProjectWizard(page, wizard);
    }

    /* (non-Javadoc)
     * Method declared on WizardStep.
     */
    public String getLabel() {
        return IDEWorkbenchMessages.CreateProjectStep_label;
    }

    /* (non-Javadoc)
     * Method declared on WizardStep.
     */
    public String getDetails() {
        return NLS.bind(IDEWorkbenchMessages.CreateProjectStep_details, stepWizard.getProjectName());
    }

    /* (non-Javadoc)
     * Method declared on WizardStep.
     */
    public IWizard getWizard() {
        return stepWizard;
    }
}
