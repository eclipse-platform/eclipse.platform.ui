/**********************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.boot;

import java.util.Vector;

public class ClassStats {
	private String className;
	private int loadingNumber=-1;
	private long timeStampsOfLoading;
	private ClassloaderStats classloader;		// the classloader that loaded this class

	private long timeToLoad;
	private long timeSpentLoadingOthers=0;

	// parentage of classes loaded
	private ClassStats loadedBy=null;
	private Vector hasLoaded=new Vector();			//COULD BE CREATED LAZILY

	private boolean duringStartup;

	//information to retrieve the stacktrace from the file
	private long beginningPosition=-1;
	private long endPosition=-1;

	static private boolean booting = true;

	public static void setBooting(boolean value) {
		booting = value;
	}

	public ClassStats(String name, ClassloaderStats cli) {
		className = name;
		timeStampsOfLoading = System.currentTimeMillis();
		duringStartup = booting;
		classloader = cli;
	}

	public void setLoadingNumber(int order) {
		loadingNumber = order;
	}

	public void loadingDone() {
		timeToLoad = System.currentTimeMillis()-timeStampsOfLoading;
	}

	public long getTotalTime() {
		return timeToLoad;
	}

	public long getRealTime() {
		return timeToLoad-timeSpentLoadingOthers;
	}

	public void timeSpentLoadingOthers(long time) {
		timeSpentLoadingOthers=timeSpentLoadingOthers+time;
	}

	public long getBeginningPosition() {
		return beginningPosition;
	}

	public long getEndPosition() {
		return endPosition;
	}

	public void setBeginningPosition(long beginningPosition) {
		this.beginningPosition = beginningPosition;
	}

	public void setEndPosition(long endPosition) {
		this.endPosition = endPosition;
	}

	public void loads(ClassStats ci) {
		hasLoaded.add(ci);
	}

	public void setLoadedBy(ClassStats ci) {
		loadedBy = ci;
	}

	public ClassStats getLoadedBy() {
		return loadedBy;
	}

	public Vector getHasLoaded() {
		return hasLoaded;
	}

	public String getClassName() {
		return className;
	}

	public boolean isDuringStartup() {
		return duringStartup;
	}

	public ClassloaderStats getClassloader() {
		return classloader;
	}

	public int getLoadingNumber() {
		return loadingNumber;
	}

	public long getTimeStampsOfLoading() {
		return timeStampsOfLoading;
	}

	public void toBaseClass() {
		duringStartup = true;
		loadingNumber = -2;
	}
}
