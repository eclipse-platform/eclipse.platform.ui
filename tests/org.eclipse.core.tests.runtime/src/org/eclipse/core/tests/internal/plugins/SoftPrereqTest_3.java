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
public class SoftPrereqTest_3 extends PluginResolveTest {
public SoftPrereqTest_3() {
	super(null);
}
public SoftPrereqTest_3(String name) {
	super(name);
}
public void baseTest() {
	
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("softPrerequisiteTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/softPrereq.3/");
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
	assertEquals("0.0", pluginDescriptors.length, 2);
	
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
	assertTrue("3.0", !requireB.isMatchedAsPerfect());
	assertTrue("3.1", !requireB.isMatchedAsEquivalent());
	assertTrue("3.2", !requireB.isMatchedAsCompatible());
	assertTrue("3.3", !requireB.isMatchedAsGreaterOrEqual());
	assertTrue("3.4", requireB.isOptional());
	assertNull("3.5", requireB.getVersionIdentifier());
	assertTrue("3.6", !requireC.isMatchedAsPerfect());
	assertTrue("3.7", !requireC.isMatchedAsEquivalent());
	assertTrue("3.8", !requireC.isMatchedAsCompatible());
	assertTrue("3.9", !requireC.isMatchedAsGreaterOrEqual());
	assertTrue("3.10", !requireC.isOptional());
	assertNull("3.11", requireC.getVersionIdentifier());
	
	// look for tests.b - shouldn't be found
	pluginDescriptors = registry.getPluginDescriptors("tests.b");
	assertEquals("4.0", pluginDescriptors.length, 0);
	
	// look for tests.c - should exist
	pluginDescriptors = registry.getPluginDescriptors("tests.c");
	assertEquals("5.0", pluginDescriptors.length, 1);
	assertEquals("5.1", pluginDescriptors[0].getUniqueIdentifier(), "tests.c");
	assertEquals("5.2", pluginDescriptors[0].getVersionIdentifier(), new PluginVersionIdentifier("2.0.0"));
}
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new SoftPrereqTest_3("baseTest"));
	return suite;
}
}
