package org.eclipse.ui.internal.dialogs;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ui.internal.WorkbenchMessages;

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
	public CreateProjectStep(int number, WizardNewProjectNameAndLocationPage page, NewProjectWizard wizard) {
		super(number);
		stepWizard = new CreateProjectWizard(page, wizard);
	}

	/* (non-Javadoc)
	 * Method declared on WizardStep.
	 */
	public String getLabel() {
		return WorkbenchMessages.getString("CreateProjectStep.label"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * Method declared on WizardStep.
	 */
	public String getDetails() {
		return WorkbenchMessages.format("CreateProjectStep.details", new Object[] {stepWizard.getProjectName()}); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * Method declared on WizardStep.
	 */
	public IWizard getWizard() {
		return stepWizard;
	}
}
