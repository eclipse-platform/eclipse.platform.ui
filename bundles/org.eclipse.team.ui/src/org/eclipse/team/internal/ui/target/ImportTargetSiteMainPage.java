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
package org.eclipse.team.internal.ui.target;

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
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.wizards.TeamWizardPage;

public class ImportTargetSiteMainPage extends TeamWizardPage {
	private Text fileText;
	private String file = ""; //$NON-NLS-1$
	private Button browseButton;

	/**
	 * @see org.eclipse.jface.wizard.WizardPage#WizardPage(String, String, ImageDescriptor)
	 */
	public ImportTargetSiteMainPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
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
				setMessage(Policy.bind("ImportTargetSiteMainPage.nonexistent_file"), ERROR); //$NON-NLS-1$
				complete = false;
			} else if (f.isDirectory()) {
				setMessage(Policy.bind("ImportTargetSiteMainPage.folder_specified"), ERROR); //$NON-NLS-1$
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

	/**
	 * single-use mutator
	 * @param file The name to use for the file.
	 */
	public void setFileName(String file) {
		if (file != null) {
			this.file = file;
		}
	}

	/**
	 * Method getFileName.
	 * @return String
	 */
	public String getFileName() {
		return file;
	}

	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			fileText.setFocus();
		}
	}

	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 1);
		initializeDialogUnits(composite);

		//TODO: add F1 help.
				
		Composite inner = new Composite(composite, SWT.NULL);
		inner.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		inner.setLayout(layout);
		
		createLabel(inner, Policy.bind("ImportTargetSiteMainPage.File_name")); //$NON-NLS-1$
		fileText = createTextField(inner);
		if (file != null) fileText.setText(file);
		fileText.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				file = fileText.getText();				
				updateEnablement();
			}
		});

		browseButton = new Button(inner, SWT.PUSH);
		browseButton.setText(Policy.bind("ImportTargetSiteMainPage.Browse")); //$NON-NLS-1$
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, browseButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		browseButton.setLayoutData(data);
		browseButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				FileDialog d = new FileDialog(getShell());
				d.setFilterExtensions(new String[] {"*.tsf", "*"}); //$NON-NLS-1$ //$NON-NLS-2$
				d.setFilterNames(new String[] {Policy.bind("ImportTargetSiteMainPage.Target_Site_Files"), Policy.bind("ImportTargetSiteMainPage.allFiles")}); //$NON-NLS-1$  //$NON-NLS-2$
				d.setFilterPath(new File(".").getAbsolutePath()); //$NON-NLS-1$
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

}
