package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.ProjectCapabilitySelectionGroup;
import org.eclipse.ui.internal.registry.Capability;
import org.eclipse.ui.internal.registry.CapabilityRegistry;

/**
 * A property page for IProject resources to view and edit the
 * capabilities assigned to the project.
 */
public class ProjectCapabilityPage extends PropertyPage {
	private IProject project;
	private ProjectCapabilitySelectionGroup capabilityGroup;

	/**
	 * Creates a new ProjectCapabilityPage.
	 */
	public ProjectCapabilityPage() {
		super();
	}
	
	/* (non-Javadoc)
	 * Method declared on PreferencePage
	 */
	protected Control createContents(Composite parent) {
		WorkbenchHelp.setHelp(parent, IHelpContextIds.PROJECT_CAPABILITY_PROPERTY_PAGE);
		noDefaultAndApplyButton();
		CapabilityRegistry reg = WorkbenchPlugin.getDefault().getCapabilityRegistry();
		Capability[] caps = reg.getProjectCapabilities(getProject());
		capabilityGroup = new ProjectCapabilitySelectionGroup(caps, reg);
		return capabilityGroup.createContents(parent);
	}
	
	/**
	 * Returns the project which this property page applies to
	 * 
	 * @return IProject the project for this property page
	 */
	private IProject getProject() {
		if (project == null)
			project = (IProject) getElement().getAdapter(IResource.class);
			
		return project;
	}

	/**
	 * @see PreferencePage#performOk
	 */
	public boolean performOk() {
		// Avoid doing any work if no changes were made.
		if (!capabilityGroup.getCapabilitiesModified())
			return true;
			
/*			
		ProjectFeatureDescriptor[] featuresToAdd = featureGroup.getProjectFeaturesToAdd();
		ProjectFeatureDescriptor[] featuresToRemove = featureGroup.getProjectFeaturesToRemove();

		// Collect the install wizards
		IProjectFeatureWizard[] installers = new IProjectFeatureWizard[featuresToAdd.length];
		for (int i = 0; i < featuresToAdd.length; i++) {
			IProjectFeature feature = featuresToAdd[i].getProjectFeature();
			if (feature == null) {
				StringBuffer msg = new StringBuffer();
				msg.append("The project feature \"");
				msg.append(featuresToAdd[i].getName());
				msg.append("\" does not provide an installer.\n\nAll feature changes to the project cancelled.");
				MessageDialog.openError(getControl().getShell(), "Project Feature Install Error", msg.toString());
				return true;
			}
			installers[i] = feature.getInstallWizard(getProject());
		}
		
		// Collect the project features to do the uninstall
		IProjectFeature[] uninstallers = new IProjectFeature[featuresToRemove.length];
		for (int i = 0; i < featuresToRemove.length; i++) {
			IProjectFeature feature = featuresToRemove[i].getProjectFeature();
			if (feature == null) {
				StringBuffer msg = new StringBuffer();
				msg.append("The project feature \"");
				msg.append(featuresToRemove[i].getName());
				msg.append("\" does not provide an uninstaller.\n\nAll feature changes to the project cancelled.");
				MessageDialog.openError(getControl().getShell(), "Project Feature Uninstall Error", msg.toString());
				return true;
			}
			uninstallers[i] = feature;
		}
		
		// Open the wizard dialog to install the new features
		if (installers.length > 0) {
			// Determine the first wizard with pages to show
			int index;
			for (index = 0; index < installers.length; index++) {
				if (installers[index].hasPages())
					break;
			}
			// If none of the wizards has pages to show, don't display the
			// dialog, just call performFinish on them
			if (index == installers.length) {
				for (index = 0; index < installers.length; index++)
					installers[index].performFinish();
			}
			else {
				MultiWizardDialog dialog = new MultiWizardDialog(getControl().getShell(), installers, index);
				dialog.open();
				if (dialog.getReturnCode() != IDialogConstants.OK_ID)
					return true;
			}
		}
			
		// Run the uninstallers
		for (int i = 0; i < uninstallers.length; i++) {
			try {
				uninstallers[i].removeInstallation(project);
			} catch (WorkbenchException e) {
				ErrorDialog.openError(getControl().getShell(), "Project Feature Uninstall Failure", null, e.getStatus());
				WorkbenchPlugin.log("Project feature uninstall failed", e.getStatus());//$NON-NLS-1$
			}
		}
*/
		
		return true;
	}
}
