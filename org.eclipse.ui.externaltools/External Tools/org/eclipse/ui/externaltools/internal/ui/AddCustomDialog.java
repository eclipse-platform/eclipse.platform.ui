package org.eclipse.ui.externaltools.internal.ui;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import java.net.URL;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.externaltools.internal.core.ToolMessages;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Dialog to prompt the user to add a custom Ant task or type.
 */
public class AddCustomDialog extends Dialog {
	private String title;
	private String description;

	//task/type attributes
	private String taskName;
	private String className;
	private URL library;

	//widgets
	private Button okButton;
	private Text nameField;
	private Text classField;
	private org.eclipse.swt.widgets.List libraryField;

	private List libraryUrls;

	/**
	 * Creates a new dialog with the given shell and title.
	 */
	protected AddCustomDialog(Shell parent, List libraryUrls, String title, String description) {
		super(parent);
		this.title = title;
		this.description = description;
		this.libraryUrls= libraryUrls;
	}
	
	/* (non-Javadoc)
	 * Method declared on Window.
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(title);
		WorkbenchHelp.setHelp(newShell, IHelpContextIds.ADD_TASK_DIALOG);
	}
	
	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(
			parent,
			IDialogConstants.OK_ID,
			IDialogConstants.OK_LABEL,
			true);
		createButton(
			parent,
			IDialogConstants.CANCEL_ID,
			IDialogConstants.CANCEL_LABEL,
			false);
		updateEnablement();
	}

	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 10;
		layout.marginWidth = 10;
		dialogArea.setLayout(layout);

		Label label = new Label(dialogArea, SWT.NONE);
		label.setText(description);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		label = new Label(dialogArea, SWT.NONE);
		label.setText(ToolMessages.getString("AddCustomDialog.name")); //$NON-NLS-1$;
		nameField = new Text(dialogArea, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		nameField.setLayoutData(data);
		nameField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateEnablement();
			}
		});

		label = new Label(dialogArea, SWT.NONE);
		label.setText(ToolMessages.getString("AddCustomDialog.class")); //$NON-NLS-1$;
		classField = new Text(dialogArea, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		classField.setLayoutData(data);
		classField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateEnablement();
			}
		});

		label = new Label(dialogArea, SWT.NONE);
		label.setText(ToolMessages.getString("AddCustomDialog.library")); //$NON-NLS-1$;
		libraryField = new org.eclipse.swt.widgets.List(dialogArea, SWT.READ_ONLY | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		libraryField.setLayoutData(data);
		libraryField.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateEnablement();
			}
		});

		//populate library combo and select input library
		if (libraryUrls == null) {
			libraryUrls = Arrays.asList(AntCorePlugin.getPlugin().getPreferences().getCustomURLs());
		}
		int selection = 0;
		Iterator itr= libraryUrls.iterator();
		int i= 0;
		while (itr.hasNext()) {
			URL lib = (URL) itr.next();
			libraryField.add(lib.getFile());
			if (lib.equals(library)) {
				selection = i;
			}
			i++;
		}
			
		//intialize fields
		if (taskName != null) {
			nameField.setText(taskName);
		}
		if (className != null) {
			classField.setText(className);
		}
		if (libraryUrls.size() >= 0) {
			libraryField.select(selection);
		}

		return dialogArea;
	}

	public String getClassName() {
		return className;
	}
	
	public URL getLibrary() {
		return library;
	}
	
	public String getTaskName() {
		return taskName;
	}

	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected void okPressed() {
		className = classField.getText();
		taskName = nameField.getText();
		int selection = libraryField.getSelectionIndex();
		if (selection >= 0) {
			library = (URL)libraryUrls.get(selection);
		}
		super.okPressed();
	}

	public void setClassName(String className) {
		this.className = className;
	}
	
	public void setLibrary(URL library) {
		this.library = library;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	private void updateEnablement() {
		if (okButton != null) {
			okButton.setEnabled(
				nameField.getText().length() > 0
					&& classField.getText().length() > 0
					&& libraryField.getSelectionIndex() >= 0);
		}
	}
}