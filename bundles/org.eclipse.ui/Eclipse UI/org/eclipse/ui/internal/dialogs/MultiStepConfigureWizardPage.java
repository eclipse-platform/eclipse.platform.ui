package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.misc.WizardStepGroup;

/**
 * This page allows the user to go thru a series of steps. Each
 * step that has a wizard will have its pages embedded into this
 * page. At the end, the step will be asked to complete its work.
 * <p>
 * Example useage:
 * <pre>
 * mainPage = new MultiStepConfigureWizardPage("multiStepWizardPage");
 * mainPage.setTitle("Project");
 * mainPage.setDescription("Configure project capability.");
 * </pre>
 * </p>
 */
public class MultiStepConfigureWizardPage extends WizardPage {
	private Composite pageSite;
	private WizardStepGroup stepGroup;
	private int stepIndex = 0;
	private IWizard stepWizard;
	
	/**
	 * Creates a new multi-step wizard page.
	 *
	 * @param pageName the name of this page
	 */
	public MultiStepConfigureWizardPage(String pageName) {
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
		createEmbeddedPageSite(composite);
		
		setControl(composite);
	}

	/**
	 * Creates the control where the step's wizard will
	 * display its pages.
	 */
	private void createEmbeddedPageSite(Composite parent) {
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
	
	/* (non-Javadoc)
	 * Method declared on IWizardPage.
	 */
	public void setPreviousPage(IWizardPage page) {
		// Do not allow to go back
		super.setPreviousPage(null);
	}

	/**
	 * Sets the steps to be displayed. Ignored if the
	 * createControl has not been called yet.
	 * 
	 * @param steps the collection of steps
	 */
	/* package */ void setSteps(WizardStep[] steps) {
		if (stepGroup != null)
			stepGroup.setSteps(steps);
	}
	
	/* (non-Javadoc)
	 * Method declared on IDialogPage.
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		getControl().getParent().update();
		
		WizardStep[] steps = stepGroup.getSteps();
		while (true) {
			WizardStep step = steps[stepIndex];
			stepWizard = step.getWizard();
			if (stepWizard.getPageCount() > 0)
				return;
			else {
				stepWizard.setContainer(getContainer());
				stepWizard.performFinish();
				stepWizard.dispose();
				stepWizard.setContainer(null);
			}
			stepIndex++;
			if (stepIndex >= steps.length)
				return;
		}
	}
}
