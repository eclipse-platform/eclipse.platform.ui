package org.eclipse.help.internal.util;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.*;

/**
 * Generic Help System Logger class for handling logging and tracing.
 *
 * debug options are determined by HelpSystem debug_level field:
 *
 * DEBUG boolean flag for optimizing out heavy duty Level 3 developer messages
 */
public class Logger {
	// This SHOULD be set to false in production
	public final static boolean DEBUG = false;

	// Cashed workbenchPlugin Log, LogListener instances
	private static ILog helpSystemLog = null;
	private static HelpLogListener helpLogListener = null;

	// Cashed workbenchPlugin ID 
	private static String workbenchPluginID = null;

	// Controls logging level
	private static int debug_level = 0;

	// Captures initialization errors
	private static boolean init_ok = true;

	static {
		initialize();
	}

	private static void initialize() {
		try {
			// get the debug level from the HelpSystem instance
			debug_level = HelpSystem.getDebugLevel();

			// get unique pluging ID and cash it for later use.
			Plugin workbenchPlugin = HelpPlugin.getDefault();//HelpSystem.getPlugin();
			workbenchPluginID = workbenchPlugin.getDescriptor().getUniqueIdentifier();
			helpSystemLog = workbenchPlugin.getLog();
			if (helpLogListener == null)
				helpLogListener = new HelpLogListener();
			helpSystemLog.addLogListener(helpLogListener);

		} catch (Exception e) {
			// Errors occured during initialize, disable logging 
			init_ok = false;
		}
	}
	/* 
	 * Log a Debug message. This is intended to be wrapped as follows:
	 * if (Logger.DEBUG)
	 *      Logger.logDebugMessage("someClassName", "someMessage");
	 *
	 * and the output will be:
	 *
	 * ---------------------------------------------------------------
	 * DEBUG  org.eclipse.help.ui  someClassName
	 *   someMessage
	 * ---------------------------------------------------------------
	 *
	 * 
	 * Note that since this message is only for developer debugging, it does not 
	 * need to be localized to proper local.
	 */

	public static synchronized void logDebugMessage(
		String className,
		String message) {
		if ((init_ok) && (debug_level >= HelpSystem.LOG_DEBUG)) {
			// ie: print all INFO, WARNING and ERROR messages
			MultiStatus debugStatus =
				new MultiStatus(workbenchPluginID, IStatus.OK, className, null);
			Status infoStatus =
				new Status(IStatus.OK, workbenchPluginID, IStatus.OK, message, null);
			debugStatus.add(infoStatus);
			helpSystemLog.log(debugStatus);
		}
	}
	/* 
	 * Log an Error message with an exception. Note that the message should already 
	 * be localized to proper local.
	 * ie: Resource.getString() should already have been called
	 */

	public static synchronized void logError(String message, Throwable ex) {
		if ((init_ok) && (debug_level >= HelpSystem.LOG_ERROR)) {
			// ie: print only ERROR messages
			if (message == null)
				message = "";
			Status errorStatus =
				new Status(IStatus.ERROR, workbenchPluginID, IStatus.OK, message, ex);
			helpSystemLog.log(errorStatus);

		}
	}
	/* 
	 * Log an Information message with an exception. Note that the message should already 
	 * be localized to proper local.
	 * ie: Resource.getString() should already have been called
	 */

	public static synchronized void logInfo(String message) {
		if ((init_ok) && (debug_level >= HelpSystem.LOG_DEBUG)) {
			if (message == null)
				message = "";
			// ie: print all INFO, WARNING and ERROR messages
			Status infoStatus =
				new Status(IStatus.INFO, workbenchPluginID, IStatus.OK, message, null);
			helpSystemLog.log(infoStatus);

		}

	}
	/* 
	 * Log a Warning message with an exception. Note that the message should already 
	 * be localized to proper local.
	 * ie: Resource.getString() should already have been called
	 */

	public static synchronized void logWarning(String message) {
		if ((init_ok) && (debug_level >= HelpSystem.LOG_WARNING)) {
			if (message == null)
				message = "";
			// ie: print all WARNING and ERROR messages
			Status warningStatus =
				new Status(IStatus.WARNING, workbenchPluginID, IStatus.OK, message, null);
			helpSystemLog.log(warningStatus);
		}

	}
	public static synchronized void setDebugLevel(int level) {
		debug_level = level;
	}
	public static void shutdown() {
		helpLogListener.shutdown();
	}
}
