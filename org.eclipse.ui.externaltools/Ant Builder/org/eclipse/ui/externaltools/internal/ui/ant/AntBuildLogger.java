package org.eclipse.ui.externaltools.internal.ui.ant;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import java.io.PrintStream;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.Project;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.externaltools.internal.core.AntUtil;
import org.eclipse.ui.externaltools.internal.core.ToolMessages;
import org.eclipse.ui.externaltools.internal.ui.BuildCanceledException;
import org.eclipse.ui.externaltools.internal.ui.LogConsoleDocument;
import org.eclipse.ui.externaltools.internal.ui.OutputStructureElement;

public class AntBuildLogger implements BuildLogger {

	protected int priorityFilter = Project.MSG_INFO;
	protected IProgressMonitor monitor;
	private int logLength = 0;
	private int lastTargetEndIndex = 0;

	public AntBuildLogger() {
		monitor = AntUtil.getCurrentProgressMonitor();
		if (monitor == null)
			monitor = new NullProgressMonitor();
	}

	/**
	 * @see BuildListener#buildStarted(BuildEvent)
	 */
	public void buildStarted(BuildEvent event) {
		checkCanceled();
	}

	/**
	 * @see BuildListener#buildFinished(BuildEvent)
	 */
	public void buildFinished(BuildEvent event) {
		checkCanceled();
		monitor.done();
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
			Project.MSG_ERR);	
	}

	/**
	 * @see BuildListener#targetStarted(BuildEvent)
	 */
	public void targetStarted(BuildEvent event) {
		checkCanceled();
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
		checkCanceled();
		monitor.worked(1);
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
		checkCanceled();
		createNewOutputStructureElement(event.getTask().getTaskName());
	}

	/**
	 * @see BuildListener#taskFinished(BuildEvent)
	 */
	public void taskFinished(BuildEvent event) {
		checkCanceled();
		handleException(event);
		finishCurrentOutputStructureElement();
		refreshConsoleTrees();
	}

	/**
	 * @see BuildListener#messageLogged(BuildEvent)
	 */
	public void messageLogged(BuildEvent event) {
		checkCanceled();
		logMessage(event.getMessage(), event.getPriority());
	}

	protected void logMessage(String message, int priority) {
		if (priority > priorityFilter)
			return;
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

	protected void checkCanceled() {
		if (monitor == null)
			return;
		if (monitor.isCanceled())
			throw new BuildCanceledException();
	}

	protected void createNewOutputStructureElement(String name) {
		createNewOutputStructureElement(name, logLength);
	}

	public void setMessageOutputLevel(int level) {
		this.priorityFilter = level;
	}

	public void setEmacsMode(boolean emacsMode) {
	}

	public void setErrorPrintStream(PrintStream err) {
	}

	public void setOutputPrintStream(PrintStream output) {
	}
	private IProgressMonitor monitorFor(IProgressMonitor monitor) {
		if (monitor == null)
			return new NullProgressMonitor();
		return monitor;
	}
}