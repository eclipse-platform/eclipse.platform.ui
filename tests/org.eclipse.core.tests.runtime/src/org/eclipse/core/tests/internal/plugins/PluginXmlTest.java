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
public class PluginXmlTest extends EclipseWorkspaceTest {
public PluginXmlTest() {
	super(null);
}
public PluginXmlTest(String name) {
	super(name);
}
public void baseTest() {

	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);
	URL pluginURLs[] = new URL[1];
	URL pURL = null;
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/plugins.parser.1/");
	try {
		pURL = new URL (pluginPath);
	} catch (java.net.MalformedURLException e) {
		assertTrue("Bad URL for " + pluginPath, true);
	}
	pluginURLs[0] = pURL;
	IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, true);
	IPluginDescriptor pd = registry.getPluginDescriptor("plugins.parser.1");

	// check descriptor
	assertTrue("0.0", null != pd);
	assertTrue("0.1", pd.getUniqueIdentifier().equals("plugins.parser.1"));
	assertTrue("0.2", pd.getLabel().equals("IBM Tooling Platform Core Tests"));
	assertTrue("0.3", pd.getProviderName().equals("IBM"));
	assertTrue("0.4", pd.getVersionIdentifier().equals(new PluginVersionIdentifier("1.2")));
	assertTrue("0.6", !pd.isPluginActivated());
	assertTrue("0.7", pd.getInstallURL().getFile().equals("/plugin/plugins.parser.1_1.2.0/"));

	// check prereqs
	IPluginPrerequisite[] prereq = pd.getPluginPrerequisites();
	assertTrue("1.0", prereq.length == 2);

	assertTrue("1.1.0", prereq[0].getUniqueIdentifier().equals("plugins.parser.2"));
	assertTrue("1.1.0.0", prereq[0].getVersionIdentifier().equals(new PluginVersionIdentifier("3.7")));
	assertTrue("1.1.1", prereq[0].getResolvedVersionIdentifier().equals(new PluginVersionIdentifier("3.7.9")));
	assertTrue("1.1.2", prereq[0].isExported());
	assertTrue("1.1.3", prereq[0].isMatchedAsCompatible());

	assertTrue("1.2.0", prereq[1].getUniqueIdentifier().equals("plugins.parser.3"));
	assertTrue("1.2.1", null == prereq[1].getVersionIdentifier());
	assertTrue("1.2.2", prereq[1].getResolvedVersionIdentifier().equals(new PluginVersionIdentifier("1.2.0")));
	assertTrue("1.2.3", !prereq[1].isExported());
	assertTrue("1.2.4", !prereq[1].isMatchedAsPerfect());
	assertTrue("1.2.4", !prereq[1].isMatchedAsEquivalent());
	assertTrue("1.2.6", !prereq[1].isMatchedAsCompatible());
	assertTrue("1.2.7", !prereq[1].isMatchedAsGreaterOrEqual());

	// check runtime libs
	ILibrary[] lib = pd.getRuntimeLibraries();
	assertTrue("2.0", lib.length == 3);

	assertTrue("2.1.0", null == lib[0].getContentFilters());
	assertTrue("2.1.1", lib[0].getPath().toString().equals("lib1.jar"));
	assertTrue("2.1.2", !lib[0].isExported());
	assertTrue("2.1.3", !lib[0].isFullyExported());

	assertTrue("2.2.0", null == lib[1].getContentFilters()); // fully exported => no filter
	assertTrue("2.2.1", lib[1].getPath().toString().equals("lib2.jar"));
	assertTrue("2.2.2", lib[1].isExported());
	assertTrue("2.2.3", lib[1].isFullyExported());

	assertTrue("2.3.0", null != lib[2].getContentFilters());
	assertTrue("2.3.0.1", lib[2].getContentFilters().length == 1);
	assertTrue("2.3.1", lib[2].getPath().toString().equals("lib3.jar"));
	assertTrue("2.3.2", lib[2].isExported());
	assertTrue("2.3.3", !lib[2].isFullyExported());

	// check declared extension points
	IExtensionPoint[] xpt;
	xpt = pd.getExtensionPoints();
	assertTrue("3.0", xpt.length == 2);

	assertTrue("3.1.0", xpt[0].getDeclaringPluginDescriptor().equals(pd));
	assertTrue("3.1.1", xpt[0].getUniqueIdentifier().equals("plugins.parser.1" + ".xpt1"));
	assertTrue("3.1.2", xpt[0].getSimpleIdentifier().equals("xpt1"));
	assertTrue("3.1.3", xpt[0].getLabel().equals("First extension point"));
	assertTrue("3.1.4", xpt[0].getSchemaReference().equals("pde/xpt1.xml"));

	assertTrue("3.2.0", xpt[1].getDeclaringPluginDescriptor().equals(pd));
	assertTrue("3.2.1", xpt[1].getUniqueIdentifier().equals("plugins.parser.1" + ".xpt2"));
	assertTrue("3.2.2", xpt[1].getSimpleIdentifier().equals("xpt2"));
	assertTrue("3.2.3", xpt[1].getLabel().equals("Second extension point"));
	assertTrue("3.2.4", xpt[1].getSchemaReference().equals(""));

	// check declared extensions
	IExtension[] ext;
	ext = pd.getExtensions();
	assertTrue("4.0", ext.length == 2);

	assertTrue("4.1.0", ext[0].getDeclaringPluginDescriptor().equals(pd));
	assertTrue("4.1.1", ext[0].getUniqueIdentifier().equals("plugins.parser.1" + ".ext1"));
	assertTrue("4.1.2", ext[0].getSimpleIdentifier().equals("ext1"));
	assertTrue("4.1.3", ext[0].getLabel().equals("First extension"));
	assertTrue("4.1.4", ext[0].getExtensionPointUniqueIdentifier().equals("plugins.parser.1" + ".xpt1"));

	assertTrue("4.2.0", ext[1].getDeclaringPluginDescriptor().equals(pd));
	assertTrue("4.2.1", null == ext[1].getUniqueIdentifier());
	assertTrue("4.2.2", null == ext[1].getSimpleIdentifier());
	assertTrue("4.2.3", ext[1].getLabel().equals(""));
	assertTrue("4.2.4", ext[1].getExtensionPointUniqueIdentifier().equals("plugins.parser.1" + ".xpt1"));

	// check configured extensions (via extension point)
	IExtensionPoint xpt1 = registry.getExtensionPoint("plugins.parser.1" + ".xpt1");
	assertTrue("5.0", null != xpt1);
	ext = xpt1.getExtensions();
	assertTrue("5.0.1", ext.length == 2);

	assertTrue("5.1.0", ext[0].getDeclaringPluginDescriptor().equals(pd));
	assertTrue("5.1.1", ext[0].getUniqueIdentifier().equals("plugins.parser.1" + ".ext1"));
	assertTrue("5.1.2", ext[0].getSimpleIdentifier().equals("ext1"));
	assertTrue("5.1.3", ext[0].getLabel().equals("First extension"));
	assertTrue("5.1.4", ext[0].getExtensionPointUniqueIdentifier().equals("plugins.parser.1" + ".xpt1"));

	assertTrue("5.2.0", ext[1].getDeclaringPluginDescriptor().equals(pd));
	assertTrue("5.2.1", null == ext[1].getUniqueIdentifier());
	assertTrue("5.2.2", null == ext[1].getSimpleIdentifier());
	assertTrue("5.2.3", ext[1].getLabel().equals(""));
	assertTrue("5.2.4", ext[1].getExtensionPointUniqueIdentifier().equals("plugins.parser.1" + ".xpt1"));

	// check configured extension (directly)
	IExtension ext1 = registry.getExtension("plugins.parser.1", "xpt1", "plugins.parser.1" + ".ext1");
	assertTrue("6.0", null != ext1);

	assertTrue("6.1.0", ext1.getDeclaringPluginDescriptor().equals(pd));
	assertTrue("6.1.1", ext1.getUniqueIdentifier().equals("plugins.parser.1" + ".ext1"));
	assertTrue("6.1.2", ext1.getSimpleIdentifier().equals("ext1"));
	assertTrue("6.1.3", ext1.getLabel().equals("First extension"));
	assertTrue("6.1.4", ext1.getExtensionPointUniqueIdentifier().equals("plugins.parser.1" + ".xpt1"));
}
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new PluginXmlTest("baseTest"));
	return suite;
}
}
