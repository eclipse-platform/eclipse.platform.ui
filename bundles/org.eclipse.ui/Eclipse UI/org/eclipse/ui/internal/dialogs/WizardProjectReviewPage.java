package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;

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
	}

}
