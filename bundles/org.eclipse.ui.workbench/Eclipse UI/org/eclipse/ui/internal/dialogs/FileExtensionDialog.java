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
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
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
	 */
	public FileExtensionDialog(Shell parentShell) {
		super(parentShell);
	}
	/* (non-Javadoc)
	 * Method declared in Window.
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(WorkbenchMessages.getString("FileExtension.shellTitle")); //$NON-NLS-1$
		//$NON-NLS-1$
		WorkbenchHelp.setHelp(
			shell,
			IHelpContextIds.FILE_EXTENSION_DIALOG);
	}
	/**
	 * Creates and returns the contents of the upper part 
	 * of the dialog (above the button bar).
	 *
	 * Subclasses should overide.
	 *
	 * @param the parent composite to contain the dialog area
	 * @return the dialog area control
	 */
	protected Control createDialogArea(Composite parent) {
		// top level composite
		Composite parentComposite = (Composite)super.createDialogArea(parent);
		
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

		setTitle(WorkbenchMessages.getString("FileExtension.dialogTitle")); //$NON-NLS-1$
		setMessage(WorkbenchMessages.getString("FileExtension.fileTypeMessage")); //$NON-NLS-1$
		
		// begin the layout

		Label label = new Label(contents, SWT.LEFT);
		label.setText(WorkbenchMessages.getString("FileExtension.fileTypeLabel")); //$NON-NLS-1$
		//$NON-NLS-1$
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

		return contents;
	}
	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		okButton.setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
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
				setErrorMessage(WorkbenchMessages.getString("FileExtension.extensionEmptyMessage")); //$NON-NLS-1$
				return false;
			}
		}

		// check for characters before * 
		// or no other characters
		// or next chatacter not '.'
		index = filename.indexOf('*');
		if (index > -1) {
			if (filename.length() == 1) {
				setErrorMessage(WorkbenchMessages.getString("FileExtension.extensionEmptyMessage")); //$NON-NLS-1$
				return false;
			}		
			if (index != 0 || filename.charAt(1) != '.') {
				setErrorMessage(WorkbenchMessages.getString("FileExtension.fileNameInvalidMessage")); //$NON-NLS-1$
				return false;
			}
		}

		setErrorMessage(null);
		return true;
	}
	public String getExtension() {
		// We need kernel api to validate the extension or a filename

		int index = filename.indexOf('.');
		if (index == -1)
			return ""; //$NON-NLS-1$
		if (index == filename.length())
			return ""; //$NON-NLS-1$
		return filename.substring(index + 1, filename.length());
	}
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
