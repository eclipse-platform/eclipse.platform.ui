package org.eclipse.core.tests.internal.plugins;

import java.net.URL;
import junit.framework.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.plugins.InternalFactory;
import org.eclipse.core.internal.plugins.PluginDescriptor;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.Policy;
/**
 */
public class SoftPrereqTest_9 extends PluginResolveTest {
public SoftPrereqTest_9() {
	super(null);
}
public SoftPrereqTest_9(String name) {
	super(name);
}
public void baseTest() {
	
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("softPrerequisiteTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/softPrereq.9/");
	URL pluginURLs[] = new URL[1];
	URL pURL = null;
	try {
		pURL = new URL (pluginPath);
	} catch (java.net.MalformedURLException e) {
		assertTrue("Bad URL for " + pluginPath, true);
	}
	pluginURLs[0] = pURL;
	IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, true);

	IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors();
	assertEquals("0.0", pluginDescriptors.length, 5);
	
	// look for tests.a
	pluginDescriptors = registry.getPluginDescriptors("tests.a");
	assertEquals("1.0", pluginDescriptors.length, 1);
	assertEquals("1.1", pluginDescriptors[0].getUniqueIdentifier(), "tests.a");
	assertEquals("1.2", pluginDescriptors[0].getVersionIdentifier(), new PluginVersionIdentifier("1.2.0"));
	
	// look for tests.b - there should be 1
	pluginDescriptors = registry.getPluginDescriptors("tests.b");
	assertEquals("2.0", pluginDescriptors.length, 1);
	assertEquals("2.1", pluginDescriptors[0].getUniqueIdentifier(), "tests.b");
	assertEquals("2.2", pluginDescriptors[0].getVersionIdentifier(), new PluginVersionIdentifier("1.1.0"));
	
	// look for tests.c - there should be 2
	pluginDescriptors = registry.getPluginDescriptors("tests.c");
	assertEquals("3.0", pluginDescriptors.length, 2);
	IPluginDescriptor pluginC_110 = null;
	IPluginDescriptor pluginC_210 = null;
	for (int i = 0; i < pluginDescriptors.length; i++) {
		boolean badVersion = false;
		IPluginDescriptor thisPlugin = pluginDescriptors[i];
		if (thisPlugin.getVersionIdentifier().equals(new PluginVersionIdentifier("1.1.0")))
			pluginC_110 = thisPlugin;
		else if (thisPlugin.getVersionIdentifier().equals(new PluginVersionIdentifier("2.1.0")))
			pluginC_210 = thisPlugin;
		else {
			badVersion = true;
			assertTrue("3.1 Bad version found" + thisPlugin.getVersionIdentifier().toString(), false);
		}
		if (!badVersion)
			assertEquals("3.2", pluginDescriptors[0].getUniqueIdentifier(), "tests.c");
	}
	assertNotNull("3.3", pluginC_110);
	assertNotNull("3.4", pluginC_210);

	// look for tests.d - there should be 1
	pluginDescriptors = registry.getPluginDescriptors("tests.d");
	assertEquals("4.0", pluginDescriptors.length, 1);
	assertEquals("4.1", pluginDescriptors[0].getUniqueIdentifier(), "tests.d");
	assertEquals("4.2", pluginDescriptors[0].getVersionIdentifier(), new PluginVersionIdentifier("2.1.0"));
}
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new SoftPrereqTest_9("baseTest"));
	return suite;
}
}
