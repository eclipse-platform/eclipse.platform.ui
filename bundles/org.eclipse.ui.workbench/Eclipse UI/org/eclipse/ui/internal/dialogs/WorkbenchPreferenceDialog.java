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
package org.eclipse.ui.internal.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;

import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * Prefence dialog for the workbench including the ability 
 * to load/save preferences.
 */
public class WorkbenchPreferenceDialog extends FilteredPreferenceDialog {
	/**
	 * The Load button id.
	 */
	private final static int LOAD_ID = IDialogConstants.CLIENT_ID + 1;

	/**
	 * The Save button id.
	 */
	private final static int SAVE_ID = IDialogConstants.CLIENT_ID + 2;
	/** 
	 * The dialog settings key for the last used import/export path.
	 */
	final static String FILE_PATH_SETTING = "PreferenceImportExportFileSelectionPage.filePath"; //$NON-NLS-1$
	 
	/**
	 * Creates a new preference dialog under the control of the given preference 
	 * manager.
	 *
	 * @param shell the parent shell
	 * @param manager the preference manager
	 */
	public WorkbenchPreferenceDialog(Shell parentShell, PreferenceManager manager) {
		super(parentShell, manager);

	}
		
	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
			case LOAD_ID : {
				loadPressed();
				return;
			}
			case SAVE_ID : {
				savePressed();
				return;
			}
		}
		super.buttonPressed(buttonId);
	}
	
	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        		
		createButton(parent, LOAD_ID, WorkbenchMessages.getString("WorkbenchPreferenceDialog.load"), false); //$NON-NLS-1$
		createButton(parent, SAVE_ID, WorkbenchMessages.getString("WorkbenchPreferenceDialog.save"), false); //$NON-NLS-1$
		Label l = new Label(parent, SWT.NONE);
		l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = (GridLayout)parent.getLayout();
		layout.numColumns++;
		layout.makeColumnsEqualWidth = false;
	
		super.createButtonsForButtonBar(parent);	
	}
	
	/**
	 * Handle a request to load preferences
	 */
	protected void loadPressed() {
		PreferenceImportExportWizard wizard = new PreferenceImportExportWizard(false, this);
		IDialogSettings workbenchSettings = WorkbenchPlugin.getDefault().getDialogSettings();
		IDialogSettings wizardSettings = workbenchSettings.getSection(FILE_PATH_SETTING); //$NON-NLS-1$
		if (wizardSettings == null)
			wizardSettings = workbenchSettings.addNewSection(FILE_PATH_SETTING); //$NON-NLS-1$
		wizard.setDialogSettings(wizardSettings);

		Shell parent = getShell();
		WizardDialog dialog = new WizardDialog(parent, wizard);
		dialog.create();
		Shell shell = dialog.getShell();
		shell.setSize(Math.max(500, shell.getSize().x), 500);
		Point childSize = shell.getSize();
		Point parentSize = parent.getSize();
		Point childLocation = new Point((parentSize.x - childSize.x) / 2, (parentSize.y - childSize.y) / 2);
		shell.setLocation(parent.toDisplay(childLocation));
		// TODO Provide a help context ID and content.
		//WorkbenchHelp.setHelp(shell, IHelpContextIds.IMPORT_WIZARD);
		int returnCode = dialog.open();
		
		/* All my values are messed up.  Reboot.  (oh, windows, you have taught 
		 * us well.)
		 */
		if (returnCode == Window.OK) {
			close();
		}
	}
			
	/**
	 * Handle a request to save preferences
	 */
	protected void savePressed() {
		PreferenceImportExportWizard wizard = new PreferenceImportExportWizard(true, this);
		IDialogSettings workbenchSettings = WorkbenchPlugin.getDefault().getDialogSettings();
		IDialogSettings wizardSettings = workbenchSettings.getSection(FILE_PATH_SETTING); //$NON-NLS-1$
		if (wizardSettings == null)
			wizardSettings = workbenchSettings.addNewSection("ExportPreferencesWizard"); //$NON-NLS-1$
		wizard.setDialogSettings(wizardSettings);
		wizard.setForcePreviousAndNextButtons(true);

		Shell parent = getShell();
		WizardDialog dialog = new WizardDialog(parent, wizard);
		dialog.create();
		Shell shell = dialog.getShell();
		shell.setSize(Math.max(500, shell.getSize().x), 500);
		Point childSize = shell.getSize();
		Point parentSize = parent.getSize();
		Point childLocation = new Point((parentSize.x - childSize.x) / 2, (parentSize.y - childSize.y) / 2);
		shell.setLocation(parent.toDisplay(childLocation));
		// TODO Provide a help context ID and content.
		//WorkbenchHelp.setHelp(shell, IHelpContextIds.EXPORT_WIZARD);
		dialog.open();	
	} 
}

