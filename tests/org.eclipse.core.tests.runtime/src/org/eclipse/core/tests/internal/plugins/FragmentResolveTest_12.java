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

public class FragmentResolveTest_12 extends EclipseWorkspaceTest {
public FragmentResolveTest_12() {
	super(null);
}
public FragmentResolveTest_12(String name) {
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
	URL pluginURLs[] = new URL[9];
	String[] pluginPath = new String[9];
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	pluginPath[0] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.12/plugin1.xml");
	
	try {
		pluginPath[1] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.12/fragment1.xml");
		pluginPath[2] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.12/fragment2.xml");
		pluginPath[3] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.12/fragment3.xml");
		pluginPath[4] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.12/fragment4.xml");
		pluginPath[5] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.12/fragment5.xml");
		pluginPath[6] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.12/fragment6.xml");
		pluginPath[7] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.12/fragment7.xml");
		pluginPath[8] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.12/fragment8.xml");
		
		for (int i = 0; i < pluginPath.length; i++) {
			pluginURLs[i] = new URL (pluginPath[i]);
		}
	} catch (java.net.MalformedURLException e) {
		fail("0.0.0 Unexpected exception - " + e.getMessage());
	}

	IPluginRegistry registry = doParsing(factory, pluginURLs, true);

	// We should have 1 plugin
	PluginDescriptorModel[] all = ((PluginRegistryModel)registry).getPlugins();
	assertTrue("1.0", all.length == 1);
	assertTrue("1.1", all[0].getId().equals("pluginOne"));
	PluginDescriptorModel plugin = all[0];

	// Check the fragment list.  There should be eight
	PluginFragmentModel[] fragments = ((PluginRegistry)registry).getFragments();
	assertTrue("1.2", fragments.length == 8);
	assertTrue("1.3", fragments[0].getId().equals("fragmentOne"));
	assertTrue("1.4", fragments[1].getId().equals("fragmentOne"));
	assertTrue("1.5", fragments[2].getId().equals("fragmentOne"));
	assertTrue("1.6", fragments[3].getId().equals("fragmentOne"));
	assertTrue("1.7", fragments[4].getId().equals("fragmentOne"));
	assertTrue("1.8", fragments[5].getId().equals("fragmentOne"));
	assertTrue("1.9", fragments[6].getId().equals("fragmentOne"));
	assertTrue("1.10", fragments[7].getId().equals("fragmentOne"));
	
	// Only the latest one should hang off the plugin
	PluginFragmentModel[] pluginFragments = plugin.getFragments();
	assertTrue("1.11", pluginFragments.length == 1);
	assertTrue("1.12", pluginFragments[0].getId().equals("fragmentOne"));
	assertTrue("1.13", pluginFragments[0].getVersion().equals("1.7.2"));
	
	// But we should only have used fragment5.xml
	LibraryModel[] libraries = plugin.getRuntime();
	assertTrue("1.14", libraries.length == 5);
}

public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new FragmentResolveTest_12("fullTest"));
	return suite;
}
}


