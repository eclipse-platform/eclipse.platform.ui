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
public class SoftPrereqTest_8 extends PluginResolveTest {
public SoftPrereqTest_8() {
	super(null);
}
public SoftPrereqTest_8(String name) {
	super(name);
}
public void baseTest() {
	
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("softPrerequisiteTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/softPrereq.8/");
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
	assertEquals("0.0", pluginDescriptors.length, 2);
	
	// look for tests.a - should not exist as it caused the conflict
	pluginDescriptors = registry.getPluginDescriptors("tests.a");
	assertEquals("1.0", pluginDescriptors.length, 0);
	
	// look for tests.b - there should be 1
	// Because tests.a causes a conflict, it is removed/disabled.  This causes
	// tests.b to be orphaned.  Therefore, tests.b gets considered a root node.
	// We then pick up the latest version of tests.b and resolve for it.
	pluginDescriptors = registry.getPluginDescriptors("tests.b");
	assertEquals("2.0", pluginDescriptors.length, 1);
	assertEquals("2.1", pluginDescriptors[0].getUniqueIdentifier(), "tests.b");
	assertEquals("2.2", pluginDescriptors[0].getVersionIdentifier(), new PluginVersionIdentifier("2.1.0"));
	
	// look for tests.c - there should be 1
	pluginDescriptors = registry.getPluginDescriptors("tests.c");
	assertEquals("3.0", pluginDescriptors.length, 1);
	assertEquals("3.1", pluginDescriptors[0].getUniqueIdentifier(), "tests.c");
	assertEquals("3.2", pluginDescriptors[0].getVersionIdentifier(), new PluginVersionIdentifier("2.1.0"));
}
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new SoftPrereqTest_8("baseTest"));
	return suite;
}
}
