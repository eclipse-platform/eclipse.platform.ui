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
import java.io.FileInputStream;
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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
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

	@Override
	public void createControl(Composite composite) {
		super.createControl(composite);
		PlatformUI.setHelp(composite, IWorkbenchHelpContextIds.PREFERENCES_IMPORT_WIZARD_PAGE);
	}

	@Override
	protected String getAllButtonText() {
		return PreferencesMessages.WizardPreferencesImportPage1_all;
	}

	@Override
	protected String getChooseButtonText() {
		return PreferencesMessages.WizardPreferencesImportPage1_choose;
	}

	@Override
	protected PreferenceTransferElement[] getTransfers() {
		if (validFromFile()) {
			try (FileInputStream fis = new FileInputStream(getDestinationValue())) {
				IPreferencesService service = Platform.getPreferencesService();
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
				for (IPreferenceFilter filter : matches) {
					for (PreferenceTransferElement element : transfers) {
						if (element.getFilter().equals(filter)) {
							returnTransfers[index++] = element;
						}
					}
				}

				PreferenceTransferElement[] destTransfers = new PreferenceTransferElement[index];
				System.arraycopy(returnTransfers, 0, destTransfers, 0, index);
				return destTransfers;
			} catch (CoreException e) {
				// Do not log core exceptions, they indicate the chosen file is not valid
				// WorkbenchPlugin.log(e.getMessage(), e);
			} catch (IOException e) {
				WorkbenchPlugin.log(e.getMessage(), e);
				return new PreferenceTransferElement[0];
			}
		}

		return new PreferenceTransferElement[0];
	}

	/**
	 * Return whether or not the file is valid.
	 *
	 * @return <code>true</code> of the file is an existing file and not a directory
	 */
	private boolean validFromFile() {
		File fromFile = new File(getDestinationValue());
		return fromFile.exists() && !fromFile.isDirectory();
	}

	@Override
	protected void setPreferenceTransfers() {
		super.setPreferenceTransfers();

		if (validFromFile() && (transfersTree.getViewer().getTree().getItemCount() == 0)) {
			descText.setText(PreferencesMessages.WizardPreferences_noSpecificPreferenceDescription);
		} else {
			descText.setText(""); //$NON-NLS-1$
		}
	}

	@Override
	protected void createTransferArea(Composite composite) {
		createDestinationGroup(composite);
		createTransfersList(composite);
	}

	/**
	 * Answer the string to display in self as the destination type
	 *
	 * @return java.lang.String
	 */
	@Override
	protected String getDestinationLabel() {
		return PreferencesMessages.WizardPreferencesImportPage1_file;
	}

	/**
	 * @return <code>true</code> if the transfer was successful, and
	 *         <code>false</code> otherwise
	 */
	@Override
	protected boolean transfer(IPreferenceFilter[] filters) {
		File importFile = new File(getDestinationValue());
		if (filters.length > 0) {
			try (FileInputStream fis = new FileInputStream(importFile)) {
				IPreferencesService service = Platform.getPreferencesService();
				try {
					IExportedPreferences prefs = service.readPreferences(fis);

					service.applyPreferences(prefs, filters);
				} catch (CoreException e) {
					WorkbenchPlugin.log(e.getMessage(), e);
					MessageDialog.open(MessageDialog.ERROR, getControl().getShell(), "", e.getLocalizedMessage(), //$NON-NLS-1$
							SWT.SHEET);
					return false;
				}
			} catch (IOException e) {
				WorkbenchPlugin.log(e.getMessage(), e);
				MessageDialog.open(MessageDialog.ERROR, getControl().getShell(), "", //$NON-NLS-1$
						e.getLocalizedMessage(), SWT.SHEET);
				return false;
			}
		}
		return true;
	}

	/**
	 * Handle events and enablements for widgets in this page
	 *
	 * @param e Event
	 */
	@Override
	public void handleEvent(Event e) {
		if (e.widget == destinationNameField) {
			setPreferenceTransfers();
		}

		super.handleEvent(e);
	}

	@Override
	protected String getFileDialogTitle() {
		return PreferencesMessages.WizardPreferencesImportPage1_title;
	}

	@Override
	protected int getFileDialogStyle() {
		return SWT.OPEN | SWT.SHEET;
	}

	@Override
	protected boolean validDestination() {
		return super.validDestination() && validFromFile();
	}

	@Override
	protected String getInvalidDestinationMessage() {
		return PreferencesMessages.WizardPreferencesImportPage1_invalidPrefFile;
	}

	@Override
	protected boolean shouldSaveTransferAll() {
		return false;
	}
}
