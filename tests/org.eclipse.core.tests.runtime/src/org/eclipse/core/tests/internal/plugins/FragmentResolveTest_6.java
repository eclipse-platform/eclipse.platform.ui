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

public class FragmentResolveTest_6 extends EclipseWorkspaceTest {
public FragmentResolveTest_6() {
	super(null);
}
public FragmentResolveTest_6(String name) {
	super(name);
}

static PluginRegistry doParsing(InternalFactory factory, URL[] pluginPath, boolean doResolve) {
	PluginRegistry registry = (PluginRegistry) RegistryLoader.parseRegistry(pluginPath, factory, false);
	if (doResolve)
		// don't trim the disabled plugins for these tests
		registry.resolve(false, true);
	registry.markReadOnly();
	registry.startup(null);
	return registry;
}

public void test1() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "fragmentResolveTest", null);
	InternalFactory factory = new InternalFactory(problems);
	URL pluginURLs[] = new URL[7];
	String[] pluginPath = new String[7];
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	try {
		pluginPath[0] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin1.xml");
		pluginPath[1] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin2.xml");
		pluginPath[2] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin3.xml");
		pluginPath[3] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin4.xml");
		pluginPath[4] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin5.xml");
		pluginPath[5] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin6.xml");
		pluginPath[6] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/fragment1.xml");
		for (int i = 0; i < pluginPath.length; i++) {
			pluginURLs[i] = new URL (pluginPath[i]);
		}
	} catch (java.net.MalformedURLException e) {
		fail("1.0.1 Unexpected exception - " + e.getMessage());
	}
	
	IPluginRegistry registry = doParsing(factory, pluginURLs, true);

	// We should have 5 plugins all with id 'tests.a'
	IPluginDescriptor[] all = registry.getPluginDescriptors();
	assertTrue("1.0", all.length == 6);
	assertTrue("1.1", all[0].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.2", all[1].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.3", all[2].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.4", all[3].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.5", all[4].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.6", all[5].getUniqueIdentifier().equals("tests.a"));

	// Make sure we got all the version numbers
	IPluginDescriptor pd125 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.2.5"));
	IPluginDescriptor pd128 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.2.8"));
	IPluginDescriptor pd123 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.2.3"));
	IPluginDescriptor pd130 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.3.0"));
	IPluginDescriptor pd300 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("3.0.0"));
	IPluginDescriptor pd453 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("4.5.3"));
	assertNotNull("1.7", pd125);
	assertNotNull("1.8", pd128);
	assertNotNull("1.9", pd123);
	assertNotNull("1.10", pd130);
	assertNotNull("1.11", pd300);
	assertNotNull("1.12", pd453);

	// Check the fragment list.  There should only be one
	PluginFragmentModel[] fragments = ((PluginRegistry)registry).getFragments();
	assertTrue("1.13", fragments.length == 1);
	assertTrue("1.14", fragments[0].getId().equals("fragmentTest"));
	assertTrue("1.15", fragments[0].getPluginVersion().equals("5.9.8"));
	
	// Now make sure we didn't hook this fragment to any plugin
	PluginFragmentModel[] linkedFragments125 = ((PluginDescriptorModel)pd125).getFragments();
	PluginFragmentModel[] linkedFragments128 = ((PluginDescriptorModel)pd128).getFragments();
	PluginFragmentModel[] linkedFragments123 = ((PluginDescriptorModel)pd123).getFragments();
	PluginFragmentModel[] linkedFragments130 = ((PluginDescriptorModel)pd130).getFragments();
	PluginFragmentModel[] linkedFragments300 = ((PluginDescriptorModel)pd300).getFragments();
	PluginFragmentModel[] linkedFragments453 = ((PluginDescriptorModel)pd453).getFragments();
	assertNull("1.16", linkedFragments125);
	assertNull("1.17", linkedFragments128);
	assertNull("1.18", linkedFragments123);
	assertNull("1.19", linkedFragments130);
	assertNull("1.20", linkedFragments300);
	assertNull("1.21", linkedFragments453);
	
	// Finally, make sure the library entry in the fragment is
	// not part of any plugin
	ILibrary[] libraries125 = pd125.getRuntimeLibraries();
	ILibrary[] libraries128 = pd128.getRuntimeLibraries();
	ILibrary[] libraries123 = pd123.getRuntimeLibraries();
	ILibrary[] libraries130 = pd130.getRuntimeLibraries();
	ILibrary[] libraries300 = pd300.getRuntimeLibraries();
	ILibrary[] libraries453 = pd453.getRuntimeLibraries();
	assertTrue("1.22", libraries125.length == 0);
	assertTrue("1.23", libraries128.length == 0);
	assertTrue("1.24", libraries123.length == 0);
	assertTrue("1.25", libraries130.length == 0);
	assertTrue("1.26", libraries300.length == 0);
	assertTrue("1.27", libraries453.length == 0);
}

public void test2() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "fragmentResolveTest", null);
	InternalFactory factory = new InternalFactory(problems);
	URL pluginURLs[] = new URL[7];
	String[] pluginPath = new String[7];
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	try {
		pluginPath[0] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin1.xml");
		pluginPath[1] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin2.xml");
		pluginPath[2] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin3.xml");
		pluginPath[3] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin4.xml");
		pluginPath[4] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin5.xml");
		pluginPath[5] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin6.xml");
		pluginPath[6] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/fragment2.xml");
		for (int i = 0; i < pluginPath.length; i++) {
			pluginURLs[i] = new URL (pluginPath[i]);
		}
	} catch (java.net.MalformedURLException e) {
		fail("1.0.1 Unexpected exception - " + e.getMessage());
	}
	
	IPluginRegistry registry = doParsing(factory, pluginURLs, true);

	// We should have 5 plugins all with id 'tests.a'
	IPluginDescriptor[] all = registry.getPluginDescriptors();
	assertTrue("1.0", all.length == 6);
	assertTrue("1.1", all[0].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.2", all[1].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.3", all[2].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.4", all[3].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.5", all[4].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.6", all[5].getUniqueIdentifier().equals("tests.a"));

	// Make sure we got all the version numbers
	IPluginDescriptor pd125 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.2.5"));
	IPluginDescriptor pd128 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.2.8"));
	IPluginDescriptor pd123 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.2.3"));
	IPluginDescriptor pd130 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.3.0"));
	IPluginDescriptor pd300 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("3.0.0"));
	IPluginDescriptor pd453 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("4.5.3"));
	assertNotNull("1.7", pd125);
	assertNotNull("1.8", pd128);
	assertNotNull("1.9", pd123);
	assertNotNull("1.10", pd130);
	assertNotNull("1.11", pd300);
	assertNotNull("1.12", pd453);

	// Check the fragment list.  There should only be one
	PluginFragmentModel[] fragments = ((PluginRegistry)registry).getFragments();
	assertTrue("1.13", fragments.length == 1);
	assertTrue("1.14", fragments[0].getId().equals("fragmentTest"));
	assertTrue("1.15", fragments[0].getPluginVersion().equals("1.2.3"));
	
	// Now make sure we hooked this fragment to the right plugin (and only
	// one plugin)
	PluginFragmentModel[] linkedFragments125 = ((PluginDescriptorModel)pd125).getFragments();
	PluginFragmentModel[] linkedFragments128 = ((PluginDescriptorModel)pd128).getFragments();
	PluginFragmentModel[] linkedFragments123 = ((PluginDescriptorModel)pd123).getFragments();
	PluginFragmentModel[] linkedFragments130 = ((PluginDescriptorModel)pd130).getFragments();
	PluginFragmentModel[] linkedFragments300 = ((PluginDescriptorModel)pd300).getFragments();
	PluginFragmentModel[] linkedFragments453 = ((PluginDescriptorModel)pd453).getFragments();
	assertNull("1.16", linkedFragments125);
	assertNotNull("1.17", linkedFragments128);
	assertNull("1.18", linkedFragments123);
	assertNull("1.19", linkedFragments130);
	assertNull("1.20", linkedFragments300);
	assertNull("1.21", linkedFragments453);
	assertTrue("1.22", linkedFragments128.length == 1);
	assertTrue("1.23", linkedFragments128[0].getId().equals("fragmentTest"));
	
	// Finally, make sure the library entry in the fragment is
	// now part of the proper plugin
	ILibrary[] libraries125 = pd125.getRuntimeLibraries();
	ILibrary[] libraries128 = pd128.getRuntimeLibraries();
	ILibrary[] libraries123 = pd123.getRuntimeLibraries();
	ILibrary[] libraries130 = pd130.getRuntimeLibraries();
	ILibrary[] libraries300 = pd300.getRuntimeLibraries();
	ILibrary[] libraries453 = pd453.getRuntimeLibraries();
	assertTrue("1.24", libraries125.length == 0);
	assertTrue("1.25", libraries128.length == 1);
	assertTrue("1.26", libraries123.length == 0);
	assertTrue("1.27", libraries130.length == 0);
	assertTrue("1.28", libraries300.length == 0);
	assertTrue("1.29", libraries453.length == 0);
	assertTrue("1.30", ((Library)libraries128[0]).getName().equals("lib1.jar"));
}

public void test3() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "fragmentResolveTest", null);
	InternalFactory factory = new InternalFactory(problems);
	URL pluginURLs[] = new URL[7];
	String[] pluginPath = new String[7];
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	try {
		pluginPath[0] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin1.xml");
		pluginPath[1] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin2.xml");
		pluginPath[2] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin3.xml");
		pluginPath[3] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin4.xml");
		pluginPath[4] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin5.xml");
		pluginPath[5] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin6.xml");
		pluginPath[6] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/fragment3.xml");
		for (int i = 0; i < pluginPath.length; i++) {
			pluginURLs[i] = new URL (pluginPath[i]);
		}
	} catch (java.net.MalformedURLException e) {
		fail("1.0.1 Unexpected exception - " + e.getMessage());
	}
	
	IPluginRegistry registry = doParsing(factory, pluginURLs, true);

	// We should have 5 plugins all with id 'tests.a'
	IPluginDescriptor[] all = registry.getPluginDescriptors();
	assertTrue("1.0", all.length == 6);
	assertTrue("1.1", all[0].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.2", all[1].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.3", all[2].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.4", all[3].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.5", all[4].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.6", all[5].getUniqueIdentifier().equals("tests.a"));

	// Make sure we got all the version numbers
	IPluginDescriptor pd125 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.2.5"));
	IPluginDescriptor pd128 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.2.8"));
	IPluginDescriptor pd123 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.2.3"));
	IPluginDescriptor pd130 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.3.0"));
	IPluginDescriptor pd300 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("3.0.0"));
	IPluginDescriptor pd453 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("4.5.3"));
	assertNotNull("1.7", pd125);
	assertNotNull("1.8", pd128);
	assertNotNull("1.9", pd123);
	assertNotNull("1.10", pd130);
	assertNotNull("1.11", pd300);
	assertNotNull("1.12", pd453);

	// Check the fragment list.  There should only be one
	PluginFragmentModel[] fragments = ((PluginRegistry)registry).getFragments();
	assertTrue("1.13", fragments.length == 1);
	assertTrue("1.14", fragments[0].getId().equals("fragmentTest"));
	assertTrue("1.15", fragments[0].getPluginVersion().equals("1.2.4"));
	
	// Now make sure we hooked this fragment to the right plugin (and only
	// one plugin)
	PluginFragmentModel[] linkedFragments125 = ((PluginDescriptorModel)pd125).getFragments();
	PluginFragmentModel[] linkedFragments128 = ((PluginDescriptorModel)pd128).getFragments();
	PluginFragmentModel[] linkedFragments123 = ((PluginDescriptorModel)pd123).getFragments();
	PluginFragmentModel[] linkedFragments130 = ((PluginDescriptorModel)pd130).getFragments();
	PluginFragmentModel[] linkedFragments300 = ((PluginDescriptorModel)pd300).getFragments();
	PluginFragmentModel[] linkedFragments453 = ((PluginDescriptorModel)pd453).getFragments();
	assertNull("1.16", linkedFragments125);
	assertNotNull("1.17", linkedFragments128);
	assertNull("1.18", linkedFragments123);
	assertNull("1.19", linkedFragments130);
	assertNull("1.20", linkedFragments300);
	assertNull("1.21", linkedFragments453);
	assertTrue("1.22", linkedFragments128.length == 1);
	assertTrue("1.23", linkedFragments128[0].getId().equals("fragmentTest"));
	
	// Finally, make sure the library entry in the fragment is
	// now part of the proper plugin
	ILibrary[] libraries125 = pd125.getRuntimeLibraries();
	ILibrary[] libraries128 = pd128.getRuntimeLibraries();
	ILibrary[] libraries123 = pd123.getRuntimeLibraries();
	ILibrary[] libraries130 = pd130.getRuntimeLibraries();
	ILibrary[] libraries300 = pd300.getRuntimeLibraries();
	ILibrary[] libraries453 = pd453.getRuntimeLibraries();
	assertTrue("1.24", libraries125.length == 0);
	assertTrue("1.25", libraries128.length == 1);
	assertTrue("1.26", libraries123.length == 0);
	assertTrue("1.27", libraries130.length == 0);
	assertTrue("1.28", libraries300.length == 0);
	assertTrue("1.29", libraries453.length == 0);
	assertTrue("1.30", ((Library)libraries128[0]).getName().equals("lib1.jar"));
}

public void test4() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "fragmentResolveTest", null);
	InternalFactory factory = new InternalFactory(problems);
	URL pluginURLs[] = new URL[7];
	String[] pluginPath = new String[7];
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	try {
		pluginPath[0] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin1.xml");
		pluginPath[1] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin2.xml");
		pluginPath[2] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin3.xml");
		pluginPath[3] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin4.xml");
		pluginPath[4] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin5.xml");
		pluginPath[5] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin6.xml");
		pluginPath[6] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/fragment4.xml");
		for (int i = 0; i < pluginPath.length; i++) {
			pluginURLs[i] = new URL (pluginPath[i]);
		}
	} catch (java.net.MalformedURLException e) {
		fail("1.0.1 Unexpected exception - " + e.getMessage());
	}
	
	IPluginRegistry registry = doParsing(factory, pluginURLs, true);

	// We should have 5 plugins all with id 'tests.a'
	IPluginDescriptor[] all = registry.getPluginDescriptors();
	assertTrue("1.0", all.length == 6);
	assertTrue("1.1", all[0].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.2", all[1].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.3", all[2].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.4", all[3].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.5", all[4].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.6", all[5].getUniqueIdentifier().equals("tests.a"));

	// Make sure we got all the version numbers
	IPluginDescriptor pd125 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.2.5"));
	IPluginDescriptor pd128 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.2.8"));
	IPluginDescriptor pd123 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.2.3"));
	IPluginDescriptor pd130 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.3.0"));
	IPluginDescriptor pd300 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("3.0.0"));
	IPluginDescriptor pd453 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("4.5.3"));
	assertNotNull("1.7", pd125);
	assertNotNull("1.8", pd128);
	assertNotNull("1.9", pd123);
	assertNotNull("1.10", pd130);
	assertNotNull("1.11", pd300);
	assertNotNull("1.12", pd453);

	// Check the fragment list.  There should only be one
	PluginFragmentModel[] fragments = ((PluginRegistry)registry).getFragments();
	assertTrue("1.13", fragments.length == 1);
	assertTrue("1.14", fragments[0].getId().equals("fragmentTest"));
	assertTrue("1.15", fragments[0].getPluginVersion().equals("2.0.0"));
	
	// Now make sure we didn't hook this fragment to any plugin
	PluginFragmentModel[] linkedFragments125 = ((PluginDescriptorModel)pd125).getFragments();
	PluginFragmentModel[] linkedFragments128 = ((PluginDescriptorModel)pd128).getFragments();
	PluginFragmentModel[] linkedFragments123 = ((PluginDescriptorModel)pd123).getFragments();
	PluginFragmentModel[] linkedFragments130 = ((PluginDescriptorModel)pd130).getFragments();
	PluginFragmentModel[] linkedFragments300 = ((PluginDescriptorModel)pd300).getFragments();
	PluginFragmentModel[] linkedFragments453 = ((PluginDescriptorModel)pd453).getFragments();
	assertNull("1.16", linkedFragments125);
	assertNull("1.17", linkedFragments128);
	assertNull("1.18", linkedFragments123);
	assertNull("1.19", linkedFragments130);
	assertNull("1.20", linkedFragments300);
	assertNull("1.21", linkedFragments453);
	
	// Finally, make sure the library entry in the fragment is
	// not part of any plugin
	ILibrary[] libraries125 = pd125.getRuntimeLibraries();
	ILibrary[] libraries128 = pd128.getRuntimeLibraries();
	ILibrary[] libraries123 = pd123.getRuntimeLibraries();
	ILibrary[] libraries130 = pd130.getRuntimeLibraries();
	ILibrary[] libraries300 = pd300.getRuntimeLibraries();
	ILibrary[] libraries453 = pd453.getRuntimeLibraries();
	assertTrue("1.22", libraries125.length == 0);
	assertTrue("1.23", libraries128.length == 0);
	assertTrue("1.24", libraries123.length == 0);
	assertTrue("1.25", libraries130.length == 0);
	assertTrue("1.26", libraries300.length == 0);
	assertTrue("1.27", libraries453.length == 0);
}

public void test5() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "fragmentResolveTest", null);
	InternalFactory factory = new InternalFactory(problems);
	URL pluginURLs[] = new URL[7];
	String[] pluginPath = new String[7];
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	try {
		pluginPath[0] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin1.xml");
		pluginPath[1] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin2.xml");
		pluginPath[2] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin3.xml");
		pluginPath[3] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin4.xml");
		pluginPath[4] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin5.xml");
		pluginPath[5] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin6.xml");
		pluginPath[6] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/fragment5.xml");
		for (int i = 0; i < pluginPath.length; i++) {
			pluginURLs[i] = new URL (pluginPath[i]);
		}
	} catch (java.net.MalformedURLException e) {
		fail("1.0.1 Unexpected exception - " + e.getMessage());
	}
	
	IPluginRegistry registry = doParsing(factory, pluginURLs, true);

	// We should have 5 plugins all with id 'tests.a'
	IPluginDescriptor[] all = registry.getPluginDescriptors();
	assertTrue("1.0", all.length == 6);
	assertTrue("1.1", all[0].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.2", all[1].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.3", all[2].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.4", all[3].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.5", all[4].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.6", all[5].getUniqueIdentifier().equals("tests.a"));

	// Make sure we got all the version numbers
	IPluginDescriptor pd125 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.2.5"));
	IPluginDescriptor pd128 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.2.8"));
	IPluginDescriptor pd123 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.2.3"));
	IPluginDescriptor pd130 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.3.0"));
	IPluginDescriptor pd300 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("3.0.0"));
	IPluginDescriptor pd453 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("4.5.3"));
	assertNotNull("1.7", pd125);
	assertNotNull("1.8", pd128);
	assertNotNull("1.9", pd123);
	assertNotNull("1.10", pd130);
	assertNotNull("1.11", pd300);
	assertNotNull("1.12", pd453);

	// Check the fragment list.  There should only be one
	PluginFragmentModel[] fragments = ((PluginRegistry)registry).getFragments();
	assertTrue("1.13", fragments.length == 1);
	assertTrue("1.14", fragments[0].getId().equals("fragmentTest"));
	assertTrue("1.15", fragments[0].getPluginVersion().equals("3.0.4"));
	
	// Now make sure we didn't hook this fragment to any plugin
	PluginFragmentModel[] linkedFragments125 = ((PluginDescriptorModel)pd125).getFragments();
	PluginFragmentModel[] linkedFragments128 = ((PluginDescriptorModel)pd128).getFragments();
	PluginFragmentModel[] linkedFragments123 = ((PluginDescriptorModel)pd123).getFragments();
	PluginFragmentModel[] linkedFragments130 = ((PluginDescriptorModel)pd130).getFragments();
	PluginFragmentModel[] linkedFragments300 = ((PluginDescriptorModel)pd300).getFragments();
	PluginFragmentModel[] linkedFragments453 = ((PluginDescriptorModel)pd453).getFragments();
	assertNull("1.16", linkedFragments125);
	assertNull("1.17", linkedFragments128);
	assertNull("1.18", linkedFragments123);
	assertNull("1.19", linkedFragments130);
	assertNull("1.20", linkedFragments300);
	assertNull("1.21", linkedFragments453);
	
	// Finally, make sure the library entry in the fragment is
	// not part of any plugin
	ILibrary[] libraries125 = pd125.getRuntimeLibraries();
	ILibrary[] libraries128 = pd128.getRuntimeLibraries();
	ILibrary[] libraries123 = pd123.getRuntimeLibraries();
	ILibrary[] libraries130 = pd130.getRuntimeLibraries();
	ILibrary[] libraries300 = pd300.getRuntimeLibraries();
	ILibrary[] libraries453 = pd453.getRuntimeLibraries();
	assertTrue("1.22", libraries125.length == 0);
	assertTrue("1.23", libraries128.length == 0);
	assertTrue("1.24", libraries123.length == 0);
	assertTrue("1.25", libraries130.length == 0);
	assertTrue("1.26", libraries300.length == 0);
	assertTrue("1.27", libraries453.length == 0);
}

public void test6() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "fragmentResolveTest", null);
	InternalFactory factory = new InternalFactory(problems);
	URL pluginURLs[] = new URL[7];
	String[] pluginPath = new String[7];
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	try {
		pluginPath[0] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin1.xml");
		pluginPath[1] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin2.xml");
		pluginPath[2] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin3.xml");
		pluginPath[3] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin4.xml");
		pluginPath[4] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin5.xml");
		pluginPath[5] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin6.xml");
		pluginPath[6] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/fragment6.xml");
		for (int i = 0; i < pluginPath.length; i++) {
			pluginURLs[i] = new URL (pluginPath[i]);
		}
	} catch (java.net.MalformedURLException e) {
		fail("1.0.1 Unexpected exception - " + e.getMessage());
	}
	
	IPluginRegistry registry = doParsing(factory, pluginURLs, true);

	// We should have 5 plugins all with id 'tests.a'
	IPluginDescriptor[] all = registry.getPluginDescriptors();
	assertTrue("1.0", all.length == 6);
	assertTrue("1.1", all[0].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.2", all[1].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.3", all[2].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.4", all[3].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.5", all[4].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.6", all[5].getUniqueIdentifier().equals("tests.a"));

	// Make sure we got all the version numbers
	IPluginDescriptor pd125 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.2.5"));
	IPluginDescriptor pd128 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.2.8"));
	IPluginDescriptor pd123 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.2.3"));
	IPluginDescriptor pd130 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.3.0"));
	IPluginDescriptor pd300 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("3.0.0"));
	IPluginDescriptor pd453 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("4.5.3"));
	assertNotNull("1.7", pd125);
	assertNotNull("1.8", pd128);
	assertNotNull("1.9", pd123);
	assertNotNull("1.10", pd130);
	assertNotNull("1.11", pd300);
	assertNotNull("1.12", pd453);

	// Check the fragment list.  There should only be one
	PluginFragmentModel[] fragments = ((PluginRegistry)registry).getFragments();
	assertTrue("1.13", fragments.length == 1);
	assertTrue("1.14", fragments[0].getId().equals("fragmentTest"));
	assertTrue("1.15", fragments[0].getPluginVersion().equals("4.6.2"));
	
	// Now make sure we didn't hook this fragment to any plugin
	PluginFragmentModel[] linkedFragments125 = ((PluginDescriptorModel)pd125).getFragments();
	PluginFragmentModel[] linkedFragments128 = ((PluginDescriptorModel)pd128).getFragments();
	PluginFragmentModel[] linkedFragments123 = ((PluginDescriptorModel)pd123).getFragments();
	PluginFragmentModel[] linkedFragments130 = ((PluginDescriptorModel)pd130).getFragments();
	PluginFragmentModel[] linkedFragments300 = ((PluginDescriptorModel)pd300).getFragments();
	PluginFragmentModel[] linkedFragments453 = ((PluginDescriptorModel)pd453).getFragments();
	assertNull("1.16", linkedFragments125);
	assertNull("1.17", linkedFragments128);
	assertNull("1.18", linkedFragments123);
	assertNull("1.19", linkedFragments130);
	assertNull("1.20", linkedFragments300);
	assertNull("1.21", linkedFragments453);
	
	// Finally, make sure the library entry in the fragment is
	// not part of any plugin
	ILibrary[] libraries125 = pd125.getRuntimeLibraries();
	ILibrary[] libraries128 = pd128.getRuntimeLibraries();
	ILibrary[] libraries123 = pd123.getRuntimeLibraries();
	ILibrary[] libraries130 = pd130.getRuntimeLibraries();
	ILibrary[] libraries300 = pd300.getRuntimeLibraries();
	ILibrary[] libraries453 = pd453.getRuntimeLibraries();
	assertTrue("1.22", libraries125.length == 0);
	assertTrue("1.23", libraries128.length == 0);
	assertTrue("1.24", libraries123.length == 0);
	assertTrue("1.25", libraries130.length == 0);
	assertTrue("1.26", libraries300.length == 0);
	assertTrue("1.27", libraries453.length == 0);
}

public void test7() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "fragmentResolveTest", null);
	InternalFactory factory = new InternalFactory(problems);
	URL pluginURLs[] = new URL[7];
	String[] pluginPath = new String[7];
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	try {
		pluginPath[0] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin1.xml");
		pluginPath[1] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin2.xml");
		pluginPath[2] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin3.xml");
		pluginPath[3] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin4.xml");
		pluginPath[4] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin5.xml");
		pluginPath[5] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin6.xml");
		pluginPath[6] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/fragment7.xml");
		for (int i = 0; i < pluginPath.length; i++) {
			pluginURLs[i] = new URL (pluginPath[i]);
		}
	} catch (java.net.MalformedURLException e) {
		fail("1.0.1 Unexpected exception - " + e.getMessage());
	}
	
	IPluginRegistry registry = doParsing(factory, pluginURLs, true);

	// We should have 5 plugins all with id 'tests.a'
	IPluginDescriptor[] all = registry.getPluginDescriptors();
	assertTrue("1.0", all.length == 6);
	assertTrue("1.1", all[0].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.2", all[1].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.3", all[2].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.4", all[3].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.5", all[4].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.6", all[5].getUniqueIdentifier().equals("tests.a"));

	// Make sure we got all the version numbers
	IPluginDescriptor pd125 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.2.5"));
	IPluginDescriptor pd128 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.2.8"));
	IPluginDescriptor pd123 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.2.3"));
	IPluginDescriptor pd130 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.3.0"));
	IPluginDescriptor pd300 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("3.0.0"));
	IPluginDescriptor pd453 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("4.5.3"));
	assertNotNull("1.7", pd125);
	assertNotNull("1.8", pd128);
	assertNotNull("1.9", pd123);
	assertNotNull("1.10", pd130);
	assertNotNull("1.11", pd300);
	assertNotNull("1.12", pd453);

	// Check the fragment list.  There should only be one
	PluginFragmentModel[] fragments = ((PluginRegistry)registry).getFragments();
	assertTrue("1.13", fragments.length == 1);
	assertTrue("1.14", fragments[0].getId().equals("fragmentTest"));
	assertTrue("1.15", fragments[0].getPluginVersion().equals("4.5.8"));
	
	// Now make sure we didn't hook this fragment to any plugin
	PluginFragmentModel[] linkedFragments125 = ((PluginDescriptorModel)pd125).getFragments();
	PluginFragmentModel[] linkedFragments128 = ((PluginDescriptorModel)pd128).getFragments();
	PluginFragmentModel[] linkedFragments123 = ((PluginDescriptorModel)pd123).getFragments();
	PluginFragmentModel[] linkedFragments130 = ((PluginDescriptorModel)pd130).getFragments();
	PluginFragmentModel[] linkedFragments300 = ((PluginDescriptorModel)pd300).getFragments();
	PluginFragmentModel[] linkedFragments453 = ((PluginDescriptorModel)pd453).getFragments();
	assertNull("1.16", linkedFragments125);
	assertNull("1.17", linkedFragments128);
	assertNull("1.18", linkedFragments123);
	assertNull("1.19", linkedFragments130);
	assertNull("1.20", linkedFragments300);
	assertNull("1.21", linkedFragments453);
	
	// Finally, make sure the library entry in the fragment is
	// not part of any plugin
	ILibrary[] libraries125 = pd125.getRuntimeLibraries();
	ILibrary[] libraries128 = pd128.getRuntimeLibraries();
	ILibrary[] libraries123 = pd123.getRuntimeLibraries();
	ILibrary[] libraries130 = pd130.getRuntimeLibraries();
	ILibrary[] libraries300 = pd300.getRuntimeLibraries();
	ILibrary[] libraries453 = pd453.getRuntimeLibraries();
	assertTrue("1.22", libraries125.length == 0);
	assertTrue("1.23", libraries128.length == 0);
	assertTrue("1.24", libraries123.length == 0);
	assertTrue("1.25", libraries130.length == 0);
	assertTrue("1.26", libraries300.length == 0);
	assertTrue("1.27", libraries453.length == 0);
}

public void test8() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "fragmentResolveTest", null);
	InternalFactory factory = new InternalFactory(problems);
	URL pluginURLs[] = new URL[7];
	String[] pluginPath = new String[7];
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	try {
		pluginPath[0] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin1.xml");
		pluginPath[1] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin2.xml");
		pluginPath[2] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin3.xml");
		pluginPath[3] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin4.xml");
		pluginPath[4] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin5.xml");
		pluginPath[5] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin6.xml");
		pluginPath[6] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/fragment8.xml");
		for (int i = 0; i < pluginPath.length; i++) {
			pluginURLs[i] = new URL (pluginPath[i]);
		}
	} catch (java.net.MalformedURLException e) {
		fail("1.0.1 Unexpected exception - " + e.getMessage());
	}
	
	IPluginRegistry registry = doParsing(factory, pluginURLs, true);

	// We should have 5 plugins all with id 'tests.a'
	IPluginDescriptor[] all = registry.getPluginDescriptors();
	assertTrue("1.0", all.length == 6);
	assertTrue("1.1", all[0].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.2", all[1].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.3", all[2].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.4", all[3].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.5", all[4].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.6", all[5].getUniqueIdentifier().equals("tests.a"));

	// Make sure we got all the version numbers
	IPluginDescriptor pd125 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.2.5"));
	IPluginDescriptor pd128 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.2.8"));
	IPluginDescriptor pd123 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.2.3"));
	IPluginDescriptor pd130 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.3.0"));
	IPluginDescriptor pd300 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("3.0.0"));
	IPluginDescriptor pd453 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("4.5.3"));
	assertNotNull("1.7", pd125);
	assertNotNull("1.8", pd128);
	assertNotNull("1.9", pd123);
	assertNotNull("1.10", pd130);
	assertNotNull("1.11", pd300);
	assertNotNull("1.12", pd453);

	// Check the fragment list.  There should only be one
	PluginFragmentModel[] fragments = ((PluginRegistry)registry).getFragments();
	assertTrue("1.13", fragments.length == 1);
	assertTrue("1.14", fragments[0].getId().equals("fragmentTest"));
	assertTrue("1.15", fragments[0].getPluginVersion().equals("4.4.2"));
	
	// Now make sure we didn't hook this fragment to any plugin
	PluginFragmentModel[] linkedFragments125 = ((PluginDescriptorModel)pd125).getFragments();
	PluginFragmentModel[] linkedFragments128 = ((PluginDescriptorModel)pd128).getFragments();
	PluginFragmentModel[] linkedFragments123 = ((PluginDescriptorModel)pd123).getFragments();
	PluginFragmentModel[] linkedFragments130 = ((PluginDescriptorModel)pd130).getFragments();
	PluginFragmentModel[] linkedFragments300 = ((PluginDescriptorModel)pd300).getFragments();
	PluginFragmentModel[] linkedFragments453 = ((PluginDescriptorModel)pd453).getFragments();
	assertNull("1.16", linkedFragments125);
	assertNull("1.17", linkedFragments128);
	assertNull("1.18", linkedFragments123);
	assertNull("1.19", linkedFragments130);
	assertNull("1.20", linkedFragments300);
	assertNull("1.21", linkedFragments453);
	
	// Finally, make sure the library entry in the fragment is
	// not part of any plugin
	ILibrary[] libraries125 = pd125.getRuntimeLibraries();
	ILibrary[] libraries128 = pd128.getRuntimeLibraries();
	ILibrary[] libraries123 = pd123.getRuntimeLibraries();
	ILibrary[] libraries130 = pd130.getRuntimeLibraries();
	ILibrary[] libraries300 = pd300.getRuntimeLibraries();
	ILibrary[] libraries453 = pd453.getRuntimeLibraries();
	assertTrue("1.22", libraries125.length == 0);
	assertTrue("1.23", libraries128.length == 0);
	assertTrue("1.24", libraries123.length == 0);
	assertTrue("1.25", libraries130.length == 0);
	assertTrue("1.26", libraries300.length == 0);
	assertTrue("1.27", libraries453.length == 0);
}

public void test9() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "fragmentResolveTest", null);
	InternalFactory factory = new InternalFactory(problems);
	URL pluginURLs[] = new URL[7];
	String[] pluginPath = new String[7];
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	try {
		pluginPath[0] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin1.xml");
		pluginPath[1] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin2.xml");
		pluginPath[2] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin3.xml");
		pluginPath[3] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin4.xml");
		pluginPath[4] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin5.xml");
		pluginPath[5] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/plugin6.xml");
		pluginPath[6] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.6/fragment9.xml");
		for (int i = 0; i < pluginPath.length; i++) {
			pluginURLs[i] = new URL (pluginPath[i]);
		}
	} catch (java.net.MalformedURLException e) {
		fail("1.0.1 Unexpected exception - " + e.getMessage());
	}
	
	IPluginRegistry registry = doParsing(factory, pluginURLs, true);

	// We should have 5 plugins all with id 'tests.a'
	IPluginDescriptor[] all = registry.getPluginDescriptors();
	assertTrue("1.0", all.length == 6);
	assertTrue("1.1", all[0].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.2", all[1].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.3", all[2].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.4", all[3].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.5", all[4].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.6", all[5].getUniqueIdentifier().equals("tests.a"));

	// Make sure we got all the version numbers
	IPluginDescriptor pd125 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.2.5"));
	IPluginDescriptor pd128 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.2.8"));
	IPluginDescriptor pd123 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.2.3"));
	IPluginDescriptor pd130 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.3.0"));
	IPluginDescriptor pd300 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("3.0.0"));
	IPluginDescriptor pd453 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("4.5.3"));
	assertNotNull("1.7", pd125);
	assertNotNull("1.8", pd128);
	assertNotNull("1.9", pd123);
	assertNotNull("1.10", pd130);
	assertNotNull("1.11", pd300);
	assertNotNull("1.12", pd453);

	// Check the fragment list.  There should only be one
	PluginFragmentModel[] fragments = ((PluginRegistry)registry).getFragments();
	assertTrue("1.13", fragments.length == 1);
	assertTrue("1.14", fragments[0].getId().equals("fragmentTest"));
	assertTrue("1.15", fragments[0].getPluginVersion().equals("4.5.1"));
	
	// Now make sure we hooked this fragment to the right plugin (and only
	// one plugin)
	PluginFragmentModel[] linkedFragments125 = ((PluginDescriptorModel)pd125).getFragments();
	PluginFragmentModel[] linkedFragments128 = ((PluginDescriptorModel)pd128).getFragments();
	PluginFragmentModel[] linkedFragments123 = ((PluginDescriptorModel)pd123).getFragments();
	PluginFragmentModel[] linkedFragments130 = ((PluginDescriptorModel)pd130).getFragments();
	PluginFragmentModel[] linkedFragments300 = ((PluginDescriptorModel)pd300).getFragments();
	PluginFragmentModel[] linkedFragments453 = ((PluginDescriptorModel)pd453).getFragments();
	assertNull("1.16", linkedFragments125);
	assertNull("1.17", linkedFragments128);
	assertNull("1.18", linkedFragments123);
	assertNull("1.19", linkedFragments130);
	assertNull("1.20", linkedFragments300);
	assertNotNull("1.21", linkedFragments453);
	assertTrue("1.22", linkedFragments453.length == 1);
	assertTrue("1.23", linkedFragments453[0].getId().equals("fragmentTest"));
	
	// Finally, make sure the library entry in the fragment is
	// now part of the proper plugin
	ILibrary[] libraries125 = pd125.getRuntimeLibraries();
	ILibrary[] libraries128 = pd128.getRuntimeLibraries();
	ILibrary[] libraries123 = pd123.getRuntimeLibraries();
	ILibrary[] libraries130 = pd130.getRuntimeLibraries();
	ILibrary[] libraries300 = pd300.getRuntimeLibraries();
	ILibrary[] libraries453 = pd453.getRuntimeLibraries();
	assertTrue("1.24", libraries125.length == 0);
	assertTrue("1.25", libraries128.length == 0);
	assertTrue("1.26", libraries123.length == 0);
	assertTrue("1.27", libraries130.length == 0);
	assertTrue("1.28", libraries300.length == 0);
	assertTrue("1.29", libraries453.length == 1);
	assertTrue("1.30", ((Library)libraries453[0]).getName().equals("lib1.jar"));
}

public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new FragmentResolveTest_6("test1"));
	suite.addTest(new FragmentResolveTest_6("test2"));
	suite.addTest(new FragmentResolveTest_6("test3"));
	suite.addTest(new FragmentResolveTest_6("test4"));
	suite.addTest(new FragmentResolveTest_6("test5"));
	suite.addTest(new FragmentResolveTest_6("test6"));
	suite.addTest(new FragmentResolveTest_6("test7"));
	suite.addTest(new FragmentResolveTest_6("test8"));
	suite.addTest(new FragmentResolveTest_6("test9"));
	return suite;
}
}


