/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Chris Gross (schtoo@schtoo.com) - support for ILogger added
 *       (bug 49497 [RCP] JFace dependency on org.eclipse.core.runtime enlarges standalone JFace applications)
 *******************************************************************************/
package org.eclipse.jface.util;

import org.eclipse.core.runtime.IStatus;

/**
 * The Policy class handles debug flags and logging within JFace.
 * 
 * @since 3.0
 */
public class Policy {

    /**
     * Constant for the the default setting for debug options.
     */
    public static final boolean DEFAULT = false;

    /**
     * The unique identifier of the JFace plug-in.
     */
    public static final String JFACE = "org.eclipse.jface";//$NON-NLS-1$

    private static ILogger log;

    /**
     * A flag to indicate whether unparented dialogs should
     * be checked.
     */
    public static boolean DEBUG_DIALOG_NO_PARENT = DEFAULT;

    /**
     * A flag to indicate whether actions are being traced.
     */
    public static boolean TRACE_ACTIONS = DEFAULT;
    
    /**
     * A boolean to determine if we are showing the new look preferences
     * dialog.
     */
    public static boolean SHOW_PREFERENCES_NEWLOOK = DEFAULT;

    /**
     * A flag to indicate whether toolbars are being traced.
     */

    public static boolean TRACE_TOOLBAR = DEFAULT;


    /**
     * Returns the dummy log to use if none has been set
     */
    private static ILogger getDummyLog() {
        return new ILogger() {
            public void log(IStatus status) {
                System.err.println(status.getMessage());
            }
        };
    }



    /**
     * Sets the logger used by JFace to log errors.
     * 
     * @param logger the logger to use, or <code>null</code> to use the default logger
     * @since 3.1
     */
    public static void setLog(ILogger logger) {
        log = logger;
    }

    /**
     * Returns the logger used by JFace to log errors.
     * <p>
     * The default logger prints the status to <code>System.err</code>.
     * </p>
     * 
     * @return the logger
     * @since 3.1
     */
    public static ILogger getLog() {
        if (log == null)
            log = getDummyLog();
        return log;
    }

}
