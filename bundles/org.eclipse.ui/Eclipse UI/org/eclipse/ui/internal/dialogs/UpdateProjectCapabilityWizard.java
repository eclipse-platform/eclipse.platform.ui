package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.InstallCapabilityStep.IProjectProvider;
import org.eclipse.ui.internal.registry.Capability;
import org.eclipse.ui.internal.registry.CapabilityRegistry;

/**
 * Standard workbench wizard that guides the user to supply
 * the necessary information to configure new capabilities
 * on an existing project.
 */
public class UpdateProjectCapabilityWizard extends MultiStepWizard implements IProjectProvider {
	private IProject project;
	private Capability[] capabilities;
	
	/**
	 * Creates a wizard.
	 * 
	 * @param project the project to configure new capabilities
	 * @param capabilities the new capabilities to configure on the project
	 */
	public UpdateProjectCapabilityWizard(IProject project, Capability[] capabilities) {
		super();
		this.project = project;
		this.capabilities = capabilities;
		initializeDefaultPageImageDescriptor();
		setWindowTitle(WorkbenchMessages.getString("UpdateProjectCapabilityWizard.windowTitle")); //$NON-NLS-1$
	}

	/**
	 * Builds the collection of steps install
	 * the chosen capabilities
	 */
	private void buildSteps() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		CapabilityRegistry reg = WorkbenchPlugin.getDefault().getCapabilityRegistry();
		Capability[] results = reg.pruneCapabilities(capabilities);
		WizardStep[] steps = new WizardStep[results.length];
		for (int i = 0; i < results.length; i++)
			steps[i] = new InstallCapabilityStep(i+1, results[i], workbench, this);
		setSteps(steps);
	}
	
	/* (non-Javadoc)
	 * Method declared on MultiStepWizard.
	 */
	protected void addCustomPages() {
	}

	/* (non-Javadoc)
	 * Method declared on MultiStepWizard.
	 */
	protected String getConfigurePageTitle() {
		return WorkbenchMessages.getString("UpdateProjectCapabilityWizard.title");
	}

	/* (non-Javadoc)
	 * Method declared on MultiStepWizard.
	 */
	protected String getConfigurePageDescription() {
		return WorkbenchMessages.getString("WizardProjectConfigurePage.description");
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
		return WorkbenchMessages.getString("UpdateProjectCapabilityWizard.title");
	}

	/* (non-Javadoc)
	 * Method declared on MultiStepWizard.
	 */
	protected String getReviewPageDescription() {
		return WorkbenchMessages.getString("WizardProjectReviewPage.description");
	}

	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public IWizardPage getStartingPage() {
		IWizardPage page = super.getStartingPage();
		buildSteps();
		return page;
	}

	/**
	 * Sets the image banner for the wizard
	 */
	protected void initializeDefaultPageImageDescriptor() {
		String iconPath = "icons/full/";//$NON-NLS-1$		
		try {
			URL installURL = WorkbenchPlugin.getDefault().getDescriptor().getInstallURL();
			URL url = new URL(installURL, iconPath + "wizban/newprj_wiz.gif");//$NON-NLS-1$
			ImageDescriptor desc = ImageDescriptor.createFromURL(url);
			setDefaultPageImageDescriptor(desc);
		}
		catch (MalformedURLException e) {
			// Should not happen. Ignore.
		}
	}
}
