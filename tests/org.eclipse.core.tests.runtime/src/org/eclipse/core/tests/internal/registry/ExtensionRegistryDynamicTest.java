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
package org.eclipse.core.tests.internal.registry;

import java.io.IOException;
import junit.framework.*;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.tests.harness.BundleTestingHelper;
import org.eclipse.core.tests.runtime.RuntimeTestsPlugin;
import org.eclipse.core.tests.runtime.TestRegistryChangeListener;
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
			bundle01 = BundleTestingHelper.installBundle(RuntimeTestsPlugin.getContext(), RuntimeTestsPlugin.TEST_FILES_ROOT + "registryEvents/bundle01");
			bundle02 = BundleTestingHelper.installBundle(RuntimeTestsPlugin.getContext(), RuntimeTestsPlugin.TEST_FILES_ROOT + "registryEvents/bundle02");
			BundleTestingHelper.refreshPackages(RuntimeTestsPlugin.getContext(), new Bundle[] {bundle01, bundle02});
			IRegistryChangeEvent event = listener.getEvent(5000);
			assertNotNull("1.0", event);
			IExtensionDelta change = event.getExtensionDelta("bundle01", "xp1", "bundle02.ext1");
			assertNotNull("1.1", change);
			assertEquals("1.2", IExtensionDelta.ADDED, change.getKind());
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
		try {
			bundle01 = BundleTestingHelper.installBundle(RuntimeTestsPlugin.getContext(), RuntimeTestsPlugin.TEST_FILES_ROOT + "registryEvents/bundle01");
			bundle02 = BundleTestingHelper.installBundle(RuntimeTestsPlugin.getContext(), RuntimeTestsPlugin.TEST_FILES_ROOT + "registryEvents/bundle02");
			BundleTestingHelper.refreshPackages(RuntimeTestsPlugin.getContext(), new Bundle[] {bundle01, bundle02});
			listener.register();
			BundleTestingHelper.refreshPackages(RuntimeTestsPlugin.getContext(), new Bundle[] {bundle02});
			IRegistryChangeEvent event = listener.getEvent(5000);
			assertNotNull("1.0", event);
			IExtensionDelta change = event.getExtensionDelta("bundle01", "xp1", "bundle02.ext1");
			assertNotNull("1.1", change);
			assertEquals("1.2", IExtensionDelta.REMOVED, change.getKind());
			event = listener.getEvent(5000);
			assertNotNull("2.0", event);
			change = event.getExtensionDelta("bundle01", "xp1", "bundle02.ext1");
			assertNotNull("2.1", change);
			assertEquals("2.2", IExtensionDelta.ADDED, change.getKind());
		} finally {
			listener.unregister();
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