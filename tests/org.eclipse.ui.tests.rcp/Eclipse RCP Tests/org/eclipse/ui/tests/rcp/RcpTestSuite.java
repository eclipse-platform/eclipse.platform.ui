/*******************************************************************************
 * Copyright (c) 2004, 2011, 2014 IBM Corporation and others.
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
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 436344
 *******************************************************************************/
package org.eclipse.ui.tests.rcp;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * The test suite for the RCP APIs in the generic workbench.
 * To run, use a headless JUnit Plug-in Test launcher, configured
 * to have [No Application] as its application.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ PlatformUITest.class, WorkbenchAdvisorTest.class, WorkbenchConfigurerTest.class,
		WorkbenchWindowConfigurerTest.class, ActionBarConfigurerTest.class, IWorkbenchPageTest.class,
		WorkbenchSaveRestoreStateTest.class, WorkbenchListenerTest.class, WorkbenchTest.class })
public class RcpTestSuite {



	public RcpTestSuite() {
	}
}
