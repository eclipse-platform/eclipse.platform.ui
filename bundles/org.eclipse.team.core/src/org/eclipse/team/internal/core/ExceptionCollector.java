/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.core;

import java.util.*;

import org.eclipse.core.runtime.*;

/**
 * Collects exceptions and can be configured to ignore duplicates exceptions. Exceptions can be logged
 * and a MultiStatus containing all collected exceptions can be returned.
 * 
 * @see org.eclipse.core.runtime.MultiStatus
 * @see org.eclipse.core.runtime.IStatus
 * 
 * @since 3.0 
 */
public class ExceptionCollector {

	private List statuses = new ArrayList();
	private String message;
	private String pluginId;
	private int severity;
	private ILog log;
		
	/**
	 * Creates a collector and initializes the parameters for the top-level exception
	 * that would be returned from <code>getStatus</code> is exceptions are collected.
	 * 
	 * @param message a human-readable message, localized to the current locale
	 * @param pluginId the unique identifier of the relevant plug-in
	 * @param severity the severity; one of <code>OK</code>,
	 *   <code>ERROR</code>, <code>INFO</code>, or <code>WARNING</code>
	 * @param code the plug-in-specific status code, or <code>OK</code>
	 * @param log the log to output the exceptions to, or <code>null</code> if
	 *   exceptions should not be logged.
	 */
	public ExceptionCollector(String message, String pluginId, int severity, ILog log) {
		this.message = message;
		this.pluginId = pluginId;
		this.severity = severity;
		this.log = log;
	}

	/**
	 * Clears the exceptions collected.
	 */
	public void clear() {
		statuses.clear();
	}

	/**
	 * Returns a status that represents the exceptions collected. If the collector
	 * is empty <code>IStatus.OK</code> is returned. Otherwise a MultiStatus containing
	 * all collected exceptions is returned.
	 * @return a multistatus containing the exceptions collected or IStatus.OK if
	 * the collector is empty. 
	 */
	public IStatus getStatus() {
		if(statuses.isEmpty()) {
			return Status.OK_STATUS;
		} else {
			MultiStatus multiStatus = new MultiStatus(pluginId, severity, message, null);
			Iterator it = statuses.iterator();
			while (it.hasNext()) {
				IStatus status = (IStatus) it.next();
				multiStatus.merge(status);
			}
			return multiStatus; 
		}
	}
	
	/**
	 * Add this exception to the collector. If a log was specified in the constructor
	 * then the exception will be output to the log. You can retreive exceptions
	 * using <code>getStatus</code>.
	 * 
	 * @param exception the exception to collect
	 */
	public void handleException(Exception e) {
		IStatus status = null;
		if(e instanceof CoreException) {
			status = ((CoreException)e).getStatus();
		}
		if(status != null) {
			logStatus(status);
			IStatus[] children = status.getChildren();
			for (int i = 0; i < children.length; i++) {
				IStatus status2 = children[i];
				logStatus(status2);
			}
		}
	}

	/**
	 * Log and accumulate exceptions once for each {plugid,code} combination.
	 */
	private void logStatus(IStatus status) {
		// collect the status
		statuses.add(status);
		
		// log if necessary
		if(log != null) {
    		String pluginId = status.getPlugin();
			log.log(new Status(status.getSeverity(), pluginId, status.getCode(), message, status.getException()));
		}		
	}
}
