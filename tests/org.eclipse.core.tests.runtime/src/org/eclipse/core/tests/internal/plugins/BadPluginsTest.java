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
import org.eclipse.core.runtime.model.*;
import org.eclipse.core.internal.plugins.*;
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.tests.harness.*;
import java.net.URL;
import junit.framework.*;


public class BadPluginsTest extends EclipseWorkspaceTest {

public BadPluginsTest() {
	super(null);
}

public BadPluginsTest(String name) {
	super(name);
}

public static Test suite() {
//	TestSuite suite = new TestSuite();
//	suite.addTest(new BadPluginsTest("testFailedFragment"));
//	return suite;
	return new TestSuite(BadPluginsTest.class);
}

public void testBadElements() {
	String[] badElements = {
		"badTopLevelElementsTest", 
		"badPluginElementsTest", 
		"badExtensionPointElementsTest", 
		"badLibrary1ElementsTest",
		"badLibrary2ElementsTest",
		"badRequiresImportElementsTest",
		"badRequiresElementsTest",
		"badRuntimeElementsTest",
	};
	String[] errorMessages = {
		Policy.bind("parse.unknownTopElement", "notAPlugin"),
		Policy.bind("parse.unknownElement", "plugin / fragment", "somethingBad"),
		Policy.bind("parse.unknownElement", "extension-point", "nameless"),
		Policy.bind("parse.unknownElement", "library", "notAnExport"),
		Policy.bind("parse.unknownElement", "export", "badElement"),
		Policy.bind("parse.unknownElement", "requires", "unrecognizedElement"),
		Policy.bind("parse.unknownElement", "requires", "notAnImport"),
		Policy.bind("parse.unknownElement", "runtime", "notALibrary"),
	};

	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	try {
		for (int i = 0; i < badElements.length; i++) {
			MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "badPluginsTestProblems", null);
			InternalFactory factory = new InternalFactory(problems);
			String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/badPluginsTest/" + badElements[i] + ".xml");
			URL pluginURLs[] = new URL[1];
			URL pURL = null;
			try {
				pURL = new URL (pluginPath);
			} catch (java.net.MalformedURLException e) {
				assertTrue("Bad URL for " + pluginPath, true);
			}
			pluginURLs[0] = pURL;
			IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, false);
			if (badElements[i].equals("badTopLevelElementsTest")) {
				IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors();
				assertTrue(i + ".0 Only one plugin", pluginDescriptors.length == 0);
				if (errorMessages[i].equals("")) {
					System.out.println ("id = <no plugin>");
					System.out.println (problems.toString());
				} else
					assertTrue(i + ".1 Got the right errors", problems.toString().indexOf(errorMessages[i]) != -1);
			} else {
				IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors();
				assertTrue(i + ".0 Only one plugin", pluginDescriptors.length == 1);
				PluginDescriptorModel plugin = (PluginDescriptorModel)pluginDescriptors[0];
				assertTrue(i + ".1 Got the right plugin", plugin.getId().equals(badElements[i]));
				if (errorMessages[i].equals("")) {
					System.out.println ("id = " + plugin.getId());
					System.out.println (problems.toString());
				} else
					assertTrue(i + ".2 Got the right errors", problems.toString().indexOf(errorMessages[i]) != -1);
			}
		}
	} catch (Exception e) {}
}

public void testBadAttributes() {
	String[] badAttrs = {
		"badPluginAttributesTest", 
		"badFragment1AttributesTest", 
		"badExtensionPointAttributesTest", 
		"badExtensionAttributesTest",
		"badRequiresImport1AttributesTest",
		"badRequiresImport2AttributesTest",
		"badRequiresImport3AttributesTest",
		"badLibrary1AttributesTest",
		"badLibrary2AttributesTest",
		"badLibrary3AttributesTest",
		"badFragment2AttributesTest", 
	};
	String[] errorMessages = {
		Policy.bind("parse.unknownAttribute", "plugin", "a-bad-attribute"),
		Policy.bind("parse.unknownAttribute", "fragment", "vendor-name"),
		Policy.bind("parse.unknownAttribute", "extension-point", "bogusAttribute"),
		Policy.bind("parse.unknownAttribute", "extension", "hello"),
		Policy.bind("parse.unknownAttribute", "import", "badImportAttr"),
		Policy.bind("parse.validExport", "notTrue"),
		Policy.bind("parse.validMatch", "incompatible"),
		Policy.bind("parse.unknownAttribute", "library", "badAttribute"),
		Policy.bind("parse.unknownAttribute", "library", "badExportAttribute"),
		Policy.bind("parse.unknownLibraryType", "source", "lib1.jar"),
		Policy.bind("parse.validMatch", "nothing"),
	};
	
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	try {
		for (int i = 0; i < badAttrs.length; i++) {
			MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "badPluginsTestProblems", null);
			InternalFactory factory = new InternalFactory(problems);
			String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/badPluginsTest/" + badAttrs[i] + ".xml");
			URL pluginURLs[] = new URL[1];
			URL pURL = null;
			try {
				pURL = new URL (pluginPath);
			} catch (java.net.MalformedURLException e) {
				assertTrue("Bad URL for " + pluginPath, true);
			}
			pluginURLs[0] = pURL;
			IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, false);
			String id = null;
			if ((badAttrs[i].equals("badFragment1AttributesTest")) ||
			    (badAttrs[i].equals("badFragment2AttributesTest"))) {
				PluginFragmentModel[] fragmentDescriptors = ((PluginRegistryModel)registry).getFragments();
				assertTrue(i + ".0 Only one fragment", fragmentDescriptors.length == 1);
				PluginFragmentModel fragment = (PluginFragmentModel)fragmentDescriptors[0];
				id = fragment.getId();
				assertTrue(i + ".1 Got the right fragment", id.equals(badAttrs[i]));
			} else {
				IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors();
				assertTrue(i + ".0 Only one plugin", pluginDescriptors.length == 1);
				PluginDescriptorModel plugin = (PluginDescriptorModel)pluginDescriptors[0];
				id = plugin.getId();
				assertTrue(i + ".1 Got the right plugin", id.equals(badAttrs[i]));
			}
			if (errorMessages[i].equals("")) {
				System.out.println("id = " + id);
				System.out.println(problems.toString());
			} else {
				assertTrue(i + ".2 Got the right errors", problems.toString().indexOf(errorMessages[i]) != -1);
			}
		}
	} catch (Exception e) {}
}

public void testBadPlugins() {
	String[] badTests = {
		"noPluginIdTest", 
		"blankPluginIdTest",
		"noPluginNameTest",
		"blankPluginNameTest", 
		"noPluginVersionTest", 
		"blankPluginVersionTest",
		"badPluginVersionTest", 
		"badPluginVersion2Test", 
		"badPluginVersion3Test", 
		"badPluginVersion4Test", 
		"badPluginVersion5Test", 
	};
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String[] errorMessages = {
		Policy.bind("parse.missingPluginId", tempPlugin.getLocation().concat("Plugin_Testing/badPluginsTest/" + badTests[0] + ".xml")),
		Policy.bind("parse.missingPluginId", tempPlugin.getLocation().concat("Plugin_Testing/badPluginsTest/" + badTests[1] + ".xml")),
		Policy.bind("parse.missingPluginName", tempPlugin.getLocation().concat("Plugin_Testing/badPluginsTest/" + badTests[2] + ".xml")),
		Policy.bind("parse.missingPluginName", tempPlugin.getLocation().concat("Plugin_Testing/badPluginsTest/" + badTests[3] + ".xml")),
		Policy.bind("parse.missingPluginVersion", tempPlugin.getLocation().concat("Plugin_Testing/badPluginsTest/" + badTests[4] + ".xml")),
		Policy.bind("parse.emptyPluginVersion"),
		Policy.bind("parse.numericServiceComponent", "1.2.bad"),
		Policy.bind("parse.numericMinorComponent", "1.bad.0"),
		Policy.bind("parse.separatorStartVersion", "..."),
		Policy.bind("parse.numericMajorComponent", "one"),
		Policy.bind("parse.fourElementPluginVersion", "1.2.3.4.5"),
	};
	
	try {
		for (int i = 0; i < badTests.length; i++) {
			MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "badPluginsTestProblems", null);
			InternalFactory factory = new InternalFactory(problems);
			String[] pluginPath = new String[1];
			pluginPath[0] = tempPlugin.getLocation().concat("Plugin_Testing/badPluginsTest/" + badTests[i] + ".xml");
			URL pluginURLs[] = new URL[1];
			for (int j = 0; j < pluginURLs.length; j++) {
				URL pURL = null;
				try {
					pURL = new URL (pluginPath[j]);
				} catch (java.net.MalformedURLException e) {
					assertTrue("Bad URL for " + pluginPath[j], true);
				}
				pluginURLs[j] = pURL;
			}
			IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, false);
			PluginDescriptorModel[] plugins = ((PluginRegistryModel)registry).getPlugins();
			assertTrue(i + ".0 No plugins found", plugins.length == 0);
			if (errorMessages[i].equals("")) {
				System.out.println("id = " + badTests[i]);
				System.out.println(problems.toString());
			} else {
				assertTrue(i + ".1 Got the right errors", problems.toString().indexOf(errorMessages[i]) != -1);
			}
		}
	} catch (Exception e) {
		fail("0.2 Unexpected exception - " + e.getMessage());
	}
}

public void testBadFragment() {
	String[] badAttrs = {
		"badFragmentsTest",
		"fragment10799",
	};
	String[] errorMessages = {
		Policy.bind("parse.missingFragmentPd", "org.eclipse.not.there", "badFragmentsTest"),
		Policy.bind("parse.badPrereqOnFrag", "Fragment 10799", "org.apache.something.which.does.not.exist")	};
	
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	try {
		for (int i = 0; i < badAttrs.length; i++) {
			MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "badPluginsTestProblems", null);
			InternalFactory factory = new InternalFactory(problems);
			String[] pluginPath = new String[2];
			pluginPath[0] = tempPlugin.getLocation().concat("Plugin_Testing/badPluginsTest/badFragmentsPluginTest.xml");
			pluginPath[1] = tempPlugin.getLocation().concat("Plugin_Testing/badPluginsTest/" + badAttrs[i] + ".xml");
			URL pluginURLs[] = new URL[2];
			for (int j = 0; j < pluginURLs.length; j++) {
				URL pURL = null;
				try {
					pURL = new URL (pluginPath[j]);
				} catch (java.net.MalformedURLException e) {
					assertTrue("Bad URL for " + pluginPath[j], true);
				}
				pluginURLs[j] = pURL;
			}
			IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, true);
			String id = null;
			PluginFragmentModel[] fragmentDescriptors = ((PluginRegistryModel)registry).getFragments();
			assertTrue(i + ".0 Only one fragment", fragmentDescriptors.length == 1);
			PluginFragmentModel fragment = (PluginFragmentModel)fragmentDescriptors[0];
			id = fragment.getId();
			assertTrue(i + ".1 Got the right fragment", id.equals(badAttrs[i]));
			IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors();
			assertTrue(i + ".2 Only one plugin", pluginDescriptors.length == 1);
			PluginDescriptorModel plugin = (PluginDescriptorModel)pluginDescriptors[0];
			id = plugin.getId();
			assertTrue(i + ".3 Got the right plugin", id.equals("badFragmentsPluginTest"));
			if (errorMessages[i].equals("")) {
				System.out.println("id = " + id);
				System.out.println(problems.toString());
			} else {
				assertTrue(i + ".4 Got the right errors", problems.toString().indexOf(errorMessages[i]) != -1);
			}
		}
	} catch (Exception e) {
		fail("0.5 Unexpected exception - " + e.getMessage());
	}
}

public void testFailedFragment() {
	String[] badAttrs = {
		"noFragmentIdTest", 
		"blankFragmentIdTest",
		"noFragmentNameTest",
		"blankFragmentNameTest", 
		"noFragmentVersionTest", 
		"blankFragmentVersionTest",
		"badFragmentVersionTest", 
		"noFragmentPluginIdTest",
		"blankFragmentPluginIdTest",
		"noFragmentPluginVersionTest",
		"blankFragmentPluginVersionTest",
	};
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String[] errorMessages = {
		Policy.bind("parse.missingPluginId", tempPlugin.getLocation().concat("Plugin_Testing/badPluginsTest/" + badAttrs[0] + ".xml")),
		Policy.bind("parse.missingPluginId", tempPlugin.getLocation().concat("Plugin_Testing/badPluginsTest/" + badAttrs[1] + ".xml")),
		Policy.bind("parse.missingPluginName", tempPlugin.getLocation().concat("Plugin_Testing/badPluginsTest/" + badAttrs[2] + ".xml")),
		Policy.bind("parse.missingPluginName", tempPlugin.getLocation().concat("Plugin_Testing/badPluginsTest/" + badAttrs[3] + ".xml")),
		Policy.bind("parse.missingPluginVersion", tempPlugin.getLocation().concat("Plugin_Testing/badPluginsTest/" + badAttrs[4] + ".xml")),
		Policy.bind("parse.emptyPluginVersion"),
		Policy.bind("parse.numericServiceComponent", "1.2.bad"),
		Policy.bind("parse.missingFPName", tempPlugin.getLocation().concat("Plugin_Testing/badPluginsTest/" + badAttrs[7] + ".xml")),
		Policy.bind("parse.missingFPName", tempPlugin.getLocation().concat("Plugin_Testing/badPluginsTest/" + badAttrs[8] + ".xml")),
		Policy.bind("parse.missingFPVersion", tempPlugin.getLocation().concat("Plugin_Testing/badPluginsTest/" + badAttrs[9] + ".xml")),
		Policy.bind("parse.emptyPluginVersion"),
	};
	
	try {
		for (int i = 0; i < badAttrs.length; i++) {
			MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "badPluginsTestProblems", null);
			InternalFactory factory = new InternalFactory(problems);
			String[] pluginPath = new String[2];
			pluginPath[0] = tempPlugin.getLocation().concat("Plugin_Testing/badPluginsTest/badFragmentsPluginTest.xml");
			pluginPath[1] = tempPlugin.getLocation().concat("Plugin_Testing/badPluginsTest/" + badAttrs[i] + ".xml");
			URL pluginURLs[] = new URL[2];
			for (int j = 0; j < pluginURLs.length; j++) {
				URL pURL = null;
				try {
					pURL = new URL (pluginPath[j]);
				} catch (java.net.MalformedURLException e) {
					assertTrue("Bad URL for " + pluginPath[j], true);
				}
				pluginURLs[j] = pURL;
			}
			IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, false);
			PluginFragmentModel[] fragmentDescriptors = ((PluginRegistryModel)registry).getFragments();
			assertTrue(i + ".0 No fragments found", fragmentDescriptors.length == 0);
			if (errorMessages[i].equals("")) {
				System.out.println("id = " + badAttrs[i]);
				System.out.println(problems.toString());
			} else {
				assertTrue(i + ".1 Got the right errors", problems.toString().indexOf(errorMessages[i]) != -1);
			}
		}
	} catch (Exception e) {
		fail("0.2 Unexpected exception - " + e.getMessage());
	}
}
public void testDuplicatePlugin() {
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String errorMessage = "Two plug-ins found with the same id: \"duplicatePluginTest\". Ignoring duplicate at \"file:";
	try {
		MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "badPluginsTestProblems", null);
		InternalFactory factory = new InternalFactory(problems);
		String[] pluginPath = new String[2];
		pluginPath[0] = tempPlugin.getLocation().concat("Plugin_Testing/badPluginsTest/duplicatePlugin1.xml");
		pluginPath[1] = tempPlugin.getLocation().concat("Plugin_Testing/badPluginsTest/duplicatePlugin2.xml");
		URL pluginURLs[] = new URL[2];
		for (int j = 0; j < pluginURLs.length; j++) {
			URL pURL = null;
			try {
				pURL = new URL (pluginPath[j]);
			} catch (java.net.MalformedURLException e) {
				assertTrue("Bad URL for " + pluginPath[j], true);
			}
			pluginURLs[j] = pURL;
		}
		IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, false);
		PluginDescriptorModel[] pluginDescriptors = ((PluginRegistryModel)registry).getPlugins();
		assertTrue("1.0 One plugin found", pluginDescriptors.length == 1);
		if (errorMessage.equals("")) {
			System.out.println(problems.toString());
		} else {
			assertTrue("1.1 Got the right errors", problems.toString().indexOf(errorMessage) != -1);
		}
	} catch (Exception e) {
		fail("0.2 Unexpected exception - " + e.getMessage());
	}
}
}

