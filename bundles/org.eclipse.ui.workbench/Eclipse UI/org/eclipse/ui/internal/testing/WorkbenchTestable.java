/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.testing;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.Workbench;
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
        	// don't use a job, since tests often wait for all jobs to complete before proceeding
            Runnable runnable = new Runnable() {
                public void run() {
                	// Some tests (notably the startup performance tests) do not want to wait for early startup.
                	// Allow this to be disabled by specifying the system property: org.eclipse.ui.testsWaitForEarlyStartup=false
                	// For details, see bug 94129 [Workbench] Performance test regression caused by workbench harness change
                	if (!"false".equalsIgnoreCase(System.getProperty(PlatformUI.PLUGIN_ID + ".testsWaitForEarlyStartup"))) {  //$NON-NLS-1$ //$NON-NLS-2$
                		waitForEarlyStartup();
                	}
                    getTestHarness().runTests();
                }
            };
            new Thread(runnable, "WorkbenchTestable").start(); //$NON-NLS-1$
        }
    }

    /**
     * Waits for the early startup job to complete.
     */
    private void waitForEarlyStartup() {
		try {
			Platform.getJobManager().join(Workbench.EARLY_STARTUP_FAMILY, null);
		} catch (OperationCanceledException e) {
			// ignore
		} catch (InterruptedException e) {
			// ignore
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
