/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.dialogs.MultiStepConfigureWizardPage.WizardStepContainer;

/**
 * Standard workbench wizard to handle a multi-step page. The
 * wizard is allowed any number of pages before the multi-step
 * page is presented to the user.
 * <p>
 * Example:
 * <pre>
 * IWizard wizard = new MultiStepWizard();
 * WizardDialog dialog = new MultiStepWizardDialog(shell, wizard);
 * dialog.open();
 * </pre>
 * </p>
 */
public abstract class MultiStepWizard extends Wizard {
	private MultiStepWizardDialog wizardDialog;
	private MultiStepReviewWizardPage reviewPage;
	private MultiStepConfigureWizardPage configPage;
	
	/**
	 * Creates an empty wizard
	 */
	protected MultiStepWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	/**
	 * Adds any custom pages to the wizard before
	 * the multi-step review and configure pages are
	 * added.
	 */
	protected abstract void addCustomPages();
	
	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public final void addPages() {
		super.addPages();
		addCustomPages();
		
		reviewPage = new MultiStepReviewWizardPage("multiStepReviewWizardPage", this);//$NON-NLS-1$
		reviewPage.setTitle(getReviewPageTitle()); //$NON-NLS-1$
		reviewPage.setDescription(getReviewPageDescription()); //$NON-NLS-1$
		this.addPage(reviewPage);
		
		configPage = new MultiStepConfigureWizardPage("multiStepConfigureWizardPage");//$NON-NLS-1$
		configPage.setTitle(getConfigurePageTitle()); //$NON-NLS-1$
		configPage.setDescription(getConfigurePageDescription()); //$NON-NLS-1$
		configPage.setWizardDialog(wizardDialog);
		this.addPage(configPage);
	}

	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public boolean canFinish() {
		if (getContainer().getCurrentPage() == reviewPage)
			return canFinishOnReviewPage();
		else if (isConfigureStepMode())
			return getStepContainer().canWizardFinish();
		else
			return false;
	}

	/**
	 * Returns whether the wizard wants the Finish button
	 * enabled on the review page. Should only happen if
	 * there is only one step and that step does not require
	 * further interaction with the user. The Next button will
	 * be disabled and the instructions updated.
	 */
	protected abstract boolean canFinishOnReviewPage();
	
	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public void dispose() {
		super.dispose();
		wizardDialog = null;
	}

	/**
	 * Returns the title for the multi-step configure
	 * page.
	 */
	protected abstract String getConfigurePageTitle();
	
	/**
	 * Returns the description for the multi-step configure
	 * page.
	 */
	protected abstract String getConfigurePageDescription();
	
	/**
	 * Returns the title for the multi-step review
	 * page.
	 */
	protected abstract String getReviewPageTitle();

	/**
	 * Returns the label used on the finish button to
	 * to indicate finishing a step. Can be <code>null</code>
	 * if no special label is required.
	 * <p>
	 * The default implementation is to return the translated
	 * label "Finish Step".
	 * </p><p>
	 * On the last step, the finish button label is changed to
	 * be "Finish" and will no be changed.
	 * </p>
	 */
	protected String getFinishStepLabel(WizardStep[] steps) {
		return WorkbenchMessages.getString("MultiStepWizard.finishLabel"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public final IWizardPage getPreviousPage(IWizardPage page) {
		if (page == configPage)
			return null;
		else
			return super.getPreviousPage(page);
	}

	/**
	 * Returns the description for the multi-step review
	 * page.
	 */
	protected abstract String getReviewPageDescription();

	/**
	 * Returns the container handler for the pages
	 * of the step's wizard.
	 */
	/* package */ WizardStepContainer getStepContainer() {
		return configPage.getStepContainer();
	}
	
	/**
	 * Handles the problem of a missing step wizard.
	 * 
	 * @return <code>true</code> to retry, <code>false</code> to terminate
	 * 		multi step wizard dialog.
	 */
	/* package */ abstract boolean handleMissingStepWizard(WizardStep step);
	
	/**
	 * Returns whether the wizard is configuring steps
	 */
	/* package */ boolean isConfigureStepMode() {
		return getContainer().getCurrentPage() == configPage;
	}
		
	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public final boolean performCancel() {
		if (isConfigureStepMode())
			return getStepContainer().performCancel();
		else
			return true;
	}
	
	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public boolean performFinish() {
		// Finish on review page is a shortcut to performing
		// the steps.
		if (getContainer().getCurrentPage() == reviewPage) {
			getContainer().showPage(configPage);
			return false;
		}
			
		// Does nothing as each step is responsible
		// to complete its work when its wizard is
		// done.
		return true;
	}
	
	/**
	 * Returns the collection of steps for the wizard.
	 */
	public final WizardStep[] getSteps() {
		if (reviewPage != null)
			return reviewPage.getSteps();
		else
			return new WizardStep[0];
	}
	
	/**
	 * Sets the collection of steps for the wizard.
	 * Ignored if the multi-step review and configure
	 * pages are not yet created.
	 */
	public final void setSteps(WizardStep[] steps) {
		if (reviewPage != null)
			reviewPage.setSteps(steps);
		if (configPage != null)
			configPage.setSteps(steps);
	}
	
	/**
	 * Sets the multi-step wizard dialog processing this
	 * wizard.
	 */
	/* package */ void setWizardDialog(MultiStepWizardDialog dialog) {
		wizardDialog = dialog;
		if (configPage != null)
			configPage.setWizardDialog(wizardDialog);
	}
}
