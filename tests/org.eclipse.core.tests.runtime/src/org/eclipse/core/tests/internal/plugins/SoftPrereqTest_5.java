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
public class SoftPrereqTest_5 extends PluginResolveTest {
public SoftPrereqTest_5() {
	super(null);
}
public SoftPrereqTest_5(String name) {
	super(name);
}
public void baseTest() {
	
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("softPrerequisiteTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/softPrereq.5/");
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
	assertEquals("0.0", pluginDescriptors.length, 1);
	
	// look for tests.a - should not be found (the real prerequisite doesn't exist)
	pluginDescriptors = registry.getPluginDescriptors("tests.a");
	assertEquals("1.0", pluginDescriptors.length, 0);

	// look for tests.b - should exist
	pluginDescriptors = registry.getPluginDescriptors("tests.b");
	assertEquals("2.0", pluginDescriptors.length, 1);
	assertEquals("2.1", pluginDescriptors[0].getUniqueIdentifier(), "tests.b");
	assertEquals("2.2", pluginDescriptors[0].getVersionIdentifier(), new PluginVersionIdentifier("1.0.1"));
	
	// look for tests.c - shouldn't be found
	pluginDescriptors = registry.getPluginDescriptors("tests.c");
	assertEquals("3.0", pluginDescriptors.length, 0);
	
}
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new SoftPrereqTest_5("baseTest"));
	return suite;
}
}
