/*******************************************************************************
 * Copyright (c) 2017, 2020 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 433608
 *******************************************************************************/
package org.eclipse.jface.tests.window;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Test;

public class ApplicationWindowTest {

	private ApplicationWindow window;

	@After
	public void tearDown() throws Exception {
		if (window != null) {
			// close the window
			window.close();
			window = null;
		}
	}

	private void testBug334093(boolean fork, boolean cancelable) throws Exception {
		window = new ApplicationWindow(null) {
			@Override
			public void create() {
				addStatusLine();
				super.create();
			}

			@Override
			protected void createTrimWidgets(Shell shell) {
				// don't actually create the status line controls
			}
		};
		window.create();
		window.run(fork, cancelable, monitor -> {
			monitor.beginTask("beginTask", 10);
			monitor.setTaskName("setTaskName");
			monitor.subTask("subTask");

			monitor.setBlocked(Status.CANCEL_STATUS);
			monitor.clearBlocked();

			monitor.worked(1);
			monitor.setCanceled(true);
			monitor.isCanceled();
			monitor.setCanceled(false);
			monitor.done();
		});
	}

	@Test
	public void testBug334093() throws Exception {
		boolean[] options = new boolean[] { true, false };
		for (boolean forkOption : options) {
			for (boolean cancelableOpton : options) {
				testBug334093(forkOption, cancelableOpton);
			}
		}
	}
}
