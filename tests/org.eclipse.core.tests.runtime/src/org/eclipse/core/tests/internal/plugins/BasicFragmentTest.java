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

public class BasicFragmentTest extends EclipseWorkspaceTest {

public BasicFragmentTest() {
	super(null);
}

public BasicFragmentTest(String name) {
	super(name);
}

public static Test suite() {
	TestSuite suite = new TestSuite();
	// These 4 tests are designed primarily to address
	// PR1GHFPFY which noted that runtime libraries in a
	// fragment were being added to the relevant plugin
	// twice.
	suite.addTest(new BasicFragmentTest("extensionPointTest"));
	suite.addTest(new BasicFragmentTest("extensionTest"));
	suite.addTest(new BasicFragmentTest("libraryTest"));
	suite.addTest(new BasicFragmentTest("requiresTest"));
	// The following test was added for PR1GHH81D.  In this
	// PR we found that we didn't always pick up the latest
	// version of a fragment to augment the plugin
	suite.addTest(new BasicFragmentTest("fragmentVersionTest"));
	return suite;
}
public void extensionPointTest() {
	// Ensure fragment extension point information is not being added twice to the plugin
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "basicFragmentTest", null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String[] pluginPaths = new String[1];
	pluginPaths[0] = tempPlugin.getLocation().concat("Plugin_Testing/PR1GHFPFYTest/extensionPointTest/");
	URL pluginURLs[] = new URL[1];
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
	IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors("PR1GHFPFYPluginTest");
	assertTrue("1.0 Only one plugin", pluginDescriptors.length == 1);
	PluginDescriptorModel plugin = (PluginDescriptorModel)pluginDescriptors[0];
	assertTrue("1.1 Got the right plugin", plugin.getId().equals("PR1GHFPFYPluginTest"));
	PluginFragmentModel[] fragmentList = ((PluginRegistryModel)registry).getFragments();
	assertTrue("1.2 Only one fragment", fragmentList.length == 1);
	PluginFragmentModel fragment = fragmentList[0];
	assertTrue("1.3 Got the right fragment", fragment.getId().equals("PR1GHFPFYFragmentTest"));
	
	// Check extension points
	ExtensionPointModel[] extensionPoints = plugin.getDeclaredExtensionPoints();
	assertTrue("1.4 Got 4 extension points", extensionPoints.length == 4);
	assertNotNull("1.5 Got xpt1 extension point", ((IPluginDescriptor)plugin).getExtensionPoint("xpt1"));
	assertNotNull("1.6 Got xpt2 extension point", ((IPluginDescriptor)plugin).getExtensionPoint("xpt2"));
	assertNotNull("1.7 Got xpt3 extension point", ((IPluginDescriptor)plugin).getExtensionPoint("xpt3"));
	assertNotNull("1.8 Got xpt4 extension point", ((IPluginDescriptor)plugin).getExtensionPoint("xpt4"));
}

public void extensionTest() {
	// Ensure fragment extension information is not being added twice to the plugin
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "basicFragmentTest", null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String[] pluginPaths = new String[1];
	pluginPaths[0] = tempPlugin.getLocation().concat("Plugin_Testing/PR1GHFPFYTest/extensionTest/");
	URL pluginURLs[] = new URL[1];
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
	IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors("PR1GHFPFYPluginTest");
	assertTrue("2.0 Only one plugin", pluginDescriptors.length == 1);
	PluginDescriptorModel plugin = (PluginDescriptorModel)pluginDescriptors[0];
	assertTrue("2.1 Got the right plugin", plugin.getId().equals("PR1GHFPFYPluginTest"));
	PluginFragmentModel[] fragmentList = ((PluginRegistryModel)registry).getFragments();
	assertTrue("2.2 Only one fragment", fragmentList.length == 1);
	PluginFragmentModel fragment = fragmentList[0];
	assertTrue("2.3 Got the right fragment", fragment.getId().equals("PR1GHFPFYFragmentTest"));
		
	// Check extensions
	ExtensionModel[] extensions = plugin.getDeclaredExtensions();
	assertTrue("2.4 Got 4 extensions", extensions.length == 4);
	assertNotNull("2.5 Got x1 extension", ((IPluginDescriptor)plugin).getExtension("x1"));
	assertNotNull("2.6 Got x2 extension", ((IPluginDescriptor)plugin).getExtension("x2"));
	assertNotNull("2.7 Got x3 extension", ((IPluginDescriptor)plugin).getExtension("x3"));
	assertNotNull("2.8 Got x4 extension", ((IPluginDescriptor)plugin).getExtension("x4"));
}

public void libraryTest() {
	// Ensure fragment library information is not being added twice to the plugin
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "basicFragmentTest", null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String[] pluginPaths = new String[1];
	pluginPaths[0] = tempPlugin.getLocation().concat("Plugin_Testing/PR1GHFPFYTest/libraryTest/");
	URL pluginURLs[] = new URL[1];
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
	IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors("PR1GHFPFYPluginTest");
	assertTrue("3.0 Only one plugin", pluginDescriptors.length == 1);
	PluginDescriptorModel plugin = (PluginDescriptorModel)pluginDescriptors[0];
	assertTrue("3.1 Got the right plugin", plugin.getId().equals("PR1GHFPFYPluginTest"));
	PluginFragmentModel[] fragmentList = ((PluginRegistryModel)registry).getFragments();
	assertTrue("3.2 Only one fragment", fragmentList.length == 1);
	PluginFragmentModel fragment = fragmentList[0];
	assertTrue("3.3 Got the right fragment", fragment.getId().equals("PR1GHFPFYFragmentTest"));
	
	// Check libraries
	LibraryModel[] libraries = plugin.getRuntime();
	assertTrue ("3.4 Got 4 libraries", libraries.length == 4);
	int idx1 = -1;
	int idx2 = -1;
	int idx3 = -1;
	int idx4 = -1;
	for (int i = 0; i < libraries.length; i++) {
		if (libraries[i].getName().equals("lib1.jar")) {
			idx1 = i;
		} else if (libraries[i].getName().equals("lib2.jar")) {
			idx2 = i;
		} else if (libraries[i].getName().equals("lib3.jar")) {
			idx3 = i;
		} else if (libraries[i].getName().equals("lib4.jar")) {
			idx4 = i;
		}
	}
	assertTrue("3.5 Have lib1.jar library", idx1 != -1);
	assertTrue("3.6 Have lib2.jar library", idx2 != -1);
	assertTrue("3.7 Have lib3.jar library", idx3 != -1);
	assertTrue("3.8 Have lib4.jar library", idx4 != -1);
	
}


public void requiresTest() {
	// Ensure fragment prerequisite information is not being added twice to the plugin
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "basicFragmentTest", null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String[] pluginPaths = new String[1];
	pluginPaths[0] = tempPlugin.getLocation().concat("Plugin_Testing/PR1GHFPFYTest/requiresTest/");
	URL pluginURLs[] = new URL[1];
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
	IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors("PR1GHFPFYPluginTest");
	assertTrue("4.0 Only one plugin", pluginDescriptors.length == 1);
	PluginDescriptorModel plugin = (PluginDescriptorModel)pluginDescriptors[0];
	assertTrue("4.1 Got the right plugin", plugin.getId().equals("PR1GHFPFYPluginTest"));
	PluginFragmentModel[] fragmentList = ((PluginRegistryModel)registry).getFragments();
	assertTrue("4.2 Only one fragment", fragmentList.length == 1);
	PluginFragmentModel fragment = fragmentList[0];
	assertTrue("4.3 Got the right fragment", fragment.getId().equals("PR1GHFPFYFragmentTest"));
	
	// Check requires
	PluginPrerequisiteModel[] requires = plugin.getRequires();
	assertTrue ("4.4 Got 4 prerequisites", requires.length == 4);
	int idx1 = -1;
	int idx2 = -1;
	int idx3 = -1;
	int idx4 = -1;
	for (int i = 0; i < requires.length; i++) {
		if (requires[i].getPlugin().equals("testa")) {
			idx1 = i;
		} else if (requires[i].getPlugin().equals("testb")) {
			idx2 = i;
		} else if (requires[i].getPlugin().equals("testc")) {
			idx3 = i;
		} else if (requires[i].getPlugin().equals("testd")) {
			idx4 = i;
		}
	}
	assertTrue("4.5 Have testa prerequisite", idx1 != -1);
	assertTrue("4.6 Have testb prerequisite", idx2 != -1);
	assertTrue("4.7 Have testc prerequisite", idx3 != -1);
	assertTrue("4.8 Have testd prerequisite", idx4 != -1);
}

public void fragmentVersionTest() {
	// Ensure that we pick up the latest version of a fragment to augment a plugin
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "basicFragmentTest", null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String[] pluginPaths = new String[4];
	pluginPaths[0] = tempPlugin.getLocation().concat("Plugin_Testing/PR1GHH81DTest/plugin.xml");
	pluginPaths[1] = tempPlugin.getLocation().concat("Plugin_Testing/PR1GHH81DTest/fragment1.xml");
	pluginPaths[2] = tempPlugin.getLocation().concat("Plugin_Testing/PR1GHH81DTest/fragment2.xml");
	pluginPaths[3] = tempPlugin.getLocation().concat("Plugin_Testing/PR1GHH81DTest/fragment3.xml");
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
	IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors("PR1GHH81DPluginTest");
	assertTrue("5.0 Only one plugin", pluginDescriptors.length == 1);
	PluginDescriptorModel plugin = (PluginDescriptorModel)pluginDescriptors[0];
	assertTrue("5.1 Got the right plugin", plugin.getId().equals("PR1GHH81DPluginTest"));
	PluginFragmentModel[] fragmentList = ((PluginRegistryModel)registry).getFragments();
	assertTrue("5.2 Three fragments", fragmentList.length == 3);
	
	// Now make sure that the resulting plugin descriptor picked up only the latest
	// version of the fragment. This means there should be a total of 5 library entries:
	// lib1, lib2, lib5, lib6, and lib7.
	LibraryModel[] libraries = plugin.getRuntime();
	assertTrue ("5.3 Got 5 libraries", libraries.length == 5);
	int idx1 = -1;
	int idx2 = -1;
	int idx5 = -1;
	int idx6 = -1;
	int idx7 = -1;
	for (int i = 0; i < libraries.length; i++) {
		if (libraries[i].getName().equals("lib1.jar")) {
			idx1 = i;
		} else if (libraries[i].getName().equals("lib2.jar")) {
			idx2 = i;
		} else if (libraries[i].getName().equals("lib5.jar")) {
			idx5 = i;
		} else if (libraries[i].getName().equals("lib6.jar")) {
			idx6 = i;
		} else if (libraries[i].getName().equals("lib7.jar")) {
			idx7 = i;
		}
	}
	assertTrue("5.4 Have lib1.jar library", idx1 != -1);
	assertTrue("5.5 Have lib2.jar library", idx2 != -1);
	assertTrue("5.6 Have lib5.jar library", idx5 != -1);
	assertTrue("5.7 Have lib6.jar library", idx6 != -1);
	assertTrue("5.8 Have lib7.jar library", idx7 != -1);
}

}

