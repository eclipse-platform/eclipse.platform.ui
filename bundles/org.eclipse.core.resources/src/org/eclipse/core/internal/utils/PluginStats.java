/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.utils;

import org.eclipse.core.runtime.CoreException;
import java.util.*;
/**
 * A placeholder for statistics about the runtime behaviour
 * of a particular plugin.  PluginStats objects are used internally
 * by the CoreStats mechanism.
 */
public class PluginStats {
	protected String pluginID;
	protected long notificationRunningTime = 0;
	protected long buildRunningTime = 0;
	protected int notificationCount = 0;
	protected int buildCount = 0;
	protected Vector exceptions = new Vector();
/**
 * PluginStats constructor comment.
 */
PluginStats(String pluginID) {
	this.pluginID = pluginID;
}
void addBuild(long elapsed) {
	buildCount++;
	buildRunningTime += elapsed;
}
void addException(Exception e) {
	exceptions.addElement(e);
}
void addNotify(long elapsed) {
	notificationCount++;
	notificationRunningTime += elapsed;
}
public int getBuildCount() {
	return buildCount;
}
public long getBuildRunningTime() {
	return buildRunningTime;
}
public Enumeration getCoreExceptions() {
	Vector runtime = new Vector();
	for (Enumeration e = exceptions.elements(); e.hasMoreElements();) {
		Exception next = (Exception)e.nextElement();
		if (next instanceof CoreException) {
			runtime.addElement(next);
		}
	}
	return runtime.elements();
}
public int getExceptionCount() {
	return exceptions.size();
}
public String getName() {
	return pluginID;
}
public int getNotifyCount() {
	return notificationCount;
}
public long getNotifyRunningTime() {
	return notificationRunningTime;
}
public Enumeration getRuntimeExceptions() {
	Vector runtime = new Vector();
	for (Enumeration e = exceptions.elements(); e.hasMoreElements();) {
		Exception next = (Exception) e.nextElement();
		if (next instanceof RuntimeException) {
			runtime.addElement(next);
		}
	}
	return runtime.elements();
}
public long getTotalRunningTime() {
	return notificationRunningTime + buildRunningTime;
}
}
