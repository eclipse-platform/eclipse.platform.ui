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
public class PluginResolveTest_21 extends PluginResolveTest {
public PluginResolveTest_21() {
	super(null);
}
public PluginResolveTest_21(String name) {
	super(name);
}
public void baseTest() {
	
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/plugins.resolve.21/");
	URL pluginURLs[] = new URL[1];
	URL pURL = null;
	try {
		pURL = new URL (pluginPath);
	} catch (java.net.MalformedURLException e) {
		assertTrue("Bad URL for " + pluginPath, true);
	}
	pluginURLs[0] = pURL;
	IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, true);

	IPluginDescriptor[] plugins = registry.getPluginDescriptors();
	assertEquals("0.0", plugins.length, 2);
	
	IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors("tests.a");
	assertEquals("1.0", pluginDescriptors.length, 1);
	assertEquals("1.1", pluginDescriptors[0].getUniqueIdentifier(), "tests.a");
	assertEquals("1.2", pluginDescriptors[0].getVersionIdentifier(), new PluginVersionIdentifier("1.1.0"));

	pluginDescriptors = registry.getPluginDescriptors("tests.b");
	assertEquals("2.0", pluginDescriptors.length, 1);
	assertEquals("2.1", pluginDescriptors[0].getUniqueIdentifier(), "tests.b");
	assertEquals("2.2", pluginDescriptors[0].getVersionIdentifier(), new PluginVersionIdentifier("2.2.4"));
	
	
}
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new PluginResolveTest_21("baseTest"));
	return suite;
}
}
