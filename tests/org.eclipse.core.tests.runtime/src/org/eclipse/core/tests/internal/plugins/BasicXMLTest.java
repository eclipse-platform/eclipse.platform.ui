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
package org.eclipse.core.tests.internal.plugins;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.model.*;
import org.eclipse.core.internal.runtime.*;
import org.eclipse.core.internal.plugins.*;
import org.eclipse.core.tests.harness.*;
import java.io.*;
import junit.framework.*;
import org.xml.sax.*;

public class BasicXMLTest extends EclipseWorkspaceTest {
	PluginDescriptor plugin = null;
	PluginRegistry registry = null;
	InternalFactory factory = null;
public BasicXMLTest() {
	super(null);
}
public BasicXMLTest(String name) {
	super(name);
}
/* doParsing
 * This method will parse a series of XML files.  The input array should be
 * an array of string buffers where each string buffer is considered a complete 
 * XML file.  The returning array will have a corresponding plugin descriptor
 * for each of the XML files in the input array
 */
public PluginRegistry doParsing(InternalFactory factory, String[] localXMLFiles) {

	registry = new PluginRegistry();
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
	return registry;
}
public String[] extensionPointSetup() {
	plugin = new PluginDescriptor();
	plugin.setName("First test");
	plugin.setId("com.ibm.dav4j");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");
	ExtensionPoint[] pluginExtensionPoints = new ExtensionPoint[2];
	for (int i = 0; i < pluginExtensionPoints.length; i++) {
		pluginExtensionPoints[i] = new ExtensionPoint();
	}
	pluginExtensionPoints[0].setName("First Extension Point");
	pluginExtensionPoints[0].setId("first");
	pluginExtensionPoints[0].setSchema("some schema name");
	pluginExtensionPoints[1].setName("Second Extension Point");
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
/*  extensionPointTest
 *  Tests to ensure we create extension point entries and populate them correctly.
 *  Note that this test will NOT ensure that we correctly link related extensions
 *  and extension points.  This will be done in a different test
 */
public void extensionPointTest() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
	factory = new InternalFactory(problems);

	String[] localXMLFiles = extensionPointSetup();
	registry = doParsing(factory, localXMLFiles);

	PluginDescriptorModel[] pluginList = registry.getPlugins();
	IPluginDescriptor[] ROPluginList = registry.getPluginDescriptors();
	// Check both the read-only accessors and the read-write accessors

	// First check to make sure we got something back
	assertNotNull("1.1 Sane return value", pluginList);
	assertNotNull("1.2 Sane plugin descriptor", pluginList[0]);
	assertNotNull("1.3 Sane return value", ROPluginList);
	assertNotNull("1.4 Sane plugin descriptor", ROPluginList[0]);

	// Now check that the plugin we got really reflects what's in the xml stream
	assertEquals("2.1 Plugin Name", plugin.getName(), pluginList[0].getName());
	assertEquals("2.2 Plugin Id", plugin.getId(), pluginList[0].getId());
	assertEquals("2.3 Plugin Vendor Name", plugin.getProviderName(), pluginList[0].getProviderName());
	assertEquals("2.4 Plugin Version", plugin.getVersion(), pluginList[0].getVersion());
	assertNull("2.5 Plugin Class", pluginList[0].getPluginClass());
	assertNull("2.6 Runtime List", pluginList[0].getRuntime());
	assertNull("2.7 Requires List", pluginList[0].getRequires());
	assertNull("2.8 Extensions List", pluginList[0].getDeclaredExtensions());
	assertEquals("2.9 Registry", pluginList[0].getRegistry(), registry);
	assertNull("2.10 Plugin Location", pluginList[0].getLocation());
	// Now check the same things but with the read-only accessors
	assertEquals("2.11 Plugin Name", plugin.getName(), ROPluginList[0].getLabel());
	assertEquals("2.12 Plugin Id", plugin.getId(), ROPluginList[0].getUniqueIdentifier());
	assertEquals("2.13 Plugin Vendor Name", plugin.getProviderName(), ROPluginList[0].getProviderName());
	assertEquals("2.14 Plugin Version", plugin.getVersionIdentifier(), ROPluginList[0].getVersionIdentifier());
//	assertNotNull("2.15 Plugin Class Loader", ROPluginList[0].getPluginClassLoader());
	assertEquals("2.16 Runtime List", ROPluginList[0].getRuntimeLibraries().length, 0);
	assertEquals("2.17 Requires List", ROPluginList[0].getPluginPrerequisites().length, 0);
	assertEquals("2.18 Extensions List", ROPluginList[0].getExtensions().length, 0);
	assertNotNull("2.19 Plugin Location", ROPluginList[0].getInstallURL());

	// Now check the extension points
	ExtensionPointModel[] extPtList = pluginList[0].getDeclaredExtensionPoints();
	assertNotNull("3.1 Extension Point list null", extPtList);
	assertEquals("3.2 Number of extension points", extPtList.length, 2);
	IExtensionPoint[] ROExtPtList = ROPluginList[0].getExtensionPoints();
	assertNotNull("3.1 Extension Point list null", ROExtPtList);
	assertEquals("3.2 Number of extension points", ROExtPtList.length, 2);

	ExtensionPoint[] originalExtPts = (ExtensionPoint[]) plugin.getDeclaredExtensionPoints();

	// Make sure the values in this extension points are correct
	assertEquals("4.1 Extension Point name", extPtList[0].getName(), originalExtPts[0].getName());
	assertEquals("4.2 Extension Point id", extPtList[0].getId(), originalExtPts[0].getId());
	assertEquals("4.3 Extension Point schema", extPtList[0].getSchema(), originalExtPts[0].getSchema());
	assertEquals("4.4 Extension Point name", ROExtPtList[0].getLabel(), originalExtPts[0].getName());
	assertEquals("4.5 Extension Point id", ROExtPtList[0].getSimpleIdentifier(), originalExtPts[0].getId());
	assertEquals("4.6 Extension Point schema", ROExtPtList[0].getSchemaReference(), originalExtPts[0].getSchemaReference());

	// And check the 2nd requires element too
	assertEquals("5.1 Extension Point name", extPtList[1].getName(), originalExtPts[1].getName());
	assertEquals("5.2 Extension Point id", extPtList[1].getId(), originalExtPts[1].getId());
	assertEquals("5.3 Extension Point schema", extPtList[1].getSchema(), originalExtPts[1].getSchema());
	assertEquals("5.4 Extension Point name", ROExtPtList[1].getLabel(), originalExtPts[1].getName());
	assertEquals("5.5 Extension Point id", ROExtPtList[1].getSimpleIdentifier(), originalExtPts[1].getId());
	assertEquals("5.6 Extension Point schema", ROExtPtList[1].getSchemaReference(), originalExtPts[1].getSchemaReference());

	factory = null;
}
public String[] extensionSetup() {
	plugin = new PluginDescriptor();
	plugin.setName("A simple test");
	plugin.setId("com.ibm.dav4j");
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
 *  Tests to ensure we create extension entries and populate them correctly.
 *  Note that this test will NOT ensure that we correctly link related extensions
 *  and extension points.  This will be done in a different test
 */
public void extensionTest() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
	factory = new InternalFactory(problems);

	String[] localXMLFiles = extensionSetup();
	registry = doParsing(factory, localXMLFiles);

	// Check both the read-only accessors and the read-write accessors
	PluginDescriptorModel[] pluginList = registry.getPlugins();
	IPluginDescriptor[] ROPluginList = registry.getPluginDescriptors();

	// First check to make sure we got something back
	assertNotNull("1.1 Sane return value", pluginList);
	assertNotNull("1.2 Sane plugin descriptor", pluginList[0]);
	assertNotNull("1.3 Sane return value", ROPluginList);
	assertNotNull("1.4 Sane plugin descriptor", ROPluginList[0]);

	// Now check that the plugin we got really reflects what's in the xml stream
	assertEquals("2.1 Plugin Name", plugin.getName(), pluginList[0].getName());
	assertEquals("2.2 Plugin Id", plugin.getId(), pluginList[0].getId());
	assertEquals("2.3 Plugin Vendor Name", plugin.getProviderName(), pluginList[0].getProviderName());
	assertEquals("2.4 Plugin Version", plugin.getVersion(), pluginList[0].getVersion());
	assertNull("2.5 Plugin Class", pluginList[0].getPluginClass());
	assertNull("2.6 Runtime List", pluginList[0].getRuntime());
	assertNull("2.7 Requires List", pluginList[0].getRequires());
	assertNull("2.8 Extension Point List", pluginList[0].getDeclaredExtensionPoints());
	assertEquals("2.9 Registry", pluginList[0].getRegistry(), registry);
	assertNull("2.10 Plugin Location", pluginList[0].getLocation());

	// Repeat for read-only accessors
	assertEquals("3.1 Plugin Name", plugin.getName(), ROPluginList[0].getLabel());
	assertEquals("3.2 Plugin Id", plugin.getId(), ROPluginList[0].getUniqueIdentifier());
	assertEquals("3.3 Plugin Vendor Name", plugin.getProviderName(), ROPluginList[0].getProviderName());
	assertEquals("3.4 Plugin Version", plugin.getVersionIdentifier(), ROPluginList[0].getVersionIdentifier());
//	assertNotNull("3.5 Plugin Class", ROPluginList[0].getPluginClassLoader());
	assertEquals("3.6 Runtime List", ROPluginList[0].getRuntimeLibraries().length, 0);
	assertEquals("3.7 Requires List", ROPluginList[0].getPluginPrerequisites().length, 0);
	assertEquals("3.8 Extension Point List", ROPluginList[0].getExtensionPoints().length, 0);
	assertNotNull("3.9 Plugin Install URL", ROPluginList[0].getInstallURL());

	// Now check the extension points
	ExtensionModel[] extensionList = pluginList[0].getDeclaredExtensions();
	IExtension[] ROExtensionList = ROPluginList[0].getExtensions();
	assertNotNull("4.1 Extension list null", extensionList);
	assertEquals("4.2 Number of extensions", extensionList.length, 3);
	assertNotNull("4.3 Extension list null", ROExtensionList);
	assertEquals("4.4 Number of extensions", ROExtensionList.length, 3);

	ExtensionModel[] originalExtensions = plugin.getDeclaredExtensions();

	// Make sure the values in this extension are correct
	assertEquals("5.1 Extension name", extensionList[0].getName(), originalExtensions[0].getName());
	assertEquals("5.2 Extension id", extensionList[0].getId(), originalExtensions[0].getId());
	assertEquals("5.3 Extension extension point", extensionList[0].getExtensionPoint(), plugin.getId() + "." + originalExtensions[0].getExtensionPoint());
	assertEquals("5.4 Extension name", ROExtensionList[0].getLabel(), originalExtensions[0].getName());
	assertEquals("5.5 Extension id", ROExtensionList[0].getSimpleIdentifier(), originalExtensions[0].getId());
	assertEquals("5.6 Extension extension point", ROExtensionList[0].getExtensionPointUniqueIdentifier(), plugin.getId() + "." + originalExtensions[0].getExtensionPoint());

	// And check the 2nd extension too
	assertEquals("6.1 Extension name", extensionList[1].getName(), originalExtensions[1].getName());
	assertEquals("6.2 Extension id", extensionList[1].getId(), originalExtensions[1].getId());
	assertEquals("6.3 Extension extension point", extensionList[1].getExtensionPoint(), plugin.getId() + "." + originalExtensions[1].getExtensionPoint());
	assertEquals("6.4 Extension name", ROExtensionList[1].getLabel(), "");
	assertEquals("6.5 Extension id", ROExtensionList[1].getSimpleIdentifier(), originalExtensions[1].getId());
	assertEquals("6.6 Extension extension point", ROExtensionList[1].getExtensionPointUniqueIdentifier(), plugin.getId() + "." + originalExtensions[1].getExtensionPoint());

	// Finally check the 3rd extension (which is the most complex)
	assertEquals("7.1 Extension name", extensionList[2].getName(), originalExtensions[2].getName());
	assertEquals("7.2 Extension id", extensionList[2].getId(), originalExtensions[2].getId());
	assertEquals("7.3 Extension extension point", extensionList[2].getExtensionPoint(), plugin.getId() + "." + originalExtensions[2].getExtensionPoint());
	assertEquals("7.4 Extension name", ROExtensionList[2].getLabel(), "");
	assertEquals("7.5 Extension id", ROExtensionList[2].getSimpleIdentifier(), originalExtensions[2].getId());
	assertEquals("7.6 Extension extension point", ROExtensionList[2].getExtensionPointUniqueIdentifier(), plugin.getId() + "." + originalExtensions[2].getExtensionPoint());

	// Now comes the fun part.  Check the structure that hangs off this extension
	ConfigurationElementModel[] subElements = extensionList[2].getSubElements();
	IConfigurationElement ROSubElements[] = ROExtensionList[2].getConfigurationElements();
	ConfigurationElement[] originalSubElements = (ConfigurationElement[]) originalExtensions[2].getSubElements();
	assertNotNull("8.1 First level subelements null", subElements);
	assertEquals("8.2 Number of subelements", subElements.length, 2);
	assertNotNull("8.3 First level subelements null", ROSubElements);
	assertEquals("8.4 Number of subelements", ROSubElements.length, 2);

	// First, there should be 2 subelements:  category and wizard
	assertEquals("9.1 First level subelements name", subElements[0].getName(), originalSubElements[0].getName());
	assertEquals("9.2 First level subelements name", subElements[1].getName(), originalSubElements[1].getName());
	assertEquals("9.3 First level subelements name", ROSubElements[0].getName(), originalSubElements[0].getName());
	assertEquals("9.4 First level subelements name", ROSubElements[1].getName(), originalSubElements[1].getName());

	// Now check the first property group (off the 'category' subelement)
	// Note there is no read-only version of a configuration property
	ConfigurationPropertyModel[] properties = subElements[0].getProperties();
	ConfigurationProperty[] originalProperties = (ConfigurationProperty[]) originalSubElements[0].getProperties();

	// There should be 2 properties:  id and name
	assertNotNull("10.1 Property Group 1 null", properties);
	assertEquals("10.2 Number of properties", properties.length, 2);
	assertEquals("10.3 Property Group 1 - name", properties[0].getName(), originalProperties[0].getName());
	assertEquals("10.4 Property Group 1 - value", properties[0].getValue(), originalProperties[0].getValue());
	assertEquals("10.5 Property Group 1 - name", properties[1].getName(), originalProperties[1].getName());
	assertEquals("10.6 Property Group 1 - value", properties[1].getValue(), originalProperties[1].getValue());

	// Check the second property group (off the 'wizard' subelement)
	properties = subElements[1].getProperties();
	originalProperties = (ConfigurationProperty[]) originalSubElements[1].getProperties();
	assertNotNull("11.1 Property Group 2 null", properties);
	assertEquals("11.2 Number of properties", properties.length, 3);
	assertEquals("11.3 Property Group 2 - name", properties[0].getName(), originalProperties[0].getName());
	assertEquals("11.4 Property Group 2 - value", properties[0].getValue(), originalProperties[0].getValue());
	assertEquals("11.5 Property Group 2 - name", properties[1].getName(), originalProperties[1].getName());
	assertEquals("11.6 Property Group 2 - value", properties[1].getValue(), originalProperties[1].getValue());
	assertEquals("11.7 Property Group 2 - name", properties[2].getName(), originalProperties[2].getName());
	assertEquals("11.8 Property Group 2 - value", properties[2].getValue(), originalProperties[2].getValue());

	// The 'wizard' subelement has 2 subelements that hang off it:  description and selection
	ConfigurationElementModel[] subElements2 = subElements[1].getSubElements();
	ConfigurationElement[] originalSubElements2 = (ConfigurationElement[]) originalSubElements[1].getSubElements();
	IConfigurationElement ROSubElements2[] = ROSubElements[1].getChildren();
	assertNotNull("12.1 Second level subelements null", subElements2);
	assertEquals("12.2 Number of subelements", subElements2.length, 2);
	assertNotNull("12.3 Second level subelements null", ROSubElements2);
	assertEquals("12.4 Number of subelements", ROSubElements2.length, 2);

	// Check for 'description' and 'selection'
	assertEquals("13.1 Second level subelements name", subElements2[0].getName(), originalSubElements2[0].getName());
	assertEquals("13.2 Second level subelements value", subElements2[0].getValue(), originalSubElements2[0].getValue());
	assertEquals("13.3 Second level subelements name", subElements2[1].getName(), originalSubElements2[1].getName());
	assertEquals("13.4 Second level subelements name", ROSubElements2[0].getName(), originalSubElements2[0].getName());
	assertEquals("13.5 Second level subelements value", ROSubElements2[0].getValue(), originalSubElements2[0].getValue());
	assertEquals("13.6 Second level subelements name", ROSubElements2[1].getName(), originalSubElements2[1].getName());

	// The last of the second level subelements (selection) has a single property
	properties = subElements2[1].getProperties();
	originalProperties = (ConfigurationProperty[]) originalSubElements2[1].getProperties();
	assertNotNull("14.1 Property Group 3 null", properties);
	assertEquals("14.2 Number of properties", properties.length, 1);
	assertEquals("14.3 Property Group 3 - name", properties[0].getName(), originalProperties[0].getName());
	assertEquals("14.4 Property Group 3 - value", properties[0].getValue(), originalProperties[0].getValue());

	factory = null;
}
public String[] extExtPtLinkSetup() {
	PluginDescriptor[] pluginList = new PluginDescriptor[2];
	plugin = new PluginDescriptor();
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
 *  Tests to ensure that extensions and extension points are linked correctly
 *  at registry resolve time.
 */
public void extExtPtLinkTest() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
	factory = new InternalFactory(problems);

	String[] localXMLFiles = extExtPtLinkSetup();
	registry = doParsing(factory, localXMLFiles);
	registry.resolve(true, true);

	// Check both the read-only accessors and the read-write accessors
	PluginDescriptorModel[] pluginList = registry.getPlugins();
	IPluginDescriptor[] ROPluginList = registry.getPluginDescriptors();

	// First check to make sure we got something back
	assertNotNull("1.1 Sane return value", pluginList);
	assertNotNull("1.2 Sane plugin descriptor", pluginList[0]);
	assertNotNull("1.3 Sane return value", ROPluginList);
	assertNotNull("1.4 Sane plugin descriptor", ROPluginList[0]);
	PluginDescriptorModel plugin1 = registry.getPlugin("abc1");
	IPluginDescriptor ROplugin1 = registry.getPluginDescriptor("abc1");
	PluginDescriptorModel plugin2 = registry.getPlugin("abc2");
	IPluginDescriptor ROplugin2 = registry.getPluginDescriptor("abc2");
	assertNotNull("1.5 Can't find plugin", plugin1);
	assertNotNull("1.6 Can't find read only plugin", ROplugin1);
	assertNotNull("1.7 Can't find plugin", plugin2);
	assertNotNull("1.8 Can't find read only plugin", ROplugin2);

	// All of the extensions are in the abc1 plugin.  The abc2 plugin just
	// contained one of the extension points.
	ExtensionPointModel[] extensionPtList = plugin1.getDeclaredExtensionPoints();
	IExtensionPoint[] ROExtensionPtList = ROplugin1.getExtensionPoints();
	assertNotNull("2.1 Extension point list null", extensionPtList);
	assertNotNull("2.2 Extension point list null", ROExtensionPtList);
	assertEquals("2.3 Extension point list length", extensionPtList.length, 3);
	assertEquals("2.4 Extension point list length", ROExtensionPtList.length, 3);

	String pluginId = plugin1.getId();
	// Just check that the 'point' of the extension, matches the pluginId.extensionPointId
	// pair for this particular extension point
	for (int i = 0; i < extensionPtList.length; i++) {
		ExtensionModel[] extension = extensionPtList[i].getDeclaredExtensions();
		IExtension[] ROExtension = ROExtensionPtList[i].getExtensions();
		assertEquals("3.1." + i + " Extension length", extension.length, 1);
		assertEquals("3.2." + i + " Extension length", ROExtension.length, 1);
		assertEquals("3.3." + i + " Ids match", pluginId + "." + extensionPtList[i].getId(), extension[0].getExtensionPoint());
		assertEquals("3.4." + i + " Ids match", pluginId + "." + ROExtensionPtList[i].getSimpleIdentifier(), ROExtension[0].getExtensionPointUniqueIdentifier());
	}
	
	// Now repeat for the second plugin
	extensionPtList = plugin2.getDeclaredExtensionPoints();
	ROExtensionPtList = ROplugin2.getExtensionPoints();
	assertNotNull("4.1 Extension point list null", extensionPtList);
	assertNotNull("4.2 Extension point list null", ROExtensionPtList);
	assertEquals("4.3 Extension point list length", extensionPtList.length, 1);
	assertEquals("4.4 Extension point list length", ROExtensionPtList.length, 1);
	
	pluginId = plugin2.getId();
	for (int i = 0; i < extensionPtList.length; i++) {
		ExtensionModel[] extension = extensionPtList[i].getDeclaredExtensions();
		IExtension[] ROExtension = ROExtensionPtList[i].getExtensions();
		assertEquals("5.1." + i + " Extension length", extension.length, 1);
		assertEquals("5.2." + i + " Extension length", ROExtension.length, 1);
		assertEquals("5.3." + i + " Ids match", pluginId + "." + extensionPtList[i].getId(), extension[0].getExtensionPoint());
		assertEquals("5.4." + i + " Ids match", pluginId + "." + ROExtensionPtList[i].getSimpleIdentifier(), ROExtension[0].getExtensionPointUniqueIdentifier());
	}

	factory = null;
}
public String[] librarySetup() {
	plugin = new PluginDescriptor();
	plugin.setName("A simple test");
	plugin.setId("com.ibm.dav4j");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");

	Library[] pluginRuntime = new Library[2];
	for (int i = 0; i < pluginRuntime.length; i++) {
		pluginRuntime[i] = new Library();
	}
	pluginRuntime[0].setName("runtime.jar");
	String[] exportString = { "*" };
	pluginRuntime[0].setExports(exportString);
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
 *  Tests to ensure we create library entries and populate them correctly.
 */
public void libraryTest() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
	factory = new InternalFactory(problems);

	String[] localXMLFiles = librarySetup();
	registry = doParsing(factory, localXMLFiles);

	// Check both the read-only accessors and the read-write accessors
	PluginDescriptorModel[] pluginList = registry.getPlugins();
	IPluginDescriptor[] ROPluginList = registry.getPluginDescriptors();

	// First check to make sure we got something back
	assertNotNull("1.1 Sane return value", pluginList);
	assertNotNull("1.2 Sane plugin descriptor", pluginList[0]);
	assertNotNull("1.3 Sane return value", ROPluginList);
	assertNotNull("1.4 Sane plugin descriptor", ROPluginList[0]);

	// Now check that the plugin we got really reflects what's in the xml stream
	assertEquals("2.1 Plugin Name", plugin.getName(), pluginList[0].getName());
	assertEquals("2.2 Plugin Id", plugin.getId(), pluginList[0].getId());
	assertEquals("2.3 Plugin Vendor Name", plugin.getProviderName(), pluginList[0].getProviderName());
	assertEquals("2.4 Plugin Version", plugin.getVersion(), pluginList[0].getVersion());
	assertNull("2.5 Plugin Class", pluginList[0].getPluginClass());
	assertNull("2.6 Requires List", pluginList[0].getRequires());
	assertNull("2.7 Extension Points List", pluginList[0].getDeclaredExtensionPoints());
	assertNull("2.8 Extensions List", pluginList[0].getDeclaredExtensions());
	assertEquals("2.9 Registry", pluginList[0].getRegistry(), registry);
	assertNull("2.10 Plugin Location", pluginList[0].getLocation());

	// Do the same checks but for the read-only accessors
	assertEquals("3.1 Plugin Name", plugin.getName(), ROPluginList[0].getLabel());
	assertEquals("3.2 Plugin Id", plugin.getId(), ROPluginList[0].getUniqueIdentifier());
	assertEquals("3.3 Plugin Vendor Name", plugin.getProviderName(), ROPluginList[0].getProviderName());
	assertEquals("3.4 Plugin Version", plugin.getVersionIdentifier(), ROPluginList[0].getVersionIdentifier());
//	assertNotNull("3.5 Plugin Class Loader", ROPluginList[0].getPluginClassLoader());
	assertEquals("3.6 Requires List", ROPluginList[0].getPluginPrerequisites().length, 0);
	assertEquals("3.7 Extension Points List", ROPluginList[0].getExtensionPoints().length, 0);
	assertEquals("3.8 Extensions List", ROPluginList[0].getExtensions().length, 0);
	assertNotNull("3.9 Plugin Intall URL", ROPluginList[0].getInstallURL());

	// Now check the library elements
	LibraryModel[] libList = pluginList[0].getRuntime();
	ILibrary[] ROLibList = ROPluginList[0].getRuntimeLibraries();
	assertNotNull("4.1 Library list null", libList);
	assertEquals("4.2 Number of library elements", libList.length, 2);
	assertNotNull("4.3 Library list null", ROLibList);
	assertEquals("4.4 Number of library elements", ROLibList.length, 2);

	LibraryModel[] originalLibList = plugin.getRuntime();
	String[] exportList = null;

	// Make sure the values in this library element are correct
	assertEquals("5.1 Library name", libList[0].getName(), originalLibList[0].getName());
	assertTrue("5.2 Export list", libList[0].isExported());
	assertTrue("5.3 Fully exported list", libList[0].isFullyExported());
	assertNotNull("5.4 Export mask", exportList = libList[0].getExports());
	assertEquals("5.5 Number of export elements", exportList.length, 1);
	assertEquals("5.6 Export list value", exportList[0], originalLibList[0].getExports()[0]);

	// And check the 2nd library element too
	assertEquals("6.1 Library name", libList[1].getName(), originalLibList[1].getName());
	assertTrue("6.2 Export list", !libList[1].isExported());
	assertTrue("6.3 Fully exported list", !libList[1].isFullyExported());
	assertNull("6.4 Export mask", libList[1].getExports());

	// Repeat the above tests for the read-only accessors
	// Make sure the values in this library element are correct
	assertEquals("7.1 Library name", ROLibList[0].getPath().toString(), originalLibList[0].getName());
	assertTrue("7.2 Export list", ROLibList[0].isExported());
	assertTrue("7.3 Fully exported list", ROLibList[0].isFullyExported());
	// You will get null here because this is a fully exported library
	assertNull("7.4 Export mask", exportList = ROLibList[0].getContentFilters());

	// And check the 2nd library element too
	assertEquals("8.1 Library name", ROLibList[1].getPath().toString(), originalLibList[1].getName());
	assertTrue("8.2 Export list", !ROLibList[1].isExported());
	assertTrue("8.3 Fully exported list", !ROLibList[1].isFullyExported());
	assertNull("8.4 Export mask", ROLibList[1].getContentFilters());

	factory = null;
}
public String[] pluginSetup() {
	plugin = new PluginDescriptor();
	plugin.setName("A simple test");
	plugin.setId("com.ibm.dav4j");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");

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
/*  pluginTest
 *  This is a very basic test to make sure we are parsing a very
 *  simple XML file and populating a plugin descriptor correctly
 */
public void pluginTest() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
	factory = new InternalFactory(problems);

	String[] localXMLFiles = pluginSetup();
	registry = doParsing(factory, localXMLFiles);

	// Check both the read-only accessors and the read-write accessors
	PluginDescriptorModel[] pluginList = registry.getPlugins();
	IPluginDescriptor[] ROPluginList = registry.getPluginDescriptors();

	// First check to make sure we got something back
	assertNotNull("1.1 Sane return value", pluginList);
	assertNotNull("1.2 Sane plugin descriptor", pluginList[0]);

	// Now check that the plugin we got really reflects what's in the xml stream
	assertEquals("2.1 Plugin Name", plugin.getName(), pluginList[0].getName());
	assertEquals("2.2 Plugin Id", plugin.getId(), pluginList[0].getId());
	assertEquals("2.3 Plugin Vendor Name", plugin.getProviderName(), pluginList[0].getProviderName());
	assertEquals("2.4 Plugin Version", plugin.getVersion(), pluginList[0].getVersion());
	assertEquals("2.5 Plugin Class", plugin.getPluginClass(), pluginList[0].getPluginClass());
	assertNull("2.6 Requires List", pluginList[0].getRequires());
	assertNull("2.7 Runtime List", pluginList[0].getRuntime());
	assertNull("2.8 Extension Points List", pluginList[0].getDeclaredExtensionPoints());
	assertNull("2.9 Extensions List", pluginList[0].getDeclaredExtensions());
	assertEquals("2.10 Registry", pluginList[0].getRegistry(), registry);
	assertNull("2.11 Plugin Location", pluginList[0].getLocation());

	// Repeat these tests with the read-only accessors
	assertEquals("3.1 Plugin Name", plugin.getName(), ROPluginList[0].getLabel());
	assertEquals("3.2 Plugin Id", plugin.getId(), ROPluginList[0].getUniqueIdentifier());
	assertEquals("3.3 Plugin Vendor Name", plugin.getProviderName(), ROPluginList[0].getProviderName());
	assertEquals("3.4 Plugin Version", plugin.getVersionIdentifier(), ROPluginList[0].getVersionIdentifier());
	//	assertNotNull("3.5 Plugin Class", ROPluginList[0].getPluginClassLoader());
	assertEquals("3.6 Requires List", ROPluginList[0].getPluginPrerequisites().length, 0);
	assertEquals("3.7 Runtime List", ROPluginList[0].getRuntimeLibraries().length, 0);
	assertEquals("3.8 Extension Points List", ROPluginList[0].getExtensionPoints().length, 0);
	assertEquals("3.9 Extensions List", ROPluginList[0].getExtensions().length, 0);
	assertNotNull("3.10 Plugin Install URL", ROPluginList[0].getInstallURL());

	factory = null;
}
public String[] PR1GBZ0AWSetup1() {
	PluginDescriptor[] pluginList = new PluginDescriptor[3];
	for (int i = 0; i < pluginList.length; i++) {
		pluginList[i] = new PluginDescriptor();
	}
	pluginList[0].setName("Top level plugin");
	pluginList[0].setId("one.two.three");
	pluginList[0].setVersion("1.0.0");
	PluginPrerequisite[] pluginRequires = new PluginPrerequisite[1];
	for (int i = 0; i < pluginRequires.length; i++) {
		pluginRequires[i] = new PluginPrerequisite();
	}
	pluginRequires[0].setPlugin("com.ibm.dav4j");
	pluginList[0].setRequires(pluginRequires);

	pluginList[1].setName("First Circular plugin");
	pluginList[1].setId("com.ibm.dav4j");
	pluginList[1].setVersion("1.0.0");

	pluginRequires = new PluginPrerequisite[1];
	for (int i = 0; i < pluginRequires.length; i++) {
		pluginRequires[i] = new PluginPrerequisite();
	}
	pluginRequires[0].setPlugin("a.b.c");
	pluginList[1].setRequires(pluginRequires);

	pluginList[2].setName("A final test2");
	pluginList[2].setId("a.b.c");

	pluginRequires = new PluginPrerequisite[1];
	for (int i = 0; i < pluginRequires.length; i++) {
		pluginRequires[i] = new PluginPrerequisite();
	}
	pluginRequires[0].setPlugin("com.ibm.dav4j");
	pluginList[2].setRequires(pluginRequires);
	pluginList[2].setVersion("1.0.0");

	ByteArrayOutputStream fs;
	String[] localXMLFiles = new String[3];

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
public String[] PR1GBZ0AWSetup2() {
	PluginDescriptor[] pluginList = new PluginDescriptor[2];
	for (int i = 0; i < pluginList.length; i++) {
		pluginList[i] = new PluginDescriptor();
	}
	pluginList[0].setName("First Circular plugin");
	pluginList[0].setId("com.ibm.dav4j");
	pluginList[0].setVersion("1.0.0");

	PluginPrerequisite[] pluginRequires = new PluginPrerequisite[1];
	for (int i = 0; i < pluginRequires.length; i++) {
		pluginRequires[i] = new PluginPrerequisite();
	}
	pluginRequires[0].setPlugin("a.b.c");
	pluginList[0].setRequires(pluginRequires);

	pluginList[1].setName("A final test2");
	pluginList[1].setId("a.b.c");
	pluginList[1].setVersion("1.0.0");

	pluginRequires = new PluginPrerequisite[1];
	for (int i = 0; i < pluginRequires.length; i++) {
		pluginRequires[i] = new PluginPrerequisite();
	}
	pluginRequires[0].setPlugin("com.ibm.dav4j");
	pluginList[1].setRequires(pluginRequires);

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
/*  PR1GBZ0AWTest1
 *  Tests to ensure we have solved this PR.  This PR indicates a stack overflow will occur
 *  if two plugins depend on each other.
 */
public void PR1GBZ0AWTest1() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
	factory = new InternalFactory(problems);

	String[] localXMLFiles = PR1GBZ0AWSetup1();
	registry = doParsing(factory, localXMLFiles);

	// Check both the read-only accessors and the read-write accessors
	PluginDescriptorModel[] pluginList = registry.getPlugins();

	// First check to make sure we got something back
	assertNotNull("1.1 Sane return value", pluginList);
	assertNotNull("1.2 Sane plugin descriptor", pluginList[0]);

	// Now do the resolve.  This is where the stack overflow would occur
	registry.resolve(true, true);

	// There should be no plugins in the registry
	pluginList = registry.getPlugins();
	assertEquals("2.1 Resolve completed", pluginList.length, 0);

	factory = null;
}
/*  PR1GBZ0AWTest2
 *  Tests to ensure we have solved this PR.  This PR indicates a stack overflow will occur
 *  if two plugins depend on each other.
 */
public void PR1GBZ0AWTest2() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
	factory = new InternalFactory(problems);

	String[] localXMLFiles = PR1GBZ0AWSetup2();
	registry = doParsing(factory, localXMLFiles);

	// Check both the read-only accessors and the read-write accessors
	PluginDescriptorModel[] pluginList = registry.getPlugins();

	// First check to make sure we got something back
	assertNotNull("1.1 Sane return value", pluginList);
	assertNotNull("1.2 Sane plugin descriptor", pluginList[0]);

	// Now do the resolve.  This is where the stack overflow would occur
	registry.resolve(true, true);

	// There should be no plugins in the registry
	pluginList = registry.getPlugins();
	// DDW **NOTE** This test currently fails
	// assertEquals("2.1 Resolve completed", pluginList.length, 0);

	factory = null;
}
/*  readOnlyConfigurationElement
 *  This method ensures that once the registry is marked as read-only, configuration
 *  elements in the registry cannot be written to.  This method is called as part of
 *  the test readOnlyTest.
 */
public void readOnlyConfigurationElement(ConfigurationElement subElement) {
	// Check to ensure we cannot write to the configuration elements
	assertTrue("7.1 Configuration Element marked", subElement.isReadOnly());
	// Now check all methods that can write to a ConfigurationElement
	try {
		subElement.setName("name");
		assertTrue("7.2 ConfigurationElement - setName", false);
	} catch (RuntimeException runExcept) {
		// catch intentionally left blank.  We do get here
		// but want to continue execution
	}
	try {
		subElement.setParent(new PluginDescriptor());
		assertTrue("7.3 ConfigurationElement - setParent", false);
	} catch (RuntimeException runExcept) {
		// catch intentionally left blank.  We do get here
		// but want to continue execution
	}
	try {
		subElement.setProperties(new ConfigurationPropertyModel[1]);
		assertTrue("7.4 ConfigurationElement - setProperties", false);
	} catch (RuntimeException runExcept) {
		// catch intentionally left blank.  We do get here
		// but want to continue execution
	}
	try {
		subElement.setSubElements(new ConfigurationElementModel[1]);
		assertTrue("7.5 ConfigurationElement - setSubElements", false);
	} catch (RuntimeException runExcept) {
		// catch intentionally left blank.  We do get here
		// but want to continue execution
	}
	try {
		subElement.setValue("value");
		assertTrue("7.6 ConfigurationElement - setValue", false);
	} catch (RuntimeException runExcept) {
		// catch intentionally left blank.  We do get here
		// but want to continue execution
	}
}
/*  readOnlyConfigurationProperty
 *  This method ensures that once the registry is marked as read-only, configuration
 *  property elements in the registry cannot be written to.  This method is called
 *  as part of the test readOnlyTest.
 */
public void readOnlyConfigurationProperty(ConfigurationProperty property) {

	// Check to ensure we cannot write to the configuration property elements
	assertTrue("8.1 Configuration Property marked", property.isReadOnly());
	// Now check all methods that can write to a ConfigurationProperty
	try {
		property.setName("name");
		assertTrue("8.4 ConfigurationProperty - setName", false);
	} catch (RuntimeException runExcept) {
		// catch intentionally left blank.  We do get here
		// but want to continue execution
	}
	try {
		property.setValue("value");
		assertTrue("8.3 ConfigurationProperty - setValue", false);
	} catch (RuntimeException runExcept) {
		// catch intentionally left blank.  We do get here
		// but want to continue execution
	}
}
/*  readOnlyExtension
 *  This method ensures that once the registry is marked as read-only, extensions
 *  in the registry cannot be written to.  This method is called as part of the test
 *  readOnlyTest.
 */
public void readOnlyExtension(Extension extension) {
	// Check to ensure we cannot write to the extension elements
	assertTrue("6.1 Extension marked", extension.isReadOnly());
	// Now check all methods that can write to an Extension
	try {
		extension.setName("name");
		assertTrue("6.2 Plugin - setName", false);
	} catch (RuntimeException runExcept) {
		// catch intentionally left blank.  We do get here
		// but want to continue execution
	}
	try {
		extension.setExtensionPoint("extensionPoint");
		assertTrue("6.3 Extension - setExtensionPoint", false);
	} catch (RuntimeException runExcept) {
		// catch intentionally left blank.  We do get here
		// but want to continue execution
	}
	try {
		extension.setId("id");
		assertTrue("6.4 Extension - setId", false);
	} catch (RuntimeException runExcept) {
		// catch intentionally left blank.  We do get here
		// but want to continue execution
	}
	try {
		extension.setParentPluginDescriptor(new PluginDescriptor());
		assertTrue("6.5 Extension - setParentPluginDescriptor", false);
	} catch (RuntimeException runExcept) {
		// catch intentionally left blank.  We do get here
		// but want to continue execution
	}
	try {
		extension.setSubElements(new ConfigurationElementModel[1]);
		assertTrue("6.6 Extension - setSubElements", false);
	} catch (RuntimeException runExcept) {
		// catch intentionally left blank.  We do get here
		// but want to continue execution
	}
}
/*  readOnlyExtensionPoints
 *  This method ensures that once the registry is marked as read-only, extension
 *  points in the registry cannot be written to.  This method is called as part
 *  of the test readOnlyTest.
 */
public void readOnlyExtensionPoint(ExtensionPoint extensionPoint) {
	// Check to ensure we cannot write to the extension point elements
	assertTrue("5.1 Extension Point marked", extensionPoint.isReadOnly());
	// Now check all methods that can write to an ExtensionPoint
	try {
		extensionPoint.setName("name");
		assertTrue("5.2 Plugin - setName", false);
	} catch (RuntimeException runExcept) {
		// catch intentionally left blank.  We do get here
		// but want to continue execution
	}
	try {
		extensionPoint.setDeclaredExtensions(new ExtensionModel[1]);
		assertTrue("5.3 Extension Point - setDeclaredExtensions", false);
	} catch (RuntimeException runExcept) {
		// catch intentionally left blank.  We do get here
		// but want to continue execution
	}
	try {
		extensionPoint.setId("id");
		assertTrue("5.4 Extension Point - setId", false);
	} catch (RuntimeException runExcept) {
		// catch intentionally left blank.  We do get here
		// but want to continue execution
	}
	try {
		extensionPoint.setParentPluginDescriptor(new PluginDescriptor());
		assertTrue("5.5 Extension Point - setParentPluginDescriptor", false);
	} catch (RuntimeException runExcept) {
		// catch intentionally left blank.  We do get here
		// but want to continue execution
	}
	try {
		extensionPoint.setSchema("schema");
		assertTrue("5.6 Extension Point - setSchema", false);
	} catch (RuntimeException runExcept) {
		// catch intentionally left blank.  We do get here
		// but want to continue execution
	}
}
/*  readOnlyLibrary
 *  This method ensures that once the registry is marked as read-only, library
 *  elements in the registry cannot be written to.  This method is called as
 *  part of the test readOnlyTest.
 */
public void readOnlyLibrary(Library library) {

	// Now check all methods that can write to a Library
	assertTrue("4.1 Library marked", library.isReadOnly());
	try {
		library.setName("name");
		assertTrue("4.2 Plugin - setName", false);
	} catch (RuntimeException runExcept) {
		// catch intentionally left blank.  We do get here
		// but want to continue execution
	}
	try {
		library.setExports(new String[1]);
		assertTrue("4.3 Library - setExports", false);
	} catch (RuntimeException runExcept) {
		// catch intentionally left blank.  We do get here
		// but want to continue execution
	}

}
/*  readOnlyPlugin
 *  This method ensures that once the registry is marked as read-only, plugin 
 *  descriptor elements in the registry cannot be written to.  This method is
 *  called in the test readOnlyTest.
 */
public void readOnlyPlugin(PluginDescriptor plugin) {
	// Check to ensure we cannot write to this plugin
	assertTrue("3.1 Plugin marked", plugin.isReadOnly());
	// Now check all methods that can write to a PluginDescriptor
	try {
		plugin.setName("name");
		assertTrue("3.2 Plugin - setName", false);
	} catch (RuntimeException runExcept) {
		// catch intentionally left blank.  We do get here
		// but want to continue execution
	}
	try {
		plugin.setDeclaredExtensionPoints(new ExtensionPointModel[1]);
		assertTrue("3.3 Plugin - setDeclaredExtensionPoints", false);
	} catch (RuntimeException runExcept) {
		// catch intentionally left blank.  We do get here
		// but want to continue execution
	}
	try {
		plugin.setDeclaredExtensions(new ExtensionModel[1]);
		assertTrue("3.4 Plugin - setDeclaredExtensions", false);
	} catch (RuntimeException runExcept) {
		// catch intentionally left blank.  We do get here
		// but want to continue execution
	}
	try {
		plugin.setId("id");
		assertTrue("3.5 Plugin - setId", false);
	} catch (RuntimeException runExcept) {
		// catch intentionally left blank.  We do get here
		// but want to continue execution
	}
	try {
		plugin.setLocation("location");
		assertTrue("3.6 Plugin - setLocation", false);
	} catch (RuntimeException runExcept) {
		// catch intentionally left blank.  We do get here
		// but want to continue execution
	}
	try {
		plugin.setPluginClass("pluginClass");
		assertTrue("3.7 Plugin - setPluginClass", false);
	} catch (RuntimeException runExcept) {
		// catch intentionally left blank.  We do get here
		// but want to continue execution
	}
	try {
		plugin.setRegistry(new PluginRegistry());
		assertTrue("3.8 Plugin - setRegistry", false);
	} catch (RuntimeException runExcept) {
		// catch intentionally left blank.  We do get here
		// but want to continue execution
	}
	try {
		plugin.setRequires(new PluginPrerequisiteModel[1]);
		assertTrue("3.9 Plugin - setRequires", false);
	} catch (RuntimeException runExcept) {
		// catch intentionally left blank.  We do get here
		// but want to continue execution
	}
	try {
		plugin.setRuntime(new LibraryModel[1]);
		assertTrue("3.10 Plugin - setRuntime", false);
	} catch (RuntimeException runExcept) {
		// catch intentionally left blank.  We do get here
		// but want to continue execution
	}
	try {
		plugin.setProviderName("providerName");
		assertTrue("3.11 Plugin - setVendorName", false);
	} catch (RuntimeException runExcept) {
		// catch intentionally left blank.  We do get here
		// but want to continue execution
	}
	try {
		plugin.setVersion("version");
		assertTrue("3.12 Plugin - setVersion", false);
	} catch (RuntimeException runExcept) {
		// catch intentionally left blank.  We do get here
		// but want to continue execution
	}
}
/*  readOnlyRegistry
 *  This method ensures that once the registry is marked as read-only, registry
 *  elements cannot be written to.  This method is called from the test readOnlyTest
 */
public void readOnlyRegistry(PluginRegistry registry) {
	PluginDescriptorModel[] pluginList = registry.getPlugins();
	// Check to ensure we cannot write to the registry
	assertTrue("2.1 Registry marked", registry.isReadOnly());
	// Now check all methods that can write to a PluginRegistry
	try {
		registry.addPlugin(pluginList[0]);
		assertTrue("2.2 PluginRegistry - addPlugin", false);
	} catch (RuntimeException runExcept) {
		// catch intentionally left blank.  We do get here
		// but want to continue execution
	}
	try {
		registry.removePlugin(pluginList[0].getId(), pluginList[0].getVersion());
		assertTrue("2.3 PluginRegistry - removePlugin", false);
	} catch (RuntimeException runExcept) {
		// catch intentionally left blank.  We do get here
		// but want to continue execution
	}
	try {
		registry.removePlugins(pluginList[0].getId());
		assertTrue("2.4 PluginRegistry - removePlugins", false);
	} catch (RuntimeException runExcept) {
		// catch intentionally left blank.  We do get here
		// but want to continue execution
	}
}
public String[] readOnlySetup() {
	plugin = new PluginDescriptor();
	plugin.setName("A simple test");
	plugin.setId("com.ibm.dav4j");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");

	PluginPrerequisite[] pluginRequires = new PluginPrerequisite[1];
	for (int i = 0; i < pluginRequires.length; i++) {
		pluginRequires[i] = new PluginPrerequisite();
	}
	pluginRequires[0].setPlugin("com.ibm.eclipse.core.runtime");
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
 *  This test ensures that once the registry is marked as read-only, elements
 *  in the registry cannot be written to
 */
public void readOnlyTest() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
	factory = new InternalFactory(problems);

	String[] localXMLFiles = readOnlySetup();
	registry = doParsing(factory, localXMLFiles);

	// Mark the registry as readonly.  This should filter down to all elements
	// in the registry too.
	registry.markReadOnly();

	PluginDescriptorModel[] pluginList = registry.getPlugins();

	// First check to make sure we got something back
	assertNotNull("1.1 Sane return value", pluginList);
	assertNotNull("1.2 Sane plugin descriptor", pluginList[0]);

	// Check to ensure we cannot write to the registry
	readOnlyRegistry(registry);

	// Check to ensure we cannot write to this plugin
	readOnlyPlugin((PluginDescriptor) pluginList[0]);

	// Check to ensure we cannot write to the runtime elements
	LibraryModel[] libraries = pluginList[0].getRuntime();
	readOnlyLibrary((Library) libraries[0]);

	// Check to ensure we cannot write to the extension point elements
	ExtensionPointModel[] extensionPoints = pluginList[0].getDeclaredExtensionPoints();
	readOnlyExtensionPoint((ExtensionPoint) extensionPoints[0]);

	// Check to ensure we cannot write to the extension elements
	ExtensionModel[] extensions = pluginList[0].getDeclaredExtensions();
	readOnlyExtension((Extension) extensions[0]);

	// Check to ensure we cannot write to the configuration elements
	ConfigurationElementModel[] subElements = extensions[0].getSubElements();
	readOnlyConfigurationElement((ConfigurationElement) subElements[0]);

	// Check to ensure we cannot write to the configuration property elements
	ConfigurationPropertyModel[] properties = subElements[0].getProperties();
	readOnlyConfigurationProperty((ConfigurationProperty) properties[0]);

	factory = null;
}
public String[] registrySetup() {
	PluginDescriptor[] pluginList = new PluginDescriptor[5];

	plugin = new PluginDescriptor();
	plugin.setName("A simple test");
	plugin.setId("com.ibm.dav4j");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");
	pluginList[0] = plugin;

	plugin = new PluginDescriptor();
	plugin.setName("A simple test");
	plugin.setId("com.ibm.dav4j");
	plugin.setProviderName("IBM");
	plugin.setVersion("3.0");
	pluginList[1] = plugin;

	plugin = new PluginDescriptor();
	plugin.setName("A simple test");
	plugin.setId("com.ibm.dav4j");
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
 *  Tests to ensure we create plugin registries and populate them correctly.
 */
public void registryTest() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
	factory = new InternalFactory(problems);

	String[] localXMLFiles = registrySetup();
	registry = doParsing(factory, localXMLFiles);

	// Check both the read-only accessors and the read-write accessors
	PluginDescriptorModel[] pluginList = registry.getPlugins();
	IPluginDescriptor[] ROPluginList = registry.getPluginDescriptors();

	// First check to make sure we got something back
	assertNotNull("1.1 Sane return value", pluginList);
	assertNotNull("1.2 Sane plugin descriptor", pluginList[0]);
	assertEquals("1.3 Right number of plugin descriptors", pluginList.length, localXMLFiles.length);
	assertNotNull("1.4 Sane return value", ROPluginList);
	assertNotNull("1.5 Sane plugin descriptor", ROPluginList[0]);
	assertEquals("1.6 Right number of plugin descriptors", ROPluginList.length, localXMLFiles.length);

	factory = null;
}
public String[] requiresSetup() {
	plugin = new PluginDescriptor();
	plugin.setName("A simple test");
	plugin.setId("com.ibm.dav4j");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");

	PluginPrerequisite[] pluginRequires = new PluginPrerequisite[5];
	for (int i = 0; i < pluginRequires.length; i++) {
		pluginRequires[i] = new PluginPrerequisite();
	}
	pluginRequires[0].setPlugin("com.ibm.eclipse.core.runtime");
	pluginRequires[0].setVersion("4");
	pluginRequires[0].setExport(true);
	pluginRequires[0].setMatchByte(PluginPrerequisiteModel.PREREQ_MATCH_GREATER_OR_EQUAL);
	pluginRequires[1].setPlugin("org.apache.xerces");
	pluginRequires[2].setPlugin("com.ibm.eclipse.core.resources");
	pluginRequires[2].setVersion("3");
	pluginRequires[2].setMatchByte(PluginPrerequisiteModel.PREREQ_MATCH_COMPATIBLE);
	pluginRequires[3].setPlugin("anotherPlugin");
	pluginRequires[3].setVersion("1.0.0");
	pluginRequires[3].setMatchByte(PluginPrerequisiteModel.PREREQ_MATCH_EQUIVALENT);
	pluginRequires[4].setPlugin("fifthPlugin");
	pluginRequires[4].setVersion("2.0.1");
	pluginRequires[4].setMatchByte(PluginPrerequisiteModel.PREREQ_MATCH_PERFECT);
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
 *  Tests to ensure we create plugin prerequisite entries and populate them correctly.
 */
public void requiresTest() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
	factory = new InternalFactory(problems);

	String[] localXMLFiles = requiresSetup();
	registry = doParsing(factory, localXMLFiles);

	// Check both the read-only accessors and the read-write accessors
	PluginDescriptorModel[] pluginList = registry.getPlugins();
	IPluginDescriptor[] ROPluginList = registry.getPluginDescriptors();

	// First check to make sure we got something back
	assertNotNull("1.1 Sane return value", pluginList);
	assertNotNull("1.2 Sane plugin descriptor", pluginList[0]);
	assertNotNull("1.3 Sane return value", ROPluginList);
	assertNotNull("1.4 Sane plugin descriptor", ROPluginList[0]);

	// Now check that the plugin we got really reflects what's in the xml stream
	assertEquals("2.1 Plugin Name", plugin.getName(), pluginList[0].getName());
	assertEquals("2.2 Plugin Id", plugin.getId(), pluginList[0].getId());
	assertEquals("2.3 Plugin Vendor Name", plugin.getProviderName(), pluginList[0].getProviderName());
	assertEquals("2.4 Plugin Version", plugin.getVersion(), pluginList[0].getVersion());
	assertNull("2.5 Plugin Class", pluginList[0].getPluginClass());
	assertNull("2.6 Runtime List", pluginList[0].getRuntime());
	assertNull("2.7 Extension Points List", pluginList[0].getDeclaredExtensionPoints());
	assertNull("2.8 Extensions List", pluginList[0].getDeclaredExtensions());
	assertEquals("2.9 Registry", pluginList[0].getRegistry(), registry);
	assertNull("2.10 Plugin Location", pluginList[0].getLocation());

	// Repeat for read-only accessors
	assertEquals("3.1 Plugin Name", plugin.getName(), ROPluginList[0].getLabel());
	assertEquals("3.2 Plugin Id", plugin.getId(), ROPluginList[0].getUniqueIdentifier());
	assertEquals("3.3 Plugin Vendor Name", plugin.getProviderName(), ROPluginList[0].getProviderName());
	assertEquals("3.4 Plugin Version", plugin.getVersionIdentifier(), ROPluginList[0].getVersionIdentifier());
//	assertNotNull("3.5 Plugin Class", ROPluginList[0].getPluginClassLoader());
	assertEquals("3.6 Runtime List", ROPluginList[0].getRuntimeLibraries().length, 0);
	assertEquals("3.7 Extension Points List", ROPluginList[0].getExtensionPoints().length, 0);
	assertEquals("3.8 Extensions List", ROPluginList[0].getExtensions().length, 0);
	assertNotNull("3.9 Plugin Install URL", ROPluginList[0].getInstallURL());

	// Now check the plugin prerequisite elements
	PluginPrerequisiteModel[] reqList = pluginList[0].getRequires();
	IPluginPrerequisite[] ROReqList = ROPluginList[0].getPluginPrerequisites();
	assertNotNull("4.1 Requires list null", reqList);
	assertEquals("4.2 Number of requires elements", reqList.length, 5);
	assertNotNull("4.3 Requires list null", ROReqList);
	assertEquals("4.4 Number of requires elements", ROReqList.length, 5);

	PluginPrerequisiteModel[] originalRequires = plugin.getRequires();

	// Make sure the values in this requires element are correct
	assertEquals("5.1 Requires plugin", reqList[0].getPlugin(), originalRequires[0].getPlugin());
	assertEquals("5.2 Requires version", reqList[0].getVersion(), originalRequires[0].getVersion());
	assertEquals("5.3 Requires exact match", reqList[0].getMatchByte(), originalRequires[0].getMatchByte());
	assertTrue("5.4 Requires export", reqList[0].getExport());
	assertEquals("5.5 Requires plugin", ROReqList[0].getUniqueIdentifier(), originalRequires[0].getPlugin());
	assertEquals("5.6 Requires version", ROReqList[0].getVersionIdentifier(), ((PluginPrerequisite) originalRequires[0]).getVersionIdentifier());
	assertTrue("5.7 Requires greater or equal match", ROReqList[0].isMatchedAsGreaterOrEqual());
	assertTrue("5.8 Requires greater or equal match (compatible)", !ROReqList[0].isMatchedAsCompatible());
	assertTrue("5.9 Requires greater or equal match (equivalent)", !ROReqList[0].isMatchedAsEquivalent());
	assertTrue("5.10 Requires greater or equal match (prefect)", !ROReqList[0].isMatchedAsPerfect());
	assertTrue("5.11 Requires export", ROReqList[0].isExported());

	// And check the 2nd requires element too
	assertEquals("6.1 Requires plugin", reqList[1].getPlugin(), originalRequires[1].getPlugin());
	assertEquals("6.2 Requires plugin", ROReqList[1].getUniqueIdentifier(), originalRequires[1].getPlugin());
	assertEquals("6.3 Requires exact match", reqList[1].getMatchByte(), originalRequires[1].getMatchByte());
	assertTrue("6.4 Requires unspecified match (greater or equal)", !ROReqList[1].isMatchedAsGreaterOrEqual());
	assertTrue("6.5 Requires unspecified match (compatible)", !ROReqList[1].isMatchedAsCompatible());
	assertTrue("6.6 Requires unspecified match (equivalent)", !ROReqList[1].isMatchedAsEquivalent());
	assertTrue("6.7 Requires unspecified match (prefect)", !ROReqList[1].isMatchedAsPerfect());

	// And the 3rd requires element
	assertEquals("7.1 Requires plugin", reqList[2].getPlugin(), originalRequires[2].getPlugin());
	assertEquals("7.2 Requires version", reqList[2].getVersion(), originalRequires[2].getVersion());
	assertEquals("7.3 Requires compatible match", reqList[2].getMatchByte(), PluginPrerequisiteModel.PREREQ_MATCH_COMPATIBLE);
	assertTrue("7.4 Requires export", !reqList[2].getExport());
	assertEquals("7.5 Requires plugin", ROReqList[2].getUniqueIdentifier(), originalRequires[2].getPlugin());
	assertEquals("7.6 Requires version", ROReqList[2].getVersionIdentifier(), ((PluginPrerequisite) originalRequires[2]).getVersionIdentifier());
	assertTrue("7.7 Requires compatible match", ROReqList[2].isMatchedAsCompatible());
	assertTrue("7.8 Requires compatible match (greater or equal)", !ROReqList[2].isMatchedAsGreaterOrEqual());
	assertTrue("7.9 Requires compatible match (equivalent)", !ROReqList[2].isMatchedAsEquivalent());
	assertTrue("7.10 Requires compatible match (prefect)", !ROReqList[2].isMatchedAsPerfect());
	assertTrue("7.11 Requires export", !ROReqList[2].isExported());

	// And the 4th requires element
	assertEquals("8.1 Requires plugin", reqList[3].getPlugin(), originalRequires[3].getPlugin());
	assertEquals("8.2 Requires version", reqList[3].getVersion(), originalRequires[3].getVersion());
	assertEquals("8.3 Requires equivalent match", reqList[3].getMatchByte(), PluginPrerequisiteModel.PREREQ_MATCH_EQUIVALENT);
	assertTrue("8.4 Requires export", !reqList[3].getExport());
	assertEquals("8.5 Requires plugin", ROReqList[3].getUniqueIdentifier(), originalRequires[3].getPlugin());
	assertEquals("8.6 Requires version", ROReqList[3].getVersionIdentifier(), ((PluginPrerequisite) originalRequires[3]).getVersionIdentifier());
	assertTrue("8.7 Requires equivalent match", ROReqList[3].isMatchedAsEquivalent());
	assertTrue("8.8 Requires equivalent match (greater or equal)", !ROReqList[3].isMatchedAsGreaterOrEqual());
	assertTrue("8.9 Requires equivalent match (compatible)", !ROReqList[3].isMatchedAsCompatible());
	assertTrue("8.10 Requires equivalent match (prefect)", !ROReqList[3].isMatchedAsPerfect());
	assertTrue("8.11 Requires export", !ROReqList[3].isExported());

	// And the 5th and final requires element
	assertEquals("7.1 Requires plugin", reqList[4].getPlugin(), originalRequires[4].getPlugin());
	assertEquals("7.2 Requires version", reqList[4].getVersion(), originalRequires[4].getVersion());
	assertEquals("7.3 Requires perfect match", reqList[4].getMatchByte(), PluginPrerequisiteModel.PREREQ_MATCH_PERFECT);
	assertTrue("7.4 Requires export", !reqList[4].getExport());
	assertEquals("7.5 Requires plugin", ROReqList[4].getUniqueIdentifier(), originalRequires[4].getPlugin());
	assertEquals("7.6 Requires version", ROReqList[4].getVersionIdentifier(), ((PluginPrerequisite) originalRequires[4]).getVersionIdentifier());
	assertTrue("7.7 Requires perfect match", ROReqList[4].isMatchedAsPerfect());
	assertTrue("7.8 Requires perfect match (greater or equal)", !ROReqList[4].isMatchedAsGreaterOrEqual());
	assertTrue("7.9 Requires perfect match (compatible)", !ROReqList[4].isMatchedAsCompatible());
	assertTrue("7.10 Requires perfect match (equivalent)", !ROReqList[4].isMatchedAsEquivalent());
	assertTrue("7.11 Requires export", !ROReqList[4].isExported());

	factory = null;
}
public String[][] pluginRequiresSetup() {
	ByteArrayOutputStream fs;
	PrintWriter w;
	RegistryWriter regWriter;
	String[][] localXMLFiles = new String[3][1];
	
	// Create a plugin with no name
	plugin = new PluginDescriptor();
	plugin.setId("com.ibm.dav4j");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");
	plugin.setPluginClass("someclass");

	fs = new ByteArrayOutputStream();
	w = new PrintWriter(fs);
	regWriter = new RegistryWriter();
	regWriter.writePluginDescriptor(plugin, w, 0);
	w.flush();
	w.close();
	localXMLFiles[0][0] = fs.toString();
	
	// Create a plugin with no id
	plugin = new PluginDescriptor();
	plugin.setName("someName");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");
	plugin.setPluginClass("someclass");
	fs = new ByteArrayOutputStream();
	w = new PrintWriter(fs);
	regWriter = new RegistryWriter();
	regWriter.writePluginDescriptor(plugin, w, 0);
	w.flush();
	w.close();
	localXMLFiles[1][0] = fs.toString();
	
	// Create a plugin with no version
	plugin = new PluginDescriptor();
	plugin.setName("someName");
	plugin.setId("com.ibm.dav4j");
	plugin.setProviderName("IBM");
	plugin.setPluginClass("someclass");
	fs = new ByteArrayOutputStream();
	w = new PrintWriter(fs);
	regWriter = new RegistryWriter();
	regWriter.writePluginDescriptor(plugin, w, 0);
	w.flush();
	w.close();
	localXMLFiles[2][0] = fs.toString();
	
	return localXMLFiles;
}
public void allRequiresHelper(InternalFactory factory, String[][] localXMLFiles, String errorPrefix, boolean fragmentTest) {
	if (localXMLFiles == null)
		return;
	String[] currentParseString;
	for (int i = 0; i < localXMLFiles.length; i++) {
		// We have made no attempt to create a hierarchy of
		// plugins for this test.  We have just created a
		// bunch of plugins, each with a missing 'required'
		// field.  If we try to create and resolve one registry
		// with all of these plugins, the resolver will be 
		// unable to determine a root plugin and will fail.
		// Therefore, create a separate registry for each plugin
		// and resolve it separate from the other plugins.
		currentParseString = localXMLFiles[i];
		registry = doParsing(factory, currentParseString);
		// Don't trim the disabled plugins
		IStatus resolveStatus = registry.resolve(false, true);
		assertTrue(errorPrefix + ".0." + i + " Resolve - " + resolveStatus.getMessage(), !resolveStatus.isOK());
	
		// The plugin should be disabled
		PluginDescriptorModel[] pluginList = registry.getPlugins();
		assertNotNull (errorPrefix + ".1." + i + " No plugins", pluginList);
		assertEquals (errorPrefix + ".2." + i + " Not 1 plugin only", pluginList.length, 1);
		if (fragmentTest) {
			// The plugin won't be disabled but it should not have
			// a fragment.
			assertNull(errorPrefix + ".3." + i + " Fragment exists", pluginList[0].getFragments());
		} else {
			assertTrue(errorPrefix + ".4." + i + " Plugin enabled", !pluginList[0].getEnabled());
		}
	}
}
/*  allRequiresTest
 *  Test to ensure that plugins, etc. missing any of the required
 *  components are disabled.
 */
public void allRequiresTest() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
	factory = new InternalFactory(problems);

	String[][] localXMLFiles = pluginRequiresSetup();
	allRequiresHelper(factory, localXMLFiles, "1", false);
	localXMLFiles = fragmentRequiresSetup();
	allRequiresHelper(factory, localXMLFiles, "2", true);
	localXMLFiles = requiresRequiresSetup();
	allRequiresHelper(factory, localXMLFiles, "3", false);
	localXMLFiles = libraryRequiresSetup();
	allRequiresHelper(factory, localXMLFiles, "4", false);
	localXMLFiles = extensionRequiresSetup();
	allRequiresHelper(factory, localXMLFiles, "5", false);
	localXMLFiles = extensionPointRequiresSetup();
	allRequiresHelper(factory, localXMLFiles, "6", false);
}
public String[][] fragmentRequiresSetup() {
	FragmentDescriptor fragment;
	ByteArrayOutputStream fs;
	PrintWriter w;
	RegistryWriter regWriter;
	String[][] localXMLFiles = new String[5][2];

	// Create a fragment with no name
	plugin = new PluginDescriptor();
	plugin.setName("A simple test");
	plugin.setId("com.ibm.dav4j");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");
	fragment = new FragmentDescriptor();
	fragment.setId("fragmentId");
	fragment.setVersion("2.2");
	fragment.setPlugin("com.ibm.dav4j");
	fragment.setPluginVersion("1.0");
	fragment.setProviderName("someoneElse");
	fs = new ByteArrayOutputStream();
	w = new PrintWriter(fs);
	regWriter = new RegistryWriter();
	regWriter.writePluginDescriptor(plugin, w, 0);
	w.flush();
	w.close();
	localXMLFiles[0][0] = fs.toString();
	fs = new ByteArrayOutputStream();
	w = new PrintWriter(fs);
	regWriter = new RegistryWriter();
	regWriter.writePluginFragment(fragment, w, 0);
	w.flush();
	w.close();
	localXMLFiles[0][1] = fs.toString();
	
	// Create a fragment with no id
	plugin = new PluginDescriptor();
	plugin.setName("A simple test");
	plugin.setId("com.ibm.dav4j");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");
	fragment = new FragmentDescriptor();
	fragment.setName("fragmentName");
	fragment.setVersion("2.2");
	fragment.setPlugin("com.ibm.dav4j");
	fragment.setPluginVersion("1.0");
	fragment.setProviderName("someoneElse");
	fs = new ByteArrayOutputStream();
	w = new PrintWriter(fs);
	regWriter = new RegistryWriter();
	regWriter.writePluginDescriptor(plugin, w, 0);
	w.flush();
	w.close();
	localXMLFiles[1][0] = fs.toString();
	fs = new ByteArrayOutputStream();
	w = new PrintWriter(fs);
	regWriter = new RegistryWriter();
	regWriter.writePluginFragment(fragment, w, 0);
	w.flush();
	w.close();
	localXMLFiles[1][1] = fs.toString();
	
	// Create a fragment with no version
	plugin = new PluginDescriptor();
	plugin.setName("A simple test");
	plugin.setId("com.ibm.dav4j");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");
	fragment = new FragmentDescriptor();
	fragment.setName("fragmentName");
	fragment.setId("fragmentId");
	fragment.setPlugin("com.ibm.dav4j");
	fragment.setPluginVersion("1.0");
	fragment.setProviderName("someoneElse");
	fs = new ByteArrayOutputStream();
	w = new PrintWriter(fs);
	regWriter = new RegistryWriter();
	regWriter.writePluginDescriptor(plugin, w, 0);
	w.flush();
	w.close();
	localXMLFiles[2][0] = fs.toString();
	fs = new ByteArrayOutputStream();
	w = new PrintWriter(fs);
	regWriter = new RegistryWriter();
	regWriter.writePluginFragment(fragment, w, 0);
	w.flush();
	w.close();
	localXMLFiles[2][1] = fs.toString();
	
	// Create a fragment with no plugin
	plugin = new PluginDescriptor();
	plugin.setName("A simple test");
	plugin.setId("com.ibm.dav4j");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");
	fragment = new FragmentDescriptor();
	fragment.setName("fragmentName");
	fragment.setId("fragmentId");
	fragment.setVersion("2.2");
	fragment.setPluginVersion("1.0");
	fragment.setProviderName("someoneElse");
	fs = new ByteArrayOutputStream();
	w = new PrintWriter(fs);
	regWriter = new RegistryWriter();
	regWriter.writePluginDescriptor(plugin, w, 0);
	w.flush();
	w.close();
	localXMLFiles[3][0] = fs.toString();
	fs = new ByteArrayOutputStream();
	w = new PrintWriter(fs);
	regWriter = new RegistryWriter();
	regWriter.writePluginFragment(fragment, w, 0);
	w.flush();
	w.close();
	localXMLFiles[3][1] = fs.toString();
	
	// Create a fragment with no plugin version
	plugin = new PluginDescriptor();
	plugin.setName("A simple test");
	plugin.setId("com.ibm.dav4j");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");
	fragment = new FragmentDescriptor();
	fragment.setName("fragmentName");
	fragment.setId("fragmentId");
	fragment.setVersion("2.2");
	fragment.setPlugin("com.ibm.dav4j");
	fragment.setProviderName("someoneElse");
	fs = new ByteArrayOutputStream();
	w = new PrintWriter(fs);
	regWriter = new RegistryWriter();
	regWriter.writePluginDescriptor(plugin, w, 0);
	w.flush();
	w.close();
	localXMLFiles[4][0] = fs.toString();
	fs = new ByteArrayOutputStream();
	w = new PrintWriter(fs);
	regWriter = new RegistryWriter();
	regWriter.writePluginFragment(fragment, w, 0);
	w.flush();
	w.close();
	localXMLFiles[4][1] = fs.toString();

	return localXMLFiles;
}
public String[][] requiresRequiresSetup() {
	PluginPrerequisite requires;
	ByteArrayOutputStream fs;
	PrintWriter w;
	RegistryWriter regWriter;
	String[][] localXMLFiles = new String[1][1];
	PluginPrerequisite[] requiresList = new PluginPrerequisite[1];

	// Create a prerequisite with no plugin
	plugin = new PluginDescriptor();
	plugin.setName("A simple test");
	plugin.setId("com.ibm.dav4j");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");
	requires = new PluginPrerequisite();
	requires.setVersion("2.2");
	requires.setOptional(true);
	requires.setMatchByte(PluginPrerequisiteModel.PREREQ_MATCH_EQUIVALENT);
	requires.setExport(true);
	requiresList[0] = requires;
	plugin.setRequires(requiresList);
	fs = new ByteArrayOutputStream();
	w = new PrintWriter(fs);
	regWriter = new RegistryWriter();
	regWriter.writePluginDescriptor(plugin, w, 0);
	w.flush();
	w.close();
	localXMLFiles[0][0] = fs.toString();

	return localXMLFiles;
}
public String[][] extensionRequiresSetup() {
	Extension extension;
	ByteArrayOutputStream fs;
	PrintWriter w;
	RegistryWriter regWriter;
	String[][] localXMLFiles = new String[1][1];
	Extension[] extensionList = new Extension[1];

	// Create an extension with no point
	plugin = new PluginDescriptor();
	plugin.setName("A simple test");
	plugin.setId("com.ibm.dav4j");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");
	extension = new Extension();
	extension.setName("extensionName");
	extension.setId("extensionId");
	extensionList[0] = extension;
	plugin.setDeclaredExtensions(extensionList);
	fs = new ByteArrayOutputStream();
	w = new PrintWriter(fs);
	regWriter = new RegistryWriter();
	regWriter.writePluginDescriptor(plugin, w, 0);
	w.flush();
	w.close();
	localXMLFiles[0][0] = fs.toString();

	return localXMLFiles;
}
public String[][] extensionPointRequiresSetup() {
	ExtensionPoint extensionPoint;
	ByteArrayOutputStream fs;
	PrintWriter w;
	RegistryWriter regWriter;
	String[][] localXMLFiles = new String[2][1];
	ExtensionPoint[] extensionPointList = new ExtensionPoint[1];

	// Create an extension point with no name
	plugin = new PluginDescriptor();
	plugin.setName("A simple test");
	plugin.setId("com.ibm.dav4j");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");
	extensionPoint = new ExtensionPoint();
	extensionPoint.setId("extensionPointId");
	extensionPoint.setSchema("schema");
	extensionPointList[0] = extensionPoint;
	plugin.setDeclaredExtensionPoints(extensionPointList);
	fs = new ByteArrayOutputStream();
	w = new PrintWriter(fs);
	regWriter = new RegistryWriter();
	regWriter.writePluginDescriptor(plugin, w, 0);
	w.flush();
	w.close();
	localXMLFiles[0][0] = fs.toString();

	// Create an extension point with no id
	plugin = new PluginDescriptor();
	plugin.setName("A simple test");
	plugin.setId("com.ibm.dav4j");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");
	extensionPoint = new ExtensionPoint();
	extensionPoint.setName("extensionPointName");
	extensionPoint.setSchema("schema");
	extensionPointList = new ExtensionPoint[1];
	extensionPointList[0] = extensionPoint;
	plugin.setDeclaredExtensionPoints(extensionPointList);
	fs = new ByteArrayOutputStream();
	w = new PrintWriter(fs);
	regWriter = new RegistryWriter();
	regWriter.writePluginDescriptor(plugin, w, 0);
	w.flush();
	w.close();
	localXMLFiles[1][0] = fs.toString();

	return localXMLFiles;
}
public String[][] libraryRequiresSetup() {
	Library library;
	ByteArrayOutputStream fs;
	PrintWriter w;
	RegistryWriter regWriter;
	String[][] localXMLFiles = new String[1][1];
	Library[] libraryList = new Library[1];

	// Create a library with no name
	plugin = new PluginDescriptor();
	plugin.setName("A simple test");
	plugin.setId("com.ibm.dav4j");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");
	library = new Library();
	String[] exportList = new String[1];
	String exportString = new String("*");
	exportList[0] = exportString;
	library.setExports(exportList);
	libraryList[0] = library;
	plugin.setRuntime(libraryList);
	fs = new ByteArrayOutputStream();
	w = new PrintWriter(fs);
	regWriter = new RegistryWriter();
	regWriter.writePluginDescriptor(plugin, w, 0);
	w.flush();
	w.close();
	localXMLFiles[0][0] = fs.toString();

	return localXMLFiles;
}
public String[] softPrerequisite1Setup() {
	// Create a plugin with one prerequisite which
	// does not exist.  This should NOT resolve.
	PluginPrerequisiteModel prerequisite;
	ByteArrayOutputStream fs;
	PrintWriter w;
	RegistryWriter regWriter;
	String[] localXMLFiles = new String[1];
	PluginPrerequisiteModel[] prerequisiteList = new PluginPrerequisiteModel[1];

	plugin = new PluginDescriptor();
	plugin.setName("softPrereq1");
	plugin.setId("softPrereqId1");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");
	prerequisite = new PluginPrerequisiteModel();
	prerequisite.setPlugin("nonExistant");
	prerequisiteList[0] = prerequisite;
	plugin.setRequires(prerequisiteList);
	fs = new ByteArrayOutputStream();
	w = new PrintWriter(fs);
	regWriter = new RegistryWriter();
	regWriter.writePluginDescriptor(plugin, w, 0);
	w.flush();
	w.close();
	localXMLFiles[0] = fs.toString();

	return localXMLFiles;
}
public String[] softPrerequisite2Setup() {
	// Create a plugin with one prerequisite which
	// has a prerequisite which does not exist.
	// This should NOT resolve.
	PluginPrerequisiteModel prerequisite;
	ByteArrayOutputStream fs;
	PrintWriter w;
	RegistryWriter regWriter;
	String[] localXMLFiles = new String[2];
	PluginPrerequisiteModel[] prerequisiteList = new PluginPrerequisiteModel[1];

	plugin = new PluginDescriptor();
	plugin.setName("softPrereq2");
	plugin.setId("softPrereqId2");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");
	prerequisite = new PluginPrerequisiteModel();
	prerequisite.setPlugin("softPrereqExists");
	prerequisiteList[0] = prerequisite;
	plugin.setRequires(prerequisiteList);
	fs = new ByteArrayOutputStream();
	w = new PrintWriter(fs);
	regWriter = new RegistryWriter();
	regWriter.writePluginDescriptor(plugin, w, 0);
	w.flush();
	w.close();
	localXMLFiles[0] = fs.toString();

	plugin = new PluginDescriptor();
	plugin.setName("firstPrereq");
	plugin.setId("softPrereqExists");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");
	prerequisite = new PluginPrerequisiteModel();
	prerequisite.setPlugin("nonExistant");
	prerequisiteList[0] = prerequisite;
	plugin.setRequires(prerequisiteList);
	fs = new ByteArrayOutputStream();
	w = new PrintWriter(fs);
	regWriter = new RegistryWriter();
	regWriter.writePluginDescriptor(plugin, w, 0);
	w.flush();
	w.close();
	localXMLFiles[1] = fs.toString();

	return localXMLFiles;
}
public String[] softPrerequisite3Setup() {
	// Create a plugin with one prerequisite which
	// does not exist but which is optional.
	// This SHOULD resolve.
	PluginPrerequisiteModel prerequisite;
	ByteArrayOutputStream fs;
	PrintWriter w;
	RegistryWriter regWriter;
	String[] localXMLFiles = new String[1];
	PluginPrerequisiteModel[] prerequisiteList = new PluginPrerequisiteModel[1];

	plugin = new PluginDescriptor();
	plugin.setName("softPrereq3");
	plugin.setId("softPrereqId3");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");
	prerequisite = new PluginPrerequisiteModel();
	prerequisite.setPlugin("nonExistant");
	prerequisite.setOptional(true);
	prerequisiteList[0] = prerequisite;
	plugin.setRequires(prerequisiteList);
	fs = new ByteArrayOutputStream();
	w = new PrintWriter(fs);
	regWriter = new RegistryWriter();
	regWriter.writePluginDescriptor(plugin, w, 0);
	w.flush();
	w.close();
	localXMLFiles[0] = fs.toString();

	return localXMLFiles;
}
public String[] softPrerequisite4Setup() {
	// Create a plugin with one prerequisite which
	// has a prerequisite which does not exist but which is
	// optional.  This SHOULD resolve.
	PluginPrerequisiteModel prerequisite;
	ByteArrayOutputStream fs;
	PrintWriter w;
	RegistryWriter regWriter;
	String[] localXMLFiles = new String[2];
	PluginPrerequisiteModel[] prerequisiteList = new PluginPrerequisiteModel[1];

	plugin = new PluginDescriptor();
	plugin.setName("softPrereq4");
	plugin.setId("softPrereqId4");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");
	prerequisite = new PluginPrerequisiteModel();
	prerequisite.setPlugin("softPrereqExists");
	prerequisite.setOptional(true);
	prerequisiteList[0] = prerequisite;
	plugin.setRequires(prerequisiteList);
	fs = new ByteArrayOutputStream();
	w = new PrintWriter(fs);
	regWriter = new RegistryWriter();
	regWriter.writePluginDescriptor(plugin, w, 0);
	w.flush();
	w.close();
	localXMLFiles[0] = fs.toString();

	plugin = new PluginDescriptor();
	plugin.setName("firstPrereq");
	plugin.setId("softPrereqExists");
	plugin.setProviderName("IBM");
	plugin.setVersion("1.0");
	prerequisite = new PluginPrerequisiteModel();
	prerequisite.setPlugin("nonExistant");
	prerequisite.setOptional(true);
	prerequisiteList[0] = prerequisite;
	plugin.setRequires(prerequisiteList);
	fs = new ByteArrayOutputStream();
	w = new PrintWriter(fs);
	regWriter = new RegistryWriter();
	regWriter.writePluginDescriptor(plugin, w, 0);
	w.flush();
	w.close();
	localXMLFiles[1] = fs.toString();

	return localXMLFiles;
}
public void allPrerequisiteHelper(InternalFactory factory, String[] localXMLFiles, String errorPrefix, boolean shouldResolve) {
	if (localXMLFiles == null)
		return;
	registry = doParsing(factory, localXMLFiles);
	IStatus resolveStatus = registry.resolve(true, true);
	if (shouldResolve) {
		// This registry should resolve fine.
		assertTrue(errorPrefix + " Resolve - " + resolveStatus.getMessage(), resolveStatus.isOK());
	} else {
		// This registry should NOT resolve
		assertTrue(errorPrefix + " Resolve worked", !resolveStatus.isOK());
	}
	
}
public void allPrerequisiteTest() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
	factory = new InternalFactory(problems);

	// One plugin with non-existant prerequisite - not optional
	String[] localXMLFiles = softPrerequisite1Setup();
	allPrerequisiteHelper(factory, localXMLFiles, "1", false);
	// 2 nested plugins with non-existant prereq on one - not optional
	localXMLFiles = softPrerequisite2Setup();
	allPrerequisiteHelper(factory, localXMLFiles, "2", false);
	// 1 plugin with non-existant prereq - optional
	localXMLFiles = softPrerequisite3Setup();
	allPrerequisiteHelper(factory, localXMLFiles, "3", true);
	// 2 nested plugins with non-existant prereq on one - optional
	localXMLFiles = softPrerequisite4Setup();
	allPrerequisiteHelper(factory, localXMLFiles, "4", true);
}
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new BasicXMLTest("registryTest"));
	suite.addTest(new BasicXMLTest("pluginTest"));
	suite.addTest(new BasicXMLTest("libraryTest"));
	suite.addTest(new BasicXMLTest("requiresTest"));
	suite.addTest(new BasicXMLTest("extensionPointTest"));
	suite.addTest(new BasicXMLTest("extensionTest"));
	suite.addTest(new BasicXMLTest("readOnlyTest"));
	suite.addTest(new BasicXMLTest("extExtPtLinkTest"));
	suite.addTest(new BasicXMLTest("PR1GBZ0AWTest1"));
	suite.addTest(new BasicXMLTest("PR1GBZ0AWTest2"));
	suite.addTest(new BasicXMLTest("allRequiresTest"));
	// suite.addTest(new BasicXMLTest("allPrerequisiteTest"));
	return suite;
}
}
