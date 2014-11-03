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
package org.eclipse.ui.tests.compare;

import junit.framework.TestCase;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.tests.dialogs.PreferenceDialogWrapper;
import org.eclipse.ui.tests.harness.util.DialogCheck;

public class UIComparePreferences extends TestCase {

    public UIComparePreferences(String name) {
        super(name);
    }

    private Shell getShell() {
        return DialogCheck.getShell();
    }

    private PreferenceDialog getPreferenceDialog(String id) {
        PreferenceDialogWrapper dialog = null;
        PreferenceManager manager = WorkbenchPlugin.getDefault()
                .getPreferenceManager();
        if (manager != null) {
            dialog = new PreferenceDialogWrapper(getShell(), manager);
            dialog.create();
            WorkbenchHelp.setHelp(dialog.getShell(),
                    IWorkbenchHelpContextIds.PREFERENCE_DIALOG);

            for (Object element : manager.getElements(
                    PreferenceManager.PRE_ORDER)) {
            IPreferenceNode node = (IPreferenceNode) element;
            if (node.getId().equals(id)) {
			dialog.showPage(node);
			break;
            }
         }
        }
        return dialog;
    }

    public void testCompareViewersPref() {
        Dialog dialog = getPreferenceDialog("org.eclipse.compare.internal.ComparePreferencePage");
        DialogCheck.assertDialog(dialog, this);
    }

}
