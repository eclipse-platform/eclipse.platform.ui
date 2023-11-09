/*******************************************************************************
 * Copyright (c) 2014 fhv.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrej ten Brummelhuis <andrejbrummelhuis@gmail.com> - initial implementation (Bug 395283)
 *     Marco Descher <marco@descher.at> - Bug 442647
 *******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.tools.emf.ui.common.Plugin;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public abstract class SaveDialogBoundsSettingsDialog extends TitleAreaDialog {

	private static final String DIALOG_WIDTH = "DIALOG_WIDTH"; //$NON-NLS-1$
	private static final String DIALOG_HEIGHT = "DIALOG_HEIGHT"; //$NON-NLS-1$
	private static final int DIALOG_MINIMUM_HEIGHT = 300;
	private static final int DIALOG_MINIMUM_WIDTH = 400;

	private final IDialogSettings dialogSettings = new DialogSettings(Plugin.ID);

	private final Preferences preferences = InstanceScope.INSTANCE.getNode(Plugin.ID);

	public SaveDialogBoundsSettingsDialog(Shell parentShell) {
		super(parentShell);

		dialogSettings.put(DIALOG_HEIGHT, preferences.getInt(DIALOG_HEIGHT, -1));
		dialogSettings.put(DIALOG_WIDTH, preferences.getInt(DIALOG_WIDTH, -1));
	}

	private void saveDialogSettings() {
		preferences.put(DIALOG_HEIGHT, dialogSettings.get(DIALOG_HEIGHT));
		preferences.put(DIALOG_WIDTH, dialogSettings.get(DIALOG_WIDTH));
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setMinimumSize(DIALOG_MINIMUM_WIDTH, DIALOG_MINIMUM_HEIGHT);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	public boolean close() {
		boolean returnValue = super.close();
		saveDialogSettings();
		return returnValue;
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		return dialogSettings;
	}

	@Override
	protected int getDialogBoundsStrategy() {
		return DIALOG_PERSISTSIZE;
	}

	public Preferences getPreferences() {
		return preferences;
	}
}
