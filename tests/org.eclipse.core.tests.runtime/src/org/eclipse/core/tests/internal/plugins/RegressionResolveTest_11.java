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
 * Test for bug 19397. Description: If an imported plugin is specified as
 * optional in the plugin XML, the versioning information is ignored.
 * 
 * Scenario: pluginA requires pluginC (any version) and optionally 
 * pluginB_2.0.0, but pluginB's version is 1.0.0 (which by its turn
 * requires pluginD). Expected result: all plug- ins are enabled AND pluginA's
 * classloader does not import pluginB's classloader.
 */
public class RegressionResolveTest_11 extends PluginResolveTest {
	public RegressionResolveTest_11(String name) {
		super(name);
	}
	public void testBug() {
		MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
		InternalFactory factory = new InternalFactory(problems);
		PluginDescriptor tempPlugin = (PluginDescriptor) Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
		String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/regression.resolve.11/");
		URL pluginURLs[] = new URL[1];
		URL pURL = null;
		try {
			pURL = new URL(pluginPath);
		} catch (java.net.MalformedURLException e) {
			assertTrue("Bad URL for " + pluginPath, true);
		}
		pluginURLs[0] = pURL;
		IPluginRegistry registry = ParseHelper.doParsing(factory, pluginURLs, true);

		if (factory.getStatus().getSeverity() != IStatus.OK) {
			System.out.println(factory.getStatus());
			fail("0.0 - " + factory.getStatus().getMessage());
		}

		IPluginDescriptor pluginA = registry.getPluginDescriptor("tests.a");
		IPluginDescriptor pluginB = registry.getPluginDescriptor("tests.b", new PluginVersionIdentifier("1.0.0"));
		IPluginDescriptor pluginC = registry.getPluginDescriptor("tests.c");
		IPluginDescriptor pluginD = registry.getPluginDescriptor("tests.d");

		// all plugins should have been enabled
		assertNotNull("1.1", pluginA);
		assertNotNull("1.2", pluginB);
		assertNotNull("1.4", pluginC);
		assertNotNull("1.5", pluginD);

		// plugin A requires optionally plugin B_2.0.0		
		IPluginPrerequisite[] requires = pluginA.getPluginPrerequisites();
		assertEquals("2.1", 2, requires.length);
		assertEquals("2.2", pluginB.getUniqueIdentifier(), requires[0].getUniqueIdentifier());
		assertEquals("2.3", new PluginVersionIdentifier(2, 0, 0), requires[0].getVersionIdentifier());
		// should be null because it was not resolved (pluginB's version is 1.0.0) 
		assertNull("2.4", requires[0].getResolvedVersionIdentifier());

		// check the resolved prerequisites for A
		IPluginPrerequisite[] resolvedPrereqsA = ((PluginDescriptor) pluginA).getPluginResolvedPrerequisites();
		assertEquals("3.1", 1, resolvedPrereqsA.length);
		assertEquals("3.2", pluginC.getUniqueIdentifier(), resolvedPrereqsA[0].getUniqueIdentifier());

		// check the resolved prerequisites for B
		IPluginPrerequisite[] resolvedPrereqsB = ((PluginDescriptor) pluginB).getPluginResolvedPrerequisites();
		assertEquals("4.1", 1, resolvedPrereqsB.length);
		assertEquals("4.2", pluginD.getUniqueIdentifier(), resolvedPrereqsB[0].getUniqueIdentifier());

	}
	public static Test suite() {
		return new TestSuite(RegressionResolveTest_11.class);
	}
}
