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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


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
}
