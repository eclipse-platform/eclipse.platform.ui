package org.eclipse.core.tests.internal.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.WorkspaceSessionTest;
/**
 */
public class LoaderTest extends WorkspaceSessionTest {
public LoaderTest() {
	super(null);
}
public LoaderTest(String name) {
	super(name);
}
public void testBase() {

	IPluginRegistry registry = InternalPlatform.getPluginRegistry();

	// get descriptors
	IPluginDescriptor pluginA = registry.getPluginDescriptor("plugin.a");
	IPluginDescriptor pluginB = registry.getPluginDescriptor("plugin.b");
	IPluginDescriptor pluginC = registry.getPluginDescriptor("plugin.c");
	IPluginDescriptor pluginD = registry.getPluginDescriptor("plugin.d");
	assertNotNull("0.0", pluginA);
	assertNotNull("0.1", pluginB);
	assertNotNull("0.2", pluginC);
	assertNotNull("0.3", pluginD);

	// get plugin loaders
	Plugin p = null;

	try {
		p = pluginA.getPlugin();
	} catch (CoreException e) {
		fail("2.0.0");
	}
	assertTrue("2.0.1", pluginA.isPluginActivated());
	assertTrue("2.1", !pluginB.isPluginActivated());
	assertTrue("2.2", !pluginC.isPluginActivated());
	assertTrue("2.3", !pluginD.isPluginActivated());
	ClassLoader loaderA = p.getClass().getClassLoader();

	try {
		p = pluginB.getPlugin();
	} catch (CoreException e) {
		fail("3.0.0");
	}
	assertTrue("3.0.1", pluginA.isPluginActivated());
	assertTrue("3.1", pluginB.isPluginActivated());
	assertTrue("3.2", !pluginC.isPluginActivated());
	assertTrue("3.3", !pluginD.isPluginActivated());
	ClassLoader loaderB = p.getClass().getClassLoader();

	try {
		p = pluginC.getPlugin();
	} catch (CoreException e) {
		fail("4.0.0");
	}
	assertTrue("4.0.1", pluginA.isPluginActivated());
	assertTrue("4.1", pluginB.isPluginActivated());
	assertTrue("4.2", pluginC.isPluginActivated());
	assertTrue("4.3", !pluginD.isPluginActivated());
	ClassLoader loaderC = p.getClass().getClassLoader();

	try {
		p = pluginD.getPlugin();
	} catch (CoreException e) {
		fail("5.0.0");
	}
	assertTrue("5.0.1", pluginA.isPluginActivated());
	assertTrue("5.1", pluginB.isPluginActivated());
	assertTrue("5.2", pluginC.isPluginActivated());
	assertTrue("5.3", pluginD.isPluginActivated());
	ClassLoader loaderD = p.getClass().getClassLoader();

	// try to load classes

	load(loaderD, "org.eclipse.core.tests.internal.plugin.a.api.ApiClass", false, "6.1");
	load(loaderD, "org.eclipse.core.tests.internal.plugin.b.api.ApiClass", false, "6.2");
	load(loaderD, "org.eclipse.core.tests.internal.plugin.c.api.ApiClass", false, "6.3");
	load(loaderD, "org.eclipse.core.tests.internal.plugin.d.api.ApiClass", true, "6.4");
	load(loaderD, "org.eclipse.core.tests.internal.plugin.a.PluginClass", false, "6.5");
	load(loaderD, "org.eclipse.core.tests.internal.plugin.b.PluginClass", false, "6.6");
	load(loaderD, "org.eclipse.core.tests.internal.plugin.c.PluginClass", false, "6.7");
	load(loaderD, "org.eclipse.core.tests.internal.plugin.d.PluginClass", true, "6.8");
	load(loaderD, "plugin.c.ExportedClass", false, "6.9");

	load(loaderC, "org.eclipse.core.tests.internal.plugin.a.api.ApiClass", false, "7.1");
	load(loaderC, "org.eclipse.core.tests.internal.plugin.b.api.ApiClass", false, "7.2");
	load(loaderC, "org.eclipse.core.tests.internal.plugin.c.api.ApiClass", true, "7.3");
	load(loaderC, "org.eclipse.core.tests.internal.plugin.d.api.ApiClass", false, "7.4");
	load(loaderC, "org.eclipse.core.tests.internal.plugin.a.PluginClass", false, "7.5");
	load(loaderC, "org.eclipse.core.tests.internal.plugin.b.PluginClass", false, "7.6");
	load(loaderC, "org.eclipse.core.tests.internal.plugin.c.PluginClass", true, "7.7");
	load(loaderC, "org.eclipse.core.tests.internal.plugin.d.PluginClass", false, "7.8");
	load(loaderC, "org.eclipse.core.tests.internal.plugin.c.ExportedClass", true, "7.9");

	load(loaderB, "org.eclipse.core.tests.internal.plugin.a.api.ApiClass", false, "8.1");
	load(loaderB, "org.eclipse.core.tests.internal.plugin.b.api.ApiClass", true, "8.2");
	load(loaderB, "org.eclipse.core.tests.internal.plugin.c.api.ApiClass", true, "8.3");
	load(loaderB, "org.eclipse.core.tests.internal.plugin.d.api.ApiClass", true, "8.4");
	load(loaderB, "org.eclipse.core.tests.internal.plugin.a.PluginClass", false, "8.5");
	load(loaderB, "org.eclipse.core.tests.internal.plugin.b.PluginClass", true, "8.6");
	load(loaderB, "org.eclipse.core.tests.internal.plugin.c.PluginClass", false, "8.7");
	load(loaderB, "org.eclipse.core.tests.internal.plugin.d.PluginClass", false, "8.8");
	load(loaderB, "org.eclipse.core.tests.internal.plugin.c.ExportedClass", true, "8.9");

	load(loaderA, "org.eclipse.core.tests.internal.plugin.a.api.ApiClass", true, "9.1");
	load(loaderA, "org.eclipse.core.tests.internal.plugin.b.api.ApiClass", true, "9.2");
	load(loaderA, "org.eclipse.core.tests.internal.plugin.c.api.ApiClass", true, "9.3");
	load(loaderA, "org.eclipse.core.tests.internal.plugin.d.api.ApiClass", false, "9.4");
	load(loaderA, "org.eclipse.core.tests.internal.plugin.a.PluginClass", true, "9.5");
	load(loaderA, "org.eclipse.core.tests.internal.plugin.b.PluginClass", true, "9.6");
	load(loaderA, "org.eclipse.core.tests.internal.plugin.c.PluginClass", false, "9.7");
	load(loaderA, "org.eclipse.core.tests.internal.plugin.d.PluginClass", false, "9.8");
	load(loaderA, "org.eclipse.core.tests.internal.plugin.c.ExportedClass", true, "9.9");

	load(null, "org.eclipse.core.tests.internal.plugin.a.api.ApiClass", true, "10.1");
	load(null, "org.eclipse.core.tests.internal.plugin.b.api.ApiClass", true, "10.2");
	load(null, "org.eclipse.core.tests.internal.plugin.c.api.ApiClass", true, "10.3");
	load(null, "org.eclipse.core.tests.internal.plugin.d.api.ApiClass", true, "10.4");
	load(null, "org.eclipse.core.tests.internal.plugin.a.PluginClass", true, "10.5");
	load(null, "org.eclipse.core.tests.internal.plugin.b.PluginClass", true, "10.6");
	load(null, "org.eclipse.core.tests.internal.plugin.c.PluginClass", true, "10.7");
	load(null, "org.eclipse.core.tests.internal.plugin.d.PluginClass", true, "10.8");
	load(null, "org.eclipse.core.tests.internal.plugin.c.ExportedClass", true, "10.9");

}
public String getResource(Object o, String name, boolean success, String msg) {

	String result = null;
	InputStream content;
	byte[] buf;

	URL url;
	if (o instanceof Class)
		url = ((Class) o).getResource(name);
	else
		url = ((ClassLoader) o).getResource(name);

	if (success) {
		assertTrue(msg + "0", url != null);
		try {
			content = url.openStream();
			buf = new byte[content.available()];
			content.read(buf);
			content.close();
			result = (new String(buf)).trim();
		} catch (IOException ea) {
			fail(msg + "1 " + ea);
		}
	} else
		assertTrue(msg + "2", url == null);

	return result;
}
public Vector getResources(ClassLoader loader, String name, String msg) {

	Vector result = new Vector();
	Enumeration enum;
	String file = null;
	String tmp = null;
	try {
		enum = loader.getResources(name);
		while (enum.hasMoreElements()) {
			file = ((URL) enum.nextElement()).getFile();
			tmp = file.substring(0, file.length() - name.length() - 1);
			tmp = tmp.substring(tmp.length() - 1, tmp.length());
			result.add(tmp);
		}
	} catch (IOException e) {
		fail(msg + "0" + e.getMessage());
	}
	return result;
}
public void getResourcesTest() {

	String path = "org.eclipse.core.tests.internal.plugins";
	String clazz = path + ".TestClass";
	String res = "test.txt";
	String absres = path.replace('.', '/') + "/" + res;

	IPluginRegistry registry = InternalPlatform.getPluginRegistry();

	// get descriptors
	IPluginDescriptor pluginA = registry.getPluginDescriptor("plugin.a");
	IPluginDescriptor pluginB = registry.getPluginDescriptor("plugin.b");
	IPluginDescriptor pluginC = registry.getPluginDescriptor("plugin.c");
	IPluginDescriptor pluginD = registry.getPluginDescriptor("plugin.d");
	assertNotNull("0.0", pluginA);
	assertNotNull("0.1", pluginB);
	assertNotNull("0.2", pluginC);
	assertNotNull("0.3", pluginD);

	// get loaders
	Plugin p = null;
	ClassLoader platform = Platform.class.getClassLoader();

	try {
		p = pluginA.getPlugin();
	} catch (CoreException e) {
		fail("2.0");
	}
	ClassLoader loaderA = p.getClass().getClassLoader();

	try {
		p = pluginB.getPlugin();
	} catch (CoreException e) {
		fail("3.0");
	}
	ClassLoader loaderB = p.getClass().getClassLoader();

	try {
		p = pluginC.getPlugin();
	} catch (CoreException e) {
		fail("4.0");
	}
	ClassLoader loaderC = p.getClass().getClassLoader();

	try {
		p = pluginD.getPlugin();
	} catch (CoreException e) {
		fail("5.0");
	}
	ClassLoader loaderD = p.getClass().getClassLoader();

	// load resources for A
	Vector v;
	v = getResources(loaderA, absres, "6.0.");
	assertTrue("6.1", v.size() == 1);
	assertTrue("6.2", v.contains("C"));

	// load resources for B
	v = getResources(loaderB, absres, "7.0.");
	assertTrue("7.1", v.size() == 3);
	assertTrue("7.2", v.contains("B"));
	assertTrue("7.3", v.contains("C"));
	assertTrue("7.4", v.contains("D"));

	// load resources for C
	v = getResources(loaderC, absres, "8.0.");
	assertTrue("8.1", v.size() == 1);
	assertTrue("8.2", v.contains("C"));

	// load resources for D
	v = getResources(loaderD, absres, "9.0.");
	assertTrue("9.1", v.size() == 1);
	assertTrue("9.2", v.contains("D"));

	// load resources for platform
	v = getResources(platform, absres, "9.0.");
	assertTrue("9.1", v.size() == 0);

}
public void getResourceTest() {

	String path = "org.eclipse.core.tests.internal.plugins";
	String clazz = path + ".TestClass";
	String res = "test.txt";
	String absres = path.replace('.', '/') + "/" + res;

	IPluginRegistry registry = InternalPlatform.getPluginRegistry();

	// get descriptors
	IPluginDescriptor pluginA = registry.getPluginDescriptor("plugin.a");
	IPluginDescriptor pluginB = registry.getPluginDescriptor("plugin.b");
	IPluginDescriptor pluginC = registry.getPluginDescriptor("plugin.c");
	IPluginDescriptor pluginD = registry.getPluginDescriptor("plugin.d");
	assertNotNull("0.0", pluginA);
	assertNotNull("0.1", pluginB);
	assertNotNull("0.2", pluginC);
	assertNotNull("0.3", pluginD);

	// get plugin loaders
	Plugin p = null;

	try {
		p = pluginA.getPlugin();
	} catch (CoreException e) {
		fail("2.0");
	}
	ClassLoader loaderA = p.getClass().getClassLoader();

	try {
		p = pluginB.getPlugin();
	} catch (CoreException e) {
		fail("3.0");
	}
	ClassLoader loaderB = p.getClass().getClassLoader();

	try {
		p = pluginC.getPlugin();
	} catch (CoreException e) {
		fail("4.0");
	}
	ClassLoader loaderC = p.getClass().getClassLoader();

	try {
		p = pluginD.getPlugin();
	} catch (CoreException e) {
		fail("5.0");
	}
	ClassLoader loaderD = p.getClass().getClassLoader();

	Class test = load(loaderA, clazz, true, "6.0");
	String text = null;

	// load resources relative to class from plugin A

	text = getResource(test, res, true, "7.0.");
	assertTrue("7.1", text.equals("Text from Plugin C")); // delegated

	// load resources directly

	text = getResource(loaderA, absres, true, "8.0.");
	assertTrue("8.1", text.equals("Text from Plugin C")); // delegated

	text = getResource(loaderB, absres, true, "9.0.");
	assertTrue("9.1", text.equals("Text from Plugin B"));

	text = getResource(loaderC, absres, true, "10.0.");
	assertTrue("10.1", text.equals("Text from Plugin C"));

	text = getResource(loaderD, absres, true, "11.0.");
	assertTrue("11.1", text.equals("Text from Plugin D"));

	text = getResource(Platform.class.getClassLoader(), absres, false, "12.0.");
}
public Class load(ClassLoader loader, String name, boolean success, String msg) {

	Class result = null;
	try {
		if (loader == null)
			result = Class.forName(name);
		else
			result = loader.loadClass(name);
		if (!success)
			fail(msg + ".0");
	} catch (ClassNotFoundException e) {
		if (success)
			// We should have found this class
			fail(msg + ".1");
		// else
			// We were expecting not to find this class so don't fail
	} catch (Exception e) {
		fail(msg + ".2" + e.getMessage());
	}
	return result;
}
//public static Test suite() {
//	TestSuite suite = new TestSuite();
//	suite.addTest(new LoaderTest("baseTest"));
////	suite.addTest(new LoaderTest("xmlTest"));
////	suite.addTest(new LoaderTest("getResourceTest"));
////	suite.addTest(new LoaderTest("getResourcesTest"));
//	return suite;
//}
public void xmlTest() {

	// xml class
	String xml = "org.apache.xerces.framework.XMLParser";

	// get platform loader (has private copy of xml4j
	IPluginRegistry registry = InternalPlatform.getPluginRegistry();
	ClassLoader platformLoader = Platform.class.getClassLoader();
	assertTrue("0.0", platformLoader instanceof org.eclipse.core.internal.boot.PlatformClassLoader);

	// get test plugin A (requires com.ibm.xml)
	Plugin p = null;
	IPluginDescriptor pd;
	IPluginDescriptor pdA = registry.getPluginDescriptor("plugin.a");
	assertTrue("0.1.0", pdA != null);
	try {
		p = pdA.getPlugin();
	} catch (CoreException e) {
		fail("0.1.1.0");
	}
	assertTrue("0.1.2", pdA.isPluginActivated());
	ClassLoader a = p.getClass().getClassLoader();
	assertTrue("0.1.3", a instanceof org.eclipse.core.internal.plugins.PluginClassLoader);

	// get test plugin D (contains unexported xml4j)
	IPluginDescriptor pdD = registry.getPluginDescriptor("plugin.d");
	assertTrue("0.2.0", pdD != null);
	try {
		p = pdD.getPlugin();
	} catch (CoreException e) {
		fail("0.2.1.0");
	}
	assertTrue("0.2.2", pdD.isPluginActivated());
	ClassLoader d = p.getClass().getClassLoader();
	assertTrue("0.2.3", d instanceof org.eclipse.core.internal.plugins.PluginClassLoader);

	// load class
	Class platformXML = load(platformLoader, xml, true, "1.0");
	Class aXML = load(a, xml, true, "1.1");
	Class dXML = load(d, xml, true, "1.2");

	if (!InternalPlatform.inVAJ()) {
		// jdk mode
		assertTrue("2.0", platformXML != aXML);
		assertTrue("2.1", platformXML != dXML);
		assertTrue("2.2", aXML != dXML);
		assertTrue("2.3", platformXML.getClassLoader() == platformLoader);
		assertTrue("2.4", aXML.getClassLoader() != a); // loaded by prereq com.ibm.xml loader
		assertTrue("2.5", dXML.getClassLoader() == d);
	} else {
		// va mode
		// in VA/Java there is realy only one class cache. All loaders return
		// the same underlying class (even though it was defined multiple
		// times. The class loader associated with the class is the last
		// loader that did a defineClass(...) for this class (ie. the answer
		// to getClassLoader() for a multiply-defined class changes 
		// as the program executes).
		assertTrue("3.0", platformXML == aXML);
		assertTrue("3.1", platformXML == dXML);
		assertTrue("3.2", aXML == dXML);
	}
}
}
