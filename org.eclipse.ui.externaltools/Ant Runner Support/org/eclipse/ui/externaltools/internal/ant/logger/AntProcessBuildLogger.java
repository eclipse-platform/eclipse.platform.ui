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
	 * Associated process - discovered on creation via process id
	 */
	private AntProcess fProcess = null;
	
	/**
	 * Current length of output
	 */
	private int fLength = 0;

	public AntProcessBuildLogger() {
		super();
		fMessageOutputLevel = Project.MSG_INFO;
	}
	
	/**
	 * @see org.eclipse.ui.externaltools.internal.ant.logger.NullBuildLogger#logMessage(java.lang.String, int)
	 */
	protected void logMessage(String message, BuildEvent event, int overridePriority) {
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
		message += "\n";
		IConsoleHyperlink link = getHyperLink(message, event);
		if (link != null) {
			fProcess.getConsole().addLink(link);
		}
		monitor.append(message);
		fLength += message.length();	
	}
	
	/**
	 * Returns a hyperlink for the given build event, or <code>null</code> if
	 * none.
	 * 
	 * @param event	 * @return hyper link, or <code>null</code>	 */
	protected IConsoleHyperlink getHyperLink(String message, BuildEvent event) {
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
					if (file.exists()) {
						try {
							int line = Integer.valueOf(lineNumber).intValue();
							return new FileLink(fLength, message.length(), file, "org.eclipse.ui.DefaultTextEditor", -1, -1, line);
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
	protected void findAntProcess(String processId) {
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

}
