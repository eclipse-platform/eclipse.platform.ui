/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.plugins;

import java.net.URL;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.plugins.InternalFactory;
import org.eclipse.core.internal.plugins.PluginDescriptor;
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.runtime.*;
/**
 */
public class SoftPrereqTest_19 extends PluginResolveTest {
public SoftPrereqTest_19() {
	super(null);
}
public SoftPrereqTest_19(String name) {
	super(name);
}
public void baseTest() {
	
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("softPrerequisiteTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/softPrereq.19/");
	URL pluginURLs[] = new URL[1];
	URL pURL = null;
	try {
		pURL = new URL (pluginPath);
	} catch (java.net.MalformedURLException e) {
		assertTrue("Bad URL for " + pluginPath, true);
	}
	pluginURLs[0] = pURL;
	IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, true);

	// Not quite identical to softPrereq.5 as a failed requirement occurs before
	// the optional one so there will be no plugins (the optional one does not get
	// considered as a new root node).
	
	IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors();
	assertEquals("1.0", 1, pluginDescriptors.length);
	assertEquals("1.1", "tests.b", pluginDescriptors[0].getUniqueIdentifier());
	assertEquals("1.2", new PluginVersionIdentifier("1.0.1"), pluginDescriptors[0].getVersionIdentifier());
}
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new SoftPrereqTest_19("baseTest"));
	return suite;
}
}
