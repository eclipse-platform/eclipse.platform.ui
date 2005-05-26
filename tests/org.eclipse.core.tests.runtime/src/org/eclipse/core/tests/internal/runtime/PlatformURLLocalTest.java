/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.runtime;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.tests.runtime.RuntimeTest;
import org.eclipse.core.tests.runtime.RuntimeTestsPlugin;

public class PlatformURLLocalTest extends RuntimeTest {

	public static void assertEquals(String tag, URL expected, URL actual, boolean external) {
		if (external) {
			assertEquals(tag, expected, actual);
			return;
		}
		try {
			assertEquals(tag + ".1", new URL(expected.getProtocol(), expected.getHost(), expected.getPort(), expected.getFile()), new URL(actual.getProtocol(), actual.getHost(), actual.getPort(), actual.getFile()));
		} catch (MalformedURLException e) {
			fail(tag + ".2", e);
		}
	}

	public static Test suite() {
		return new TestSuite(PlatformURLLocalTest.class);
	}

	public PlatformURLLocalTest(String name) {
		super(name);
	}

	public void testPlatformURLConfigResolution() {
		URL platformURL = null;
		try {
			// 	create a fake URL
			platformURL = new URL("platform:/config/x");
		} catch (MalformedURLException e) {
			fail("1.0", e);
		}
		URL resolvedURL = null;
		try {
			resolvedURL = Platform.resolve(platformURL);
		} catch (IOException e) {
			fail("2.0", e);
		}
		assertFalse("3.0", platformURL.equals(resolvedURL));
		URL expected = null;
		try {
			expected = new URL(Platform.getConfigurationLocation().getURL(), "x");
		} catch (MalformedURLException e) {
			fail("4.0", e);
		}
		assertEquals("5.0", expected, resolvedURL, false);
	}

	public void testPlatformURLMetaResolution() {
		URL platformURL = null;
		try {
			// 	create a fake URL
			platformURL = new URL("platform:/meta/" + PI_RUNTIME_TESTS + "/x");
		} catch (MalformedURLException e) {
			fail("1.0", e);
		}
		URL resolvedURL = null;
		try {
			resolvedURL = Platform.resolve(platformURL);
		} catch (IOException e) {
			fail("2.0", e);
		}
		assertFalse("3.0", platformURL.equals(resolvedURL));
		URL expected = null;
		try {
			expected = new URL(RuntimeTestsPlugin.getPlugin().getStateLocation().toFile().toURL(), "x");
		} catch (MalformedURLException e) {
			fail("4.0", e);
		}
		assertEquals("5.0", expected, resolvedURL, false);
	}
}
