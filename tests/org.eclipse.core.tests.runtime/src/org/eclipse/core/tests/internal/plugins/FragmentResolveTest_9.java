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

public class FragmentResolveTest_9 extends EclipseWorkspaceTest {
public FragmentResolveTest_9() {
	super(null);
}
public FragmentResolveTest_9(String name) {
	super(name);
}

public void fullTest() {
	String[] fragmentIds = {"1.3.2", "1.3.7", "1.2.5", "1.2.8",
		"1.2.2", "1.2.0", "1.1.5", "1.1.0", "1.3.5", "3.0.0", "3.2.5", "3.2.4"}; 
	boolean[] matchesFragment = {false, false, true, false,
		true, true, true, true, false, false, false, false};

	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "fragmentResolveTest", null);
	InternalFactory factory = new InternalFactory(problems);

	for (int i = 0; i < fragmentIds.length; i++) {
		URL pluginURLs[] = new URL[2];
		try {
			PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
			String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.9/plugin1.xml");
			pluginURLs[0] = new URL (pluginPath);
			pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/fragment.resolve.9/fragment" + (i + 1) + ".xml");
			pluginURLs[1] = new URL (pluginPath);
		} catch (java.net.MalformedURLException e) {
			fail("0.0.1 Unexpected exception - " + e.getMessage());
		}
		IPluginRegistry registry = ParseHelper.doParsing(factory, pluginURLs, true);
		IPluginDescriptor pd = registry.getPluginDescriptor("tests.a");
	
		// check descriptor
		assertNotNull(i + ".0", pd);
		assertTrue(i + ".1", pd.getUniqueIdentifier().equals("tests.a"));
	
		// check to see if we have all plugins
		IPluginDescriptor[] all = registry.getPluginDescriptors();
		assertTrue(i + ".2", all.length == 1);
		assertTrue(i + ".3", all[0].getUniqueIdentifier().equals("tests.a"));
		assertTrue(i + ".4", all[0].getVersionIdentifier().equals(new PluginVersionIdentifier("1.2.5")));
	
		// Check the fragment list.  There should only be one
		PluginFragmentModel[] fragment = ((PluginRegistry) registry).getFragments("fragmentTest");
		assertTrue(i + ".5", fragment.length == 1);
		assertTrue(i + ".6", fragment[0].getId().equals("fragmentTest"));
		
		// Have we got all the fragments
		PluginFragmentModel[] fragments = ((PluginRegistry)registry).getFragments();
		assertTrue(i + ".7", fragments.length == 1);
		assertTrue(i + ".8", fragments[0].getId().equals("fragmentTest"));
		assertTrue(i + ".9", fragments[0].getPluginVersion().equals(fragmentIds[i]));
		
		if (matchesFragment[i]) {
			// Now make sure we hooked this fragment and this plugin
			PluginFragmentModel[] linkedFragments = ((PluginDescriptorModel)pd).getFragments();
			assertNotNull(i + ".10", linkedFragments);
			assertTrue(i + ".11", linkedFragments.length == 1);
			assertTrue(i + ".12", linkedFragments[0].getId().equals("fragmentTest"));
			
			// Finally, make sure the library entry in the fragment is
			// now part of the plugin
			ILibrary[] libraries = pd.getRuntimeLibraries();
			assertTrue(i + ".13", libraries.length == 1);
			assertTrue(i + ".14", ((Library)libraries[0]).getName().equals("lib1.jar"));
		} else {
			// Now make sure we didn't hook this fragment and this plugin.
			PluginFragmentModel[] linkedFragments = ((PluginDescriptorModel)pd).getFragments();
			assertNull(i + ".10", linkedFragments);
			
			// Finally, make sure the library entry in the fragment is
			// not part of the plugin
			ILibrary[] libraries = pd.getRuntimeLibraries();
			assertTrue(i + ".11", libraries.length == 0);
		}
	}
}

public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new FragmentResolveTest_9("fullTest"));
	return suite;
}
}


