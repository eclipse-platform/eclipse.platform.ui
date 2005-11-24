/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.databinding.scenarios;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * To run this test suite, right-click and select "Run As JUnit Plug-in Test".
 * This will also start an Eclipse instance. To clean up the launch
 * configuration, open up its "Main" tab and select "[No Application] - Headless
 * Mode" as the application to run. You can also run this class as an SWT
 * application.
 */
public class BindingScenariosTestSuite extends TestSuite {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	private static Display display;

	private static Shell shell;

	public static Test suite() {
		return new TestSetup(new BindingScenariosTestSuite()) {
			protected void setUp() throws Exception {
				Display d = Display.getDefault();
				if (d == null) {
					display = new Display();
					d = display;
				}
				shell = new Shell(d, SWT.SHELL_TRIM);
			}

			protected void tearDown() throws Exception {
				shell.close();
				shell.dispose();
				if (display != null) {
					display.dispose();
				}
			}
		};
	}

	public BindingScenariosTestSuite() {
		addTestSuite(UpdatableFactoriesTest.class);
		addTestSuite(PropertyScenarios.class);
		addTestSuite(CustomScenarios.class);
		addTestSuite(CustomConverterScenarios.class);
		addTestSuite(MasterDetailScenarios.class);
		addTestSuite(ReadOnlyComboScenarios.class);
		addTestSuite(TableScenarios.class);
		addTestSuite(TreeScenarios.class);
		// Test each of the basic SWT controls
		addTestSuite(TextControlScenario.class);
		addTestSuite(SpinnerControlScenario.class);	
		addTestSuite(ButtonControlScenario.class);		
		// Test each of the basic JFace controls
		addTestSuite(ComboViewerScenario.class);		
		addTestSuite(ListViewerScenario.class);		
	}

	public static Shell getShell() {
		return shell;
	}

}
