/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.wizards;

import java.io.File;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.internal.ui.*;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;

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
		setDescription(TeamUIMessages.ImportProjectSetMainPage_description); 
	}
	
	/*
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 1);
		initializeDialogUnits(composite);

		// set F1 help
        PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IHelpContextIds.IMPORT_PROJECT_SET_PAGE);
				
		Composite inner = new Composite(composite, SWT.NULL);
		inner.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		inner.setLayout(layout);
		
		createLabel(inner, TeamUIMessages.ImportProjectSetMainPage_Project_Set_File_Name__2); 
		fileText = createTextField(inner);
		if (file != null) fileText.setText(file);
		fileText.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				file = fileText.getText();				
				updateEnablement();
			}
		});

		browseButton = new Button(inner, SWT.PUSH);
		browseButton.setText(TeamUIMessages.ImportProjectSetMainPage_Browse_3); 
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, browseButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		browseButton.setLayoutData(data);
		browseButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				FileDialog d = new FileDialog(getShell());
				d.setFilterExtensions(new String[] {"*.psf", "*"}); //$NON-NLS-1$ //$NON-NLS-2$
				d.setFilterNames(new String[] {TeamUIMessages.ImportProjectSetMainPage_Project_Set_Files_2, TeamUIMessages.ImportProjectSetMainPage_allFiles}); //
				String fileName= getFileName();
				if (fileName != null && fileName.length() > 0) {
					int separator= fileName.lastIndexOf(System.getProperty ("file.separator").charAt (0)); //$NON-NLS-1$
					if (separator != -1) {
						fileName= fileName.substring(0, separator);
					}
				} else {
					fileName= ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
				}
				d.setFilterPath(fileName);
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
		createWorkingSetButton.setText(TeamUIMessages.ImportProjectSetMainPage_createWorkingSetLabel); 
		createWorkingSetButton.setSelection(createWorkingSet);
		GridData data = new GridData();
		data.horizontalSpan = numColumns;
		createWorkingSetButton.setLayoutData(data);

		final Label label = new Label(composite, SWT.NONE);
		label.setText(TeamUIMessages.ImportProjectSetMainPage_workingSetLabel); 
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
				setMessage(TeamUIMessages.ImportProjectSetMainPage_workingSetNameEmpty, ERROR); 
				return false;
			} else {
				// todo: verify name doesn't already exist
				IWorkingSet existingSet = TeamUIPlugin.getPlugin().getWorkbench().getWorkingSetManager().getWorkingSet(workingSetName);
				if (existingSet != null) {
					setMessage(TeamUIMessages.ImportProjectSetMainPage_workingSetNameExists, WARNING); 
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
				setMessage(TeamUIMessages.ImportProjectSetMainPage_The_specified_file_does_not_exist_4, ERROR); 
				complete = false;
			} else if (f.isDirectory()) {
				setMessage(TeamUIMessages.ImportProjectSetMainPage_You_have_specified_a_folder_5, ERROR); 
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
