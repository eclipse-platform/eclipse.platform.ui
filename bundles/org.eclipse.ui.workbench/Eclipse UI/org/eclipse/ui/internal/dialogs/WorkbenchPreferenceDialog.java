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
package org.eclipse.ui.internal.dialogs;

import java.io.File;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;




/**
 * Prefence dialog for the workbench including the ability 
 * to load/save preferences.
 */
public class WorkbenchPreferenceDialog extends PreferenceDialog {
	/**
	 * The Load button.
	 */
	private Button loadButton;
	/**
	 * The Load button id.
	 */
	private final static int LOAD_ID = IDialogConstants.CLIENT_ID + 1;
	/** 
	 * The Load dialogs settings key
	 */
	private final static String LOAD_SETTING = "WorkbenchPreferenceDialog.load";	//$NON-NLS-1$
	
	/**
	 * The Save button.
	 */
	private Button saveButton;
	/**
	 * The Save button id.
	 */
	private final static int SAVE_ID = IDialogConstants.CLIENT_ID + 2;
	/** 
	 * The Save dialogs settings key
	 */
	private final static String SAVE_SETTING = "WorkbenchPreferenceDialog.save"; //$NON-NLS-1$
	
	/**
	 * The extension for preferences files
	 */
	private final static String PREFERENCE_EXT = "epf"; //$NON-NLS-1$
	
	/**
	 * The extensions for the file dialogs
	 */
	private final static String[] DIALOG_PREFERENCE_EXTENSIONS = new String[] {"*."+PREFERENCE_EXT, "*.*"}; //$NON-NLS-2$ //$NON-NLS-1$

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
		
		loadButton = createButton(parent, LOAD_ID, WorkbenchMessages.getString("WorkbenchPreferenceDialog.load"), false); //$NON-NLS-1$
		saveButton = createButton(parent, SAVE_ID, WorkbenchMessages.getString("WorkbenchPreferenceDialog.save"), false); //$NON-NLS-1$
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
		// Get the file to load
		String lastFilename = WorkbenchPlugin.getDefault().getDialogSettings().get(LOAD_SETTING);
		FileDialog d = new FileDialog(getShell(), SWT.OPEN);
		d.setFileName(lastFilename);
		d.setFilterExtensions(DIALOG_PREFERENCE_EXTENSIONS);
		String filename = d.open();
		if (filename == null)
			return;
		// Append the default filename if none was specifed	and such a file does not exist
		IPath path = new Path(filename);
		if (path.getFileExtension() == null) {
			if (!path.toFile().exists()) {
				path = path.addFileExtension(PREFERENCE_EXT);			
				filename = path.toOSString();
			}
		}

		WorkbenchPlugin.getDefault().getDialogSettings().put(LOAD_SETTING, filename);
			
		// Verify the file
		IStatus status = Preferences.validatePreferenceVersions(path);		
		if (status.getSeverity() == IStatus.ERROR) {
			// Show the error and about
			ErrorDialog.openError(
				getShell(), 
				WorkbenchMessages.getString("WorkbenchPreferenceDialog.loadErrorTitle"), //$NON-NLS-1$
				WorkbenchMessages.format("WorkbenchPreferenceDialog.verifyErrorMessage", new Object[]{filename}), //$NON-NLS-1$
				status);
			return;	
		}
		if (status.getSeverity() == IStatus.WARNING) {
			// Show the warning and give the option to continue
			int result = PreferenceErrorDialog.openError(
				getShell(), 
				WorkbenchMessages.getString("WorkbenchPreferenceDialog.loadErrorTitle"), //$NON-NLS-1$
				WorkbenchMessages.format("WorkbenchPreferenceDialog.verifyWarningMessage", new Object[]{filename}), //$NON-NLS-1$
				status);
			if (result != Dialog.OK)
				return;	
		}
		// Load file
		try {
			Preferences.importPreferences(path);
		} catch (CoreException e) {
			ErrorDialog.openError(
				getShell(), 
				WorkbenchMessages.getString("WorkbenchPreferenceDialog.loadErrorTitle"), //$NON-NLS-1$
				WorkbenchMessages.format("WorkbenchPreferenceDialog.loadErrorMessage", new Object[]{filename}), //$NON-NLS-1$
				e.getStatus());
			return;	
		}
		
		MessageDialog.openInformation(
			getShell(),
			WorkbenchMessages.getString("WorkbenchPreferenceDialog.loadTitle"), //$NON-NLS-1$
			WorkbenchMessages.format("WorkbenchPreferenceDialog.loadMessage", new Object[]{filename})); //$NON-NLS-1$
			
		// Close the dialog since it shows an invalid state
		close();
	}
			
	/**
	 * Handle a request to save preferences
	 */
	protected void savePressed() {
		// Get the file to save
		String lastFilename = WorkbenchPlugin.getDefault().getDialogSettings().get(SAVE_SETTING);
		FileDialog d = new FileDialog(getShell(), SWT.SAVE);
		d.setFileName(lastFilename);
		d.setFilterExtensions(DIALOG_PREFERENCE_EXTENSIONS);
		String filename = d.open();
		if (filename == null)
			return;
		// Append the default filename if none was specifed	
		IPath path = new Path(filename);
		if (path.getFileExtension() == null) {
			path = path.addFileExtension(PREFERENCE_EXT);			
			filename = path.toOSString();
		}
			
		WorkbenchPlugin.getDefault().getDialogSettings().put(SAVE_SETTING, filename);
		
		// See if the file already exists
		File file = path.toFile();
		if(file.exists()) {
			if(!MessageDialog.openConfirm(
				getShell(),
				WorkbenchMessages.getString("WorkbenchPreferenceDialog.saveTitle"), //$NON-NLS-1$
				WorkbenchMessages.format("WorkbenchPreferenceDialog.existsErrorMessage", new Object[]{filename}))) //$NON-NLS-1$
					return;
		}			

		// Save all the pages and give them a chance to abort
		Iterator nodes = getPreferenceManager().getElements(PreferenceManager.PRE_ORDER).iterator();
		while (nodes.hasNext()) {
			IPreferenceNode node = (IPreferenceNode) nodes.next();
			IPreferencePage page = node.getPage();
			if (page != null) {
				if(!page.performOk())
					return;
			}
		}

		long lastModified = file.lastModified();
		// Save to file
		try {
			Preferences.exportPreferences(path); 
		} catch (CoreException e) {
			ErrorDialog.openError(
				getShell(), 
				WorkbenchMessages.getString("WorkbenchPreferenceDialog.saveErrorTitle"), //$NON-NLS-1$
				WorkbenchMessages.format("WorkbenchPreferenceDialog.saveErrorMessage", new Object[]{filename}), //$NON-NLS-1$
				e.getStatus());
				return;
		}
		// See if we actually created a file (there where preferences to export).
		if(file.exists() && file.lastModified() != lastModified) {
			MessageDialog.openInformation(
				getShell(),
				WorkbenchMessages.getString("WorkbenchPreferenceDialog.saveTitle"), //$NON-NLS-1$
				WorkbenchMessages.format("WorkbenchPreferenceDialog.saveMessage", new Object[]{filename})); //$NON-NLS-1$
		} else {
			MessageDialog.openError(
				getShell(),
				WorkbenchMessages.getString("WorkbenchPreferenceDialog.saveErrorTitle"), //$NON-NLS-1$
				WorkbenchMessages.getString("WorkbenchPreferenceDialog.noPreferencesMessage")); //$NON-NLS-1$
		}			
		
		
		// Close since we have "performed Ok" and cancel is no longer valid
		close();	
	}
}

