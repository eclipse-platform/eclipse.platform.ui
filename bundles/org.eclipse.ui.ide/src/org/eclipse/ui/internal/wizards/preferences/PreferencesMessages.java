/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.ui.internal.wizards.preferences;

import org.eclipse.osgi.util.NLS;

public class PreferencesMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ui.internal.wizards.preferences.messages";//$NON-NLS-1$

	public static String PreferencesExportWizard_export;
	public static String WizardPreferencesExportPage1_exportTitle;
	public static String WizardPreferencesExportPage1_exportDescription;

	public static String WizardPreferencesExportPage1_preferences;
	public static String WizardPreferencesExportPage1_noPrefFile;
	public static String WizardPreferencesExportPage1_overwrite;
	public static String WizardPreferencesExportPage1_saveAs;
	public static String WizardPreferencesExportPage1_all;
	public static String WizardPreferencesExportPage1_choose;
	public static String WizardPreferencesExportPage1_file;

	public static String WizardPreferencesImportPage1_importTitle;
	public static String WizardPreferencesImportPage1_importDescription;
	public static String WizardPreferencesImportPage1_all;
	public static String WizardPreferencesImportPage1_choose;
	public static String WizardPreferencesImportPage1_file;

	public static String PreferencesExport_error;
	public static String PreferencesExport_browse;
	public static String PreferencesExport_createTargetDirectory;
	public static String PreferencesExport_directoryCreationError;
	public static String ExportFile_overwriteExisting;



	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, PreferencesMessages.class);
	}
}