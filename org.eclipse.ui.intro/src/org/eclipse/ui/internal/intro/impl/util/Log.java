/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.intro.impl.util;

import org.eclipse.core.runtime.*;
import org.eclipse.ui.internal.intro.impl.*;

/**
 * Utility class for logging, based on Platform logging classes. The log
 * listerner used is the base one supplied by the platform. Error messages are
 * always logged. Warning messages are only logged when the plugin is in debug
 * mode. Info messages are only logged when the /trace/logInfo debug option is
 * set to true.
 *  
 */
public class Log implements IIntroConstants {

    /**
     * This MUST be set to <b>false </b> in production. <br>
     * Used to compile out developement debug messages. <br>
     */
    public static final boolean DEBUG = true;

    /**
     * Flag that controls logging of information messages
     */
    private static boolean logInfo = false;

    private final static ILog pluginLog = IntroPlugin.getDefault().getLog();

    static {
        // init debug options based on settings defined in ".options" file. If
        // the plugin is not in debug mode, no point setting debug options.
        if (IntroPlugin.getDefault().isDebugging()) {
            logInfo = getDebugOption("/trace/logInfo"); //$NON-NLS-1$
        }

    }

    private static boolean getDebugOption(String option) {
        return "true".equalsIgnoreCase(//$NON-NLS-1$
                Platform.getDebugOption(PLUGIN_ID + option));
    }

    /**
     * Log an Error message with an exception. Note that the message should
     * already be localized to proper local. Errors are always logged.
     */
    public static synchronized void error(String message, Throwable ex) {
        if (message == null)
            message = ""; //$NON-NLS-1$
        Status errorStatus = new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK,
                message, ex);
        pluginLog.log(errorStatus);
    }

    /**
     * Log an Information message. Note that the message should already be
     * localized to proper local. Info messages are only logged when the
     * /trace/logInfo debug option is true.
     */
    public static synchronized void info(String message) {
        if (!logInfo)
            // logging of info messages is not enabled.
            return;

        if (message == null)
            message = ""; //$NON-NLS-1$
        Status infoStatus = new Status(IStatus.INFO, PLUGIN_ID, IStatus.OK,
                message, null);
        pluginLog.log(infoStatus);
    }

    /**
     * Log a Warning message. Note that the message should already be localized
     * to proper local. Warning messages are only logged when the plugin is in
     * debug mode.
     */
    public static synchronized void warning(String message) {
        if (!IntroPlugin.getDefault().isDebugging())
            // plugin is not in debug mode. Default is to not log warning
            // messages.
            return;

        if (message == null)
            message = ""; //$NON-NLS-1$
        Status warningStatus = new Status(IStatus.WARNING, PLUGIN_ID,
                IStatus.OK, message, null);
        pluginLog.log(warningStatus);
    }

    /**
     * Log a development debug message. Debug messages are compiled out.
     */
    public static synchronized void debugMessage(String className,
            String message) {
        if (DEBUG) {
            MultiStatus debugStatus = new MultiStatus(PLUGIN_ID, IStatus.OK,
                    className, null);
            Status infoStatus = new Status(IStatus.OK, PLUGIN_ID, IStatus.OK,
                    message, null);
            debugStatus.add(infoStatus);
            pluginLog.log(debugStatus);
        }
    }
}