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
package org.eclipse.search2.internal.ui;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Thomas Mäder
 *
 */
public class ThrottlingProgressMonitor implements IProgressMonitor {
	private IProgressMonitor fWrappedMonitor;
	private float fThrottleRatio;
	private long fLastCalled;
	private long fSubMilis;

	public ThrottlingProgressMonitor(IProgressMonitor wrapped, float throttleRatio) {
		fWrappedMonitor= wrapped;
		fThrottleRatio= throttleRatio;
		fSubMilis= 0;
	}

	public void beginTask(String name, int totalWork) {
		fWrappedMonitor.beginTask(name, totalWork);
	}

	public void done() {
		fWrappedMonitor.done();
	}

	public void internalWorked(double work) {
		fWrappedMonitor.internalWorked(work);
		if (fLastCalled != 0) {
			long sleepTime= System.currentTimeMillis()-fLastCalled;
			sleepTime *= fThrottleRatio;
			sleepTime= Math.min(100, sleepTime);
			if (sleepTime < 1) {
				fSubMilis++;
				if (fSubMilis > 50) {
					sleepTime= 1;
					fSubMilis= 0;
				}
			}
			fLastCalled= System.currentTimeMillis();
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				// ignore
				e.printStackTrace();
			}
		} else {
			fLastCalled= System.currentTimeMillis();
		}
	}

	public boolean isCanceled() {
		return fWrappedMonitor.isCanceled();
	}

	public void setCanceled(boolean value) {
		fWrappedMonitor.setCanceled(value);
	}

	public void setTaskName(String name) {
		fWrappedMonitor.setTaskName(name);
	}

	public void subTask(String name) {
		fWrappedMonitor.subTask(name);
	}

	public void worked(int work) {
		fWrappedMonitor.worked(work);
	}
}
