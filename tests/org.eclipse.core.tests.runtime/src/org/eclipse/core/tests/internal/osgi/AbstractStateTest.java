/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
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
import org.eclipse.core.runtime.adaptor.testsupport.SimplePlatformAdmin;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;
import org.eclipse.osgi.service.resolver.*;
import org.osgi.framework.BundleException;

public abstract class AbstractStateTest extends EclipseWorkspaceTest {
	protected PlatformAdmin platformAdmin;
	protected void setUp() throws Exception {
		super.setUp();
		platformAdmin = new SimplePlatformAdmin(getRandomLocation().toFile());
	}
	AbstractStateTest(String testName) {
		super(testName);
	}
	public static void assertContains(String tag, Object[] array, Object element) {
		for (int i = 0; i < array.length; i++)
			if (array[i] == element)
				return;
		fail(tag);
	}
	public static void assertEquals(State original, State copy) {
		assertEquals("", original, copy);
	}
	public static void assertEquals(String tag, BundleDescription original, BundleDescription copy) {
		assertEquals(tag + ".0", original.getBundleId(), copy.getBundleId());
		assertEquals(tag + ".1", original.getUniqueId(), copy.getUniqueId());
		assertEquals(tag + ".2", original.getVersion(), copy.getVersion());
		assertEquals(tag + ".3", original.getLocation(), copy.getLocation());
		assertEquals(tag + ".4", original.getState(), copy.getState());
		assertEquals(tag + ".5", original.getHost(), copy.getHost());
		PackageSpecification[] originalPackages = original.getPackages();
		PackageSpecification[] copyPackages = copy.getPackages();
		assertEquals(tag + ".6", originalPackages.length, copyPackages.length);
		for (int i = 0; i < originalPackages.length; i++)
			assertEquals(tag + ".7." + i, originalPackages[i], copyPackages[i]);
		String[] originalProvidedPackages = original.getProvidedPackages();
		String[] copyProvidedPackages = copy.getProvidedPackages();
		assertEquals(tag + ".8", originalProvidedPackages.length, copyProvidedPackages.length);
		for (int i = 0; i < originalProvidedPackages.length; i++)
			assertEquals(tag + ".9." + i, originalProvidedPackages[i], copyProvidedPackages[i]);
		BundleSpecification[] originalRequiredBundles = original.getRequiredBundles();
		BundleSpecification[] copyRequiredBundles = copy.getRequiredBundles();
		assertEquals(tag + ".10", originalRequiredBundles.length, copyRequiredBundles.length);
		for (int i = 0; i < originalRequiredBundles.length; i++)
			assertEquals(tag + ".11." + i, originalRequiredBundles[i], copyRequiredBundles[i]);
	}
	public static void assertEquals(String tag, State original, State copy) {
		BundleDescription[] originalBundles = original.getBundles();
		BundleDescription[] copyBundles = copy.getBundles();
		assertEquals(tag + ".1", originalBundles.length, copyBundles.length);
		for (int i = 0; i < originalBundles.length; i++)
			assertEquals(tag + ".2." + i, originalBundles[i], copyBundles[i]);
		assertEquals(tag + ".3", original.isResolved(), copy.isResolved());
		BundleDescription[] originalResolvedBundles = original.getBundles();
		BundleDescription[] copyResolvedBundles = copy.getBundles();
		assertEquals(tag + ".4", originalResolvedBundles.length, copyResolvedBundles.length);
		for (int i = 0; i < originalResolvedBundles.length; i++)
			assertEquals(tag + ".5." + i, originalResolvedBundles[i], copyResolvedBundles[i]);
	}
	public static void assertEquals(String tag, VersionConstraint original, VersionConstraint copy) {
		assertEquals(tag + ".0", original == null, copy == null);
		if (original == null)
			return;
		assertEquals(tag + ".1", original.getName(), copy.getName());
		assertEquals(tag + ".2", original.getVersionSpecification(), copy.getVersionSpecification());
		assertEquals(tag + ".3", original.getMatchingRule(), copy.getMatchingRule());
		assertEquals(tag + ".4", original.getActualVersion(), copy.getActualVersion());
		assertEquals(tag + ".5", original.getSupplier() == null, copy.getSupplier() == null);
		if (original.getSupplier() != null)
			assertEquals(tag + ".6", original.getSupplier().getUniqueId(), copy.getSupplier().getUniqueId());
	}
	public static void assertFullyResolved(String tag, BundleDescription bundle) {
		assertTrue(tag + "a", bundle.isResolved());
		PackageSpecification[] packages = bundle.getPackages();
		for (int i = 0; i < packages.length; i++)
			assertNotNull(tag + "b_" + i, packages[i].getSupplier());
		HostSpecification host = bundle.getHost();
		if (host != null)
			assertNotNull(tag + "c", host.getSupplier());
		BundleSpecification[] requiredBundles = bundle.getRequiredBundles();
		for (int i = 0; i < requiredBundles.length; i++)
			assertNotNull(tag + "d_" + i, requiredBundles[i].getSupplier());
	}
	public static void assertFullyUnresolved(String tag, BundleDescription bundle) {
		assertFalse(tag + "a", bundle.isResolved());
		PackageSpecification[] packages = bundle.getPackages();
		for (int i = 0; i < packages.length; i++)
			assertNull(tag + "b_" + i, packages[i].getSupplier());
		HostSpecification host = bundle.getHost();
		if (host != null)
			assertNull(tag + "c", host.getSupplier());
		BundleSpecification[] requiredBundles = bundle.getRequiredBundles();
		for (int i = 0; i < requiredBundles.length; i++)
			assertNull(tag + "d_" + i, requiredBundles[i].getSupplier());
	}
	public static void assertIdentical(String tag, State original, State copy) {
		assertEquals(tag + ".0a", original.isResolved(), copy.isResolved());
		assertEquals(tag + ".0b", original.getTimeStamp(), copy.getTimeStamp());
		assertEquals(tag, original, copy);
	}
	public State buildComplexState() throws BundleException {
		State state = buildEmptyState();
		/*
		 * org.eclipse.b1_1.0 exports org.eclipse.p1_1.0 imports org.eclipse.p2
		 */
		final String B1_LOCATION = "org.eclipse.b1";
		final String B1_MANIFEST = "Bundle-SymbolicName: org.eclipse.b1\n" + "Bundle-Version: 1.0\n" + "Export-Package: org.eclipse.p1;specification-version=1.0\n" + "Import-Package: org.eclipse.p2";
		BundleDescription b1 = state.getFactory().createBundleDescription(parseManifest(B1_MANIFEST), B1_LOCATION, 1);
		state.addBundle(b1);
		/*
		 * org.eclipse.b2_2.0 exports org.eclipse.p2 imports org.eclipse.p1
		 */
		final String B2_LOCATION = "org.eclipse.b2";
		final String B2_MANIFEST = "Bundle-SymbolicName: org.eclipse.b2\n" + "Bundle-Version: 2.0\n" + "Export-Package: org.eclipse.p2\n" + "Import-Package: org.eclipse.p1";
		BundleDescription b2 = state.getFactory().createBundleDescription(parseManifest(B2_MANIFEST), B2_LOCATION, 2);
		state.addBundle(b2);
		/*
		 * org.eclipse.b3_2.0 exports org.eclipse.p2_2.0
		 */
		final String B3_LOCATION = "org.eclipse.b3";
		final String B3_MANIFEST = "Bundle-SymbolicName: org.eclipse.b3\n" + "Bundle-Version: 2.0\n" + "Export-Package: org.eclipse.p2; specification-version=2.0";
		BundleDescription b3 = state.getFactory().createBundleDescription(parseManifest(B3_MANIFEST), B3_LOCATION, 3);
		state.addBundle(b3);
		/*
		 * org.eclipse.b4_1.0 requires org.eclipse.b1_*
		 */
		final String B4_LOCATION = "org.eclipse.b4";
		final String B4_MANIFEST = "Bundle-SymbolicName: org.eclipse.b4\n" + "Bundle-Version: 2.0\n" + "Require-Bundle: org.eclipse.b1";
		BundleDescription b4 = state.getFactory().createBundleDescription(parseManifest(B4_MANIFEST), B4_LOCATION, 4);
		state.addBundle(b4);
		/*
		 * org.eclipse.b5_1.0 fragment for org.eclipse.b3_*
		 */
		final String B5_LOCATION = "org.eclipse.b5";
		final String B5_MANIFEST = "Bundle-SymbolicName: org.eclipse.b5\n" + "Bundle-Version: 1.0\n" + "Fragment-Host: org.eclipse.b3";
		BundleDescription b5 = state.getFactory().createBundleDescription(parseManifest(B5_MANIFEST), B5_LOCATION, 5);
		state.addBundle(b5);
		/*
		 * org.eclipse.b6_1.0 requires org.eclipse.b4
		 */
		final String B6_LOCATION = "org.eclipse.b6";
		final String B6_MANIFEST = "Bundle-SymbolicName: org.eclipse.b6\n" + "Bundle-Version: 1.0\n" + "Require-Bundle: org.eclipse.b4";
		BundleDescription b6 = state.getFactory().createBundleDescription(parseManifest(B6_MANIFEST), B6_LOCATION, 6);
		state.addBundle(b6);
		return state;
	}
	public State buildEmptyState() {
		State state = platformAdmin.getState();
		state.setResolver(platformAdmin.getResolver());
		return state;
	}
	public State buildInitialState() throws BundleException {
		State state = buildEmptyState();
		/*
		 * org.eclipse.b1_1.0 exports org.eclipse.p1_1.0
		 */
		final String SYSTEM_BUNDLE_LOCATION = "org.eclipse.b1";
		final String SYSTEM_BUNDLE_MANIFEST = "Bundle-SymbolicName: org.osgi.framework\n" + "Bundle-Version: 3.0\n" + "Export-Package: org.osgi.framework; specification-version=3.0";
		BundleDescription b0 = state.getFactory().createBundleDescription(parseManifest(SYSTEM_BUNDLE_MANIFEST), SYSTEM_BUNDLE_LOCATION, 0);
		state.addBundle(b0);
		return state;
	}
	public State buildSimpleState() throws BundleException {
		State state = buildEmptyState();
		/*
		 * org.eclipse.b1_1.0 exports org.eclipse.p1_1.0 imports org.eclipse.p2
		 */
		final String B1_LOCATION = "org.eclipse.b1";
		final String B1_MANIFEST = "Bundle-SymbolicName: org.eclipse.b1\n" + "Bundle-Version: 1.0\n" + "Export-Package: org.eclipse.p1;specification-version=1.0\n" + "Import-Package: org.eclipse.p2";
		BundleDescription b1 = state.getFactory().createBundleDescription(parseManifest(B1_MANIFEST), B1_LOCATION, 1);
		state.addBundle(b1);
		/*
		 * org.eclipse.b2_2.0 exports org.eclipse.p2 imports org.eclipse.p1
		 */
		final String B2_LOCATION = "org.eclipse.b2";
		final String B2_MANIFEST = "Bundle-SymbolicName: org.eclipse.b2\n" + "Bundle-Version: 2.0\n" + "Export-Package: org.eclipse.p2\n" + "Import-Package: org.eclipse.p1";
		BundleDescription b2 = state.getFactory().createBundleDescription(parseManifest(B2_MANIFEST), B2_LOCATION, 2);
		state.addBundle(b2);
		/*
		 * org.eclipse.b3_2.0 imports org.eclipse.p1_2.0
		 */
		final String B3_LOCATION = "org.eclipse.b3";
		final String B3_MANIFEST = "Bundle-SymbolicName: org.eclipse.b3\n" + "Bundle-Version: 2.0\n" + "Import-Package: org.eclipse.p1; specification-version=2.0";
		BundleDescription b3 = state.getFactory().createBundleDescription(parseManifest(B3_MANIFEST), B3_LOCATION, 3);
		state.addBundle(b3);
		return state;
	}
	public static Dictionary parseManifest(String manifest) {
		Dictionary entries = new Hashtable();
		StringTokenizer tokenizer = new StringTokenizer(manifest, ":\n");
		while (tokenizer.hasMoreTokens()) {
			String key = tokenizer.nextToken();
			String value = tokenizer.hasMoreTokens() ? tokenizer.nextToken().trim() : "";
			entries.put(key, value);
		}
		return entries;
	}
}