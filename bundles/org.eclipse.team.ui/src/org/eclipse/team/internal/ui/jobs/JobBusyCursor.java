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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.*;

/**
 * This is temporary until the UI adds support for this directly into views.
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=51991
 */
public class JobBusyCursor {

	private Composite composite;
	private Cursor waitCursor;

	public JobBusyCursor(Composite composite) {
		this.composite = composite;
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
					if (cursor != null && cursor.isDisposed()) return;
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
	}
	
	public void started() {
		showCursor(getWaitCursor());
	}

	public void finished() {
		showCursor(null);
	}

	public Composite getComposite() {
		return composite;
	}
}
