package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * A dialog to show a multi-step wizard to the end user. 
 * <p>
 * In typical usage, the client instantiates this class with 
 * a multi-step wizard. The dialog serves as the wizard container
 * and orchestrates the presentation of its pages.
 * <p>
 * The standard layout is roughly as follows: 
 * it has an area at the top containing both the
 * wizard's title, description, and image; the actual wizard page
 * appears in the middle; below that is a progress indicator
 * (which is made visible if needed); and at the bottom
 * of the page is message line and a button bar containing 
 * Help, Next, Back, Finish, and Cancel buttons (or some subset).
 * </p>
 * <p>
 * This class is not intended to be subclassed by clients.
 * </p>
 */
public class MultiStepWizardDialog extends WizardDialog {
	private MultiStepWizard multiStepWizard;
	
	/**
	 * Creates a new wizard dialog for the given wizard. 
	 *
	 * @param parentShell the parent shell
	 * @param newWizard the multi-step wizard this dialog is working on
	 */
	public MultiStepWizardDialog(Shell parentShell, MultiStepWizard newWizard) {
		super(parentShell, newWizard);
		multiStepWizard = newWizard;
		multiStepWizard.setWizardDialog(this);
	}

	/**
	 * Forces the wizard dialog to close
	 */
	/* package */ void forceClose() {
		super.finishPressed();
	}
	
	/* (non-Javadoc)
	 * Method declared on WizardDialog.
	 */
	protected void backPressed() {
		if (multiStepWizard.isConfigureStepMode())
			multiStepWizard.getStepContainer().backPressed();
		else
			super.backPressed();
	}
	
	/* (non-Javadoc)
	 * Method declared on WizardDialog.
	 */
	protected void finishPressed() {
		if (multiStepWizard.isConfigureStepMode()) {
			boolean success = multiStepWizard.getStepContainer().performFinish();
			if (success)
				multiStepWizard.getStepContainer().processCurrentStep();
		} else {
			super.finishPressed();
		}
	}	

	/* (non-Javadoc)
	 * Method declared on WizardDialog.
	 */
	protected void helpPressed() {
		if (multiStepWizard.isConfigureStepMode())
			multiStepWizard.getStepContainer().helpPressed();
		else
			super.helpPressed();
	}

	/* (non-Javadoc)
	 * Method declared on WizardDialog.
	 */
	protected void nextPressed() {
		if (multiStepWizard.isConfigureStepMode())
			multiStepWizard.getStepContainer().nextPressed();
		else
			super.nextPressed();
	}
	
	/**
	 * Updates everything in the dialog
	 */
	/* package */ void updateAll() {
		super.update();
	}
}
