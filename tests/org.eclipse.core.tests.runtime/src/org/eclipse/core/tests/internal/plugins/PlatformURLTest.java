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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.*;
import java.util.*;

import org.eclipse.core.internal.boot.InternalBootLoader;
import org.eclipse.core.internal.boot.PlatformURLConnection;
import org.eclipse.core.internal.boot.PlatformURLHandler;
import org.eclipse.core.internal.plugins.PluginDescriptor;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.WorkspaceSessionTest;
/**
 */
public class PlatformURLTest extends WorkspaceSessionTest {
public PlatformURLTest() {
	super(null);
}
public PlatformURLTest(String name) {
	super(name);
}
public URL createURL(String id, String spec, boolean success) {

	URL url = null;

	try {
		url = new URL(spec);
		if (!success)
			fail(id + ".0 " + url.toString());
	} catch (MalformedURLException e) {
		if (success)
			fail(id + ".1 " + e);
	}

	return url;

}
public URL createURL(String id, URL base, String spec, boolean success) {

	URL url = null;

	try {
		url = new URL(base, spec);
		if (!success)
			fail(id + ".0 " + url.toString());
	} catch (MalformedURLException e) {
		if (success)
			fail(id + ".1 " + e);
	}

	return url;

}
public void testPlatformURLLoader() {

	IPluginRegistry registry = InternalPlatform.getPluginRegistry();

	// get descriptors
	IPluginDescriptor pd = registry.getPluginDescriptor("plugin.a");
	assertNotNull("0.0", pd);

	URL path = null;
	try {
		path = new URL(pd.getInstallURL(), "plugin_a_external.jar!/");
	} catch (MalformedURLException e) {
		fail("1.0 " + e);
	};

	// load a class from a jar
	ClassLoader l = new URLClassLoader(new URL[] { path }, null);
	Class c = null;
	try {
		c = l.loadClass("org.eclipse.core.tests.internal.plugin.a.Dummy");
	} catch (ClassNotFoundException e) {
		fail("2.0 " + e);
	};
	URL src = c.getProtectionDomain().getCodeSource().getLocation();

	// do it again using new loader
	l = new URLClassLoader(new URL[] { path }, null);
	c = null;
	try {
		c = l.loadClass("org.eclipse.core.tests.internal.plugin.a.Dummy");
	} catch (ClassNotFoundException e) {
		fail("3.0 " + e);
	};
	src = c.getProtectionDomain().getCodeSource().getLocation();

	c = null;

}
public void testPlatformURLResolve() {

	IPluginRegistry registry = InternalPlatform.getPluginRegistry();

	// get descriptors
	IPluginDescriptor pluginA = registry.getPluginDescriptor("plugin.a");
	IPluginDescriptor pluginB = registry.getPluginDescriptor("plugin.b");
	IPluginDescriptor pluginC = registry.getPluginDescriptor("plugin.c");
	IPluginDescriptor pluginD = registry.getPluginDescriptor("plugin.d" );
	assertNotNull("0.0", pluginA);
	assertNotNull("0.1", pluginB);
	assertNotNull("0.2", pluginC);
	assertNotNull("0.3", pluginD);

	URL install = null;
	URL url = null;
	URL resolvedURL = null;
	URL resolvedURL2 = null;
	URL localURL = null;
	String result = null;

	// resolve/ asLocalURL for plugin directory URL
	install = url = pluginA.getInstallURL();
	assertNotNull("1.0.0", url);
	assertTrue("1.0.1", url.getProtocol().equals(PlatformURLHandler.PROTOCOL));

	try {
		resolvedURL = Platform.resolve(url);
	} catch (IOException e) {
		fail("1.1.0 " + e);
	};
	assertNotNull("1.1.1", resolvedURL);
	assertTrue("1.1.2", !resolvedURL.getProtocol().equals(PlatformURLHandler.PROTOCOL));

	try {
		resolvedURL2 = Platform.resolve(resolvedURL);
	} catch (IOException e) {
		fail("1.2.0 " + e);
	};
	assertTrue("1.2.1", resolvedURL2.equals(resolvedURL));

	try {
		localURL = Platform.asLocalURL(url);
		fail("1.3.0");
	} catch (IOException e) {
	};

	// resolve/ asLocalURL for file URL

	try {
		url = new URL(install, "plugin.xml");
	} catch (java.io.IOException e) {
		fail("2.0.0.0 " + e);
	}
	assertNotNull("2.0.0", url);
	assertTrue("2.0.1", url.getProtocol().equals(PlatformURLHandler.PROTOCOL));

	try {
		resolvedURL = Platform.resolve(url);
	} catch (IOException e) {
		fail("2.1.0 " + e);
	};
	assertNotNull("2.1.1", resolvedURL);
	assertTrue("2.1.2", !resolvedURL.getProtocol().equals(PlatformURLHandler.PROTOCOL));

	try {
		resolvedURL2 = Platform.resolve(resolvedURL);
	} catch (IOException e) {
		fail("2.2.0 " + e);
	};
	assertTrue("2.2.1", resolvedURL2.equals(resolvedURL));

	try {
		localURL = Platform.asLocalURL(url);
	} catch (IOException e) {
		fail("2.3.0 " + e);
	};
	assertTrue("2.3.1", localURL.getProtocol().equals(PlatformURLHandler.FILE));

	// resolve/ asLocalURL for jar URL 

	try {
		url = new URL(install, "plugin_a_external.jar!/");
	} catch (java.io.IOException e) {
		fail("3.0.0.0 " + e);
	}
	assertNotNull("3.0.0", url);
	assertTrue("3.0.1", url.getProtocol().equals(PlatformURLHandler.PROTOCOL));

	try {
		resolvedURL = Platform.resolve(url);
	} catch (IOException e) {
		fail("3.1.0 " + e);
	};
	assertNotNull("3.1.1", resolvedURL);
	assertTrue("3.1.2", !resolvedURL.getProtocol().equals(PlatformURLHandler.PROTOCOL));

	try {
		resolvedURL2 = Platform.resolve(resolvedURL);
	} catch (IOException e) {
		fail("3.2.0 " + e);
	};
	assertTrue("3.2.1", resolvedURL2.equals(resolvedURL));

	try {
		localURL = Platform.asLocalURL(url);
	} catch (IOException e) {
		fail("3.3.0 " + e);
	};
	assertTrue("3.3.1", localURL.getProtocol().equals(PlatformURLHandler.JAR));

	// resolve/ asLocalURL for class inside jar URL 

	try {
		url = new URL(install, "plugin_a_external.jar!/org/eclipse/core/tests/internal/plugin/a/DummyClass.class");
	} catch (java.io.IOException e) {
		fail("4.0.0.0 " + e);
	}
	assertNotNull("4.0.0", url);
	assertTrue("4.0.1", url.getProtocol().equals(PlatformURLHandler.PROTOCOL));

	try {
		resolvedURL = Platform.resolve(url);
	} catch (IOException e) {
		fail("4.1.0 " + e);
	};
	assertNotNull("4.1.1", resolvedURL);
	assertTrue("4.1.2", !resolvedURL.getProtocol().equals(PlatformURLHandler.PROTOCOL));

	try {
		resolvedURL2 = Platform.resolve(resolvedURL);
	} catch (IOException e) {
		fail("4.2.0 " + e);
	};
	assertTrue("4.2.1", resolvedURL2.equals(resolvedURL));

	try {
		localURL = Platform.asLocalURL(url);
	} catch (IOException e) {
		fail("4.3.0 " + e);
	};
	assertTrue("4.3.1", localURL.getProtocol().equals(PlatformURLHandler.JAR));

}
public void testPlatformURL() {

	IPluginRegistry registry = InternalPlatform.getPluginRegistry();

	// get descriptors
	IPluginDescriptor pluginA = registry.getPluginDescriptor("plugin.a");
	IPluginDescriptor pluginB = registry.getPluginDescriptor("plugin.b");
	IPluginDescriptor pluginC = registry.getPluginDescriptor("plugin.c");
	IPluginDescriptor pluginD = registry.getPluginDescriptor("plugin.d" );
	assertNotNull("0.0", pluginA);
	assertNotNull("0.1", pluginB);
	assertNotNull("0.2", pluginC);
	assertNotNull("0.3", pluginD);

	URL url = null;
	String result = null;

	// bad protocol spec "badurl"
	url = createURL("1.0", "platform:badurl/" + "plugin.a" + "/", true);
	result = getResourceStringFromURL("1.1", url, "key", false);

	// nonexistent plugin
	url = createURL("2.0", "platform:plugin/some.bad.plugin/", true);
	result = getResourceStringFromURL("2.1", url, "key", false);

	// good plugin
	url = createURL("3.0", "platform:plugin/" + "plugin.a" + "/", true);
	url = createURL("3.1", url, "plugin.properties", true);
	result = getResourceStringFromURL("3.2", url, "key", true);
	assertNotNull("3.3", result);
	assertTrue("3.4", result.equals("Test String"));

	// good plugin from cache
	url = createURL("4.0", "platform:plugin/" + "plugin.a" + "/", true);
	url = createURL("4.1", url, "plugin.properties", true);
	PlatformURLConnection euc = null;
	try {
		euc = (PlatformURLConnection) url.openConnection();
	} catch (IOException e) {
		fail("4.1.1 " + e);
	}
	try {
		url = euc.getURLAsLocal();
	} catch (IOException e) {
		fail("4.1.2 " + e);
	}
	result = getResourceStringFromURL("4.4", url, "key", true);
	assertNotNull("4.3", result);
	assertTrue("4.4", result.equals("Test String"));
}
public String getResourceStringFromConnection(String id, URLConnection uc, String key, boolean success) {

	InputStream is = null;
	PropertyResourceBundle bundle = null;
	String result = null;

	try {
		try {
			is = uc.getInputStream();
			if (!success)
				fail(id + ".2 " + uc.getURL().toString());
		} catch (IOException e) {
			if (success)
				fail(id + ".3 " + e);
			return null;
		}

		try {
			bundle = new PropertyResourceBundle(is);
			if (!success)
				fail(id + ".4 " + uc.getURL().toString());
		} catch (IOException e) {
			if (success)
				fail(id + ".5 " + e);
			return null;
		}

		try {
			result = bundle.getString(key);
			if (!success)
				fail(id + ".6 " + uc.getURL().toString());
		} catch (MissingResourceException e) {
			if (success)
				fail(id + ".7 " + e);
			return null;
		}

	} finally {
		if (is != null)
			try {
				is.close();
			} catch (IOException e) {
			}
	}

	return result;

}
public String getResourceStringFromURL(String id, URL url, String key, boolean success) {

	URLConnection uc = null;
	InputStream is = null;
	PropertyResourceBundle bundle = null;
	String result = null;

	try {
		try {
			uc = url.openConnection();
			if (!success)
				fail(id + ".0 " + url.toString());
		} catch (IOException e) {
			if (success)
				fail(id + ".1 " + e);
			return null;
		}

		try {
			is = uc.getInputStream();
			if (!success)
				fail(id + ".2 " + url.toString());
		} catch (IOException e) {
			if (success)
				fail(id + ".3 " + e);
			return null;
		}

		try {
			bundle = new PropertyResourceBundle(is);
			if (!success)
				fail(id + ".4 " + url.toString());
		} catch (IOException e) {
			if (success)
				fail(id + ".5 " + e);
			return null;
		}

		try {
			result = bundle.getString(key);
			if (!success)
				fail(id + ".6 " + url.toString());
		} catch (MissingResourceException e) {
			if (success)
				fail(id + ".7 " + e);
			return null;
		}

	} finally {
		if (is != null)
			try {
				is.close();
			} catch (IOException e) {
			}
	}

	return result;

}
private void buildPropertyFile(String fileName, String fileData, URL fileLocation) {
	String fullFileName = fileLocation.getFile() + fileName;
	try {
		FileOutputStream fs = new FileOutputStream(fullFileName);
		PrintWriter w = new PrintWriter(fs);
		try {
			w.println(fileData);
			w.flush();
		} finally {
			w.close();
		}
	} catch (FileNotFoundException ioe) {
		fail ("Unable to create test file " + fullFileName);
	}

}
private void buildResourceEnv(IPluginDescriptor pluginB, IPluginDescriptor pluginC, String nl, String smallnl) {
	buildPropertyFile("plugin_" + nl + ".properties", "key = Test String from " + nl, 
		pluginC.find(new Path("./")));
	if (!smallnl.equals(nl))
		buildPropertyFile("plugin_" + smallnl + ".properties", "key = Test String from " + smallnl, 
			pluginC.find(new Path("./")));
	buildPropertyFile("plugin_" + smallnl + ".properties", "key = Test String from " + smallnl, 
		pluginB.find(new Path("./")));
}

private void cleanupResourceEnv(IPluginDescriptor pluginB, IPluginDescriptor pluginC, String nl, String smallnl) {
	File file = new File(pluginB.find(new Path("./")).getFile() + "plugin_" + smallnl + ".properties");
	file.delete();
	file = new File(pluginC.find(new Path("./")).getFile() + "plugin_" + nl + ".properties");
	file.delete();
	if (!smallnl.equals(nl)) {
		file = new File(pluginC.find(new Path("./")).getFile() + "plugin_" + smallnl + ".properties");
		file.delete();
	}
}

public void testGetResourceString() {

	IPluginRegistry registry = InternalPlatform.getPluginRegistry();

	// get descriptors
	IPluginDescriptor pluginA = registry.getPluginDescriptor("plugin.a");
	IPluginDescriptor pluginB = registry.getPluginDescriptor("plugin.b");
	IPluginDescriptor pluginC = registry.getPluginDescriptor("plugin.c");
	IPluginDescriptor pluginD = registry.getPluginDescriptor("plugin.d" );
	assertNotNull("0.0", pluginA);
	assertNotNull("0.1", pluginB);
	assertNotNull("0.2", pluginC);
	assertNotNull("0.3", pluginD);

	// We are making the assumption here that nl will have at 
	// most 2 segments
	String nl = InternalBootLoader.getNL();
	String smallnl = nl;
	int i = nl.lastIndexOf('_');
	if (i != -1) {
		smallnl = nl.substring(0, i);
	}
	
	// resource strings - no lookup
	String s1 = "Hello World";
	String s2 = "%%" + s1;
	// resource strings - default bundle lookup
	String s3 = "Test String";
	String key = "%key";
	String bad = "%bad";
	
	try {
		buildResourceEnv(pluginB, pluginC, nl, smallnl);
	
		assertEquals("2.0", s1, pluginA.getResourceString(s1));
		assertEquals("2.1", s2.substring(1), pluginA.getResourceString(s2));
	
		assertEquals("3.0", s3, pluginA.getResourceString(key));
		assertEquals("3.1", s3 + " from " + smallnl, pluginB.getResourceString(key));
		assertEquals("3.2", s3 + " from " + nl, pluginC.getResourceString(key));
		assertEquals("3.3", key, pluginD.getResourceString(key));
	
		assertEquals("4.0", bad, pluginA.getResourceString(bad));
		assertEquals("4.1", bad, pluginB.getResourceString(bad));
		assertEquals("4.2", bad, pluginC.getResourceString(bad));
		assertEquals("4.3", bad, pluginD.getResourceString(bad));
	
		assertEquals("5.0", s1, pluginA.getResourceString(bad + " " + s1));
		assertEquals("5.1", s1, pluginB.getResourceString(bad + " " + s1));
		assertEquals("5.2", s1, pluginC.getResourceString(bad + " " + s1));
		assertEquals("5.3", s1, pluginD.getResourceString(bad + " " + s1));
	
		assertEquals("6.0", s3, pluginA.getResourceString(key + " " + s1));
		assertEquals("6.1", s3 + " from " + smallnl, pluginB.getResourceString(key + " " + s1));
		assertEquals("6.2", s3 + " from " + nl, pluginC.getResourceString(key + " " + s1));
		assertEquals("6.3", s1, pluginD.getResourceString(key + " " + s1));
	} finally {
		cleanupResourceEnv(pluginB, pluginC, nl, smallnl);
	}

	// resource strings - specified bundle
	ClassLoader loader = new URLClassLoader(new URL[] { pluginD.getInstallURL()}, null);
	ResourceBundle bundle = null;
	try {
		bundle = ResourceBundle.getBundle("resource", Locale.getDefault(), loader);
	} catch (MissingResourceException e) {
		// We don't need to worry about the locale here.  We should always find
		// resource.properties
		fail("7.0.1");
	};
	assertNotNull("7.0.2", bundle);

	assertEquals("7.1", s1, pluginA.getResourceString(s1, bundle));
	assertEquals("7.2", s2.substring(1), pluginA.getResourceString(s2, bundle));
	assertEquals("7.3", "resource.properties " + s3, pluginA.getResourceString(key, bundle));
	assertEquals("7.4", bad, pluginA.getResourceString(bad, bundle));
	assertEquals("7.5", s1, pluginA.getResourceString(bad + " " + s1, bundle));
	assertEquals("7.6", "resource.properties " + s3, pluginA.getResourceString(key + " " + s1, bundle));

	assertEquals("8.1", s1, pluginB.getResourceString(s1, bundle));
	assertEquals("8.2", s2.substring(1), pluginB.getResourceString(s2, bundle));
	assertEquals("8.3", "resource.properties " + s3, pluginB.getResourceString(key, bundle));
	assertEquals("8.4", bad, pluginB.getResourceString(bad, bundle));
	assertEquals("8.5", s1, pluginB.getResourceString(bad + " " + s1, bundle));
	assertEquals("8.6", "resource.properties " + s3, pluginB.getResourceString(key + " " + s1, bundle));

	assertEquals("9.1", s1, pluginC.getResourceString(s1, bundle));
	assertEquals("9.2", s2.substring(1), pluginC.getResourceString(s2, bundle));
	assertEquals("9.3", "resource.properties " + s3, pluginC.getResourceString(key, bundle));
	assertEquals("9.4", bad, pluginC.getResourceString(bad, bundle));
	assertEquals("9.5", s1, pluginC.getResourceString(bad + " " + s1, bundle));
	assertEquals("9.6", "resource.properties " + s3, pluginC.getResourceString(key + " " + s1, bundle));

	assertEquals("10.1", s1, pluginD.getResourceString(s1, bundle));
	assertEquals("10.2", s2.substring(1), pluginD.getResourceString(s2, bundle));
	assertEquals("10.3", "resource.properties " + s3, pluginD.getResourceString(key, bundle));
	assertEquals("10.4", bad, pluginD.getResourceString(bad, bundle));
	assertEquals("10.5", s1, pluginD.getResourceString(bad + " " + s1, bundle));
	assertEquals("10.6", "resource.properties " + s3, pluginD.getResourceString(key + " " + s1, bundle));

}
}
