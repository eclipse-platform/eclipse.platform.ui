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
package org.eclipse.team.internal.ui.wizards;

import java.io.File;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.internal.ui.IHelpContextIds;
import org.eclipse.team.internal.ui.TeamUIMessages;
import org.eclipse.ui.PlatformUI;

public class ImportProjectSetMainPage extends TeamWizardPage {
	Combo fileCombo;
	String file = ""; //$NON-NLS-1$
	Button browseButton;
	Button addToWorkingSet;
	Text workingSetField;
	
	private boolean createWorkingSet = false;
	private String workingSetName = ""; //$NON-NLS-1$
	
	private boolean haveBrowsed;
	
	// constants
	//private static final int SIZING_TEXT_FIELD_WIDTH = 80;

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
		inner.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		inner.setLayout(layout);
		
		createLabel(inner, TeamUIMessages.ImportProjectSetMainPage_Project_Set_File_Name__2); 

		fileCombo = createDropDownCombo(inner);
		file = PsfFilenameStore.getSuggestedDefault();
		fileCombo.setItems(PsfFilenameStore.getHistory());
		fileCombo.setText(file);
		fileCombo.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				file = fileCombo.getText();				
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
					fileCombo.setText(f);
					file = f;
				}
			}
		});

		addWorkingSetSection(composite);
		setControl(composite);
		updateEnablement();
        Dialog.applyDialogFont(parent);
	}

	private void addWorkingSetSection(Composite composite) {

		addToWorkingSet = new Button(composite, SWT.CHECK | SWT.LEFT);
		addToWorkingSet.setText( TeamUIMessages.ImportProjectSetMainPage_AddToWorkingSet);
		GridData data = new GridData();
		data.horizontalSpan = 2;
		addToWorkingSet.setLayoutData(data);
		
		addToWorkingSet.setSelection(false);
		addToWorkingSet.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				createWorkingSet= !createWorkingSet;
				updateEnablement();
			}
		});

		Composite inner = new Composite(composite, SWT.NULL);
		inner.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		inner.setLayout(layout);

		workingSetField = createTextField(inner);
		workingSetField.setEditable(false);
		browseButton = new Button(inner, SWT.PUSH);
		browseButton.setText(TeamUIMessages.ImportProjectSetMainPage_Browse);
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		Point minSize = browseButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		data.widthHint = Math.max(widthHint, minSize.x);
		browseButton.setLayoutData(data);
	
		
		//keep track if the user has browsed for working sets; don't show any error message until then
		haveBrowsed = false;
		browseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//open workspace selection dialog
				final WorkingSetsDialog dialog = new WorkingSetsDialog(getShell());
				haveBrowsed = true;
				if (dialog.open() == Window.OK)
					workingSetField.setText(dialog.getSelectedWorkingSet());
				
				updateEnablement();
			}
		});
		updateEnablement();

	}
	
	private boolean validateWorkingSetName() {
		if (addToWorkingSet.getSelection()) {
			workingSetName = workingSetField.getText();
			if (workingSetName.length() == 0) {
				setMessage(TeamUIMessages.ImportProjectSetMainPage_workingSetNameEmpty, ERROR); 
				return false;
			}
		}
		setMessage(null);
		return true;
	}
	
	private void updateEnablement() {
		boolean complete;
		setMessage(null);
		
		workingSetField.setEnabled(addToWorkingSet.getSelection());
		browseButton.setEnabled(addToWorkingSet.getSelection());
		
		if (file.length() == 0) {
			setPageComplete(false);
			return;
		} else {
			// See if the file exists
			File f = new File(file);
			if (!f.exists()) {
				setMessage(TeamUIMessages.ImportProjectSetMainPage_The_specified_file_does_not_exist_4, ERROR); 
				setPageComplete(false);
				return;
			} else if (f.isDirectory()) {
				setMessage(TeamUIMessages.ImportProjectSetMainPage_You_have_specified_a_folder_5, ERROR); 
				setPageComplete(false);
				return;
			} 
		}
		
		//If add to working set checkbox selected and the user has not selected
		//a working set, mark page incomplete
		if (addToWorkingSet.getSelection() && !haveBrowsed){
			setPageComplete(false);
			return;
		}
		
		complete = validateWorkingSetName();
			
		setPageComplete(complete);
	}

	public String getFileName() {
		return file;
	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			fileCombo.setFocus();
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
