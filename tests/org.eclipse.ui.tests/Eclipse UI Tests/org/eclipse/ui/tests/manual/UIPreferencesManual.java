/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.manual;

import java.util.Iterator;

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

    public UIPreferencesManual(String name) {
        super(name);
    }

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

            for (Iterator iterator = manager.getElements(
                    PreferenceManager.PRE_ORDER).iterator(); iterator.hasNext();) {
                IPreferenceNode node = (IPreferenceNode) iterator.next();
                if (node
                        .getId()
                        .equals(
                                "org.eclipse.ui.tests.manual.BrokenUpdatePreferencePage")) {
                    dialog.showPage(node);
                    BrokenUpdatePreferencePage page = (BrokenUpdatePreferencePage) dialog
                            .getPage(node);
                    page.changeFont();
                    page.changePluginPreference();
                    break;
                }
            }
        }

    }

}
