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

public class PluginResolveTest_34 extends PluginResolveTest {
	public PluginResolveTest_34() {
		super(null);
	}
	public PluginResolveTest_34(String name) {
		super(name);
	}

	/**
	 * Registry:
	 * a_1.0.0 -> b_2.0.0
	 * b_2.0.0 -> c_2.0.0
	 * b_1.0.0 -> c_1.0.0 
	 * c_1.0.0
	 * 
	 * After resolution:
	 * b_1.0.0 -> c_1.0.0
	 * c_1.0.0
	 */
	public void testResolve() {
		MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
		InternalFactory factory = new InternalFactory(problems);
		PluginDescriptor tempPlugin = (PluginDescriptor) Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
		String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/plugins.resolve.34/");
		URL pluginURLs[] = new URL[1];
		URL pURL = null;
		try {
			pURL = new URL(pluginPath);
		} catch (java.net.MalformedURLException e) {
			assertTrue("Bad URL for " + pluginPath, true);
		}
		pluginURLs[0] = pURL;
		IPluginRegistry registry = ParseHelper.doParsing(factory, pluginURLs, true);

		IPluginDescriptor[] plugins = registry.getPluginDescriptors();
		assertEquals("1.0", 2, plugins.length);

		plugins = registry.getPluginDescriptors("tests.b");
		assertEquals("2.0", 1, plugins.length);
		assertEquals("2.1", plugins[0].getVersionIdentifier().toString(), "1.0.0");

		plugins = registry.getPluginDescriptors("tests.c");
		assertEquals("3.0", 1, plugins.length);
		assertEquals("3.1", plugins[0].getVersionIdentifier().toString(), "1.0.0");

	}
	public static Test suite() {
		return new TestSuite(PluginResolveTest_34.class);
	}
}
