/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import org.eclipse.core.runtime.ProgressMonitorWrapper;

public class ThrottlingProgressMonitor extends ProgressMonitorWrapper {
	private float fThrottleRatio;
	private long fLastCalled;
	private long fSubMilis;

	public ThrottlingProgressMonitor(IProgressMonitor wrapped, float throttleRatio) {
		super(wrapped);
		fThrottleRatio= throttleRatio;
		fSubMilis= 0;
		fLastCalled= 0;
	}

	public void internalWorked(double work) {
		super.internalWorked(work);
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
			if (sleepTime > 0) {
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					// ignore
				}
			} else {
				Thread.yield();
			}
		} else {
			fLastCalled= System.currentTimeMillis();
		}
	}
}
