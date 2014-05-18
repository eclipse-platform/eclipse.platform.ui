/*******************************************************************************
 * Copyright (c) 2014 fhv.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrej ten Brummelhuis <andrejbrummelhuis@gmail.com> - initial implementation (Bug 395283)
 *******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public abstract class SaveDialogBoundsSettingsDialog extends TitleAreaDialog {

	private static final String ORG_ECLIPSE_E4_TOOLS_EMF_UI = "org.eclipse.e4.tools.emf.ui"; //$NON-NLS-1$

	private static final String DIALOG_ORIGIN_X = "DIALOG_X_ORIGIN"; //$NON-NLS-1$
	private static final String DIALOG_ORIGIN_Y = "DIALOG_Y_ORIGIN"; //$NON-NLS-1$
	private static final String DIALOG_WIDTH = "DIALOG_WIDTH"; //$NON-NLS-1$
	private static final String DIALOG_HEIGHT = "DIALOG_HEIGHT"; //$NON-NLS-1$

	private IDialogSettings dialogSettings;

	private Preferences preferences = InstanceScope.INSTANCE.getNode(ORG_ECLIPSE_E4_TOOLS_EMF_UI);

	public SaveDialogBoundsSettingsDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		dialogSettings = new DialogSettings(ORG_ECLIPSE_E4_TOOLS_EMF_UI);
		dialogSettings.put(DIALOG_HEIGHT, preferences.getInt(DIALOG_HEIGHT, -1));
		dialogSettings.put(DIALOG_WIDTH, preferences.getInt(DIALOG_WIDTH, -1));
		dialogSettings.put(DIALOG_ORIGIN_X, preferences.getInt(DIALOG_ORIGIN_X, -1));
		dialogSettings.put(DIALOG_ORIGIN_Y, preferences.getInt(DIALOG_ORIGIN_Y, -1));
		return dialogSettings;
	}

	private void saveDialogSettings() {
		preferences.put(DIALOG_HEIGHT, dialogSettings.get(DIALOG_HEIGHT));
		preferences.put(DIALOG_WIDTH, dialogSettings.get(DIALOG_WIDTH));
		preferences.put(DIALOG_ORIGIN_X, dialogSettings.get(DIALOG_ORIGIN_X));
		preferences.put(DIALOG_ORIGIN_Y, dialogSettings.get(DIALOG_ORIGIN_Y));
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean close() {
		boolean returnValue = super.close();
		saveDialogSettings();
		return returnValue;
	}
}
