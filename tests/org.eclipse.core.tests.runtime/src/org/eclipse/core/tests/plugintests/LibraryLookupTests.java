/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.core.tests.plugintests;

import java.net.URL;
import java.net.URLClassLoader;

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
	// The one in the bin directory should be found first and %key
	// translated to
	// "Test string from pluginC bin directory".

	// DDW - We would like to find the bin version first but
	// there is code in PluginDescriptor.ResourceBundle getResourceBundle(Locale)
	// which deliberately puts the plugin root directory at the head
	// of the classpath?!! FIXME
//	resourceHelper(registry, "pluginc", "C", "Test string from pluginC bin directory");

	// PluginD has one plugin.properties file:
	// 	<fragmentRootDirectory>/plugin.properties
	// This should be found and the "%key" translated to 
	// "Test string from pluginD fragment root directory".
	
	// DDW FIXME
//	resourceHelper(registry, "plugind", "D", "Test string from pluginD fragment root directory");

	// PluginE has one plugin.properties file
	// 	<fragmentRootDirectory>/bin/plugin.properties
	// This should be found and the "%key" translated to 
	// "Test string from pluginE fragment bin directory".
	resourceHelper(registry, "plugine", "E", "Test string from pluginE fragment bin directory");

	// PluginF has two plugin.properties files
	// 	<fragmentRootDirectory>/plugin.properties
	// 	<fragmentRootDirectory>/bin/plugin.properties
	// The one in the bin directory should be found first and %key
	// translated to
	// "Test string from pluginF fragment bin directory".
	resourceHelper(registry, "pluginf", "F", "Test string from pluginF fragment bin directory");

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
	resourceHelper(registry, "plugini", "I", "Test string from pluginI bin directory");

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
	resourceHelper(registry, "pluginn", "N", "Test string from pluginN bin directory");

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

public IPluginDescriptor codeHelper(IPluginRegistry registry, String pluginName, String errorPrefix, String className) {
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
	return plugin;
}

public void testCode() {
	IPluginRegistry registry = InternalPlatform.getPluginRegistry();

	String classNamePrefix = "org.eclipse.core.tests.internal.runtimetests.";
	// codePluginA has one jar file
	//	<pluginRootDirectory>/codePluginA.jar (with class SampleA).
	IPluginDescriptor myPlugin = codeHelper(registry, "codePluginA", "A", classNamePrefix + "SampleA");

	// codePluginB has one jar file
	//	<pluginRootDirectory>/bin/codePluginB.jar (with class SampleB).
	codeHelper(registry, "codePluginB", "B", classNamePrefix + "SampleB");

	// codePluginC has two jar files
	//	<pluginRootDirectory>/codePluginC.jar (with class SampleCPR)
	//	<pluginRootDirectory>/bin/codePluginC.jar (with class SampleCPB).
	codeHelper(registry, "codePluginC", "C", classNamePrefix + "SampleCPB");

	// codePluginD has one jar file
	//	<fragmentRootDirectory>/codePluginD.jar (with class SampleD).
	codeHelper(registry, "codePluginD", "D", classNamePrefix + "SampleD");

	// codePluginE has one jar file
	//	<fragmentRootDirectory>/bin/codePluginE.jar (with class SampleE).
	codeHelper(registry, "codePluginE", "E", classNamePrefix + "SampleE");

	// codePluginF has two jar files
	//	<fragmentRootDirectory>/codePluginF.jar (with class SampleFFR)
	//	<fragmentRootDirectory>/bin/codePluginF.jar (with class SampleFFB).
	codeHelper(registry, "codePluginF", "F", classNamePrefix + "SampleFFB");

	// codePluginG has two jar files
	//	<pluginRootDirectory>/codePluginG.jar (with class SampleGPR)
	//	<fragmentRootDirectory>/codePluginG.jar (with class SampleGFR).
	codeHelper(registry, "codePluginG", "G", classNamePrefix + "SampleGPR");

	// codePluginH has two jar files
	//	<pluginRootDirectory>/codePluginH.jar (with class SampleGHPR)
	//	<fragmentRootDirectory>/bin/codePluginH.jar (with class SampleHFB).
	codeHelper(registry, "codePluginH", "H", classNamePrefix + "SampleHFB");

	// codePluginI has two jar files
	//	<pluginRootDirectory>/bin/codePluginI.jar (with class SampleIPB)
	//	<fragmentRootDirectory>/codePluginI.jar (with class SampleIFR).
	codeHelper(registry, "codePluginI", "I", classNamePrefix + "SampleIPB");

	// codePluginJ has two jar files
	//	<pluginRootDirectory>/bin/codePluginJ.jar (with class SampleJPB)
	//	<fragmentRootDirectory>/bin/codePluginJ.jar (with class SampleJFB).
	codeHelper(registry, "codePluginJ", "J", classNamePrefix + "SampleJPB");

	// codePluginK has three jar files
	//	<pluginRootDirectory>/codePluginK.jar (with class SampleKPR)
	//	<pluginRootDirectory>/bin/codePluginK.jar (with class SampleKPB)
	//	<fragmentRootDirectory>/codePluginK.jar (with class SampleKFR).
	codeHelper(registry, "codePluginK", "K", classNamePrefix + "SampleKPB");

	// codePluginL has three jar files
	//	<pluginRootDirectory>/codePluginL.jar (with class SampleLPR)
	//	<pluginRootDirectory>/bin/codePluginL.jar (with class SampleLPB)
	//	<fragmentRootDirectory>/bin/codePluginL.jar (with class SampleLFB).
	codeHelper(registry, "codePluginL", "L", classNamePrefix + "SampleLPB");

	// codePluginM has three jar files
	//	<pluginRootDirectory>/codePluginM.jar (with class SampleMPR)
	//	<fragmentRootDirectory>/codePluginM.jar (with class SampleMFR).
	//	<fragmentRootDirectory>/bin/codePluginM.jar (with class SampleMFB).
	codeHelper(registry, "codePluginM", "M", classNamePrefix + "SampleMFB");

	// codePluginN has three jar files
	//	<pluginRootDirectory>/bin/codePluginN.jar (with class SampleNPB)
	//	<fragmentRootDirectory>/codePluginN.jar (with class SampleNFR).
	//	<fragmentRootDirectory>/bin/codePluginN.jar (with class SampleNFB).
	codeHelper(registry, "codePluginN", "N", classNamePrefix + "SampleNPB");

	// codePluginO has four jar files
	//	<pluginRootDirectory>/codePluginO.jar (with class SampleOPR)
	//	<pluginRootDirectory>/bin/codePluginO.jar (with class SampleOPB)
	//	<fragmentRootDirectory>/codePluginO.jar (with class SampleOFR).
	//	<fragmentRootDirectory>/bin/codePluginO.jar (with class SampleOFB).
	codeHelper(registry, "codePluginO", "O", classNamePrefix + "SampleOPB");
}

public void test3093 () {
	/* A plugin/fragment entry like 
	 * 		library name="$nl$/"
	 * should result in a file: url added on the classpath.
	 * The bug indicated that a jar: url was being added.
	 */
	IPluginRegistry registry = InternalPlatform.getPluginRegistry();
	IPluginDescriptor plugin = registry.getPluginDescriptor("test3093PluginA");
	assertNotNull("1.0", plugin);
	// Activate this plugin
	Plugin active = null;
	try {
		active = plugin.getPlugin();
	} catch (CoreException ce) {
		fail("1.1 Core exception encountered.",ce);
	}
	URL[] cp = ((URLClassLoader)plugin.getPluginClassLoader()).getURLs();
	assertTrue("1.2 One URL on class path", cp.length == 1);
	String urlString = cp[0].toString();
	// Make sure we have the right protocol (not jar: but file:)
	assertTrue("1.3 Right protocol", urlString.indexOf("file:") != -1);
	// Make sure we really picked up the right directory
	assertTrue("1.4 Contains nl directory", urlString.endsWith("org.eclipse.core.tests.runtime/Plugintests_Testing/Bug3093/plugins/pluginA/nl/en/CA/"));
}
}
