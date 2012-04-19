/*******************************************************************************
 * Copyright (c) 2003, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.commands;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for all areas of command support for the platform.
 */
public final class CommandsTestSuite extends TestSuite {

	/**
	 * Returns the suite. This is required to use the JUnit Launcher.
	 */
	public static final Test suite() {
		return new CommandsTestSuite();
	}

	/**
	 * Construct the test suite.
	 */
	public CommandsTestSuite() {
		addTest(new TestSuite(CommandExecutionTest.class));
		addTest(new TestSuite(Bug66182Test.class));
		addTest(new TestSuite(Bug70503Test.class));
		addTest(new TestSuite(Bug73756Test.class));
		addTest(new TestSuite(Bug74982Test.class));
		addTest(new TestSuite(Bug74990Test.class));
		addTest(new TestSuite(Bug87856Test.class));
		addTest(new TestSuite(Bug125792Test.class));
		addTest(new TestSuite(CommandManagerTest.class));
		addTest(new TestSuite(CommandParameterTypeTest.class));
		addTest(new TestSuite(CommandSerializationTest.class));
		addTest(new TestSuite(HelpContextIdTest.class));
		addTest(new TestSuite(StateTest.class));
		addTest(new TestSuite(HandlerActivationTest.class));
		addTest(new TestSuite(CommandCallbackTest.class));
		addTest(new TestSuite(CommandEnablementTest.class));
		addTest(new TestSuite(CommandActionTest.class));
		addTest(new TestSuite(ActionDelegateProxyTest.class));
		addTest(ToggleStateTest.suite());
		addTest(new TestSuite(RadioStateTest.class));
	}
}
