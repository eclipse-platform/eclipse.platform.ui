/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.runtime.*;

/**
 * Test cases for the Platform API
 */
public class PlatformTest extends RuntimeTest {

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

	public static Test suite() {
		TestSuite suite = new TestSuite(PlatformTest.class.getName());
		suite.addTest(new PlatformTest("testKeyRing1"));
		suite.addTest(new PlatformTest("testKeyRing2"));
		suite.addTest(new PlatformTest("testGetCommandLine"));
		suite.addTest(new PlatformTest("testGetLocation"));
		//	suite.addTest(new PlatformTest("testRetrievePlugins"));
		suite.addTest(new PlatformTest("testRunnable"));
		return suite;
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

	public void testRetrievePlugins() {
		assertNull("1.0", Platform.getPlugin(""));
		assertNull("1.1", Platform.getPlugin("qwert666yuiop"));

		IPluginRegistry registry = Platform.getPluginRegistry();
		IPluginDescriptor descriptors[] = registry.getPluginDescriptors();

		for (int i = 0; i < descriptors.length; i++) {
			assertNotNull("2." + i, Platform.getPlugin(descriptors[i].getUniqueIdentifier()).getDescriptor().getInstallURL());
			IPath location;
			try {
				location = Platform.getPluginStateLocation(descriptors[i].getPlugin());
				assertTrue("3." + i, true);
			} catch (CoreException e) {
				assertTrue("3." + i, false);
				continue; // no point continuing this descriptor
			}

			assertNotNull("4." + i, location);
		}
	}

	public void testRunnable() {
		final Vector exceptions = new Vector();

		ISafeRunnable runnable = new ISafeRunnable() {
			public void handleException(Throwable exception) {
				exceptions.addElement(exception);
			}

			public void run() throws Exception {
				throw new Exception("PlatformTest.testRunnable: this exception is thrown on purpose as part of the test.");
			}
		};

		Platform.run(runnable);

		assertEquals("1.0", exceptions.size(), 1);
	}

}