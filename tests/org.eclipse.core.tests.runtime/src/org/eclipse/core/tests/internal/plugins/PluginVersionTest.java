package org.eclipse.core.tests.internal.plugins;

import java.net.URL;
import junit.framework.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.plugins.InternalFactory;
import org.eclipse.core.internal.plugins.PluginDescriptor;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.tests.harness.*;
/**
 */
public class PluginVersionTest extends EclipseWorkspaceTest {
public PluginVersionTest() {
	super(null);
}
public PluginVersionTest(String name) {
	super(name);
}
public void baseTest() {

	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);
	URL pluginURLs[] = new URL[1];
	URL pURL = null;
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/plugins.version.1/");
	try {
		pURL = new URL (pluginPath);
	} catch (java.net.MalformedURLException e) {
		assertTrue("Bad URL for " + pluginPath, true);
	}
	pluginURLs[0] = pURL;
	IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, true);
	IPluginDescriptor pd = registry.getPluginDescriptor("plugin.a");

	// check descriptor
	assertTrue("0.0", null != pd);
	assertTrue("0.1", pd.getUniqueIdentifier().equals("plugin.a"));

	// check to see if we have all plugins
	IPluginDescriptor[] all = registry.getPluginDescriptors();
	assertTrue("1.0", all.length != 0);
	int count = 0;
	for (int i = 0; i < all.length; i++) {
		if (all[i].getUniqueIdentifier().equals("plugin.a"))
			count++;
	}
	assertTrue("1.1", count == 3);

	// try again using filtered list
	all = registry.getPluginDescriptors("plugin.a");
	assertTrue("1.2", all.length == 3);

	// try again using filtered list and invalid id
	all = registry.getPluginDescriptors("plugin.a" + ".invalid");
	assertTrue("1.3", all.length == 0);

	// lookup descriptors individually
	PluginVersionIdentifier v = new PluginVersionIdentifier("1.0.0");
	pd = registry.getPluginDescriptor("plugin.a", v);
	assertTrue("2.0.0", pd == null);

	v = new PluginVersionIdentifier("1.0.1");
	pd = registry.getPluginDescriptor("plugin.a", v);
	assertTrue("2.1.0", pd != null);
	assertTrue("2.1.1", "plugin.a".equals(pd.getUniqueIdentifier()));
	assertTrue("2.1.2", v.equals(pd.getVersionIdentifier()));

	v = new PluginVersionIdentifier("1.1.0");
	pd = registry.getPluginDescriptor("plugin.a", v);
	assertTrue("2.2.0", pd != null);
	assertTrue("2.2.1", "plugin.a".equals(pd.getUniqueIdentifier()));
	assertTrue("2.2.2", v.equals(pd.getVersionIdentifier()));

	v = new PluginVersionIdentifier("2.0.0");
	pd = registry.getPluginDescriptor("plugin.a", v);
	assertTrue("2.3.0", pd == null);

	v = new PluginVersionIdentifier("2.0.15");
	pd = registry.getPluginDescriptor("plugin.a", v);
	assertTrue("2.4.0", pd != null);
	assertTrue("2.4.1", "plugin.a".equals(pd.getUniqueIdentifier()));
	assertTrue("2.4.2", v.equals(pd.getVersionIdentifier()));

	v = new PluginVersionIdentifier("9.9.9");
	pd = registry.getPluginDescriptor("plugin.a", v);
	assertTrue("2.5.0", pd == null);

}
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new PluginVersionTest("baseTest"));
	return suite;
}
}
