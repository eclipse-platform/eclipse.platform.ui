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
package org.eclipse.team.internal.ui.wizards;

import java.io.File;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.internal.ui.IHelpContextIds;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.help.WorkbenchHelp;

public class ImportProjectSetMainPage extends TeamWizardPage {
	Text fileText;
	String file = ""; //$NON-NLS-1$
	Button browseButton;
	Button createWorkingSetButton;
	Text workingSetNameField;
	
	private boolean createWorkingSet = false;
	private String workingSetName = ""; //$NON-NLS-1$
	
	// constants
	private static final int SIZING_TEXT_FIELD_WIDTH = 80;

	public ImportProjectSetMainPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
		setDescription(Policy.bind("ImportProjectSetMainPage.description")); //$NON-NLS-1$
	}
	
	/*
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 1);
		initializeDialogUnits(composite);

		// set F1 help
		WorkbenchHelp.setHelp(composite, IHelpContextIds.IMPORT_PROJECT_SET_PAGE);
				
		Composite inner = new Composite(composite, SWT.NULL);
		inner.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		inner.setLayout(layout);
		
		createLabel(inner, Policy.bind("ImportProjectSetMainPage.Project_Set_File_Name__2")); //$NON-NLS-1$
		fileText = createTextField(inner);
		if (file != null) fileText.setText(file);
		fileText.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				file = fileText.getText();				
				updateEnablement();
			}
		});

		browseButton = new Button(inner, SWT.PUSH);
		browseButton.setText(Policy.bind("ImportProjectSetMainPage.Browse_3")); //$NON-NLS-1$
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, browseButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		browseButton.setLayoutData(data);
		browseButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				FileDialog d = new FileDialog(getShell());
				d.setFilterExtensions(new String[] {"*.psf", "*"}); //$NON-NLS-1$ //$NON-NLS-2$
				d.setFilterNames(new String[] {Policy.bind("ImportProjectSetMainPage.Project_Set_Files_2"), Policy.bind("ImportProjectSetMainPage.allFiles")}); //$NON-NLS-1$  //$NON-NLS-2$
				d.setFilterPath(new File(".").getAbsolutePath()); //$NON-NLS-1$
				String f = d.open();
				if (f != null) {
					fileText.setText(f);
					file = f;
				}
			}
		});

		createWorkinSetCreationArea(inner, 3);
		setControl(composite);
		updateEnablement();
        Dialog.applyDialogFont(parent);
	}

	/**
	 * Method createWorkinSetCreationArea.
	 * @param inner
	 */
	private void createWorkinSetCreationArea(Composite composite, int numColumns) {
		
		createWorkingSetButton = new Button(composite, SWT.CHECK | SWT.RIGHT);
		createWorkingSetButton.setText(Policy.bind("ImportProjectSetMainPage.createWorkingSetLabel")); //$NON-NLS-1$
		createWorkingSetButton.setSelection(createWorkingSet);
		GridData data = new GridData();
		data.horizontalSpan = numColumns;
		createWorkingSetButton.setLayoutData(data);

		final Label label = new Label(composite, SWT.NONE);
		label.setText(Policy.bind("ImportProjectSetMainPage.workingSetLabel")); //$NON-NLS-1$
		data = new GridData();
		data.horizontalSpan = 1;
		label.setLayoutData(data);
		label.setEnabled(createWorkingSet);
		
		workingSetNameField = new Text(composite, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		data.horizontalSpan = 1;
		workingSetNameField.setLayoutData(data);
		workingSetNameField.setEnabled(createWorkingSet);

		createWorkingSetButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				createWorkingSet = createWorkingSetButton.getSelection();
				label.setEnabled(createWorkingSet);
				workingSetNameField.setEnabled(createWorkingSet);
				updateEnablement();
			}
		});
		workingSetNameField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateEnablement();
			}
		});
	}

	private boolean validateWorkingSetName() {
		if (createWorkingSet) {
			workingSetName =  workingSetNameField.getText();
			if (workingSetName.length() == 0) {
				setMessage(Policy.bind("ImportProjectSetMainPage.workingSetNameEmpty"), ERROR); //$NON-NLS-1$
				return false;
			} else {
				// todo: verify name doesn't already exist
				IWorkingSet existingSet = TeamUIPlugin.getPlugin().getWorkbench().getWorkingSetManager().getWorkingSet(workingSetName);
				if (existingSet != null) {
					setMessage(Policy.bind("ImportProjectSetMainPage.workingSetNameExists"), WARNING); //$NON-NLS-1$
					return true;
				}
			}
		}
		setMessage(null);
		return true;
	}
	
	private void updateEnablement() {
		boolean complete;
		setMessage(null);
		if (file.length() == 0) {
			complete = false;
		} else {
			// See if the file exists
			File f = new File(file);
			if (!f.exists()) {
				setMessage(Policy.bind("ImportProjectSetMainPage.The_specified_file_does_not_exist_4"), ERROR); //$NON-NLS-1$
				complete = false;
			} else if (f.isDirectory()) {
				setMessage(Policy.bind("ImportProjectSetMainPage.You_have_specified_a_folder_5"), ERROR); //$NON-NLS-1$
				complete = false;
			} else {
				complete = validateWorkingSetName();
			}
		}
		setPageComplete(complete);
	}
	
	public String getFileName() {
		return file;
	}
	public void setFileName(String file) {
		if (file != null) {
			this.file = file;
		}
	}
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			fileText.setFocus();
		}
	}

	/**
	 * @return String
	 */
	public String getWorkingSetName() {
		if (!createWorkingSet) return null;
		return workingSetName;
	}

}
