/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.internal;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class WorkerJob extends Job {

	private final Worker worker;
	private Shell shell;
	private boolean fModal = true;
	
	public WorkerJob(String name) {
		super(name);
		worker = new Worker(name);
	}

	protected IStatus run(IProgressMonitor monitor) {
		if (isRunModally()) {
			runModally();
		} else {
			worker.run(monitor);
		}
		// reschedule to ensure we don't miss a task
		IStatus result = getResult(worker);
		schedule();
		return result;
	}

	protected boolean isRunModally() {
		return fModal;
	}

	private void runModally() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				IRunnableWithProgress runnable = new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException,
					InterruptedException {
						worker.run(monitor);
					}
				};
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
				try {
					if (shell == null) {
						PlatformUI.getWorkbench().getProgressService().run(true, true, runnable);
					} else {
						dialog.run(true, true, runnable);
					}
				} catch (InvocationTargetException e) {
					// Shouldn't happen so just log it
					CompareUIPlugin.log(e.getTargetException());
				} catch (InterruptedException e) {
					throw new OperationCanceledException();
				}
			}
		});
	}

	private IStatus getResult(Worker w) {
		Throwable[] errors = w.getErrors();
		if (errors.length == 0)
			return Status.OK_STATUS;
		if (errors.length == 1)
			return new Status(IStatus.ERROR, CompareUIPlugin.PLUGIN_ID, 0, errors[0].getMessage(), errors[0]);
		List statii = new ArrayList();
		for (int i = 0; i < errors.length; i++) {
			Throwable throwable = errors[i];
			statii.add(new Status(IStatus.ERROR, CompareUIPlugin.PLUGIN_ID, 0, errors[0].getMessage(), throwable));
		}
		return new MultiStatus(CompareUIPlugin.PLUGIN_ID, 0, (IStatus[]) statii.toArray(new IStatus[statii.size()]), "Multiple errors occurred while processing compare editor events", null);
	}
	
	public boolean shouldRun() {
		return worker.hasWork();
	}
	
	public void add(IRunnableWithProgress runnable) {
		worker.add(runnable);
		schedule();
	}

	public void setShell(Shell shell) {
		this.shell = shell;
	}

	public void setModal(boolean modal) {
		fModal = modal;
	}

}
