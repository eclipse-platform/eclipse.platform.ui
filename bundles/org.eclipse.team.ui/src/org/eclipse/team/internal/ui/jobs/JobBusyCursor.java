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
package org.eclipse.team.internal.ui.jobs;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * This class will show a busy cursor over a control when jobs of a particular type
 * are running.
 */
public class JobBusyCursor implements IJobListener {

	private Control control;
	private Cursor waitCursor;
	private QualifiedName jobType;

	public JobBusyCursor(Control control, QualifiedName jobType) {
		this.jobType = jobType;
		synchronized (this) {
			JobStatusHandler.addJobListener(this, jobType);
			setControl(control);
		}
	}
	
	private Cursor getWaitCursor() {
		if (waitCursor == null) {
			Display display = Display.getCurrent();
			if (display == null) {
				display = Display.getDefault();
			}
			waitCursor = new Cursor(display, SWT.CURSOR_APPSTARTING);
		}
		return waitCursor;
	}
	
	private void showCursor(final Cursor cursor) {
		if (getControl() == null) return;
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				synchronized (this) {
					if (getControl() != null && !getControl().isDisposed()) {
						getControl().setCursor(cursor);
					}
				}
			}
		});
	}
	
	public synchronized void dispose() {
		if (waitCursor != null) {
			waitCursor.dispose();
		}
		JobStatusHandler.removeJobListener(this, jobType);
	}

	private void showBusyCursor() {
		showCursor(getWaitCursor());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.jobs.IJobListener#started(org.eclipse.core.runtime.QualifiedName)
	 */
	public void started(QualifiedName jobType) {
		showBusyCursor();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.jobs.IJobListener#finished(org.eclipse.core.runtime.QualifiedName)
	 */
	public void finished(QualifiedName jobType) {
		showCursor(null);
	}

	/**
	 * @return Returns the control.
	 */
	public Control getControl() {
		return control;
	}

	/**
	 * Set the control in which the busy indicator should appear.
	 * Setting the control to <code>null</code> will prevent the cursor
	 * from appearing anywhere. When a control is provided, the busy
	 * cursor will appear if there is currently a job of the requested
	 * type running.
	 * @param control The control to set.
	 */
	public synchronized void setControl(Control control) {
		this.control = control;
		if (control != null) {
			if (JobStatusHandler.hasRunningJobs(jobType)) {
				showBusyCursor();
			}
		}
	}



}
