package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * Third page for the new project creation wizard. This page
 * allows the user to review the capabilities of the new project.
 * <p>
 * Example useage:
 * <pre>
 * mainPage = new WizardProjectReviewPage("wizardProjectReviewPage");
 * mainPage.setTitle("Project");
 * mainPage.setDescription("Review project.");
 * </pre>
 * </p>
 */
public class WizardProjectReviewPage extends WizardPage {
	private Text detailsField;

	/**
	 * Creates a new project review wizard page.
	 *
	 * @param pageName the name of this page
	 */
	protected WizardProjectReviewPage(String pageName) {
		super(pageName);
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

		WorkbenchHelp.setHelp(composite, IHelpContextIds.NEW_PROJECT_REVIEW_WIZARD_PAGE);

		createStepGroup(composite);
		createDetailsGroup(composite);
		createInstructionsGroup(composite);
		
		setControl(composite);
	}
	
	/**
	 * Creates the control for the details
	 */
	private void createDetailsGroup(Composite parent) {
		// Create a composite to hold everything together
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		// Add a label to identify the details text field
		Label label = new Label(composite, SWT.LEFT);
		label.setText(WorkbenchMessages.getString("WizardProjectReviewPage.detailsLabel")); //$NON-NLS-1$
		GridData data = new GridData();
		data.verticalAlignment = SWT.TOP;
		label.setLayoutData(data);
		
		// Text field to display the step's details
		detailsField = new Text(composite, SWT.WRAP | SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
		detailsField.setText("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"); // prefill to show 15 lines
		detailsField.setEditable(false);
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		detailsField.setLayoutData(data);
	}
	
	/**
	 * Creates the control for the instructions
	 */
	private void createInstructionsGroup(Composite parent) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(WorkbenchMessages.getString("WizardProjectReviewPage.instructionLabel")); //$NON-NLS-1$
		GridData data = new GridData();
		data.verticalAlignment = SWT.TOP;
		data.horizontalSpan = 2;
		label.setLayoutData(data);
	}
	
	/**
	 * Creates the control for the step list
	 */
	private void createStepGroup(Composite parent) {
		// Create a composite to hold everything together
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		// Add a label to identify the step list field
		Label label = new Label(composite, SWT.LEFT);
		label.setText(WorkbenchMessages.getString("WizardProjectReviewPage.stepsLabel")); //$NON-NLS-1$
		GridData data = new GridData();
		data.verticalAlignment = SWT.TOP;
		label.setLayoutData(data);
	}
}
