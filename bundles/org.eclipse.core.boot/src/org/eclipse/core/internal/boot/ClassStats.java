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
package org.eclipse.core.internal.boot;

import java.util.ArrayList;

/**
 * Maintain statistics about a loaded class.
 */
public class ClassStats {
	private String className; // fully qualified name of this class 
	private ClassloaderStats classloader; // the classloader that loaded this class
	private int loadOrder = -1;

	private long timestamp; // time at which this class was loaded
	private long timeLoading; // time to load the class
	private long timeLoadingOthers = 0; // time spent loading classes which has been triggered by this class  

	// parentage of classes loaded
	private ClassStats loadedBy = null; // a reference to the class that loaded this class
	private ArrayList loaded = new ArrayList(2); // a reference to the classes that this class loaded

	private boolean duringStartup; // indicate if the class was loaded during platform startup

	//information to retrieve the stacktrace from the file
	private long traceStart = -1;
	private long traceEnd = -1;

	static private boolean booting = true; // whether or not the platform is starting up.

	public static void setBooting(boolean value) {
		booting = value;
	}

	public ClassStats(String name, ClassloaderStats classloader) {
		className = name;
		timestamp = System.currentTimeMillis();
		duringStartup = booting;
		this.classloader = classloader;
	}

	public void setLoadOrder(int order) {
		loadOrder = order;
	}

	public void loadingDone() {
		timeLoading = System.currentTimeMillis() - timestamp;
	}

	public long getTimeLoading() {
		return timeLoading;
	}

	public long getLocalTimeLoading() {
		return timeLoading - timeLoadingOthers;
	}

	public void addTimeLoadingOthers(long time) {
		timeLoadingOthers = timeLoadingOthers + time;
	}

	public long getTraceStart() {
		return traceStart;
	}

	public long getTraceEnd() {
		return traceEnd;
	}

	public void setTraceStart(long position) {
		traceStart = position;
	}

	public void setTraceEnd(long position) {
		traceEnd = position;
	}

	public void loaded(ClassStats child) {
		loaded.add(child);
	}

	public void setLoadedBy(ClassStats parent) {
		loadedBy = parent;
	}

	public ClassStats getLoadedBy() {
		return loadedBy;
	}

	public ArrayList getLoadedClasses() {
		return loaded;
	}

	public String getClassName() {
		return className;
	}

	public boolean isStartupClass() {
		return duringStartup;
	}

	public ClassloaderStats getClassloader() {
		return classloader;
	}

	public int getLoadOrder() {
		return loadOrder;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void toBaseClass() {
		duringStartup = true;
		loadOrder = -2;
	}
}
