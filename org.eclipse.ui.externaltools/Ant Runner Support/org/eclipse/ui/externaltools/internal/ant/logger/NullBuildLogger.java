package org.eclipse.ui.externaltools.internal.ant.logger;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.io.PrintStream;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.Project;
import org.eclipse.ant.core.AntSecurityException;
import org.eclipse.ui.externaltools.internal.model.ToolMessages;
import org.eclipse.ui.externaltools.internal.ui.LogConsoleDocument;

public class NullBuildLogger implements BuildLogger {

	private int fMessageOutputLevel = LogConsoleDocument.MSG_INFO;
	private PrintStream fErr= null;
	private PrintStream fOut= null;
	
	/**
	 * An exception that has already been logged.	 */
	private Throwable fHandledException= null;
	
	/**
	 * @see org.apache.tools.ant.BuildLogger#setMessageOutputLevel(int)
	 */
	public void setMessageOutputLevel(int level) {
		fMessageOutputLevel= toConsolePriority(level);
	}
	
	protected int getMessageOutputLevel() {
		return fMessageOutputLevel;
	}

	/**
	 * @see org.apache.tools.ant.BuildLogger#setEmacsMode(boolean)
	 */
	public void setEmacsMode(boolean emacsMode) {
	}

	/**
	 * @see org.apache.tools.ant.BuildListener#buildStarted(org.apache.tools.ant.BuildEvent)
	 */
	public void buildStarted(BuildEvent event) {
	}

	/**
	 * @see org.apache.tools.ant.BuildListener#buildFinished(org.apache.tools.ant.BuildEvent)
	 */
	public void buildFinished(BuildEvent event) {
		handleException(event);
		fHandledException= null;
	}

	/**
	 * @see org.apache.tools.ant.BuildListener#targetStarted(org.apache.tools.ant.BuildEvent)
	 */
	public void targetStarted(BuildEvent event) {
	}

	/**
	 * @see org.apache.tools.ant.BuildListener#targetFinished(org.apache.tools.ant.BuildEvent)
	 */
	public void targetFinished(BuildEvent event) {
		handleException(event);
	}

	/**
	 * @see org.apache.tools.ant.BuildListener#taskStarted(org.apache.tools.ant.BuildEvent)
	 */
	public void taskStarted(BuildEvent event) {
	}

	/**
	 * @see org.apache.tools.ant.BuildListener#taskFinished(org.apache.tools.ant.BuildEvent)
	 */
	public void taskFinished(BuildEvent event) {
		handleException(event);
	}

	/**
	 * @see BuildListener#messageLogged(BuildEvent)
	 */
	public void messageLogged(BuildEvent event) {
		logMessage(event.getMessage(), toConsolePriority(event.getPriority()));
	}

	protected PrintStream getErrorPrintStream() {
		return fErr;
	}
	
	protected PrintStream getOutputPrintStream() {
		return fOut;
	}
	
	/**
	 * @see org.apache.tools.ant.BuildLogger#setErrorPrintStream(java.io.PrintStream)
	 */
	public void setErrorPrintStream(PrintStream err) {
		//this build logger logs to "null" unless
		//the user has explicitly set a logfile to use
		if (err == System.err) {
			fErr= null;
		} else {
			fErr= err;
		}
	}

	/**
	 * @see org.apache.tools.ant.BuildLogger#setOutputPrintStream(java.io.PrintStream)
	 */
	public void setOutputPrintStream(PrintStream output) {
		//this build logger logs to "null" unless
		//the user has explicitly set a logfile to use
		if (output == System.out) {
			fOut= null;
		} else {
			fOut= output;
		}
	}
	
	protected void logMessage(String message, int priority) {
		if (priority > getMessageOutputLevel()) {
			return;
		}
		
		if (priority == LogConsoleDocument.MSG_ERR) {
			if (getErrorPrintStream() != null && getErrorPrintStream() != System.err) {
				//user has designated to log to a logfile
				getErrorPrintStream().println(message);
			}
		} else {
			if (getOutputPrintStream() != null && getOutputPrintStream() != System.out) {
				//user has designated to log to a logfile
				getOutputPrintStream().println(message);
			} 
		}
	}
	
	protected void handleException(BuildEvent event) {
		Throwable exception = event.getException();
		if (exception == null || exception == fHandledException
		|| exception instanceof AntSecurityException) {
			return;
		}
		fHandledException= exception;
		logMessage(
			ToolMessages.format(
				"NullBuildLogger.buildException", //$NON-NLS-1$
				new String[] { exception.toString()}),
				LogConsoleDocument.MSG_ERR);	
	}
	
	/**
	 * Converts a Ant project's priority level to a priority
	 * level used by the Log Console.
	 */
	private int toConsolePriority(int antPriority) {
		switch (antPriority) {
			case Project.MSG_ERR:
				return LogConsoleDocument.MSG_ERR;
			case Project.MSG_WARN:
				return LogConsoleDocument.MSG_WARN;	
			case Project.MSG_INFO:
				return LogConsoleDocument.MSG_INFO;	
			case Project.MSG_VERBOSE:
				return LogConsoleDocument.MSG_VERBOSE;	
			case Project.MSG_DEBUG:
				return LogConsoleDocument.MSG_DEBUG;	
			default:
				return LogConsoleDocument.MSG_INFO;	
		}
	}
}
