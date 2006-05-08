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
package org.eclipse.ui.tests.dialogs;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.tests.harness.util.DialogCheck;

public class UIErrorDialogs extends TestCase {

    public UIErrorDialogs(String name) {
        super(name);
    }

    private Shell getShell() {
        return DialogCheck.getShell();
    }

    /*
     * Get an example ErrorDialog with a status and a couple of 
     * child statuses.
     */
    private ErrorDialog getMultiStatusErrorDialog() {

        IStatus[] childStatuses = new IStatus[2];
        childStatuses[0] = new Status(IStatus.ERROR, "org.eclipse.ui.tests",
                IStatus.ERROR, "Error message 1", new Throwable());
        childStatuses[1] = new Status(IStatus.ERROR, "org.eclipse.ui.tests",
                IStatus.ERROR, "Error message 2", new Throwable());
        MultiStatus mainStatus = new MultiStatus("org.eclipse.ui.tests",
                IStatus.ERROR, childStatuses, "Main error", new Throwable());

        return new ErrorDialog(getShell(), "Error Test", "Error message",
                mainStatus, IStatus.ERROR);
    }

    public void testErrorClipboard() {
        Dialog dialog = getMultiStatusErrorDialog();
        DialogCheck.assertDialog(dialog, this);
    }

}
