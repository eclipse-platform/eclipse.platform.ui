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
package org.eclipse.ltk.internal.ui.refactoring;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;

import org.eclipse.ui.PlatformUI;

public class ProgressService {
	
	public static void runSuspended(boolean fork, boolean cancelable, WorkbenchRunnableAdapter adapter) throws InvocationTargetException, InterruptedException {
		runSuspended(PlatformUI.getWorkbench().getProgressService(), fork, cancelable, adapter, adapter.getSchedulingRule());
	}
	
	public static void runSuspended(boolean fork, boolean cancelable, IRunnableWithProgress runnable, ISchedulingRule rule) throws InvocationTargetException, InterruptedException {
		runSuspended(PlatformUI.getWorkbench().getProgressService(), fork, cancelable, runnable, rule);
	}
	
	public static void runSuspended(IRunnableContext context, boolean fork, boolean cancelable, WorkbenchRunnableAdapter adapter) throws InvocationTargetException, InterruptedException {
		runSuspended(context, fork, cancelable, adapter, adapter.getSchedulingRule());
	}
	
	public static void runSuspended(IRunnableContext context, boolean fork, boolean cancelable, IRunnableWithProgress runnable, final ISchedulingRule rule) throws InvocationTargetException, InterruptedException {
		final IJobManager manager= Platform.getJobManager();
		try {
			try {
				BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
					public void run() {
						manager.suspend(rule, null);
					}
				});
			} catch (OperationCanceledException e) {
				throw new InterruptedException(e.getMessage());
			}
			context.run(fork, cancelable, runnable);
		} catch(OperationCanceledException e) {
			// make sure we don't leak any operation canceled exceptions
			throw new InterruptedException(e.getMessage());
		} finally {
			manager.resume(rule);
		}
	}
}
