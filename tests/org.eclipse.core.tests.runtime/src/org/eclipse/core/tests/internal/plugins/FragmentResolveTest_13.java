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

public class FragmentResolveTest_13 extends EclipseWorkspaceTest {
public FragmentResolveTest_13() {
	super(null);
}
public FragmentResolveTest_13(String name) {
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
	URL pluginURLs[] = new URL[16];
	String[] pluginPath = new String[16];
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	pluginPath[0] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.13/plugin1.xml");
	
	try {
		pluginPath[1] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.13/fragment1.xml");
		pluginPath[2] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.13/fragment2.xml");
		pluginPath[3] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.13/fragment3.xml");
		pluginPath[4] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.13/fragment4.xml");
		pluginPath[5] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.13/fragment5.xml");
		pluginPath[6] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.13/fragment6.xml");
		pluginPath[7] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.13/fragment7.xml");
		pluginPath[8] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.13/fragment8.xml");
		pluginPath[9] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.13/fragment21.xml");
		pluginPath[10] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.13/fragment22.xml");
		pluginPath[11] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.13/fragment23.xml");
		pluginPath[12] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.13/fragment24.xml");
		pluginPath[13] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.13/fragment25.xml");
		pluginPath[14] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.13/fragment26.xml");
		pluginPath[15] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.13/fragment27.xml");
		
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

	// Check the fragment list.  There should be 15
	PluginFragmentModel[] fragments = ((PluginRegistry)registry).getFragments();
	assertTrue("1.2", fragments.length == 15);
	
	// Only 2 fragments should hang off the plugin
	PluginFragmentModel[] pluginFragments = plugin.getFragments();
	assertTrue("1.3", pluginFragments.length == 2);
	PluginFragmentModel fragment1 = null;
	PluginFragmentModel fragment2 = null;
	for (int i = 0; i < pluginFragments.length; i++) {
		if (pluginFragments[i].getId().equals("fragmentOne"))
			fragment1 = pluginFragments[i];
		else if (pluginFragments[i].getId().equals("fragmentTwo"))
			fragment2 = pluginFragments[i];
	}
			
	// Make sure we picked up both fragments
	assertNotNull("1.4", fragment1);
	assertNotNull("1.5", fragment2);
	
	// Make sure we picked up the right version for each fragment
	assertTrue("1.6", fragment1.getVersion().equals("4.5.0"));
	assertTrue("1.7", fragment2.getVersion().equals("3.0.0"));
	
	// But we should only have used fragment5.xml
	LibraryModel[] libraries = plugin.getRuntime();
	assertTrue("1.8", libraries.length == 4);
	
	// Make sure we got the right libraries
	boolean lib1 = false;
	boolean lib2 = false;
	boolean lib3 = false;
	boolean lib21 = false;
	boolean somethingElse = false;
	
	for (int i = 0; i < libraries.length; i++) {
		String libName = libraries[i].getName();
		if (libName.equals("lib1.jar"))
			lib1 = true;
		else if (libName.equals("lib2.jar"))
			lib2 = true;
		else if (libName.equals("lib3.jar"))
			lib3 = true;
		else if (libName.equals("lib21.jar"))
			lib21 = true;
		else
			somethingElse = true;
	}
	assertTrue("1.9", lib1 && lib2 && lib3 && lib21 && !somethingElse);
}

public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new FragmentResolveTest_13("fullTest"));
	return suite;
}
}


