package org.eclipse.ui.externaltools.internal.ant.logger;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.apache.tools.ant.BuildEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IProcess;
import org
	.eclipse
	.ui
	.externaltools
	.internal
	.ant
	.launchConfigurations
	.AntProcess;
import org
	.eclipse
	.ui
	.externaltools
	.internal
	.ant
	.launchConfigurations
	.AntStreamMonitor;
import org
	.eclipse
	.ui
	.externaltools
	.internal
	.ant
	.launchConfigurations
	.AntStreamsProxy;
import org.eclipse.ui.externaltools.internal.ui.LogConsoleDocument;
	
/**
 */
public class AntProcessBuildLogger extends NullBuildLogger {
	
	/**
	 * Associated process - discovered on creation via process id
	 */
	private AntProcess fProcess = null;

	public AntProcessBuildLogger() {
		super();
	}
	
	/**
	 * @see org.eclipse.ui.externaltools.internal.ant.logger.NullBuildLogger#logMessage(java.lang.String, int)
	 */
	protected void logMessage(String message, int priority) {
		if (priority > getMessageOutputLevel()) {
			return;
		}
		
		if (fProcess == null) {
			return;
		}
		
		AntStreamsProxy proxy = (AntStreamsProxy)fProcess.getStreamsProxy();
		switch (priority) {
			case LogConsoleDocument.MSG_INFO:
				((AntStreamMonitor)proxy.getOutputStreamMonitor()).append(message);
				break;
			case LogConsoleDocument.MSG_ERR:
				((AntStreamMonitor)proxy.getErrorStreamMonitor()).append(message);
				break;
			case LogConsoleDocument.MSG_DEBUG:
				((AntStreamMonitor)proxy.getDebugStreamMonitor()).append(message);
				break;
			case LogConsoleDocument.MSG_WARN:
				((AntStreamMonitor)proxy.getWarningStreamMonitor()).append(message);
				break;
			case LogConsoleDocument.MSG_VERBOSE:
				((AntStreamMonitor)proxy.getVerboseStreamMonitor()).append(message);
				break;
		}		
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

}
