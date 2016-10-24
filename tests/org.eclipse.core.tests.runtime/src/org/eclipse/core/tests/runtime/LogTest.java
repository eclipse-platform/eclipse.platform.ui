/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stefan Xenos - initial API and implementation
 *     Stefan Xenos - bug 174539 - add a 1-argument convert(...) method
 *     Stefan Xenos - bug 174040 - SubMonitor#convert doesn't always set task name
 *     Stefan Xenos - bug 206942 - Regression test for infinite progress reporting rate
 *     IBM Corporation - bug 252446 - SubMonitor.newChild passes zero ticks to child
 *     Alexander Kurtakov <akurtako@redhat.com> - bug 458490
 *******************************************************************************/
package org.eclipse.core.tests.runtime;

import java.util.ArrayList;
import java.util.List;
import junit.framework.*;
import org.eclipse.core.runtime.*;

public class LogTest extends TestCase {
	private static String testMessage;

	private final List<IStatus> loggedStatus = new ArrayList<>();

	private ILogListener logListener = new ILogListener() {
		@Override
		public void logging(IStatus status, String plugin) {
			if (plugin.equals(RuntimeTestsPlugin.PI_RUNTIME_TESTS)) {
				loggedStatus.add(status);
			}
		}
	};

	public LogTest() {
		super();
	}

	public LogTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		testMessage = getName();
		Log.getLog(this).addLogListener(logListener);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		Log.getLog(this).removeLogListener(logListener);
	}

	public void testCorrectLogIsSelectedForObjectContext() {
		assertEquals(RuntimeTestsPlugin.getPlugin().getLog(), Log.getLog(this));
	}

	public void testCorrectLogIsSelectedForClassContext() {
		assertEquals(RuntimeTestsPlugin.getPlugin().getLog(), Log.getLog(LogTest.class));
	}

	public void testCorrectLogIsSelectedForStringContext() {
		assertEquals(RuntimeTestsPlugin.getPlugin().getLog(), Log.getLog(RuntimeTestsPlugin.PI_RUNTIME_TESTS));
	}

	public void testLogError() {
		Log.error(this, testMessage);
		assertLogged(IStatus.ERROR, testMessage, null);
	}

	public void testLogErrorWithException() {
		RuntimeException exception = new RuntimeException();
		Log.error(this, testMessage, exception);
		assertLogged(IStatus.ERROR, testMessage, exception);
	}

	public void testLogWarning() {
		Log.warning(this, testMessage);
		assertLogged(IStatus.WARNING, testMessage, null);
	}

	public void testLogWarningWithException() {
		RuntimeException exception = new RuntimeException();
		Log.warning(this, testMessage, exception);
		assertLogged(IStatus.WARNING, testMessage, exception);
	}

	public void testLogInfo() {
		Log.info(this, testMessage);
		assertLogged(IStatus.INFO, testMessage, null);
	}

	public void testLogInfoWithException() {
		RuntimeException exception = new RuntimeException();
		Log.info(this, testMessage, exception);
		assertLogged(IStatus.INFO, testMessage, exception);
	}

	public void testLog() {
		Status status = new Status(IStatus.ERROR, RuntimeTestsPlugin.PI_RUNTIME_TESTS, testMessage);
		Log.log(this, status);
		assertLogged(IStatus.ERROR, testMessage, null);
	}

	private void assertLogged(int error, String message, Throwable exception) {
		assertEquals("The wrong number of messages were logged", 1, loggedStatus.size());
		IStatus status = loggedStatus.get(0);
		assertEquals("The log message had the wrong severity", error, status.getSeverity());
		assertEquals("The wrong exception was logged", exception, status.getException());
	}

	public static Test suite() {
		return new TestSuite(LogTest.class);
	}
}
