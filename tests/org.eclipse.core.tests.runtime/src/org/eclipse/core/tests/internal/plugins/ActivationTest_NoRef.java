package org.eclipse.core.tests.internal.plugins;

import org.eclipse.core.boot.IPlatformRunnable;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.WorkspaceSessionTest;
/**
 */
public class ActivationTest_NoRef extends WorkspaceSessionTest {
public ActivationTest_NoRef() {
	super(null);
}
public ActivationTest_NoRef(String name) {
	super(name);
}
public void testExtensionNoReferenceTest() {

	IPluginRegistry registry = InternalPlatform.getPluginRegistry();

	// get descriptors
	IPluginDescriptor pluginA = registry.getPluginDescriptor("plugin.a");
	IPluginDescriptor pluginB = registry.getPluginDescriptor("plugin.b");
	IPluginDescriptor pluginC = registry.getPluginDescriptor("plugin.c");
	assertNotNull("0.0", pluginA);
	assertNotNull("0.1", pluginB);
	assertNotNull("0.2", pluginC);

	// check initial activation state
	assertTrue("1.0", !pluginA.isPluginActivated());
	assertTrue("1.1", !pluginB.isPluginActivated());
	assertTrue("1.2", !pluginC.isPluginActivated());

	// get base extension
	IConfigurationElement[] cfig = registry.getConfigurationElementsFor("plugin.a", "case", "plugin.a" + ".base");
	assertTrue("2.0", cfig.length == 1);
	IPlatformRunnable o = null;
	try {
		o = (IPlatformRunnable) cfig[0].createExecutableExtension("run");
	} catch (CoreException e) {
		fail("2.1");
	}

	// check activation state
	assertTrue("3.0", pluginA.isPluginActivated());
	assertTrue("3.1", !pluginB.isPluginActivated());
	assertTrue("3.2", !pluginC.isPluginActivated());

	// run the extension
	try {
		Object result = o.run(null);
	} catch (Exception e) {
		fail("4.0");
	}

	// check activation state
	assertTrue("5.0", pluginA.isPluginActivated());
	assertTrue("5.1", !pluginB.isPluginActivated());
	assertTrue("5.2", !pluginC.isPluginActivated());

}
}
