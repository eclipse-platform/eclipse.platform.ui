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
package org.eclipse.core.tests.internal.runtime;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.runtime.AuthorizationDatabase;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.tests.runtime.RuntimeTest;

public class AuthorizationDatabaseTest extends RuntimeTest {
	public AuthorizationDatabaseTest() {
		super(null);
	}

	public AuthorizationDatabaseTest(String name) {
		super(name);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(AuthorizationDatabaseTest.class.getName());
		suite.addTest(new AuthorizationDatabaseTest("test1"));
		suite.addTest(new AuthorizationDatabaseTest("test2"));
		suite.addTest(new AuthorizationDatabaseTest("test3"));
		return suite;
	}

	public void test1() {
		File file = new File(Platform.getLocation().toFile(), Long.toString(System.currentTimeMillis()) + ".auth");
		try {
			String filename = file.getAbsolutePath();
			String password = "testing";
			if (file.exists()) {
				file.delete();
			}

			AuthorizationDatabase db = new AuthorizationDatabase(filename, password);

			URL serverUrl = new URL("http://www.oti.com/");
			URL resourceUrl = new URL("http://www.oti.com/folder/");
			String realm = "WallyWorld";
			String authScheme = "Basic";
			Map info = new Hashtable(2);
			info.put("username", "jonathan");
			info.put("password", "testing");

			db.addAuthorizationInfo(serverUrl, realm, authScheme, info);
			db.addProtectionSpace(resourceUrl, realm);

			db.save();

			db = new AuthorizationDatabase(filename, password);

			info = db.getAuthorizationInfo(serverUrl, realm, authScheme);
			assertEquals("00", "jonathan", info.get("username"));
			assertEquals("01", "testing", info.get("password"));

			assertEquals("02", realm, db.getProtectionSpace(resourceUrl));
			assertEquals("03", realm, db.getProtectionSpace(new URL(resourceUrl.toString() + "file")));
		} catch (Exception e) {
			assertTrue("04", false);
		} finally {
			file.delete();
		}

	}

	public void test2() {
		AuthorizationDatabase db = new AuthorizationDatabase();

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

		db.addProtectionSpace(url1, realm1);

		assertEquals("00", realm1, db.getProtectionSpace(url1));
		assertEquals("01", realm1, db.getProtectionSpace(url2));

		db.addProtectionSpace(url2, realm1);

		assertEquals("02", realm1, db.getProtectionSpace(url1));
		assertEquals("03", realm1, db.getProtectionSpace(url2));

		db.addProtectionSpace(url2, realm2);

		assertTrue("04", db.getProtectionSpace(url1) == null);
		assertEquals("05", realm2, db.getProtectionSpace(url2));

		db.addProtectionSpace(url1, realm1);

		assertEquals("05", realm1, db.getProtectionSpace(url1));
		assertEquals("06", realm1, db.getProtectionSpace(url2));
	}

	public void test3() {
		AuthorizationDatabase db = new AuthorizationDatabase();

		URL url1 = null;
		try {
			url1 = new URL("http://www.oti.com");
		} catch (MalformedURLException e) {
			assertTrue("00", false);
		}

		Hashtable info = new Hashtable(2);
		db.addAuthorizationInfo(url1, "realm1", "Basic", info);

		assertTrue("01", db.getAuthorizationInfo(url1, "realm1", "Basic") != null);
		db.flushAuthorizationInfo(url1, "realm1", "Basic");
		assertTrue("02", db.getAuthorizationInfo(url1, "realm1", "Basic") == null);
	}
}