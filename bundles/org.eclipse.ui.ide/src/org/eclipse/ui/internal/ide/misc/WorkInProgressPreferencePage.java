/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.misc;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.internal.WorkbenchPlugin;
/**
 * Temporary "Work in Progress" PreferencePage for Job control
 * 
 * @since 3.0
 */
public class WorkInProgressPreferencePage extends PreferencePage
		implements
			IWorkbenchPreferencePage {
	Button newProgressPreference;
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite spacer = new Composite(parent, SWT.NONE);
		createNewProgressControls(spacer);
		GridData spacerData = new GridData(GridData.FILL_BOTH
				| GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		spacer.setLayoutData(spacerData);
		spacer.setLayout(new GridLayout());
		return spacer;
	}
	private void createNewProgressControls(Composite parent) {
		newProgressPreference = new Button(parent, SWT.CHECK | SWT.TOP);
		newProgressPreference.setText("Use new progress view"); //$NON-NLS-1$
		newProgressPreference.setSelection(WorkbenchPlugin.getDefault()
				.getPreferenceStore().getBoolean("USE_NEW_PROGRESS"));//$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		//Nothing to do here
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		Platform.getPlugin(Platform.PI_RUNTIME).savePluginPreferences();
		WorkbenchPlugin.getDefault().getPreferenceStore().setValue(
				"USE_NEW_PROGRESS", newProgressPreference.getSelection());//$NON-NLS-1$
		return super.performOk();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		newProgressPreference.setSelection(false);
	}
}