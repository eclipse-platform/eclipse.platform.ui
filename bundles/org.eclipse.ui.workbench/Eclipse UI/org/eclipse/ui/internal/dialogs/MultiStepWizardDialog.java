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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.*;

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

	/**
	 * Returns the multi-step wizard for this dialog
	 */
	/* package */ MultiStepWizard getMultiStepWizard() {
		return multiStepWizard;
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
	 * Sets the label for the finish button
	 */
	/* package */ void setFinishLabel(String label) {
		Button button = getButton(IDialogConstants.FINISH_ID);
		if (button == null)
			return;

		if (label == null) {
			if (!button.getText().equals(IDialogConstants.FINISH_LABEL)) {
				button.setText(IDialogConstants.FINISH_LABEL);
				((Composite)button.getParent()).layout(true);
			}
		} else {
			button.setText(label);
			((Composite)button.getParent()).layout(true);
		}
	}
	
	/**
	 * Updates everything in the dialog
	 */
	/* package */ void updateAll() {
		super.update();
	}
	
	/**
	 * Updates the layout of the dialog
	 */
	/* package */ void updateLayout() {
		super.updateSize(getCurrentPage());
	}
}
