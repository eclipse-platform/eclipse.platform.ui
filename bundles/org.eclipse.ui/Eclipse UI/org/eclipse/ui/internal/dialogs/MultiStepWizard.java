package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
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
		
		reviewPage = new MultiStepReviewWizardPage("multiStepReviewWizardPage");//$NON-NLS-1$
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
		if (isConfigureStepMode())
			return getStepContainer().canWizardFinish();
		else
			return false;
	}

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
	public final boolean performFinish() {
		// Does nothing as each step is responsible
		// to complete its work then its wizard is
		// done.
		return true;
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
