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
public class ConflictResolveTest_7 extends PluginResolveTest {
public ConflictResolveTest_7() {
	super(null);
}
public ConflictResolveTest_7(String name) {
	super(name);
}
public void baseTest() {
	
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("conflictResolveTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/conflictResolve.7/");
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
	assertEquals("0.0", pluginDescriptors.length, 6);
	
	// look for tests.a
	pluginDescriptors = registry.getPluginDescriptors("tests.a");
	assertEquals("1.0", pluginDescriptors.length, 1);
	assertEquals("1.1", pluginDescriptors[0].getUniqueIdentifier(), "tests.a");
	assertEquals("1.2", pluginDescriptors[0].getVersionIdentifier(), new PluginVersionIdentifier("1.2.0"));
	
	// look for tests.b
	pluginDescriptors = registry.getPluginDescriptors("tests.b");
	assertEquals("2.0", pluginDescriptors.length, 1);
	assertEquals("2.1", pluginDescriptors[0].getUniqueIdentifier(), "tests.b");
	assertEquals("2.2", pluginDescriptors[0].getVersionIdentifier(), new PluginVersionIdentifier("2.1.0"));
	
	// look for tests.c
	pluginDescriptors = registry.getPluginDescriptors("tests.c");
	assertEquals("3.0", pluginDescriptors.length, 1);
	assertEquals("3.1", pluginDescriptors[0].getUniqueIdentifier(), "tests.c");
	assertEquals("3.2", pluginDescriptors[0].getVersionIdentifier(), new PluginVersionIdentifier("2.1.0"));

	// look for tests.d
	pluginDescriptors = registry.getPluginDescriptors("tests.d");
	assertEquals("4.0", pluginDescriptors.length, 1);
	assertEquals("4.1", pluginDescriptors[0].getUniqueIdentifier(), "tests.d");
	assertEquals("4.2", pluginDescriptors[0].getVersionIdentifier(), new PluginVersionIdentifier("1.1.0"));

	// look for tests.e
	pluginDescriptors = registry.getPluginDescriptors("tests.e");
	assertEquals("5.0", pluginDescriptors.length, 1);
	assertEquals("5.1", pluginDescriptors[0].getUniqueIdentifier(), "tests.e");
	assertEquals("5.2", pluginDescriptors[0].getVersionIdentifier(), new PluginVersionIdentifier("1.1.0"));

	// look for tests.f
	pluginDescriptors = registry.getPluginDescriptors("tests.f");
	assertEquals("6.0", pluginDescriptors.length, 1);
	assertEquals("6.1", pluginDescriptors[0].getUniqueIdentifier(), "tests.f");
	assertEquals("6.2", pluginDescriptors[0].getVersionIdentifier(), new PluginVersionIdentifier("1.2.0"));
}
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new ConflictResolveTest_7("baseTest"));
	return suite;
}
}
