package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.registry.Capability;

/**
 * Represents the removal of capabilities step in a multi-step
 * wizard.
 */
public class RemoveCapabilitiesStep extends WizardStep {
	private Capability[] capabilities;
	private RemoveCapabilitiesWizard stepWizard;
	
	/**
	 * Creates the remove capabilities step
	 * 
	 * @param number step order number
	 * @param natureIds the list of nature ids to keep on the project
	 * @param project the project to remove the capabilities from
	 * @param capabilities the capabilities to be removed
	 */
	public RemoveCapabilitiesStep(int number, String[] natureIds, Capability[] capabilities, IProject project) {
		super(number);
		this.capabilities = capabilities;
		this.stepWizard = new RemoveCapabilitiesWizard(project, natureIds);
	}

	/* (non-Javadoc)
	 * Method declared on WizardStep.
	 */
	public String getLabel() {
		return WorkbenchMessages.getString("RemoveCapabilitiesStep.label"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * Method declared on WizardStep.
	 */
	public String getDetails() {
		StringBuffer msg = new StringBuffer();
		msg.append(WorkbenchMessages.getString("RemoveCapabilitiesStep.details")); //$NON-NLS-1$
		for (int i = 0; i < capabilities.length; i++) {
			msg.append("\n    "); //$NON-NLS-1$
			msg.append(capabilities[i].getName());
		}
		return msg.toString();
	}

	/* (non-Javadoc)
	 * Method declared on WizardStep.
	 */
	public IWizard getWizard() {
		return stepWizard;
	}

}
