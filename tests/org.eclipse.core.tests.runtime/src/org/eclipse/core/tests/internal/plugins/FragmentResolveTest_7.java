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

import java.net.URL;
import junit.framework.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.model.*;
import org.eclipse.core.internal.plugins.InternalFactory;
import org.eclipse.core.internal.plugins.*;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.tests.harness.*;

public class FragmentResolveTest_7 extends EclipseWorkspaceTest {
public FragmentResolveTest_7() {
	super(null);
}
public FragmentResolveTest_7(String name) {
	super(name);
}

public void test1() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "fragmentResolveTest", null);
	InternalFactory factory = new InternalFactory(problems);
	URL pluginURLs[] = new URL[2];
	try {
		PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
		String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.7/plugin1.xml");
		pluginURLs[0] = new URL (pluginPath);
		pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.7/fragment1.xml");
		pluginURLs[1] = new URL (pluginPath);
	} catch (java.net.MalformedURLException e) {
		fail("1.0.1 Unexpected exception - " + e.getMessage());
	}
	IPluginRegistry registry = ParseHelper.doParsing(factory, pluginURLs, true);
	IPluginDescriptor pd = registry.getPluginDescriptor("tests.a");

	// check descriptor
	assertNotNull("1.0", pd);
	assertTrue("1.1", pd.getUniqueIdentifier().equals("tests.a"));

	// check to see if we have all plugins
	IPluginDescriptor[] all = registry.getPluginDescriptors();
	assertTrue("1.2", all.length == 1);
	assertTrue("1.3", all[0].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.4", all[0].getVersionIdentifier().equals(new PluginVersionIdentifier("1.2.0")));

	// Check the fragment list.  There should only be one
	PluginFragmentModel[] fragment = ((PluginRegistry) registry).getFragments("fragmentTest");
	assertTrue("1.5", fragment.length == 1);
	assertTrue("1.6", fragment[0].getId().equals("fragmentTest"));
	
	// Have we got all the fragments
	PluginFragmentModel[] fragments = ((PluginRegistry)registry).getFragments();
	assertTrue("1.7", fragments.length == 1);
	assertTrue("1.8", fragments[0].getId().equals("fragmentTest"));
	assertTrue("1.9", fragments[0].getPluginVersion().equals("1.2.0"));
	
	// Now make sure we hooked this fragment and this plugin
	PluginFragmentModel[] linkedFragments = ((PluginDescriptorModel)pd).getFragments();
	assertNotNull("1.10", linkedFragments);
	assertTrue("1.11", linkedFragments.length == 1);
	assertTrue("1.12", linkedFragments[0].getId().equals("fragmentTest"));
	
	// Finally, make sure the library entry in the fragment is
	// now part of the plugin
	ILibrary[] libraries = pd.getRuntimeLibraries();
	assertTrue("1.13", libraries.length == 1);
	assertTrue("1.14", ((Library)libraries[0]).getName().equals("lib1.jar"));
}

public void test2() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "fragmentResolveTest", null);
	InternalFactory factory = new InternalFactory(problems);
	URL pluginURLs[] = new URL[2];
	try {
		PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
		String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.7/plugin1.xml");
		pluginURLs[0] = new URL (pluginPath);
		pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.7/fragment2.xml");
		pluginURLs[1] = new URL (pluginPath);
	} catch (java.net.MalformedURLException e) {
		fail("2.0.1 Unexpected exception - " + e.getMessage());
	}
	IPluginRegistry registry = ParseHelper.doParsing(factory, pluginURLs, true);
	IPluginDescriptor pd = registry.getPluginDescriptor("tests.a");

	// check descriptor
	assertNotNull("2.0", pd);
	assertTrue("2.1", pd.getUniqueIdentifier().equals("tests.a"));

	// check to see if we have all plugins
	IPluginDescriptor[] all = registry.getPluginDescriptors();
	assertTrue("2.2", all.length == 1);
	assertTrue("2.3", all[0].getUniqueIdentifier().equals("tests.a"));
	assertTrue("2.4", all[0].getVersionIdentifier().equals(new PluginVersionIdentifier("1.2.0")));

	// Check the fragment list.  There should only be one
	PluginFragmentModel[] fragment = ((PluginRegistry) registry).getFragments("fragmentTest");
	assertTrue("2.5", fragment.length == 1);
	assertTrue("2.6", fragment[0].getId().equals("fragmentTest"));
	
	// Have we got all the fragments
	PluginFragmentModel[] fragments = ((PluginRegistry)registry).getFragments();
	assertTrue("2.7", fragments.length == 1);
	assertTrue("2.8", fragments[0].getId().equals("fragmentTest"));
	assertTrue("2.9", fragments[0].getPluginVersion().equals("1.3.0"));
	
	// Now make sure we didn't hook this fragment and this plugin (they aren't compatible)
	PluginFragmentModel[] linkedFragments = ((PluginDescriptorModel)pd).getFragments();
	assertNull("2.10", linkedFragments);
	
	// Finally, make sure the library entry in the fragment is
	// not part of the plugin
	ILibrary[] libraries = pd.getRuntimeLibraries();
	assertTrue("2.11", libraries.length == 0);
}

public void test3() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "fragmentResolveTest", null);
	InternalFactory factory = new InternalFactory(problems);
	URL pluginURLs[] = new URL[2];
	try {
		PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
		String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.7/plugin1.xml");
		pluginURLs[0] = new URL (pluginPath);
		pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.7/fragment3.xml");
		pluginURLs[1] = new URL (pluginPath);
	} catch (java.net.MalformedURLException e) {
		fail("3.0.1 Unexpected exception - " + e.getMessage());
	}
	IPluginRegistry registry = ParseHelper.doParsing(factory, pluginURLs, true);
	IPluginDescriptor pd = registry.getPluginDescriptor("tests.a");

	// check descriptor
	assertNotNull("3.0", pd);
	assertTrue("3.1", pd.getUniqueIdentifier().equals("tests.a"));

	// check to see if we have all plugins
	IPluginDescriptor[] all = registry.getPluginDescriptors();
	assertTrue("3.2", all.length == 1);
	assertTrue("3.3", all[0].getUniqueIdentifier().equals("tests.a"));
	assertTrue("3.4", all[0].getVersionIdentifier().equals(new PluginVersionIdentifier("1.2.0")));

	// Check the fragment list.  There should only be one
	PluginFragmentModel[] fragment = ((PluginRegistry) registry).getFragments("fragmentTest");
	assertTrue("3.5", fragment.length == 1);
	assertTrue("3.6", fragment[0].getId().equals("fragmentTest"));
	
	// Have we got all the fragments
	PluginFragmentModel[] fragments = ((PluginRegistry)registry).getFragments();
	assertTrue("3.7", fragments.length == 1);
	assertTrue("3.8", fragments[0].getId().equals("fragmentTest"));
	assertTrue("3.9", fragments[0].getPluginVersion().equals("1.1.0"));
	
	// Now make sure we hooked this fragment and this plugin
	PluginFragmentModel[] linkedFragments = ((PluginDescriptorModel)pd).getFragments();
	assertNotNull("3.10", linkedFragments);
	assertTrue("3.11", linkedFragments.length == 1);
	assertTrue("3.12", linkedFragments[0].getId().equals("fragmentTest"));
	
	// Finally, make sure the library entry in the fragment is
	// now part of the plugin
	ILibrary[] libraries = pd.getRuntimeLibraries();
	assertTrue("3.13", libraries.length == 1);
	assertTrue("3.14", ((Library)libraries[0]).getName().equals("lib1.jar"));
}

public void test4() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "fragmentResolveTest", null);
	InternalFactory factory = new InternalFactory(problems);
	URL pluginURLs[] = new URL[2];
	try {
		PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
		String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.7/plugin1.xml");
		pluginURLs[0] = new URL (pluginPath);
		pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.7/fragment4.xml");
		pluginURLs[1] = new URL (pluginPath);
	} catch (java.net.MalformedURLException e) {
		fail("4.0.1 Unexpected exception - " + e.getMessage());
	}
	IPluginRegistry registry = ParseHelper.doParsing(factory, pluginURLs, true);
	IPluginDescriptor pd = registry.getPluginDescriptor("tests.a");

	// check descriptor
	assertNotNull("4.0", pd);
	assertTrue("4.1", pd.getUniqueIdentifier().equals("tests.a"));

	// check to see if we have all plugins
	IPluginDescriptor[] all = registry.getPluginDescriptors();
	assertTrue("4.2", all.length == 1);
	assertTrue("4.3", all[0].getUniqueIdentifier().equals("tests.a"));
	assertTrue("4.4", all[0].getVersionIdentifier().equals(new PluginVersionIdentifier("1.2.0")));

	// Check the fragment list.  There should only be one
	PluginFragmentModel[] fragment = ((PluginRegistry) registry).getFragments("fragmentTest");
	assertTrue("4.5", fragment.length == 1);
	assertTrue("4.6", fragment[0].getId().equals("fragmentTest"));
	
	// Have we got all the fragments
	PluginFragmentModel[] fragments = ((PluginRegistry)registry).getFragments();
	assertTrue("4.7", fragments.length == 1);
	assertTrue("4.8", fragments[0].getId().equals("fragmentTest"));
	assertTrue("4.9", fragments[0].getPluginVersion().equals("2.0.0"));
	
	// Now make sure we didn't hook this fragment and this plugin (they aren't compatible)
	PluginFragmentModel[] linkedFragments = ((PluginDescriptorModel)pd).getFragments();
	assertNull("4.10", linkedFragments);
	
	// Finally, make sure the library entry in the fragment is
	// not part of the plugin
	ILibrary[] libraries = pd.getRuntimeLibraries();
	assertTrue("4.11", libraries.length == 0);
}

public void test5() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "fragmentResolveTest", null);
	InternalFactory factory = new InternalFactory(problems);
	URL pluginURLs[] = new URL[2];
	try {
		PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
		String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.7/plugin1.xml");
		pluginURLs[0] = new URL (pluginPath);
		pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.7/fragment5.xml");
		pluginURLs[1] = new URL (pluginPath);
	} catch (java.net.MalformedURLException e) {
		fail("5.0.1 Unexpected exception - " + e.getMessage());
	}
	IPluginRegistry registry = ParseHelper.doParsing(factory, pluginURLs, true);
	IPluginDescriptor pd = registry.getPluginDescriptor("tests.a");

	// check descriptor
	assertNotNull("5.0", pd);
	assertTrue("5.1", pd.getUniqueIdentifier().equals("tests.a"));

	// check to see if we have all plugins
	IPluginDescriptor[] all = registry.getPluginDescriptors();
	assertTrue("5.2", all.length == 1);
	assertTrue("5.3", all[0].getUniqueIdentifier().equals("tests.a"));
	assertTrue("5.4", all[0].getVersionIdentifier().equals(new PluginVersionIdentifier("1.2.0")));

	// Check the fragment list.  There should only be one
	PluginFragmentModel[] fragment = ((PluginRegistry) registry).getFragments("fragmentTest");
	assertTrue("5.5", fragment.length == 1);
	assertTrue("5.6", fragment[0].getId().equals("fragmentTest"));
	
	// Have we got all the fragments
	PluginFragmentModel[] fragments = ((PluginRegistry)registry).getFragments();
	assertTrue("5.7", fragments.length == 1);
	assertTrue("5.8", fragments[0].getId().equals("fragmentTest"));
	assertTrue("5.9", fragments[0].getPluginVersion().equals("1.2.1"));
	
	// Now make sure we didn't hook this fragment and this plugin.
	// They are not compatible unless the service number is 0 in this case.
	PluginFragmentModel[] linkedFragments = ((PluginDescriptorModel)pd).getFragments();
	assertNull("5.10", linkedFragments);
	
	// Finally, make sure the library entry in the fragment is
	// not part of the plugin
	ILibrary[] libraries = pd.getRuntimeLibraries();
	assertTrue("5.11", libraries.length == 0);
}

public void test6() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "fragmentResolveTest", null);
	InternalFactory factory = new InternalFactory(problems);
	URL pluginURLs[] = new URL[2];
	try {
		PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
		String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.7/plugin2.xml");
		pluginURLs[0] = new URL (pluginPath);
		pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.7/fragment6.xml");
		pluginURLs[1] = new URL (pluginPath);
	} catch (java.net.MalformedURLException e) {
		fail("6.0.1 Unexpected exception - " + e.getMessage());
	}
	IPluginRegistry registry = ParseHelper.doParsing(factory, pluginURLs, true);
	IPluginDescriptor pd = registry.getPluginDescriptor("tests.a");

	// check descriptor
	assertNotNull("6.0", pd);
	assertTrue("6.1", pd.getUniqueIdentifier().equals("tests.a"));

	// check to see if we have all plugins
	IPluginDescriptor[] all = registry.getPluginDescriptors();
	assertTrue("6.2", all.length == 1);
	assertTrue("6.3", all[0].getUniqueIdentifier().equals("tests.a"));
	assertTrue("6.4", all[0].getVersionIdentifier().equals(new PluginVersionIdentifier("1.2.6")));

	// Check the fragment list.  There should only be one
	PluginFragmentModel[] fragment = ((PluginRegistry) registry).getFragments("fragmentTest");
	assertTrue("6.5", fragment.length == 1);
	assertTrue("6.6", fragment[0].getId().equals("fragmentTest"));
	
	// Have we got all the fragments
	PluginFragmentModel[] fragments = ((PluginRegistry)registry).getFragments();
	assertTrue("6.7", fragments.length == 1);
	assertTrue("6.8", fragments[0].getId().equals("fragmentTest"));
	assertTrue("6.9", fragments[0].getPluginVersion().equals("1.2.4"));
	
	// Now make sure we hooked this fragment and this plugin
	PluginFragmentModel[] linkedFragments = ((PluginDescriptorModel)pd).getFragments();
	assertNotNull("6.10", linkedFragments);
	assertTrue("6.11", linkedFragments.length == 1);
	assertTrue("6.12", linkedFragments[0].getId().equals("fragmentTest"));
	
	// Finally, make sure the library entry in the fragment is
	// now part of the plugin
	ILibrary[] libraries = pd.getRuntimeLibraries();
	assertTrue("6.13", libraries.length == 1);
	assertTrue("6.14", ((Library)libraries[0]).getName().equals("lib1.jar"));
}

public void test7() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "fragmentResolveTest", null);
	InternalFactory factory = new InternalFactory(problems);
	URL pluginURLs[] = new URL[2];
	try {
		PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
		String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.7/plugin2.xml");
		pluginURLs[0] = new URL (pluginPath);
		pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.7/fragment7.xml");
		pluginURLs[1] = new URL (pluginPath);
	} catch (java.net.MalformedURLException e) {
		fail("7.0.1 Unexpected exception - " + e.getMessage());
	}
	IPluginRegistry registry = ParseHelper.doParsing(factory, pluginURLs, true);
	IPluginDescriptor pd = registry.getPluginDescriptor("tests.a");

	// check descriptor
	assertNotNull("7.0", pd);
	assertTrue("7.1", pd.getUniqueIdentifier().equals("tests.a"));

	// check to see if we have all plugins
	IPluginDescriptor[] all = registry.getPluginDescriptors();
	assertTrue("7.2", all.length == 1);
	assertTrue("7.3", all[0].getUniqueIdentifier().equals("tests.a"));
	assertTrue("7.4", all[0].getVersionIdentifier().equals(new PluginVersionIdentifier("1.2.6")));

	// Check the fragment list.  There should only be one
	PluginFragmentModel[] fragment = ((PluginRegistry) registry).getFragments("fragmentTest");
	assertTrue("7.5", fragment.length == 1);
	assertTrue("7.6", fragment[0].getId().equals("fragmentTest"));
	
	// Have we got all the fragments
	PluginFragmentModel[] fragments = ((PluginRegistry)registry).getFragments();
	assertTrue("7.7", fragments.length == 1);
	assertTrue("7.8", fragments[0].getId().equals("fragmentTest"));
	assertTrue("7.9", fragments[0].getPluginVersion().equals("1.2.8"));
	
	// Now make sure we didn't hook this fragment and this plugin
	PluginFragmentModel[] linkedFragments = ((PluginDescriptorModel)pd).getFragments();
	assertNull("7.10", linkedFragments);
	
	// Finally, make sure the library entry in the fragment is
	// not part of the plugin
	ILibrary[] libraries = pd.getRuntimeLibraries();
	assertTrue("7.11", libraries.length == 0);
}

public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new FragmentResolveTest_7("test1"));
	suite.addTest(new FragmentResolveTest_7("test2"));
	suite.addTest(new FragmentResolveTest_7("test3"));
	suite.addTest(new FragmentResolveTest_7("test4"));
	suite.addTest(new FragmentResolveTest_7("test5"));
	suite.addTest(new FragmentResolveTest_7("test6"));
	suite.addTest(new FragmentResolveTest_7("test7"));
	return suite;
}
}


