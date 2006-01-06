/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.commands;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.ExternalActionManager;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * A tests whether is active will log an exception if the command is not
 * defined.
 * 
 * @since 3.1
 */
public final class Bug73756Test extends UITestCase {

    /**
     * Whether a log message has occurred.
     */
    private boolean logWritten = false;

    /**
     * Constructs a new instance of <code>Bug73756Test</code>.
     * 
     * @param name
     *            The name of the test
     */
    public Bug73756Test(final String name) {
        super(name);
    }

    /**
     * Tests that calling <code>isActive()</code> on an undefined command
     * causes a log message to be written. This simple calls
     * <code>isActive()</code> for a bogus command identifier. A log listener
     * flips a boolean flag if a log message is written.
     */
    public final void testUndefinedCommandIsActiveLogged() {
        // Attach the log listener.
        final ILog log = WorkbenchPlugin.getDefault().getLog();
        final ILogListener logListener = new ILogListener() {
            public void logging(IStatus status, String plugin) {
                logWritten = true;
            }
        };
        log.addLogListener(logListener);

        // Check if a bogus command is active.
        ExternalActionManager.getInstance().getCallback().isActive(
                "a command that is not defined");

        // Check to see if the log was written to.
        assertTrue("A warning should have been logged.", logWritten);
    }
}
