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
package org.eclipse.update.internal.ui.preferences;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.jface.dialogs.Dialog;

/**
 * Insert the type's description here.
 * @see PreferencePage
 */
public class AppServerPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {
	//private BooleanFieldEditor masterField;


	/**
	 * The constructor.
	 */
	public AppServerPreferencePage() {
		super(GRID);
		setPreferenceStore(UpdateUI.getDefault().getPreferenceStore());
		setDescription(UpdateUI.getString("AppServerPreferencePage.description"));
	}

	/**
	 * Insert the method's description here.
	 * @see PreferencePage#init
	 */
	public void init(IWorkbench workbench) {
	}
	public void createFieldEditors() {
		WorkbenchHelp.setHelp(
			getFieldEditorParent(),
			"org.eclipse.update.ui.AppServerPreferencePage");
		addField(
			new BooleanFieldEditor(
				UpdateUI.P_MASTER_SWITCH,
				UpdateUI.getString("AppServerPreferencePage.masterSwitch"),
				getFieldEditorParent()));
	}
	
	public void createControl(Composite parent) {
		super.createControl(parent);
		Dialog.applyDialogFont(getControl());
		WorkbenchHelp.setHelp(
			parent,"org.eclipse.update.ui.AppServerPreferencePage");
	}	


	public boolean performOk() {
		boolean result = super.performOk();
		if (result) {
			final boolean bag[] = new boolean[1];
			BusyIndicator.showWhile(getControl().getDisplay(), new Runnable() {
				public void run() {
					try {
						handleServerActivation();
						bag[0] = true;
					} catch (CoreException e) {
						UpdateUI.logException(e);
						bag[0] = false;
					}
				}
			});
			result = bag[0];
		}
		if (result)
			UpdateUI.getDefault().savePluginPreferences();
		return result;
	}

	private void handleServerActivation() throws CoreException {
		boolean masterSwitch = getPreferenceStore().getBoolean(UpdateUI.P_MASTER_SWITCH);
		boolean webAppRunning = UpdateUI.getDefault().isWebAppStarted();

		if (!masterSwitch && webAppRunning) {
			// remove Web app
			UpdateUI.getDefault().stopWebApp();
		} else if (masterSwitch && !webAppRunning) {
			// add Web app
			UpdateUI.getDefault().startWebApp();
		}
	}
}