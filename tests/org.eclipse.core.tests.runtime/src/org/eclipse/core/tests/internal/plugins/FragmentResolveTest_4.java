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

public class FragmentResolveTest_4 extends EclipseWorkspaceTest {
public FragmentResolveTest_4() {
	super(null);
}
public FragmentResolveTest_4(String name) {
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
	String[] fragmentIds = {"1.2.1", "1.2.2", "1.2.0", "1.1.0", 
		"1.1.1", "1.5.0", "1.5.1", "3.0.0", "3.2.1"}; 

	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "fragmentResolveTest", null);
	InternalFactory factory = new InternalFactory(problems);
	URL pluginURLs[] = new URL[6];
	String[] pluginPath = new String[6];
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	try {
		pluginPath[0] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.4/plugin1.xml");
		pluginPath[1] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.4/plugin2.xml");
		pluginPath[2] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.4/plugin3.xml");
		pluginPath[3] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.4/plugin4.xml");
		pluginPath[4] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.4/plugin5.xml");
		for (int i = 0; i < pluginPath.length - 1; i++) {
			pluginURLs[i] = new URL (pluginPath[i]);
		}
	} catch (java.net.MalformedURLException e) {
		fail("0.0.0 Unexpected exception - " + e.getMessage());
	}
	
	for (int i = 0; i < fragmentIds.length; i++) {
		pluginPath[5] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.4/fragment" + (i + 1) + ".xml");
		try {
			pluginURLs[5] = new URL (pluginPath[5]);
		} catch (java.net.MalformedURLException e) {
			fail("0.0.1 Unexpected exception - " + e.getMessage());
		}
		IPluginRegistry registry = doParsing(factory, pluginURLs, true);
	
		// We should have 5 plugins all with id 'tests.a'
		IPluginDescriptor[] all = registry.getPluginDescriptors();
		assertTrue(i + ".0", all.length == 5);
		assertTrue(i + ".1", all[0].getUniqueIdentifier().equals("tests.a"));
		assertTrue(i + ".2", all[1].getUniqueIdentifier().equals("tests.a"));
		assertTrue(i + ".3", all[2].getUniqueIdentifier().equals("tests.a"));
		assertTrue(i + ".4", all[3].getUniqueIdentifier().equals("tests.a"));
		assertTrue(i + ".5", all[4].getUniqueIdentifier().equals("tests.a"));
	
		// Make sure we got all the version numbers
		IPluginDescriptor pd121 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.2.1"));
		IPluginDescriptor pd140 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.4.0"));
		IPluginDescriptor pd117 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.1.7"));
		IPluginDescriptor pd302 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("3.0.2"));
		IPluginDescriptor pd021 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("0.2.1"));
		assertNotNull(i + ".6", pd121);
		assertNotNull(i + ".7", pd140);
		assertNotNull(i + ".8", pd117);
		assertNotNull(i + ".9", pd302);
		assertNotNull(i + ".10", pd021);
	
		// Check the fragment list.  There should only be one
		PluginFragmentModel[] fragments = ((PluginRegistry)registry).getFragments();
		assertTrue(i + ".11", fragments.length == 1);
		assertTrue(i + ".12", fragments[0].getId().equals("fragmentTest"));
		assertTrue(i + ".13", fragments[0].getPluginVersion().equals(fragmentIds[i]));
		
		if (fragmentIds[i].equals("1.2.1")) {
			// Now make sure we hooked this fragment to the right plugin (and only
			// one plugin)
			PluginFragmentModel[] linkedFragments121 = ((PluginDescriptorModel)pd121).getFragments();
			PluginFragmentModel[] linkedFragments140 = ((PluginDescriptorModel)pd140).getFragments();
			PluginFragmentModel[] linkedFragments117 = ((PluginDescriptorModel)pd117).getFragments();
			PluginFragmentModel[] linkedFragments302 = ((PluginDescriptorModel)pd302).getFragments();
			PluginFragmentModel[] linkedFragments021 = ((PluginDescriptorModel)pd021).getFragments();
			assertNotNull(i + ".14", linkedFragments121);
			assertNull(i + ".15", linkedFragments140);
			assertNull(i + ".16", linkedFragments117);
			assertNull(i + ".17", linkedFragments302);
			assertNull(i + ".18", linkedFragments021);
			assertTrue(i + ".19", linkedFragments121.length == 1);
			assertTrue(i + ".20", linkedFragments121[0].getId().equals("fragmentTest"));
			
			// Finally, make sure the library entry in the fragment is
			// now part of the proper plugin
			ILibrary[] libraries121 = pd121.getRuntimeLibraries();
			ILibrary[] libraries140 = pd140.getRuntimeLibraries();
			ILibrary[] libraries117 = pd117.getRuntimeLibraries();
			ILibrary[] libraries302 = pd302.getRuntimeLibraries();
			ILibrary[] libraries021 = pd021.getRuntimeLibraries();
			assertTrue(i + ".21", libraries121.length == 1);
			assertTrue(i + ".22", libraries140.length == 0);
			assertTrue(i + ".23", libraries117.length == 0);
			assertTrue(i + ".24", libraries302.length == 0);
			assertTrue(i + ".25", libraries021.length == 0);
			assertTrue(i + ".26", ((Library)libraries121[0]).getName().equals("lib1.jar"));
		} else {
			// Now make sure we didn't hook this fragment to any plugin
			PluginFragmentModel[] linkedFragments121 = ((PluginDescriptorModel)pd121).getFragments();
			PluginFragmentModel[] linkedFragments140 = ((PluginDescriptorModel)pd140).getFragments();
			PluginFragmentModel[] linkedFragments117 = ((PluginDescriptorModel)pd117).getFragments();
			PluginFragmentModel[] linkedFragments302 = ((PluginDescriptorModel)pd302).getFragments();
			PluginFragmentModel[] linkedFragments021 = ((PluginDescriptorModel)pd021).getFragments();
			assertNull(i + ".14", linkedFragments121);
			assertNull(i + ".15", linkedFragments140);
			assertNull(i + ".16", linkedFragments117);
			assertNull(i + ".17", linkedFragments302);
			assertNull(i + ".18", linkedFragments021);
			
			// Finally, make sure the library entry in the fragment is
			// not part of the proper plugin
			ILibrary[] libraries121 = pd121.getRuntimeLibraries();
			ILibrary[] libraries140 = pd140.getRuntimeLibraries();
			ILibrary[] libraries117 = pd117.getRuntimeLibraries();
			ILibrary[] libraries302 = pd302.getRuntimeLibraries();
			ILibrary[] libraries021 = pd021.getRuntimeLibraries();
			assertTrue(i + ".19", libraries121.length == 0);
			assertTrue(i + ".20", libraries140.length == 0);
			assertTrue(i + ".21", libraries117.length == 0);
			assertTrue(i + ".22", libraries302.length == 0);
			assertTrue(i + ".23", libraries021.length == 0);
		}
	}
}

public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new FragmentResolveTest_4("fullTest"));
	return suite;
}
}


