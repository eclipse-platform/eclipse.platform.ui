/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.jface.progress;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

public abstract class UIJob extends NotifyingJob {
	private Display display;

	/**
	 * Convenience method to return a status for an exception.
	 * @param exception
	 * @return
	 */
	public static IStatus errorStatus(Throwable exception) {
		return new Status(
			IStatus.ERROR,
			"org.eclipse.jface",
			IStatus.ERROR,
			exception.getMessage(),
			exception);

	}
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public final IStatus run(final IProgressMonitor monitor) {

		Display asyncDisplay = getDisplay();
		
		if (asyncDisplay == null) {
			
			System.out.println("No display");
			return new Status(
				IStatus.ERROR,
				"org.eclipse.jface",
				IStatus.ERROR,
				"Display must be set",
				null);
		}
		asyncDisplay.asyncExec(new Runnable() {
			public void run() {
				IStatus result = null;
				try {
					result = runInUIThread(monitor);
				} finally {
					if (result == null)
						result =
							new Status(
								IStatus.ERROR,
								"org.eclipse.ui",
								1,
								"Error",
								null);
					done(result);
				}
			}
		});
		return Job.ASYNC_FINISH;
	}
	/**
	 * Run the job in the UI Thread.
	 */
	public abstract IStatus runInUIThread(IProgressMonitor monitor);

	public void setDisplay(Display runDisplay) {
		display = runDisplay;
	}
	/**
	 * Get the display for use by the receiver.
	 * @return
	 */
	public Display getDisplay() {
		return display;
	}

}
