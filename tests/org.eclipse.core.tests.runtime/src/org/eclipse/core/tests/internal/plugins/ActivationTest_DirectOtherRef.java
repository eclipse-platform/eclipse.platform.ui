package org.eclipse.core.tests.internal.plugins;


import org.eclipse.core.boot.IPlatformRunnable;
import org.eclipse.core.internal.plugins.PluginClassLoader;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.WorkspaceSessionTest;
/**
 */
public class ActivationTest_DirectOtherRef extends WorkspaceSessionTest {
public ActivationTest_DirectOtherRef() {
	super(null);
}
public ActivationTest_DirectOtherRef(String name) {
	super(name);
}
public void testExtensionDirectReferenceToOtherClassTest() {

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
	IConfigurationElement[] cfig = registry.getConfigurationElementsFor("plugin.a", "case", "plugin.a" + ".referenceOther");
	assertTrue("2.0", cfig.length == 1);
	IPlatformRunnable o = null;
	try {
		o = (IPlatformRunnable) cfig[0].createExecutableExtension("run");
	} catch (CoreException e) {
		fail("2.1 " + e.getMessage());
	}

	// check activation state
	assertTrue("3.0", pluginA.isPluginActivated());
	assertTrue("3.1", !pluginB.isPluginActivated());
	assertTrue("3.2", !pluginC.isPluginActivated());

	// run the extension
	Object result = null;
	try {
		result = o.run(null);
	} catch (Exception e) {
		fail("4.0 " + e.getMessage());
	}
	if (result instanceof Exception) {
		fail("4.1 " + ((Exception) result).getMessage());
	}

	// check activation state
	assertTrue("5.0", pluginA.isPluginActivated());
	assertTrue("5.1", !pluginB.isPluginActivated());
	assertTrue("5.2", pluginC.isPluginActivated()); // by direct reference to class in C from A.run()

	// check result
	if (result instanceof Class) {
		ClassLoader loader = ((Class) result).getClassLoader();
		if (loader instanceof PluginClassLoader) {
			assertTrue("6.0", pluginC == ((PluginClassLoader) loader).getPluginDescriptor());
		} else
			fail("6.1");
	} else
		fail("6.2");

	// check activation state (no change)
	assertTrue("7.0", pluginA.isPluginActivated());
	assertTrue("7.1", !pluginB.isPluginActivated());
	assertTrue("7.2", pluginC.isPluginActivated());

}
}
