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
package org.eclipse.core.tests.internal.dynamicregistry;

import java.io.IOException;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.tests.harness.BundleTestingHelper;
import org.eclipse.core.tests.runtime.RuntimeTestsPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

import junit.framework.TestCase;

public class ExtensionRegistryStaticTest extends TestCase {
	public void testA() throws IOException, BundleException {
		//test the addition of an extension point
		String name = "A";
		Bundle bundle01 = BundleTestingHelper.installBundle(RuntimeTestsPlugin.getContext(), RuntimeTestsPlugin.TEST_FILES_ROOT + "registry/test" + name);
		BundleTestingHelper.refreshPackages(RuntimeTestsPlugin.getContext(), new Bundle[] {bundle01});
		testExtensionPoint(name);
	}
	
	private void testExtensionPoint(String name) {
		assertNotNull(Platform.getExtensionRegistry().getExtensionPoint("test" + name + ".xpt" + name));
		assertEquals(Platform.getExtensionRegistry().getExtensionPoint("test" + name + ".xpt" + name).getLabel(), "Label xpt" + name);
		assertEquals(Platform.getExtensionRegistry().getExtensionPoint("test" + name + ".xpt" + name).getNamespace(), "test" + name);
		assertEquals(Platform.getExtensionRegistry().getExtensionPoint("test" + name + ".xpt" + name).getSchemaReference(), "schema/xpt" + name + ".exsd");			
	}
	
	public void testB() throws IOException, BundleException {
		//test the addition of an extension without extension point
		Bundle bundle01 = BundleTestingHelper.installBundle(RuntimeTestsPlugin.getContext(), RuntimeTestsPlugin.TEST_FILES_ROOT + "registry/testB");
		BundleTestingHelper.refreshPackages(RuntimeTestsPlugin.getContext(), new Bundle[] {bundle01});
		assertNull(Platform.getExtensionRegistry().getExtension("testB", "xptB", "ext1"));
	}
	
	public void testC() throws IOException, BundleException {
		//test the addition of an extension point then the addition of an extension
		Bundle bundle01 = BundleTestingHelper.installBundle(RuntimeTestsPlugin.getContext(), RuntimeTestsPlugin.TEST_FILES_ROOT + "registry/testC/1");
		BundleTestingHelper.refreshPackages(RuntimeTestsPlugin.getContext(), new Bundle[] {bundle01});
		testExtensionPoint("C1");
		Bundle bundle02 = BundleTestingHelper.installBundle(RuntimeTestsPlugin.getContext(), RuntimeTestsPlugin.TEST_FILES_ROOT + "registry/testC/2");
		BundleTestingHelper.refreshPackages(RuntimeTestsPlugin.getContext(), new Bundle[] {bundle02});
		
		//Test the configurataion elements
		assertEquals(Platform.getExtensionRegistry().getExtension("testC1", "xptC1", "testC2.ext1").getConfigurationElements().length, 0);
		assertEquals(Platform.getExtensionRegistry().getConfigurationElementsFor("testC1.xptC1").length, 0);
		
		//Test the number of extension in the extension point
		assertEquals(Platform.getExtensionRegistry().getExtensions("testC2").length, 1);
		
		//Test the extension
		assertNotNull(Platform.getExtensionRegistry().getExtension("testC2.ext1"));
		assertNotNull(Platform.getExtensionRegistry().getExtension("testC1", "xptC1", "testC2.ext1"));
		assertNotNull(Platform.getExtensionRegistry().getExtension("testC1.xptC1", "testC2.ext1"));
		
		assertNotNull(Platform.getExtensionRegistry().getExtensionPoint("testC1.xptC1").getExtension("testC2.ext1"));
		assertEquals(Platform.getExtensionRegistry().getExtensionPoint("testC1.xptC1").getExtensions()[0].getUniqueIdentifier(), "testC2.ext1");
	}
	
	public void testD() throws IOException, BundleException {
		//test the addition of an extension then the addition of an extension point
		Bundle bundle02 = BundleTestingHelper.installBundle(RuntimeTestsPlugin.getContext(), RuntimeTestsPlugin.TEST_FILES_ROOT + "registry/testD/2");
		BundleTestingHelper.refreshPackages(RuntimeTestsPlugin.getContext(), new Bundle[] {bundle02});
		Bundle bundle01 = BundleTestingHelper.installBundle(RuntimeTestsPlugin.getContext(), RuntimeTestsPlugin.TEST_FILES_ROOT + "registry/testD/1");
		BundleTestingHelper.refreshPackages(RuntimeTestsPlugin.getContext(), new Bundle[] {bundle01});
		testExtensionPoint("D1");

		//Test the configurataion elements
		assertEquals(Platform.getExtensionRegistry().getExtension("testD1", "xptD1", "testD2.ext1").getConfigurationElements().length, 0);
		assertEquals(Platform.getExtensionRegistry().getConfigurationElementsFor("testD1.xptD1").length, 0);
		
		//Test the number of extension in the extension point
		assertEquals(Platform.getExtensionRegistry().getExtensions("testD2").length, 1);
		
		//Test the extension
		assertNotNull(Platform.getExtensionRegistry().getExtension("testD2.ext1"));
		assertNotNull(Platform.getExtensionRegistry().getExtension("testD1", "xptD1", "testD2.ext1"));
		assertNotNull(Platform.getExtensionRegistry().getExtension("testD1.xptD1", "testD2.ext1"));
		
		assertNotNull(Platform.getExtensionRegistry().getExtensionPoint("testD1.xptD1").getExtension("testD2.ext1"));
		assertEquals(Platform.getExtensionRegistry().getExtensionPoint("testD1.xptD1").getExtensions()[0].getUniqueIdentifier(), "testD2.ext1");
	}
	
	public void testE() throws IOException, BundleException {
		//test the addition of an extension point and then add the extension through a fragment
		Bundle bundle01 = BundleTestingHelper.installBundle(RuntimeTestsPlugin.getContext(), RuntimeTestsPlugin.TEST_FILES_ROOT + "registry/testE/1");
		BundleTestingHelper.refreshPackages(RuntimeTestsPlugin.getContext(), new Bundle[] {bundle01});
		Bundle bundle02 = BundleTestingHelper.installBundle(RuntimeTestsPlugin.getContext(), RuntimeTestsPlugin.TEST_FILES_ROOT + "registry/testE/2");
		BundleTestingHelper.refreshPackages(RuntimeTestsPlugin.getContext(), new Bundle[] {bundle02});
		testExtensionPoint("E1");
		
		Platform.getExtensionRegistry().getExtensionPoint("testE1.xptE1").getExtensions()[0].getUniqueIdentifier();
		//Test the configurataion elements
		assertEquals(Platform.getExtensionRegistry().getExtension("testE1", "xptE1", "testE1.ext1").getConfigurationElements().length, 0);
		assertEquals(Platform.getExtensionRegistry().getConfigurationElementsFor("testE1.xptE1").length, 0);
		
		//Test the number of extension in the extension point
		assertEquals(Platform.getExtensionRegistry().getExtensions("testE1").length, 1);
		
		//Test the extension
		assertNotNull(Platform.getExtensionRegistry().getExtension("testE1", "xptE1", "testE1.ext1"));
		assertNotNull(Platform.getExtensionRegistry().getExtension("testE1.xptE1", "testE1.ext1"));
//		assertNotNull(Platform.getExtensionRegistry().getExtension("testE1.ext1"));  //This test exhibits a bug in the 3.0 implementation
		
		assertNotNull(Platform.getExtensionRegistry().getExtensionPoint("testE1.xptE1").getExtension("testE1.ext1"));
		assertEquals(Platform.getExtensionRegistry().getExtensionPoint("testE1.xptE1").getExtensions()[0].getUniqueIdentifier(), "testE1.ext1");
	}
	
	public void testF() throws IOException, BundleException {
		//test the addition of an extension point and then add the extension through a fragment
		Bundle bundle01 = BundleTestingHelper.installBundle(RuntimeTestsPlugin.getContext(), RuntimeTestsPlugin.TEST_FILES_ROOT + "registry/testE/1");
		BundleTestingHelper.refreshPackages(RuntimeTestsPlugin.getContext(), new Bundle[] {bundle01});
		Bundle bundle02 = BundleTestingHelper.installBundle(RuntimeTestsPlugin.getContext(), RuntimeTestsPlugin.TEST_FILES_ROOT + "registry/testE/2");
		BundleTestingHelper.refreshPackages(RuntimeTestsPlugin.getContext(), new Bundle[] {bundle02});
		testExtensionPoint("E1");
		
		Platform.getExtensionRegistry().getExtensionPoint("testE1.xptE1").getExtensions()[0].getUniqueIdentifier();
		//Test the configurataion elements
		assertEquals(Platform.getExtensionRegistry().getExtension("testE1", "xptE1", "testE1.ext1").getConfigurationElements().length, 0);
		assertEquals(Platform.getExtensionRegistry().getConfigurationElementsFor("testE1.xptE1").length, 0);
		
		//Test the number of extension in the extension point
		assertEquals(Platform.getExtensionRegistry().getExtensions("testE1").length, 1);
		
		//Test the extension
//		assertNotNull(Platform.getExtensionRegistry().getExtension("testE1.ext1")); this seems to exhibit a bug in the 3.0 implementation
		assertNotNull(Platform.getExtensionRegistry().getExtension("testE1", "xptE1", "testE1.ext1"));
		assertNotNull(Platform.getExtensionRegistry().getExtension("testE1.xptE1", "testE1.ext1"));
		
		assertNotNull(Platform.getExtensionRegistry().getExtensionPoint("testE1.xptE1").getExtension("testE1.ext1"));
		assertEquals(Platform.getExtensionRegistry().getExtensionPoint("testE1.xptE1").getExtensions()[0].getUniqueIdentifier(), "testE1.ext1");
	}
}
