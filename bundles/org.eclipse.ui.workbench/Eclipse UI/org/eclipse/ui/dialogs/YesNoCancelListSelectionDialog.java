/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;

/**
 * YesNoCancelListSelectionDialog is a list selection dialog that also allows
 * the user to select no as a result.
 * 
 * @deprecated Providing Cancel in addition to Yes/No is confusing. It is better
 *             to subclass the regular ListSelectionDialog, which uses
 *             OK/Cancel, and provide a separate checkbox if necessary.
 */
public class YesNoCancelListSelectionDialog extends ListSelectionDialog {
    /**
     * 
     * Create a list selection dialog with a possible Yes/No or Cancel result. 
     * 
     * @param parentShell
     * @param input
     * @param contentProvider
     * @param labelProvider
     * @param message
     * @deprecated see class comment
     */
    public YesNoCancelListSelectionDialog(
            org.eclipse.swt.widgets.Shell parentShell,
            Object input,
            org.eclipse.jface.viewers.IStructuredContentProvider contentProvider,
            org.eclipse.jface.viewers.ILabelProvider labelProvider,
            String message) {
        super(parentShell, input, contentProvider, labelProvider, message);
    }

    /*
     * (non-Javadoc) Method declared on Dialog.
     */
    protected void buttonPressed(int buttonId) {
        switch (buttonId) {
        case IDialogConstants.YES_ID: {
            yesPressed();
            return;
        }
        case IDialogConstants.NO_ID: {
            noPressed();
            return;
        }
        case IDialogConstants.CANCEL_ID: {
            cancelPressed();
            return;
        }
        }
    }

    /*
     * (non-Javadoc) Method declared in Window.
     */
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(shell,
                IWorkbenchHelpContextIds.YES_NO_CANCEL_LIST_SELECTION_DIALOG);
    }

    /*
     * (non-Javadoc) Method declared on Dialog.
     */
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.YES_ID,
                IDialogConstants.YES_LABEL, true);
        createButton(parent, IDialogConstants.NO_ID, IDialogConstants.NO_LABEL,
                false);
        createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, false);
    }

    /**
     * Notifies that the no button of this dialog has been pressed.
     * <p>
     * The <code>Dialog</code> implementation of this framework method sets
     * this dialog's return code to <code>IDialogConstants.NO_ID</code> and
     * closes the dialog. Subclasses may override if desired.
     * </p>
     */
    protected void noPressed() {
        setReturnCode(IDialogConstants.NO_ID);
        close();
    }

    /**
     * Notifies that the yes button of this dialog has been pressed. Do the same
     * as an OK but set the return code to YES_ID instead.
     */
    protected void yesPressed() {
        okPressed();
        setReturnCode(IDialogConstants.YES_ID);
    }
}
