package org.eclipse.core.tests.internal.plugins;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.model.*;
import org.eclipse.core.internal.runtime.*;
import org.eclipse.core.internal.plugins.*;
import org.eclipse.core.tests.harness.*;
import java.io.*;
import java.net.URL;
import junit.framework.*;
import org.xml.sax.*;

public class BadPluginsTest extends EclipseWorkspaceTest {

public BadPluginsTest() {
	super(null);
}

public BadPluginsTest(String name) {
	super(name);
}

public static Test suite() {

	return new TestSuite(BadPluginsTest.class);

//	TestSuite suite = new TestSuite();
//	suite.addTest(new BadPluginsTest("badElements"));
//	suite.addTest(new BadPluginsTest("badAttributes"));
//	suite.addTest(new BadPluginsTest("badPlugins"));
//	suite.addTest(new BadPluginsTest("badFragment"));
//	suite.addTest(new BadPluginsTest("failedFragment"));
//	suite.addTest(new BadPluginsTest("duplicatePlugin"));
//	return suite;
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
		"badTopLevelElementsTest.xml: Unknown element notAPlugin, found at the top level, ignored.",
		"badPluginElementsTest.xml: Unknown element somethingBad, found within a plugin / fragment, ignored.",
		"badExtensionPointElementsTest.xml: Unknown element nameless, found within a extension-point, ignored.",
		"badLibrary1ElementsTest.xml: Unknown element notAnExport, found within a library, ignored.",
		"badLibrary2ElementsTest.xml: Unknown element badElement, found within a export, ignored.",
		"badRequiresImportElementsTest.xml: Unknown element unrecognizedElement, found within a requires, ignored.",
		"badRequiresElementsTest.xml: Unknown element notAnImport, found within a requires, ignored.",
		"badRuntimeElementsTest.xml: Unknown element notALibrary, found within a runtime, ignored.",
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
		"badPluginAttributesTest.xml: Unknown attribute a-bad-attribute for element plugin ignored.",
		"badFragment1AttributesTest.xml: Unknown attribute vendor-name for element fragment ignored.",
		"badExtensionPointAttributesTest.xml: Unknown attribute bogusAttribute for element extension-point ignored.",
		"badExtensionAttributesTest.xml: Unknown attribute hello for element extension ignored.",
		"badRequiresImport1AttributesTest.xml: Unknown attribute badImportAttr for element import ignored.",
		"badRequiresImport2AttributesTest.xml: notTrue is not a valid value for the attribute \"export\".   Use \"true\" or \"false\".",
		"badRequiresImport3AttributesTest.xml: incompatible is not a valid value for the attribute \"match\".   Use \"perfect\", \"equivalent\", \"compatible\" or \"greaterOrEqual\".",
		"badLibrary1AttributesTest.xml: Unknown attribute badAttribute for element library ignored.",
		"badLibrary2AttributesTest.xml: Unknown attribute badExportAttribute for element library ignored.",
		"badLibrary3AttributesTest.xml: Unknown library type source for library lib1.jar.",
		"badFragment2AttributesTest.xml: nothing is not a valid value for the attribute \"match\".   Use \"perfect\", \"equivalent\", \"compatible\" or \"greaterOrEqual\".",
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
	String[] errorMessages = {
		"Id attribute missing from plugin or fragment at file:",
		"Id attribute missing from plugin or fragment at file:",
		"Name attribute missing from plugin or fragment at file:",
		"Name attribute missing from plugin or fragment at file:",
		"Version attribute missing from plugin or fragment at file:",
		"A plugin version identifier must be non-empty.",
		"The service (3rd) component of plugin version identifier, 1.2.bad, must be numeric.",
		"The minor (2nd) component of plugin version identifier, 1.bad.0, must be numeric.",
		"Plugin version identifier, ..., must not start with a separator character.",
		"The major (1st) component of plugin version identifier, one, must be numeric.",
		"Plugin version identifier, 1.2.3.4.5, can contain a maximum of four components.",
		"",
	};
	
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
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
		"Plugin descriptor org.eclipse.not.there not found for fragment badFragmentsTest.  Fragment ignored.",
		"Fragment Fragment 10799 requires non-existant plugin org.apache.something.which.does.not.exist.  Fragment ignored.",
	};
	
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
	String[] errorMessages = {
		"Id attribute missing from plugin or fragment at file:",
		"Id attribute missing from plugin or fragment at file:",
		"Name attribute missing from plugin or fragment at file:",
		"Name attribute missing from plugin or fragment at file:",
		"Version attribute missing from plugin or fragment at file:",
		"A plugin version identifier must be non-empty.",
		"The service (3rd) component of plugin version identifier, 1.2.bad, must be numeric.",
		"Plugin name attribute missing from fragment at file:",
		"Plugin name attribute missing from fragment at file:",
		"Plugin version attribute missing from fragment at file:",
		"A plugin version identifier must be non-empty.",
	};
	
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
	String errorMessage = "Two plugins found with the same id: duplicatePluginTest. Ignoring duplicate at file:";
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

