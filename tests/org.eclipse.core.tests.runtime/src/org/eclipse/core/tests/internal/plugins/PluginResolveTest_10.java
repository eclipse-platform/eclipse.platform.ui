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
public class PluginResolveTest_10 extends PluginResolveTest {
public PluginResolveTest_10() {
	super(null);
}
public PluginResolveTest_10(String name) {
	super(name);
}
public void baseTest() {
	
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/plugins.resolve.10/");
	URL pluginURLs[] = new URL[1];
	URL pURL = null;
	try {
		pURL = new URL (pluginPath);
	} catch (java.net.MalformedURLException e) {
		assertTrue("Bad URL for " + pluginPath, true);
	}
	pluginURLs[0] = pURL;
	IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, true);

	IPluginDescriptor pluginDescriptor = null;

	PluginVersionIdentifier verIdA = new PluginVersionIdentifier("1.2.0");
	pluginDescriptor = registry.getPluginDescriptor("tests.a", verIdA);
	assertNotNull("0.0", pluginDescriptor);
	assertEquals("0.1", pluginDescriptor.getUniqueIdentifier(), "tests.a");
	assertEquals("0.2", pluginDescriptor.getVersionIdentifier(), verIdA);
	verIdA = null;

	PluginVersionIdentifier verIdB = new PluginVersionIdentifier("1.0.0");
	pluginDescriptor = registry.getPluginDescriptor("tests.b", verIdB);
	assertNotNull("1.0", pluginDescriptor);
	assertEquals("1.1", pluginDescriptor.getUniqueIdentifier(), "tests.b");
	assertEquals("1.2", pluginDescriptor.getVersionIdentifier(), verIdB);
	verIdB = null;

	PluginVersionIdentifier verIdC1 = new PluginVersionIdentifier("1.1.0");
	PluginVersionIdentifier verIdC2 = new PluginVersionIdentifier("2.0.5");
	pluginDescriptor = registry.getPluginDescriptor("tests.c", verIdC1);
	assertNotNull("2.0", pluginDescriptor);
	assertEquals("2.1", pluginDescriptor.getUniqueIdentifier(), "tests.c");
	assertEquals("2.2", pluginDescriptor.getVersionIdentifier(), verIdC1);
	verIdC1 = null;
	pluginDescriptor = registry.getPluginDescriptor("tests.c", verIdC2);
	assertNotNull("2.3", pluginDescriptor);
	assertEquals("2.4", pluginDescriptor.getUniqueIdentifier(), "tests.c");
	assertEquals("2.5", pluginDescriptor.getVersionIdentifier(), verIdC2);
	verIdC2 = null;

	PluginVersionIdentifier verIdD = new PluginVersionIdentifier("1.0.0");
	pluginDescriptor = registry.getPluginDescriptor("tests.d", verIdD);
	assertNotNull("3.0", pluginDescriptor);
	assertEquals("3.1", pluginDescriptor.getUniqueIdentifier(), "tests.d");
	assertEquals("3.2", pluginDescriptor.getVersionIdentifier(), verIdD);
	verIdD = null;
}
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new PluginResolveTest_10("baseTest"));
	return suite;
}
}


