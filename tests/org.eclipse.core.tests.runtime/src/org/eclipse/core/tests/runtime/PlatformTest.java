/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.framework.log.FrameworkLog;
import org.osgi.framework.ServiceReference;

/**
 * Test cases for the Platform API
 */
public class PlatformTest extends RuntimeTest {

	private FrameworkLog logService;
	private ServiceReference logRef;
	private java.io.File originalLocation;

	/**
	 * Need a zero argument constructor to satisfy the test harness.
	 * This constructor should not do any real work nor should it be
	 * called by user code.
	 */
	public PlatformTest() {
		super(null);
	}

	public PlatformTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		//ensure platform locations are initialized
		Platform.getLogFileLocation();

		//setup reference to log service, and remember original log location
		logRef = RuntimeTestsPlugin.getContext().getServiceReference(FrameworkLog.class.getName());
		logService = (FrameworkLog) RuntimeTestsPlugin.getContext().getService(logRef);
		originalLocation = logService.getFile();
	}

	protected void tearDown() throws Exception {
		//undo any damage done by log location test
		super.tearDown();
		logService.setFile(originalLocation, true);
		RuntimeTestsPlugin.getContext().ungetService(logRef);
	}

	public static Test suite() {
		return new TestSuite(PlatformTest.class);
	}

	public void testKeyRing1() {
		URL serverUrl = null;
		try {
			serverUrl = new URL("http://www.hostname.com/");
		} catch (MalformedURLException e) {
			assertTrue("e1", false);
		}

		String realm = "realm1@hostname.com";
		String authScheme = "Basic";
		Map info = new Hashtable(2);
		info.put("username", "nogard");
		info.put("password", "secret");

		try {
			Platform.addAuthorizationInfo(serverUrl, realm, authScheme, info);
		} catch (CoreException e) {
			assertTrue("e2", false);
		}

		info = Platform.getAuthorizationInfo(serverUrl, realm, authScheme);

		assertEquals("00", "nogard", info.get("username"));
		assertEquals("01", "secret", info.get("password"));

		try {
			Platform.flushAuthorizationInfo(serverUrl, realm, authScheme);
		} catch (CoreException e) {
			assertTrue("e3", false);
		}

		info = Platform.getAuthorizationInfo(serverUrl, realm, authScheme);

		assertTrue("02", info == null);
	}

	public void testKeyRing2() {
		URL url1 = null;
		URL url2 = null;
		try {
			url1 = new URL("http://www.oti.com/file1");
			url2 = new URL("http://www.oti.com/folder1/");
		} catch (MalformedURLException e) {
			assertTrue("00", false);
		}

		String realm1 = "realm1";
		String realm2 = "realm2";

		try {
			Platform.addProtectionSpace(url1, realm1);
		} catch (CoreException e) {
			assertTrue("e0", false);
		}

		assertEquals("00", realm1, Platform.getProtectionSpace(url1));
		assertEquals("01", realm1, Platform.getProtectionSpace(url2));

		try {
			Platform.addProtectionSpace(url2, realm1);
		} catch (CoreException e) {
			assertTrue("e1", false);
		}

		assertEquals("02", realm1, Platform.getProtectionSpace(url1));
		assertEquals("03", realm1, Platform.getProtectionSpace(url2));

		try {
			Platform.addProtectionSpace(url2, realm2);
		} catch (CoreException e) {
			assertTrue("e2", false);
		}

		assertTrue("04", Platform.getProtectionSpace(url1) == null);
		assertEquals("05", realm2, Platform.getProtectionSpace(url2));

		try {
			Platform.addProtectionSpace(url1, realm1);
		} catch (CoreException e) {
			assertTrue("e3", false);
		}

		assertEquals("05", realm1, Platform.getProtectionSpace(url1));
		assertEquals("06", realm1, Platform.getProtectionSpace(url2));
	}

	public void testGetCommandLine() {
		assertNotNull("1.0", Platform.getCommandLineArgs());
	}

	public void testGetLocation() {
		assertNotNull("1.0", Platform.getLocation());
	}

	/**
	 * API test for {@link Platform#getResourceBundle(org.osgi.framework.Bundle)}
	 */
	public void testGetResourceBundle() {
		//ensure it returns non-null for bundle with a resource bundle
		ResourceBundle bundle = Platform.getResourceBundle(Platform.getBundle("org.eclipse.core.runtime"));
		assertNotNull(bundle);
		//ensure it throws exception for bundle with no resource bundle
		boolean failed = false;
		try {
			bundle = Platform.getResourceBundle(Platform.getBundle("org.eclipse.core.tests.runtime"));
		} catch (MissingResourceException e) {
			//expected
			failed = true;
		}
		assertTrue(failed);
	}

	public void testGetLogLocation() throws IOException {
		IPath initialLocation = Platform.getLogFileLocation();
		System.out.println(Platform.getLogFileLocation());
		Platform.getStateLocation(Platform.getBundle("org.eclipse.equinox.common"));//causes DataArea to be initialzed
		System.out.println(Platform.getLogFileLocation());

		assertNotNull("1.0", initialLocation);

		//ensure result is same as log service
		IPath logPath = new Path(logService.getFile().getAbsolutePath());
		assertEquals("2.0", logPath, initialLocation);

		//changing log service location should change log location
		File newLocation = File.createTempFile("testGetLogLocation", null);
		logService.setFile(newLocation, true);
		assertEquals("3.0", new Path(newLocation.getAbsolutePath()), Platform.getLogFileLocation());

		//when log is non-local, should revert to default location
		logService.setWriter(new StringWriter(), true);
		assertEquals("4.0", initialLocation, Platform.getLogFileLocation());
	}

	public void testRunnable() {
		final Vector exceptions = new Vector();

		final List collected = new ArrayList();

		// add a log listener to ensure that we report using the right plug-in id
		ILogListener logListener = new ILogListener() {
			public void logging(IStatus status, String plugin) {
				collected.add(status);
			}
		};
		Platform.addLogListener(logListener);

		final Exception exception = new Exception("PlatformTest.testRunnable: this exception is thrown on purpose as part of the test.");
		ISafeRunnable runnable = new ISafeRunnable() {
			public void handleException(Throwable exception) {
				exceptions.addElement(exception);
			}

			public void run() throws Exception {
				throw exception;
			}
		};

		Platform.run(runnable);

		Platform.removeLogListener(logListener);

		assertEquals("1.0", exceptions.size(), 1);
		assertEquals("1.1", exception, exceptions.firstElement());

		// ensures the status object produced has the right plug-in id (bug 83614)
		assertEquals("2.0", collected.size(), 1);
		assertEquals("2.1", RuntimeTest.PI_RUNTIME_TESTS, ((IStatus) collected.get(0)).getPlugin());
	}

}
