/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.LayoutConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * This class is used to prompt the user for a file name & extension.
 */
public class FileExtensionDialog extends TitleAreaDialog {
	
	private static final String DIALOG_SETTINGS_SECTION = "FileExtensionDialogSettings"; //$NON-NLS-1$
	
    private String filename = ""; //$NON-NLS-1$
    
    private String initialValue;

    private Text filenameField;

    private Button okButton;

	private String title;

	private String helpContextId;

	private final String headerTitle;

	private final String message2;

	private final String label;

    /**
     * Constructs a new file extension dialog.
     * @param parentShell the parent shell
     */
    public FileExtensionDialog(Shell parentShell) {
		this(parentShell, WorkbenchMessages.FileExtension_shellTitle,
				IWorkbenchHelpContextIds.FILE_EXTENSION_DIALOG,
				WorkbenchMessages.FileExtension_dialogTitle,
				WorkbenchMessages.FileExtension_fileTypeMessage,
				WorkbenchMessages.FileExtension_fileTypeLabel);
	}
    
    /**
     * Constructs a new file extension dialog.
     * 
     * @param parentShell the parent shell
     * @param title the dialog title
     * @param helpContextId the help context for this dialog
     * @param headerTitle the dialog header
     * @param message the dialog message
     * @param label the label for the "file type" field
     * @since 3.4
     */
    public FileExtensionDialog(Shell parentShell, String title, String helpContextId, String headerTitle, String message, String label) {
    	super(parentShell);
    	this.title = title;
    	this.helpContextId = helpContextId;
		this.headerTitle = headerTitle;
		message2 = message;
		this.label = label;
    	
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(title);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(shell, helpContextId);
    }

   
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent) {
		Composite parentComposite = (Composite) super.createDialogArea(parent);

		Composite contents = new Composite(parentComposite, SWT.NONE);
		contents.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		setTitle(headerTitle);
		setMessage(message2);

		new Label(contents, SWT.LEFT)
				.setText(label);

		filenameField = new Text(contents, SWT.SINGLE | SWT.BORDER);
		if (initialValue != null) {
			filenameField.setText(initialValue);
		}
		filenameField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				if (event.widget == filenameField) {
					filename = filenameField.getText().trim();
					okButton.setEnabled(validateFileType());
				}
			}
		});
		filenameField.setFocus();

		Dialog.applyDialogFont(parentComposite);

		Point defaultMargins = LayoutConstants.getMargins();
		GridLayoutFactory.fillDefaults().numColumns(2).margins(
				defaultMargins.x, defaultMargins.y).generateLayout(contents);
		
		return contents;
	}

   
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
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
        int index = filename.lastIndexOf('.');
        if (index == filename.length() - 1) {
            if (index == 0 || (index == 1 && filename.charAt(0) == '*')) {
                setErrorMessage(WorkbenchMessages.FileExtension_extensionEmptyMessage); 
                return false;
            }
        }

        // check for characters before * 
        // or no other characters
        // or next chatacter not '.'
        // or another *
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
            if (filename.length() > index && filename.indexOf('*', index + 1) != -1) {
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

        int index = filename.lastIndexOf('.');
        if (index == -1) {
			return ""; //$NON-NLS-1$
		}
        if (index == filename.length()) {
			return ""; //$NON-NLS-1$
		}
        return filename.substring(index + 1, filename.length());
    }

    /**
     * Get the name.
     * 
     * @return the name
     */
    public String getName() {
        // We need kernel api to validate the extension or a filename

        int index = filename.lastIndexOf('.');
        if (index == -1) {
			return filename;
		}
        if (index == 0) {
			return "*"; //$NON-NLS-1$
		}
        return filename.substring(0, index);
    }
    
    /**
	 * Sets the initial value that should be prepopulated in this dialog.
	 * 
	 * @param initialValue
	 *            the value to be displayed to the user
	 * @since 3.4
	 */
    public void setInitialValue(String initialValue) {
    	this.initialValue = initialValue;
    }
   
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsSettings()
     */
    protected IDialogSettings getDialogBoundsSettings() {
        IDialogSettings settings = WorkbenchPlugin.getDefault().getDialogSettings();
        IDialogSettings section = settings.getSection(DIALOG_SETTINGS_SECTION);
        if (section == null) section = settings.addNewSection(DIALOG_SETTINGS_SECTION);
        return section;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#isResizable()
     */
    protected boolean isResizable() {
    	return true;
    }
}
