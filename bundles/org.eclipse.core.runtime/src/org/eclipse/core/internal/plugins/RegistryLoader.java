package org.eclipse.core.internal.plugins;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
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
	long thisTick = (new java.util.Date()).getTime();
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
	long startTick = 0;
	if (debug) {
		startTick = (new java.util.Date()).getTime();
		debug("Begin");
	}
	PluginRegistryModel result = processManifestFiles(pluginPath);
	if (debug) {
		long endTick = (new java.util.Date()).getTime();
		debug("End" + (startTick != 0 ? ": total " + (endTick - startTick) + "ms" : ""));
	}
	return result;
}
public static PluginRegistryModel parseRegistry(URL[] pluginPath, Factory factory, boolean debug) {
	return new RegistryLoader(factory, debug).parseRegistry(pluginPath);
}
private PluginDescriptorModel processManifestFile(URL manifest) {
	InputStream is = null;
	try {
		is = manifest.openStream();
	} catch (IOException e) {
		factory.error(new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, "No plugin found for: " + manifest, null));
		if (debug)
			debug("No plugin found for: " + manifest);
		return null;
	}
	PluginDescriptorModel result = null;
	try {
		try {
			result = new PluginParser((Factory) factory).parse(new InputSource(is));
			String url = manifest.toString();
			result.setLocation(url.substring(0, 1 + url.lastIndexOf('/')));
		} finally {
			is.close();
		}
	} catch (SAXParseException se) {
		/* exception details logged by parser */
		factory.error(new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("errorProcessing", new String[] { manifest.toString()}), null));
	} catch (Exception e) {
		factory.error(new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, Policy.bind("errorProcessing", new String[] { manifest.toString()}), null));
	}
	return result;
}
private PluginRegistryModel processManifestFiles(URL[] pluginPath) {
	PluginRegistryModel result = factory.createPluginRegistry();
	for (int i = 0; i < pluginPath.length; i++) {
		if (debug)
			debug("Path - " + pluginPath[i]);
		if (pluginPath[i].getFile().endsWith("/")) {
			// directory entry - search for plugins
			String[] members = getPathMembers(pluginPath[i]);
			for (int j = 0; j < members.length; j++) {
				try {
					PluginDescriptorModel entry = processManifestFile(new URL(pluginPath[i], members[j] + "/plugin.xml"));
					if (entry != null) {
						result.addPlugin(entry);
						entry.setRegistry(result);
					}
				} catch (java.net.MalformedURLException e) {
				}
				if (debug)
					debug("Processed - " + members[j]);
			}
		} else {
			// specific file entry - load the given file
			PluginDescriptorModel entry = processManifestFile(pluginPath[i]);
			if (entry != null) {
				result.addPlugin(entry);
				entry.setRegistry(result);
			}
			if (debug)
				debug("Processed - " + pluginPath[i]);
		}
	}
	return result;
}
}
