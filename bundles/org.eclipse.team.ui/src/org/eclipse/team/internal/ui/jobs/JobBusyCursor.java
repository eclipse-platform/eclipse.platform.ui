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
import org.eclipse.swt.widgets.*;

/**
 * This class will show a busy cursor over a control when jobs of a particular type
 * are running.
 */
public class JobBusyCursor implements IJobListener {

	private Composite composite;
	private Cursor waitCursor;
	private QualifiedName jobType;

	public JobBusyCursor(Composite composite, QualifiedName jobType) {
		this.composite = composite;
		this.jobType = jobType;
		synchronized (this) {
			JobStatusHandler.addJobListener(this, jobType);
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
		if (getComposite() == null || (cursor != null && cursor.isDisposed())) return;
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				synchronized (this) {
					setCursorDeep(cursor, getComposite());
				}
			}
		});
	}
	
	private void setCursorDeep(Cursor cursor, Control control) {
		if (control != null && !control.isDisposed()) {
			control.setCursor(cursor);
			if(control instanceof Composite) {
				Composite composite = (Composite)control;
				Control[] children = composite.getChildren();
				for (int i = 0; i < children.length; i++) {
					setCursorDeep(cursor, children[i]);
				}
			}
		}
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
	public Composite getComposite() {
		return composite;
	}
}
