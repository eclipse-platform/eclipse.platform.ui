/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.core.tests.internal.registrycache;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.model.*;
import org.eclipse.core.internal.plugins.*;
import org.eclipse.core.internal.runtime.InternalPlatform;import org.eclipse.core.tests.harness.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import org.xml.sax.*;

public class CacheHelpers extends EclipseWorkspaceTest {
	public static final String DEFAULT_LOCATION = InternalPlatform.getMetaArea().getRegistryPath().toOSString();

public CacheHelpers() {
	super();
}

public CacheHelpers(String name) {
	super(name);
}
public void compareConfigurationElements(ConfigurationElementModel originalConfigurationElement, ConfigurationElementModel cachedConfigurationElement) {
	assertEquals("8.0 Confuguration element name", originalConfigurationElement.getName(), cachedConfigurationElement.getName());
	assertTrue("8.1 Confuguration element read only flag", originalConfigurationElement.isReadOnly() == cachedConfigurationElement.isReadOnly());
	assertEquals("8.2 Confuguration element value", originalConfigurationElement.getValue(), cachedConfigurationElement.getValue());

	// Parent is either another configuration element or an extension.  How
	// should I ensure the parents are identical?

	// Compare children
	ConfigurationElementModel[] originalSubElements = originalConfigurationElement.getSubElements();
	ConfigurationElementModel[] cachedSubElements = cachedConfigurationElement.getSubElements();
	int originalLength = originalSubElements == null ? 0 : originalSubElements.length;
	int cachedLength = cachedSubElements == null ? 0 : cachedSubElements.length;
	assertEquals("8.5 SubElements length", originalLength, cachedLength);
	for (int i = 0; i < originalLength; i++) {
		compareConfigurationElements(originalSubElements[i], cachedSubElements[i]);
	}

	// Compare properties
	ConfigurationPropertyModel[] originalProperties = originalConfigurationElement.getProperties();
	ConfigurationPropertyModel[] cachedProperties = cachedConfigurationElement.getProperties();
	originalLength = originalProperties == null ? 0 : originalProperties.length;
	cachedLength = cachedProperties == null ? 0 : cachedProperties.length;
	assertEquals("8.6 Properties length", originalLength, cachedLength);
	for (int i = 0; i < originalLength; i++) {
		compareConfigurationProperties(originalProperties[i], cachedProperties[i]);
	}
}
public void compareConfigurationProperties(ConfigurationPropertyModel originalConfigurationProperty, ConfigurationPropertyModel cachedConfigurationProperty) {
	assertEquals("9.0 Confuguration property name", originalConfigurationProperty.getName(), cachedConfigurationProperty.getName());
	assertTrue("9.1 Confuguration property read only flag", originalConfigurationProperty.isReadOnly() == cachedConfigurationProperty.isReadOnly());
	assertEquals("9.2 Confuguration property value", originalConfigurationProperty.getValue(), cachedConfigurationProperty.getValue());
}
public void compareExtensionPoints(ExtensionPointModel originalExtensionPoint, ExtensionPointModel cachedExtensionPoint) {
	assertEquals("6.0 Extension Point name", originalExtensionPoint.getName(), cachedExtensionPoint.getName());
	assertTrue("6.1 Extension Point read only flag", originalExtensionPoint.isReadOnly() == cachedExtensionPoint.isReadOnly());
	assertEquals("6.2 Extension Point id", originalExtensionPoint.getId(), cachedExtensionPoint.getId());
	assertEquals("6.3 Extension Point schema", originalExtensionPoint.getSchema(), cachedExtensionPoint.getSchema());

	// Make sure the parents are the same by comparing their id and version.
	// The actual compare of the parent (plugin or fragment) will happen at a different level.
	PluginModel originalParent = originalExtensionPoint.getParent();
	PluginModel cachedParent = cachedExtensionPoint.getParent();
	assertEquals("6.4 Parent id", originalParent.getId(), cachedParent.getId());
	assertEquals("6.5 Parent version", originalParent.getVersion(), cachedParent.getVersion());

	// Compare extension name and ids only.  The extension comparison will happen
	// at a different level
	ExtensionModel[] originalExtensions = originalExtensionPoint.getDeclaredExtensions();
	ExtensionModel[] cachedExtensions = cachedExtensionPoint.getDeclaredExtensions();
	int originalLength = originalExtensions == null ? 0 : originalExtensions.length;
	int cachedLength = cachedExtensions == null ? 0 : cachedExtensions.length;
	assertEquals("6.6 Extensions length", originalLength, cachedLength);
	for (int i = 0; i < originalLength; i++) {
		assertEquals("6.6." + i + " Extension name", originalExtensions[i].getName(), cachedExtensions[i].getName());
		assertEquals("6.7." + i + " Extension id", originalExtensions[i].getId(), cachedExtensions[i].getId());
	}
}
public void compareExtensions(ExtensionModel originalExtension, ExtensionModel cachedExtension) {
	assertEquals("7.0 Extension name", originalExtension.getName(), cachedExtension.getName());
	assertTrue("7.1 Extension read only flag", originalExtension.isReadOnly() == cachedExtension.isReadOnly());
	assertEquals("7.2 Extension id", originalExtension.getId(), cachedExtension.getId());
	assertEquals("7.3 Extension point", originalExtension.getExtensionPoint(), cachedExtension.getExtensionPoint());

	// Make sure the parents are the same by comparing their id and version.
	// The actual compare of the parents will happen at a different level.
	PluginModel originalParent = originalExtension.getParent();
	PluginModel cachedParent = cachedExtension.getParent();
	assertEquals("7.4 Parent id", originalParent.getId(), cachedParent.getId());
	assertEquals("7.5 Parent version", originalParent.getVersion(), cachedParent.getVersion());

	// Compare subElements
	ConfigurationElementModel[] originalSubElements = originalExtension.getSubElements();
	ConfigurationElementModel[] cachedSubElements = cachedExtension.getSubElements();
	int originalLength = originalSubElements == null ? 0 : originalSubElements.length;
	int cachedLength = cachedSubElements == null ? 0 : cachedSubElements.length;
	assertEquals("7.6 SubElements length", originalLength, cachedLength);
	for (int i = 0; i < originalLength; i++) {
		compareConfigurationElements(originalSubElements[i], cachedSubElements[i]);
	}
}
public void compareFragments(PluginFragmentModel originalFragment, PluginFragmentModel cachedFragment) {

	assertEquals("8.0 Fragment name", originalFragment.getName(), cachedFragment.getName());
	assertTrue("8.1 Fragment read only flag", originalFragment.isReadOnly() == cachedFragment.isReadOnly());
	assertEquals("8.2 Fragment id", originalFragment.getId(), cachedFragment.getId());
	assertEquals("8.3 Fragment provider name", originalFragment.getProviderName(), cachedFragment.getProviderName());
	assertEquals("8.4 Fragment version", originalFragment.getVersion(), cachedFragment.getVersion());
	assertEquals("8.5 Fragment location", originalFragment.getLocation(), cachedFragment.getLocation());

	int originalLength, cachedLength = 0;

	// Compare Prerequisites
	PluginPrerequisiteModel[] originalRequires = originalFragment.getRequires();
	PluginPrerequisiteModel[] cachedRequires = cachedFragment.getRequires();
	originalLength = originalRequires == null ? 0 : originalRequires.length;
	cachedLength = cachedRequires == null ? 0 : cachedRequires.length;
	assertEquals("8.6 Requires list", originalLength, cachedLength);
	for (int i = 0; i < originalLength; i++) {
		compareRequires(originalRequires[i], cachedRequires[i]);
	}

	// Compare Libraries
	LibraryModel[] originalLibrary = originalFragment.getRuntime();
	LibraryModel[] cachedLibrary = cachedFragment.getRuntime();
	originalLength = originalLibrary == null ? 0 : originalLibrary.length;
	cachedLength = cachedLibrary == null ? 0 : cachedLibrary.length;
	assertEquals("8.7 Library list", originalLength, cachedLength);
	for (int i = 0; i < originalLength; i++) {
		compareLibraries(originalLibrary[i], cachedLibrary[i]);
	}

	// Compare Extensions
	ExtensionModel[] originalExtensions = originalFragment.getDeclaredExtensions();
	ExtensionModel[] cachedExtensions = cachedFragment.getDeclaredExtensions();
	originalLength = originalExtensions == null ? 0 : originalExtensions.length;
	cachedLength = cachedExtensions == null ? 0 : cachedExtensions.length;
	assertEquals("8.8 Extension list", originalLength, cachedLength);
	for (int i = 0; i < originalLength; i++) {
		compareExtensions(originalExtensions[i], cachedExtensions[i]);
	}

	// Compare Extension Points
	ExtensionPointModel[] originalExtensionPoints = originalFragment.getDeclaredExtensionPoints();
	ExtensionPointModel[] cachedExtensionPoints = cachedFragment.getDeclaredExtensionPoints();
	originalLength = originalExtensionPoints == null ? 0 : originalExtensionPoints.length;
	cachedLength = cachedExtensionPoints == null ? 0 : cachedExtensionPoints.length;
	assertEquals("8.9 Extension Point list", originalLength, cachedLength);
	for (int i = 0; i < originalLength; i++) {
		compareExtensionPoints(originalExtensionPoints[i], cachedExtensionPoints[i]);
	}
}
public void compareLibraries(LibraryModel originalLibrary, LibraryModel cachedLibrary) {
	assertEquals("5.0 Library name", originalLibrary.getName(), cachedLibrary.getName());
	assertTrue("5.1 Library read only flag", originalLibrary.isReadOnly() == cachedLibrary.isReadOnly());
	assertTrue("5.2 Library isExported flag", originalLibrary.isExported() == cachedLibrary.isExported());
	assertTrue("5.3 Library isFullyExported flag", originalLibrary.isFullyExported() == cachedLibrary.isFullyExported());
	assertEquals("5.4 Library type", originalLibrary.getType(), cachedLibrary.getType());
	String[] originalExports = originalLibrary.getExports();
	String[] cachedExports = cachedLibrary.getExports();
	int originalLength = originalExports == null ? 0 : originalExports.length;
	int cachedLength = cachedExports == null ? 0 : cachedExports.length;
	assertEquals("5.5 Library Exports length", originalLength, cachedLength);
	for (int i = 0; i < originalLength; i++) {
		assertEquals("5.5." + i + " Library export string", originalExports[i], cachedExports[i]);
	}
}
public void comparePlugins(PluginDescriptorModel originalPlugin, PluginDescriptorModel cachedPlugin) {

	assertEquals("3.0 Plugin name", originalPlugin.getName(), cachedPlugin.getName());
	assertTrue("3.1 Plugin read only flag", originalPlugin.isReadOnly() == cachedPlugin.isReadOnly());
	assertEquals("3.2 Plugin id", originalPlugin.getId(), cachedPlugin.getId());
	assertEquals("3.3 Plugin provider name", originalPlugin.getProviderName(), cachedPlugin.getProviderName());
	assertEquals("3.4 Plugin version", originalPlugin.getVersion(), cachedPlugin.getVersion());
	assertEquals("3.5 Plugin class", originalPlugin.getPluginClass(), cachedPlugin.getPluginClass());
	assertEquals("3.6 Plugin location", originalPlugin.getLocation(), cachedPlugin.getLocation());
	assertTrue("3.7 Plugin enabled", originalPlugin.getEnabled() == cachedPlugin.getEnabled());

	int originalLength, cachedLength = 0;

	// Compare Prerequisites
	PluginPrerequisiteModel[] originalRequires = originalPlugin.getRequires();
	PluginPrerequisiteModel[] cachedRequires = cachedPlugin.getRequires();
	originalLength = originalRequires == null ? 0 : originalRequires.length;
	cachedLength = cachedRequires == null ? 0 : cachedRequires.length;
	assertEquals("3.8 Requires list", originalLength, cachedLength);
	for (int i = 0; i < originalLength; i++) {
		compareRequires(originalRequires[i], cachedRequires[i]);
	}

	// Compare Libraries
	LibraryModel[] originalLibrary = originalPlugin.getRuntime();
	LibraryModel[] cachedLibrary = cachedPlugin.getRuntime();
	originalLength = originalLibrary == null ? 0 : originalLibrary.length;
	cachedLength = cachedLibrary == null ? 0 : cachedLibrary.length;
	assertEquals("3.9 Library list", originalLength, cachedLength);
	for (int i = 0; i < originalLength; i++) {
		compareLibraries(originalLibrary[i], cachedLibrary[i]);
	}

	// Compare Extensions
	ExtensionModel[] originalExtensions = originalPlugin.getDeclaredExtensions();
	ExtensionModel[] cachedExtensions = cachedPlugin.getDeclaredExtensions();
	originalLength = originalExtensions == null ? 0 : originalExtensions.length;
	cachedLength = cachedExtensions == null ? 0 : cachedExtensions.length;
	assertEquals("3.10 Extension list", originalLength, cachedLength);
	for (int i = 0; i < originalLength; i++) {
		compareExtensions(originalExtensions[i], cachedExtensions[i]);
	}

	// Compare Extension Points
	ExtensionPointModel[] originalExtensionPoints = originalPlugin.getDeclaredExtensionPoints();
	ExtensionPointModel[] cachedExtensionPoints = cachedPlugin.getDeclaredExtensionPoints();
	originalLength = originalExtensionPoints == null ? 0 : originalExtensionPoints.length;
	cachedLength = cachedExtensionPoints == null ? 0 : cachedExtensionPoints.length;
	assertEquals("3.11 Extension Point list", originalLength, cachedLength);
	for (int i = 0; i < originalLength; i++) {
		compareExtensionPoints(originalExtensionPoints[i], cachedExtensionPoints[i]);
	}
	
	// Compare fragments
	PluginFragmentModel[] originalFragments = originalPlugin.getFragments();
	PluginFragmentModel[] cachedFragments = cachedPlugin.getFragments();
	originalLength = originalFragments == null ? 0 : originalFragments.length;
	cachedLength = cachedFragments == null ? 0 : cachedFragments.length;
	assertEquals("3.12 Fragment list", originalLength, cachedLength);
	for (int i = 0; i < originalLength; i++) {
		compareFragments(originalFragments[i], cachedFragments[i]);
	}
}
public void compareRegistries(PluginRegistryModel originalRegistry, PluginRegistryModel cachedRegistry) {
	assertTrue("2.0 Read only flags", originalRegistry.isReadOnly() == cachedRegistry.isReadOnly());
	assertTrue("2.1 Registry resolved", originalRegistry.isResolved() == cachedRegistry.isResolved());

	PluginDescriptorModel[] originalPlugins = originalRegistry.getPlugins();
	PluginDescriptorModel[] cachedPlugins = cachedRegistry.getPlugins();
	int originalLength = originalPlugins == null ? 0 : originalPlugins.length;
	int cachedLength = cachedPlugins == null ? 0 : cachedPlugins.length;

	assertEquals("2.3 Different number of plugins in registries", originalLength, cachedLength);

	// Note that the plugins are located in a hash map instead of a linear list
	// of some form.  As a result, they are not guarenteed to be in the same order
	// in both registries.  Therefore, ensure there are the same number of plugins
	// in both the original and the cached registries.  Then, for each plugin in
	// the original registry, find its mate in the cached registry (using version
	// and id) and then compare these.  If a mate can't be found, fail.
	for (int i = 0; i < originalLength; i++) {
		// find the mate in the cache
		PluginDescriptorModel matePlugin = cachedRegistry.getPlugin(originalPlugins[i].getId(), originalPlugins[i].getVersion());
		assertNotNull("2.4 Can't find plugin in cache " + originalPlugins[i].getId() + "/" + originalPlugins[i].getVersion(), matePlugin);
		comparePlugins(originalPlugins[i], matePlugin);
	}
	
	// Check to make sure we have the same number of fragments
	PluginFragmentModel[] originalFragments = originalRegistry.getFragments();
	PluginFragmentModel[] cachedFragments = cachedRegistry.getFragments();
	originalLength = originalFragments == null ? 0 : originalFragments.length;
	cachedLength = cachedFragments == null ? 0 : cachedFragments.length;

	assertEquals("2.5 Different number of fragments in registries", originalLength, cachedLength);

	// The same ordering problem we have for plugins is true of fragments
	for (int i = 0; i < originalLength; i++) {
		// find the mate in the cache
		PluginFragmentModel mateFragment = cachedRegistry.getFragment(originalFragments[i].getId(), originalFragments[i].getVersion());
		assertNotNull("2.6 Can't find fragment in cache " + originalFragments[i].getId() + "/" + originalFragments[i].getVersion(), mateFragment);
		compareFragments(originalFragments[i], mateFragment);
	}
}
public void compareRequires(PluginPrerequisiteModel originalPrerequisite, PluginPrerequisiteModel cachedPrerequisite) {
	assertEquals("4.0 Requires name", originalPrerequisite.getName(), cachedPrerequisite.getName());
	assertTrue("4.1 Requires read only flag", originalPrerequisite.isReadOnly() == cachedPrerequisite.isReadOnly());
	assertEquals("4.2 Requires version", originalPrerequisite.getVersion(), cachedPrerequisite.getVersion());
	assertEquals("4.3 Requires resolved version", originalPrerequisite.getResolvedVersion(), cachedPrerequisite.getResolvedVersion());
	assertTrue("4.4 Requires match flag", originalPrerequisite.getMatchByte() == cachedPrerequisite.getMatchByte());
	assertTrue("4.5 Requires export flag", originalPrerequisite.getExport() == cachedPrerequisite.getExport());
	assertEquals("4.6 Requires plugin", originalPrerequisite.getPlugin(), cachedPrerequisite.getPlugin());
	assertEquals("4.7 Requires optional", originalPrerequisite.getOptional(), cachedPrerequisite.getOptional());
}
public PluginRegistryModel doCacheWriteAndRead(PluginRegistryModel inRegistry, Factory factory) {
	File cacheFile = new File(DEFAULT_LOCATION);
	DataOutputStream output = null;
	try {
		output = new DataOutputStream(new FileOutputStream(cacheFile));
	} catch (IOException ioe) {
		fail("1.0 IOException encountered", ioe);
	}
	doCacheWrite(inRegistry, output, factory);
	DataInputStream input = null;
	try {
		input = new DataInputStream(new FileInputStream(cacheFile));
	} catch (IOException ioe) {
		fail("2.0 IOException encountered", ioe);
	}
	return doCacheRead(inRegistry, input, factory);
}
public PluginRegistryModel doCacheRead(PluginRegistryModel inRegistry, DataInputStream input, Factory factory) {
	// Cobble together a plugin path
	Map regIndex = InternalPlatform.getRegIndex();
	URL[] pluginPath = null;
	if (regIndex != null) {
		int entrySize = regIndex.keySet().size();
		pluginPath = new URL[entrySize];
		int i = 0;
		for (Iterator list = regIndex.keySet().iterator(); list.hasNext();) {
			String fileName = (String)list.next();
			fileName = "file:" + fileName;
			try {
				pluginPath[i++] = new URL(fileName);
			} catch (MalformedURLException badURL) {
				assertTrue("2.1 Bad url found for " + fileName + ".", true);
			}
		}
	}
	RegistryCacheReader cacheReader = new RegistryCacheReader(factory);
	PluginRegistryModel newRegistry = cacheReader.readPluginRegistry(input, pluginPath, false);
	return newRegistry;
}
public void doCacheWrite(PluginRegistryModel inRegistry, DataOutputStream output, Factory factory) {
	// write the registry to the cache
	RegistryCacheWriter cacheWriter = new RegistryCacheWriter();
	cacheWriter.writePluginRegistry(inRegistry, output);
}
/* doInitialParsing
 * This method will parse a series of XML files.  The input array should be
 * an array of string buffers where each string buffer is considered a complete 
 * XML file.  All of the resulting plugins will be put into a registry
 * which is returned.  This registry will be resolved if the boolean parameter
 * doResolve is set to true. 
 */
public PluginRegistryModel doInitialParsing(InternalFactory factory, String[] localXMLFiles, boolean doResolve) {

	PluginRegistry registry = new PluginRegistry();
	if (localXMLFiles == null || localXMLFiles.length == 0)
		return null;

	for (int i = 0; i < localXMLFiles.length; i++) {
		String thisXMLString = localXMLFiles[i];
		PluginModel localPlugin = null;
		try {
			ByteArrayInputStream is = new ByteArrayInputStream(thisXMLString.toString().getBytes());
			PluginParser p = new PluginParser(factory);
			localPlugin = p.parsePlugin(new InputSource(is));
		} catch (SAXParseException se) {
			/* exception details logged by parser */
			fail("1.2 SAX Parser exception encountered.", se);
		} catch (Exception e) {
			fail("1.3 ", e);
		}
		if (localPlugin instanceof PluginDescriptorModel) {
			registry.addPlugin((PluginDescriptorModel)localPlugin);
		} else {
			registry.addFragment((PluginFragmentModel)localPlugin);
		}
		localPlugin.setRegistry(registry);
	}
	// Now resolve the registry if asked
	if (doResolve) {
		IStatus resolveStatus = registry.resolve(true, true);
		assertTrue("0.1 Registry resolve problems", resolveStatus.isOK());
	}
	
	return registry;
}
}
