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
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.GridData;
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
	private static final String KEY_DESCRIPTION =
		"AppServerPreferencePage.description";
	private static final String PREFIX = UpdateUI.getPluginId();
	public static final String P_MASTER_SWITCH = PREFIX + ".appServer";
	public static final String P_ENCODE_URLS = PREFIX + ".encodeURLs";
	private static final String KEY_MASTER_SWITCH =
		"AppServerPreferencePage.masterSwitch";
	private static final String KEY_ENCODE_URLS =
		"AppServerPreferencePage.encodeURLs";
	//private MasterField masterField;
	private BooleanFieldEditor masterField;

/*
	class MasterField extends BooleanFieldEditor {
		BooleanFieldEditor slave;
		public MasterField(String property, String key, Composite parent) {
			super(property, key, parent);
		}

		protected void valueChanged(boolean oldValue, boolean newValue) {
			super.valueChanged(oldValue, newValue);
			slave.setEnabled(newValue, getFieldEditorParent());
		}

		void update() {
			slave.setEnabled(getBooleanValue(), getFieldEditorParent());
		}

		public void setSlave(BooleanFieldEditor slave) {
			this.slave = slave;
		}
	}
*/

	/**
	 * The constructor.
	 */
	public AppServerPreferencePage() {
		super(GRID);
		setPreferenceStore(UpdateUI.getDefault().getPreferenceStore());
		setDescription(UpdateUI.getString(KEY_DESCRIPTION));
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
		masterField =
			new BooleanFieldEditor(
				P_MASTER_SWITCH,
				UpdateUI.getString(KEY_MASTER_SWITCH),
				getFieldEditorParent());
		addField(masterField);
		/*
		BooleanFieldEditor encodeURLs =
			new BooleanFieldEditor(
				P_ENCODE_URLS,
				UpdateUI.getString(KEY_ENCODE_URLS),
				getFieldEditorParent());
		addField(encodeURLs);
		masterField.setSlave(encodeURLs);
		*/
	}
	
	public void createControl(Composite parent) {
		super.createControl(parent);
		Dialog.applyDialogFont(getControl());
		WorkbenchHelp.setHelp(
			parent,"org.eclipse.update.ui.AppServerPreferencePage");
	}	
/*
	protected void initialize() {
		super.initialize();
		masterField.update();
	}
*/
	protected void createSpacer(Composite composite, int columnSpan) {
		Label label = new Label(composite, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = columnSpan;
		label.setLayoutData(gd);
	}
	public static boolean getUseApplicationServer() {
		IPreferenceStore store =
			UpdateUI.getDefault().getPreferenceStore();
		return store.getBoolean(P_MASTER_SWITCH);
	}

	public static boolean getEncodeURLs() {
		return true;
		/*
		IPreferenceStore store =
			UpdateUI.getDefault().getPreferenceStore();
		return store.getBoolean(P_ENCODE_URLS);
		*/
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
		boolean masterSwitch = getUseApplicationServer();
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