/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.core.internal.model;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.Messages;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.model.*;
import org.eclipse.osgi.util.NLS;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

public class RegistryLoader {
	private Factory factory;

	// debug support
	private boolean debug = false;
	private long lastTick = System.currentTimeMillis();//used for performance timing

	private RegistryLoader(Factory factory, boolean debug) {
		super();
		this.debug = debug;
		this.factory = factory;
	}

	private void debug(String msg) {
		long thisTick = System.currentTimeMillis();
		System.out.println("RegistryLoader: " + msg + " [+" + (thisTick - lastTick) + "ms]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		lastTick = thisTick;
	}

	private String[] getPathMembers(URL path) {
		String[] list = null;
		String protocol = path.getProtocol();
		if (protocol.equals("file")) { //$NON-NLS-1$
			list = (new File(path.getFile())).list();
		} else {
			// XXX: attempt to read URL and see if we got html dir page
		}
		return list == null ? new String[0] : list;
	}

	/**
	 * Reports an error and returns true.
	 */
	private boolean parseProblem(String message) {
		factory.error(new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, message, null));
		return true;
	}

	private PluginRegistryModel parseRegistry(URL[] pluginPath) {
		long startTick = System.currentTimeMillis();
		PluginRegistryModel result = processManifestFiles(pluginPath);
		if (InternalPlatform.DEBUG) {
			long endTick = System.currentTimeMillis();
			debug("Parsed Registry: " + (endTick - startTick) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
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
				debug("No plugin found for: " + manifest); //$NON-NLS-1$
			return null;
		}
		PluginModel result = null;
		try {
			try {
				InputSource in = new InputSource(is);
				// Give the system id a value in case we want it for
				// error reporting within the parser.
				in.setSystemId(manifest.getFile());
				result = new PluginParser((Factory) factory).parsePlugin(in);
			} finally {
				is.close();
			}
		} catch (SAXParseException se) {
			/* exception details logged by parser */
			factory.error(new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, NLS.bind(Messages.parse_errorProcessing, manifest), null));
		} catch (Exception e) {
			factory.error(new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, NLS.bind(Messages.parse_errorProcessing, manifest + ":  " + e.getMessage()), null)); //$NON-NLS-1$
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
			debug("Path - " + location); //$NON-NLS-1$
		if (location.getFile().endsWith("/")) { //$NON-NLS-1$
			// directory entry - search for plugins
			String[] members = getPathMembers(location);
			for (int j = 0; j < members.length; j++) {
				boolean found = false;
				try {
					found = processPluginPathFile(registry, new URL(location, members[j] + "/plugin.xml")); //$NON-NLS-1$
					if (!found)
						found = processPluginPathFile(registry, new URL(location, members[j] + "/fragment.xml")); //$NON-NLS-1$
				} catch (MalformedURLException e) {
					// Skip bad URLs
				}
				if (debug)
					debug(found ? "Processed - " : "Processed (not found) - " + members[j]); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} else {
			// specific file entry - load the given file
			boolean found = processPluginPathFile(registry, location);
			if (debug)
				debug(found ? "Processed - " : "Processed (not found) - " + location); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * @return true if a file was found at the given location, and false otherwise.
	 */
	private boolean processPluginPathFile(PluginRegistryModel registry, URL location) {
		PluginModel entry = processManifestFile(location);
		if (entry == null)
			return false;
		// Make sure all the required fields are here.
		// This prevents us from things like NullPointerExceptions
		// when we are assuming a field exists.
		if (!requiredPluginModel(entry, location)) {
			entry = null;
			return false;
		}
		entry.setVersion(getQualifiedVersion(entry, location)); // check for version qualifier
		if (entry instanceof PluginDescriptorModel) {
			if (entry.getId() == null || entry.getVersion() == null) {
				return parseProblem(NLS.bind(Messages.parse_nullPluginIdentifier, location));
			}
			//skip duplicate entries
			if (registry.getPlugin(entry.getId(), entry.getVersion()) != null) {
				return parseProblem(NLS.bind(Messages.parse_duplicatePlugin, entry.getId(), location));
			}
			registry.addPlugin((PluginDescriptorModel) entry);
		} else {
			if (entry.getId() == null || entry.getVersion() == null) {
				return parseProblem(NLS.bind(Messages.parse_nullFragmentIdentifier, location));
			}
			if (entry instanceof PluginFragmentModel) {
				registry.addFragment((PluginFragmentModel) entry);
			} else {
				return parseProblem(NLS.bind(Messages.parse_unknownEntry, location));
			}
		}
		String url = location.toString();
		url = url.substring(0, 1 + url.lastIndexOf('/'));
		entry.setRegistry(registry);
		entry.setLocation(url);
		// this is for the registry cache
		// InternalPlatform.addLastModifiedTime(location.getFile(), new File(location.getFile()).lastModified());
		return true;
	}

	private String getQualifiedVersion(PluginModel entry, URL base) {
		if (entry == null || entry.getVersion() == null || entry.getId() == null)
			return null;

		InputStream is = null;
		try {
			// check to see if we have buildmanifest.properties for this plugin
			URL manifest = null;
			manifest = new URL(base, "buildmanifest.properties"); //$NON-NLS-1$
			Properties props = new Properties();
			is = manifest.openStream();
			props.load(is);

			// lookup qualifier for this plugin and "morph" the identifier if needed
			String key = "plugin@" + entry.getId(); //$NON-NLS-1$
			String qualifier = props.getProperty(key);
			if (qualifier == null)
				return entry.getVersion();
			PluginVersionIdentifier v = new PluginVersionIdentifier(entry.getVersion());
			if (!v.getQualifierComponent().equals("")) //$NON-NLS-1$
				return entry.getVersion();
			else
				return (new PluginVersionIdentifier(v.getMajorComponent(), v.getMinorComponent(), v.getServiceComponent(), qualifier)).toString();
		} catch (Exception e) {
			return entry.getVersion();
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
					// Don't throw anything back if the close fails
				}
		}
	}

	private boolean requiredPluginModel(PluginModel plugin, URL location) {
		String name = plugin.getName();
		String id = plugin.getId();
		String version = plugin.getVersion();
		int nameLength = name == null ? 0 : name.length();
		int idLength = id == null ? 0 : id.length();
		int versionLength = version == null ? 0 : version.length();

		if (nameLength <= 0) {
			parseProblem(NLS.bind(Messages.parse_missingPluginName, location));
			return false;
		}
		if (idLength <= 0) {
			parseProblem(NLS.bind(Messages.parse_missingPluginId, location));
			return false;
		}
		if (versionLength <= 0) {
			parseProblem(NLS.bind(Messages.parse_missingPluginVersion, location));
			return false;
		}

		if (plugin instanceof PluginFragmentModel) {
			String pluginName = ((PluginFragmentModel) plugin).getPlugin();
			String pluginVersion = ((PluginFragmentModel) plugin).getPluginVersion();
			int pNameLength = pluginName == null ? 0 : pluginName.length();
			int pNameVersion = pluginVersion == null ? 0 : pluginVersion.length();
			if (pNameLength <= 0) {
				parseProblem(NLS.bind(Messages.parse_missingFPName, location));
				return false;
			}
			if (pNameVersion <= 0) {
				parseProblem(NLS.bind(Messages.parse_missingFPVersion, location));
				return false;
			}
		}
		return true;
	}
}
