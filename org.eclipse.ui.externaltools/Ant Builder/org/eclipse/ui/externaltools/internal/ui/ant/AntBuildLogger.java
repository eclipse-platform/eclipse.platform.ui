package org.eclipse.ui.externaltools.internal.ui.ant;

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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.externaltools.internal.core.ToolMessages;
import org.eclipse.ui.externaltools.internal.ui.LogConsoleDocument;
import org.eclipse.ui.externaltools.internal.ui.OutputStructureElement;

public class AntBuildLogger implements BuildLogger {

	protected int priorityFilter = LogConsoleDocument.MSG_INFO;
	private int logLength = 0;
	private int lastTargetEndIndex = 0;
	
	private PrintStream fErr= null;
	private PrintStream fOut= null;

	public AntBuildLogger() {
	}

	/**
	 * @see BuildListener#buildStarted(BuildEvent)
	 */
	public void buildStarted(BuildEvent event) {
	}

	/**
	 * @see BuildListener#buildFinished(BuildEvent)
	 */
	public void buildFinished(BuildEvent event) {
		handleException(event);
	}

	protected void handleException(BuildEvent event) {
		Throwable exception = event.getException();
		if (exception == null)
			return;
		logMessage(
			ToolMessages.format(
				"AntBuildLogger.buildException", // $NON-NLS-1$
				new String[] { exception.toString()}),
			LogConsoleDocument.MSG_ERR);	
	}

	/**
	 * @see BuildListener#targetStarted(BuildEvent)
	 */
	public void targetStarted(BuildEvent event) {
		createNewOutputStructureElement(event.getTarget().getName(), logLength);
	}

	protected void refreshConsoleTrees() {
		final LogConsoleDocument doc = LogConsoleDocument.getInstance();
		if (!doc.hasViews())
			return;
		// we get the display from the console #0 (that exists for sure because consoles!=null)
		Display display = doc.getDisplay();
		// create a new thread for synchronizing all the refresh operations
		display.syncExec(new Runnable() {
			public void run() {
				doc.refreshTree();
			}
		});
	}

	protected void createNewOutputStructureElement(String name, int index) {
		LogConsoleDocument doc = LogConsoleDocument.getInstance();
		OutputStructureElement newElement =
			new OutputStructureElement(
				name,
				doc.getCurrentOutputStructureElement(),
				index);
		doc.setCurrentOutputStructureElement(newElement);
	}

	/**
	 * @see BuildListener#targetFinished(BuildEvent)
	 */
	public void targetFinished(BuildEvent event) {
		handleException(event);
		finishCurrentOutputStructureElement();
		// store the end index of this target's log (so that we can use it later)
		lastTargetEndIndex = logLength;
		refreshConsoleTrees();
	}

	/**
	 * @see BuildListener#taskStarted(BuildEvent)
	 */
	public void taskStarted(BuildEvent event) {
		createNewOutputStructureElement(event.getTask().getTaskName());
	}

	/**
	 * @see BuildListener#taskFinished(BuildEvent)
	 */
	public void taskFinished(BuildEvent event) {
		handleException(event);
		finishCurrentOutputStructureElement();
		refreshConsoleTrees();
	}

	/**
	 * @see BuildListener#messageLogged(BuildEvent)
	 */
	public void messageLogged(BuildEvent event) {
		logMessage(event.getMessage(), toConsolePriority(event.getPriority()));
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

	protected void logMessage(String message, int priority) {
		if (priority > priorityFilter) {
			return;
		}
		
		if (priority == LogConsoleDocument.MSG_ERR) {
			if (fErr != null && fErr != System.err) {
				//user has designated to log to a logfile
				fErr.println(message);
				return;
			}
		} else {
			if (fOut != null && fOut != System.out) {
				//user has designated to log to a logfile
				fOut.println(message);
				return;
			} 
		}
		message += '\n';
		LogConsoleDocument doc = LogConsoleDocument.getInstance();
		doc.append(message, priority);
		logLength += message.length();
	}

	protected void finishCurrentOutputStructureElement() {
		LogConsoleDocument doc = LogConsoleDocument.getInstance();
		// sets the index that indicates the end of the log part linked to this element
		OutputStructureElement output = doc.getCurrentOutputStructureElement();
		output.setEndIndex(logLength);
		// and sets the current element to the parent of the element
		doc.setCurrentOutputStructureElement(output.getParent());
	}

	protected void createNewOutputStructureElement(String name) {
		createNewOutputStructureElement(name, logLength);
	}

	/**
	 * @see org.apache.tools.ant.BuildLogger#setMessageOutputLevel(int)
	 */
	public void setMessageOutputLevel(int level) {
		this.priorityFilter = toConsolePriority(level);
	}

	/**
	 * @see org.apache.tools.ant.BuildLogger#setEmacsMode(boolean)
	 */
	public void setEmacsMode(boolean emacsMode) {
	}

	/**
	 * @see org.apache.tools.ant.BuildLogger#setErrorPrintStream(java.io.PrintStream)
	 */
	public void setErrorPrintStream(PrintStream err) {
		fErr= err;
	}

	/**
	 * @see org.apache.tools.ant.BuildLogger#setOutputPrintStream(java.io.PrintStream)
	 */
	public void setOutputPrintStream(PrintStream output) {
		fOut= output;
	}
	
	private IProgressMonitor monitorFor(IProgressMonitor monitor) {
		if (monitor == null) {
			return new NullProgressMonitor();
		}
		return monitor;
	}
}