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
public class PluginResolveTest_5 extends PluginResolveTest {
public PluginResolveTest_5() {
	super(null);
}
public PluginResolveTest_5(String name) {
	super(name);
}
public void baseTest() {
	// prerequisite loop test

	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);
	URL pluginURLs[] = new URL[1];
	URL pURL = null;
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/plugins.resolve.5/");
	try {
		pURL = new URL (pluginPath);
	} catch (java.net.MalformedURLException e) {
		assertTrue("Bad URL for " + pluginPath, true);
	}
	pluginURLs[0] = pURL;
	IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, true);
	IPluginDescriptor pd;

	pd = registry.getPluginDescriptor("tests.a");
	assertNull("0.1", pd);

	pd = registry.getPluginDescriptor("tests.b");
	assertNull("0.2", pd);

	pd = registry.getPluginDescriptor("tests.c");
	assertNull("0.3", pd);

	pd = registry.getPluginDescriptor("tests.d");
	assertNull("0.4", pd);

}
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new PluginResolveTest_5("baseTest"));
	return suite;
}
}
