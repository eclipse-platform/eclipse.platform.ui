package org.eclipse.update.internal.ui.manager;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.swt.events.*;
import java.util.*;
import org.eclipse.ui.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.layout.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.update.internal.ui.model.*;
import java.net.*;
import org.eclipse.update.internal.ui.UpdateUIPlugin;

public class NewFolderDialog extends Dialog {
	public static final String KEY_TITLE = "NewFolderDialog.title";
	private static final String KEY_NAME = "NewFolderDialog.name";
	private Text nameText;
	private String name;
	private Button okButton;

	public NewFolderDialog(Shell parentShell) {
		super(parentShell);
	}

	public void okPressed() {
		name = nameText.getText();
		super.okPressed();
	}

	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		okButton =
			createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(
			parent,
			IDialogConstants.CANCEL_ID,
			IDialogConstants.CANCEL_LABEL,
			false);
		okButton.setEnabled(false);
	}

	public Control createDialogArea(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		container.setLayout(layout);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label label = new Label(container, SWT.NULL);
		label.setText(UpdateUIPlugin.getResourceString(KEY_NAME));
		nameText = new Text(container, SWT.SINGLE | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		nameText.setLayoutData(gd);

		nameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validateName();
			}
		});
		return container;
	}

	private void validateName() {
		okButton.setEnabled(nameText.getText().length() > 0);
	}

	public BookmarkFolder getNewFolder() {
		return new BookmarkFolder(name);
	}
}