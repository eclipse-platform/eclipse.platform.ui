/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

public class ExceptionCollector {

	private Map exceptionBucket = new HashMap();
	private List statuses = new ArrayList();
	private String message;
	private String pluginId;
	private int code;
	private ILog log;
	
	public ExceptionCollector(String message, String pluginId, int code, ILog log) {
		this.message = message;
		this.pluginId = pluginId;
		this.code = code;
		this.log = log;
	}

	public void clear() {
		statuses.clear();
		exceptionBucket.clear();
	}

	public IStatus getStatus() {
		if(statuses.isEmpty()) {
			return Status.OK_STATUS;
		} else {
			MultiStatus multiStatus = new MultiStatus(pluginId, code, message, null);
			Iterator it = statuses.iterator();
			while (it.hasNext()) {
				IStatus status = (IStatus) it.next();
				multiStatus.merge(status);
			}
			return multiStatus; 
		}
	}

	/**
	 * Handle exceptions that occur in the decorator. 
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
	 * Log exceptions once for each {plugid,code} combination. This is to avoid
	 * flooding the log. 
	 */
	private void logStatus(IStatus status) {
		String pluginId = status.getPlugin();
		List codes = (List)exceptionBucket.get(pluginId);
		Integer code = new Integer(status.getCode());
		if(codes != null) {
			if(codes.contains(code)) {
				return;
			}
		} 
		codes = new ArrayList(1);
		codes.add(code);
		exceptionBucket.put(pluginId, codes);
		statuses.add(status);
		if(log != null) {
			log.log(new Status(status.getSeverity(), pluginId, status.getCode(), message, status.getException()));
		}		
	}
}
