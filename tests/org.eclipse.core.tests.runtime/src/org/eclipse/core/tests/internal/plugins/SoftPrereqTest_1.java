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
public class SoftPrereqTest_1 extends PluginResolveTest {
public SoftPrereqTest_1() {
	super(null);
}
public SoftPrereqTest_1(String name) {
	super(name);
}
public void baseTest() {
	
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("softPrerequisiteTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/softPrereq.1/");
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
	assertEquals("0.0", pluginDescriptors.length, 1);
	assertEquals("0.1", pluginDescriptors[0].getUniqueIdentifier(), "tests.a");
	assertEquals("0.2", pluginDescriptors[0].getVersionIdentifier(), new PluginVersionIdentifier("1.2.0"));
	
	// make sure it also has an optional prerequisite
	IPluginPrerequisite[] requires = pluginDescriptors[0].getPluginPrerequisites();
	assertEquals("1.0", requires.length, 1);
	assertEquals("1.1", requires[0].getUniqueIdentifier(), "tests.b");
	assertTrue("1.2", !requires[0].isMatchedAsPerfect());
	assertTrue("1.3", !requires[0].isMatchedAsEquivalent());
	assertTrue("1.4", !requires[0].isMatchedAsCompatible());
	assertTrue("1.5", !requires[0].isMatchedAsGreaterOrEqual());
	assertTrue("1.6", requires[0].isOptional());
}
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new SoftPrereqTest_1("baseTest"));
	return suite;
}
}
