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
package org.eclipse.search.core.tests;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 */
public class LongQuery extends NullQuery {
	
	private boolean fIsRunning= false;
	
	public boolean canRunInBackground() {
		return true;
	}
	
	public IStatus run(IProgressMonitor monitor) {
		fIsRunning= true;
		while (!monitor.isCanceled()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// should not happen
			}
		}
		fIsRunning= false;
		return new Status(IStatus.OK, "some plugin", 0, "No message", null);
	}
	
	public boolean isRunning() {
		return fIsRunning;
	}
}
