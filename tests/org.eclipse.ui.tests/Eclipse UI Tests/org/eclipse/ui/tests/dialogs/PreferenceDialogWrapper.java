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
package org.eclipse.ui.tests.dialogs;

import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.widgets.Shell;

public class PreferenceDialogWrapper extends PreferenceDialog {

    public PreferenceDialogWrapper(Shell parentShell, PreferenceManager manager) {
        super(parentShell, manager);
    }

    public boolean showPage(IPreferenceNode node) {
        return super.showPage(node);
    }

    public IPreferencePage getPage(IPreferenceNode node) {
        if (node == null)
            return null;

        // Create the page if nessessary
        if (node.getPage() == null)
            node.createPage();

        if (node.getPage() == null)
            return null;

        return node.getPage();
    }
}