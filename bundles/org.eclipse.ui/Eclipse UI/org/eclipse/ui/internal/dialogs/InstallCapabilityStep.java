package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ui.ICapabilityWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.internal.registry.Capability;

/**
 * Represents a capability install step in a multi-step
 * wizard.
 */
public class InstallCapabilityStep extends WizardStep {
	private Capability capability;
	private ICapabilityWizard wizard;
	private IWorkbench workbench;
	private NewProjectWizard stepWizard;
	
	/**
	 * Creates the capability install step
	 * 
	 * @param capability the capability to install
	 */
	public InstallCapabilityStep(int number, Capability capability, IWorkbench workbench, NewProjectWizard stepWizard) {
		super(number);
		this.capability = capability;
		this.workbench = workbench;
		this.stepWizard = stepWizard;
	}

	/* (non-Javadoc)
	 * Method declared on WizardStep.
	 */
	public String getLabel() {
		return capability.getName();
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
			wizard.init(workbench, StructuredSelection.EMPTY, stepWizard.getNewProject());
			wizard.addPages();
		}
		
		return wizard;
	}
}
