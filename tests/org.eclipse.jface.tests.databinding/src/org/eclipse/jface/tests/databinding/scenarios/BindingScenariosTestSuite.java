/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
 *     Brad Reynolds - bug 116920
 *******************************************************************************/
package org.eclipse.jface.tests.databinding.scenarios;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import junit.extensions.TestSetup;
import junit.framework.JUnit4TestAdapter;


/**
 * To run this test suite, right-click and select "Run As JUnit Plug-in Test".
 * This will also start an Eclipse instance. To clean up the launch
 * configuration, open up its "Main" tab and select "[No Application] - Headless
 * Mode" as the application to run. You can also run this class as an SWT
 * application.
 */
@RunWith(Suite.class)
@SuiteClasses({ ButtonControlScenario.class, ComboScenarios.class, ComboUpdatingTest.class, ComboViewerScenario.class,
		CustomConverterScenarios.class, CustomScenarios.class, ListViewerScenario.class, MasterDetailScenarios.class,
		NewTableScenarios.class, NPETestScenario.class, PropertyScenarios.class, SpinnerControlScenario.class,
		TableScenarios.class, TextControlScenario.class })
public class BindingScenariosTestSuite {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	private static Display display;

	private static Shell shell;

	public static junit.framework.Test suite() {
		return new TestSetup(new JUnit4TestAdapter(BindingScenariosTestSuite.class)) {
			@Override
			public void setUp() throws Exception {
				Display d = Display.getDefault();
				shell = new Shell(d, SWT.SHELL_TRIM);
				shell.setLayout(new FillLayout());
			}

			@Override
			public void tearDown() throws Exception {
				shell.close();
				shell.dispose();
				if (display != null) {
					display.dispose();
				}
			}
		};
	}

	public static Shell getShell() {
		return shell;
	}

}
