/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime.compatibility;

import java.io.IOException;
import junit.framework.*;
import org.eclipse.core.internal.plugins.InternalPlatform;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.eclipse.core.tests.harness.BundleTestingHelper;
import org.eclipse.core.tests.runtime.RuntimeTestsPlugin;
import org.osgi.framework.*;

public class PluginCompatibilityTests extends TestCase {

	public PluginCompatibilityTests(String name) {
		super(name);
	}

	// see bug 59013
	public void testPluginWithNoRuntimeLibrary() throws BundleException, IOException {
		assertNull("0.0", BundleTestingHelper.getBundles(RuntimeTestsPlugin.getContext(), "bundle01", "1.0"));
		BundleTestingHelper.runWithBundles("0.1", new Runnable() {
			public void run() {
				Bundle[] installed = BundleTestingHelper.getBundles(RuntimeTestsPlugin.getContext(), "bundle01", "1.0");
				assertEquals("1.0", 1, installed.length);
				assertEquals("1.0", "bundle01", installed[0].getSymbolicName());
				assertEquals("1.1", new Version("1.0"), new Version((String) installed[0].getHeaders().get(Constants.BUNDLE_VERSION)));
				assertEquals("1.2", Bundle.RESOLVED, installed[0].getState());
				IPluginDescriptor descriptor = InternalPlatform.getPluginRegistry().getPluginDescriptor("bundle01", new PluginVersionIdentifier("1.0"));
				assertNotNull("2.0", descriptor);
				assertNotNull("2.1", descriptor.getRuntimeLibraries());
				// see bug 89845. Changed in 3.1...even bundles with no libraries have "dot"
				// on the classpath
				assertEquals("2.2", 1, descriptor.getRuntimeLibraries().length);
				assertEquals("2.3", ".", descriptor.getRuntimeLibraries()[0].getPath().toString());
			}
		}, RuntimeTestsPlugin.getContext(), new String[] {RuntimeTestsPlugin.TEST_FILES_ROOT + "compatibility/bundle01"}, null);
	}

	public static Test suite() {
		return new TestSuite(PluginCompatibilityTests.class);
	}
}
