/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.registry;

import java.io.IOException;
import junit.framework.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.dynamichelpers.*;
import org.eclipse.core.tests.harness.BundleTestingHelper;
import org.eclipse.core.tests.harness.TestRegistryChangeListener;
import org.eclipse.core.tests.runtime.RuntimeTestsPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

public class ExtensionRegistryDynamicTest extends TestCase {

	public ExtensionRegistryDynamicTest() {
		super();
	}

	public ExtensionRegistryDynamicTest(String name) {
		super(name);
	}

	public void testAddition() throws IOException, BundleException {
		Bundle bundle01 = null;
		Bundle bundle02 = null;
		TestRegistryChangeListener listener = new TestRegistryChangeListener("bundle01", "xp1", "bundle02", "ext1");
		listener.register();
		try {
			bundle01 = BundleTestingHelper.installBundle("0.1", RuntimeTestsPlugin.getContext(), RuntimeTestsPlugin.TEST_FILES_ROOT + "registryEvents/bundle01");
			bundle02 = BundleTestingHelper.installBundle("0.2", RuntimeTestsPlugin.getContext(), RuntimeTestsPlugin.TEST_FILES_ROOT + "registryEvents/bundle02");
			BundleTestingHelper.refreshPackages(RuntimeTestsPlugin.getContext(), new Bundle[] {bundle01, bundle02});
			IExtensionRegistry registry = RegistryFactory.getRegistry();
			IExtensionPoint extPoint = registry.getExtensionPoint("bundle01.xp1");
			IExtension[] extensions = extPoint.getExtensions();
			assertEquals("0.9", extensions.length, 1);

			assertEquals("1.2", IExtensionDelta.ADDED, listener.eventTypeReceived(20000));
		} finally {
			listener.unregister();
			if (bundle01 != null)
				bundle01.uninstall();
			if (bundle02 != null)
				bundle02.uninstall();
		}
	}

	/**
	 * @see bug 65783
	 */
	public void testReresolving() throws IOException, BundleException {
		Bundle bundle01 = null;
		Bundle bundle02 = null;
		TestRegistryChangeListener listener = new TestRegistryChangeListener("bundle01", "xp1", "bundle02", "ext1");
		listener.register();
		try {
			bundle01 = BundleTestingHelper.installBundle("0.1", RuntimeTestsPlugin.getContext(), RuntimeTestsPlugin.TEST_FILES_ROOT + "registryEvents/bundle01");
			bundle02 = BundleTestingHelper.installBundle("0.2", RuntimeTestsPlugin.getContext(), RuntimeTestsPlugin.TEST_FILES_ROOT + "registryEvents/bundle02");
			BundleTestingHelper.refreshPackages(RuntimeTestsPlugin.getContext(), new Bundle[] {bundle01, bundle02});
			assertEquals("0.5", IExtensionDelta.ADDED, listener.eventTypeReceived(20000));
			BundleTestingHelper.refreshPackages(RuntimeTestsPlugin.getContext(), new Bundle[] {bundle02});
			assertEquals("1.2", IExtensionDelta.REMOVED, listener.eventTypeReceived(10000));
			assertEquals("2.2", IExtensionDelta.ADDED, listener.eventTypeReceived(10000));
		} finally {
			listener.unregister();
			if (bundle01 != null)
				bundle01.uninstall();
			if (bundle02 != null)
				bundle02.uninstall();
		}
	}

	boolean additionCalled = false;
	boolean removalCalled = false;

	/**
	 * @see bug 178028
	 */
	public void testEventTracker() throws IOException, BundleException {
		Bundle bundle01 = null;
		Bundle bundle02 = null;
		TestRegistryChangeListener listener = new TestRegistryChangeListener("bundle01", "xp1", "bundle02", "ext1");
		listener.register();
		TestRegistryChangeListener lastListener = null;
		try {
			bundle01 = BundleTestingHelper.installBundle("0.1", RuntimeTestsPlugin.getContext(), RuntimeTestsPlugin.TEST_FILES_ROOT + "registryEvents/bundle01");
			bundle02 = BundleTestingHelper.installBundle("0.2", RuntimeTestsPlugin.getContext(), RuntimeTestsPlugin.TEST_FILES_ROOT + "registryEvents/bundle02");
			BundleTestingHelper.refreshPackages(RuntimeTestsPlugin.getContext(), new Bundle[] {bundle01, bundle02});
			assertEquals("0.5", IExtensionDelta.ADDED, listener.eventTypeReceived(20000));

			ExtensionTracker tracker = new ExtensionTracker();
			IExtensionRegistry registry = RegistryFactory.getRegistry();
			IExtensionPoint extPoint = registry.getExtensionPoint("bundle01.xp1");

			// reset state variables
			additionCalled = false;
			removalCalled = false;

			tracker.registerHandler(new IExtensionChangeHandler() {

				public void addExtension(IExtensionTracker currentTracker, IExtension extension) {
					additionCalled = true;
				}

				public void removeExtension(IExtension extension, Object[] objects) {
					removalCalled = true;
				}
			}, ExtensionTracker.createExtensionPointFilter(extPoint));

			lastListener = new TestRegistryChangeListener("bundle01", "xp1", "bundle02", "ext1");
			// this relies on implementation details: listeners are called in the order they are registered
			lastListener.register();

			bundle02.uninstall();
			bundle02 = null;

			// make sure that all listener processed by synching on the last added listener
			assertEquals("3.0", IExtensionDelta.REMOVED, lastListener.eventTypeReceived(20000));

			assertFalse(additionCalled);
			assertTrue(removalCalled);
		} finally {
			listener.unregister();
			if (lastListener != null)
				lastListener.unregister();
			if (bundle01 != null)
				bundle01.uninstall();
			if (bundle02 != null)
				bundle02.uninstall();
		}
	}

	public static Test suite() {
		return new TestSuite(ExtensionRegistryDynamicTest.class);
	}

}
