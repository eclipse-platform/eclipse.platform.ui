/*******************************************************************************
 * Copyright (c) 2019 SAP SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Christian Georgi (SAP SE) - Bug 540440
 *******************************************************************************/
package org.eclipse.ui.internal.keys.show;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * Toggles whether to show keyboard shortcuts
 */
public class ShowKeysToggleHandler extends AbstractHandler {

	public static final String COMMAND_ID = "org.eclipse.ui.toggleShowKeys"; //$NON-NLS-1$
	private static ShowKeysUI showKeysUI;

	@Override
	public Object execute(ExecutionEvent event) {
		IPreferenceStore prefStore = WorkbenchPlugin.getDefault().getPreferenceStore();
		boolean newValue = toggleValue(IPreferenceConstants.SHOW_KEYS_ENABLED_FOR_KEYBOARD, prefStore);
		// deliberately keep both values the same, i.e. set the second pref to the same
		// value
		prefStore.setValue(IPreferenceConstants.SHOW_KEYS_ENABLED_FOR_MOUSE_EVENTS, newValue);
		if (newValue) {
			showPreview(prefStore);
		}
		return newValue;
	}

	private boolean toggleValue(String key, IPreferenceStore prefStore) {
		boolean newValue = !prefStore.getBoolean(key);
		prefStore.setValue(key, newValue);
		return newValue;
	}

	private void showPreview(IPreferenceStore prefStore) {
		if (showKeysUI == null) {
			// keep a singleton so that multiple quick invocations of this command
			// do not end up in multiple popups
			showKeysUI = new ShowKeysUI(PlatformUI.getWorkbench(), prefStore);
		}
		showKeysUI.openForPreview(ShowKeysToggleHandler.COMMAND_ID, null);
	}

}
