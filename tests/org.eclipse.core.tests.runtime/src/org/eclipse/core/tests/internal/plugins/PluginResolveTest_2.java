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
public class PluginResolveTest_2 extends PluginResolveTest {
public PluginResolveTest_2() {
	super(null);
}
public PluginResolveTest_2(String name) {
	super(name);
}
public void baseTest() {

	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);
	URL pluginURLs[] = new URL[1];
	URL pURL = null;
	try {
		PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
		String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/plugins.resolve.2/");
		pURL = new URL (pluginPath);
	} catch (java.net.MalformedURLException e) {
		fail("0.0.0 Unexpected exception - " + e.getMessage());
	}
	pluginURLs[0] = pURL;
	IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, true);
	IPluginDescriptor pd = null;

	pd = checkResolved(registry, "0.0", "tests.a", "1.2.2");
	checkResolvedPrereqs("0.1", pd, new String[] { "tests.b" }, new String[] { "2.2.0" });

	pd = checkResolved(registry, "1.0", "tests.b", "2.2.0");
	checkResolvedPrereqs("1.1", pd, new String[] { "tests.c" }, new String[] { "2.0.5" });

	pd = checkResolved(registry, "2.0", "tests.c", "2.0.5");
	checkResolvedPrereqs("2.1", pd, new String[] { "tests.d" }, new String[] { "2.0.0" });

	pd = checkResolved(registry, "3.0", "tests.d", "2.0.0");
	checkResolvedPrereqs("3.1", pd, new String[] { "tests.e" }, new String[] { "2.1.0" });

	pd = checkResolved(registry, "4.0", "tests.e", "2.1.0");
	checkResolvedPrereqs("4.1", pd, new String[] {	}, new String[] {	});
}
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new PluginResolveTest_2("baseTest"));
	return suite;
}
}
