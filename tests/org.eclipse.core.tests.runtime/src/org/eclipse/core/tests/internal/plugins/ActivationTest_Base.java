package org.eclipse.core.tests.internal.plugins;


import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.WorkspaceSessionTest;
/**
 */
public class ActivationTest_Base extends WorkspaceSessionTest {
public ActivationTest_Base() {
	super(null);
}
public ActivationTest_Base(String name) {
	super(name);
}
public void testBase() {

	IPluginRegistry registry = InternalPlatform.getPluginRegistry();

	// get descriptors
	IPluginDescriptor pdA = registry.getPluginDescriptor("plugin.a");
	IPluginDescriptor pdB = registry.getPluginDescriptor("plugin.b");
	IPluginDescriptor pdC = registry.getPluginDescriptor("plugin.c");
	assertNotNull("0.0", pdA);
	assertNotNull("0.1", pdB);
	assertNotNull("0.2", pdC);

	// check initial activation state
	assertTrue("1.0", !pdA.isPluginActivated());
	assertTrue("1.1", !pdB.isPluginActivated());
	assertTrue("1.2", !pdC.isPluginActivated());

	Plugin p = null;
	// get plugin class for plugin A
	try {
		p = pdA.getPlugin();
	} catch (CoreException e) {
		fail("2.0.0");
	}
	assertTrue("2.0.1", pdA.isPluginActivated());
	assertTrue("2.1", !pdB.isPluginActivated());
	assertTrue("2.2", !pdC.isPluginActivated());
	assertTrue("2.3", p.getClass().getName().equals("org.eclipse.core.tests.internal.plugin.a.PluginClass"));

	// get plugin class for plugin B
	try {
		p = pdB.getPlugin();
	} catch (CoreException e) {
		fail("3.0.0");
	}
	assertTrue("3.0.1", pdA.isPluginActivated());
	assertTrue("3.1", pdB.isPluginActivated());
	assertTrue("3.2", !pdC.isPluginActivated());
	assertTrue("3.3", p.getClass().getName().equals("org.eclipse.core.tests.internal.plugin.b.PluginClass"));

	// get plugin class for plugin C
	try {
		p = pdC.getPlugin();
	} catch (CoreException e) {
		fail("4.0.0");
	}
	assertTrue("4.0.1", pdA.isPluginActivated());
	assertTrue("4.1", pdB.isPluginActivated());
	assertTrue("4.2", pdC.isPluginActivated());
	assertTrue("4.3", p.getClass().getName().equals("org.eclipse.core.tests.internal.plugin.c.PluginClass"));

}
}
