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
import org.eclipse.core.runtime.model.PluginPrerequisiteModel;
import org.eclipse.core.internal.plugins.InternalFactory;
import org.eclipse.core.internal.plugins.PluginDescriptor;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.Policy;
/**
 */
public class RegressionResolveTest_4 extends PluginResolveTest {
public RegressionResolveTest_4() {
	super(null);
}
public RegressionResolveTest_4(String name) {
	super(name);
}
public void baseTest() {
	
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/regression.resolve.4/");
	URL pluginURLs[] = new URL[1];
	URL pURL = null;
	try {
		pURL = new URL (pluginPath);
	} catch (java.net.MalformedURLException e) {
		assertTrue("Bad URL for " + pluginPath, true);
	}
	pluginURLs[0] = pURL;
	IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, true);

	IPluginDescriptor[] plugins = registry.getPluginDescriptors();
	assertEquals("0.0", plugins.length, 2);
	
	IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors("tests.a");
	assertEquals("1.0", pluginDescriptors.length, 1);
	assertEquals("1.1", pluginDescriptors[0].getUniqueIdentifier(), "tests.a");
	assertEquals("1.2", pluginDescriptors[0].getVersionIdentifier(), new PluginVersionIdentifier("1.1.0"));
	
	// Grab the prerequisite and examine it later
	IPluginPrerequisite[] requires = pluginDescriptors[0].getPluginPrerequisites();

	pluginDescriptors = registry.getPluginDescriptors("tests.b");
	assertEquals("2.0", pluginDescriptors.length, 1);
	assertEquals("2.1", pluginDescriptors[0].getUniqueIdentifier(), "tests.b");
	assertEquals("2.2", pluginDescriptors[0].getVersionIdentifier(), new PluginVersionIdentifier("2.2.0"));
	
	// Now check the prerequisite and the return values of getMatch()
	assertEquals("3.0", requires.length, 1);
	assertTrue("3.1", !requires[0].isMatchedAsExact());
	assertTrue("3.2", requires[0].isMatchedAsCompatible());
	PluginPrerequisiteModel req = (PluginPrerequisiteModel)requires[0];
	assertTrue("3.3", req.getMatch() == false);
	assertTrue("3.4", !(req.getMatch()) == true);
	
}
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new RegressionResolveTest_4("baseTest"));
	return suite;
}
}
