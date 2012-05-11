/*******************************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation and others.
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
import org.eclipse.core.tests.harness.BundleTestingHelper;
import org.eclipse.core.tests.runtime.RuntimeTestsPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

public class NamespaceTest extends TestCase {

	public NamespaceTest(String name) {
		super(name);
	}

	public void testNamespaceBasic() throws IOException, BundleException {
		//test the addition of an extension point
		Bundle bundle01 = BundleTestingHelper.installBundle("Plugin", RuntimeTestsPlugin.getContext(), RuntimeTestsPlugin.TEST_FILES_ROOT + "registry/testNamespace/1");
		BundleTestingHelper.refreshPackages(RuntimeTestsPlugin.getContext(), new Bundle[] {bundle01});

		// Extension point and extension should be present 
		IExtensionPoint extpt = Platform.getExtensionRegistry().getExtensionPoint("org.abc.xptNS1");
		assertNotNull(extpt);
		assertTrue(extpt.getNamespaceIdentifier().equals("org.abc"));
		assertTrue(extpt.getContributor().getName().equals("testNamespace1"));
		assertTrue(extpt.getSimpleIdentifier().equals("xptNS1"));
		assertTrue(extpt.getUniqueIdentifier().equals("org.abc.xptNS1"));

		IExtension ext = Platform.getExtensionRegistry().getExtension("org.abc.extNS1");
		assertNotNull(ext);
		assertTrue(ext.getNamespaceIdentifier().equals("org.abc"));
		assertTrue(ext.getContributor().getName().equals("testNamespace1"));
		assertTrue(ext.getSimpleIdentifier().equals("extNS1"));
		assertTrue(ext.getUniqueIdentifier().equals("org.abc.extNS1"));

		// Check linkage extension <-> extension point
		assertTrue(ext.getExtensionPointUniqueIdentifier().equals(extpt.getUniqueIdentifier()));
		IExtension[] extensions = extpt.getExtensions();
		assertTrue(extensions.length == 1);
		assertTrue(extensions[0].equals(ext));

		// Exactly one extension and one extension point in the "org.abc" namespace
		IExtensionPoint[] namespaceExtensionPoints = Platform.getExtensionRegistry().getExtensionPoints("org.abc");
		assertTrue(namespaceExtensionPoints.length == 1);
		assertTrue(namespaceExtensionPoints[0].equals(extpt));
		IExtension[] namespaceExtensions = Platform.getExtensionRegistry().getExtensions("org.abc");
		assertTrue(namespaceExtensions.length == 1);
		assertTrue(namespaceExtensions[0].equals(ext));

		// There should not be extension points or extensions in the default namespace
		IExtensionPoint[] defaultExtensionPoints = Platform.getExtensionRegistry().getExtensionPoints("testNamespace1");
		assertTrue(defaultExtensionPoints.length == 0);
		IExtension[] defaultExtensions = Platform.getExtensionRegistry().getExtensions("testNamespace1");
		assertTrue(defaultExtensions.length == 0);

		// remove the first bundle
		bundle01.uninstall();
		BundleTestingHelper.refreshPackages(RuntimeTestsPlugin.getContext(), new Bundle[] {bundle01});
	}

	public void testNamespaceDynamic() throws BundleException, IOException {

		// add another bundle
		Bundle anotherNamespaceBundle = BundleTestingHelper.installBundle("Plugin", RuntimeTestsPlugin.getContext(), RuntimeTestsPlugin.TEST_FILES_ROOT + "registry/testNamespace/2");
		BundleTestingHelper.refreshPackages(RuntimeTestsPlugin.getContext(), new Bundle[] {anotherNamespaceBundle});

		// all elements from the first bundle should be gone
		IExtensionPoint extpt_removed = Platform.getExtensionRegistry().getExtensionPoint("org.abc.xptNS1");
		assertNull(extpt_removed);
		IExtension ext_removed = Platform.getExtensionRegistry().getExtension("org.abc.extNS1");
		assertNull(ext_removed);

		// all elements from the second bundle should still be present
		IExtensionPoint extpt2 = Platform.getExtensionRegistry().getExtensionPoint("org.abc.xptNS2");
		assertNotNull(extpt2);
		IExtension ext2 = Platform.getExtensionRegistry().getExtension("org.abc.extNS2");
		assertNotNull(ext2);

		// Exactly one extension and one extension point in the "org.abc" namespace
		IExtensionPoint[] namespaceExtensionPoints2 = Platform.getExtensionRegistry().getExtensionPoints("org.abc");
		assertTrue(namespaceExtensionPoints2.length == 1);
		assertTrue(namespaceExtensionPoints2[0].equals(extpt2));
		IExtension[] namespaceExtensions2 = Platform.getExtensionRegistry().getExtensions("org.abc");
		assertTrue(namespaceExtensions2.length == 1);
		assertTrue(namespaceExtensions2[0].equals(ext2));
		
		// remove the second bundle
		anotherNamespaceBundle.uninstall();
		BundleTestingHelper.refreshPackages(RuntimeTestsPlugin.getContext(), new Bundle[] {anotherNamespaceBundle});
	}

	public static Test suite() {
		//Order is important
		TestSuite sameSession = new TestSuite(NamespaceTest.class.getName());
		sameSession.addTest(new NamespaceTest("testNamespaceBasic"));
		sameSession.addTest(new NamespaceTest("testNamespaceDynamic"));
		return sameSession;
	}
}
