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
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.tests.harness.*;
import org.eclipse.core.tests.internal.registrycache.CacheHelpers.*;
import java.io.*;
import junit.framework.*;
import org.xml.sax.*;

public class SimpleCacheTests extends CacheHelpers {
public SimpleCacheTests() {
	super();
}
public SimpleCacheTests(String name) {
	super(name);
}
public String[] extensionSetup() {
	PluginDescriptorModel plugin = new PluginDescriptor();
	plugin.setName("A simple test");
	plugin.setId("org.eclipse.webdav");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");

	Extension[] pluginExtensions = new Extension[3];
	for (int i = 0; i < pluginExtensions.length; i++) {
		pluginExtensions[i] = new Extension();
		pluginExtensions[i].setParentPluginDescriptor(plugin);
	}
	pluginExtensions[0].setName("First Extension");
	pluginExtensions[0].setId("first");
	pluginExtensions[0].setExtensionPoint("First Extension Point");

	pluginExtensions[1].setExtensionPoint("Second Extension Point");

	pluginExtensions[2].setExtensionPoint("Third Extension Point");

	// First group of properties
	ConfigurationProperty[] propertyGroup1 = new ConfigurationProperty[2];
	for (int i = 0; i < propertyGroup1.length; i++) {
		propertyGroup1[i] = new ConfigurationProperty();
	}
	propertyGroup1[0].setName("id");
	propertyGroup1[0].setValue("category_id");
	propertyGroup1[1].setName("name");
	propertyGroup1[1].setValue("category_name");
	// Second group of properties
	ConfigurationProperty[] propertyGroup2 = new ConfigurationProperty[3];
	for (int i = 0; i < propertyGroup2.length; i++) {
		propertyGroup2[i] = new ConfigurationProperty();
	}
	propertyGroup2[0].setName("id");
	propertyGroup2[0].setValue("wizard_id");
	propertyGroup2[1].setName("name");
	propertyGroup2[1].setValue("wizard_name");
	propertyGroup2[2].setName("class");
	propertyGroup2[2].setValue("wizard_class");
	// Third group of properties
	ConfigurationProperty[] propertyGroup3 = new ConfigurationProperty[1];
	for (int i = 0; i < propertyGroup3.length; i++) {
		propertyGroup3[i] = new ConfigurationProperty();
	}
	propertyGroup3[0].setName("class");
	propertyGroup3[0].setValue("a selection class");

	// First subelement group
	ConfigurationElement[] subElementGroup1 = new ConfigurationElement[2];
	for (int i = 0; i < subElementGroup1.length; i++) {
		subElementGroup1[i] = new ConfigurationElement();
		subElementGroup1[i].setParent(pluginExtensions[2]);
	}
	subElementGroup1[0].setName("category");
	subElementGroup1[0].setProperties(propertyGroup1);

	subElementGroup1[1].setName("wizard");
	subElementGroup1[1].setProperties(propertyGroup2);

	// Second subelement group
	ConfigurationElement[] subElementGroup2 = new ConfigurationElement[2];
	for (int i = 0; i < subElementGroup2.length; i++) {
		subElementGroup2[i] = new ConfigurationElement();
		subElementGroup2[i].setParent(subElementGroup1[1]);
	}
	subElementGroup2[0].setName("description");
	subElementGroup2[0].setValue("This is just a description");
	subElementGroup2[1].setName("selection");
	subElementGroup2[1].setProperties(propertyGroup3);

	// Now link them together
	subElementGroup1[1].setSubElements(subElementGroup2);
	pluginExtensions[2].setSubElements(subElementGroup1);
	plugin.setDeclaredExtensions(pluginExtensions);

	ByteArrayOutputStream fs;
	fs = new ByteArrayOutputStream();
	PrintWriter w = new PrintWriter(fs);
	RegistryWriter regWriter = new RegistryWriter();
	regWriter.writePluginDescriptor(plugin, w, 0);
	w.flush();
	w.close();

	String[] localXMLFiles = new String[1];
	localXMLFiles[0] = fs.toString();
	return localXMLFiles;
}
/*  extensionTest
 *  Tests to ensure we cache extension entries correctly.
 */
public void extensionTest() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);

	String[] localXMLFiles = extensionSetup();
	// Don't resolve this registry as we don't define all the extension
	// points.
	PluginRegistryModel registry = doInitialParsing(factory, localXMLFiles, false);

	// write to a cache file and read it back in
	PluginRegistryModel cachedRegistry = doCacheWriteAndRead(registry, factory);

	// Now compare the registries.  They should be the same
	compareRegistries(registry, cachedRegistry);

	registry = cachedRegistry = null;
	factory = null;
}
public String[] extExtPtLinkSetup() {
	PluginDescriptor[] pluginList = new PluginDescriptor[2];
	PluginDescriptor plugin = new PluginDescriptor();
	plugin.setName("A simple test");
	plugin.setId("abc1");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");

	Extension[] pluginExtensions = new Extension[4];
	for (int i = 0; i < pluginExtensions.length; i++) {
		pluginExtensions[i] = new Extension();
		pluginExtensions[i].setParentPluginDescriptor(plugin);
	}
	pluginExtensions[0].setName("First Extension");
	pluginExtensions[0].setId("first");
	pluginExtensions[0].setExtensionPoint("abc1.firstExtPt");
	pluginExtensions[1].setName("Second Extension");
	pluginExtensions[1].setId("second");
	pluginExtensions[1].setExtensionPoint("abc1.secondExtPt");
	pluginExtensions[2].setName("Third Extension");
	pluginExtensions[2].setId("third");
	pluginExtensions[2].setExtensionPoint("abc1.thirdExtPt");
	pluginExtensions[3].setName("Fourth Extension");
	pluginExtensions[3].setId("fourth");
	pluginExtensions[3].setExtensionPoint("abc2.anotherExtPt");
	plugin.setDeclaredExtensions(pluginExtensions);

	ExtensionPoint[] pluginExtensionPoints = new ExtensionPoint[3];
	for (int i = 0; i < pluginExtensionPoints.length; i++) {
		pluginExtensionPoints[i] = new ExtensionPoint();
	}
	pluginExtensionPoints[0].setName("First Extension Point");
	pluginExtensionPoints[0].setId("firstExtPt");
	pluginExtensionPoints[1].setName("Second Extension Point");
	pluginExtensionPoints[1].setId("secondExtPt");
	pluginExtensionPoints[2].setName("Third Extension Point");
	pluginExtensionPoints[2].setId("thirdExtPt");
	plugin.setDeclaredExtensionPoints(pluginExtensionPoints);

	pluginList[0] = plugin;

	plugin = new PluginDescriptor();
	plugin.setName("A simple test2");
	plugin.setId("abc2");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");
	pluginExtensionPoints = new ExtensionPoint[1];
	for (int i = 0; i < pluginExtensionPoints.length; i++) {
		pluginExtensionPoints[i] = new ExtensionPoint();
	}
	pluginExtensionPoints[0].setName("Another Extension Point");
	pluginExtensionPoints[0].setId("anotherExtPt");
	plugin.setDeclaredExtensionPoints(pluginExtensionPoints);

	pluginList[1] = plugin;

	ByteArrayOutputStream fs;
	String[] localXMLFiles = new String[2];
	for (int i = 0; i < pluginList.length; i++) {
		fs = new ByteArrayOutputStream();
		PrintWriter w = new PrintWriter(fs);
		RegistryWriter regWriter = new RegistryWriter();
		regWriter.writePluginDescriptor(pluginList[i], w, 0);
		w.flush();
		w.close();

		localXMLFiles[i] = fs.toString();
	}
	return localXMLFiles;
}
/*  extExtPtLinkTest
 *  Tests to ensure we cache cross links for extensions and extension
 *  points correctly.
 */
public void extExtPtLinkTest() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);

	String[] localXMLFiles = extExtPtLinkSetup();
	PluginRegistryModel registry = doInitialParsing(factory, localXMLFiles, true);

	// write to a cache file and read it back in
	PluginRegistryModel cachedRegistry = doCacheWriteAndRead(registry, factory);

	// Now compare the registries.  They should be the same
	compareRegistries(registry, cachedRegistry);

	registry = cachedRegistry = null;
	factory = null;
}
public String[] fragmentExtensionSetup() {
	PluginDescriptorModel plugin = new PluginDescriptor();
	plugin.setName("A simple test");
	plugin.setId("org.eclipse.webdav");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");
	
	PluginFragmentModel fragment = new FragmentDescriptor();
	fragment.setName("fragment name");
	fragment.setPlugin("org.eclipse.webdav");
	fragment.setPluginVersion("1.0");
	fragment.setVersion("2.0");
	fragment.setId("fragmentId");
	
	// XXX Remove this once the issue of extensions have fragments
	// as parents is solved
	PluginFragmentModel[] fragmentList = new PluginFragmentModel[1];
	fragmentList[0] = fragment;
	plugin.setFragments(fragmentList);
	
	Extension[] pluginExtensions = new Extension[3];
	for (int i = 0; i < pluginExtensions.length; i++) {
		pluginExtensions[i] = new Extension();
		pluginExtensions[i].setParentPluginDescriptor(plugin);
	}
	pluginExtensions[0].setName("First Extension");
	pluginExtensions[0].setId("first");
	pluginExtensions[0].setExtensionPoint("First Extension Point");

	pluginExtensions[1].setExtensionPoint("Second Extension Point");

	pluginExtensions[2].setExtensionPoint("Third Extension Point");

	// First group of properties
	ConfigurationProperty[] propertyGroup1 = new ConfigurationProperty[2];
	for (int i = 0; i < propertyGroup1.length; i++) {
		propertyGroup1[i] = new ConfigurationProperty();
	}
	propertyGroup1[0].setName("id");
	propertyGroup1[0].setValue("category_id");
	propertyGroup1[1].setName("name");
	propertyGroup1[1].setValue("category_name");
	// Second group of properties
	ConfigurationProperty[] propertyGroup2 = new ConfigurationProperty[3];
	for (int i = 0; i < propertyGroup2.length; i++) {
		propertyGroup2[i] = new ConfigurationProperty();
	}
	propertyGroup2[0].setName("id");
	propertyGroup2[0].setValue("wizard_id");
	propertyGroup2[1].setName("name");
	propertyGroup2[1].setValue("wizard_name");
	propertyGroup2[2].setName("class");
	propertyGroup2[2].setValue("wizard_class");
	// Third group of properties
	ConfigurationProperty[] propertyGroup3 = new ConfigurationProperty[1];
	for (int i = 0; i < propertyGroup3.length; i++) {
		propertyGroup3[i] = new ConfigurationProperty();
	}
	propertyGroup3[0].setName("class");
	propertyGroup3[0].setValue("a selection class");

	// First subelement group
	ConfigurationElement[] subElementGroup1 = new ConfigurationElement[2];
	for (int i = 0; i < subElementGroup1.length; i++) {
		subElementGroup1[i] = new ConfigurationElement();
		subElementGroup1[i].setParent(pluginExtensions[2]);
	}
	subElementGroup1[0].setName("category");
	subElementGroup1[0].setProperties(propertyGroup1);

	subElementGroup1[1].setName("wizard");
	subElementGroup1[1].setProperties(propertyGroup2);

	// Second subelement group
	ConfigurationElement[] subElementGroup2 = new ConfigurationElement[2];
	for (int i = 0; i < subElementGroup2.length; i++) {
		subElementGroup2[i] = new ConfigurationElement();
		subElementGroup2[i].setParent(subElementGroup1[1]);
	}
	subElementGroup2[0].setName("description");
	subElementGroup2[0].setValue("This is just a description");
	subElementGroup2[1].setName("selection");
	subElementGroup2[1].setProperties(propertyGroup3);

	// Now link them together
	subElementGroup1[1].setSubElements(subElementGroup2);
	pluginExtensions[2].setSubElements(subElementGroup1);
	fragment.setDeclaredExtensions(pluginExtensions);

	ByteArrayOutputStream fs;
	fs = new ByteArrayOutputStream();
	PrintWriter w = new PrintWriter(fs);
	RegistryWriter regWriter = new RegistryWriter();
	regWriter.writePluginDescriptor(plugin, w, 0);
	w.flush();
	w.close();

	String[] localXMLFiles = new String[1];
	localXMLFiles[0] = fs.toString();
		
	return localXMLFiles;
}
/*  fragmentExtensionTest
 *  Tests to ensure we cache extension entries off a fragment correctly.
 */
public void fragmentExtensionTest() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);

	String[] localXMLFiles = fragmentExtensionSetup();
	PluginRegistryModel registry = doInitialParsing(factory, localXMLFiles, false);

	// write to a cache file and read it back in
	// Don't resolve this registry as we haven't put in all the extension
	// points that are needed
	PluginRegistryModel cachedRegistry = doCacheWriteAndRead(registry, factory);

	// Now compare the registries.  They should be the same
	compareRegistries(registry, cachedRegistry);

	registry = cachedRegistry = null;
	factory = null;
}
public String[] fragmentExtExtPtLinkSetup() {
	PluginDescriptor[] pluginList = new PluginDescriptor[2];
	PluginDescriptor plugin = new PluginDescriptor();
	plugin.setName("A simple test");
	plugin.setId("abc1");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");
	
	FragmentDescriptor[] fragmentList = new FragmentDescriptor[2];
	FragmentDescriptor fragment = new FragmentDescriptor();
	fragment.setPlugin("abc1");
	fragment.setPluginVersion("1.0");
	fragment.setName("A fragment");
	fragment.setProviderName("SomeoneElse");
	fragment.setId("fragmentId1");
	fragment.setVersion("1.1");
	FragmentDescriptor[] pluginFragmentList = new FragmentDescriptor[1];
	pluginFragmentList[0] = fragment;
	plugin.setFragments(pluginFragmentList);

	Extension[] pluginExtensions = new Extension[4];
	for (int i = 0; i < pluginExtensions.length; i++) {
		pluginExtensions[i] = new Extension();
		pluginExtensions[i].setParentPluginDescriptor(plugin);
	}
	pluginExtensions[0].setName("First Extension");
	pluginExtensions[0].setId("first");
	pluginExtensions[0].setExtensionPoint("abc1.firstExtPt");
	pluginExtensions[1].setName("Second Extension");
	pluginExtensions[1].setId("second");
	pluginExtensions[1].setExtensionPoint("abc1.secondExtPt");
	pluginExtensions[2].setName("Third Extension");
	pluginExtensions[2].setId("third");
	pluginExtensions[2].setExtensionPoint("abc1.thirdExtPt");
	pluginExtensions[3].setName("Fourth Extension");
	pluginExtensions[3].setId("fourth");
	pluginExtensions[3].setExtensionPoint("abc2.anotherExtPt");
	fragment.setDeclaredExtensions(pluginExtensions);

	ExtensionPoint[] pluginExtensionPoints = new ExtensionPoint[3];
	for (int i = 0; i < pluginExtensionPoints.length; i++) {
		pluginExtensionPoints[i] = new ExtensionPoint();
	}
	pluginExtensionPoints[0].setName("First Extension Point");
	pluginExtensionPoints[0].setId("firstExtPt");
	pluginExtensionPoints[1].setName("Second Extension Point");
	pluginExtensionPoints[1].setId("secondExtPt");
	pluginExtensionPoints[2].setName("Third Extension Point");
	pluginExtensionPoints[2].setId("thirdExtPt");
	fragment.setDeclaredExtensionPoints(pluginExtensionPoints);

	pluginList[0] = plugin;
	fragmentList[0] = fragment;

	plugin = new PluginDescriptor();
	plugin.setName("A simple test2");
	plugin.setId("abc2");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");
	fragment = new FragmentDescriptor();
	fragment.setName("Second Fragment");
	fragment.setPlugin("abc2");
	fragment.setPluginVersion("1.0");
	fragment.setVersion("3.4");
	fragment.setId("fragmentId2");
	pluginFragmentList = new FragmentDescriptor[1];
	pluginFragmentList[0] = fragment;
	plugin.setFragments(pluginFragmentList);
	pluginExtensionPoints = new ExtensionPoint[1];
	for (int i = 0; i < pluginExtensionPoints.length; i++) {
		pluginExtensionPoints[i] = new ExtensionPoint();
	}
	pluginExtensionPoints[0].setName("Another Extension Point");
	pluginExtensionPoints[0].setId("anotherExtPt");
	fragment.setDeclaredExtensionPoints(pluginExtensionPoints);

	pluginList[1] = plugin;
	fragmentList[1] = fragment;

	ByteArrayOutputStream fs;
	String[] localXMLFiles = new String[4];
	for (int i = 0; i < pluginList.length; i++) {
		fs = new ByteArrayOutputStream();
		PrintWriter w = new PrintWriter(fs);
		RegistryWriter regWriter = new RegistryWriter();
		regWriter.writePluginDescriptor(pluginList[i], w, 0);
		w.flush();
		w.close();

		localXMLFiles[i] = fs.toString();
	}
	for (int i = 0; i < fragmentList.length; i++) {
		fs = new ByteArrayOutputStream();
		PrintWriter w = new PrintWriter(fs);
		RegistryWriter regWriter = new RegistryWriter();
		regWriter.writePluginFragment(fragmentList[i], w, 0);
		w.flush();
		w.close();

		localXMLFiles[pluginList.length + i] = fs.toString();
	}
	return localXMLFiles;
}
/*  fragmentExtExtPtLinkTest
 *  Tests to ensure we cache cross links for extensions and extension
 *  points within fragments correctly.
 */
public void fragmentExtExtPtLinkTest() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);

	String[] localXMLFiles = fragmentExtExtPtLinkSetup();
	PluginRegistryModel registry = doInitialParsing(factory, localXMLFiles, true);
	// IStatus resolveStatus = registry.resolve(true, true);

	// write to a cache file and read it back in
	PluginRegistryModel cachedRegistry = doCacheWriteAndRead(registry, factory);

	// Now compare the registries.  They should be the same
	compareRegistries(registry, cachedRegistry);

	registry = cachedRegistry = null;
	factory = null;
}
public String[] fragmentLibrarySetup() {
	PluginDescriptor plugin = new PluginDescriptor();
	plugin.setName("A simple test");
	plugin.setId("org.eclipse.webdav");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");
	
	FragmentDescriptor fragment = new FragmentDescriptor();
	fragment.setName("A fragment name");
	fragment.setVersion("2.2");
	fragment.setPlugin("org.eclipse.webdav");
	fragment.setPluginVersion("1.0");
	fragment.setId("fragmentId");
	FragmentDescriptor[] fragmentList = new FragmentDescriptor[1];
	fragmentList[0] = fragment;
	plugin.setFragments(fragmentList);

	Library[] pluginRuntime = new Library[2];
	for (int i = 0; i < pluginRuntime.length; i++) {
		pluginRuntime[i] = new Library();
	}
	pluginRuntime[0].setName("runtime.jar");
	String[] exportString = { "*" };
	pluginRuntime[0].setExports(exportString);
	pluginRuntime[0].setType("code");
	pluginRuntime[1].setName("xerces.jar");
	fragment.setRuntime(pluginRuntime);

	ByteArrayOutputStream fs;
	fs = new ByteArrayOutputStream();
	PrintWriter w = new PrintWriter(fs);
	RegistryWriter regWriter = new RegistryWriter();
	regWriter.writePluginDescriptor(plugin, w, 0);
	w.flush();
	w.close();

	String[] localXMLFiles = new String[1];
	localXMLFiles[0] = fs.toString();
	return localXMLFiles;
}
/*  fragmentLibraryTest
 *  Tests to ensure we cache library entries off fragements correctly.
 */
public void fragmentLibraryTest() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);

	String[] localXMLFiles = fragmentLibrarySetup();
	PluginRegistryModel registry = doInitialParsing(factory, localXMLFiles, true);

	// write to a cache file and read it back in
	PluginRegistryModel cachedRegistry = doCacheWriteAndRead(registry, factory);

	// Now compare the registries.  They should be the same
	compareRegistries(registry, cachedRegistry);

	registry = cachedRegistry = null;
	factory = null;
}
public String[] fragmentReadOnlySetup() {
	PluginDescriptor plugin = new PluginDescriptor();
	plugin.setName("A simple test");
	plugin.setId("org.eclipse.webdav");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");
	
	FragmentDescriptor fragment = new FragmentDescriptor();
	fragment.setName("fragment1");
	fragment.setPlugin("org.eclipse.webdav");
	fragment.setPluginVersion("1.0");
	fragment.setVersion("1.4");
	fragment.setId("newFragmentId");
	FragmentDescriptor[] fragmentList = new FragmentDescriptor[1];
	fragmentList[0] = fragment;
	plugin.setFragments(fragmentList);

	PluginPrerequisite[] pluginRequires = new PluginPrerequisite[1];
	for (int i = 0; i < pluginRequires.length; i++) {
		pluginRequires[i] = new PluginPrerequisite();
	}
	pluginRequires[0].setPlugin("org.eclipse.core.runtime");
	fragment.setRequires(pluginRequires);

	Library[] pluginRuntime = new Library[1];
	for (int i = 0; i < pluginRuntime.length; i++) {
		pluginRuntime[i] = new Library();
	}
	pluginRuntime[0].setName("runtime.jar");
	fragment.setRuntime(pluginRuntime);

	Extension[] pluginExtensions = new Extension[1];
	for (int i = 0; i < pluginExtensions.length; i++) {
		pluginExtensions[i] = new Extension();
		pluginExtensions[i].setParentPluginDescriptor(plugin);
	}
	pluginExtensions[0].setName("First Extension");
	fragment.setDeclaredExtensions(pluginExtensions);
	ConfigurationElement[] subElements = new ConfigurationElement[1];
	for (int i = 0; i < subElements.length; i++) {
		subElements[i] = new ConfigurationElement();
		subElements[i].setParent(pluginExtensions[0]);
	}
	subElements[0].setName("FirstSubelement");
	ConfigurationProperty[] properties = new ConfigurationProperty[1];
	for (int i = 0; i < properties.length; i++) {
		properties[i] = new ConfigurationProperty();
	}
	properties[0].setName("FirstProperty");
	properties[0].setValue("FirstValue");
	subElements[0].setProperties(properties);
	pluginExtensions[0].setSubElements(subElements);

	ExtensionPoint[] pluginExtensionPoints = new ExtensionPoint[1];
	for (int i = 0; i < pluginExtensionPoints.length; i++) {
		pluginExtensionPoints[i] = new ExtensionPoint();
	}
	pluginExtensionPoints[0].setName("First Extension Point");
	fragment.setDeclaredExtensionPoints(pluginExtensionPoints);

	ByteArrayOutputStream fs;
	fs = new ByteArrayOutputStream();
	PrintWriter w = new PrintWriter(fs);
	RegistryWriter regWriter = new RegistryWriter();
	regWriter.writePluginDescriptor(plugin, w, 0);
	w.flush();
	w.close();

	String[] localXMLFiles = new String[1];
	localXMLFiles[0] = fs.toString();
	return localXMLFiles;
}
/*  fragmentReadOnlyTest
 *  This is a very basic test to make sure we are caching a very
 *  simple registry and reading it back in correctly.
 */
public void fragmentReadOnlyTest() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);

	String[] localXMLFiles = fragmentReadOnlySetup();
	PluginRegistryModel registry = doInitialParsing(factory, localXMLFiles, true);
	registry.markReadOnly();

	// write to a cache file and read it back in
	PluginRegistryModel cachedRegistry = doCacheWriteAndRead(registry, factory);

	// Now compare the registries.  They should be the same
	compareRegistries(registry, cachedRegistry);

	registry = cachedRegistry = null;
	factory = null;
}
public String[] fragmentSetup() {
	PluginDescriptor plugin = new PluginDescriptor();
	plugin.setName("A simple test");
	plugin.setId("org.eclipse.webdav");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");
	
	ByteArrayOutputStream fs = new ByteArrayOutputStream();
	PrintWriter w = new PrintWriter(fs);
	RegistryWriter regWriter = new RegistryWriter();
	regWriter.writePluginDescriptor(plugin, w, 0);
	w.flush();
	w.close();
	String[] localXMLFiles = new String[2];
	localXMLFiles[0] = fs.toString();

	FragmentDescriptor fragment = new FragmentDescriptor();
	fragment.setPlugin("org.eclipse.webdav");
	fragment.setPluginVersion("1.0");
	fragment.setName("The fragment's name");
	fragment.setId("fragmentId");
	fragment.setVersion("1.9");
	fragment.setMatch(PluginFragmentModel.FRAGMENT_MATCH_PERFECT);
	regWriter.writePluginFragment(fragment, w, 0);
	fs = new ByteArrayOutputStream();
	w = new PrintWriter(fs);
	regWriter = new RegistryWriter();
	regWriter.writePluginFragment(fragment, w, 0);
	w.flush();
	w.close();
	localXMLFiles[1] = fs.toString();

	return localXMLFiles;
}
/*  fragmentTest
 *  This is a very basic test to make sure we are caching a very
 *  simple fragment (one plugin with attirbutes only and a fragment
 *  that causes the name to change) and reading
 *  it back in correctly.
 */
public void fragmentTest() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);

	String[] localXMLFiles = fragmentSetup();
	PluginRegistryModel registry = doInitialParsing(factory, localXMLFiles, true);

	// write to a cache file and read it back in
	PluginRegistryModel cachedRegistry = doCacheWriteAndRead(registry, factory);

	// Now compare the registries.  They should be the same
	compareRegistries(registry, cachedRegistry);

	registry = cachedRegistry = null;
	factory = null;
}
public String[] fragmentPluginSetup() {
	PluginDescriptor[] plugins = new PluginDescriptor[5];
	
	PluginDescriptor plugin = new PluginDescriptor();
	plugin.setName("A simple test");
	plugin.setId("id1.0");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");
	plugins[0] = plugin;

	plugin = new PluginDescriptor();
	plugin.setName("A simple test");
	plugin.setId("id1.5");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.5");
	plugins[1] = plugin;

	plugin = new PluginDescriptor();
	plugin.setName("A simple test");
	plugin.setId("id1.9");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.9");
	plugins[2] = plugin;

	plugin = new PluginDescriptor();
	plugin.setName("A simple test");
	plugin.setId("id3.2");
	plugin.setProviderName("IBM");
	plugin.setVersion("3.2");
	plugins[3] = plugin;

	plugin = new PluginDescriptor();
	plugin.setName("A simple test");
	plugin.setId("id4.7");
	plugin.setProviderName("IBM");
	plugin.setVersion("4.7");
	plugins[4] = plugin;	

	FragmentDescriptor[] fragmentList = new FragmentDescriptor[5];

	FragmentDescriptor[] pluginFragmentList = new FragmentDescriptor[1];
	FragmentDescriptor fragment = new FragmentDescriptor();
	fragment.setPlugin("id1.0");
	fragment.setPluginVersion("1.0");
	fragment.setName("The fragment's name");
	fragment.setId("frag1");
	fragment.setVersion("1.1");
	pluginFragmentList[0] = fragment;
	plugins[0].setFragments(pluginFragmentList);
	fragmentList[0] = fragment;

	pluginFragmentList = new FragmentDescriptor[1];
	fragment = new FragmentDescriptor();
	fragment.setPlugin("id1.5");
	fragment.setPluginVersion("1.5");
	fragment.setName("The fragment's name");
	fragment.setId("frag2");
	fragment.setVersion("1.1");
	fragment.setMatch(PluginFragmentModel.FRAGMENT_MATCH_PERFECT);
	pluginFragmentList[0] = fragment;
	plugins[1].setFragments(pluginFragmentList);
	fragmentList[1] = fragment;

	pluginFragmentList = new FragmentDescriptor[1];
	fragment = new FragmentDescriptor();
	fragment.setPlugin("id1.9");
	fragment.setPluginVersion("1.9");
	fragment.setName("The fragment's name");
	fragment.setId("frag3");
	fragment.setVersion("1.1");
	fragment.setMatch(PluginFragmentModel.FRAGMENT_MATCH_EQUIVALENT);
	pluginFragmentList[0] = fragment;
	plugins[2].setFragments(pluginFragmentList);
	fragmentList[2] = fragment;

	pluginFragmentList = new FragmentDescriptor[1];
	fragment = new FragmentDescriptor();
	fragment.setPlugin("id3.2");
	fragment.setPluginVersion("3.0");
	fragment.setName("The fragment's name");
	fragment.setId("frag4");
	fragment.setVersion("1.1");
	fragment.setMatch(PluginFragmentModel.FRAGMENT_MATCH_COMPATIBLE);
	pluginFragmentList[0] = fragment;
	plugins[3].setFragments(pluginFragmentList);
	fragmentList[3] = fragment;

	pluginFragmentList = new FragmentDescriptor[1];
	fragment = new FragmentDescriptor();
	fragment.setPlugin("id4.7");
	fragment.setPluginVersion("1.5");
	fragment.setName("The fragment's name");
	fragment.setId("frag5");
	fragment.setVersion("1.1");
	fragment.setMatch(PluginFragmentModel.FRAGMENT_MATCH_GREATER_OR_EQUAL);
	pluginFragmentList[0] = fragment;
	plugins[4].setFragments(pluginFragmentList);
	fragmentList[4] = fragment;

	String[] localXMLFiles = new String[plugins.length + fragmentList.length];
	for (int i = 0; i < plugins.length; i++) {
		ByteArrayOutputStream fs = new ByteArrayOutputStream();
		PrintWriter w = new PrintWriter(fs);
		RegistryWriter regWriter = new RegistryWriter();
		regWriter.writePluginDescriptor(plugins[i], w, 0);
		w.flush();
		w.close();
	
		localXMLFiles[i] = fs.toString();
	}
	
	for (int i = 0; i < fragmentList.length; i++) {
		ByteArrayOutputStream fs = new ByteArrayOutputStream();
		PrintWriter w = new PrintWriter(fs);
		RegistryWriter regWriter = new RegistryWriter();
		regWriter.writePluginFragment(fragmentList[i], w, 0);
		w.flush();
		w.close();
		localXMLFiles[plugins.length + i] = fs.toString();
	}

	return localXMLFiles;
}
/*  fragmentPluginTest
 *  This is a very basic test to make sure we are caching a plugin
 *  with one fragment and reading it back in correctly.
 */
public void fragmentPluginTest() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);

	String[] localXMLFiles = fragmentPluginSetup();
	PluginRegistryModel registry = doInitialParsing(factory, localXMLFiles, true);

	// write to a cache file and read it back in
	PluginRegistryModel cachedRegistry = doCacheWriteAndRead(registry, factory);

	// Now compare the registries.  They should be the same
	compareRegistries(registry, cachedRegistry);

	registry = cachedRegistry = null;
	factory = null;
}
public String[] fragmentRegistrySetup() {
	PluginDescriptor[] pluginList = new PluginDescriptor[5];

	PluginDescriptor plugin = new PluginDescriptor();
	plugin.setName("A simple test");
	plugin.setId("org.eclipse.webdav");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");
	pluginList[0] = plugin;

	plugin = new PluginDescriptor();
	plugin.setName("A simple test");
	plugin.setId("org.eclipse.webdav");
	plugin.setProviderName("IBM");
	plugin.setVersion("3.0");
	pluginList[1] = plugin;

	plugin = new PluginDescriptor();
	plugin.setName("A simple test");
	plugin.setId("org.eclipse.webdav");
	plugin.setProviderName("IBM");
	plugin.setVersion("2.0");
	pluginList[2] = plugin;

	plugin = new PluginDescriptor();
	plugin.setName("A Different Name");
	plugin.setId("newId");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");
	pluginList[3] = plugin;

	plugin = new PluginDescriptor();
	plugin.setName("A third name");
	plugin.setId("thirdId");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");
	pluginList[4] = plugin;

	FragmentDescriptor[] fragmentList = new FragmentDescriptor[5];
	FragmentDescriptor fragment = new FragmentDescriptor();
	fragment.setName("fragment");
	fragment.setPlugin("org.eclipse.webdav");
	fragment.setProviderName("Something");
	fragment.setPluginVersion("1.0");
	fragment.setId("fragmentId");
	fragment.setVersion("3.3");
	fragmentList[0] = fragment;

	fragment = new FragmentDescriptor();
	fragment.setName("fragment");
	fragment.setPlugin("org.eclipse.webdav");
	fragment.setVersion("1.1");
	fragment.setPluginVersion("3.0");
	fragment.setId("fragmentId");
	fragmentList[1] = fragment;

	fragment = new FragmentDescriptor();
	fragment.setName("fragment");
	fragment.setPlugin("org.eclipse.webdav");
	fragment.setPluginVersion("2.0");
	fragment.setId("fragmentId");
	fragment.setVersion("2.2");
	fragmentList[2] = fragment;

	fragment = new FragmentDescriptor();
	fragment.setName("A Different Name For a Fragment");
	fragment.setPlugin("newId");
	fragment.setProviderName("IBM");
	fragment.setPluginVersion("1.0");
	fragment.setId("newFragmentId");
	fragment.setVersion("3.3");
	fragmentList[3] = fragment;

	fragment = new FragmentDescriptor();
	fragment.setName("final fragment");
	fragment.setPlugin("thirdId");
	fragment.setId("weird");
	fragment.setProviderName("IBM");
	fragment.setPluginVersion("1.0");
	fragment.setVersion("7");
	fragmentList[4] = fragment;

	String[] localXMLFiles = new String[pluginList.length + fragmentList.length];
	for (int i = 0; i < pluginList.length; i++) {
		ByteArrayOutputStream fs;
		fs = new ByteArrayOutputStream();
		PrintWriter w = new PrintWriter(fs);
		RegistryWriter regWriter = new RegistryWriter();
		regWriter.writePluginDescriptor(pluginList[i], w, 0);
		w.flush();
		w.close();

		localXMLFiles[i] = fs.toString();
	}
	for (int i = 0; i < fragmentList.length; i++) {
		ByteArrayOutputStream fs;
		fs = new ByteArrayOutputStream();
		PrintWriter w = new PrintWriter(fs);
		RegistryWriter regWriter = new RegistryWriter();
		regWriter.writePluginFragment(fragmentList[i], w, 0);
		w.flush();
		w.close();

		localXMLFiles[i + pluginList.length] = fs.toString();
	}

	return localXMLFiles;
}
/*  fragmentRegistryTest
 *  Tests to ensure we cache a simple registry with fragments correctly.
 */
public void fragmentRegistryTest() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);

	String[] localXMLFiles = fragmentRegistrySetup();
	PluginRegistryModel registry = doInitialParsing(factory, localXMLFiles, true);

	// write to a cache file and read it back in
	PluginRegistryModel cachedRegistry = doCacheWriteAndRead(registry, factory);

	// Now compare the registries.  They should be the same
	compareRegistries(registry, cachedRegistry);

	registry = cachedRegistry = null;
	factory = null;
}
public String[] fragmentRequiresSetup() {
	PluginDescriptor plugin = new PluginDescriptor();
	plugin.setName("A simple test");
	plugin.setId("org.eclipse.webdav");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");
	
	FragmentDescriptor fragment = new FragmentDescriptor();
	fragment.setName("fragment2");
	fragment.setPlugin("org.eclipse.webdav");
	fragment.setPluginVersion("1.0");
	fragment.setVersion("1.0");
	fragment.setId("fragmentId2");

	PluginPrerequisite[] pluginRequires = new PluginPrerequisite[3];
	for (int i = 0; i < pluginRequires.length; i++) {
		pluginRequires[i] = new PluginPrerequisite();
	}
	pluginRequires[0].setPlugin("pluginId1");
	pluginRequires[0].setVersion("4");
	pluginRequires[0].setExport(true);
	pluginRequires[0].setMatchByte(PluginPrerequisiteModel.PREREQ_MATCH_EQUIVALENT);
	pluginRequires[1].setPlugin("pluginId2");
	pluginRequires[1].setOptional(true);
	pluginRequires[2].setPlugin("pluginId3");
	pluginRequires[2].setVersion("3");
	fragment.setRequires(pluginRequires);

	ByteArrayOutputStream fs;
	fs = new ByteArrayOutputStream();
	PrintWriter w = new PrintWriter(fs);
	RegistryWriter regWriter = new RegistryWriter();
	regWriter.writePluginDescriptor(plugin, w, 0);
	w.flush();
	w.close();

	String[] localXMLFiles = new String[5];
	localXMLFiles[0] = fs.toString();

	fs = new ByteArrayOutputStream();
	w = new PrintWriter(fs);
	regWriter = new RegistryWriter();
	regWriter.writePluginFragment(fragment, w, 0);
	w.flush();
	w.close();
	localXMLFiles[1] = fs.toString();
	
	plugin = new PluginDescriptor();
	plugin.setName("First Prerequisite");
	plugin.setId("pluginId1");
	plugin.setProviderName("IBM");
	plugin.setVersion("4");
	fs = new ByteArrayOutputStream();
	w = new PrintWriter(fs);
	regWriter = new RegistryWriter();
	regWriter.writePluginDescriptor(plugin, w, 0);
	w.flush();
	w.close();
	localXMLFiles[2] = fs.toString();
	
	plugin = new PluginDescriptor();
	plugin.setName("Second Prerequisite");
	plugin.setId("pluginId2");
	plugin.setProviderName("IBM");
	plugin.setVersion("4");
	fs = new ByteArrayOutputStream();
	w = new PrintWriter(fs);
	regWriter = new RegistryWriter();
	regWriter.writePluginDescriptor(plugin, w, 0);
	w.flush();
	w.close();
	localXMLFiles[3] = fs.toString();
	
	plugin = new PluginDescriptor();
	plugin.setName("Third Prerequisite");
	plugin.setId("pluginId3");
	plugin.setProviderName("IBM");
	plugin.setVersion("3");
	fs = new ByteArrayOutputStream();
	w = new PrintWriter(fs);
	regWriter = new RegistryWriter();
	regWriter.writePluginDescriptor(plugin, w, 0);
	w.flush();
	w.close();
	localXMLFiles[4] = fs.toString();
	
	return localXMLFiles;
}
/*  fragmentRequiresTest
 *  Tests to ensure we cache plugin prerequisite entries that hang
 *  off a fragment correctly.
 */
public void fragmentRequiresTest() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);

	String[] localXMLFiles = fragmentRequiresSetup();
	// Don't resolve the registry.  All the prerequisites are not there.
	// XXX - THIS SHOULD FAIL BUT DOES NOT
	// 1GDU4UP: ITPCORE:ALL - fragments - don't check for existence of prereq's
	PluginRegistryModel registry = doInitialParsing(factory, localXMLFiles, true);

	// write to a cache file and read it back in
	PluginRegistryModel cachedRegistry = doCacheWriteAndRead(registry, factory);

	// Now compare the registries.  They should be the same
	compareRegistries(registry, cachedRegistry);

	registry = cachedRegistry = null;
	factory = null;
}
public String[] librarySetup() {
	PluginDescriptor plugin = new PluginDescriptor();
	plugin.setName("A simple test");
	plugin.setId("org.eclipse.webdav");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");

	Library[] pluginRuntime = new Library[2];
	for (int i = 0; i < pluginRuntime.length; i++) {
		pluginRuntime[i] = new Library();
	}
	pluginRuntime[0].setName("runtime.jar");
	String[] exportString = { "*" };
	pluginRuntime[0].setExports(exportString);
	pluginRuntime[0].setType("code");
	pluginRuntime[1].setName("xerces.jar");
	plugin.setRuntime(pluginRuntime);

	ByteArrayOutputStream fs;
	fs = new ByteArrayOutputStream();
	PrintWriter w = new PrintWriter(fs);
	RegistryWriter regWriter = new RegistryWriter();
	regWriter.writePluginDescriptor(plugin, w, 0);
	w.flush();
	w.close();

	String[] localXMLFiles = new String[1];
	localXMLFiles[0] = fs.toString();
	return localXMLFiles;
}
/*  libraryTest
 *  Tests to ensure we cache library entries correctly.
 */
public void libraryTest() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);

	String[] localXMLFiles = librarySetup();
	PluginRegistryModel registry = doInitialParsing(factory, localXMLFiles, true);

	// write to a cache file and read it back in
	PluginRegistryModel cachedRegistry = doCacheWriteAndRead(registry, factory);

	// Now compare the registries.  They should be the same
	compareRegistries(registry, cachedRegistry);

	registry = cachedRegistry = null;
	factory = null;
}
public String[] pluginSetup() {
	PluginDescriptor plugin = new PluginDescriptor();
	plugin.setName("A simple test");
	plugin.setId("org.eclipse.webdav");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");

	ByteArrayOutputStream fs = new ByteArrayOutputStream();
	PrintWriter w = new PrintWriter(fs);
	RegistryWriter regWriter = new RegistryWriter();
	regWriter.writePluginDescriptor(plugin, w, 0);
	w.flush();
	w.close();

	String[] localXMLFiles = new String[1];
	localXMLFiles[0] = fs.toString();
	return localXMLFiles;
}
/*  pluginTest
 *  This is a very basic test to make sure we are caching a very
 *  simple registry (one plugin with attirbutes only) and reading
 *  it back in correctly.
 */
public void pluginTest() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);

	String[] localXMLFiles = pluginSetup();
	PluginRegistryModel registry = doInitialParsing(factory, localXMLFiles, true);

	// write to a cache file and read it back in
	PluginRegistryModel cachedRegistry = doCacheWriteAndRead(registry, factory);

	// Now compare the registries.  They should be the same
	compareRegistries(registry, cachedRegistry);

	registry = cachedRegistry = null;
	factory = null;
}
public String[] readOnlySetup() {
	PluginDescriptor plugin = new PluginDescriptor();
	plugin.setName("A simple test");
	plugin.setId("org.eclipse.webdav");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");

	PluginPrerequisite[] pluginRequires = new PluginPrerequisite[1];
	for (int i = 0; i < pluginRequires.length; i++) {
		pluginRequires[i] = new PluginPrerequisite();
	}
	pluginRequires[0].setPlugin("org.eclipse.core.runtime");
	plugin.setRequires(pluginRequires);

	Library[] pluginRuntime = new Library[1];
	for (int i = 0; i < pluginRuntime.length; i++) {
		pluginRuntime[i] = new Library();
	}
	pluginRuntime[0].setName("runtime.jar");
	plugin.setRuntime(pluginRuntime);

	Extension[] pluginExtensions = new Extension[1];
	for (int i = 0; i < pluginExtensions.length; i++) {
		pluginExtensions[i] = new Extension();
		pluginExtensions[i].setParentPluginDescriptor(plugin);
	}
	pluginExtensions[0].setName("First Extension");
	plugin.setDeclaredExtensions(pluginExtensions);
	ConfigurationElement[] subElements = new ConfigurationElement[1];
	for (int i = 0; i < subElements.length; i++) {
		subElements[i] = new ConfigurationElement();
		subElements[i].setParent(pluginExtensions[0]);
	}
	subElements[0].setName("FirstSubelement");
	ConfigurationProperty[] properties = new ConfigurationProperty[1];
	for (int i = 0; i < properties.length; i++) {
		properties[i] = new ConfigurationProperty();
	}
	properties[0].setName("FirstProperty");
	properties[0].setValue("FirstValue");
	subElements[0].setProperties(properties);
	pluginExtensions[0].setSubElements(subElements);

	ExtensionPoint[] pluginExtensionPoints = new ExtensionPoint[1];
	for (int i = 0; i < pluginExtensionPoints.length; i++) {
		pluginExtensionPoints[i] = new ExtensionPoint();
	}
	pluginExtensionPoints[0].setName("First Extension Point");
	plugin.setDeclaredExtensionPoints(pluginExtensionPoints);

	ByteArrayOutputStream fs;
	fs = new ByteArrayOutputStream();
	PrintWriter w = new PrintWriter(fs);
	RegistryWriter regWriter = new RegistryWriter();
	regWriter.writePluginDescriptor(plugin, w, 0);
	w.flush();
	w.close();

	String[] localXMLFiles = new String[1];
	localXMLFiles[0] = fs.toString();
	return localXMLFiles;
}
/*  readOnlyTest
 *  This is a very basic test to make sure we are caching a very
 *  simple registry and reading it back in correctly.
 */
public void readOnlyTest() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);

	String[] localXMLFiles = readOnlySetup();
	// Don't resolve the registry as all the prerequisites aren't there
	PluginRegistryModel registry = doInitialParsing(factory, localXMLFiles, false);
	registry.markReadOnly();

	// write to a cache file and read it back in
	PluginRegistryModel cachedRegistry = doCacheWriteAndRead(registry, factory);

	// Now compare the registries.  They should be the same
	compareRegistries(registry, cachedRegistry);

	registry = cachedRegistry = null;
	factory = null;
}
/*  realRegistryTest
 *  Tests to ensure we correctly cache the real plugin registry we are using.
 */
public void realRegistryTest() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);

	PluginRegistryModel registry = (PluginRegistryModel) Platform.getPluginRegistry();

	// write to a cache file and read it back in
	PluginRegistryModel cachedRegistry = doCacheWriteAndRead(registry, factory);

	// Now compare the registries.  They should be the same
	compareRegistries(registry, cachedRegistry);

	registry = cachedRegistry = null;
	factory = null;
}
/*  realRegistryTest
 *  Tests to ensure we correctly cache the real plugin registry we are using.
 */
public void testRegistryEOF() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);

	PluginRegistryModel registry = (PluginRegistryModel) Platform.getPluginRegistry();

	// write to a cache file and read it back in
	ByteArrayOutputStream b = new ByteArrayOutputStream();
	DataOutputStream output = new DataOutputStream(b);
	doCacheWrite(registry, output, factory);

	byte[] source = b.toByteArray();
	byte[] dest = new byte[source.length-1];
	System.arraycopy(source, 0, dest, 0, source.length - 1);
	
	DataInputStream input = new DataInputStream(new ByteArrayInputStream(dest));
	PluginRegistryModel cachedRegistry = doCacheRead(registry, input, factory);

	assertNull("1.0", cachedRegistry);

	registry = cachedRegistry = null;
	factory = null;
}

public String[] registrySetup() {
	PluginDescriptor[] pluginList = new PluginDescriptor[5];

	PluginDescriptor plugin = new PluginDescriptor();
	plugin.setName("A simple test");
	plugin.setId("org.eclipse.webdav");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");
	pluginList[0] = plugin;

	plugin = new PluginDescriptor();
	plugin.setName("A simple test");
	plugin.setId("org.eclipse.webdav");
	plugin.setProviderName("IBM");
	plugin.setVersion("3.0");
	pluginList[1] = plugin;

	plugin = new PluginDescriptor();
	plugin.setName("A simple test");
	plugin.setId("org.eclipse.webdav");
	plugin.setProviderName("IBM");
	plugin.setVersion("2.0");
	pluginList[2] = plugin;

	plugin = new PluginDescriptor();
	plugin.setName("A Different Name");
	plugin.setId("newId");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");
	pluginList[3] = plugin;

	plugin = new PluginDescriptor();
	plugin.setName("A third name");
	plugin.setId("thirdId");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");
	pluginList[4] = plugin;

	String[] localXMLFiles = new String[pluginList.length];
	for (int i = 0; i < pluginList.length; i++) {
		ByteArrayOutputStream fs;
		fs = new ByteArrayOutputStream();
		PrintWriter w = new PrintWriter(fs);
		RegistryWriter regWriter = new RegistryWriter();
		regWriter.writePluginDescriptor(pluginList[i], w, 0);
		w.flush();
		w.close();

		localXMLFiles[i] = fs.toString();
	}

	return localXMLFiles;
}
/*  registryTest
 *  Tests to ensure we cache a simple registry correctly.
 */
public void registryTest() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);

	String[] localXMLFiles = registrySetup();
	PluginRegistryModel registry = doInitialParsing(factory, localXMLFiles, true);

	// write to a cache file and read it back in
	PluginRegistryModel cachedRegistry = doCacheWriteAndRead(registry, factory);

	// Now compare the registries.  They should be the same
	compareRegistries(registry, cachedRegistry);

	registry = cachedRegistry = null;
	factory = null;
}
public String[] requiresSetup() {
	PluginDescriptor plugin = new PluginDescriptor();
	plugin.setName("A simple test");
	plugin.setId("org.eclipse.webdav");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");

	PluginPrerequisite[] pluginRequires = new PluginPrerequisite[5];
	for (int i = 0; i < pluginRequires.length; i++) {
		pluginRequires[i] = new PluginPrerequisite();
	}
	pluginRequires[0].setPlugin("org.eclipse.core.runtime");
	pluginRequires[0].setVersion("4");
	pluginRequires[0].setExport(true);
	pluginRequires[0].setMatchByte(PluginPrerequisiteModel.PREREQ_MATCH_PERFECT);
	pluginRequires[1].setPlugin("org.apache.xerces");
	pluginRequires[1].setOptional(true);
	pluginRequires[1].setVersion("1.4");
	pluginRequires[1].setMatchByte(PluginPrerequisiteModel.PREREQ_MATCH_EQUIVALENT);
	pluginRequires[2].setPlugin("org.eclipse.core.resources");
	pluginRequires[2].setVersion("3");
	pluginRequires[2].setMatchByte(PluginPrerequisiteModel.PREREQ_MATCH_COMPATIBLE);
	pluginRequires[3].setPlugin("anotherPrerequisite");
	pluginRequires[3].setVersion("2.1.3");
	pluginRequires[3].setMatchByte(PluginPrerequisiteModel.PREREQ_MATCH_GREATER_OR_EQUAL);
	pluginRequires[4].setPlugin("finalPrerequisite");
	plugin.setRequires(pluginRequires);

	ByteArrayOutputStream fs;
	fs = new ByteArrayOutputStream();
	PrintWriter w = new PrintWriter(fs);
	RegistryWriter regWriter = new RegistryWriter();
	regWriter.writePluginDescriptor(plugin, w, 0);
	w.flush();
	w.close();

	String[] localXMLFiles = new String[1];
	localXMLFiles[0] = fs.toString();
	return localXMLFiles;
}
/*  requiresTest
 *  Tests to ensure we cache plugin prerequisite entries correctly.
 */
public void requiresTest() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);

	String[] localXMLFiles = requiresSetup();
	// Don't resolve the registry.  All the prerequisites aren't there
	PluginRegistryModel registry = doInitialParsing(factory, localXMLFiles, false);

	// write to a cache file and read it back in
	PluginRegistryModel cachedRegistry = doCacheWriteAndRead(registry, factory);

	// Now compare the registries.  They should be the same
	compareRegistries(registry, cachedRegistry);

	registry = cachedRegistry = null;
	factory = null;
}
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new SimpleCacheTests("pluginTest"));
	suite.addTest(new SimpleCacheTests("requiresTest"));
	suite.addTest(new SimpleCacheTests("libraryTest"));
	suite.addTest(new SimpleCacheTests("extensionTest"));
	suite.addTest(new SimpleCacheTests("readOnlyTest"));
	suite.addTest(new SimpleCacheTests("registryTest"));
	suite.addTest(new SimpleCacheTests("extExtPtLinkTest"));
	suite.addTest(new SimpleCacheTests("realRegistryTest"));
	suite.addTest(new SimpleCacheTests("fragmentTest"));
	suite.addTest(new SimpleCacheTests("fragmentPluginTest"));
	suite.addTest(new SimpleCacheTests("fragmentExtensionTest"));
	suite.addTest(new SimpleCacheTests("fragmentExtExtPtLinkTest"));
	suite.addTest(new SimpleCacheTests("fragmentLibraryTest"));
	suite.addTest(new SimpleCacheTests("fragmentReadOnlyTest"));
	suite.addTest(new SimpleCacheTests("fragmentRequiresTest"));
	suite.addTest(new SimpleCacheTests("fragmentRegistryTest"));
	suite.addTest(new SimpleCacheTests("testRegistryEOF"));
	return suite;
}
}
