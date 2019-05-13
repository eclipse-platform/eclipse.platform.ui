/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
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
package org.eclipse.ui.tests.autotests;

import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * @since 3.1
 */
public abstract class UITestCaseWithResult extends UITestCase implements AutoTest {
	private AbstractTestLogger resultLog;

	public UITestCaseWithResult(String testName, AbstractTestLogger log) {
		super(testName);
		this.resultLog = log;
	}

	@Override
	protected final void runTest() throws Throwable {
		String testName = getName();

		TestResult result;
		try {
			result = new TestResult(performTest());
		} catch (Throwable t) {
			result = new TestResult(t);
		}

		resultLog.reportResult(testName, result);
	}

}
