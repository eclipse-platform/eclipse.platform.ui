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
public class SoftPrereqTest_21 extends PluginResolveTest {
public SoftPrereqTest_21() {
	super(null);
}
public SoftPrereqTest_21(String name) {
	super(name);
}
public void baseTest() {
	
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("softPrerequisiteTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/softPrereq.21/");
	URL pluginURLs[] = new URL[1];
	URL pURL = null;
	try {
		pURL = new URL (pluginPath);
	} catch (java.net.MalformedURLException e) {
		assertTrue("Bad URL for " + pluginPath, true);
	}
	pluginURLs[0] = pURL;
	IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, true);

	IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors();
	assertEquals("0.0", pluginDescriptors.length, 4);
	
	// Should be identical to softPrereq.7 as the only difference is the order of the 
	// requires.
	
	// look for tests.a
	pluginDescriptors = registry.getPluginDescriptors("tests.a");
	assertEquals("1.0", pluginDescriptors.length, 1);
	assertEquals("1.1", pluginDescriptors[0].getUniqueIdentifier(), "tests.a");
	assertEquals("1.2", pluginDescriptors[0].getVersionIdentifier(), new PluginVersionIdentifier("1.2.0"));
	
	// make sure it also has an optional prerequisite
	IPluginPrerequisite[] requires = pluginDescriptors[0].getPluginPrerequisites();
	assertEquals("2.0", requires.length, 2);
	IPluginPrerequisite requireB = null;
	IPluginPrerequisite requireC = null;
	for (int i = 0; i < requires.length; i++) {
		if (requires[i].getUniqueIdentifier().equals("tests.b"))
			requireB = requires[i];
		else if (requires[i].getUniqueIdentifier().equals("tests.c"))
			requireC = requires[i];
	}
	assertNotNull("2.1", requireB);
	assertNotNull("2.2", requireC);
	
	// Now check the data in the prerequisites
	assertTrue("3.0", requireB.isMatchedAsCompatible());
	assertTrue("3.1", !requireB.isMatchedAsExact());
	assertTrue("3.2", requireB.isOptional());
	assertEquals("3.3", requireB.getVersionIdentifier(), new PluginVersionIdentifier("1.1.0"));
	assertTrue("3.4", requireC.isMatchedAsCompatible());
	assertTrue("3.5", !requireC.isMatchedAsExact());
	assertTrue("3.6", !requireC.isOptional());
	assertEquals("3.7", requireC.getVersionIdentifier(), new PluginVersionIdentifier("2.1.0"));
	
	// look for tests.b - there should be 1
	pluginDescriptors = registry.getPluginDescriptors("tests.b");
	assertEquals("4.0", pluginDescriptors.length, 1);
	assertEquals("4.1", pluginDescriptors[0].getUniqueIdentifier(), "tests.b");
	assertEquals("4.2", pluginDescriptors[0].getVersionIdentifier(), new PluginVersionIdentifier("1.1.0"));
	
	// look for tests.c - there should be 2
	pluginDescriptors = registry.getPluginDescriptors("tests.c");
	assertEquals("5.0", pluginDescriptors.length, 2);
	IPluginDescriptor pluginC_110 = null;
	IPluginDescriptor pluginC_210 = null;
	for (int i = 0; i < pluginDescriptors.length; i++) {
		boolean badVersion = false;
		IPluginDescriptor thisPlugin = pluginDescriptors[i];
		if (thisPlugin.getVersionIdentifier().equals(new PluginVersionIdentifier("1.1.0"))) {
			pluginC_110 = thisPlugin;
		} else if (thisPlugin.getVersionIdentifier().equals(new PluginVersionIdentifier("2.1.0"))) {
			pluginC_210 = thisPlugin;
		} else {
			assertTrue ("5.1 Bad version number found" + thisPlugin.getVersionIdentifier().toString(), false);
			badVersion = true;
		}
		// Check the common components of these plugins
		if (thisPlugin != null && !badVersion) {
			assertEquals("5.2", thisPlugin.getUniqueIdentifier(), "tests.c");
		}
	}
	assertNotNull("5.3", pluginC_110);
	assertNotNull("5.4", pluginC_210);
}
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new SoftPrereqTest_21("baseTest"));
	return suite;
}
}
