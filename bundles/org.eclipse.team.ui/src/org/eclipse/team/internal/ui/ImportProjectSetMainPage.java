/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.team.internal.ui;

import java.io.File;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class ImportProjectSetMainPage extends TeamWizardPage {
	Text fileText;
	String file = ""; //$NON-NLS-1$
	Button browseButton;
	
	public ImportProjectSetMainPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}
	
	/*
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 1);
		initializeDialogUnits(composite);
		
		createLabel(composite, Policy.bind("ImportProjectSetMainPage.Project_Set_File_Name__2")); //$NON-NLS-1$
		
		Composite inner = new Composite(composite, SWT.NULL);
		inner.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		inner.setLayout(layout);
		
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
				d.setFilterExtensions(new String[] {Policy.bind("ImportProjectSetMainPage.*.psf_1")}); //$NON-NLS-1$
				d.setFilterNames(new String[] {Policy.bind("ImportProjectSetMainPage.Project_Set_Files_2")}); //$NON-NLS-1$
				String f = d.open();
				if (f != null) {
					fileText.setText(f);
					file = f;
				}
			}
		});

		setControl(composite);
		updateEnablement();
	}

	private void updateEnablement() {
		boolean complete;
		
		if (file.length() == 0) {
			setMessage(null);
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
				complete = true;
			}
		}
		if (complete) {
			setMessage(null);
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
}