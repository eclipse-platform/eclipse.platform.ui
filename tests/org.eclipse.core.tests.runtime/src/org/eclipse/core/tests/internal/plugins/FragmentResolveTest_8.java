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

public class FragmentResolveTest_8 extends EclipseWorkspaceTest {
public FragmentResolveTest_8() {
	super(null);
}
public FragmentResolveTest_8(String name) {
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
	URL pluginURLs[] = new URL[5];
	try {
		PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
		String[] pluginPath = new String[5];
		pluginPath[0] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.8/plugin1.xml");
		pluginPath[1] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.8/plugin2.xml");
		pluginPath[2] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.8/plugin3.xml");
		pluginPath[3] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.8/plugin4.xml");
		pluginPath[4] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.8/fragment1.xml");
		for (int i = 0; i < pluginPath.length; i++) {
			pluginURLs[i] = new URL (pluginPath[i]);
		}
	} catch (java.net.MalformedURLException e) {
		fail("1.0.1 Unexpected exception - " + e.getMessage());
	}
	IPluginRegistry registry = doParsing(factory, pluginURLs, true);

	// We should have 4 plugins all with id 'tests.a'
	IPluginDescriptor[] all = registry.getPluginDescriptors();
	assertTrue("1.0", all.length == 4);
	assertTrue("1.1", all[0].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.2", all[1].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.3", all[2].getUniqueIdentifier().equals("tests.a"));
	assertTrue("1.4", all[3].getUniqueIdentifier().equals("tests.a"));

	// Make sure we got all the version numbers
	IPluginDescriptor pd124 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.2.4"));
	IPluginDescriptor pd130 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.3.0"));
	IPluginDescriptor pd240 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("2.4.0"));
	IPluginDescriptor pd110 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.1.0"));
	assertNotNull("1.5", pd124);
	assertNotNull("1.6", pd130);
	assertNotNull("1.7", pd240);
	assertNotNull("1.8", pd110);

	// Check the fragment list.  There should only be one
	PluginFragmentModel[] fragments = ((PluginRegistry)registry).getFragments();
	assertTrue("1.9", fragments.length == 1);
	assertTrue("1.10", fragments[0].getId().equals("fragmentTest"));
	assertTrue("1.11", fragments[0].getPluginVersion().equals("1.2.3"));
	
	// Now make sure we hooked this fragment to the right plugin (and only
	// one plugin)
	PluginFragmentModel[] linkedFragments124 = ((PluginDescriptorModel)pd124).getFragments();
	PluginFragmentModel[] linkedFragments130 = ((PluginDescriptorModel)pd130).getFragments();
	PluginFragmentModel[] linkedFragments240 = ((PluginDescriptorModel)pd240).getFragments();
	PluginFragmentModel[] linkedFragments110 = ((PluginDescriptorModel)pd110).getFragments();
	assertNull("1.12", linkedFragments124);
	assertNotNull("1.13", linkedFragments130);
	assertNull("1.14", linkedFragments240);
	assertNull("1.15", linkedFragments110);
	assertTrue("1.16", linkedFragments130.length == 1);
	assertTrue("1.17", linkedFragments130[0].getId().equals("fragmentTest"));
	
	// Finally, make sure the library entry in the fragment is
	// now part of the proper plugin
	ILibrary[] libraries124 = pd124.getRuntimeLibraries();
	ILibrary[] libraries130 = pd130.getRuntimeLibraries();
	ILibrary[] libraries240 = pd240.getRuntimeLibraries();
	ILibrary[] libraries110 = pd110.getRuntimeLibraries();
	assertTrue("1.18", libraries124.length == 0);
	assertTrue("1.19", libraries130.length == 1);
	assertTrue("1.20", libraries240.length == 0);
	assertTrue("1.21", libraries110.length == 0);
	assertTrue("1.22", ((Library)libraries130[0]).getName().equals("lib1.jar"));
}

public void test2() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "fragmentResolveTest", null);
	InternalFactory factory = new InternalFactory(problems);
	URL pluginURLs[] = new URL[5];
	try {
		PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
		String[] pluginPath = new String[5];
		pluginPath[0] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.8/plugin1.xml");
		pluginPath[1] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.8/plugin2.xml");
		pluginPath[2] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.8/plugin3.xml");
		pluginPath[3] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.8/plugin4.xml");
		pluginPath[4] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.8/fragment2.xml");
		for (int i = 0; i < pluginPath.length; i++) {
			pluginURLs[i] = new URL (pluginPath[i]);
		}
	} catch (java.net.MalformedURLException e) {
		fail("2.0.1 Unexpected exception - " + e.getMessage());
	}
	IPluginRegistry registry = doParsing(factory, pluginURLs, true);

	// We should have 4 plugins all with id 'tests.a'
	IPluginDescriptor[] all = registry.getPluginDescriptors();
	assertTrue("2.0", all.length == 4);
	assertTrue("2.1", all[0].getUniqueIdentifier().equals("tests.a"));
	assertTrue("2.2", all[1].getUniqueIdentifier().equals("tests.a"));
	assertTrue("2.3", all[2].getUniqueIdentifier().equals("tests.a"));
	assertTrue("2.4", all[3].getUniqueIdentifier().equals("tests.a"));

	// Make sure we got all the version numbers
	IPluginDescriptor pd124 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.2.4"));
	IPluginDescriptor pd130 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.3.0"));
	IPluginDescriptor pd240 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("2.4.0"));
	IPluginDescriptor pd110 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.1.0"));
	assertNotNull("2.5", pd124);
	assertNotNull("2.6", pd130);
	assertNotNull("2.7", pd240);
	assertNotNull("2.8", pd110);

	// Check the fragment list.  There should only be one
	PluginFragmentModel[] fragments = ((PluginRegistry)registry).getFragments();
	assertTrue("2.9", fragments.length == 1);
	assertTrue("2.10", fragments[0].getId().equals("fragmentTest"));
	assertTrue("2.11", fragments[0].getPluginVersion().equals("1.0.0"));
	
	// Now make sure we hooked this fragment to the right plugin (and only
	// one plugin)
	PluginFragmentModel[] linkedFragments124 = ((PluginDescriptorModel)pd124).getFragments();
	PluginFragmentModel[] linkedFragments130 = ((PluginDescriptorModel)pd130).getFragments();
	PluginFragmentModel[] linkedFragments240 = ((PluginDescriptorModel)pd240).getFragments();
	PluginFragmentModel[] linkedFragments110 = ((PluginDescriptorModel)pd110).getFragments();
	assertNull("2.12", linkedFragments124);
	assertNotNull("2.13", linkedFragments130);
	assertNull("2.14", linkedFragments240);
	assertNull("2.15", linkedFragments110);
	assertTrue("2.16", linkedFragments130.length == 1);
	assertTrue("2.17", linkedFragments130[0].getId().equals("fragmentTest"));
	
	// Finally, make sure the library entry in the fragment is
	// now part of the proper plugin
	ILibrary[] libraries124 = pd124.getRuntimeLibraries();
	ILibrary[] libraries130 = pd130.getRuntimeLibraries();
	ILibrary[] libraries240 = pd240.getRuntimeLibraries();
	ILibrary[] libraries110 = pd110.getRuntimeLibraries();
	assertTrue("2.18", libraries124.length == 0);
	assertTrue("2.19", libraries130.length == 1);
	assertTrue("2.20", libraries240.length == 0);
	assertTrue("2.21", libraries110.length == 0);
	assertTrue("2.22", ((Library)libraries130[0]).getName().equals("lib1.jar"));
}

public void test3() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "fragmentResolveTest", null);
	InternalFactory factory = new InternalFactory(problems);
	URL pluginURLs[] = new URL[5];
	try {
		PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
		String[] pluginPath = new String[5];
		pluginPath[0] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.8/plugin1.xml");
		pluginPath[1] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.8/plugin2.xml");
		pluginPath[2] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.8/plugin3.xml");
		pluginPath[3] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.8/plugin4.xml");
		pluginPath[4] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.8/fragment3.xml");
		for (int i = 0; i < pluginPath.length; i++) {
			pluginURLs[i] = new URL (pluginPath[i]);
		}
	} catch (java.net.MalformedURLException e) {
		fail("3.0.1 Unexpected exception - " + e.getMessage());
	}
	IPluginRegistry registry = doParsing(factory, pluginURLs, true);

	// We should have 4 plugins all with id 'tests.a'
	IPluginDescriptor[] all = registry.getPluginDescriptors();
	assertTrue("3.0", all.length == 4);
	assertTrue("3.1", all[0].getUniqueIdentifier().equals("tests.a"));
	assertTrue("3.2", all[1].getUniqueIdentifier().equals("tests.a"));
	assertTrue("3.3", all[2].getUniqueIdentifier().equals("tests.a"));
	assertTrue("3.4", all[3].getUniqueIdentifier().equals("tests.a"));

	// Make sure we got all the version numbers
	IPluginDescriptor pd124 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.2.4"));
	IPluginDescriptor pd130 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.3.0"));
	IPluginDescriptor pd240 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("2.4.0"));
	IPluginDescriptor pd110 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.1.0"));
	assertNotNull("3.5", pd124);
	assertNotNull("3.6", pd130);
	assertNotNull("3.7", pd240);
	assertNotNull("3.8", pd110);

	// Check the fragment list.  There should only be one
	PluginFragmentModel[] fragments = ((PluginRegistry)registry).getFragments();
	assertTrue("3.9", fragments.length == 1);
	assertTrue("3.10", fragments[0].getId().equals("fragmentTest"));
	assertTrue("3.11", fragments[0].getPluginVersion().equals("2.1.1"));
	
	// Now make sure we hooked this fragment to the right plugin (and only
	// one plugin)
	PluginFragmentModel[] linkedFragments124 = ((PluginDescriptorModel)pd124).getFragments();
	PluginFragmentModel[] linkedFragments130 = ((PluginDescriptorModel)pd130).getFragments();
	PluginFragmentModel[] linkedFragments240 = ((PluginDescriptorModel)pd240).getFragments();
	PluginFragmentModel[] linkedFragments110 = ((PluginDescriptorModel)pd110).getFragments();
	assertNull("3.12", linkedFragments124);
	assertNull("3.13", linkedFragments130);
	assertNotNull("3.14", linkedFragments240);
	assertNull("3.15", linkedFragments110);
	assertTrue("3.16", linkedFragments240.length == 1);
	assertTrue("3.17", linkedFragments240[0].getId().equals("fragmentTest"));
	
	// Finally, make sure the library entry in the fragment is
	// now part of the proper plugin
	ILibrary[] libraries124 = pd124.getRuntimeLibraries();
	ILibrary[] libraries130 = pd130.getRuntimeLibraries();
	ILibrary[] libraries240 = pd240.getRuntimeLibraries();
	ILibrary[] libraries110 = pd110.getRuntimeLibraries();
	assertTrue("3.18", libraries124.length == 0);
	assertTrue("3.19", libraries130.length == 0);
	assertTrue("3.20", libraries240.length == 1);
	assertTrue("3.21", libraries110.length == 0);
	assertTrue("3.22", ((Library)libraries240[0]).getName().equals("lib1.jar"));
}

public void test4() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "fragmentResolveTest", null);
	InternalFactory factory = new InternalFactory(problems);
	URL pluginURLs[] = new URL[5];
	try {
		PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
		String[] pluginPath = new String[5];
		pluginPath[0] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.8/plugin1.xml");
		pluginPath[1] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.8/plugin2.xml");
		pluginPath[2] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.8/plugin3.xml");
		pluginPath[3] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.8/plugin4.xml");
		pluginPath[4] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.8/fragment4.xml");
		for (int i = 0; i < pluginPath.length; i++) {
			pluginURLs[i] = new URL (pluginPath[i]);
		}
	} catch (java.net.MalformedURLException e) {
		fail("4.0.1 Unexpected exception - " + e.getMessage());
	}
	IPluginRegistry registry = doParsing(factory, pluginURLs, true);

	// We should have 4 plugins all with id 'tests.a'
	IPluginDescriptor[] all = registry.getPluginDescriptors();
	assertTrue("4.0", all.length == 4);
	assertTrue("4.1", all[0].getUniqueIdentifier().equals("tests.a"));
	assertTrue("4.2", all[1].getUniqueIdentifier().equals("tests.a"));
	assertTrue("4.3", all[2].getUniqueIdentifier().equals("tests.a"));
	assertTrue("4.4", all[3].getUniqueIdentifier().equals("tests.a"));

	// Make sure we got all the version numbers
	IPluginDescriptor pd124 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.2.4"));
	IPluginDescriptor pd130 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.3.0"));
	IPluginDescriptor pd240 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("2.4.0"));
	IPluginDescriptor pd110 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.1.0"));
	assertNotNull("4.5", pd124);
	assertNotNull("4.6", pd130);
	assertNotNull("4.7", pd240);
	assertNotNull("4.8", pd110);

	// Check the fragment list.  There should only be one
	PluginFragmentModel[] fragments = ((PluginRegistry)registry).getFragments();
	assertTrue("4.9", fragments.length == 1);
	assertTrue("4.10", fragments[0].getId().equals("fragmentTest"));
	assertTrue("4.11", fragments[0].getPluginVersion().equals("2.5.0"));
	
	// Now make sure we didn't hood this fragment anywhere
	PluginFragmentModel[] linkedFragments124 = ((PluginDescriptorModel)pd124).getFragments();
	PluginFragmentModel[] linkedFragments130 = ((PluginDescriptorModel)pd130).getFragments();
	PluginFragmentModel[] linkedFragments240 = ((PluginDescriptorModel)pd240).getFragments();
	PluginFragmentModel[] linkedFragments110 = ((PluginDescriptorModel)pd110).getFragments();
	assertNull("4.12", linkedFragments124);
	assertNull("4.13", linkedFragments130);
	assertNull("4.14", linkedFragments240);
	assertNull("4.15", linkedFragments110);
	
	// Finally, make sure the library entry didn't show
	// up in any of the plugins
	ILibrary[] libraries124 = pd124.getRuntimeLibraries();
	ILibrary[] libraries130 = pd130.getRuntimeLibraries();
	ILibrary[] libraries240 = pd240.getRuntimeLibraries();
	ILibrary[] libraries110 = pd110.getRuntimeLibraries();
	assertTrue("4.16", libraries124.length == 0);
	assertTrue("4.17", libraries130.length == 0);
	assertTrue("4.18", libraries240.length == 0);
	assertTrue("4.19", libraries110.length == 0);
}

public void test5() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "fragmentResolveTest", null);
	InternalFactory factory = new InternalFactory(problems);
	URL pluginURLs[] = new URL[5];
	try {
		PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
		String[] pluginPath = new String[5];
		pluginPath[0] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.8/plugin1.xml");
		pluginPath[1] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.8/plugin2.xml");
		pluginPath[2] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.8/plugin3.xml");
		pluginPath[3] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.8/plugin4.xml");
		pluginPath[4] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.8/fragment5.xml");
		for (int i = 0; i < pluginPath.length; i++) {
			pluginURLs[i] = new URL (pluginPath[i]);
		}
	} catch (java.net.MalformedURLException e) {
		fail("5.0.1 Unexpected exception - " + e.getMessage());
	}
	IPluginRegistry registry = doParsing(factory, pluginURLs, true);

	// We should have 4 plugins all with id 'tests.a'
	IPluginDescriptor[] all = registry.getPluginDescriptors();
	assertTrue("5.0", all.length == 4);
	assertTrue("5.1", all[0].getUniqueIdentifier().equals("tests.a"));
	assertTrue("5.2", all[1].getUniqueIdentifier().equals("tests.a"));
	assertTrue("5.3", all[2].getUniqueIdentifier().equals("tests.a"));
	assertTrue("5.4", all[3].getUniqueIdentifier().equals("tests.a"));

	// Make sure we got all the version numbers
	IPluginDescriptor pd124 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.2.4"));
	IPluginDescriptor pd130 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.3.0"));
	IPluginDescriptor pd240 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("2.4.0"));
	IPluginDescriptor pd110 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.1.0"));
	assertNotNull("5.5", pd124);
	assertNotNull("5.6", pd130);
	assertNotNull("5.7", pd240);
	assertNotNull("5.8", pd110);

	// Check the fragment list.  There should only be one
	PluginFragmentModel[] fragments = ((PluginRegistry)registry).getFragments();
	assertTrue("5.9", fragments.length == 1);
	assertTrue("5.10", fragments[0].getId().equals("fragmentTest"));
	assertTrue("5.11", fragments[0].getPluginVersion().equals("1.2.4"));
	
	// Now make sure we hooked this fragment to the right plugin (and only
	// one plugin)
	PluginFragmentModel[] linkedFragments124 = ((PluginDescriptorModel)pd124).getFragments();
	PluginFragmentModel[] linkedFragments130 = ((PluginDescriptorModel)pd130).getFragments();
	PluginFragmentModel[] linkedFragments240 = ((PluginDescriptorModel)pd240).getFragments();
	PluginFragmentModel[] linkedFragments110 = ((PluginDescriptorModel)pd110).getFragments();
	assertNull("5.12", linkedFragments124);
	assertNotNull("5.13", linkedFragments130);
	assertNull("5.14", linkedFragments240);
	assertNull("5.15", linkedFragments110);
	assertTrue("5.16", linkedFragments130.length == 1);
	assertTrue("5.17", linkedFragments130[0].getId().equals("fragmentTest"));
	
	// Finally, make sure the library entry in the fragment is
	// now part of the proper plugin
	ILibrary[] libraries124 = pd124.getRuntimeLibraries();
	ILibrary[] libraries130 = pd130.getRuntimeLibraries();
	ILibrary[] libraries240 = pd240.getRuntimeLibraries();
	ILibrary[] libraries110 = pd110.getRuntimeLibraries();
	assertTrue("5.18", libraries124.length == 0);
	assertTrue("5.19", libraries130.length == 1);
	assertTrue("5.20", libraries240.length == 0);
	assertTrue("5.21", libraries110.length == 0);
	assertTrue("5.22", ((Library)libraries130[0]).getName().equals("lib1.jar"));
}

public void test6() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "fragmentResolveTest", null);
	InternalFactory factory = new InternalFactory(problems);
	URL pluginURLs[] = new URL[5];
	try {
		PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
		String[] pluginPath = new String[5];
		pluginPath[0] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.8/plugin1.xml");
		pluginPath[1] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.8/plugin2.xml");
		pluginPath[2] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.8/plugin3.xml");
		pluginPath[3] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.8/plugin4.xml");
		pluginPath[4] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.8/fragment6.xml");
		for (int i = 0; i < pluginPath.length; i++) {
			pluginURLs[i] = new URL (pluginPath[i]);
		}
	} catch (java.net.MalformedURLException e) {
		fail("6.0.1 Unexpected exception - " + e.getMessage());
	}
	IPluginRegistry registry = doParsing(factory, pluginURLs, true);

	// We should have 4 plugins all with id 'tests.a'
	IPluginDescriptor[] all = registry.getPluginDescriptors();
	assertTrue("6.0", all.length == 4);
	assertTrue("6.1", all[0].getUniqueIdentifier().equals("tests.a"));
	assertTrue("6.2", all[1].getUniqueIdentifier().equals("tests.a"));
	assertTrue("6.3", all[2].getUniqueIdentifier().equals("tests.a"));
	assertTrue("6.4", all[3].getUniqueIdentifier().equals("tests.a"));

	// Make sure we got all the version numbers
	IPluginDescriptor pd124 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.2.4"));
	IPluginDescriptor pd130 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.3.0"));
	IPluginDescriptor pd240 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("2.4.0"));
	IPluginDescriptor pd110 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.1.0"));
	assertNotNull("6.5", pd124);
	assertNotNull("6.6", pd130);
	assertNotNull("6.7", pd240);
	assertNotNull("6.8", pd110);

	// Check the fragment list.  There should only be one
	PluginFragmentModel[] fragments = ((PluginRegistry)registry).getFragments();
	assertTrue("6.9", fragments.length == 1);
	assertTrue("6.10", fragments[0].getId().equals("fragmentTest"));
	assertTrue("6.11", fragments[0].getPluginVersion().equals("3.0.0"));
	
	// Now make sure we didn't hook this fragment anywhere
	PluginFragmentModel[] linkedFragments124 = ((PluginDescriptorModel)pd124).getFragments();
	PluginFragmentModel[] linkedFragments130 = ((PluginDescriptorModel)pd130).getFragments();
	PluginFragmentModel[] linkedFragments240 = ((PluginDescriptorModel)pd240).getFragments();
	PluginFragmentModel[] linkedFragments110 = ((PluginDescriptorModel)pd110).getFragments();
	assertNull("6.12", linkedFragments124);
	assertNull("6.13", linkedFragments130);
	assertNull("6.14", linkedFragments240);
	assertNull("6.15", linkedFragments110);
	
	// Finally, make sure the library entry in the fragment doesn't
	// show up in any of the plugins
	ILibrary[] libraries124 = pd124.getRuntimeLibraries();
	ILibrary[] libraries130 = pd130.getRuntimeLibraries();
	ILibrary[] libraries240 = pd240.getRuntimeLibraries();
	ILibrary[] libraries110 = pd110.getRuntimeLibraries();
	assertTrue("6.16", libraries124.length == 0);
	assertTrue("6.17", libraries130.length == 0);
	assertTrue("6.18", libraries240.length == 0);
	assertTrue("6.19", libraries110.length == 0);
}

public void test7() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "fragmentResolveTest", null);
	InternalFactory factory = new InternalFactory(problems);
	URL pluginURLs[] = new URL[5];
	try {
		PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
		String[] pluginPath = new String[5];
		pluginPath[0] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.8/plugin1.xml");
		pluginPath[1] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.8/plugin2.xml");
		pluginPath[2] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.8/plugin3.xml");
		pluginPath[3] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.8/plugin4.xml");
		pluginPath[4] = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.8/fragment7.xml");
		for (int i = 0; i < pluginPath.length; i++) {
			pluginURLs[i] = new URL (pluginPath[i]);
		}
	} catch (java.net.MalformedURLException e) {
		fail("7.0.1 Unexpected exception - " + e.getMessage());
	}
	IPluginRegistry registry = doParsing(factory, pluginURLs, true);

	// We should have 4 plugins all with id 'tests.a'
	IPluginDescriptor[] all = registry.getPluginDescriptors();
	assertTrue("7.0", all.length == 4);
	assertTrue("7.1", all[0].getUniqueIdentifier().equals("tests.a"));
	assertTrue("7.2", all[1].getUniqueIdentifier().equals("tests.a"));
	assertTrue("7.3", all[2].getUniqueIdentifier().equals("tests.a"));
	assertTrue("7.4", all[3].getUniqueIdentifier().equals("tests.a"));

	// Make sure we got all the version numbers
	IPluginDescriptor pd124 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.2.4"));
	IPluginDescriptor pd130 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.3.0"));
	IPluginDescriptor pd240 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("2.4.0"));
	IPluginDescriptor pd110 = registry.getPluginDescriptor("tests.a", new PluginVersionIdentifier("1.1.0"));
	assertNotNull("7.5", pd124);
	assertNotNull("7.6", pd130);
	assertNotNull("7.7", pd240);
	assertNotNull("7.8", pd110);

	// Check the fragment list.  There should only be one
	PluginFragmentModel[] fragments = ((PluginRegistry)registry).getFragments();
	assertTrue("7.9", fragments.length == 1);
	assertTrue("7.10", fragments[0].getId().equals("fragmentTest"));
	assertTrue("7.11", fragments[0].getPluginVersion().equals("2.4.0"));
	
	// Now make sure we hooked this fragment to the right plugin (and only
	// one plugin)
	PluginFragmentModel[] linkedFragments124 = ((PluginDescriptorModel)pd124).getFragments();
	PluginFragmentModel[] linkedFragments130 = ((PluginDescriptorModel)pd130).getFragments();
	PluginFragmentModel[] linkedFragments240 = ((PluginDescriptorModel)pd240).getFragments();
	PluginFragmentModel[] linkedFragments110 = ((PluginDescriptorModel)pd110).getFragments();
	assertNull("7.12", linkedFragments124);
	assertNull("7.13", linkedFragments130);
	assertNotNull("7.14", linkedFragments240);
	assertNull("7.15", linkedFragments110);
	assertTrue("7.16", linkedFragments240.length == 1);
	assertTrue("7.17", linkedFragments240[0].getId().equals("fragmentTest"));
	
	// Finally, make sure the library entry in the fragment is
	// now part of the proper plugin
	ILibrary[] libraries124 = pd124.getRuntimeLibraries();
	ILibrary[] libraries130 = pd130.getRuntimeLibraries();
	ILibrary[] libraries240 = pd240.getRuntimeLibraries();
	ILibrary[] libraries110 = pd110.getRuntimeLibraries();
	assertTrue("7.18", libraries124.length == 0);
	assertTrue("7.19", libraries130.length == 0);
	assertTrue("7.20", libraries240.length == 1);
	assertTrue("7.21", libraries110.length == 0);
	assertTrue("7.22", ((Library)libraries240[0]).getName().equals("lib1.jar"));
}

public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new FragmentResolveTest_8("test1"));
	suite.addTest(new FragmentResolveTest_8("test2"));
	suite.addTest(new FragmentResolveTest_8("test3"));
	suite.addTest(new FragmentResolveTest_8("test4"));
	suite.addTest(new FragmentResolveTest_8("test5"));
	suite.addTest(new FragmentResolveTest_8("test6"));
	suite.addTest(new FragmentResolveTest_8("test7"));
	return suite;
}
}


