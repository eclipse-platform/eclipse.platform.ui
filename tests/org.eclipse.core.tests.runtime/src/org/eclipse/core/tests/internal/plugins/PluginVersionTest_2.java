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
public class PluginVersionTest_2 extends EclipseWorkspaceTest {
public PluginVersionTest_2() {
	super(null);
}
public PluginVersionTest_2(String name) {
	super(name);
}
public void baseTest() {

	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);
	URL pluginURLs[] = new URL[1];
	URL pURL = null;
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/plugins.version.2/");
	try {
		pURL = new URL (pluginPath);
	} catch (java.net.MalformedURLException e) {
		assertTrue("Bad URL for " + pluginPath, true);
	}
	pluginURLs[0] = pURL;
	IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, true);
	
	IPluginDescriptor pd;
	IPluginDescriptor[] list;
	IPluginPrerequisite[] prereqs;

	// Check plugin a
	list = registry.getPluginDescriptors("a");
	assertEquals("1.0", list.length, 1);
	pd = list[0];
	assertEquals("1.1", pd.getVersionIdentifier(), new PluginVersionIdentifier("2.0.0"));
	prereqs = pd.getPluginPrerequisites();
	assertEquals("1.2", prereqs.length, 1);
	assertEquals("1.3", prereqs[0].getUniqueIdentifier(), "b");
	assertEquals("1.4", prereqs[0].getResolvedVersionIdentifier(), new PluginVersionIdentifier("1.2.0"));
	
	// Check plugin b
	list = registry.getPluginDescriptors("b");
	assertEquals("2.0", list.length, 1);
	pd = list[0];
	assertEquals("2.1", pd.getVersionIdentifier(), new PluginVersionIdentifier("1.2.0"));
	prereqs = pd.getPluginPrerequisites();
	assertEquals("2.2", prereqs.length, 1);
	assertEquals("2.3", prereqs[0].getUniqueIdentifier(), "c");
	assertEquals("2.4", prereqs[0].getResolvedVersionIdentifier(), new PluginVersionIdentifier("1.3.0"));
	
	// Check plugin c
	list = registry.getPluginDescriptors("c");
	assertEquals("3.0", list.length, 1);
	pd = list[0];
	assertEquals("3.1", pd.getVersionIdentifier(), new PluginVersionIdentifier("1.3.0"));
	prereqs = pd.getPluginPrerequisites();
	assertEquals("3.2", prereqs.length, 2);
	IPluginPrerequisite prereqD = null;
	IPluginPrerequisite prereqE = null;
	for (int i = 0; i < prereqs.length; i++) {
		if (prereqs[i].getUniqueIdentifier().equals("d"))
			prereqD = prereqs[i];
		else if (prereqs[i].getUniqueIdentifier().equals("e"))
			prereqE = prereqs[i];
	}
	assertNotNull("3.3", prereqD);
	assertNotNull("3.4", prereqE);
	assertEquals("3.5", prereqD.getResolvedVersionIdentifier(), new PluginVersionIdentifier("1.3.0"));
	assertEquals("3.6", prereqE.getResolvedVersionIdentifier(), new PluginVersionIdentifier("1.3.2"));
	
	// Check plugin d
	list = registry.getPluginDescriptors("d");
	assertEquals("4.0", list.length, 1);
	pd = list[0];
	assertEquals("4.1", pd.getVersionIdentifier(), new PluginVersionIdentifier("1.3.0"));
	prereqs = pd.getPluginPrerequisites();
	assertEquals("4.2", prereqs.length, 0);
	
	// Check plugin e
	list = registry.getPluginDescriptors("e");
	assertEquals("5.0", list.length, 1);
	pd = list[0];
	assertEquals("5.1", pd.getVersionIdentifier(), new PluginVersionIdentifier("1.3.2"));
	prereqs = pd.getPluginPrerequisites();
	assertEquals("5.2", prereqs.length, 0);
}

public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new PluginVersionTest_2("baseTest"));
	return suite;
}
}
