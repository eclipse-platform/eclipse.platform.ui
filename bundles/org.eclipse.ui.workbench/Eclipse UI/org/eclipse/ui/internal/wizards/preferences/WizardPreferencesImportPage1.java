/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.wizards.preferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IExportedPreferences;
import org.eclipse.core.runtime.preferences.IPreferenceFilter;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.preferences.PreferenceTransferElement;

/**
 * Page 1 of the base preference import Wizard
 * 
 * 
 * @since 3.1
 */
public class WizardPreferencesImportPage1 extends WizardPreferencesPage {

    /**
     * Create a new instance of the receiver with name pageName.
     * @param pageName
     */
    protected WizardPreferencesImportPage1(String pageName) {
        super(pageName);
        setTitle(PreferencesMessages.WizardPreferencesImportPage1_importTitle);
        setDescription(PreferencesMessages.WizardPreferencesImportPage1_importDescription);
    }

    /**
     * Create an instance of this class
     */
    public WizardPreferencesImportPage1() {
        this("preferencesImportPage1");//$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.wizards.preferences.WizardPreferencesPage#getAllButtonText()
     */
    protected String getAllButtonText() {
        return PreferencesMessages.WizardPreferencesImportPage1_all;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.wizards.preferences.WizardPreferencesPage#getChooseButtonText()
     */
    protected String getChooseButtonText() {
        return PreferencesMessages.WizardPreferencesImportPage1_choose;
    }

   
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.wizards.preferences.WizardPreferencesPage#getTransfers()
     */
    protected PreferenceTransferElement[] getTransfers() {
        if (validFromFile()) {
            FileInputStream fis;

            try {
                fis = new FileInputStream(getDestinationValue());
            } catch (FileNotFoundException e) {
                WorkbenchPlugin.log(e.getMessage(), e);
                return new PreferenceTransferElement[0];
            }
            IPreferencesService service = Platform.getPreferencesService();
            try {
                IExportedPreferences prefs;
                prefs = service.readPreferences(fis);
                PreferenceTransferElement[] transfers = super.getTransfers();
                IPreferenceFilter[] filters = new IPreferenceFilter[transfers.length];
                for (int i = 0; i < transfers.length; i++) {
                    PreferenceTransferElement transfer = transfers[i];
                    filters[i] = transfer.getFilter();
                }
                IPreferenceFilter[] matches = service.matches(prefs, filters);
                PreferenceTransferElement[] returnTransfers = new PreferenceTransferElement[matches.length];
                int index = 0;
                for (int i = 0; i < matches.length; i++) {
                    IPreferenceFilter filter = matches[i];
                    for (int j = 0; j < transfers.length; j++) {
                        PreferenceTransferElement element = transfers[j];
                        if (element.getFilter().equals(filter)) {
							returnTransfers[index++] = element;
						}                        
                    }
                }

                PreferenceTransferElement[] destTransfers = new PreferenceTransferElement[index];
                System.arraycopy(returnTransfers, 0, destTransfers, 0, index);
                return destTransfers;
            } catch (CoreException e) {
            	//Do not log core exceptions, they indicate the chosen file is not valid
                //WorkbenchPlugin.log(e.getMessage(), e);
            } finally {
                try {
                    fis.close();
                } catch (IOException e) {
                    WorkbenchPlugin.log(e.getMessage(), e);
                }
            }
        }

        return new PreferenceTransferElement[0];
    }

    /**
     * Return whether or not the file is valid.
     * @return <code>true</code> of the file is an existing
     * file and not a directory
     */
    private boolean validFromFile() {
        File fromFile = new File(getDestinationValue());
        return fromFile.exists() && !fromFile.isDirectory();
    }

    protected void setPreferenceTransfers() {
    	super.setPreferenceTransfers();	
    	
		if(validFromFile() && (transfersTable.getItemCount() == 0)) {
			text.setText(PreferencesMessages.WizardPreferences_noSpecificPreferenceDescription);
		} else {
			text.setText(""); //$NON-NLS-1$
		}
	}
  
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.wizards.preferences.WizardPreferencesPage#createTransferArea(org.eclipse.swt.widgets.Composite)
     */
    protected void createTransferArea(Composite composite) {
        createDestinationGroup(composite);
        createTransfersList(composite);
    }

    /**
     * Answer the string to display in self as the destination type
     * 
     * @return java.lang.String
     */
    protected String getDestinationLabel() {
        return PreferencesMessages.WizardPreferencesImportPage1_file;
    }

    /**
     * @param filters
     * @return <code>true</code> if the transfer was succesful, and
     *         <code>false</code> otherwise
     */
    protected boolean transfer(IPreferenceFilter[] filters) {
        File importFile = new File(getDestinationValue());
        FileInputStream fis = null;
        try {
            if (filters.length > 0) {
                try {
                    fis = new FileInputStream(importFile);
                } catch (FileNotFoundException e) {
                    WorkbenchPlugin.log(e.getMessage(), e);
                    MessageDialog.openError(getControl().getShell(), new String(), e.getLocalizedMessage());
                    return false;
                }
                IPreferencesService service = Platform.getPreferencesService();
                try {
                    IExportedPreferences prefs = service.readPreferences(fis);
                    
                    service.applyPreferences(prefs, filters);
                } catch (CoreException e) {
                    WorkbenchPlugin.log(e.getMessage(), e);
                    MessageDialog.openError(getControl().getShell(), new String(), e.getLocalizedMessage());
                    return false;
                }
            }
        } finally {
            if (fis != null) {
				try {
                    fis.close();
                } catch (IOException e) {
                	WorkbenchPlugin.log(e.getMessage(), e);
                	MessageDialog.openError(getControl().getShell(), new String(), e.getLocalizedMessage());
                }
			}
        }
        return true;
    }

    /**
     * Handle events and enablements for widgets in this page
     * 
     * @param e
     *            Event
     */
    public void handleEvent(Event e) {
        if (e.widget == destinationNameField) {
			setPreferenceTransfers();
		}

        super.handleEvent(e);
    }
	
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.wizards.preferences.WizardPreferencesPage#getFileDialogTitle()
     */
    protected String getFileDialogTitle(){
		return PreferencesMessages.WizardPreferencesImportPage1_title;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.wizards.preferences.WizardPreferencesPage#getFileDialogStyle()
	 */
	protected int getFileDialogStyle() {
		return SWT.OPEN;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.wizards.preferences.WizardPreferencesPage#validDestination()
	 */
	protected boolean validDestination() {
		return super.validDestination() && validFromFile();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.wizards.preferences.WizardPreferencesPage#getInvalidDestinationMessage()
	 */
	protected String getInvalidDestinationMessage() {
		return PreferencesMessages.WizardPreferencesImportPage1_invalidPrefFile;
	}
}
