package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.misc.WizardStepGroup;

/**
 * Fourth page for the new project creation wizard. This page
 * allows the user to configure each capability of the new project.
 * <p>
 * Example useage:
 * <pre>
 * mainPage = new WizardProjectConfigurePage("wizardProjectConfigurePage");
 * mainPage.setTitle("Project");
 * mainPage.setDescription("Configure project capability.");
 * </pre>
 * </p>
 */
public class WizardProjectConfigurePage extends WizardPage {
	private Composite pageSite;
	private WizardStepGroup stepGroup;
	
	/**
	 * Creates a new project configure wizard page.
	 *
	 * @param pageName the name of this page
	 */
	public WizardProjectConfigurePage(String pageName) {
		super(pageName);
	}

	/* (non-Javadoc)
	 * Method declared on IWizardPage
	 */
	public boolean canFlipToNextPage() {
		// Already know there is a next page...
		return isPageComplete();
	}

	/* (non-Javadoc)
	 * Method declared on IDialogPage.
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		WorkbenchHelp.setHelp(composite, IHelpContextIds.NEW_PROJECT_CONFIGURE_WIZARD_PAGE);

		createStepGroup(composite);
		createInstallWizardSite(composite);
		
		setControl(composite);
	}

	/**
	 * Creates the control where the install wizard
	 * pages will be displayed.
	 */
	private void createInstallWizardSite(Composite parent) {
		pageSite = new Composite(parent, SWT.NULL);
		pageSite.setLayout(new GridLayout());
		pageSite.setLayoutData(new GridData(GridData.FILL_BOTH));
	}

	/**
	 * Creates the control for the step list
	 */
	private void createStepGroup(Composite parent) {
		initializeDialogUnits(parent);
		stepGroup = new WizardStepGroup(convertWidthInCharsToPixels(2));
		stepGroup.createContents(parent);
	}
	
	/**
	 * Sets the steps to be displayed.
	 * 
	 * @param steps the collection of steps
	 */
	/* package */ void setSteps(WizardStep[] steps) {
		if (stepGroup != null)
			stepGroup.setSteps(steps);
	}
}
