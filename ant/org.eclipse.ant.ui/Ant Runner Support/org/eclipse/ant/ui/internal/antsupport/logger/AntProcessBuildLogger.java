/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Conway - Patch for Bug 28052
 *******************************************************************************/

package org.eclipse.ant.ui.internal.antsupport.logger;

import java.io.File;
import java.text.MessageFormat;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.util.FileUtils;
import org.eclipse.ant.core.AntSecurityException;
import org.eclipse.ant.ui.internal.launchConfigurations.AntProcess;
import org.eclipse.ant.ui.internal.launchConfigurations.AntStreamMonitor;
import org.eclipse.ant.ui.internal.launchConfigurations.AntStreamsProxy;
import org.eclipse.ant.ui.internal.launchConfigurations.TaskLinkManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.console.FileLink;
import org.eclipse.debug.ui.console.IConsoleHyperlink;
import org.eclipse.jface.text.Region;
import org.eclipse.ant.ui.internal.antsupport.AntSupportMessages;
	
/**
 */
public class AntProcessBuildLogger extends NullBuildLogger {
	
	private File fBuildFileParent= null;
	private long fStartTime;
	
	/**
	 * Size of left-hand column for right-justified task name.
	 * @see #logMessage(String, BuildEvent, -1)
	  */
	public static final int LEFT_COLUMN_SIZE = 15;

	/**
	 * Associated process - discovered as needed to log messages
	 */
	private AntProcess fProcess = null;
	
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
		AntProcess antProcess = getAntProcess(event.getProject().getUserProperty(AntProcess.ATTR_ANT_PROCESS_ID));
		if (antProcess == null) {
			return;
		}
		
		AntStreamMonitor monitor = getMonitor(priority);
		
		if (event.getTarget() == null) {
			// look for "Buildfile:" message
			if (message.startsWith("Buildfile:")) { //$NON-NLS-1$
				String fileName = message.substring(10).trim();
				IFile file = getFileForLocation(fileName);
				if (file != null) {
					FileLink link = new FileLink(file, null,  -1, -1, -1);
					TaskLinkManager.addTaskHyperlink(fProcess, link, new Region(11 + System.getProperty("line.separator").length(), fileName.length()), fileName); //$NON-NLS-1$
					fBuildFileParent= file.getLocation().toFile().getParentFile();
				}
			}
		}
		
		StringBuffer fullMessage= new StringBuffer(System.getProperty("line.separator")); //$NON-NLS-1$
		
		if (event.getTask() != null && !fEmacsMode) {
			getAdornedMessage(event, fullMessage);
		}
		fullMessage.append(message);
		message= fullMessage.toString();
		
		monitor.append(message);
		logMessageToLogFile(message, priority);
		
	}

	/**
	 * Builds a right justified task prefix for the given build event, placing it
	 * in the given string buffer. Creates a hyperlink for the task prefix. 
	 *  
	 * @param event build event
	 * @param fullMessage buffer to place task prefix in
	 */
	private void getAdornedMessage(BuildEvent event, StringBuffer fullMessage) {
		String name = event.getTask().getTaskName();
		if (name == null) {
			name = "null"; //$NON-NLS-1$
		}
		int size = LEFT_COLUMN_SIZE - (name.length() + 3);
		for (int i = 0; i < size; i++) {
			fullMessage.append(' ');
		}
		fullMessage.append('[');
		fullMessage.append(name);
		fullMessage.append("] "); //$NON-NLS-1$
		int offset = Math.max(size, 0) + 1;
		int length = LEFT_COLUMN_SIZE - size - 3;
		IConsoleHyperlink taskLink = getTaskLink(event);
		if (taskLink != null) {
			TaskLinkManager.addTaskHyperlink(fProcess, taskLink, new Region(offset, length), name);
		}
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
	 * Returns a hyperlink for the given task, or <code>null</code> if unable to
	 * parse a valid location for the task. The link is set to exist at the specified
	 * offset and length.
	 * 
	 * @return hyper link, or <code>null</code>
	 */
	private IConsoleHyperlink getTaskLink(BuildEvent event) {
		Task task = event.getTask();
		if (task != null) {
			Location location = task.getLocation();
			if (location != null) {
				String path = location.toString().trim();
				if (path.length() == 0) {
					return null;
				}
				if (path.startsWith("file:")) { //$NON-NLS-1$
					// remove "file:"
					path= path.substring(5, path.length());
				}
				// format is file:F:L: where F is file path, and L is line number
				int index = path.lastIndexOf(':');
				if (index == path.length() - 1) {
					// remove trailing ':'
					path = path.substring(0, index);
					index = path.lastIndexOf(':');
				}
				// split file and line number
				String fileName = path.substring(0, index);
				IFile file = getFileForLocation(fileName);
				if (file != null) {
					try {
						String lineNumber = path.substring(index + 1);
						int line = Integer.parseInt(lineNumber);
						return new FileLink(file, null, -1, -1, line);
					} catch (NumberFormatException e) {
					}
				}
			}
		}
		return null;
	}	
	
	/**
	 * Returns the associated Ant process, finding it if necessary, if not
	 * already found.
	 */
	private AntProcess getAntProcess(String processId) {
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
		return fProcess;
	}

	/**
	 * Set the start time.
	 * 
	 * @see org.apache.tools.ant.BuildListener#buildStarted(org.apache.tools.ant.BuildEvent)
	 */
	public void buildStarted(BuildEvent event) {
		fStartTime= System.currentTimeMillis();
	}
	
	/**
	 * @see org.apache.tools.ant.BuildListener#buildFinished(org.apache.tools.
	 * ant.BuildEvent)
	 */
	public void buildFinished(BuildEvent event) {
		handleException(event);
		fHandledException= null;
		fBuildFileParent= null;
		logMessage(getTimeString(System.currentTimeMillis() - fStartTime), event, fMessageOutputLevel);
		fProcess= null;
		event.getProject().removeBuildListener(this);
	}
	
	private String getTimeString(long milliseconds) {
			long seconds = milliseconds / 1000;
			long minutes = seconds / 60;
			seconds= seconds % 60;
		
			StringBuffer result= new StringBuffer(AntSupportMessages.getString("AntProcessBuildLogger.Total_time")); //$NON-NLS-1$
			if (minutes > 0) {
				result.append(minutes);
				if (minutes > 1) {
					result.append(AntSupportMessages.getString("AntProcessBuildLogger._minutes_2")); //$NON-NLS-1$
				} else {
					result.append(AntSupportMessages.getString("AntProcessBuildLogger._minute_3")); //$NON-NLS-1$
				}
			}
			if (seconds > 0) {
				if (minutes > 0) {
					result.append(' ');
				}
				result.append(seconds);
			
				if (seconds > 1) {
					result.append(AntSupportMessages.getString("AntProcessBuildLogger._seconds_4")); //$NON-NLS-1$
				} else {
					result.append(AntSupportMessages.getString("AntProcessBuildLogger._second_5")); //$NON-NLS-1$
				} 
			}
			if (seconds == 0 && minutes == 0) {
				result.append(milliseconds);
				result.append(AntSupportMessages.getString("AntProcessBuildLogger._milliseconds_6"));		 //$NON-NLS-1$
			}
			return result.toString();
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
		logMessage(MessageFormat.format(AntSupportMessages.getString("AntProcessBuildLogger.BUILD_FAILED__{0}_1"), new String[] { exception.toString()}), //$NON-NLS-1$
					event, Project.MSG_ERR);	
	}

	/**
	 * @see org.apache.tools.ant.BuildListener#targetStarted(org.apache.tools.ant.BuildEvent)
	 */
	public void targetStarted(BuildEvent event) {
		if (Project.MSG_INFO > getMessageOutputLevel()) {
			return;
		}
		StringBuffer msg= new StringBuffer(System.getProperty("line.separator")); //$NON-NLS-1$
		msg.append(event.getTarget().getName());
		msg.append(':');
		logMessage(msg.toString(), event, Project.MSG_INFO);
	}

	/**
	 * Returns the workspace file associated with the given absolute path in the
	 * local file system, or <code>null</code> if none.
	 *   
	 * @param absolutePath
	 * @return file or <code>null</code>
	 */
	private IFile getFileForLocation(String absolutePath) {
		IPath filePath= new Path(absolutePath);
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(filePath);
		if (file == null) {
			//relative path
			File relativeFile= null;
			try {
				//this call is ok if fBuildFileParent is null
				relativeFile= FileUtils.newFileUtils().resolveFile(fBuildFileParent, absolutePath);
				filePath= new Path(relativeFile.getAbsolutePath());
				file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(filePath);
				if (file == null) {
					return null;
				}
			} catch (BuildException be) {
				return null;
			}
		}
		
		if (file.exists()) {
			return file;
		}
		return null;
	}
}
