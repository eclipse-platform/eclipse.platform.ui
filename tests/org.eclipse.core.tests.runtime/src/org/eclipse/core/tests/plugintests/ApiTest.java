package org.eclipse.core.tests.plugintests;

import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.plugins.*;
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.tests.harness.*;
import java.net.*;
import junit.framework.*;

public class ApiTest extends EclipseWorkspaceTest {
public ApiTest() {
	super(null);
}
public ApiTest(String name) {
	super(name);
}
public void baseTest() {

	String id = "com.ibm.eclipse.core.tests.plugins.parser.1";

	String[] pid = { "com.ibm.eclipse.core.tests.plugins.parser.2", "com.ibm.eclipse.core.tests.plugins.parser.3" };

	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);
	URL URLList[] = new URL[1];
	try {
		URLList[0] = new URL("vaLoader:/N:/Eclipse TOR Core Test/0.042b/project_resources/IBM Eclipse Core Tests Plugin/plugins-test-files/plugins.parser.1/");
	} catch (java.net.MalformedURLException mfe) {
		assertTrue("Malformed URL Exception encountered", true);
	}
	IPluginRegistry registry = doParsing(factory, URLList);
	IPluginDescriptor plugin = registry.getPluginDescriptor(id);

	// check descriptor
	assertNotNull("0.0", plugin);
	assertEquals("0.1", plugin.getUniqueIdentifier(), id);
	assertEquals("0.2", plugin.getLabel(), "IBM Tooling Platform Core Tests");
	assertEquals("0.3", plugin.getProviderName(), "IBM");
	assertEquals("0.4", plugin.getVersionIdentifier(), new PluginVersionIdentifier("1.2"));
	assertTrue("0.6", !plugin.isPluginActivated());
	assertEquals("0.7", plugin.getInstallURL().toExternalForm(), "eclipse:/plugin/com.ibm.eclipse.core.tests.plugins.parser.1/");

	// check prereqs
	IPluginPrerequisite[] prereq = plugin.getPluginPrerequisites();
	assertEquals("1.0", prereq.length, 2);

	assertEquals("1.1.0", prereq[0].getUniqueIdentifier(), pid[0]);
	assertEquals("1.1.1", prereq[0].getVersionIdentifier(), new PluginVersionIdentifier("3.7"));
	assertTrue("1.1.2", prereq[0].isExported());
	assertTrue("1.1.3", !prereq[0].isMatchedAsCompatible());
	assertTrue("1.1.4", prereq[0].isMatchedAsExact());

	assertEquals("1.2.0", prereq[1].getUniqueIdentifier(), pid[1]);
	assertNull("1.2.1", prereq[1].getVersionIdentifier());
	assertTrue("1.2.2", !prereq[1].isExported());
	assertTrue("1.2.3", prereq[1].isMatchedAsCompatible());
	assertTrue("1.2.4", !prereq[1].isMatchedAsExact());

	// check runtime libs
	ILibrary[] lib = plugin.getRuntimeLibraries();
	assertTrue("2.0", lib.length == 3);

	assertNull("2.1.0", lib[0].getContentFilters());
	assertEquals("2.1.1", lib[0].getPath().toString(), "lib1.jar");
	assertTrue("2.1.2", !lib[0].isExported());
	assertTrue("2.1.3", !lib[0].isFullyExported());

	assertNull("2.2.0", lib[1].getContentFilters()); // fully exported => no filter
	assertEquals("2.2.1", lib[1].getPath().toString(), "lib2.jar");
	assertTrue("2.2.2", lib[1].isExported());
	assertTrue("2.2.3", lib[1].isFullyExported());

	assertNotNull("2.3.0", lib[2].getContentFilters());
	assertEquals("2.3.0.1", lib[2].getContentFilters().length, 1);
	assertEquals("2.3.1", lib[2].getPath().toString(), "lib3.jar");
	assertTrue("2.3.2", lib[2].isExported());
	assertTrue("2.3.3", !lib[2].isFullyExported());

	// check declared extension points
	IExtensionPoint[] xpt;
	xpt = plugin.getExtensionPoints();
	assertEquals("3.0", xpt.length, 2);

	assertEquals("3.1.0", xpt[0].getDeclaringPluginDescriptor(), plugin);
	assertEquals("3.1.1", xpt[0].getUniqueIdentifier(), id + ".xpt1");
	assertEquals("3.1.2", xpt[0].getSimpleIdentifier(), "xpt1");
	assertEquals("3.1.3", xpt[0].getLabel(), "First extension point");

	assertEquals("3.2.0", xpt[1].getDeclaringPluginDescriptor(), plugin);
	assertEquals("3.2.1", xpt[1].getUniqueIdentifier(), id + ".xpt2");
	assertEquals("3.2.2", xpt[1].getSimpleIdentifier(), "xpt2");
	assertEquals("3.2.3", xpt[1].getLabel(), "");

	// check declared extensions
	IExtension[] ext;
	ext = plugin.getExtensions();
	assertTrue("4.0", ext.length == 2);

	assertEquals("4.1.0", ext[0].getDeclaringPluginDescriptor(), plugin);
	assertEquals("4.1.1", ext[0].getUniqueIdentifier(), id + ".ext1");
	assertEquals("4.1.2", ext[0].getSimpleIdentifier(), "ext1");
	assertEquals("4.1.3", ext[0].getLabel(), "First extension");
	assertEquals("4.1.4", ext[0].getExtensionPointUniqueIdentifier(), id + ".xpt1");

	assertEquals("4.2.0", ext[1].getDeclaringPluginDescriptor(), plugin);
	assertNull("4.2.1", ext[1].getUniqueIdentifier());
	assertNull("4.2.2", ext[1].getSimpleIdentifier());
	assertEquals("4.2.3", ext[1].getLabel(), "");
	assertEquals("4.2.4", ext[1].getExtensionPointUniqueIdentifier(), id + ".xpt1");

	// check configured extensions (via extension point)
	IExtensionPoint xpt1 = registry.getExtensionPoint(id + ".xpt1");
	assertNotNull("5.0", xpt1);
	ext = xpt1.getExtensions();
	assertEquals("5.0.1", ext.length, 2);

	assertEquals("5.1.0", ext[0].getDeclaringPluginDescriptor(), plugin);
	assertEquals("5.1.1", ext[0].getUniqueIdentifier(), id + ".ext1");
	assertEquals("5.1.2", ext[0].getSimpleIdentifier(), "ext1");
	assertEquals("5.1.3", ext[0].getLabel(), "First extension");
	assertEquals("5.1.4", ext[0].getExtensionPointUniqueIdentifier(), id + ".xpt1");

	assertEquals("5.2.0", ext[1].getDeclaringPluginDescriptor(), plugin);
	assertNull("5.2.1", ext[1].getUniqueIdentifier());
	assertNull("5.2.2", ext[1].getSimpleIdentifier());
	assertEquals("5.2.3", ext[1].getLabel(), "");
	assertEquals("5.2.4", ext[1].getExtensionPointUniqueIdentifier(), id + ".xpt1");

	// check configured extension (directly)
	IExtension ext1 = registry.getExtension(id, "xpt1", id + ".ext1");
	assertNotNull("6.0", ext1);

	assertEquals("6.1.0", ext1.getDeclaringPluginDescriptor(), plugin);
	assertEquals("6.1.1", ext1.getUniqueIdentifier(), id + ".ext1");
	assertEquals("6.1.2", ext1.getSimpleIdentifier(), "ext1");
	assertEquals("6.1.3", ext1.getLabel(), "First extension");
	assertEquals("6.1.4", ext1.getExtensionPointUniqueIdentifier(), id + ".xpt1");
}
/* doParsing
 * This method will parse a series of XML files.  The input array should be
 * an array of string buffers where each string buffer is considered a complete 
 * XML file.  The returning array will have a corresponding plugin descriptor
 * for each of the XML files in the input array
 */
public PluginRegistry doParsing(InternalFactory factory, URL[] pluginPath) {
	PluginRegistry registry = (PluginRegistry) RegistryLoader.parseRegistry(pluginPath, factory, false);
	registry.resolve(true, true);
	registry.markReadOnly();
	registry.startup(null);
	return registry;
}
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new ApiTest("baseTest"));
	suite.addTest(new ApiTest("versionIdTest"));
	return suite;
}
public void versionIdTest() {

	MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("registryTestProblems", new String[0]), null);
	InternalFactory factory = new InternalFactory(problems);
	URL URLList[] = new URL[1];
	try {
		URLList[0] = new URL("vaLoader:/N:/Eclipse TOR Core Test/0.042b/project_resources/IBM Eclipse Core Tests Plugin/plugins-test-files/plugins.parser.1/");
	} catch (java.net.MalformedURLException mfe) {
	}
	IPluginRegistry registry = doParsing(factory, URLList);

	PluginVersionIdentifier v1;
	PluginVersionIdentifier v2;

	v1 = null;
	try {
		v1 = new PluginVersionIdentifier("1");
	} catch (Throwable e) {
		Assert.fail("1.1.0 " + e);
	}
	assertTrue("1.1.1", v1 != null);
	assertTrue("1.1.2", v1.getMajorComponent() == 1);
	assertTrue("1.1.3", v1.getMinorComponent() == 0);
	assertTrue("1.1.4", v1.getServiceComponent() == 0);
	assertTrue("1.1.5", v1.equals(new PluginVersionIdentifier(v1.toString())));

	v2 = null;
	try {
		v2 = new PluginVersionIdentifier("1.0");
	} catch (Throwable e) {
		Assert.fail("1.2.0 " + e);
	}
	assertTrue("1.2.1", v2 != null);
	assertTrue("1.2.2", v2.getMajorComponent() == 1);
	assertTrue("1.2.3", v2.getMinorComponent() == 0);
	assertTrue("1.2.4", v2.getServiceComponent() == 0);
	assertTrue("1.2.5", v2.equals(new PluginVersionIdentifier(v2.toString())));
	assertTrue("1.2.6", v2.equals(v1));
	assertTrue("1.2.7", v2.isCompatibleWith(v1));
	assertTrue("1.2.8", v2.isEquivalentTo(v1));

	v2 = null;
	try {
		v2 = new PluginVersionIdentifier("1.0.0");
	} catch (Throwable e) {
		Assert.fail("1.3.0 " + e);
	}
	assertTrue("1.3.1", v2 != null);
	assertTrue("1.3.2", v2.getMajorComponent() == 1);
	assertTrue("1.3.3", v2.getMinorComponent() == 0);
	assertTrue("1.3.4", v2.getServiceComponent() == 0);
	assertTrue("1.3.5", v2.equals(new PluginVersionIdentifier(v2.toString())));
	assertTrue("1.3.6", v2.equals(v1));
	assertTrue("1.3.7", v2.isCompatibleWith(v1));
	assertTrue("1.3.8", v2.isEquivalentTo(v1));

	v1 = null;
	v2 = null;
	try {
		v1 = new PluginVersionIdentifier("1.2.3");
	} catch (Throwable e) {
		Assert.fail("1.4.0 " + e);
	}
	try {
		v2 = new PluginVersionIdentifier("1.2.3");
	} catch (Throwable e) {
		Assert.fail("1.4.1 " + e);
	}
	assertTrue("1.4.2", v2.equals(v1));
	assertTrue("1.4.3", v2.isCompatibleWith(v1));
	assertTrue("1.4.4", v2.isEquivalentTo(v1));

	v2 = null;
	try {
		v2 = new PluginVersionIdentifier("1.2.4");
	} catch (Throwable e) {
		Assert.fail("1.5.1 " + e);
	}
	assertTrue("1.5.2", !v2.equals(v1));
	assertTrue("1.5.3", v2.isCompatibleWith(v1));
	assertTrue("1.5.4", v2.isEquivalentTo(v1));

	v2 = null;
	try {
		v2 = new PluginVersionIdentifier("1.2.2");
	} catch (Throwable e) {
		Assert.fail("1.6.1 " + e);
	}
	assertTrue("1.6.2", !v2.equals(v1));
	assertTrue("1.6.3", !v2.isCompatibleWith(v1));
	assertTrue("1.6.4", !v2.isEquivalentTo(v1));

	v2 = null;
	try {
		v2 = new PluginVersionIdentifier("1.3.3");
	} catch (Throwable e) {
		Assert.fail("1.7.1 " + e);
	}
	assertTrue("1.7.2", !v2.equals(v1));
	assertTrue("1.7.3", v2.isCompatibleWith(v1));
	assertTrue("1.7.4", !v2.isEquivalentTo(v1));

	v2 = null;
	try {
		v2 = new PluginVersionIdentifier("1.3.4");
	} catch (Throwable e) {
		Assert.fail("1.8.1 " + e);
	}
	assertTrue("1.8.2", !v2.equals(v1));
	assertTrue("1.8.3", v2.isCompatibleWith(v1));
	assertTrue("1.8.4", !v2.isEquivalentTo(v1));

	v2 = null;
	try {
		v2 = new PluginVersionIdentifier("1.3.2");
	} catch (Throwable e) {
		Assert.fail("1.9.1 " + e);
	}
	assertTrue("1.9.2", !v2.equals(v1));
	assertTrue("1.9.3", v2.isCompatibleWith(v1));
	assertTrue("1.9.4", !v2.isEquivalentTo(v1));

	v2 = null;
	try {
		v2 = new PluginVersionIdentifier("2.2.3");
	} catch (Throwable e) {
		Assert.fail("1.10.1 " + e);
	}
	assertTrue("1.10.2", !v2.equals(v1));
	assertTrue("1.10.3", !v2.isCompatibleWith(v1));
	assertTrue("1.10.4", !v2.isEquivalentTo(v1));

	v2 = null;
	try {
		v2 = new PluginVersionIdentifier("2.2.4");
	} catch (Throwable e) {
		Assert.fail("1.11.1 " + e);
	}
	assertTrue("1.11.2", !v2.equals(v1));
	assertTrue("1.11.3", !v2.isCompatibleWith(v1));
	assertTrue("1.11.4", !v2.isEquivalentTo(v1));

	v2 = null;
	try {
		v2 = new PluginVersionIdentifier("2.2.2");
	} catch (Throwable e) {
		Assert.fail("1.12.1 " + e);
	}
	assertTrue("1.12.2", !v2.equals(v1));
	assertTrue("1.12.3", !v2.isCompatibleWith(v1));
	assertTrue("1.12.4", !v2.isEquivalentTo(v1));
}
}
