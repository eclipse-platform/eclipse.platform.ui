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
package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * This class is used to prompt the user for a file name & extension.
 */
public class FileExtensionDialog extends TitleAreaDialog {
    private String filename = ""; //$NON-NLS-1$

    private Text filenameField;

    private Button okButton;

    /**
     * Constructs a new file extension dialog.
     * @param parentShell the parent shell
     */
    public FileExtensionDialog(Shell parentShell) {
        super(parentShell);
    }

    /* (non-Javadoc)
     * Method declared in Window.
     */
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(WorkbenchMessages.FileExtension_shellTitle);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(shell,
				IWorkbenchHelpContextIds.FILE_EXTENSION_DIALOG);
    }

    /**
     * Creates and returns the contents of the upper part 
     * of the dialog (above the button bar).
     *
     * Subclasses should overide.
     *
     * @param parent the parent composite to contain the dialog area
     * @return the dialog area control
     */
    protected Control createDialogArea(Composite parent) {
        // top level composite
        Composite parentComposite = (Composite) super.createDialogArea(parent);

        // create a composite with standard margins and spacing
        Composite contents = new Composite(parentComposite, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        layout.numColumns = 2;
        contents.setLayout(layout);
        contents.setLayoutData(new GridData(GridData.FILL_BOTH));
        contents.setFont(parentComposite.getFont());

        setTitle(WorkbenchMessages.FileExtension_dialogTitle); 
        setMessage(WorkbenchMessages.FileExtension_fileTypeMessage);

        // begin the layout

        Label label = new Label(contents, SWT.LEFT);
        label.setText(WorkbenchMessages.FileExtension_fileTypeLabel);

        GridData data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        label.setLayoutData(data);
        label.setFont(parent.getFont());

        filenameField = new Text(contents, SWT.SINGLE | SWT.BORDER);
        filenameField.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                if (event.widget == filenameField) {
                    filename = filenameField.getText().trim();
                    okButton.setEnabled(validateFileType());
                }
            }
        });
        data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        data.grabExcessHorizontalSpace = true;
        filenameField.setLayoutData(data);
        filenameField.setFocus();

        Dialog.applyDialogFont(parentComposite);
        
        return contents;
    }

    /* (non-Javadoc)
     * Method declared on Dialog.
     */
    protected void createButtonsForButtonBar(Composite parent) {
        okButton = createButton(parent, IDialogConstants.OK_ID,
                IDialogConstants.OK_LABEL, true);
        okButton.setEnabled(false);
        createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, false);
    }

    /**
     * Validate the user input for a file type
     */
    private boolean validateFileType() {
        // We need kernel api to validate the extension or a filename

        // check for empty name and extension
        if (filename.length() == 0) {
            setErrorMessage(null);
            return false;
        }

        // check for empty extension if there is no name
        int index = filename.indexOf('.');
        if (index == filename.length() - 1) {
            if (index == 0 || (index == 1 && filename.charAt(0) == '*')) {
                setErrorMessage(WorkbenchMessages.FileExtension_extensionEmptyMessage); 
                return false;
            }
        }

        // check for characters before * 
        // or no other characters
        // or next chatacter not '.'
        index = filename.indexOf('*');
        if (index > -1) {
            if (filename.length() == 1) {
                setErrorMessage(WorkbenchMessages.FileExtension_extensionEmptyMessage); 
                return false;
            }
            if (index != 0 || filename.charAt(1) != '.') {
                setErrorMessage(WorkbenchMessages.FileExtension_fileNameInvalidMessage);
                return false;
            }
        }

        setErrorMessage(null);
        return true;
    }

    /**
     * Get the extension.
     * 
     * @return the extension
     */
    public String getExtension() {
        // We need kernel api to validate the extension or a filename

        int index = filename.indexOf('.');
        if (index == -1)
            return ""; //$NON-NLS-1$
        if (index == filename.length())
            return ""; //$NON-NLS-1$
        return filename.substring(index + 1, filename.length());
    }

    /**
     * Get the name.
     * 
     * @return the name
     */
    public String getName() {
        // We need kernel api to validate the extension or a filename

        int index = filename.indexOf('.');
        if (index == -1)
            return filename;
        if (index == 0)
            return "*"; //$NON-NLS-1$
        return filename.substring(0, index);
    }
}
