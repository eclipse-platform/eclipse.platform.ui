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

public class FragmentResolveTest_10 extends EclipseWorkspaceTest {
public FragmentResolveTest_10() {
	super(null);
}
public FragmentResolveTest_10(String name) {
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

public void fullTest() {
	String[] fragmentIds = {"5.9.8", "1.2.3", "1.2.4", "2.0.0", "3.0.4",
		"4.6.2", "4.5.8", "4.4.2", "4.5.1"}; 
	boolean[] matchesPlugin = {false, true, true, true, true, 
		false, false, true, true};

	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "fragmentResolveTest", null);
	InternalFactory factory = new InternalFactory(problems);
	URL pluginURLs[] = new URL[7];
	String[] pluginPath = new String[7];
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	try {
		pluginPath[0] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.10/plugin1.xml");
		pluginPath[1] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.10/plugin2.xml");
		pluginPath[2] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.10/plugin3.xml");
		pluginPath[3] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.10/plugin4.xml");
		pluginPath[4] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.10/plugin5.xml");
		pluginPath[5] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.10/plugin6.xml");
		for (int i = 0; i < pluginPath.length - 1; i++) {
			pluginURLs[i] = new URL (pluginPath[i]);
		}
	} catch (java.net.MalformedURLException e) {
		fail("0.0.0 Unexpected exception - " + e.getMessage());
	}
	
	for (int i = 0; i < fragmentIds.length; i++) {
		try {
			pluginPath[6] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.10/fragment" + (i+1) + ".xml");
			pluginURLs[6] = new URL (pluginPath[6]);
		} catch (java.net.MalformedURLException e) {
			fail("0.0.1 Unexpected exception - " + e.getMessage());
		}
	
		IPluginRegistry registry = doParsing(factory, pluginURLs, true);
	
		// We should have 6 plugins all with id 'tests.a'
		IPluginDescriptor[] all = registry.getPluginDescriptors();
		assertTrue(i + ".0", all.length == 6);
		assertTrue(i + ".1", all[0].getUniqueIdentifier().equals("tests.a"));
		assertTrue(i + ".2", all[1].getUniqueIdentifier().equals("tests.a"));
		assertTrue(i + ".3", all[2].getUniqueIdentifier().equals("tests.a"));
		assertTrue(i + ".4", all[3].getUniqueIdentifier().equals("tests.a"));
		assertTrue(i + ".5", all[4].getUniqueIdentifier().equals("tests.a"));
		assertTrue(i + ".6", all[5].getUniqueIdentifier().equals("tests.a"));
	
		// Make sure we got all the version numbers
		IPluginDescriptor pd125 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.2.5"));
		IPluginDescriptor pd128 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.2.8"));
		IPluginDescriptor pd123 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.2.3"));
		IPluginDescriptor pd130 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.3.0"));
		IPluginDescriptor pd300 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("3.0.0"));
		IPluginDescriptor pd453 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("4.5.3"));
		assertNotNull(i + ".7", pd125);
		assertNotNull(i + ".8", pd128);
		assertNotNull(i + ".9", pd123);
		assertNotNull(i + ".10", pd130);
		assertNotNull(i + ".11", pd300);
		assertNotNull(i + ".12", pd453);
	
		// Check the fragment list.  There should only be one
		PluginFragmentModel[] fragments = ((PluginRegistry)registry).getFragments();
		assertTrue(i + ".13", fragments.length == 1);
		assertTrue(i + ".14", fragments[0].getId().equals("fragmentTest"));
		assertTrue(i + ".15", fragments[0].getPluginVersion().equals(fragmentIds[i]));
		
		if (matchesPlugin[i]) {		
			// Now make sure we hooked this fragment to the right plugin (and only
			// one plugin)
			PluginFragmentModel[] linkedFragments125 = ((PluginDescriptorModel)pd125).getFragments();
			PluginFragmentModel[] linkedFragments128 = ((PluginDescriptorModel)pd128).getFragments();
			PluginFragmentModel[] linkedFragments123 = ((PluginDescriptorModel)pd123).getFragments();
			PluginFragmentModel[] linkedFragments130 = ((PluginDescriptorModel)pd130).getFragments();
			PluginFragmentModel[] linkedFragments300 = ((PluginDescriptorModel)pd300).getFragments();
			PluginFragmentModel[] linkedFragments453 = ((PluginDescriptorModel)pd453).getFragments();
			assertNull(i + ".16", linkedFragments125);
			assertNull(i + ".17", linkedFragments128);
			assertNull(i + ".18", linkedFragments123);
			assertNull(i + ".19", linkedFragments130);
			assertNull(i + ".20", linkedFragments300);
			assertNotNull(i + ".21", linkedFragments453);
			assertTrue(i + ".22", linkedFragments453.length == 1);
			assertTrue(i + ".23", linkedFragments453[0].getId().equals("fragmentTest"));
			
			// Finally, make sure the library entry in the fragment is
			// now part of the proper plugin
			ILibrary[] libraries125 = pd125.getRuntimeLibraries();
			ILibrary[] libraries128 = pd128.getRuntimeLibraries();
			ILibrary[] libraries123 = pd123.getRuntimeLibraries();
			ILibrary[] libraries130 = pd130.getRuntimeLibraries();
			ILibrary[] libraries300 = pd300.getRuntimeLibraries();
			ILibrary[] libraries453 = pd453.getRuntimeLibraries();
			assertTrue(i + ".24", libraries125.length == 0);
			assertTrue(i + ".25", libraries128.length == 0);
			assertTrue(i + ".26", libraries123.length == 0);
			assertTrue(i + ".27", libraries130.length == 0);
			assertTrue(i + ".28", libraries300.length == 0);
			assertTrue(i + ".29", libraries453.length == 1);
			assertTrue(i + ".30", ((Library)libraries453[0]).getName().equals("lib1.jar"));
		} else {
			// Now make sure we didn't hook this fragment to any plugin
			PluginFragmentModel[] linkedFragments125 = ((PluginDescriptorModel)pd125).getFragments();
			PluginFragmentModel[] linkedFragments128 = ((PluginDescriptorModel)pd128).getFragments();
			PluginFragmentModel[] linkedFragments123 = ((PluginDescriptorModel)pd123).getFragments();
			PluginFragmentModel[] linkedFragments130 = ((PluginDescriptorModel)pd130).getFragments();
			PluginFragmentModel[] linkedFragments300 = ((PluginDescriptorModel)pd300).getFragments();
			PluginFragmentModel[] linkedFragments453 = ((PluginDescriptorModel)pd453).getFragments();
			assertNull(i + ".16", linkedFragments125);
			assertNull(i + ".17", linkedFragments128);
			assertNull(i + ".18", linkedFragments123);
			assertNull(i + ".19", linkedFragments130);
			assertNull(i + ".20", linkedFragments300);
			assertNull(i + ".21", linkedFragments453);
			
			// Finally, make sure the library entry in the fragment is
			// not part of any plugin
			ILibrary[] libraries125 = pd125.getRuntimeLibraries();
			ILibrary[] libraries128 = pd128.getRuntimeLibraries();
			ILibrary[] libraries123 = pd123.getRuntimeLibraries();
			ILibrary[] libraries130 = pd130.getRuntimeLibraries();
			ILibrary[] libraries300 = pd300.getRuntimeLibraries();
			ILibrary[] libraries453 = pd453.getRuntimeLibraries();
			assertTrue(i + ".22", libraries125.length == 0);
			assertTrue(i + ".23", libraries128.length == 0);
			assertTrue(i + ".24", libraries123.length == 0);
			assertTrue(i + ".25", libraries130.length == 0);
			assertTrue(i + ".26", libraries300.length == 0);
			assertTrue(i + ".27", libraries453.length == 0);
		}
	}
}

public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new FragmentResolveTest_10("fullTest"));
	return suite;
}
}


