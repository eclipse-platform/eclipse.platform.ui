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

import junit.framework.TestCase;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.YesNoCancelListSelectionDialog;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.model.AdaptableList;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchPartLabelProvider;
import org.eclipse.ui.tests.util.DialogCheck;

public class DeprecatedUIDialogs extends TestCase {
    private static final String PROJECT_SELECTION_MESSAGE = WorkbenchMessages
            .getString("BuildOrderPreference.selectOtherProjects");

    private static final String FILTER_SELECTION_MESSAGE = ResourceNavigatorMessagesCopy
            .getString("FilterSelection.message");

    public DeprecatedUIDialogs(String name) {
        super(name);
    }

    private Shell getShell() {
        return DialogCheck.getShell();
    }

    public void testSaveAll() {
        YesNoCancelListSelectionDialog dialog = new YesNoCancelListSelectionDialog(
                getShell(), new AdaptableList(),
                new WorkbenchContentProvider(),
                new WorkbenchPartLabelProvider(), WorkbenchMessages
                        .getString("EditorManager.saveResourcesMessage"));
        dialog.setTitle(WorkbenchMessages
                .getString("EditorManager.saveResourcesTitle"));
        DialogCheck.assertDialog(dialog, this);
    }

}

