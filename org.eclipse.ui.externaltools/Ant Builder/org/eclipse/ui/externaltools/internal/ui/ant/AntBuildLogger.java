package org.eclipse.ui.externaltools.internal.ui.ant;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.apache.tools.ant.BuildEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.externaltools.internal.ui.LogConsoleDocument;
import org.eclipse.ui.externaltools.internal.ui.OutputStructureElement;

public class AntBuildLogger extends NullBuildLogger {

	private int fLogLength = 0;
	private int fLastTargetEndIndex = 0;
	
	public AntBuildLogger() {
	}

	/**
	 * @see BuildListener#targetStarted(BuildEvent)
	 */
	public void targetStarted(BuildEvent event) {
		createNewOutputStructureElement(event.getTarget().getName(), fLogLength);
	}

	protected void refreshConsoleTrees() {
		final LogConsoleDocument doc = LogConsoleDocument.getInstance();
		if (!doc.hasViews()) {
			return;
		}
		// we get the display from the console #0 (that exists for sure because consoles!=null)
		Display display = doc.getDisplay();
		// create a new thread for synchronizing all the refresh operations
		display.asyncExec(new Runnable() {
			public void run() {
				doc.refreshTree();
			}
		});
	}

	protected void createNewOutputStructureElement(String name, int index) {
		LogConsoleDocument doc = LogConsoleDocument.getInstance();
		OutputStructureElement newElement =
			new OutputStructureElement(name, doc.getCurrentOutputStructureElement(), index);
		doc.setCurrentOutputStructureElement(newElement);
	}

	/**
	 * @see BuildListener#targetFinished(BuildEvent)
	 */
	public void targetFinished(BuildEvent event) {
		handleException(event);
		finishCurrentOutputStructureElement();
		// store the end index of this target's log (so that we can use it later)
		fLastTargetEndIndex = fLogLength;
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

	protected void logMessage(String message, int priority) {
		if (priority > getMessageOutputLevel()) {
			return;
		}
		super.logMessage(message, priority);
		message += '\n';
		LogConsoleDocument doc = LogConsoleDocument.getInstance();
		doc.append(message, priority);
		fLogLength += message.length();
	}

	protected void finishCurrentOutputStructureElement() {
		LogConsoleDocument doc = LogConsoleDocument.getInstance();
		// sets the index that indicates the end of the log part linked to this element
		OutputStructureElement output = doc.getCurrentOutputStructureElement();
		output.setEndIndex(fLogLength);
		// and sets the current element to the parent of the element
		doc.setCurrentOutputStructureElement(output.getParent());
	}

	protected void createNewOutputStructureElement(String name) {
		createNewOutputStructureElement(name, fLogLength);
	}
	
	private IProgressMonitor monitorFor(IProgressMonitor monitor) {
		if (monitor == null) {
			return new NullProgressMonitor();
		}
		return monitor;
	}
}