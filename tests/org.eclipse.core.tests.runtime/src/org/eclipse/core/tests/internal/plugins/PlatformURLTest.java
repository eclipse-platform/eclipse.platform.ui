package org.eclipse.core.tests.internal.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.*;

import org.eclipse.core.internal.boot.PlatformURLConnection;
import org.eclipse.core.internal.boot.PlatformURLHandler;
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

	// check locale (test files setup for en_US)
	assertTrue(Locale.getDefault().toString().equals("en_CA"));

	// resource strings - no lookup
	String s1 = "Hello World";
	String s2 = "%%" + s1;

	assertTrue("2.0", pluginA.getResourceString(s1).equals(s1));
	assertTrue("2.1", pluginA.getResourceString(s2).equals(s2.substring(1)));

	// resource strings - default bundle lookup
	String s3 = "Test String";
	String key = "%key";
	String bad = "%bad";

	assertTrue("3.0", pluginA.getResourceString(key).equals(s3));
	assertTrue("3.1", pluginB.getResourceString(key).equals(s3 + " en"));
	assertTrue("3.2", pluginC.getResourceString(key).equals(s3 + " en_CA"));
	assertTrue("3.3", pluginD.getResourceString(key).equals(key));

	assertTrue("4.0", pluginA.getResourceString(bad).equals(bad));
	assertTrue("4.1", pluginB.getResourceString(bad).equals(bad));
	assertTrue("4.2", pluginC.getResourceString(bad).equals(bad));
	assertTrue("4.3", pluginD.getResourceString(bad).equals(bad));

	assertTrue("5.0", pluginA.getResourceString(bad + " " + s1).equals(s1));
	assertTrue("5.1", pluginB.getResourceString(bad + " " + s1).equals(s1));
	assertTrue("5.2", pluginC.getResourceString(bad + " " + s1).equals(s1));
	assertTrue("5.3", pluginD.getResourceString(bad + " " + s1).equals(s1));

	assertTrue("6.0", pluginA.getResourceString(key + " " + s1).equals(s3));
	assertTrue("6.1", pluginB.getResourceString(key + " " + s1).equals(s3 + " en"));
	assertTrue("6.2", pluginC.getResourceString(key + " " + s1).equals(s3 + " en_CA"));
	assertTrue("6.3", pluginD.getResourceString(key + " " + s1).equals(s1));

	// resource strings - specified bundle
	ClassLoader loader = new URLClassLoader(new URL[] { pluginD.getInstallURL()}, null);
	ResourceBundle bundle = null;
	try {
		bundle = ResourceBundle.getBundle("resource", Locale.getDefault(), loader);
	} catch (MissingResourceException e) {
		fail("7.0.1");
	};
	assertTrue("7.0.2", bundle != null);

	assertTrue("7.1", pluginA.getResourceString(s1, bundle).equals(s1));
	assertTrue("7.2", pluginA.getResourceString(s2, bundle).equals(s2.substring(1)));
	assertTrue("7.3", pluginA.getResourceString(key, bundle).equals("resource.properties " + s3));
	assertTrue("7.4", pluginA.getResourceString(bad, bundle).equals(bad));
	assertTrue("7.5", pluginA.getResourceString(bad + " " + s1, bundle).equals(s1));
	assertTrue("7.6", pluginA.getResourceString(key + " " + s1, bundle).equals("resource.properties " + s3));

	assertTrue("8.1", pluginB.getResourceString(s1, bundle).equals(s1));
	assertTrue("8.2", pluginB.getResourceString(s2, bundle).equals(s2.substring(1)));
	assertTrue("8.3", pluginB.getResourceString(key, bundle).equals("resource.properties " + s3));
	assertTrue("8.4", pluginB.getResourceString(bad, bundle).equals(bad));
	assertTrue("8.5", pluginB.getResourceString(bad + " " + s1, bundle).equals(s1));
	assertTrue("8.6", pluginB.getResourceString(key + " " + s1, bundle).equals("resource.properties " + s3));

	assertTrue("9.1", pluginC.getResourceString(s1, bundle).equals(s1));
	assertTrue("9.2", pluginC.getResourceString(s2, bundle).equals(s2.substring(1)));
	assertTrue("9.3", pluginC.getResourceString(key, bundle).equals("resource.properties " + s3));
	assertTrue("9.4", pluginC.getResourceString(bad, bundle).equals(bad));
	assertTrue("9.5", pluginC.getResourceString(bad + " " + s1, bundle).equals(s1));
	assertTrue("9.6", pluginC.getResourceString(key + " " + s1, bundle).equals("resource.properties " + s3));

	assertTrue("10.1", pluginD.getResourceString(s1, bundle).equals(s1));
	assertTrue("10.2", pluginD.getResourceString(s2, bundle).equals(s2.substring(1)));
	assertTrue("10.3", pluginD.getResourceString(key, bundle).equals("resource.properties " + s3));
	assertTrue("10.4", pluginD.getResourceString(bad, bundle).equals(bad));
	assertTrue("10.5", pluginD.getResourceString(bad + " " + s1, bundle).equals(s1));
	assertTrue("10.6", pluginD.getResourceString(key + " " + s1, bundle).equals("resource.properties " + s3));

}
}
