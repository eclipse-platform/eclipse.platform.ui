package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Represents a step in a multi-step wizard.
 */
public abstract class WizardStep {
	/**
	 * Creates a wizard step.
	 */
	public WizardStep() {
		super();
	}

	/**
	 * Returns the label for this step that can
	 * be presented to the user.
	 * 
	 * @return String the label of this step
	 */
	abstract public String getLabel();
	
	/**
	 * Returns an explaination of this step that can
	 * be presented to the user.
	 * 
	 * @return String the details of this step
	 */
	abstract public String getDetails();
}
