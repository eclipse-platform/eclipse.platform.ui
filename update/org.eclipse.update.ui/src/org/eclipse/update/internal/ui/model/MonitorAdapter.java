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
package org.eclipse.update.internal.ui.model;

import org.eclipse.core.runtime.IProgressMonitor;

public class MonitorAdapter implements IProgressMonitor {

	/**
	 * @see IProgressMonitor#beginTask(String, int)
	 */
	public void beginTask(String taskName, int count) {
	}


	/**
	 * @see IProgressMonitor#done()
	 */
	public void done() {
	}


	/**
	 * @see IProgressMonitor#internalWorked(double)
	 */
	public void internalWorked(double arg0) {
	}


	/**
	 * @see IProgressMonitor#isCanceled()
	 */
	public boolean isCanceled() {
		return false;
	}


	/**
	 * @see IProgressMonitor#setCanceled(boolean)
	 */
	public void setCanceled(boolean arg0) {
	}


	/**
	 * @see IProgressMonitor#setTaskName(String)
	 */
	public void setTaskName(String arg0) {
	}


	/**
	 * @see IProgressMonitor#subTask(String)
	 */
	public void subTask(String arg0) {
	}


	/**
	 * @see IProgressMonitor#worked(int)
	 */
	public void worked(int arg0) {
	}


}

