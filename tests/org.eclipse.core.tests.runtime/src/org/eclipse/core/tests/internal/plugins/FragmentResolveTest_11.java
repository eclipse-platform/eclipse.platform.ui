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

public class FragmentResolveTest_11 extends EclipseWorkspaceTest {
public FragmentResolveTest_11() {
	super(null);
}
public FragmentResolveTest_11(String name) {
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
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "fragmentResolveTest", null);
	InternalFactory factory = new InternalFactory(problems);
	URL pluginURLs[] = new URL[3];
	String[] pluginPath = new String[3];
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	try {
		pluginPath[0] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.11/plugin1.xml");
		pluginURLs[0] = new URL (pluginPath[0]);
	} catch (java.net.MalformedURLException e) {
		fail("0.0.0 Unexpected exception - " + e.getMessage());
	}
	
	try {
		pluginPath[1] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.11/fragment1.xml");
		pluginPath[2] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.11/fragment2.xml");
		pluginURLs[1] = new URL (pluginPath[1]);
		pluginURLs[2] = new URL (pluginPath[2]);
	} catch (java.net.MalformedURLException e) {
		fail("0.0.1 Unexpected exception - " + e.getMessage());
	}

	IPluginRegistry registry = doParsing(factory, pluginURLs, true);

	// We should have 1 plugin
	PluginDescriptorModel[] all = ((PluginRegistryModel)registry).getPlugins();
	assertTrue("1.0", all.length == 1);
	assertTrue("1.1", all[0].getId().equals("pluginOne"));
	PluginDescriptorModel plugin = all[0];

	// Check the fragment list.  There should only be one
	PluginFragmentModel[] fragments = ((PluginRegistry)registry).getFragments();
	assertTrue("1.2", fragments.length == 2);
	assertTrue("1.3", fragments[0].getId().equals("fragmentOne"));
	assertTrue("1.4", fragments[1].getId().equals("fragmentOne"));
	
	PluginFragmentModel[] pluginFragments = plugin.getFragments();
	assertTrue("1.5", pluginFragments.length == 1);
	assertTrue("1.6", pluginFragments[0].getId().equals("fragmentOne"));
	assertTrue("1.7", pluginFragments[0].getVersion().equals("1.0.1"));
	
	LibraryModel[] libraries = plugin.getRuntime();
	assertTrue("1.8", libraries.length == 2);
}

public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new FragmentResolveTest_11("fullTest"));
	return suite;
}
}


