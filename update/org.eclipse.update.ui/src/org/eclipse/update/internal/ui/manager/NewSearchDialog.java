package org.eclipse.update.internal.ui.manager;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.internal.ui.UpdateUIPlugin;
import org.eclipse.update.internal.ui.search.*;

public class NewSearchDialog extends Dialog {
	public static final String KEY_TITLE = "NewSearchDialog.title";
	private static final String KEY_NAME = "NewSearchDialog.name";
	private static final String KEY_CATEGORY = "NewSearchDialog.category";
	private Text nameText;
	private Combo categoryCombo;
	private String name;
	private SearchCategoryDescriptor descriptor;
	private Button okButton;
	private SearchCategoryDescriptor[] descriptors;

	public NewSearchDialog(Shell parentShell) {
		super(parentShell);
	}

	public void okPressed() {
		name = nameText.getText();
		int index = categoryCombo.getSelectionIndex();
		descriptor = descriptors[index];
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

		label = new Label(container, SWT.NULL);
		label.setText(UpdateUIPlugin.getResourceString(KEY_CATEGORY));

		categoryCombo = new Combo(container, SWT.READ_ONLY);
		descriptors = SearchCategoryRegistryReader.getDefault().getCategoryDescriptors();
		for (int i=0; i<descriptors.length; i++) {
			categoryCombo.add(descriptors[i].getName());
		}
		gd = new GridData(GridData.FILL_HORIZONTAL);
		categoryCombo.setLayoutData(gd);
		return container;
	}

	private void validateName() {
		okButton.setEnabled(nameText.getText().length() > 0);
	}

	public SearchObject getNewSearch() {
		return new SearchObject(name, descriptor);
	}
}