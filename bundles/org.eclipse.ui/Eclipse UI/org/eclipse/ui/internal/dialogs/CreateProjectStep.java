package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * Represents the create project step in a multi-step
 * wizard.
 */
public class CreateProjectStep extends WizardStep {
	private WizardNewProjectCreationPage page;
	
	/**
	 * Creates the project creation step
	 * 
	 * @param page the wizard page containing the new project name
	 */
	public CreateProjectStep(int number, WizardNewProjectCreationPage page) {
		super(number);
		this.page = page;
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
		return WorkbenchMessages.format("CreateProjectStep.details", new Object[] {page.getProjectName()}); //$NON-NLS-1$
	}

}
