/*******************************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.core.internal.resources;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class DelayedSnapshotRunnable implements Runnable {
	private boolean canceled;
	private SaveManager saveManager;
	private long delay;
	
	private static final long MIN_DELAY= 1000;

	public DelayedSnapshotRunnable(SaveManager manager, long delay) {
		saveManager= manager;
		if (delay < MIN_DELAY)
			delay= MIN_DELAY;
		this.delay= delay;
		canceled= false;
	}
	
	/*
	 * @see Runnable#run()
	 */
	public void run() {
		synchronized (this) {
			try {
				wait(delay);
			} catch (InterruptedException e) {
				canceled= true;
			}
		}
		if (!canceled) {
			runSnapshot();
		}		
	}

	public synchronized void cancel() {
		canceled= true;
		notifyAll();
	}

	private void runSnapshot() {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor m) throws CoreException {
				if (!canceled) {
					Workspace workspace= (Workspace)ResourcesPlugin.getWorkspace();
					if (workspace != null) {
						workspace.getWorkManager().avoidAutoBuild();
						saveManager.requestSnapshot();
					}
				}
			}
		};
		try {
			IWorkspace workspace= ResourcesPlugin.getWorkspace();			
			if (workspace != null) {
				workspace.run(runnable, null);
			}
		} catch(CoreException e) {
			ResourcesPlugin.getPlugin().getLog().log(e.getStatus());
		}		
	}	
}
