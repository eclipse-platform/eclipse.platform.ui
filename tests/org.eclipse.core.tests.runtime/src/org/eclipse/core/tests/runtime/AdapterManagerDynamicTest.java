/*******************************************************************************
 * Copyright (c) 2008, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime;

import java.io.IOException;
import junit.framework.*;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.tests.harness.BundleTestingHelper;
import org.eclipse.core.tests.internal.registry.WaitingRegistryListener;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

/**
 * Tests reaction of AdapterManager on addition and removal of adapters from
 * the extension registry.
 */
public class AdapterManagerDynamicTest extends TestCase {

	final private static int MAX_TIME_PER_BUNDLE = 10000; // maximum time to wait for bundle event in milliseconds

	// Provided by bundle1; has an extension ID 
	private static final String BUNDLE1_TYPE_ID = "abc.SomethingElseA1";
	// Provided by bundle1; has no extension ID 
	private static final String BUNDLE1_TYPE_NO_ID = "abc.SomethingElseA2";

	// Provided by bundle2; has an extension ID 
	private static final String BUNDLE2_TYPE_ID = "abc.SomethingElseB1";
	// Provided by bundle2; has no extension ID 
	private static final String BUNDLE2_TYPE_NO_ID = "abc.SomethingElseB2";

	private IAdapterManager manager;

	public AdapterManagerDynamicTest(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(AdapterManagerDynamicTest.class);
	}

	public AdapterManagerDynamicTest() {
		super("");
	}

	protected void setUp() throws Exception {
		super.setUp();
		manager = Platform.getAdapterManager();
	}

	protected void tearDown() throws Exception {
		manager = null;
		super.tearDown();
	}

	/**
	 * This test uses waiting listener for synchronization (events from bundle being
	 * installed or un-installed are not propagated right away).
	 */
	public void testDynamicBundles() throws IOException, BundleException {

		// check that adapters not available
		TestAdaptable adaptable = new TestAdaptable();
		assertFalse(manager.hasAdapter(adaptable, BUNDLE1_TYPE_ID));
		assertFalse(manager.hasAdapter(adaptable, BUNDLE1_TYPE_NO_ID));
		assertFalse(manager.hasAdapter(adaptable, BUNDLE2_TYPE_ID));
		assertFalse(manager.hasAdapter(adaptable, BUNDLE2_TYPE_NO_ID));

		Bundle bundle01 = null;
		Bundle bundle02 = null;
		WaitingRegistryListener listener = new WaitingRegistryListener();
		listener.register("org.eclipse.core.runtime.adapters");
		try {
			bundle01 = BundleTestingHelper.installBundle("0.1", RuntimeTestsPlugin.getContext(), RuntimeTestsPlugin.TEST_FILES_ROOT + "adapters/dynamic/A");
			bundle02 = BundleTestingHelper.installBundle("0.2", RuntimeTestsPlugin.getContext(), RuntimeTestsPlugin.TEST_FILES_ROOT + "adapters/dynamic/B");
			BundleTestingHelper.refreshPackages(RuntimeTestsPlugin.getContext(), new Bundle[] {bundle01, bundle02});

			// synchronization: listener should receive 2 groups of events
			assertTrue(listener.waitFor(2, 2 * MAX_TIME_PER_BUNDLE) == 2);

			// now has to have all 4 adapters
			assertTrue(manager.hasAdapter(adaptable, BUNDLE1_TYPE_ID));
			assertTrue(manager.hasAdapter(adaptable, BUNDLE1_TYPE_NO_ID));
			assertTrue(manager.hasAdapter(adaptable, BUNDLE2_TYPE_ID));
			assertTrue(manager.hasAdapter(adaptable, BUNDLE2_TYPE_NO_ID));

			listener.reset();
			bundle02.uninstall();
			bundle02 = null;

			// synchronization: listener should receive 1 group of events
			assertTrue(listener.waitFor(1, MAX_TIME_PER_BUNDLE) == 1);

			// now 2 installed; 2 not
			assertTrue(manager.hasAdapter(adaptable, BUNDLE1_TYPE_ID));
			assertTrue(manager.hasAdapter(adaptable, BUNDLE1_TYPE_NO_ID));
			assertFalse(manager.hasAdapter(adaptable, BUNDLE2_TYPE_ID));
			assertFalse(manager.hasAdapter(adaptable, BUNDLE2_TYPE_NO_ID));

			listener.reset();
			bundle01.uninstall();
			bundle01 = null;

			// synchronization: listener should receive 1 group of events
			assertTrue(listener.waitFor(1, MAX_TIME_PER_BUNDLE) == 1);

			// and all should be uninstalled again
			assertFalse(manager.hasAdapter(adaptable, BUNDLE1_TYPE_ID));
			assertFalse(manager.hasAdapter(adaptable, BUNDLE1_TYPE_NO_ID));
			assertFalse(manager.hasAdapter(adaptable, BUNDLE2_TYPE_ID));
			assertFalse(manager.hasAdapter(adaptable, BUNDLE2_TYPE_NO_ID));

		} finally {
			listener.unregister();
			// in case of exception in the process 
			if (bundle01 != null)
				bundle01.uninstall();
			if (bundle02 != null)
				bundle02.uninstall();
		}
	}

}
