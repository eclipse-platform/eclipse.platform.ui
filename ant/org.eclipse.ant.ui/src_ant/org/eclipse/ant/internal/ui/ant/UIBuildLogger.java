package org.eclipse.ant.internal.ui.ant;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.Vector;

import org.apache.tools.ant.*;
import org.eclipse.ant.internal.ui.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;

public class UIBuildLogger implements BuildLogger {

	protected int priorityFilter = Project.MSG_INFO;
	protected IProgressMonitor monitor;
	private int logLength = 0;
	private int lastTargetEndIndex = 0;

public UIBuildLogger() {
	AntUIPlugin plugin = AntUIPlugin.getPlugin();
	this.monitor = Policy.monitorFor(plugin.getCurrentProgressMonitor());
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
	logMessage(Policy.bind("exception.buildException", exception.toString()), Project.MSG_ERR);
}

/**
 * @see BuildListener#targetStarted(BuildEvent)
 */
public void targetStarted(BuildEvent event) {
	checkCanceled();
	createNewOutputStructureElement(event.getTarget().getName(), logLength);	
}

protected void refreshConsoleTrees() {
	final Vector consoles = AntConsole.getInstances();
	if (consoles.size() == 0)
		return;
	// we get the display from the console #0 (that exists for sure because consoles!=null)
	Display display = ((AntConsole) consoles.get(0)).getSite().getShell().getDisplay();
	// create a new thread for synchronizing all the refresh operations
	display.syncExec(new Runnable() {
		public void run() {
			for (Iterator iterator = consoles.iterator(); iterator.hasNext();) {
				AntConsole console = (AntConsole) iterator.next();
				console.refreshTree();
			}
		}
	});
}

protected void createNewOutputStructureElement(String name, int index) {
	for (Iterator iterator = AntConsole.getInstances().iterator(); iterator.hasNext();) {
		AntConsole console = (AntConsole) iterator.next();
		OutputStructureElement newElement = new OutputStructureElement(name, console.getCurrentOutputStructureElement(), index);
		console.setCurrentOutputStructureElement(newElement);
	}
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
	for (Iterator iterator = AntConsole.getInstances().iterator(); iterator.hasNext();) {
		AntConsole console = (AntConsole) iterator.next();
		console.append(message, priority);
	}
	logLength += message.length();
}

protected void finishCurrentOutputStructureElement() {
	for (Iterator iterator = AntConsole.getInstances().iterator(); iterator.hasNext();) {
		AntConsole console = (AntConsole) iterator.next();
	    // sets the index that indicates the end of the log part linked to this element
	    OutputStructureElement output = console.getCurrentOutputStructureElement();
		output.setEndIndex(logLength);
		// and sets the current element to the parent of the element
		console.setCurrentOutputStructureElement(output.getParent());
	}
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
}