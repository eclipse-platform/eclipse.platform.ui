/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.ui.tests.harness.util.DialogCheck;

public class DeprecatedUIDialogs extends TestCase {

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
                new WorkbenchPartLabelProvider(), WorkbenchMessages.EditorManager_saveResourcesMessage);
        dialog.setTitle(WorkbenchMessages.EditorManager_saveResourcesTitle);
        DialogCheck.assertDialog(dialog, this);
    }

}

