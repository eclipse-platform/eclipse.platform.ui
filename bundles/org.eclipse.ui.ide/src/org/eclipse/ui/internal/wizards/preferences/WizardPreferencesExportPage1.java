/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferenceFilter;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * Page 1 of the base preference export Wizard
 * 
 * 
 * @since 3.1
 * 
 */
public class WizardPreferencesExportPage1 extends WizardPreferencesPage
        implements Listener {

    // constants
    private static final String PREFERENCESEXPORTPAGE1 = "preferencesExportPage1"; // //$NON-NLS-1$

    /**
     * Create an instance of this class
     */
    protected WizardPreferencesExportPage1(String name) {
        super(name);
        setTitle(PreferencesMessages.WizardPreferencesExportPage1_exportTitle);
        setDescription(PreferencesMessages.WizardPreferencesExportPage1_exportDescription);
    }

    /**
     * Create an instance of this class
     * 
     */
    public WizardPreferencesExportPage1() {
        this(PREFERENCESEXPORTPAGE1);//$NON-NLS-1$
    }

    protected String getAllButtonText() {
        return PreferencesMessages.WizardPreferencesExportPage1_all;
    }

    protected String getChooseButtonText() {
        return PreferencesMessages.WizardPreferencesExportPage1_choose;
    }

    /**
     * @param composite
     * 
     */
    protected void createTransferArea(Composite composite) {
        createTransfersList(composite);
        createDestinationGroup(composite);
        createOptionsGroup(composite);
    }

    /**
     * Answer the string to display in self as the destination type
     * 
     * @return java.lang.String
     */
    protected String getDestinationLabel() {
        return PreferencesMessages.WizardPreferencesExportPage1_file;
    }

    /**
     * @param transfers
     * @return <code>true</code> if the transfer was succesful, and
     *         <code>false</code> otherwise
     */
    protected boolean transfer(IPreferenceFilter[] transfers) {
        File exportFile = new File(getDestinationValue());
        if (!ensureTargetIsValid(exportFile)) {
            return false;
        }
        FileOutputStream fos = null;
        try {
            if (transfers.length > 0) {
                try {
                    fos = new FileOutputStream(exportFile);
                } catch (FileNotFoundException e) {
                    WorkbenchPlugin.log(e.getMessage(), e);
                    return false;
                }
                IPreferencesService service = Platform.getPreferencesService();
                try {
                    service.exportPreferences(service.getRootNode(), transfers,
                            fos);
                } catch (CoreException e) {
                    WorkbenchPlugin.log(e.getMessage(), e);
                    return false;
                }
            }
        } finally {
            if (fos != null)
                try {
                    fos.close();
                } catch (IOException e) {
                    WorkbenchPlugin.log(e.getMessage(), e);
                }
        }
        return true;
    }
}
