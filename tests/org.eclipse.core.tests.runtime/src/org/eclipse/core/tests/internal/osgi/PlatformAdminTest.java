/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.osgi;

import java.util.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.runtime.adaptor.testsupport.SimpleBundleInstaller;
import org.eclipse.core.runtime.adaptor.testsupport.SimplePlatformAdmin;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;
import org.eclipse.osgi.service.resolver.*;
import org.osgi.framework.BundleException;

/**
 * NOTE: this test should not be enabled until we get a solution for the problem 
 * with non-visibility of internal packages in the framework/adaptor implementation. 
 */
public class PlatformAdminTest extends EclipseWorkspaceTest {
	public PlatformAdminTest(String name) {
		super(name);
	}
	private final static String A1_LOCATION = "org.eclipse.a";
	private final static String A1_MANIFEST = "Bundle-SymbolicName: org.eclipse.a\n" + "Bundle-Version: 1.0.0\n" + "Export-Package: org.eclipse.package1,org.eclipse.package2\n" + ";Import-Package: org.eclipse.package3";
	private final static String B1_LOCATION = "org.eclipse.b";
	private final static String B1_MANIFEST = "Bundle-SymbolicName: org.eclipse.b\n" + "Bundle-Version: 1.0.0\n" + "Provide-Package: org.eclipse.b.package1,org.eclipse.b.package2,org.eclipse.b.package3\n" + "Export-Package: org.eclipse.package3";
	private final static String C1_LOCATION = "org.eclipse.c";
	private final static String C1_MANIFEST = "Bundle-SymbolicName: org.eclipse.c\n" + "Bundle-Version: 1.0.0\n" + "Required-Bundle: org.eclipse.b";
	private final static String D1_LOCATION = "org.eclipse.d";
	private final static String D1_MANIFEST = "Bundle-SymbolicName: org.eclipse.d\n" + "Bundle-Version: 1.0.0";
	public void testCommit() {
		PlatformAdmin platformAdmin = new SimplePlatformAdmin(new SimpleBundleInstaller(), getRandomLocation().toFile());
		State state = platformAdmin.getState();
		BundleDescription a1 = null;
		BundleDescription b1 = null;
		BundleDescription c1 = null;
		BundleDescription d1 = null;
		try {
			a1 = state.getFactory().createBundleDescription(parseManifest(A1_MANIFEST), A1_LOCATION, 3);
			b1 = state.getFactory().createBundleDescription(parseManifest(B1_MANIFEST), B1_LOCATION, 1);
			c1 = state.getFactory().createBundleDescription(parseManifest(C1_MANIFEST), C1_LOCATION, 2);
			d1 = state.getFactory().createBundleDescription(parseManifest(D1_MANIFEST), D1_LOCATION, 4);
		} catch (BundleException e) {
			fail("0.0", e);
		}
		state.addBundle(b1);
		state.addBundle(c1);
		assertEquals("0.5", 2, state.getBundles().length);
		try {
			platformAdmin.commit(state);
		} catch (BundleException e) {
			fail("1.0 - " + e.toString());
		}
		assertEquals("2.0", 2, platformAdmin.getState().getBundles().length);
		State staleState = platformAdmin.getState();
		state = platformAdmin.getState();
		state.addBundle(a1);
		try {
			platformAdmin.commit(state);
		} catch (BundleException e) {
			fail("3.0 - " + e.toString());
		}
		assertEquals("4.0", 3, platformAdmin.getState().getBundles().length);
		staleState.addBundle(d1);
		try {
			platformAdmin.commit(staleState);
			fail("5.0");
		} catch (BundleException e) {
			// that is ok, it should have failed
		}
		state = platformAdmin.getState();
		assertNotNull("5.9", state.removeBundle(1));
		try {
			platformAdmin.commit(state);
		} catch (BundleException e) {
			fail("6.0 - " + e.toString());
		}
		assertEquals("7.0", 2, platformAdmin.getState().getBundles().length);
	}
	private Dictionary parseManifest(String manifest) {
		Dictionary entries = new Hashtable();
		StringTokenizer tokenizer = new StringTokenizer(manifest, ":\n");
		while (tokenizer.hasMoreTokens()) {
			String key = tokenizer.nextToken();
			String value = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : "";
			entries.put(key, value);
		}
		return entries;
	}
	public static Test suite() {
		return new TestSuite(PlatformAdminTest.class);
	}
}
