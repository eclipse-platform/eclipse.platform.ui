/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import junit.framework.TestSuite;

/**
 * Test the workbench. This suite was created as a
 * workaround for problems running the suites from the
 * command line.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	IWorkbenchTest.class,
	IWorkbenchWindowTest.class,
})
public class IWorkbenchTestSuite extends TestSuite {
}
