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
import org.eclipse.core.runtime.model.*;
import org.eclipse.core.internal.plugins.InternalFactory;
import org.eclipse.core.internal.plugins.*;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.tests.harness.*;

public class OrphanTest extends EclipseWorkspaceTest {
public OrphanTest() {
	super(null);
}
public OrphanTest(String name) {
	super(name);
}
	// Comment out test1, 2 and 3 until bug 21465 - 
	// "Plugin Resolving - reorder REQUIRES clause & different
	// plugins are disabled" is fixed.

public void test1() {
	/*  Plugin structure is as follows:
	 * 
	 *     testa requires testb
	 *     testa requires testc
	 * 
	 *     testb requires testd
	 * 
	 *     testc requires teste
	 * 
	 *     testd has no prerequisites but doesn't have all the
	 *        required fields and will, therefore, be disabled
	 * 
	 *     teste has no prerequisites
	 * 
	 * The resulting plugin registry should contain only testc
	 * and teste.  All other plugins should be disabled (and
	 * trimmed).
	 */
/*	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "fragmentResolveTest", null);
	InternalFactory factory = new InternalFactory(problems);
	URL pluginURLs[] = new URL[1];
	try {
		PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
		String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/orphan.1/");
		pluginURLs[0] = new URL (pluginPath);
	} catch (java.net.MalformedURLException e) {
		fail("1.0 Unexpected exception - " + e.getMessage());
	}
	IPluginRegistry registry = ParseHelper.doParsing(factory, pluginURLs, true);
	
	IPluginDescriptor[] pds = registry.getPluginDescriptors();
	// Should only be 2 plugins
	assertEquals("1.1 Should only have 2 plugins", 2, pds.length);
	// Should have a plugin testc and another called teste
	IPluginDescriptor pd = registry.getPluginDescriptor("testc");
	assertNotNull("1.2 Should find plugin testc", pd);
	pd = registry.getPluginDescriptor("teste");
	assertNotNull("1.3 Should find plugin teste", pd);*/
}

public void test2() {
	/*  Plugin structure is as follows:
	 * 
	 *     testa requires testb
	 *     testa requires testc
	 *     testa requires testd
	 * 
	 *     testb requires teste
	 *     testb requires testf
	 *     testb requires testg
	 * 
	 *     testc requires testh
	 *     testc requires testi
	 *     testc requires testj
	 * 
	 *     testd requires testk
	 *     testd requires testl
	 *     testd requires testm
	 * 
	 *     teste has no prerequisites
	 * 
	 *     testf has no prerequisites
	 * 
	 *     testg has no prerequisites
	 * 
	 *     testh has no prerequisites
	 * 
	 *     testi has no prerequisites and is missing a required
	 *        field so it will be disabled
	 * 
	 *     testj has no prerequisites
	 * 
	 *     testk has no prerequisites
	 * 
	 *     testl has no prerequisites
	 * 
	 *     testm has no prerequisites
	 * 
	 * The resulting plugin registry should contain the following
	 * plugins:
	 *    testb
	 *    testd
	 *    teste
	 *    testf
	 *    testg
	 *    testh
	 *    testj
	 *    testk
	 *    testl
	 *    testm
	 * All other plugins (a, c, and i) should be disabled
	 * (and trimmed).
	 */
/*	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "fragmentResolveTest", null);
	InternalFactory factory = new InternalFactory(problems);
	URL pluginURLs[] = new URL[1];
	try {
		PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
		String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/orphan.2/");
		pluginURLs[0] = new URL (pluginPath);
	} catch (java.net.MalformedURLException e) {
		fail("2.0 Unexpected exception - " + e.getMessage());
	}
	IPluginRegistry registry = ParseHelper.doParsing(factory, pluginURLs, true);
	IPluginDescriptor[] pds = registry.getPluginDescriptors();
	// Should only be 10 plugins
	assertEquals("2.1 Should only have 10 plugins", 10, pds.length);
	// Should have a plugins as listed in comment above
	IPluginDescriptor pd = registry.getPluginDescriptor("testb");
	assertNotNull("2.2 Should find plugin testb", pd);
	pd = registry.getPluginDescriptor("testd");
	assertNotNull("2.3 Should find plugin testd", pd);
	pd = registry.getPluginDescriptor("teste");
	assertNotNull("2.4 Should find plugin teste", pd);
	pd = registry.getPluginDescriptor("testf");
	assertNotNull("2.5 Should find plugin testf", pd);
	pd = registry.getPluginDescriptor("testg");
	assertNotNull("2.6 Should find plugin testg", pd);
	pd = registry.getPluginDescriptor("testh");
	assertNotNull("2.6 Should find plugin testh", pd);
	pd = registry.getPluginDescriptor("testj");
	assertNotNull("2.6 Should find plugin testj", pd);
	pd = registry.getPluginDescriptor("testk");
	assertNotNull("2.7 Should find plugin testk", pd);
	pd = registry.getPluginDescriptor("testl");
	assertNotNull("2.8 Should find plugin testl", pd);
	pd = registry.getPluginDescriptor("testm");
	assertNotNull("2.9 Should find plugin testm", pd);*/
}

public void test3() {
	/*  Plugin structure is as follows:
	 * 
	 *     There are 2 root plugins - testa and testa2
	 * 
	 *     testa requires testb
	 *     testa requires testc
	 *     testa requires testd
	 * 
	 *     testb requires teste
	 *     testb requires testf
	 *     testb requires testg
	 * 
	 *     testc requires testh
	 *     testc requires testi
	 *     testc requires testj
	 * 
	 *     testd has no prerequisites
	 * 
	 *     teste has no prerequisites
	 * 
	 *     testf has no prerequisites
	 * 
	 *     testg has no prerequisites
	 * 
	 *     testh has no prerequisites
	 * 
	 *     testi has no prerequisites and is missing a required
	 *        field so it will be disabled
	 * 
	 *     testj has no prerequisites
	 * 
	 *     testa2 requires testb
	 *     testa2 requires testd
	 * 
	 * The resulting plugin registry should contain the following
	 * plugins:
	 *    testb
	 *    testd
	 *    teste
	 *    testf
	 *    testg
	 *    testh
	 *    testj
	 *    testa2
	 * All other plugins (a, c, and i) should be disabled
	 * (and trimmed).
	 */
/*	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "fragmentResolveTest", null);
	InternalFactory factory = new InternalFactory(problems);
	URL pluginURLs[] = new URL[1];
	try {
		PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
		String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/orphan.3/");
		pluginURLs[0] = new URL (pluginPath);
	} catch (java.net.MalformedURLException e) {
		fail("3.0 Unexpected exception - " + e.getMessage());
	}
	IPluginRegistry registry = ParseHelper.doParsing(factory, pluginURLs, true);
	IPluginDescriptor[] pds = registry.getPluginDescriptors();
	// Should only be 8 plugins
	assertEquals("3.1 Should only have 8 plugins", 8, pds.length);
	// Should have a plugins as listed in comment above
	IPluginDescriptor pd = registry.getPluginDescriptor("testb");
	assertNotNull("3.2 Should find plugin testb", pd);
	pd = registry.getPluginDescriptor("testd");
	assertNotNull("3.3 Should find plugin testd", pd);
	pd = registry.getPluginDescriptor("teste");
	assertNotNull("3.4 Should find plugin teste", pd);
	pd = registry.getPluginDescriptor("testf");
	assertNotNull("3.5 Should find plugin testf", pd);
	pd = registry.getPluginDescriptor("testg");
	assertNotNull("3.6 Should find plugin testg", pd);
	pd = registry.getPluginDescriptor("testh");
	assertNotNull("3.6 Should find plugin testh", pd);
	pd = registry.getPluginDescriptor("testj");
	assertNotNull("3.6 Should find plugin testj", pd);
	pd = registry.getPluginDescriptor("testa2");
	assertNotNull("3.7 Should find plugin testa2", pd);*/
}

public void test4() {
	/*  Plugin structure is as follows:
	 * 
	 *     testa requires testb
	 *     testa requires testc
	 * 
	 *     testb requires testd
	 * 
	 *     testc requires teste
	 * 
	 *     testd has no prerequisites but doesn't have all the
	 *        required fields and will, therefore, be disabled
	 * 
	 *     teste has no prerequisites
	 * 
	 * The resulting plugin registry should contain only testc
	 * and teste.  All other plugins should be disabled (and
	 * trimmed).
	 * 
	 * This is the same as test1 but the order of the requires
	 * in the plugin is such that any disabled plugins are at the
	 * end of the requires clause.  Yes, this does make a
	 * difference.  See bug 21465 - "Plugin Resolving - reorder
	 * REQUIRES clause & different plugins are disabled".
	 */
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "fragmentResolveTest", null);
	InternalFactory factory = new InternalFactory(problems);
	URL pluginURLs[] = new URL[1];
	try {
		PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
		String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/orphan.4/");
		pluginURLs[0] = new URL (pluginPath);
	} catch (java.net.MalformedURLException e) {
		fail("4.0 Unexpected exception - " + e.getMessage());
	}
	IPluginRegistry registry = ParseHelper.doParsing(factory, pluginURLs, true);
	
	IPluginDescriptor[] pds = registry.getPluginDescriptors();
	// Should only be 2 plugins
	assertEquals("4.1 Should only have 2 plugins", 2, pds.length);
	// Should have a plugin testc and another called teste
	IPluginDescriptor pd = registry.getPluginDescriptor("testc");
	assertNotNull("4.2 Should find plugin testc", pd);
	pd = registry.getPluginDescriptor("teste");
	assertNotNull("4.3 Should find plugin teste", pd);
}

public void test5() {
	/*  Plugin structure is as follows:
	 * 
	 *     testa requires testb
	 *     testa requires testc
	 *     testa requires testd
	 * 
	 *     testb requires teste
	 *     testb requires testf
	 *     testb requires testg
	 * 
	 *     testc requires testh
	 *     testc requires testi
	 *     testc requires testj
	 * 
	 *     testd requires testk
	 *     testd requires testl
	 *     testd requires testm
	 * 
	 *     teste has no prerequisites
	 * 
	 *     testf has no prerequisites
	 * 
	 *     testg has no prerequisites
	 * 
	 *     testh has no prerequisites
	 * 
	 *     testi has no prerequisites and is missing a required
	 *        field so it will be disabled
	 * 
	 *     testj has no prerequisites
	 * 
	 *     testk has no prerequisites
	 * 
	 *     testl has no prerequisites
	 * 
	 *     testm has no prerequisites
	 * 
	 * The resulting plugin registry should contain the following
	 * plugins:
	 *    testb
	 *    testd
	 *    teste
	 *    testf
	 *    testg
	 *    testh
	 *    testj
	 *    testk
	 *    testl
	 *    testm
	 * All other plugins (a, c, and i) should be disabled
	 * (and trimmed).
	 * 
	 * This is the same as test2 but the order of the requires
	 * in the plugin is such that any disabled plugins are at the
	 * end of the requires clause.  Yes, this does make a
	 * difference.  See bug 21465 - "Plugin Resolving - reorder
	 * REQUIRES clause & different plugins are disabled".
	 */
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "fragmentResolveTest", null);
	InternalFactory factory = new InternalFactory(problems);
	URL pluginURLs[] = new URL[1];
	try {
		PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
		String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/orphan.5/");
		pluginURLs[0] = new URL (pluginPath);
	} catch (java.net.MalformedURLException e) {
		fail("5.0 Unexpected exception - " + e.getMessage());
	}
	IPluginRegistry registry = ParseHelper.doParsing(factory, pluginURLs, true);
	IPluginDescriptor[] pds = registry.getPluginDescriptors();
	// Should only be 10 plugins
	assertEquals("5.1 Should only have 10 plugins", 10, pds.length);
	// Should have a plugins as listed in comment above
	IPluginDescriptor pd = registry.getPluginDescriptor("testb");
	assertNotNull("5.2 Should find plugin testb", pd);
	pd = registry.getPluginDescriptor("testd");
	assertNotNull("5.3 Should find plugin testd", pd);
	pd = registry.getPluginDescriptor("teste");
	assertNotNull("5.4 Should find plugin teste", pd);
	pd = registry.getPluginDescriptor("testf");
	assertNotNull("5.5 Should find plugin testf", pd);
	pd = registry.getPluginDescriptor("testg");
	assertNotNull("5.6 Should find plugin testg", pd);
	pd = registry.getPluginDescriptor("testh");
	assertNotNull("5.6 Should find plugin testh", pd);
	pd = registry.getPluginDescriptor("testj");
	assertNotNull("5.6 Should find plugin testj", pd);
	pd = registry.getPluginDescriptor("testk");
	assertNotNull("5.7 Should find plugin testk", pd);
	pd = registry.getPluginDescriptor("testl");
	assertNotNull("5.8 Should find plugin testl", pd);
	pd = registry.getPluginDescriptor("testm");
	assertNotNull("5.9 Should find plugin testm", pd);
}

public void test6() {
	/*  Plugin structure is as follows:
	 * 
	 *     There are 2 root plugins - testa and testa2
	 * 
	 *     testa requires testb
	 *     testa requires testc
	 *     testa requires testd
	 * 
	 *     testb requires teste
	 *     testb requires testf
	 *     testb requires testg
	 * 
	 *     testc requires testh
	 *     testc requires testi
	 *     testc requires testj
	 * 
	 *     testd has no prerequisites
	 * 
	 *     teste has no prerequisites
	 * 
	 *     testf has no prerequisites
	 * 
	 *     testg has no prerequisites
	 * 
	 *     testh has no prerequisites
	 * 
	 *     testi has no prerequisites and is missing a required
	 *        field so it will be disabled
	 * 
	 *     testj has no prerequisites
	 * 
	 *     testa2 requires testb
	 *     testa2 requires testd
	 * 
	 * The resulting plugin registry should contain the following
	 * plugins:
	 *    testb
	 *    testd
	 *    teste
	 *    testf
	 *    testg
	 *    testh
	 *    testj
	 *    testa2
	 * All other plugins (a, c, and i) should be disabled
	 * (and trimmed).
	 * 
	 * This is the same as test3 but the order of the requires
	 * in the plugin is such that any disabled plugins are at the
	 * end of the requires clause.  Yes, this does make a
	 * difference.  See bug 21465 - "Plugin Resolving - reorder
	 * REQUIRES clause & different plugins are disabled".
	 */
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "fragmentResolveTest", null);
	InternalFactory factory = new InternalFactory(problems);
	URL pluginURLs[] = new URL[1];
	try {
		PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
		String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/orphan.6/");
		pluginURLs[0] = new URL (pluginPath);
	} catch (java.net.MalformedURLException e) {
		fail("6.0 Unexpected exception - " + e.getMessage());
	}
	IPluginRegistry registry = ParseHelper.doParsing(factory, pluginURLs, true);
	IPluginDescriptor[] pds = registry.getPluginDescriptors();
	// Should only be 8 plugins
	assertEquals("6.1 Should only have 8 plugins", 8, pds.length);
	// Should have a plugins as listed in comment above
	IPluginDescriptor pd = registry.getPluginDescriptor("testb");
	assertNotNull("6.2 Should find plugin testb", pd);
	pd = registry.getPluginDescriptor("testd");
	assertNotNull("6.3 Should find plugin testd", pd);
	pd = registry.getPluginDescriptor("teste");
	assertNotNull("6.4 Should find plugin teste", pd);
	pd = registry.getPluginDescriptor("testf");
	assertNotNull("6.5 Should find plugin testf", pd);
	pd = registry.getPluginDescriptor("testg");
	assertNotNull("6.6 Should find plugin testg", pd);
	pd = registry.getPluginDescriptor("testh");
	assertNotNull("6.6 Should find plugin testh", pd);
	pd = registry.getPluginDescriptor("testj");
	assertNotNull("6.6 Should find plugin testj", pd);
	pd = registry.getPluginDescriptor("testa2");
	assertNotNull("6.7 Should find plugin testa2", pd);
}

public static Test suite() {
	TestSuite suite = new TestSuite();
	// Comment out test1, 2 and 3 until bug 21465 - 
	// "Plugin Resolving - reorder REQUIRES clause & different
	// plugins are disabled" is fixed.
//	suite.addTest(new OrphanTest("test1"));
//	suite.addTest(new OrphanTest("test2"));
//	suite.addTest(new OrphanTest("test3"));
	suite.addTest(new OrphanTest("test4"));
	suite.addTest(new OrphanTest("test5"));
	suite.addTest(new OrphanTest("test6"));
	return suite;
}
}

