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
package org.eclipse.compare.contentmergeviewer;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.core.runtime.IProgressMonitor;
import java.lang.reflect.InvocationTargetException;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;


class DelayedProgressMonitor implements IProgressMonitor {
	
	ProgressMonitorDialog fProgressDialog;
	IProgressMonitor fRealProgressMonitor;
	String fTaskName;
	String fSubTaskName;
	int fTotalWork;
	int fWorked;
	boolean fCancelable;
	Shell fShell;
	int fTime;
	
	
	DelayedProgressMonitor(Shell shell) {
		fShell= shell;
	}

	/*
	 * @see IProgressMonitor#beginTask(String, int)
	 */
	public void beginTask(String name, int totalWork) {
		fTaskName= name;
		fTotalWork= totalWork;
		fTime= 0;
	}

	/*
	 * @see IProgressMonitor#done()
	 */
	public void done() {
		if (fRealProgressMonitor != null)
			fRealProgressMonitor.done();
	}

	/*
	 * @see IProgressMonitor#internalWorked(double)
	 */
	public void internalWorked(double work) {
		if (fRealProgressMonitor != null) {
			fRealProgressMonitor.internalWorked(work);
		}
	}
	
	private void checkTimeout() {
		if (fRealProgressMonitor == null) {
			
			//if (fTime++ < 100)
			//	return;
			
			fProgressDialog= new ProgressMonitorDialog(fShell);
			fProgressDialog.setCancelable(true);
			fProgressDialog.open();
			fRealProgressMonitor= fProgressDialog.getProgressMonitor();
			fRealProgressMonitor.beginTask(fTaskName, fTotalWork);
			if (fSubTaskName != null)
				fRealProgressMonitor.subTask(fSubTaskName);
			fRealProgressMonitor.worked(fWorked);
		}
	}

	/*
	 * @see IProgressMonitor#isCanceled()
	 */
	public boolean isCanceled() {
		checkTimeout();
		if (fRealProgressMonitor != null)
			return fRealProgressMonitor.isCanceled();
		return false;
	}

	/*
	 * @see IProgressMonitor#setCanceled(boolean)
	 */
	public void setCanceled(boolean value) {
		if (fRealProgressMonitor != null)
			fRealProgressMonitor.setCanceled(value);
		else
			fCancelable= value;
	}

	/*
	 * @see IProgressMonitor#setTaskName(String)
	 */
	public void setTaskName(String name) {
		if (fRealProgressMonitor != null)
			fRealProgressMonitor.setTaskName(name);
		else
			fTaskName= name;
	}

	/*
	 * @see IProgressMonitor#subTask(String)
	 */
	public void subTask(String name) {
		if (fRealProgressMonitor != null)
			fRealProgressMonitor.subTask(name);
		else
			fSubTaskName= name;
	}

	/*
	 * @see IProgressMonitor#worked(int)
	 */
	public void worked(int work) {
		if (fRealProgressMonitor != null)
			fRealProgressMonitor.internalWorked(work);
		else {
			fWorked+= work;
			checkTimeout();
		}
	}
	
	public static void run(Shell shell, boolean fork, boolean cancelable, IRunnableWithProgress runnable)
						throws InvocationTargetException, InterruptedException {
		
		DelayedProgressMonitor pm= new DelayedProgressMonitor(shell);
		pm.checkTimeout();
		try {
			ModalContext.run(runnable, fork, pm, shell.getDisplay());
		} finally {
			if (pm.fProgressDialog != null)
				pm.fProgressDialog.close();
		}
	}
}

