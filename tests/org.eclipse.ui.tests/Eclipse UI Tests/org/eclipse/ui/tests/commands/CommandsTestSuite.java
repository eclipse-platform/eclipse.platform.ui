/*******************************************************************************
 * Copyright (c) 2003, 2019 IBM Corporation and others.
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
 *     Paul Pazderski - Bug 546546: migrate to JUnit4 suite
 *******************************************************************************/
package org.eclipse.ui.tests.commands;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Tests for all areas of command support for the platform.
 */
@Suite
@SelectClasses({
	CommandExecutionTest.class,
	Bug73756Test.class,
	Bug125792Test.class,
	Bug417762Test.class,
	CommandManagerTest.class,
	CommandParameterTypeTest.class,
	CommandSerializationTest.class,
	HandlerActivationTest.class,
	CommandCallbackTest.class,
	CommandActionTest.class,
	HelpContextIdTest.class,
	StateTest.class,
	CommandEnablementTest.class,
	ActionDelegateProxyTest.class,
	ToggleStateTest.class,
	RadioStateTest.class,
	WorkbenchStateTest.class,
	E4CommandImageTest.class
})
public final class CommandsTestSuite {
}
