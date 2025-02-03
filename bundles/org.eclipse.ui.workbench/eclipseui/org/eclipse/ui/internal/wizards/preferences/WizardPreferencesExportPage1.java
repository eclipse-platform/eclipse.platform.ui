/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.wizards.preferences;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferenceFilter;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.preferences.PreferenceTransferElement;

/**
 * Page 1 of the base preference export Wizard
 *
 * @since 3.1
 */
public class WizardPreferencesExportPage1 extends WizardPreferencesPage {

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
	 */
	public WizardPreferencesExportPage1() {
		this(PREFERENCESEXPORTPAGE1);
	}

	@Override
	public void createControl(Composite composite) {
		super.createControl(composite);
		PlatformUI.setHelp(composite, IWorkbenchHelpContextIds.PREFERENCES_EXPORT_WIZARD_PAGE);
	}

	protected String getOutputSuffix() {
		return ".epf"; //$NON-NLS-1$
	}

	/**
	 * Answer the contents of self's destination specification widget
	 *
	 * @return java.lang.String
	 */
	@Override
	protected String getDestinationValue() {
		String idealSuffix = getOutputSuffix();
		String destinationText = super.getDestinationValue();

		// only append a suffix if the destination doesn't already have a . in
		// its last path segment.
		// Also prevent the user from selecting a directory. Allowing this will
		// create a ".epf" file in the directory
		if (destinationText.length() != 0 && !destinationText.endsWith(File.separator)) {
			int dotIndex = destinationText.lastIndexOf('.');
			if (dotIndex != -1) {
				// the last path separator index
				int pathSepIndex = destinationText.lastIndexOf(File.separator);
				if (pathSepIndex != -1 && dotIndex < pathSepIndex) {
					destinationText += idealSuffix;
				}
			} else {
				destinationText += idealSuffix;
			}
		}

		return destinationText;
	}

	@Override
	protected String getAllButtonText() {
		return PreferencesMessages.WizardPreferencesExportPage1_all;
	}

	@Override
	protected String getChooseButtonText() {
		return PreferencesMessages.WizardPreferencesExportPage1_choose;
	}

	@Override
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
	@Override
	protected String getDestinationLabel() {
		return PreferencesMessages.WizardPreferencesExportPage1_file;
	}

	/*
	 * return the PreferenceTransgerElements specified
	 */
	@Override
	protected PreferenceTransferElement[] getTransfers() {
		PreferenceTransferElement[] elements = super.getTransfers();
		PreferenceTransferElement[] returnElements = new PreferenceTransferElement[elements.length];
		IPreferenceFilter[] filters = new IPreferenceFilter[1];
		IPreferenceFilter[] matches;
		IPreferencesService service = Platform.getPreferencesService();
		int count = 0;
		try {
			for (PreferenceTransferElement element : elements) {
				filters[0] = element.getFilter();
				matches = service.matches((IEclipsePreferences) service.getRootNode().node("instance"), filters); //$NON-NLS-1$
				if (matches.length > 0) {
					returnElements[count++] = element;
				}
			}
			elements = new PreferenceTransferElement[count];
			System.arraycopy(returnElements, 0, elements, 0, count);
		} catch (CoreException e) {
			WorkbenchPlugin.log(e.getMessage(), e);
			return new PreferenceTransferElement[0];
		}
		return elements;
	}

	/**
	 * @return <code>true</code> if the transfer was successful, and
	 *         <code>false</code> otherwise
	 */
	@Override
	protected boolean transfer(IPreferenceFilter[] transfers) {
		File exportFile = new File(getDestinationValue());
		if (!ensureTargetIsValid(exportFile)) {
			return false;
		}
		if (transfers.length > 0) {
			try (FileOutputStream fos = new FileOutputStream(exportFile)) {
				IPreferencesService service = Platform.getPreferencesService();
				try {
					service.exportPreferences(service.getRootNode(), transfers, fos);
				} catch (CoreException e) {
					reportException(e);
					return false;
				}
			} catch (IOException e) {
				reportException(e);
				return false;
			}
		}
		return true;
	}

	@Override
	protected String getFileDialogTitle() {
		return PreferencesMessages.WizardPreferencesExportPage1_title;
	}

	@Override
	protected int getFileDialogStyle() {
		return SWT.SAVE | SWT.SHEET;
	}

	@Override
	protected String getInvalidDestinationMessage() {
		return PreferencesMessages.WizardPreferencesExportPage1_noPrefFile;
	}

	@Override
	protected boolean shouldSaveTransferAll() {
		return true;
	}

	private void reportException(Exception e) {
		Shell shell = getControl().getShell();
		WorkbenchPlugin.log(e.getMessage(), e);
		MessageDialog.open(MessageDialog.ERROR, shell, "", e.getLocalizedMessage(), SWT.SHEET); //$NON-NLS-1$
	}
}
