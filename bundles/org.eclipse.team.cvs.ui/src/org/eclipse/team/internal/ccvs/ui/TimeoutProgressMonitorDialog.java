/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class TimeoutProgressMonitorDialog extends ProgressMonitorDialog {
	// the timeout
	private int timeout;
	// the number of currently running runnables.
	private int runningRunnables = 0;

	/**
	 * Creates a progress monitor dialog under the given shell.
	 * The dialog has a standard title and no image. 
	 * <code>open</code> is non-blocking.
	 *
	 * @param parent the parent shell
	 * @param timeout the delay after which the dialog will be opened during a run()
	 */
	public TimeoutProgressMonitorDialog(Shell parent, int timeout) {
		super(parent);
		this.timeout = timeout;
	}

	/* (non-Javadoc)
	 * Method declared on ITeamRunnableContext.
	 * Runs the given <code>IRunnableWithProgress</code> with the progress monitor for this
	 * progress dialog.  The dialog is opened before it is run, and closed after it completes.
	 */
	public void run(final boolean fork, boolean cancelable, final IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
		setCancelable(cancelable);
		create(); // create the Shell but don't open it yet
		try {
			runningRunnables++;
			final Display display = getShell().getDisplay();
			display.timerExec(timeout, new Runnable() {
				public void run() {
					Shell shell = getShell();
					if (shell != null && ! shell.isDisposed()) open();
				}
			});
			
			final Exception[] holder = new Exception[1];
			BusyIndicator.showWhile(display, new Runnable() {
				public void run() {
					try {
						ModalContext.run(runnable, fork, getProgressMonitor(), display);
					} catch (InvocationTargetException ite) {
						holder[0] = ite;
					} catch (InterruptedException ie) {
						holder[0] = ie;
					}
				}
			});
			if (holder[0] != null) {
				if (holder[0] instanceof InvocationTargetException) {
					throw (InvocationTargetException) holder[0];
				} else if (holder[0] instanceof InterruptedException) {
					throw (InterruptedException) holder[0];
				}
			}
		} finally {
			runningRunnables--;
			close();
		}
	}
	
	public boolean close() {
		if (runningRunnables <= 0) return super.close();
		return false;
	}
}
