package org.eclipse.ui.externaltools.internal.ant.logger;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.util.StringUtils;
import org.eclipse.ant.core.AntSecurityException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.console.FileLink;
import org.eclipse.debug.ui.console.IConsoleHyperlink;
import org.eclipse.ui.externaltools.internal.ant.launchConfigurations.AntProcess;
import org.eclipse.ui.externaltools.internal.ant.launchConfigurations.AntStreamMonitor;
import org.eclipse.ui.externaltools.internal.ant.launchConfigurations.AntStreamsProxy;
import org.eclipse.ui.externaltools.internal.model.ToolMessages;
	
/**
 */
public class AntProcessBuildLogger extends NullBuildLogger {
	
	/**
	   * Size of left-hand column for right-justified task name.
	   * @see #logMessage(String, BuildEvent, -1)
	  */
	public static final int LEFT_COLUMN_SIZE = 15;

	/**
	 * Associated process - discovered on creation via process id
	 */
	private AntProcess fProcess = null;
	
	/**
	 * Current length of output
	 */
	private int fLength = 0;
	
	/**
	 * @see org.eclipse.ui.externaltools.internal.ant.logger.NullBuildLogger#logMessage(java.lang.String, int)
	 */
	private void logMessage(String message, BuildEvent event, int overridePriority) {
		int priority= overridePriority;
		if (priority == -1) {
			priority= event.getPriority();
		} 
		
		if (priority > getMessageOutputLevel()) {
			return;
		}
		
		if (fProcess == null) {
			return;
		}
		
		AntStreamMonitor monitor = getMonitor(priority);
		
		StringBuffer fullMessage= new StringBuffer();
		fullMessage.append(StringUtils.LINE_SEP);
		
		int linkAdjustment= 0;
		if (event.getTask() != null && !fEmacsMode) {
			linkAdjustment= getAdornedMessage(event, fullMessage);
		}
		fullMessage.append(message);
		message= fullMessage.toString();
		
		IConsoleHyperlink link = getHyperLink(message, event, linkAdjustment);
		if (link != null) {
			fProcess.getConsole().addLink(link);
		}
		monitor.append(message);
		logMessageToLogFile(message, priority);
		
		fLength += message.length();	
	}

	private int getAdornedMessage(BuildEvent event, StringBuffer fullMessage) {
		String name = event.getTask().getTaskName();
		int size = LEFT_COLUMN_SIZE - (name.length() + 3);
		for (int i = 0; i < size; i++) {
			fullMessage.append(" ");
		}
		fullMessage.append('[');
		fullMessage.append(name);
		fullMessage.append("] ");
		return size;
	}

	private AntStreamMonitor getMonitor(int priority) {
		AntStreamsProxy proxy = (AntStreamsProxy)fProcess.getStreamsProxy();
		AntStreamMonitor monitor = null;
		switch (priority) {
			case Project.MSG_INFO:
				monitor = (AntStreamMonitor)proxy.getOutputStreamMonitor();
				break;
			case Project.MSG_ERR:
				monitor = (AntStreamMonitor)proxy.getErrorStreamMonitor();
				break;
			case Project.MSG_DEBUG:
				monitor = (AntStreamMonitor)proxy.getDebugStreamMonitor();
				break;
			case Project.MSG_WARN:
				monitor = (AntStreamMonitor)proxy.getWarningStreamMonitor();
				break;
			case Project.MSG_VERBOSE:
				monitor = (AntStreamMonitor)proxy.getVerboseStreamMonitor();
				break;
		}
		return monitor;
	}

	private void logMessageToLogFile(String message, int priority) {
		if (priority == Project.MSG_ERR) {
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
	
	/**
	 * Returns a hyperlink for the given build event, or <code>null</code> if
	 * none.
	 * 	 * @return hyper link, or <code>null</code>	 */
	private IConsoleHyperlink getHyperLink(String message, BuildEvent event, int linkAdjustment) {
		Task task = event.getTask();
		if (task != null) {
			Location location = task.getLocation();
			if (location != null) {
				String path = location.toString().trim();
				// format is file:F:L: where F is file path, and L is line number
				int index = path.indexOf(':');
				if (index >= 0) {
					// remove "file:"
					path = path.substring(index + 1);
					index = path.lastIndexOf(':');
					if (index == path.length() - 1) {
						// remove trailing ':'
						path = path.substring(0, index);
						index = path.lastIndexOf(':');
					}
					// split file and line number
					String fileName = path.substring(0, index);
					String lineNumber = path.substring(index + 1);
					IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(fileName));
					if (file != null && file.exists()) {
						try {
							int line = Integer.valueOf(lineNumber).intValue();
							return new FileLink(fLength + linkAdjustment + StringUtils.LINE_SEP.length(), message.length() - linkAdjustment, file, null, -1, -1, line);
						} catch (NumberFormatException e) {
						}
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Looks for associated ant process, if not already found.
	 */
	private void findAntProcess(String processId) {
		if (fProcess == null && processId != null) {
			IProcess[] all = DebugPlugin.getDefault().getLaunchManager().getProcesses();
			for (int i = 0; i < all.length; i++) {
				IProcess process = all[i];
				if (process instanceof AntProcess && processId.equals(process.getAttribute(AntProcess.ATTR_ANT_PROCESS_ID))) {
					fProcess = (AntProcess)process;
					break;
				}
			}
		}
	}

	/**
	 * Find assoicated ant process
	 * 
	 * @see org.apache.tools.ant.BuildListener#buildStarted(org.apache.tools.ant.BuildEvent)
	 */
	public void buildStarted(BuildEvent event) {
		findAntProcess(event.getProject().getUserProperty(AntProcess.ATTR_ANT_PROCESS_ID));
		super.buildStarted(event);
	}

	/**
	 * @see BuildListener#messageLogged(BuildEvent)
	 */
	public void messageLogged(BuildEvent event) {
		logMessage(event.getMessage(), event, -1);
	}
	
	protected void handleException(BuildEvent event) {
		Throwable exception = event.getException();
		if (exception == null || exception == fHandledException
		|| exception instanceof OperationCanceledException
		|| exception instanceof AntSecurityException) {
			return;
		}
		fHandledException= exception;
		logMessage(ToolMessages.format(
					"NullBuildLogger.buildException", //$NON-NLS-1$
					new String[] { exception.toString()}),
					event, Project.MSG_ERR);	
	}
		
	/**
	 * @see org.apache.tools.ant.BuildLogger#setMessageOutputLevel(int)
	 */
	public void setMessageOutputLevel(int level) {
		fMessageOutputLevel= level;
	}

	public void targetStarted(BuildEvent event) {
		if (Project.MSG_INFO > getMessageOutputLevel()) {
			return;
		}
		StringBuffer msg= new StringBuffer(StringUtils.LINE_SEP);
		msg.append(event.getTarget().getName());
		msg.append(':');
		logMessage(msg.toString(), event, Project.MSG_INFO);
	}
}
