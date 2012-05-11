/*******************************************************************************
 * Copyright (c) 2007, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.registry;

import java.io.*;
import java.net.URL;
import junit.framework.*;
import org.eclipse.core.internal.registry.osgi.RegistryStrategyOSGI;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.runtime.RuntimeTestsPlugin;

/**
 * Test proper clean-up in case registry gets invalid XML contribution.
 * @since 3.4
 */
public class InputErrorTest extends TestCase {

	static private final String DATA_LOCATION = "Plugin_Testing/registry/errorHandling/";

	/**
	 * Use customized registry strategy to both check error processing 
	 * and to remove expected error messages from test log.
	 */
	private class RegistryStrategyLog extends RegistryStrategyOSGI {

		public String msg = null;

		public RegistryStrategyLog(File[] theStorageDir, boolean[] cacheReadOnly, Object key) {
			super(theStorageDir, cacheReadOnly, key);
		}

		public void log(IStatus status) {
			msg = status.getMessage();
		}
	}

	public InputErrorTest() {
		super();
	}

	public InputErrorTest(String name) {
		super(name);
	}

	private InputStream getStream(String location) {
		URL xml = RuntimeTestsPlugin.getContext().getBundle().getEntry(DATA_LOCATION + location);
		assertNotNull(xml);
		try {
			return new BufferedInputStream(xml.openStream());
		} catch (IOException ex) {
			fail("Unable to open input stream to XML");
		}
		return null;
	}

	public void testErrorCleanupPoints() {
		RegistryStrategyLog strategy = new RegistryStrategyLog(null, null, null); // RegistryFactory.createOSGiStrategy(null, null, null);
		IExtensionRegistry localRegistry = RegistryFactory.createRegistry(strategy, null, null);
		IContributor contributor = ContributorFactorySimple.createContributor("testErrorHandling");

		// 1) attempt to add information from mis-formed XML
		InputStream is = getStream("bad/point/plugin.xml");
		assertNotNull(is);
		boolean added = localRegistry.addContribution(is, contributor, false, "test", null, null);
		assertFalse(added);
		IExtensionPoint bundleExtPointA = localRegistry.getExtensionPoint("testErrorHandling.xptErrorTestA");
		assertNull(bundleExtPointA);
		IExtensionPoint bundleExtPointB = localRegistry.getExtensionPoint("testErrorHandling.xptErrorTestB");
		assertNull(bundleExtPointB);

		assertNotNull(strategy.msg);
		strategy.msg = null;

		// 2) add properly formed XML
		is = getStream("good/point/plugin.xml");
		assertNotNull(is);
		added = localRegistry.addContribution(is, contributor, false, "test", null, null);
		assertTrue(added);
		bundleExtPointA = localRegistry.getExtensionPoint("testErrorHandling.xptErrorTestA");
		assertNotNull(bundleExtPointA);
		bundleExtPointB = localRegistry.getExtensionPoint("testErrorHandling.xptErrorTestB");
		assertNotNull(bundleExtPointB);

		assertNull(strategy.msg);
		localRegistry.stop(null);
	}

	public void testErrorCleanupExtensions() {
		RegistryStrategyLog strategy = new RegistryStrategyLog(null, null, null); // RegistryFactory.createOSGiStrategy(null, null, null);
		IExtensionRegistry localRegistry = RegistryFactory.createRegistry(strategy, null, null);
		IContributor contributor = ContributorFactorySimple.createContributor("testErrorHandling");

		// 1) attempt to add information from mis-formed XML
		InputStream is = getStream("bad/extension/plugin.xml");
		assertNotNull(is);
		boolean added = localRegistry.addContribution(is, contributor, false, "test", null, null);
		assertFalse(added);
		IExtensionPoint bundleExtPointA = localRegistry.getExtensionPoint("testErrorHandling.xptErrorTestA");
		assertNull(bundleExtPointA);

		IExtension extensionA = localRegistry.getExtension("testErrorHandling.testExtA");
		assertNull(extensionA);
		IExtension extensionB = localRegistry.getExtension("testErrorHandling.testExtB");
		assertNull(extensionB);
		IExtension extensionC = localRegistry.getExtension("testErrorHandling.testExtC");
		assertNull(extensionC);

		assertNotNull(strategy.msg);
		strategy.msg = null;

		// 2) add properly formed XML
		is = getStream("good/extension/plugin.xml");
		assertNotNull(is);
		added = localRegistry.addContribution(is, contributor, false, "test", null, null);
		assertTrue(added);
		bundleExtPointA = localRegistry.getExtensionPoint("testErrorHandling.xptErrorTestA");
		assertNotNull(bundleExtPointA);

		checkExtension(localRegistry, "testErrorHandling.testExtA", "valueGoodA");
		checkExtension(localRegistry, "testErrorHandling.testExtB", "valueGoodB");
		checkExtension(localRegistry, "testErrorHandling.testExtC", "valueGoodC");

		assertNull(strategy.msg);
		localRegistry.stop(null);
	}

	private void checkExtension(IExtensionRegistry registry, String extID, String expectedValue) {
		IExtension extensionA = registry.getExtension(extID);
		assertNotNull(extensionA);
		IConfigurationElement[] configElements = extensionA.getConfigurationElements();
		assertTrue(configElements.length == 1);
		String value = configElements[0].getAttribute("testAttr");
		assertTrue(expectedValue.equals(value));
	}

	public static Test suite() {
		return new TestSuite(InputErrorTest.class);
	}

}
