/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.testing;

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.testing.TestableObject;

/**
 * The Workbench's testable object facade to a test harness.
 * 
 * @since 3.0
 */
public class WorkbenchTestable extends TestableObject {

    private Display display;

    private IWorkbench workbench;

    private boolean oldAutomatedMode;

    private boolean oldIgnoreErrors;

    /**
     * Constructs a new workbench testable object.
     */
    public WorkbenchTestable() {
        // do nothing
    }

    /**
     * Initializes the workbench testable with the display and workbench,
     * and notifies all listeners that the tests can be run.
     * 
     * @param display the display
     * @param workbench the workbench
     */
    public void init(Display display, IWorkbench workbench) {
        Assert.isNotNull(display);
        Assert.isNotNull(workbench);
        this.display = display;
        this.workbench = workbench;
        if (getTestHarness() != null) {
            Runnable runnable = new Runnable() {
                public void run() {
                    getTestHarness().runTests();
                }
            };
            new Thread(runnable, "WorkbenchTestable").start(); //$NON-NLS-1$
        }
    }

    /**
     * The <code>WorkbenchTestable</code> implementation of this 
     * <code>TestableObject</code> method ensures that the workbench
     * has been set.
     */
    public void testingStarting() {
        Assert.isNotNull(workbench);
        oldAutomatedMode = ErrorDialog.AUTOMATED_MODE;
        ErrorDialog.AUTOMATED_MODE = true;
        oldIgnoreErrors = SafeRunnable.getIgnoreErrors();
        SafeRunnable.setIgnoreErrors(true);
    }

    /**
     * The <code>WorkbenchTestable</code> implementation of this 
     * <code>TestableObject</code> method flushes the event queue,
     * runs the test in a <code>syncExec</code>, then flushes the
     * event queue again.
     */
    public void runTest(Runnable testRunnable) {
        Assert.isNotNull(workbench);
        display.syncExec(testRunnable);
    }

    /**
     * The <code>WorkbenchTestable</code> implementation of this 
     * <code>TestableObject</code> method flushes the event queue, 
     * then closes the workbench.
     */
    public void testingFinished() {
        // force events to be processed, and ensure the close is done in the UI thread
        display.syncExec(new Runnable() {
            public void run() {
                Assert.isTrue(workbench.close());
            }
        });
        ErrorDialog.AUTOMATED_MODE = oldAutomatedMode;
        SafeRunnable.setIgnoreErrors(oldIgnoreErrors);
    }
}