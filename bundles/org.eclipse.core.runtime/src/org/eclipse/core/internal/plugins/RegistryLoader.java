package org.eclipse.core.internal.plugins;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.model.*;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.Policy;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

public class RegistryLoader {
	private Factory factory;

	// debug support
	private boolean debug = false;
	private long startTick = (new java.util.Date()).getTime(); // used for performance timings
	private long lastTick = startTick;
private RegistryLoader(Factory factory, boolean debug) {
	super();
	this.debug = debug;
	this.factory = factory;
}
private void debug(String msg) {
	long thisTick = System.currentTimeMillis();
	System.out.println("RegistryLoader: " + msg + " [+"+ (thisTick - lastTick) + "ms]");
	lastTick = thisTick;
}
private String[] getPathMembers(URL path) {
	String[] list = null;
	String protocol = path.getProtocol();
	if (protocol.equals("file") || (InternalPlatform.inVAJ() && protocol.equals("valoader"))) {
		list = (new File(path.getFile())).list();
	} else {
		// XXX: attempt to read URL and see if we got html dir page
	}
	return list == null ? new String[0] : list;
}
private PluginRegistryModel parseRegistry(URL[] pluginPath) {
	long startTick = System.currentTimeMillis();
	PluginRegistryModel result = processManifestFiles(pluginPath);
	if (InternalPlatform.DEBUG) {
		long endTick = System.currentTimeMillis();
		debug("Parsed Registry: " + (endTick - startTick) + "ms");
	}
	return result;
}
public static PluginRegistryModel parseRegistry(URL[] pluginPath, Factory factory, boolean debug) {
	return new RegistryLoader(factory, debug).parseRegistry(pluginPath);
}
private PluginModel processManifestFile(URL manifest) {
	InputStream is = null;
	try {
		is = manifest.openStream();
	} catch (IOException e) {
		if (debug)
			debug("No plugin found for: " + manifest);
		return null;
	}
	PluginModel result = null;
	try {
		try {
			result = new PluginParser((Factory) factory).parsePlugin(new InputSource(is));
		} finally {
			is.close();
		}
	} catch (SAXParseException se) {
		/* exception details logged by parser */
		factory.error(new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("parse.errorProcessing", manifest.toString()), null));
	} catch (Exception e) {
		factory.error(new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("parse.errorProcessing", manifest.toString()), null));
	}
	return result;
}
private PluginRegistryModel processManifestFiles(URL[] pluginPath) {
	PluginRegistryModel result = factory.createPluginRegistry();
	for (int i = 0; i < pluginPath.length; i++)
		processPluginPathEntry(result, pluginPath[i]);
	return result;
}
private void processPluginPathEntry(PluginRegistryModel registry, URL location) {
	if (debug)
		debug("Path - " + location);
	if (location.getFile().endsWith("/")) {
		// directory entry - search for plugins
		String[] members = getPathMembers(location);
		for (int j = 0; j < members.length; j++) {
			try {
				boolean found = processPluginPathFile(registry, new URL(location, members[j] + "/plugin.xml"));
				if (!found)
					found = processPluginPathFile(registry, new URL(location, members[j] + "/fragment.xml"));
			} catch (MalformedURLException e) {
			}
			if (debug)
				debug("Processed - " + members[j]);
		}
	} else {
		// specific file entry - load the given file
		boolean found = processPluginPathFile(registry, location);
		if (debug)
			debug("Processed - " + location);
	}
}
private boolean processPluginPathFile(PluginRegistryModel registry, URL location) {
	PluginModel entry = processManifestFile(location);
	if (entry == null)
		return false;

	String url = location.toString();
	url = url.substring(0, 1 + url.lastIndexOf('/'));
	if (entry instanceof PluginDescriptorModel)
		registry.addPlugin((PluginDescriptorModel) entry);
	else
		if (entry instanceof PluginFragmentModel)
			registry.addFragment((PluginFragmentModel) entry);
		else
			// XXX log some kind of error or throw an exception here
			return false;
	entry.setRegistry(registry);
	entry.setLocation(url);
	return true;
}
}
