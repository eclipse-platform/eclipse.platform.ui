package org.eclipse.core.tests.plugintests;

import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.WorkspaceSessionTest;
import org.eclipse.core.tests.internal.runtimetests.*;
/**
 */
public class LibraryLookupTests extends WorkspaceSessionTest {
public LibraryLookupTests() {
	super(null);
}
public LibraryLookupTests(String name) {
	super(name);
}

public void resourceHelper(IPluginRegistry registry, String pluginName, String errorPrefix, String newKey) {
	IPluginDescriptor plugin = registry.getPluginDescriptor(pluginName);
	assertNotNull(errorPrefix + ".0.resource", plugin);
	// check initial activation state
	assertTrue(errorPrefix + ".1.resource", !plugin.isPluginActivated());
	// Now try to get a resource string from this plugin.
	// This should not cause the plugin to be activated.
	String str = plugin.getResourceString("%key");
	assertTrue(errorPrefix + ".2.resource", !plugin.isPluginActivated());
	assertNotNull(errorPrefix + ".3.resource", str);
	assertTrue(errorPrefix + ".4.resource", str.equals(newKey));
}

public void testResources() {
	IPluginRegistry registry = InternalPlatform.getPluginRegistry();
	
	// PluginA has one plugin.properties file:
	// 	<pluginRootDirectory>/plugin.properties
	// This should be found and the "%key" translated to
	// "Test string from pluginA root directory".
	resourceHelper(registry, "plugina", "A", "Test string from pluginA root directory");


	// PluginB has one plugin.properties file:
	// 	<pluginRootDirectory>/bin/plugin.properties
	// This should be found and the "%key" translated to 
	// "Test string from pluginB bin directory".
	resourceHelper(registry, "pluginb", "B", "Test string from pluginB bin directory");

	// PluginC has two plugin.properties files:
	// 	<pluginRootDirectory>/plugin.properties
	// 	<pluginRootDirectory>/bin/plugin.properties
	// The one in the root directory should be found first and %key
	// translated to
	// "Test string from pluginC root directory".
	resourceHelper(registry, "pluginc", "C", "Test string from pluginC root directory");

	// PluginD has one plugin.properties file:
	// 	<fragmentRootDirectory>/plugin.properties
	// This should be found and the "%key" translated to 
	// "Test string from pluginD fragment root directory".
	resourceHelper(registry, "plugind", "D", "Test string from pluginD fragment root directory");

	// PluginE has one plugin.properties file
	// 	<fragmentRootDirectory>/bin/plugin.properties
	// This should be found and the "%key" translated to 
	// "Test string from pluginE fragment bin directory".
	resourceHelper(registry, "plugine", "E", "Test string from pluginE fragment bin directory");

	// PluginF has two plugin.properties files
	// 	<fragmentRootDirectory>/plugin.properties
	// 	<fragmentRootDirectory>/bin/plugin.properties
	// The one in the root directory should be found first and %key
	// translated to
	// "Test string from pluginF fragment root directory".
	resourceHelper(registry, "pluginf", "F", "Test string from pluginF fragment root directory");

	// PluginG has two plugin.properties files
	// 	<pluginRootDirectory>/plugin.properties
	// 	<fragmentRootDirectory>/plugin.properties
	// The one in the root directory should be found first and %key
	// translated to
	// "Test string from pluginG root directory".
	resourceHelper(registry, "pluging", "G", "Test string from pluginG root directory");

	// PluginH has two plugin.properties files
	// 	<pluginRootDirectory>/plugin.properties
	// 	<fragmentRootDirectory>/bin/plugin.properties
	// The one in the root directory should be found first and %key
	// translated to
	// "Test string from pluginH root directory".
	resourceHelper(registry, "pluginh", "H", "Test string from pluginH root directory");

	// PluginI has two plugin.properties files
	// 	<pluginRootDirectory>/bin/plugin.properties
	// 	<fragmentRootDirectory>/plugin.properties
	// The one in the plugin bin directory should be found first and %key
	// translated to
	// "Test string from pluginI bin directory".
//	resourceHelper(registry, "plugini", "I", "Test string from pluginI bin directory");

	// PluginJ has two plugin.properties files
	// 	<pluginRootDirectory>/bin/plugin.properties
	// 	<fragmentRootDirectory>/bin/plugin.properties
	// The one in the plugin bin directory should be found first and %key
	// translated to
	// "Test string from pluginJ bin directory".
	resourceHelper(registry, "pluginj", "J", "Test string from pluginJ bin directory");

	// PluginK has three plugin.properties files
	// 	<pluginRootDirectory>/plugin.properties
	// 	<pluginRootDirectory>/bin/plugin.properties
	// 	<fragmentRootDirectory>/plugin.properties
	// The one in the plugin root directory should be found first and %key
	// translated to
	// "Test string from pluginK root directory".
	resourceHelper(registry, "plugink", "K", "Test string from pluginK root directory");

	// PluginL has three plugin.properties files
	// 	<pluginRootDirectory>/plugin.properties
	// 	<pluginRootDirectory>/bin/plugin.properties
	// 	<fragmentRootDirectory>/bin/plugin.properties
	// The one in the plugin root directory should be found first and %key
	// translated to
	// "Test string from pluginL root directory".
	resourceHelper(registry, "pluginl", "L", "Test string from pluginL root directory");

	// PluginM has three plugin.properties files
	// 	<pluginRootDirectory>/plugin.properties
	// 	<fragmentRootDirectory>/plugin.properties
	// 	<fragmentRootDirectory>/bin/plugin.properties
	// The one in the plugin root directory should be found first and %key
	// translated to
	// "Test string from pluginM root directory".
	resourceHelper(registry, "pluginm", "M", "Test string from pluginM root directory");

	// PluginN has three plugin.properties files
	// 	<pluginRootDirectory>/bin/plugin.properties
	// 	<fragmentRootDirectory>/plugin.properties
	// 	<fragmentRootDirectory>/bin/plugin.properties
	// The one in the plugin bin directory should be found first and %key
	// translated to
	// "Test string from pluginN bin directory".
//	resourceHelper(registry, "pluginn", "N", "Test string from pluginN bin directory");

	// PluginH has four plugin.properties files
	// 	<pluginRootDirectory>/plugin.properties
	// 	<pluginRootDirectory>/bin/plugin.properties
	// 	<fragmentRootDirectory>/plugin.properties
	// 	<fragmentRootDirectory>/bin/plugin.properties
	// The one in the plugin root directory should be found first and %key
	// translated to
	// "Test string from pluginO root directory".
	resourceHelper(registry, "plugino", "O", "Test string from pluginO root directory");
}

public void codeHelper(IPluginRegistry registry, String pluginName, String errorPrefix, String className) {
	IPluginDescriptor plugin = registry.getPluginDescriptor(pluginName);
	assertNotNull(errorPrefix + ".0.code", plugin);
	// check initial activation state
	assertTrue(errorPrefix + ".1.code", !plugin.isPluginActivated());
	// Now try to access the plugin class.  This should cause
	// the plugin to be activated.
	Plugin active = null;
	try {
		active = plugin.getPlugin();
	} catch (CoreException ce) {
		fail(errorPrefix + ".2.code Core exception encountered.",ce);
	}
	assertTrue (errorPrefix + ".3.code", active.getClass().getName().equals(className));
	assertTrue(errorPrefix + ".4.code", plugin.isPluginActivated());
}

public void testCode() {
	IPluginRegistry registry = InternalPlatform.getPluginRegistry();

	String classNamePrefix = "org.eclipse.core.tests.internal.runtimetests.";
	// codePluginA has one jar file
	//	<pluginRootDirectory>/codePluginA.jar (with class SampleA).
	codeHelper(registry, "codePluginA", "A", classNamePrefix + "SampleA");

	// codePluginB has one jar file
	//	<pluginRootDirectory>/bin/codePluginB.jar (with class SampleB).
	codeHelper(registry, "codePluginB", "B", classNamePrefix + "SampleB");

	// codePluginC has two jar files
	//	<pluginRootDirectory>/codePluginC.jar (with class SampleCPR)
	//	<pluginRootDirectory>/bin/codePluginC.jar (with class SampleCPB).
	codeHelper(registry, "codePluginC", "C", classNamePrefix + "SampleCPR");

	// codePluginD has one jar file
	//	<fragmentRootDirectory>/codePluginD.jar (with class SampleD).
	codeHelper(registry, "codePluginD", "D", classNamePrefix + "SampleD");

	// codePluginE has one jar file
	//	<fragmentRootDirectory>/bin/codePluginE.jar (with class SampleE).
	codeHelper(registry, "codePluginE", "E", classNamePrefix + "SampleE");

	// codePluginF has two jar files
	//	<fragmentRootDirectory>/codePluginF.jar (with class SampleFFR)
	//	<fragmentRootDirectory>/bin/codePluginF.jar (with class SampleFFB).
	codeHelper(registry, "codePluginF", "F", classNamePrefix + "SampleFFR");
}
}
