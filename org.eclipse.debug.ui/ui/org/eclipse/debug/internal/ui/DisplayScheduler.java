package org.eclipse.debug.internal.ui;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IScheduler;

/**
 * A scheduler for the UI thread
 */
public class DisplayScheduler implements IScheduler {

	/**
	 * @see org.eclipse.debug.core.IScheduler#scheduleRunnables()
	 */
	public void scheduleRunnables() {
		Runnable r = new Runnable() {
			public void run() {
				DebugPlugin.getDefault().schedule(DisplayScheduler.this);
			}
		};
		DebugUIPlugin.getStandardDisplay().asyncExec(r);
	}

}
