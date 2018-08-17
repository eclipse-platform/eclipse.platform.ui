/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 474132
 *******************************************************************************/

package org.eclipse.ui.tests.operations;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests for the platform operations support.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	OperationsAPITest.class,
	WorkbenchOperationHistoryTests.class,
	MultiThreadedOperationsTests.class,
	WorkbenchOperationStressTests.class,
	WorkspaceOperationsTests.class
})
public class OperationsTestSuite {
}
