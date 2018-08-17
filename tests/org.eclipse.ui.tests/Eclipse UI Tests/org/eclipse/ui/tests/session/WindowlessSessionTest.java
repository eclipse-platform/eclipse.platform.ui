/*******************************************************************************

 * Copyright (c) 2010, 2012 IBM Corporation and others.
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
package org.eclipse.ui.tests.session;

import junit.framework.TestSuite;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.tests.harness.util.UITestCase;


/**
 * @since 3.7
 */
public class WindowlessSessionTest extends UITestCase {
	public static TestSuite suite() {
		TestSuite ts = new TestSuite("org.eclipse.ui.tests.session.WindowlessSessionTest");
		ts.addTest(new WindowlessSessionTest("testWindowlessWorkbench"));
		return ts;
	}

	public WindowlessSessionTest(String name) {
		super(name);
	}

	public void testWindowlessWorkbench() throws Exception {

		// There should not be any windows in this app
		assertTrue(fWorkbench.getWorkbenchWindowCount() == 0);

		// Now open a window
		IWorkbenchWindow window = fWorkbench.openWorkbenchWindow(null);

		// window count should be 1
		assertTrue(fWorkbench.getWorkbenchWindowCount() == 1);

		window.close();

		// now the workbench should stay without a window
		assertTrue(fWorkbench.getWorkbenchWindowCount() == 0);
	}
}
