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
import org.eclipse.core.tests.harness.*;
/**
 */
public class CircularTest extends EclipseWorkspaceTest {

public CircularTest() {
	super(null);
}
public CircularTest(String name) {
	super(name);
}
public void fullCircle() {
	
	// Test a situation where there is a circular dependency that
	// includes the root plugin

	String errorMessage = "Unable to resolve plug-in registry.";
		
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "circularTestProblems", null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String[] pluginPath = new String[2];
	pluginPath[0] = tempPlugin.getLocation().concat("Plugin_Testing/circular/tests.a.xml");
	pluginPath[1] = tempPlugin.getLocation().concat("Plugin_Testing/circular/tests.b.xml");
	URL pluginURLs[] = new URL[2];
	URL pURL = null;
	for (int i = 0; i < pluginPath.length; i++) {
		try {
			pURL = new URL (pluginPath[i]);
		} catch (java.net.MalformedURLException e) {
			assertTrue("Bad URL for " + pluginPath[i], true);
		}
		pluginURLs[i] = pURL;
		pURL = null;
	}
	IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, true);

	IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors();
	assertEquals("1.0", pluginDescriptors.length, 0);
	// Now make sure we got some error reporting here
	assertTrue("1.1 Got the right errors", problems.toString().indexOf(errorMessage) != -1);
}

public void partCircle() {
	
	// Test a situation where there is a circular dependency
	// just below the root plugin (so the root is not part
	// of the circle).
	
	String errorMessage = "Detected prerequisite loop from tests.a to tests.b.";

	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "circularTestProblems", null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String[] pluginPath = new String[3];
	pluginPath[0] = tempPlugin.getLocation().concat("Plugin_Testing/circular/tests.a.xml");
	pluginPath[1] = tempPlugin.getLocation().concat("Plugin_Testing/circular/tests.b.xml");
	pluginPath[2] = tempPlugin.getLocation().concat("Plugin_Testing/circular/tests.top.xml");
	URL pluginURLs[] = new URL[3];
	URL pURL = null;
	for (int i = 0; i < pluginPath.length; i++) {
		try {
			pURL = new URL (pluginPath[i]);
		} catch (java.net.MalformedURLException e) {
			assertTrue("Bad URL for " + pluginPath[i], true);
		}
		pluginURLs[i] = pURL;
		pURL = null;
	}
	IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, true);

	IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors();
	assertEquals("2.0", pluginDescriptors.length, 0);
	assertTrue("2.1 Got the right errors", problems.toString().indexOf(errorMessage) != -1);
}
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new CircularTest("fullCircle"));
	suite.addTest(new CircularTest("partCircle"));
	return suite;
}
}


