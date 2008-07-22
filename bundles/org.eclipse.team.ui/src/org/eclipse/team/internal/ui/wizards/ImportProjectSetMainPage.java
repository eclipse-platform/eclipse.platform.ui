/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.internal.ui.*;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WorkingSetGroup;

public class ImportProjectSetMainPage extends TeamWizardPage {
	Combo fileCombo;
	String file = ""; //$NON-NLS-1$
	Button browseButton;
	
	private boolean runInBackground = isRunInBackgroundPreferenceOn();
	// a wizard shouldn't be in an error state until the state has been modified by the user
	private int messageType = NONE;
	private WorkingSetGroup workingSetGroup; 
	
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
		
		Button runInBackgroundCheckbox = SWTUtils.createCheckBox(composite, TeamUIMessages.ImportProjectSetMainPage_runInBackground, 3);
		
		runInBackgroundCheckbox.setSelection(isRunInBackgroundPreferenceOn());
		runInBackgroundCheckbox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				runInBackground = !runInBackground;
			}
		});
		
		setControl(composite);
		updateEnablement();
		Dialog.applyDialogFont(parent);
		// future messages will be of type error
		messageType = ERROR;
	}

	private void addWorkingSetSection(Composite composite) {
		workingSetGroup = new WorkingSetGroup(
				composite,
				null,
				new String[] { "org.eclipse.ui.resourceWorkingSetPage", //$NON-NLS-1$
						"org.eclipse.jdt.ui.JavaWorkingSetPage" /* JavaWorkingSetUpdater.ID */}); //$NON-NLS-1$
	}
	
	private void updateEnablement() {
		boolean complete = false;
		setMessage(null);
		
		if (file.length() == 0) {
			setMessage(TeamUIMessages.ImportProjectSetMainPage_specifyFile, messageType);
			setPageComplete(false);
			return;
		} else {
			// See if the file exists
			File f = new File(file);
			if (!f.exists()) {
				setMessage(TeamUIMessages.ImportProjectSetMainPage_The_specified_file_does_not_exist_4, messageType); 
				setPageComplete(false);
				return;
			} else if (f.isDirectory()) {
				setMessage(TeamUIMessages.ImportProjectSetMainPage_You_have_specified_a_folder_5, messageType); 
				setPageComplete(false);
				return;
			} else if (!ProjectSetImporter.isValidProjectSetFile(file)) {
				setMessage(TeamUIMessages.ImportProjectSetMainPage_projectSetFileInvalid, messageType);
				setPageComplete(false);
				return;
			}
			complete = true;
		}
		
		if (complete) {
			setErrorMessage(null);
			setDescription(TeamUIMessages.ImportProjectSetMainPage_description);
		}
			
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
	 * Return the working sets selected on the page or an empty array if none
	 * were selected.
	 * 
	 * @return the selected working sets or an empty array
	 */
	public IWorkingSet[] getWorkingSets() {
		return workingSetGroup.getSelectedWorkingSets();
	}
	
	private static boolean isRunInBackgroundPreferenceOn() {
		return TeamUIPlugin.getPlugin().getPreferenceStore().getBoolean(
				IPreferenceIds.RUN_IMPORT_IN_BACKGROUND);
	}
	
	public boolean isRunInBackgroundOn() {
		return runInBackground;
	}
}
