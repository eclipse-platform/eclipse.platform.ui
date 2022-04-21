/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
package org.eclipse.ui.tests.manual;

import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.tests.dialogs.PreferenceDialogWrapper;
import org.eclipse.ui.tests.dialogs.UIPreferencesAuto;

/**
 * The UIPreferencesManual is a test case that requires
 * the user to click OK on message dialog when it is
 * run
 */

public class UIPreferencesManual extends UIPreferencesAuto {

	/**
	 * Test the bad update preference page by generating all
	 * of the dialog errors.
	 */
	public void testBrokenListenerPref() {

		PreferenceDialogWrapper dialog = null;
		PreferenceManager manager = WorkbenchPlugin.getDefault()
				.getPreferenceManager();
		if (manager != null) {
			dialog = new PreferenceDialogWrapper(getShell(), manager);
			dialog.create();

			for (IPreferenceNode node : manager.getElements(PreferenceManager.PRE_ORDER)) {
				if (node.getId().equals("org.eclipse.ui.tests.manual.BrokenUpdatePreferencePage")) {
					dialog.showPage(node);
					BrokenUpdatePreferencePage page = (BrokenUpdatePreferencePage) dialog.getPage(node);
					page.changeFont();
					page.changePluginPreference();
					break;
				}
			}
		}

	}

}
