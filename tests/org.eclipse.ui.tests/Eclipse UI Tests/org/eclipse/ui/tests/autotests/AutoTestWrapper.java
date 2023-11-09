/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
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

import junit.framework.TestCase;

/**
 * @since 3.1
 */
public class AutoTestWrapper extends TestCase {
	private final AutoTest test;
	private final AbstractTestLogger log;

	public AutoTestWrapper(AutoTest test, AbstractTestLogger resultLog) {
		super(test.getName());

		this.test = test;
		this.log = resultLog;
	}

	@Override
	protected void runTest() throws Throwable {
		String testName = test.getName();

		TestResult result;
		try {
			result = new TestResult(test.performTest());
		} catch (Throwable t) {
			result = new TestResult(t);
		}

		log.reportResult(testName, result);
	}
}
