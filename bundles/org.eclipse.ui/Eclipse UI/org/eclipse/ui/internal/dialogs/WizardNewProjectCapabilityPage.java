package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.ProjectCapabilitySelectionGroup;
import org.eclipse.ui.internal.registry.Capability;
import org.eclipse.ui.internal.registry.CapabilityRegistry;
import org.eclipse.ui.internal.registry.Category;

/**
 * Second page for the new project creation wizard. This page
 * collects the capabilities of the new project.
 * <p>
 * Example useage:
 * <pre>
 * mainPage = new WizardNewProjectCapabilityPage("wizardNewProjectCapabilityPage");
 * mainPage.setTitle("Project");
 * mainPage.setDescription("Choose project's capabilities.");
 * </pre>
 * </p>
 */
public class WizardNewProjectCapabilityPage extends WizardPage {
	// initial value stores
	private Capability[] initialProjectCapabilities;
	private Category[] initialSelectedCategories;

	// widgets
	private ProjectCapabilitySelectionGroup capabilityGroup;
	
	/**
	 * Creates a new project capabilities wizard page.
	 *
	 * @param pageName the name of this page
	 */
	protected WizardNewProjectCapabilityPage(String pageName) {
		super(pageName);
	}

	/* (non-Javadoc)
	 * Method declared on IDialogPage.
	 */
	public void createControl(Composite parent) {
		WorkbenchHelp.setHelp(parent, IHelpContextIds.NEW_PROJECT_CAPABILITY_WIZARD_PAGE);
		CapabilityRegistry reg = WorkbenchPlugin.getDefault().getCapabilityRegistry();
		capabilityGroup = new ProjectCapabilitySelectionGroup(initialProjectCapabilities, reg);
		setControl(capabilityGroup.createContents(parent));
		capabilityGroup.setInitialSelectedCategories(initialSelectedCategories);
		
		capabilityGroup.setCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				getWizard().getContainer().updateButtons();
			}
		});
	}
	
	/**
	 * Sets the initial categories to be selected.
	 * 
	 * @param categories initial categories to select
	 */
	public void setInitialSelectedCategories(Category[] categories) {
		initialSelectedCategories = categories;
	}
	
	/**
	 * Sets the initial project capabilities to be selected.
	 * 
	 * @param capabilities initial project capabilities to select
	 */
	public void setInitialProjectCapabilities(Capability[] capabilities) {
		initialProjectCapabilities = capabilities;
	}
}
