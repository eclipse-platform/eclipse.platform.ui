/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.ILogger;
import org.eclipse.jface.util.ISafeRunnableRunner;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.util.SafeRunnable;

/**
 * Utility class for setting up JFace for use by Eclipse.
 * 
 * @since 3.1
 */
final class JFaceUtil {

    private JFaceUtil() {
        // prevents intantiation
    }

    /**
     * Initializes JFace for use by Eclipse.
     */
    public static void initializeJFace() {
        // Set the Platform to run all SafeRunnables
        SafeRunnable.setRunner(new ISafeRunnableRunner() {
            public void run(ISafeRunnable code) {
                Platform.run(code);
            }
        });

        // Log all errors to the main runtime log
        Policy.setLog(new ILogger() {
            public void log(IStatus status) {
                WorkbenchPlugin.getDefault().getLog().log(status);
            }
        });

        // Get all debug options from Platform
        if ("true".equalsIgnoreCase(Platform.getDebugOption("/debug"))) { //$NON-NLS-1$ //$NON-NLS-2$
            Policy.DEBUG_DIALOG_NO_PARENT = "true".equalsIgnoreCase(Platform.getDebugOption(Policy.JFACE + "/debug/dialog/noparent")); //$NON-NLS-1$ //$NON-NLS-2$
            Policy.TRACE_ACTIONS = "true".equalsIgnoreCase(Platform.getDebugOption(Policy.JFACE + "/trace/actions")); //$NON-NLS-1$ //$NON-NLS-2$
            Policy.TRACE_TOOLBAR = "true".equalsIgnoreCase(Platform.getDebugOption(Policy.JFACE + "/trace/toolbarDisposal")); //$NON-NLS-1$ //$NON-NLS-2$
        }

    }
}
