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
package org.eclipse.core.tests.internal.registrycache;

import java.io.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.registry.*;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;
import org.osgi.util.tracker.ServiceTracker;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class RegistryCacheTest extends EclipseWorkspaceTest {
	private static final String BUNDLE_B_WITH_EXTENSION = "<plugin><extension point=\"bundleA.xp1\"><cfg1 property=\"value\"><cfg11/></cfg1><cfg2/></extension></plugin>";
	private static final String BUNDLE_A_WITH_EXTENSION_POINT = "<plugin><extension-point id=\"xp1\"/></plugin>";
	private static final String BUNDLE_A_WITH_EXTENSION_AND_EXTENSION_POINT = "<plugin><extension-point id=\"xp1\"/><extension point=\"bundleA.xp1\"><cfg1 property=\"value\"><cfg11/></cfg1><cfg2/></extension></plugin>";
	protected ExtensionRegistry registry;

	public RegistryCacheTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		registry = new ExtensionRegistry();
	}

	/**
	 * A single plugin declaring an extension point and an extension to it.
	 */
	public void testSingleBundle() throws Exception {
		String manifest = BUNDLE_A_WITH_EXTENSION_AND_EXTENSION_POINT;
		Namespace bundle = parseManifest("bundleA", manifest);
		registry.add(bundle);
		File cacheFile = getRandomLocation().toFile();
		try {
			new RegistryCacheWriter(cacheFile).saveCache(registry);
			assertTrue("file not created", cacheFile.isFile());
			ExtensionRegistry cached = createRegistryReader(cacheFile).loadCache();
			assertEquals(registry, cached);
		} finally {
			ensureDoesNotExistInFileSystem(cacheFile);
		}
	}

	protected RegistryCacheReader createRegistryReader(File cacheFile) {
		return new RegistryCacheReader(cacheFile, new MultiStatus(Platform.PI_RUNTIME, 0, "", null), false, true);
	}

	/**
	 * Two plugins, one declaring an extension point and the other, an extension.
	 */
	public void testTwoBundles() throws Exception {
		String bundle1Manifest = BUNDLE_A_WITH_EXTENSION_POINT;
		String bundle2Manifest = BUNDLE_B_WITH_EXTENSION;
		Namespace bundle1 = parseManifest("bundleA", bundle1Manifest);
		Namespace bundle2 = parseManifest("bundleB", bundle2Manifest);
		registry.add(bundle1);
		registry.add(bundle2);
		File cacheFile = getRandomLocation().toFile();
		try {
			new RegistryCacheWriter(cacheFile).saveCache(registry);
			assertTrue("file not created", cacheFile.isFile());
			ExtensionRegistry cached = createRegistryReader(cacheFile).loadCache();
			assertEquals(registry, cached);
		} finally {
			ensureDoesNotExistInFileSystem(cacheFile);
		}
	}

	private Namespace parseManifest(String symbolicName, Reader input) throws IOException, SAXException, ParserConfigurationException {
		ExtensionsParser parser = new ExtensionsParser(new MultiStatus(Platform.PI_RUNTIME, 0, "", null));
		ServiceTracker xmlTracker = new ServiceTracker(InternalPlatform.getDefault().getBundleContext(), SAXParserFactory.class.getName(), null);
		xmlTracker.open();
		Namespace result = parser.parseManifest(xmlTracker, new InputSource(input), ExtensionsParser.PLUGIN, "plugin.xml", null);
		result.setUniqueIdentifier(symbolicName);
		return result;
	}

	private Namespace parseManifest(String symbolicName, String manifest) throws IOException, SAXException, ParserConfigurationException {
		return parseManifest(symbolicName, new StringReader(manifest));
	}

	private void assertEquals(ExtensionRegistry reg1, ExtensionRegistry reg2) {
		assertTrue("registry.1", (reg1 == null && reg2 == null) || (reg1 != null && reg2 != null));
		if (reg1 == null)
			return;
		String[] reg1elementIds = reg1.getNamespaces();
		String[] reg2elementIds = reg2.getNamespaces();
		assertEquals("registry.2", reg1elementIds.length, reg2elementIds.length);
		for (int i = 0; i < reg2elementIds.length; i++)
			assertEquals("registry.3" + i, reg1elementIds, reg2elementIds);
		for (int i = 0; i < reg2elementIds.length; i++)
			assertEquals(reg1.getNamespace(reg1elementIds[i]), reg2.getNamespace(reg2elementIds[i]));
	}

	private void assertEquals(Namespace bundle1, Namespace bundle2) {
		//check basic attributes
		assertEquals("bundle.1", bundle1.getName(), bundle2.getName());
		assertEquals("bundle.2", bundle1.getUniqueIdentifier(), bundle2.getUniqueIdentifier());
		assertEquals("bundle.3", bundle1.getHostIdentifier(), bundle2.getHostIdentifier());
		// check extension points
		IExtensionPoint[] bundle1xps = bundle1.getExtensionPoints();
		IExtensionPoint[] bundle2xps = bundle2.getExtensionPoints();
		assertEquals("bundle.5a", bundle1xps.length, bundle2xps.length);
		for (int i = 0; i < bundle2xps.length; i++)
			assertEquals(bundle1xps[i], bundle2xps[i]);
		//check extensions
		IExtension[] bundle1Exts = bundle1.getExtensions();
		IExtension[] bundle2Exts = bundle2.getExtensions();
		assertEquals("bundle.6a", bundle1Exts.length, bundle2Exts.length);
		for (int i = 0; i < bundle2Exts.length; i++)
			assertEquals(bundle1Exts[i], bundle2Exts[i]);
	}

	private void assertEquals(IExtensionPoint xp1, IExtensionPoint xp2) {
		assertEquals("extension point.1", xp1.getLabel(), xp2.getLabel());
		assertEquals("extension point.2", xp1.getSimpleIdentifier(), xp2.getSimpleIdentifier());
		assertEquals("extension point.3", xp1.getSchemaReference(), xp2.getSchemaReference());
		IExtension[] xp1Extensions = xp1.getExtensions();
		IExtension[] xp2Extensions = xp2.getExtensions();
		assertEquals("extension point.4", xp1Extensions.length, xp2Extensions.length);
		for (int i = 0; i < xp2Extensions.length; i++)
			assertEquals(xp1Extensions[i], xp2Extensions[i]);
	}

	private void assertEquals(IExtension ext1, IExtension ext2) {
		Extension originalExtension = (Extension) ext1;
		Extension cachedExtension = (Extension) ext2;
		assertEquals("extension.1", originalExtension.getName(), cachedExtension.getName());
		assertEquals("extension.3", originalExtension.getSimpleIdentifier(), cachedExtension.getSimpleIdentifier());
		assertEquals("extension.4", originalExtension.getExtensionPointIdentifier(), cachedExtension.getExtensionPointIdentifier());
		// Compare subElements
		IConfigurationElement[] originalSubElements = originalExtension.getConfigurationElements();
		IConfigurationElement[] cachedSubElements = cachedExtension.getConfigurationElements();
		int originalLength = originalSubElements == null ? 0 : originalSubElements.length;
		int cachedLength = cachedSubElements == null ? 0 : cachedSubElements.length;
		assertEquals("extension.5", originalLength, cachedLength);
		for (int i = 0; i < originalLength; i++)
			assertEquals((ConfigurationElement) originalSubElements[i], (ConfigurationElement) cachedSubElements[i]);
	}

	private void assertEquals(ConfigurationElement originalConfigurationElement, ConfigurationElement cachedConfigurationElement) {
		assertEquals("config element.1", originalConfigurationElement.getName(), cachedConfigurationElement.getName());
		assertEquals("config element.3", originalConfigurationElement.getValue(), cachedConfigurationElement.getValue());
		// Compare children
		IConfigurationElement[] originalSubElements = originalConfigurationElement.getChildren();
		IConfigurationElement[] cachedSubElements = cachedConfigurationElement.getChildren();
		int originalLength = originalSubElements == null ? 0 : originalSubElements.length;
		int cachedLength = cachedSubElements == null ? 0 : cachedSubElements.length;
		assertEquals("config element.4a", originalLength, cachedLength);
		for (int i = 0; i < originalLength; i++)
			assertEquals((ConfigurationElement) originalSubElements[i], (ConfigurationElement) cachedSubElements[i]);
		// Compare properties
		ConfigurationProperty[] originalProperties = originalConfigurationElement.getProperties();
		ConfigurationProperty[] cachedProperties = cachedConfigurationElement.getProperties();
		originalLength = originalProperties == null ? 0 : originalProperties.length;
		cachedLength = cachedProperties == null ? 0 : cachedProperties.length;
		assertEquals("config element.5a", originalLength, cachedLength);
		for (int i = 0; i < originalLength; i++)
			assertEquals(originalProperties[i], cachedProperties[i]);
	}

	private void assertEquals(ConfigurationProperty originalConfigurationProperty, ConfigurationProperty cachedConfigurationProperty) {
		assertEquals("config property.1", originalConfigurationProperty.getName(), cachedConfigurationProperty.getName());
		assertEquals("config property.3", originalConfigurationProperty.getValue(), cachedConfigurationProperty.getValue());
	}

	public static Test suite() {
		return new TestSuite(RegistryCacheTest.class);
	}
}