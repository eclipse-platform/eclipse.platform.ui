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

import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.WorkspaceSessionTest;

public class JarSearchTests extends WorkspaceSessionTest {
	
public JarSearchTests() {
	super(null);
}
public JarSearchTests(String name) {
	super(name);
}
public void test1() {
	IPluginRegistry registry = Platform.getPluginRegistry();
	IPluginDescriptor[] plugins = registry.getPluginDescriptors("JarLookupTest1");
	assertTrue("1.0 Only one jarLookup1 plugin", plugins.length == 1);
	System.out.println("jarLookup1 plugin name = " + plugins[0].getLabel());
	assertTrue("1.1 Plugin name translated", plugins[0].getLabel().equals("Jar Lookup Test 1"));

	plugins = registry.getPluginDescriptors("JarLookupTest2");
	assertTrue("2.0 Only one jarLookup2 plugin", plugins.length == 1);
	System.out.println("jarLookup2 plugin name = " + plugins[0].getLabel());
	assertTrue("2.1 Plugin name translated", plugins[0].getLabel().equals("Jar Lookup Test 2"));

	plugins = registry.getPluginDescriptors("JarLookupTest3");
	assertTrue("3.0 Only one jarLookup3 plugin", plugins.length == 1);
	System.out.println("jarLookup3 plugin name = " + plugins[0].getLabel());
	assertTrue("3.1 Plugin name translated", plugins[0].getLabel().equals("%pluginName"));

	plugins = registry.getPluginDescriptors("JarLookupTest4");
	assertTrue("4.0 Only one jarLookup4 plugin", plugins.length == 1);
	System.out.println("jarLookup4 plugin name = " + plugins[0].getLabel());
	assertTrue("4.1 Plugin name translated", plugins[0].getLabel().equals("Jar Lookup Test 4"));
}

}
