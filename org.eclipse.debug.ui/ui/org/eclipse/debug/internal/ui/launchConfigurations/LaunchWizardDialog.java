/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.launchConfigurations;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * Lanuch wizard dialog
 */
public class LaunchWizardDialog extends WizardDialog {
    
    private LaunchWizard fLaunchWizard;

    /**
     * Creates a new wizard dialog for the given wizard. 
     *
     * @param parentShell the parent shell
     * @param newWizard the wizard this dialog is working on
     */
    public LaunchWizardDialog(Shell parentShell, LaunchWizard newWizard) {
        super(parentShell, newWizard);
        fLaunchWizard = newWizard;
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        Button button = getButton(IDialogConstants.FINISH_ID);
        ILaunchMode launchMode = DebugPlugin.getDefault().getLaunchManager().getLaunchMode(fLaunchWizard.getMode());
        button.setText(launchMode.getLabel());
        button = getButton(IDialogConstants.CANCEL_ID);
        button.setText(LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Close_1")); //$NON-NLS-1$
    }
    
    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.wizard.WizardDialog#backPressed()
     */
    protected void backPressed() {
        if (fLaunchWizard.getConfigurationPage().performCancel()) {
            super.backPressed();
        }
    }
    
}
