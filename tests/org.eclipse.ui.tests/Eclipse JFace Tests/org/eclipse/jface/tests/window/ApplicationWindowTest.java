/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.tests.window;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IProgressMonitorWithBlocking;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.widgets.Shell;

public class ApplicationWindowTest extends TestCase {

	private ApplicationWindow window;

	protected void tearDown() throws Exception {
		if (window != null) {
			// close the window
			window.close();
			window = null;
		}
		super.tearDown();
	}

	private void testBug334093(boolean fork, boolean cancelable)
			throws Exception {
		window = new ApplicationWindow(null) {
			public void create() {
				addStatusLine();
				super.create();
			}

			protected void createTrimWidgets(Shell shell) {
				// don't actually create the status line controls
			}
		};
		window.create();
		window.run(fork, cancelable, new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				monitor.beginTask("beginTask", 10);
				monitor.setTaskName("setTaskName");
				monitor.subTask("subTask");

				if (monitor instanceof IProgressMonitorWithBlocking) {
					IProgressMonitorWithBlocking blockingMonitor = (IProgressMonitorWithBlocking) monitor;
					blockingMonitor.setBlocked(Status.CANCEL_STATUS);
					blockingMonitor.clearBlocked();
				}

				monitor.worked(1);
				monitor.setCanceled(true);
				monitor.isCanceled();
				monitor.setCanceled(false);
				monitor.done();
			}
		});
	}

	public void testBug334093_TrueTrue() throws Exception {
		testBug334093(true, true);
	}

	public void testBug334093_TrueFalse() throws Exception {
		testBug334093(true, false);
	}

	public void testBug334093_FalseTrue() throws Exception {
		testBug334093(false, true);
	}

	public void testBug334093_FalseFalse() throws Exception {
		testBug334093(false, false);
	}

}
