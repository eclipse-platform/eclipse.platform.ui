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
import java.net.URL;
import junit.framework.*;
import org.xml.sax.*;

public class ExtensiveFragmentTest extends EclipseWorkspaceTest {

public ExtensiveFragmentTest() {
	super(null);
}

public ExtensiveFragmentTest(String name) {
	super(name);
}

public static Test suite() {
	TestSuite suite = new TestSuite();
	// test extension points
	suite.addTest(new ExtensiveFragmentTest("extensionPoint1Test"));
	suite.addTest(new ExtensiveFragmentTest("extensionPoint2Test"));
	suite.addTest(new ExtensiveFragmentTest("extensionPoint3Test"));
	suite.addTest(new ExtensiveFragmentTest("extensionPoint4Test"));
	suite.addTest(new ExtensiveFragmentTest("extensionPoint5Test"));
	// test extensions
	suite.addTest(new ExtensiveFragmentTest("extension1Test"));
	suite.addTest(new ExtensiveFragmentTest("extension2Test"));
	suite.addTest(new ExtensiveFragmentTest("extension3Test"));
	suite.addTest(new ExtensiveFragmentTest("extension4Test"));
	suite.addTest(new ExtensiveFragmentTest("extension5Test"));
	// test libraries
	suite.addTest(new ExtensiveFragmentTest("library1Test"));
	suite.addTest(new ExtensiveFragmentTest("library2Test"));
	suite.addTest(new ExtensiveFragmentTest("library3Test"));
	suite.addTest(new ExtensiveFragmentTest("library4Test"));
	suite.addTest(new ExtensiveFragmentTest("library5Test"));
	// test prerequisites
	suite.addTest(new ExtensiveFragmentTest("requires1Test"));
	suite.addTest(new ExtensiveFragmentTest("requires2Test"));
	suite.addTest(new ExtensiveFragmentTest("requires3Test"));
	suite.addTest(new ExtensiveFragmentTest("requires4Test"));
	suite.addTest(new ExtensiveFragmentTest("requires5Test"));
	return suite;
}

public void extensionPoint1Test() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "extensiveFragmentTest", null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String[] pluginPaths = new String[2];
	pluginPaths[0] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/extensionPoint1Test.xml");
	pluginPaths[1] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/fragmentExtensionPoint1Test.xml");
	URL pluginURLs[] = new URL[2];
	for (int i = 0; i < pluginURLs.length; i++) {
		URL pURL = null;
		try {
			pURL = new URL (pluginPaths[i]);
		} catch (java.net.MalformedURLException e) {
			assertTrue("Bad URL for " + pluginPaths[i], true);
		}
		pluginURLs[i] = pURL;
	}
	IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, true);
	IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors();
	assertTrue("1.0 Only one plugin", pluginDescriptors.length == 1);
	PluginDescriptorModel plugin = (PluginDescriptorModel)pluginDescriptors[0];
	assertTrue("1.1 Got the right plugin", plugin.getId().equals("extensionPoint1Test"));
	PluginFragmentModel[] fragmentList = ((PluginRegistryModel)registry).getFragments();
	assertTrue("1.2 Only one fragment", fragmentList.length == 1);
	PluginFragmentModel fragment = fragmentList[0];
	assertTrue("1.3 Got the right fragment", fragment.getId().equals("fragmentExtensionPoint1Test"));
	
	// Now make sure we got all 3 extension points
	ExtensionPointModel[] extPts = plugin.getDeclaredExtensionPoints();
	assertTrue("1.4 Got 3 extension points", extPts.length == 3);
	assertNotNull("1.5 Got xpt1 extension point", ((IPluginDescriptor)plugin).getExtensionPoint("xpt1"));
	assertNotNull("1.6 Got xpt2 extension point", ((IPluginDescriptor)plugin).getExtensionPoint("xpt2"));
	assertNotNull("1.7 Got xpt3 extension point", ((IPluginDescriptor)plugin).getExtensionPoint("xpt3"));
}

public void extensionPoint2Test() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "extensiveFragmentTest", null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String[] pluginPaths = new String[2];
	pluginPaths[0] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/extensionPoint2Test.xml");
	pluginPaths[1] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/fragmentExtensionPoint2Test.xml");
	URL pluginURLs[] = new URL[2];
	for (int i = 0; i < pluginURLs.length; i++) {
		URL pURL = null;
		try {
			pURL = new URL (pluginPaths[i]);
		} catch (java.net.MalformedURLException e) {
			assertTrue("Bad URL for " + pluginPaths[i], true);
		}
		pluginURLs[i] = pURL;
	}
	IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, true);
	IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors();
	assertTrue("2.0 Only one plugin", pluginDescriptors.length == 1);
	PluginDescriptorModel plugin = (PluginDescriptorModel)pluginDescriptors[0];
	assertTrue("2.1 Got the right plugin", plugin.getId().equals("extensionPoint2Test"));
	PluginFragmentModel[] fragmentList = ((PluginRegistryModel)registry).getFragments();
	assertTrue("2.2 Only one fragment", fragmentList.length == 1);
	PluginFragmentModel fragment = fragmentList[0];
	assertTrue("2.3 Got the right fragment", fragment.getId().equals("fragmentExtensionPoint2Test"));
	
	// Now make sure we got 1 extension point
	ExtensionPointModel[] extPts = plugin.getDeclaredExtensionPoints();
	assertTrue("2.4 Got 1 extension point", extPts.length == 1);
	assertNotNull("2.5 Got xpt3 extension point", ((IPluginDescriptor)plugin).getExtensionPoint("xpt3"));
}

public void extensionPoint3Test() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "extensiveFragmentTest", null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String[] pluginPaths = new String[2];
	pluginPaths[0] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/extensionPoint3Test.xml");
	pluginPaths[1] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/fragmentExtensionPoint3Test.xml");
	URL pluginURLs[] = new URL[2];
	for (int i = 0; i < pluginURLs.length; i++) {
		URL pURL = null;
		try {
			pURL = new URL (pluginPaths[i]);
		} catch (java.net.MalformedURLException e) {
			assertTrue("Bad URL for " + pluginPaths[i], true);
		}
		pluginURLs[i] = pURL;
	}
	IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, true);
	IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors();
	assertTrue("3.0 Only one plugin", pluginDescriptors.length == 1);
	PluginDescriptorModel plugin = (PluginDescriptorModel)pluginDescriptors[0];
	assertTrue("3.1 Got the right plugin", plugin.getId().equals("extensionPoint3Test"));
	PluginFragmentModel[] fragmentList = ((PluginRegistryModel)registry).getFragments();
	assertTrue("3.2 Only one fragment", fragmentList.length == 1);
	PluginFragmentModel fragment = fragmentList[0];
	assertTrue("3.3 Got the right fragment", fragment.getId().equals("fragmentExtensionPoint3Test"));
	
	// Now make sure we got all 2 extension points
	ExtensionPointModel[] extPts = plugin.getDeclaredExtensionPoints();
	assertTrue("3.4 Got 2 extension points", extPts.length == 2);
	assertNotNull("3.5 Got xpt1 extension point", ((IPluginDescriptor)plugin).getExtensionPoint("xpt1"));
	assertNotNull("3.6 Got xpt2 extension point", ((IPluginDescriptor)plugin).getExtensionPoint("xpt2"));
}

public void extensionPoint4Test() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "extensiveFragmentTest", null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String[] pluginPaths = new String[2];
	pluginPaths[0] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/extensionPoint4Test.xml");
	pluginPaths[1] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/fragmentExtensionPoint4Test.xml");
	URL pluginURLs[] = new URL[2];
	for (int i = 0; i < pluginURLs.length; i++) {
		URL pURL = null;
		try {
			pURL = new URL (pluginPaths[i]);
		} catch (java.net.MalformedURLException e) {
			assertTrue("Bad URL for " + pluginPaths[i], true);
		}
		pluginURLs[i] = pURL;
	}
	IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, true);
	IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors();
	assertTrue("4.0 Only one plugin", pluginDescriptors.length == 1);
	PluginDescriptorModel plugin = (PluginDescriptorModel)pluginDescriptors[0];
	assertTrue("4.1 Got the right plugin", plugin.getId().equals("extensionPoint4Test"));
	PluginFragmentModel[] fragmentList = ((PluginRegistryModel)registry).getFragments();
	assertTrue("4.2 Only one fragment", fragmentList.length == 1);
	PluginFragmentModel fragment = fragmentList[0];
	assertTrue("4.3 Got the right fragment", fragment.getId().equals("fragmentExtensionPoint4Test"));
	
	// Now make sure we got all 2 extension points
	ExtensionPointModel[] extPts = plugin.getDeclaredExtensionPoints();
	assertTrue("4.4 Got 2 extension points", extPts.length == 2);
	assertNotNull("4.5 Got xpt1 extension point", ((IPluginDescriptor)plugin).getExtensionPoint("xpt1"));
	assertNotNull("4.6 Got xpt3 extension point", ((IPluginDescriptor)plugin).getExtensionPoint("xpt3"));
}

public void extensionPoint5Test() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "extensiveFragmentTest", null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String[] pluginPaths = new String[2];
	pluginPaths[0] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/extensionPoint5Test.xml");
	pluginPaths[1] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/fragmentExtensionPoint5Test.xml");
	URL pluginURLs[] = new URL[2];
	for (int i = 0; i < pluginURLs.length; i++) {
		URL pURL = null;
		try {
			pURL = new URL (pluginPaths[i]);
		} catch (java.net.MalformedURLException e) {
			assertTrue("Bad URL for " + pluginPaths[i], true);
		}
		pluginURLs[i] = pURL;
	}
	IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, true);
	IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors();
	assertTrue("5.0 Only one plugin", pluginDescriptors.length == 1);
	PluginDescriptorModel plugin = (PluginDescriptorModel)pluginDescriptors[0];
	assertTrue("5.1 Got the right plugin", plugin.getId().equals("extensionPoint5Test"));
	PluginFragmentModel[] fragmentList = ((PluginRegistryModel)registry).getFragments();
	assertTrue("5.2 Only one fragment", fragmentList.length == 1);
	PluginFragmentModel fragment = fragmentList[0];
	assertTrue("5.3 Got the right fragment", fragment.getId().equals("fragmentExtensionPoint5Test"));
	
	// Now make sure we got all 8 extension points
	ExtensionPointModel[] extPts = plugin.getDeclaredExtensionPoints();
	assertTrue("5.4 Got 8 extension points", extPts.length == 8);
	assertNotNull("5.5 Got xpt1 extension point", ((IPluginDescriptor)plugin).getExtensionPoint("xpt1"));
	assertNotNull("5.6 Got xpt2 extension point", ((IPluginDescriptor)plugin).getExtensionPoint("xpt2"));
	assertNotNull("5.7 Got xpt3 extension point", ((IPluginDescriptor)plugin).getExtensionPoint("xpt3"));
	assertNotNull("5.8 Got xpt4 extension point", ((IPluginDescriptor)plugin).getExtensionPoint("xpt4"));
	assertNotNull("5.9 Got xpt5 extension point", ((IPluginDescriptor)plugin).getExtensionPoint("xpt5"));
	assertNotNull("5.10 Got xpt6 extension point", ((IPluginDescriptor)plugin).getExtensionPoint("xpt6"));
	assertNotNull("5.11 Got xpt7 extension point", ((IPluginDescriptor)plugin).getExtensionPoint("xpt7"));
	assertNotNull("5.12 Got xpt8 extension point", ((IPluginDescriptor)plugin).getExtensionPoint("xpt8"));
}

public void extension1Test() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "extensiveFragmentTest", null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String[] pluginPaths = new String[2];
	pluginPaths[0] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/extension1Test.xml");
	pluginPaths[1] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/fragmentExtension1Test.xml");
	URL pluginURLs[] = new URL[2];
	for (int i = 0; i < pluginURLs.length; i++) {
		URL pURL = null;
		try {
			pURL = new URL (pluginPaths[i]);
		} catch (java.net.MalformedURLException e) {
			assertTrue("Bad URL for " + pluginPaths[i], true);
		}
		pluginURLs[i] = pURL;
	}
	IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, true);
	IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors();
	assertTrue("6.0 Only one plugin", pluginDescriptors.length == 1);
	PluginDescriptorModel plugin = (PluginDescriptorModel)pluginDescriptors[0];
	assertTrue("6.1 Got the right plugin", plugin.getId().equals("extension1Test"));
	PluginFragmentModel[] fragmentList = ((PluginRegistryModel)registry).getFragments();
	assertTrue("6.2 Only one fragment", fragmentList.length == 1);
	PluginFragmentModel fragment = fragmentList[0];
	assertTrue("6.3 Got the right fragment", fragment.getId().equals("fragmentExtension1Test"));
	
	// Now make sure we got all 3 extensions
	ExtensionModel[] extensions = plugin.getDeclaredExtensions();
	assertTrue("6.4 Got 3 extensions", extensions.length == 3);
	assertNotNull("6.5 Got xpt1 extension", ((IPluginDescriptor)plugin).getExtension("xpt1"));
	assertNotNull("6.6 Got xpt2 extension", ((IPluginDescriptor)plugin).getExtension("xpt2"));
	assertNotNull("6.7 Got xpt3 extension", ((IPluginDescriptor)plugin).getExtension("xpt3"));
}

public void extension2Test() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "extensiveFragmentTest", null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String[] pluginPaths = new String[2];
	pluginPaths[0] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/extension2Test.xml");
	pluginPaths[1] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/fragmentExtension2Test.xml");
	URL pluginURLs[] = new URL[2];
	for (int i = 0; i < pluginURLs.length; i++) {
		URL pURL = null;
		try {
			pURL = new URL (pluginPaths[i]);
		} catch (java.net.MalformedURLException e) {
			assertTrue("Bad URL for " + pluginPaths[i], true);
		}
		pluginURLs[i] = pURL;
	}
	IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, true);
	IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors();
	assertTrue("7.0 Only one plugin", pluginDescriptors.length == 1);
	PluginDescriptorModel plugin = (PluginDescriptorModel)pluginDescriptors[0];
	assertTrue("7.1 Got the right plugin", plugin.getId().equals("extension2Test"));
	PluginFragmentModel[] fragmentList = ((PluginRegistryModel)registry).getFragments();
	assertTrue("7.2 Only one fragment", fragmentList.length == 1);
	PluginFragmentModel fragment = fragmentList[0];
	assertTrue("7.3 Got the right fragment", fragment.getId().equals("fragmentExtension2Test"));
	
	// Now make sure we got 1 extension
	ExtensionModel[] extensions = plugin.getDeclaredExtensions();
	assertTrue("7.4 Got 1 extension", extensions.length == 1);
	assertNotNull("7.5 Got xpt3 extension", ((IPluginDescriptor)plugin).getExtension("xpt3"));
}

public void extension3Test() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "extensiveFragmentTest", null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String[] pluginPaths = new String[2];
	pluginPaths[0] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/extension3Test.xml");
	pluginPaths[1] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/fragmentExtension3Test.xml");
	URL pluginURLs[] = new URL[2];
	for (int i = 0; i < pluginURLs.length; i++) {
		URL pURL = null;
		try {
			pURL = new URL (pluginPaths[i]);
		} catch (java.net.MalformedURLException e) {
			assertTrue("Bad URL for " + pluginPaths[i], true);
		}
		pluginURLs[i] = pURL;
	}
	IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, true);
	IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors();
	assertTrue("8.0 Only one plugin", pluginDescriptors.length == 1);
	PluginDescriptorModel plugin = (PluginDescriptorModel)pluginDescriptors[0];
	assertTrue("8.1 Got the right plugin", plugin.getId().equals("extension3Test"));
	PluginFragmentModel[] fragmentList = ((PluginRegistryModel)registry).getFragments();
	assertTrue("8.2 Only one fragment", fragmentList.length == 1);
	PluginFragmentModel fragment = fragmentList[0];
	assertTrue("8.3 Got the right fragment", fragment.getId().equals("fragmentExtension3Test"));
	
	// Now make sure we got all 2 extensions
	ExtensionModel[] extensions = plugin.getDeclaredExtensions();
	assertTrue("8.4 Got 2 extensions", extensions.length == 2);
	assertNotNull("8.5 Got xpt1 extension", ((IPluginDescriptor)plugin).getExtension("xpt1"));
	assertNotNull("8.6 Got xpt2 extension", ((IPluginDescriptor)plugin).getExtension("xpt2"));
}

public void extension4Test() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "extensiveFragmentTest", null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String[] pluginPaths = new String[2];
	pluginPaths[0] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/extension4Test.xml");
	pluginPaths[1] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/fragmentExtension4Test.xml");
	URL pluginURLs[] = new URL[2];
	for (int i = 0; i < pluginURLs.length; i++) {
		URL pURL = null;
		try {
			pURL = new URL (pluginPaths[i]);
		} catch (java.net.MalformedURLException e) {
			assertTrue("Bad URL for " + pluginPaths[i], true);
		}
		pluginURLs[i] = pURL;
	}
	IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, true);
	IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors();
	assertTrue("9.0 Only one plugin", pluginDescriptors.length == 1);
	PluginDescriptorModel plugin = (PluginDescriptorModel)pluginDescriptors[0];
	assertTrue("9.1 Got the right plugin", plugin.getId().equals("extension4Test"));
	PluginFragmentModel[] fragmentList = ((PluginRegistryModel)registry).getFragments();
	assertTrue("9.2 Only one fragment", fragmentList.length == 1);
	PluginFragmentModel fragment = fragmentList[0];
	assertTrue("9.3 Got the right fragment", fragment.getId().equals("fragmentExtension4Test"));
	
	// Now make sure we got all 2 extensions
	ExtensionModel[] extensions = plugin.getDeclaredExtensions();
	assertTrue("9.4 Got 2 extensions", extensions.length == 2);
	assertNotNull("9.5 Got xpt1 extension", ((IPluginDescriptor)plugin).getExtension("xpt1"));
	assertNotNull("9.6 Got xpt3 extension", ((IPluginDescriptor)plugin).getExtension("xpt3"));
}

public void extension5Test() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "extensiveFragmentTest", null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String[] pluginPaths = new String[2];
	pluginPaths[0] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/extension5Test.xml");
	pluginPaths[1] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/fragmentExtension5Test.xml");
	URL pluginURLs[] = new URL[2];
	for (int i = 0; i < pluginURLs.length; i++) {
		URL pURL = null;
		try {
			pURL = new URL (pluginPaths[i]);
		} catch (java.net.MalformedURLException e) {
			assertTrue("Bad URL for " + pluginPaths[i], true);
		}
		pluginURLs[i] = pURL;
	}
	IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, true);
	IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors();
	assertTrue("10.0 Only one plugin", pluginDescriptors.length == 1);
	PluginDescriptorModel plugin = (PluginDescriptorModel)pluginDescriptors[0];
	assertTrue("10.1 Got the right plugin", plugin.getId().equals("extension5Test"));
	PluginFragmentModel[] fragmentList = ((PluginRegistryModel)registry).getFragments();
	assertTrue("10.2 Only one fragment", fragmentList.length == 1);
	PluginFragmentModel fragment = fragmentList[0];
	assertTrue("10.3 Got the right fragment", fragment.getId().equals("fragmentExtension5Test"));
	
	// Now make sure we got all 8 extensions
	ExtensionModel[] extensions = plugin.getDeclaredExtensions();
	assertTrue("10.4 Got 8 extensions", extensions.length == 8);
	assertNotNull("10.5 Got xpt1 extension", ((IPluginDescriptor)plugin).getExtension("xpt1"));
	assertNotNull("10.6 Got xpt2 extension", ((IPluginDescriptor)plugin).getExtension("xpt2"));
	assertNotNull("10.7 Got xpt3 extension", ((IPluginDescriptor)plugin).getExtension("xpt3"));
	assertNotNull("10.8 Got xpt4 extension", ((IPluginDescriptor)plugin).getExtension("xpt4"));
	assertNotNull("10.9 Got xpt5 extension", ((IPluginDescriptor)plugin).getExtension("xpt5"));
	assertNotNull("10.10 Got xpt6 extension", ((IPluginDescriptor)plugin).getExtension("xpt6"));
	assertNotNull("10.11 Got xpt7 extension", ((IPluginDescriptor)plugin).getExtension("xpt7"));
	assertNotNull("10.12 Got xpt8 extension", ((IPluginDescriptor)plugin).getExtension("xpt8"));
}

public void library1Test() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "extensiveFragmentTest", null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String[] pluginPaths = new String[2];
	pluginPaths[0] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/library1Test.xml");
	pluginPaths[1] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/fragmentLibrary1Test.xml");
	URL pluginURLs[] = new URL[2];
	for (int i = 0; i < pluginURLs.length; i++) {
		URL pURL = null;
		try {
			pURL = new URL (pluginPaths[i]);
		} catch (java.net.MalformedURLException e) {
			assertTrue("Bad URL for " + pluginPaths[i], true);
		}
		pluginURLs[i] = pURL;
	}
	IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, true);
	IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors();
	assertTrue("11.0 Only one plugin", pluginDescriptors.length == 1);
	PluginDescriptorModel plugin = (PluginDescriptorModel)pluginDescriptors[0];
	assertTrue("11.1 Got the right plugin", plugin.getId().equals("library1Test"));
	PluginFragmentModel[] fragmentList = ((PluginRegistryModel)registry).getFragments();
	assertTrue("11.2 Only one fragment", fragmentList.length == 1);
	PluginFragmentModel fragment = fragmentList[0];
	assertTrue("11.3 Got the right fragment", fragment.getId().equals("fragmentLibrary1Test"));
	
	// Now make sure we got all 3 libraries
	LibraryModel[] libraries = plugin.getRuntime();
	assertTrue("11.4 Got 3 libraries", libraries.length == 3);
	int lib1Idx = -1;
	int lib2Idx = -1;
	int lib3Idx = -1;
	for (int i = 0; i < libraries.length; i++) {
		if (libraries[i].getName().equals("lib1.jar")) {
			lib1Idx = i;
		} else if (libraries[i].getName().equals("lib2.jar")) {
			lib2Idx = i;
		} else if (libraries[i].getName().equals("lib3.jar")) {
			lib3Idx = i;
		}
	}
	assertTrue("11.5 Got lib1 library", lib1Idx != -1);
	assertTrue("11.6 Got lib2 library", lib2Idx != -1);
	assertTrue("11.7 Got lib3 library", lib3Idx != -1);
}

public void library2Test() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "extensiveFragmentTest", null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String[] pluginPaths = new String[2];
	pluginPaths[0] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/library2Test.xml");
	pluginPaths[1] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/fragmentLibrary2Test.xml");
	URL pluginURLs[] = new URL[2];
	for (int i = 0; i < pluginURLs.length; i++) {
		URL pURL = null;
		try {
			pURL = new URL (pluginPaths[i]);
		} catch (java.net.MalformedURLException e) {
			assertTrue("Bad URL for " + pluginPaths[i], true);
		}
		pluginURLs[i] = pURL;
	}
	IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, true);
	IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors();
	assertTrue("12.0 Only one plugin", pluginDescriptors.length == 1);
	PluginDescriptorModel plugin = (PluginDescriptorModel)pluginDescriptors[0];
	assertTrue("12.1 Got the right plugin", plugin.getId().equals("library2Test"));
	PluginFragmentModel[] fragmentList = ((PluginRegistryModel)registry).getFragments();
	assertTrue("12.2 Only one fragment", fragmentList.length == 1);
	PluginFragmentModel fragment = fragmentList[0];
	assertTrue("12.3 Got the right fragment", fragment.getId().equals("fragmentLibrary2Test"));
	
	// Now make sure we got 1 library
	LibraryModel[] libraries = plugin.getRuntime();
	assertTrue("12.4 Got 1 library", libraries.length == 1);
	assertTrue("12.5 Got lib3 library", libraries[0].getName().equals("lib3.jar"));
}

public void library3Test() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "extensiveFragmentTest", null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String[] pluginPaths = new String[2];
	pluginPaths[0] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/library3Test.xml");
	pluginPaths[1] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/fragmentLibrary3Test.xml");
	URL pluginURLs[] = new URL[2];
	for (int i = 0; i < pluginURLs.length; i++) {
		URL pURL = null;
		try {
			pURL = new URL (pluginPaths[i]);
		} catch (java.net.MalformedURLException e) {
			assertTrue("Bad URL for " + pluginPaths[i], true);
		}
		pluginURLs[i] = pURL;
	}
	IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, true);
	IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors();
	assertTrue("13.0 Only one plugin", pluginDescriptors.length == 1);
	PluginDescriptorModel plugin = (PluginDescriptorModel)pluginDescriptors[0];
	assertTrue("13.1 Got the right plugin", plugin.getId().equals("library3Test"));
	PluginFragmentModel[] fragmentList = ((PluginRegistryModel)registry).getFragments();
	assertTrue("13.2 Only one fragment", fragmentList.length == 1);
	PluginFragmentModel fragment = fragmentList[0];
	assertTrue("13.3 Got the right fragment", fragment.getId().equals("fragmentLibrary3Test"));
	
	// Now make sure we got all 2 libraries
	LibraryModel[] libraries = plugin.getRuntime();
	assertTrue("13.4 Got 2 libraries", libraries.length == 2);
	int lib1Idx = -1;
	int lib2Idx = -1;
	for (int i = 0; i < libraries.length; i ++) {
		if (libraries[i].getName().equals("lib1.jar")) {
			lib1Idx = i;
		} else if (libraries[i].getName().equals("lib2.jar")) {
			lib2Idx = i;
		}
	}
	assertTrue("13.5 Got lib1 library", lib1Idx != -1);
	assertTrue("13.6 Got lib2 library", lib2Idx != -1);
}

public void library4Test() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "extensiveFragmentTest", null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String[] pluginPaths = new String[2];
	pluginPaths[0] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/library4Test.xml");
	pluginPaths[1] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/fragmentLibrary4Test.xml");
	URL pluginURLs[] = new URL[2];
	for (int i = 0; i < pluginURLs.length; i++) {
		URL pURL = null;
		try {
			pURL = new URL (pluginPaths[i]);
		} catch (java.net.MalformedURLException e) {
			assertTrue("Bad URL for " + pluginPaths[i], true);
		}
		pluginURLs[i] = pURL;
	}
	IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, true);
	IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors();
	assertTrue("14.0 Only one plugin", pluginDescriptors.length == 1);
	PluginDescriptorModel plugin = (PluginDescriptorModel)pluginDescriptors[0];
	assertTrue("14.1 Got the right plugin", plugin.getId().equals("library4Test"));
	PluginFragmentModel[] fragmentList = ((PluginRegistryModel)registry).getFragments();
	assertTrue("14.2 Only one fragment", fragmentList.length == 1);
	PluginFragmentModel fragment = fragmentList[0];
	assertTrue("14.3 Got the right fragment", fragment.getId().equals("fragmentLibrary4Test"));
	
	// Now make sure we got all 2 libraries
	LibraryModel[] libraries = plugin.getRuntime();
	assertTrue("14.4 Got 2 libraries", libraries.length == 2);
	int lib1Idx = -1;
	int lib3Idx = -1;
	for (int i = 0; i < libraries.length; i++) {
		if (libraries[i].getName().equals("lib1.jar")) {
			lib1Idx = i;
		} else if (libraries[i].getName().equals("lib3.jar")) {
			lib3Idx = i;
		}
	}
	assertTrue("14.5 Got lib1 library", lib1Idx != -1);
	assertTrue("14.6 Got lib3 library", lib3Idx != -1);
}

public void library5Test() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "extensiveFragmentTest", null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String[] pluginPaths = new String[2];
	pluginPaths[0] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/library5Test.xml");
	pluginPaths[1] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/fragmentLibrary5Test.xml");
	URL pluginURLs[] = new URL[2];
	for (int i = 0; i < pluginURLs.length; i++) {
		URL pURL = null;
		try {
			pURL = new URL (pluginPaths[i]);
		} catch (java.net.MalformedURLException e) {
			assertTrue("Bad URL for " + pluginPaths[i], true);
		}
		pluginURLs[i] = pURL;
	}
	IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, true);
	IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors();
	assertTrue("15.0 Only one plugin", pluginDescriptors.length == 1);
	PluginDescriptorModel plugin = (PluginDescriptorModel)pluginDescriptors[0];
	assertTrue("15.1 Got the right plugin", plugin.getId().equals("library5Test"));
	PluginFragmentModel[] fragmentList = ((PluginRegistryModel)registry).getFragments();
	assertTrue("15.2 Only one fragment", fragmentList.length == 1);
	PluginFragmentModel fragment = fragmentList[0];
	assertTrue("15.3 Got the right fragment", fragment.getId().equals("fragmentLibrary5Test"));
	
	// Now make sure we got all 8 libraries
	LibraryModel[] libraries = plugin.getRuntime();
	assertTrue("15.4 Got 8 libraries", libraries.length == 8);
	int lib1Idx = -1;
	int lib2Idx = -1;
	int lib3Idx = -1;
	int lib4Idx = -1;
	int lib5Idx = -1;
	int lib6Idx = -1;
	int lib7Idx = -1;
	int lib8Idx = -1;
	for (int i = 0; i < libraries.length; i++) {
		if (libraries[i].getName().equals("lib1.jar")) {
			lib1Idx = i;
		} else if (libraries[i].getName().equals("lib2.jar")) {
			lib2Idx = i;
		} else if (libraries[i].getName().equals("lib3.jar")) {
			lib3Idx = i;
		} else if (libraries[i].getName().equals("lib4.jar")) {
			lib4Idx = i;
		} else if (libraries[i].getName().equals("lib5.jar")) {
			lib5Idx = i;
		} else if (libraries[i].getName().equals("lib6.jar")) {
			lib6Idx = i;
		} else if (libraries[i].getName().equals("lib7.jar")) {
			lib7Idx = i;
		} else if (libraries[i].getName().equals("lib8.jar")) {
			lib8Idx = i;
		}
	}
	assertTrue("15.5 Got lib1 library", lib1Idx != -1);
	assertTrue("15.6 Got lib2 library", lib2Idx != -1);
	assertTrue("15.7 Got lib3 library", lib3Idx != -1);
	assertTrue("15.8 Got lib4 library", lib4Idx != -1);
	assertTrue("15.9 Got lib5 library", lib5Idx != -1);
	assertTrue("15.10 Got lib6 library", lib6Idx != -1);
	assertTrue("15.11 Got lib7 library", lib7Idx != -1);
	assertTrue("15.12 Got lib8 library", lib8Idx != -1);
}

public void requires1Test() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "extensiveFragmentTest", null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String[] pluginPaths = new String[5];
	pluginPaths[0] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/requires1Test.xml");
	pluginPaths[1] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/fragmentRequires1Test.xml");
	pluginPaths[2] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/plugin1.xml");
	pluginPaths[3] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/plugin2.xml");
	pluginPaths[4] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/plugin3.xml");
	URL pluginURLs[] = new URL[5];
	for (int i = 0; i < pluginURLs.length; i++) {
		URL pURL = null;
		try {
			pURL = new URL (pluginPaths[i]);
		} catch (java.net.MalformedURLException e) {
			assertTrue("Bad URL for " + pluginPaths[i], true);
		}
		pluginURLs[i] = pURL;
	}
	IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, true);
	IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors("requires1Test");
	assertTrue("16.0 Only one plugin", pluginDescriptors.length == 1);
	PluginDescriptorModel plugin = (PluginDescriptorModel)pluginDescriptors[0];
	assertTrue("16.1 Got the right plugin", plugin.getId().equals("requires1Test"));
	PluginFragmentModel[] fragmentList = ((PluginRegistryModel)registry).getFragments();
	assertTrue("16.2 Only one fragment", fragmentList.length == 1);
	PluginFragmentModel fragment = fragmentList[0];
	assertTrue("16.3 Got the right fragment", fragment.getId().equals("fragmentRequires1Test"));
	
	// Now make sure we got all 3 prerequisites
	PluginPrerequisiteModel[] requires = plugin.getRequires();
	assertTrue("16.4 Got 3 prerequisites", requires.length == 3);
	int req1Idx = -1;
	int req2Idx = -1;
	int req3Idx = -1;
	for (int i = 0; i < requires.length; i++) {
		if (requires[i].getPlugin().equals("plugin1")) {
			req1Idx = i;
		} else if (requires[i].getPlugin().equals("plugin2")) {
			req2Idx = i;
		} else if (requires[i].getPlugin().equals("plugin3")) {
			req3Idx = i;
		}
	}
	assertTrue("16.5 Got plugin1 prerequisite", req1Idx != -1);
	assertTrue("16.6 Got plugin2 prerequisite", req2Idx != -1);
	assertTrue("16.7 Got plugin3 prerequisite", req3Idx != -1);
}

public void requires2Test() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "extensiveFragmentTest", null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String[] pluginPaths = new String[3];
	pluginPaths[0] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/requires2Test.xml");
	pluginPaths[1] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/fragmentRequires2Test.xml");
	pluginPaths[2] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/plugin3.xml");
	URL pluginURLs[] = new URL[3];
	for (int i = 0; i < pluginURLs.length; i++) {
		URL pURL = null;
		try {
			pURL = new URL (pluginPaths[i]);
		} catch (java.net.MalformedURLException e) {
			assertTrue("Bad URL for " + pluginPaths[i], true);
		}
		pluginURLs[i] = pURL;
	}
	IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, true);
	IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors("requires2Test");
	assertTrue("17.0 Only one plugin", pluginDescriptors.length == 1);
	PluginDescriptorModel plugin = (PluginDescriptorModel)pluginDescriptors[0];
	assertTrue("17.1 Got the right plugin", plugin.getId().equals("requires2Test"));
	PluginFragmentModel[] fragmentList = ((PluginRegistryModel)registry).getFragments();
	assertTrue("17.2 Only one fragment", fragmentList.length == 1);
	PluginFragmentModel fragment = fragmentList[0];
	assertTrue("17.3 Got the right fragment", fragment.getId().equals("fragmentRequires2Test"));
	
	// Now make sure we got 1 prerequisite
	PluginPrerequisiteModel[] requires = plugin.getRequires();
	assertTrue("17.4 Got 1 prerequisite", requires.length == 1);
	assertTrue("17.5 Got plugin3 prerequisite", requires[0].getPlugin().equals("plugin3"));
}

public void requires3Test() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "extensiveFragmentTest", null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String[] pluginPaths = new String[4];
	pluginPaths[0] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/requires3Test.xml");
	pluginPaths[1] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/fragmentRequires3Test.xml");
	pluginPaths[2] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/plugin1.xml");
	pluginPaths[3] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/plugin2.xml");
	URL pluginURLs[] = new URL[4];
	for (int i = 0; i < pluginURLs.length; i++) {
		URL pURL = null;
		try {
			pURL = new URL (pluginPaths[i]);
		} catch (java.net.MalformedURLException e) {
			assertTrue("Bad URL for " + pluginPaths[i], true);
		}
		pluginURLs[i] = pURL;
	}
	IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, true);
	IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors("requires3Test");
	assertTrue("18.0 Only one plugin", pluginDescriptors.length == 1);
	PluginDescriptorModel plugin = (PluginDescriptorModel)pluginDescriptors[0];
	assertTrue("18.1 Got the right plugin", plugin.getId().equals("requires3Test"));
	PluginFragmentModel[] fragmentList = ((PluginRegistryModel)registry).getFragments();
	assertTrue("18.2 Only one fragment", fragmentList.length == 1);
	PluginFragmentModel fragment = fragmentList[0];
	assertTrue("18.3 Got the right fragment", fragment.getId().equals("fragmentRequires3Test"));
	
	// Now make sure we got all 2 prerequisites
	PluginPrerequisiteModel[] requires = plugin.getRequires();
	assertTrue("18.4 Got 2 prerequisites", requires.length == 2);
	int req1Idx = -1;
	int req2Idx = -1;
	for (int i = 0; i < requires.length; i ++) {
		if (requires[i].getPlugin().equals("plugin1")) {
			req1Idx = i;
		} else if (requires[i].getPlugin().equals("plugin2")) {
			req2Idx = i;
		}
	}
	assertTrue("18.5 Got plugin1 prerequisite", req1Idx != -1);
	assertTrue("18.6 Got plugin2 prerequisite", req2Idx != -1);
}

public void requires4Test() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "extensiveFragmentTest", null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String[] pluginPaths = new String[4];
	pluginPaths[0] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/requires4Test.xml");
	pluginPaths[1] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/fragmentRequires4Test.xml");
	pluginPaths[2] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/plugin1.xml");
	pluginPaths[3] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/plugin3.xml");
	URL pluginURLs[] = new URL[4];
	for (int i = 0; i < pluginURLs.length; i++) {
		URL pURL = null;
		try {
			pURL = new URL (pluginPaths[i]);
		} catch (java.net.MalformedURLException e) {
			assertTrue("Bad URL for " + pluginPaths[i], true);
		}
		pluginURLs[i] = pURL;
	}
	IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, true);
	IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors("requires4Test");
	assertTrue("19.0 Only one plugin", pluginDescriptors.length == 1);
	PluginDescriptorModel plugin = (PluginDescriptorModel)pluginDescriptors[0];
	assertTrue("19.1 Got the right plugin", plugin.getId().equals("requires4Test"));
	PluginFragmentModel[] fragmentList = ((PluginRegistryModel)registry).getFragments();
	assertTrue("19.2 Only one fragment", fragmentList.length == 1);
	PluginFragmentModel fragment = fragmentList[0];
	assertTrue("19.3 Got the right fragment", fragment.getId().equals("fragmentRequires4Test"));
	
	// Now make sure we got all 2 prerequisites
	PluginPrerequisiteModel[] requires = plugin.getRequires();
	assertTrue("19.4 Got 2 prerequisites", requires.length == 2);
	int req1Idx = -1;
	int req3Idx = -1;
	for (int i = 0; i < requires.length; i++) {
		if (requires[i].getPlugin().equals("plugin1")) {
			req1Idx = i;
		} else if (requires[i].getPlugin().equals("plugin3")) {
			req3Idx = i;
		}
	}
	assertTrue("19.5 Got plugin1 prerequisite", req1Idx != -1);
	assertTrue("19.6 Got plugin3 prerequisite", req3Idx != -1);
}

public void requires5Test() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "extensiveFragmentTest", null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String[] pluginPaths = new String[10];
	pluginPaths[0] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/requires5Test.xml");
	pluginPaths[1] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/fragmentRequires5Test.xml");
	pluginPaths[2] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/plugin1.xml");
	pluginPaths[3] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/plugin2.xml");
	pluginPaths[4] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/plugin3.xml");
	pluginPaths[5] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/plugin4.xml");
	pluginPaths[6] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/plugin5.xml");
	pluginPaths[7] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/plugin6.xml");
	pluginPaths[8] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/plugin7.xml");
	pluginPaths[9] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveFragmentTest/plugin8.xml");
	URL pluginURLs[] = new URL[10];
	for (int i = 0; i < pluginURLs.length; i++) {
		URL pURL = null;
		try {
			pURL = new URL (pluginPaths[i]);
		} catch (java.net.MalformedURLException e) {
			assertTrue("Bad URL for " + pluginPaths[i], true);
		}
		pluginURLs[i] = pURL;
	}
	IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, true);
	IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors("requires5Test");
	assertTrue("20.0 Only one plugin", pluginDescriptors.length == 1);
	PluginDescriptorModel plugin = (PluginDescriptorModel)pluginDescriptors[0];
	assertTrue("20.1 Got the right plugin", plugin.getId().equals("requires5Test"));
	PluginFragmentModel[] fragmentList = ((PluginRegistryModel)registry).getFragments();
	assertTrue("20.2 Only one fragment", fragmentList.length == 1);
	PluginFragmentModel fragment = fragmentList[0];
	assertTrue("20.3 Got the right fragment", fragment.getId().equals("fragmentRequires5Test"));
	
	// Now make sure we got all 8 prerequisites
	PluginPrerequisiteModel[] requires = plugin.getRequires();
	assertTrue("20.4 Got 8 prerequisites", requires.length == 8);
	int req1Idx = -1;
	int req2Idx = -1;
	int req3Idx = -1;
	int req4Idx = -1;
	int req5Idx = -1;
	int req6Idx = -1;
	int req7Idx = -1;
	int req8Idx = -1;
	for (int i = 0; i < requires.length; i++) {
		if (requires[i].getPlugin().equals("plugin1")) {
			req1Idx = i;
		} else if (requires[i].getPlugin().equals("plugin2")) {
			req2Idx = i;
		} else if (requires[i].getPlugin().equals("plugin3")) {
			req3Idx = i;
		} else if (requires[i].getPlugin().equals("plugin4")) {
			req4Idx = i;
		} else if (requires[i].getPlugin().equals("plugin5")) {
			req5Idx = i;
		} else if (requires[i].getPlugin().equals("plugin6")) {
			req6Idx = i;
		} else if (requires[i].getPlugin().equals("plugin7")) {
			req7Idx = i;
		} else if (requires[i].getPlugin().equals("plugin8")) {
			req8Idx = i;
		}
	}
	assertTrue("20.5 Got plugin1 prerequisite", req1Idx != -1);
	assertTrue("20.6 Got plugin2 prerequisite", req2Idx != -1);
	assertTrue("20.7 Got plugin3 prerequisite", req3Idx != -1);
	assertTrue("20.8 Got plugin4 prerequisite", req4Idx != -1);
	assertTrue("20.9 Got plugin5 prerequisite", req5Idx != -1);
	assertTrue("20.10 Got plugin6 prerequisite", req6Idx != -1);
	assertTrue("20.11 Got plugin7 prerequisite", req7Idx != -1);
	assertTrue("20.12 Got plugin8 prerequisite", req8Idx != -1);
}

}

