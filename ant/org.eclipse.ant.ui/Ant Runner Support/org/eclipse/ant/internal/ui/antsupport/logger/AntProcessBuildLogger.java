/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Conway - Patch for Bug 28052
 *******************************************************************************/

package org.eclipse.ant.internal.ui.antsupport.logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.StringUtils;
import org.eclipse.ant.internal.core.AbstractEclipseBuildLogger;
import org.eclipse.ant.internal.ui.AntUtil;
import org.eclipse.ant.internal.ui.ExternalHyperlink;
import org.eclipse.ant.internal.ui.IAntUIConstants;
import org.eclipse.ant.internal.ui.antsupport.AntSupportMessages;
import org.eclipse.ant.internal.ui.antsupport.logger.util.AntDebugState;
import org.eclipse.ant.internal.ui.launchConfigurations.AntProcess;
import org.eclipse.ant.internal.ui.launchConfigurations.AntStreamMonitor;
import org.eclipse.ant.internal.ui.launchConfigurations.AntStreamsProxy;
import org.eclipse.ant.internal.ui.launchConfigurations.TaskLinkManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.console.FileLink;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.ui.console.IHyperlink;
	
public class AntProcessBuildLogger extends NullBuildLogger {
	
	private File fBuildFileParent= null;
	private long fStartTime;
    private Map fFileNameToIFile= new HashMap();

	/**
	 * Associated process - discovered as needed to log messages
	 */
	private AntProcess fProcess = null;
	
	protected void logMessage(String message, BuildEvent event, int overridePriority) {
		int priority= overridePriority;
		if (priority == -1) {
			priority= event.getPriority();
		} 
		
		if (priority > getMessageOutputLevel()) {
			return;
		}
		AntProcess antProcess = getAntProcess(fProcessId);
		if (antProcess == null) {
			return;
		}
		
		StringBuffer fullMessage= new StringBuffer();
		 if (!loggingToLogFile()) {
			fullMessage.append(System.getProperty("line.separator")); //$NON-NLS-1$
		}
		if (event.getException() == null && event.getTask() != null && !fEmacsMode) {
			adornMessage(event, fullMessage);
		} else {
			fullMessage.append(message);
		}
		message= fullMessage.toString();
		
		if (loggingToLogFile()) {
			logMessageToLogFile(message, priority);
		} else {
			AntStreamMonitor monitor = getMonitor(priority);
			monitor.append(message);
		}
	}

	/**
	 * Builds a right justified task prefix for the given build event, placing it
	 * in the given string buffer. Creates the hyperlinks for the task prefix. 
	 *  
	 * @param event build event
	 * @param fullMessage buffer to place task prefix in
	 */
	private void adornMessage(BuildEvent event, StringBuffer fullMessage) {
		String name = event.getTask().getTaskName();
		if (name == null) {
			name = "null"; //$NON-NLS-1$
		}
		Location location = event.getTask().getLocation();
		StringBuffer column= new StringBuffer();
		int size = IAntUIConstants.LEFT_COLUMN_SIZE - (name.length() + 3);
		for (int i = 0; i < size; i++) {
			column.append(' ');
		}
		StringBuffer labelBuff= new StringBuffer();
		labelBuff.append('[');
		labelBuff.append(name);
		labelBuff.append("] "); //$NON-NLS-1$
		
		int offset = Math.max(size, 0) + 1;
		String label= labelBuff.toString();
		if (event.getMessage() == null) {
			return;
		}
		try {
			BufferedReader r = new BufferedReader(new StringReader(event.getMessage()));
			String line = r.readLine();
			fullMessage.append(column);
			appendAndLink(fullMessage, location, label, offset, line);
			line = r.readLine();
			while (line != null) {
				fullMessage.append(System.getProperty("line.separator")); //$NON-NLS-1$
				fullMessage.append(column);
				appendAndLink(fullMessage, location, label, offset, line);
				line = r.readLine();
			}
		} catch (IOException e) {
			if (event.getMessage() != null) {
				fullMessage.append(label).append(event.getMessage());
			}
		}
	}
	
	private void appendAndLink(StringBuffer fullMessage, Location location, String label, int offset, String line) {
		fullMessage.append(label);
		fullMessage.append(line);
		if (location != null) {
			String newLine= (label + line).trim();
			IRegion region= new Region(offset, label.length() - 3); // only want the name length "[name] "
			IHyperlink link= getLocationLink(location);
			if (link != null) {
				TaskLinkManager.addTaskHyperlink(getAntProcess(fProcessId), link, region, newLine);
			}
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
			getErrorPrintStream().println(message);
		} else {
			getOutputPrintStream().println(message);
		}
	}
	
	/**
	 * Returns a hyperlink for the given task, or <code>null</code> if unable to
	 * parse a valid location for the task. The link is set to exist at the specified
	 * offset and length.
	 * 
	 * @return hyper link, or <code>null</code>
	 */
	private IHyperlink getLocationLink(Location location) {
		if (location != null && !location.equals(Location.UNKNOWN_LOCATION)) {
            try {
                String fileName= location.getFileName();
                IFile file= (IFile) fFileNameToIFile.get(fileName);
                int lineNumber= location.getLineNumber();
                if (file != null) {
                    return new FileLink(file, null, -1, -1, lineNumber);
                } 
                file= AntUtil.getFileForLocation(fileName, fBuildFileParent);
                if (file != null) {
                    fFileNameToIFile.put(fileName, file);
                    return new FileLink(file, null, -1, -1, lineNumber);
                }
                File javaIOFile= FileUtils.newFileUtils().resolveFile(fBuildFileParent, fileName);
                if (javaIOFile.exists()) {
                    return new ExternalHyperlink(javaIOFile, lineNumber);
                }
            } catch (NoSuchMethodError e) {
                //support for Ant older than 1.6
                return AntUtil.getLocationLink(location.toString(), fBuildFileParent);
            }
		}
		return null;
	}	
	
	/**
	 * Returns the associated Ant process, finding it if necessary, if not
	 * already found.
	 */
	protected AntProcess getAntProcess(String processId) {
		if (fProcess == null && processId != null) {
			IProcess[] all = DebugPlugin.getDefault().getLaunchManager().getProcesses();
			for (int i = 0; i < all.length; i++) {
				IProcess process = all[i];
				if (process instanceof AntProcess && processId.equals(process.getAttribute(AbstractEclipseBuildLogger.ANT_PROCESS_ID))) {
					fProcess = (AntProcess)process;
					break;
				}
			}
		}
		return fProcess;
	}

	/* (non-Javadoc)
	 * Set the start time.
	 * 
	 * @see org.apache.tools.ant.BuildListener#buildStarted(org.apache.tools.ant.BuildEvent)
	 */
	public void buildStarted(BuildEvent event) {
		fStartTime= System.currentTimeMillis();
	}
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.BuildListener#buildFinished(org.apache.tools.ant.BuildEvent)
	 */
	public void buildFinished(BuildEvent event) {
        String message= handleException(event);
        if (message != null) {
            logMessage(message, event, Project.MSG_ERR);
            int fileStart= message.indexOf(AntSupportMessages.NullBuildLogger_1);
            fileStart= fileStart + AntSupportMessages.NullBuildLogger_1.length() + StringUtils.LINE_SEP.length();
            AntUtil.linkBuildFailedMessage(message.substring(fileStart).trim(), getAntProcess(fProcessId));
        }
		fHandledException= null;
		fBuildFileParent= null;
		if (!(event.getException() instanceof OperationCanceledException)) {
			logMessage(getTimeString(System.currentTimeMillis() - fStartTime), event, fMessageOutputLevel);
		}
		fProcess= null;
		event.getProject().removeBuildListener(this);
        fFileNameToIFile= null;
	}
	
	private String getTimeString(long milliseconds) {
			long seconds = milliseconds / 1000;
			long minutes = seconds / 60;
			seconds= seconds % 60;
		
			StringBuffer result= new StringBuffer(AntSupportMessages.AntProcessBuildLogger_Total_time); //$NON-NLS-1$
			if (minutes > 0) {
				result.append(minutes);
				if (minutes > 1) {
					result.append(AntSupportMessages.AntProcessBuildLogger__minutes_2); //$NON-NLS-1$
				} else {
					result.append(AntSupportMessages.AntProcessBuildLogger__minute_3); //$NON-NLS-1$
				}
			}
			if (seconds > 0) {
				if (minutes > 0) {
					result.append(' ');
				}
				result.append(seconds);
			
				if (seconds > 1) {
					result.append(AntSupportMessages.AntProcessBuildLogger__seconds_4); //$NON-NLS-1$
				} else {
					result.append(AntSupportMessages.AntProcessBuildLogger__second_5); //$NON-NLS-1$
				} 
			}
			if (seconds == 0 && minutes == 0) {
				result.append(milliseconds);
				result.append(AntSupportMessages.AntProcessBuildLogger__milliseconds_6);		 //$NON-NLS-1$
			}
			
			result.append(System.getProperty("line.separator")); //$NON-NLS-1$
			return result.toString();
		}
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.BuildListener#messageLogged(org.apache.tools.ant.BuildEvent)
	 */
	public void messageLogged(BuildEvent event) {
		if (event.getPriority() > getMessageOutputLevel()) {
			return;
		}
		if (event.getMessage() != null && event.getMessage().length() > 0) {
			logMessage(event.getMessage(), event, -1);
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.tools.ant.BuildListener#targetStarted(org.apache.tools.ant.BuildEvent)
	 */
	public void targetStarted(BuildEvent event) {
		if (Project.MSG_INFO > getMessageOutputLevel()) {
			return;
		}
		Target target= event.getTarget();
		StringBuffer msg= new StringBuffer(System.getProperty("line.separator")); //$NON-NLS-1$
		String targetName= target.getName();
		msg.append(targetName);
		msg.append(':');
		String message= msg.toString();
		Location location= AntDebugState.getLocation(target);
		if (location != null && location != Location.UNKNOWN_LOCATION) {
			IRegion region= new Region(0, targetName.length());
			IHyperlink link= getLocationLink(location);
			if (link != null) {
				TaskLinkManager.addTaskHyperlink(getAntProcess(fProcessId), link, region, message.trim());
			}
		}
		logMessage(message, event, Project.MSG_INFO);
	}
	
	private boolean loggingToLogFile() {
		//check if user has designated to log to a logfile
		return getErrorPrintStream() != null && getErrorPrintStream() != System.err;
	}
}
