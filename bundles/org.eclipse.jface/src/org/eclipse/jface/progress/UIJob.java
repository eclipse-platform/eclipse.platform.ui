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

/**
 * The UIJob is a Job that runs within the UI Thread via an
 * asyncExec.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.jface.resource.JFaceResources;

public abstract class UIJob extends Job {
	private Display display;
	
	private static final String PLUGIN_NAME = "org.eclipse.jface"; //$NON-NLS-1$

	/**
	 * Create a new instance of the receiver.
	 */
	public UIJob() {
		super();
	}

	/**
	 * Create a new instance of the receiver with the supplied
	 * Display.
	 * @param jobDisplay
	 */
	public UIJob(Display jobDisplay) {
		this();
		display = jobDisplay;
	}

	/**
	 * Convenience method to return a status for an exception.
	 * @param exception
	 * @return
	 */
	public static IStatus errorStatus(Throwable exception) {
		return new Status(
			IStatus.ERROR,
			PLUGIN_NAME,
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
			return new Status(
				IStatus.ERROR,
				PLUGIN_NAME,
				IStatus.ERROR,
				JFaceResources.getString("UIJob.displayNotSet"), //$NON-NLS-1$
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
								PLUGIN_NAME,
								1,
								JFaceResources.getString("Error"), //$NON-NLS-1$
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
