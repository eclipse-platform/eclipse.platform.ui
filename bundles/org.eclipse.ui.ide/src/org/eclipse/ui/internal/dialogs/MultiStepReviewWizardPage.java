package org.eclipse.ui.internal.dialogs;

/******************************************************************************* 
 * Copyright (c) 2000, 2003 IBM Corporation and others. 
 * All rights reserved. This program and the accompanying materials! 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 * 
 * Contributors: 
 *   IBM Corporation - initial API and implementation 
 *   Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog font should be
 *      activated and used by other components.
*********************************************************************/
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.misc.WizardStepGroup;

/**
 * This page allows the user to review the steps to be done.
 * <p>
 * Example useage:
 * <pre>
 * mainPage = new MultiStepReviewWizardPage("multiStepReviewPage");
 * mainPage.setTitle("Project");
 * mainPage.setDescription("Review project.");
 * </pre>
 * </p>
 */
public class MultiStepReviewWizardPage extends WizardPage {
	private Text detailsField;
	private Label instructionLabel;
	private WizardStepGroup stepGroup;
	private MultiStepWizard stepWizard;

	/**
	 * Creates a new step review wizard page.
	 *
	 * @param pageName the name of this page
	 */
	public MultiStepReviewWizardPage(String pageName, MultiStepWizard stepWizard) {
		super(pageName);
		this.stepWizard = stepWizard;
	}

	/* (non-Javadoc)
	 * Method declared on IWizardPage
	 */
	public boolean canFlipToNextPage() {
		// Already know there is a next page...
		return isPageComplete() && !stepWizard.canFinishOnReviewPage();
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
		Font font = parent.getFont();
		// Create a composite to hold everything together
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		// Add a label to identify the details text field
		Label label = new Label(composite, SWT.LEFT);
		label.setText(WorkbenchMessages.getString("MultiStepReviewWizardPage.detailsLabel")); //$NON-NLS-1$
		GridData data = new GridData();
		data.verticalAlignment = SWT.TOP;
		label.setLayoutData(data);
		label.setFont(font);
		
		// Text field to display the step's details
		detailsField = new Text(composite, SWT.WRAP | SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
		detailsField.setText("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"); // prefill to show 15 lines //$NON-NLS-1$
		detailsField.setEditable(false);
		detailsField.setLayoutData(new GridData(GridData.FILL_BOTH));
		detailsField.setFont(font);
	}
	
	/**
	 * Creates the control for the instructions
	 */
	private void createInstructionsGroup(Composite parent) {
		instructionLabel = new Label(parent, SWT.LEFT);
		instructionLabel.setText(WorkbenchMessages.getString("MultiStepReviewWizardPage.instructionLabel")); //$NON-NLS-1$
		GridData data = new GridData();
		data.verticalAlignment = SWT.TOP;
		data.horizontalSpan = 2;
		instructionLabel.setLayoutData(data);
		instructionLabel.setFont(parent.getFont());
	}
	
	/**
	 * Creates the control for the step list
	 */
	private void createStepGroup(Composite parent) {
		stepGroup = new WizardStepGroup();
		stepGroup.createContents(parent);
		stepGroup.setSelectionListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection sel = (IStructuredSelection)event.getSelection();
					WizardStep step = (WizardStep)sel.getFirstElement();
					if (step != null)
						detailsField.setText(step.getDetails());
				}
			}
		});
	}
	
	/**
	 * Returns the steps to be displayed.
	 */
	/* package */ WizardStep[] getSteps() {
		if (stepGroup != null)
			return stepGroup.getSteps();
		else
			return new WizardStep[0];
	}

	/**
	 * Sets the steps to be displayed. Has no effect if
	 * the page is not yet created.
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
		if (visible) {
			if (stepWizard.canFinishOnReviewPage())
				instructionLabel.setText(WorkbenchMessages.getString("MultiStepReviewWizardPage.instructionFinishLabel")); //$NON-NLS-1$
			else
				instructionLabel.setText(WorkbenchMessages.getString("MultiStepReviewWizardPage.instructionNextLabel")); //$NON-NLS-1$
			((Composite)getControl()).layout(true);
		}
	}
}
