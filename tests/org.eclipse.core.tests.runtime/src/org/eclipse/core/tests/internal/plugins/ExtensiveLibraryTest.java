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

public class ExtensiveLibraryTest extends EclipseWorkspaceTest {

public ExtensiveLibraryTest() {
	super(null);
}

public ExtensiveLibraryTest(String name) {
	super(name);
}

public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new ExtensiveLibraryTest("noLibraryTest"));
	suite.addTest(new ExtensiveLibraryTest("oneLibraryTest"));
	suite.addTest(new ExtensiveLibraryTest("oneLibraryOneExportTest"));
	suite.addTest(new ExtensiveLibraryTest("multiLibraryOneExportTest"));
	suite.addTest(new ExtensiveLibraryTest("multiLibraryMultiExportTest"));
	suite.addTest(new ExtensiveLibraryTest("emptyLibraryTest"));
	return suite;
}

public void noLibraryTest() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("extensiveLibraryTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/extensiveLibraryTest/noLibraryTest.xml");
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
	assertTrue("1.0 Only one plugin", pluginDescriptors.length == 1);
	PluginDescriptorModel plugin = (PluginDescriptorModel)pluginDescriptors[0];
	assertTrue("1.1 Got the right plugin", plugin.getId().equals("noLibraryTest"));
	assertNull("1.2 No library entries", plugin.getRuntime());
}

public void oneLibraryTest() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("extensiveLibraryTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/extensiveLibraryTest/oneLibraryTest.xml");
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
	assertTrue("2.0 Only one plugin", pluginDescriptors.length == 1);
	PluginDescriptorModel plugin = (PluginDescriptorModel)pluginDescriptors[0];
	assertTrue("2.1 Got the right plugin", plugin.getId().equals("oneLibraryTest"));
	assertTrue("2.2 One library entry", plugin.getRuntime().length == 1);
	LibraryModel library = (LibraryModel)plugin.getRuntime()[0];
	assertTrue("2.3 Got the right library", library.getName().equals("lib1.jar"));
	assertNull("2.4 No exports", library.getExports());
}

public void oneLibraryOneExportTest() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("extensiveLibraryTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String pluginPath[] = new String[2];
	pluginPath[0] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveLibraryTest/oneLibraryOneExport1Test.xml");
	pluginPath[1] = tempPlugin.getLocation().concat("Plugin_Testing/extensiveLibraryTest/oneLibraryOneExport2Test.xml");
	URL pluginURLs[] = new URL[2];
	for (int i = 0; i < pluginPath.length; i++) {
		URL pURL = null;
		try {
			pURL = new URL (pluginPath[i]);
		} catch (java.net.MalformedURLException e) {
			assertTrue("Bad URL for " + pluginPath[i], true);
		}
		pluginURLs[i] = pURL;
	}
	IPluginRegistry registry = ParseHelper.doParsing (factory, pluginURLs, true);
	IPluginDescriptor[] pluginDescriptors = registry.getPluginDescriptors();
	assertTrue("3.0 Two plugins", pluginDescriptors.length == 2);

	// Check out the first library which should be fully exported ("*" as the export list)
	IPluginDescriptor[] pluginArray = registry.getPluginDescriptors("oneLibraryOneExport1Test");
	assertTrue("3.1 Got the right plugin", pluginArray.length == 1);
	PluginDescriptorModel plugin = (PluginDescriptorModel)pluginArray[0];
	assertTrue("3.2 One library entry", plugin.getRuntime().length == 1);
	LibraryModel library = (LibraryModel)plugin.getRuntime()[0];
	assertTrue("3.3 Got the right library", library.getName().equals("lib2.jar"));
	assertTrue("3.4 Exported", library.isExported());
	assertTrue("3.5 Fully exported", library.isFullyExported());

	// Check out the second library which should be not be fully exported
	pluginArray = registry.getPluginDescriptors("oneLibraryOneExport2Test");
	assertTrue("3.6 Got the right plugin", pluginArray.length == 1);
	plugin = (PluginDescriptorModel)pluginArray[0];
	assertTrue("3.7 One library entry", plugin.getRuntime().length == 1);
	library = (LibraryModel)plugin.getRuntime()[0];
	assertTrue("3.8 Got the right library", library.getName().equals("lib2.jar"));
	assertTrue("3.9 Exported", library.isExported());
	assertTrue("3.10 Not fully exported", !library.isFullyExported());
}

public void multiLibraryOneExportTest() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("extensiveLibraryTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/extensiveLibraryTest/multiLibraryOneExportTest.xml");
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
	assertTrue("4.0 Only one plugin", pluginDescriptors.length == 1);
	PluginDescriptorModel plugin = (PluginDescriptorModel)pluginDescriptors[0];
	assertTrue("4.1 Got the right plugin", plugin.getId().equals("multiLibraryOneExportTest"));
	LibraryModel libraryArray[] = plugin.getRuntime(); 
	assertTrue("4.2 Got 3 library entries", libraryArray.length == 3);
	
	// Check out the libraries individually
	// First library should be "lib1.jar" with no exports
	LibraryModel library = libraryArray[0];
	assertTrue("4.3 Got the right library", library.getName().equals("lib1.jar"));
	assertNull("4.4 No exports", library.getExports());
	assertTrue("4.5 Not exported", !library.isExported());
	assertTrue("4.6 Not fully exported", !library.isFullyExported());
	
	// Second library should be "lib2.jar" and be fully exported
	library = libraryArray[1];
	assertTrue("4.7 Got the right library", library.getName().equals("lib2.jar"));
	assertTrue("4.8 Exported", library.isExported());
	assertTrue("4.9 Fully exported", library.isFullyExported());
	// Check the export list too
	String exportList[] = library.getExports();
	assertTrue("4.10 Only one export", exportList.length == 1);
	assertTrue("4.11 Fully exported mask", exportList[0].equals("*"));

	// Third library should be "lib3.jar" and be exported with the export
	// mask containing only "a.b.C"
	library = libraryArray[2];
	assertTrue("4.12 Got the right library", library.getName().equals("lib3.jar"));
	assertTrue("4.13 Exported", library.isExported());
	assertTrue("4.14 Not fully exported", !library.isFullyExported());
	// Now check the export list
	exportList = library.getExports();
	assertTrue("4.15 Only one export", exportList.length == 1);
	assertTrue("4.16 Exported mask", exportList[0].equals("a.b.C"));
}

public void multiLibraryMultiExportTest() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("extensiveLibraryTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/extensiveLibraryTest/multiLibraryMultiExportTest.xml");
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
	assertTrue("5.0 Only one plugin", pluginDescriptors.length == 1);
	PluginDescriptorModel plugin = (PluginDescriptorModel)pluginDescriptors[0];
	assertTrue("5.1 Got the right plugin", plugin.getId().equals("multiLibraryMultiExportTest"));
	LibraryModel libraryArray[] = plugin.getRuntime(); 
	assertTrue("5.2 Got 3 library entries", libraryArray.length == 3);
	
	// Check out the libraries individually
	// First library should be "lib1.jar" with no exports
	LibraryModel library = libraryArray[0];
	assertTrue("5.3 Got the right library", library.getName().equals("lib1.jar"));
	assertNull("5.4 No exports", library.getExports());
	assertTrue("5.5 Not exported", !library.isExported());
	assertTrue("5.6 Not fully exported", !library.isFullyExported());
	
	// Second library should be "lib2.jar" and be fully exported
	library = libraryArray[1];
	assertTrue("5.7 Got the right library", library.getName().equals("lib2.jar"));
	assertTrue("5.8 Exported", library.isExported());
	assertTrue("5.9 Fully exported", library.isFullyExported());
	// Check the export list too
	String exportList[] = library.getExports();
	assertTrue("5.10 Three exports", exportList.length == 3);
	assertTrue("5.11 Mask[0]", exportList[0].equals("a"));
	assertTrue("5.12 Mask[1]", exportList[1].equals("*"));
	assertTrue("5.13 Mask[2]", exportList[2].equals("b"));

	// Third library should be "lib3.jar" and be exported with the export
	// mask containing "a.b.C" and "a.b.D"
	library = libraryArray[2];
	assertTrue("5.14 Got the right library", library.getName().equals("lib3.jar"));
	assertTrue("5.15 Exported", library.isExported());
	assertTrue("5.16 Not fully exported", !library.isFullyExported());
	// Now check the export list
	exportList = library.getExports();
	assertTrue("5.17 Only one export", exportList.length == 2);
	assertTrue("5.18 Mask[0]", exportList[0].equals("a.b.C"));
	assertTrue("5.16 Mask[1]", exportList[1].equals("a.b.D"));
}

public void emptyLibraryTest() {
	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("extensiveLibraryTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);
	PluginDescriptor tempPlugin = (PluginDescriptor)Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.core.tests.runtime");
	String pluginPath = tempPlugin.getLocation().concat("Plugin_Testing/extensiveLibraryTest/emptyLibraryTest.xml");
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
	// Should not get any plugins as the one library entry does not
	// have the only required field, name
	assertTrue("6.0 No plugins", pluginDescriptors.length == 0);
}

}

