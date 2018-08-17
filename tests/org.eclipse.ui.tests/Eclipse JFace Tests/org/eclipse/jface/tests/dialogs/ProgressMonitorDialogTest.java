/*******************************************************************************
 * Copyright (c) 2010, 2017 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.jface.tests.dialogs;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Display;

import junit.framework.TestCase;

public class ProgressMonitorDialogTest extends TestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// ensure we've initialized a display for this thread
		Display.getDefault();
	}

	private void testRun(boolean fork, boolean cancelable) throws Exception {
		ProgressMonitorDialog pmd = new ProgressMonitorDialog(null);
		pmd.open();
		pmd.run(fork, cancelable, monitor -> {
			// nothing to do, just need this to happen to test bug 299731
		});

		// process asynchronous runnables, the error will happen here when we
		// try to do some with a widget that has already been disposed
		while (Display.getDefault().readAndDispatch()) {
		}
	}

	public void testRunTrueTrue() throws Exception {
		testRun(true, true);
	}

	public void testRunTrueFalse() throws Exception {
		testRun(true, false);
	}

	public void testRunFalseTrue() throws Exception {
		testRun(false, true);
	}

	public void testRunFalseFalse() throws Exception {
		testRun(false, false);
	}
}
