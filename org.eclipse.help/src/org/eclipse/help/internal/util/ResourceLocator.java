/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.util;
import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.*;
import org.eclipse.help.internal.*;
import org.osgi.framework.*;
public class ResourceLocator {
	public static final String CONTENTPRODUCER_XP_NAME = "contentProducer"; //$NON-NLS-1$
	public static final String CONTENTPRODUCER_XP_FULLNAME = HelpPlugin.PLUGIN_ID
			+ "." + CONTENTPRODUCER_XP_NAME; //$NON-NLS-1$
	private static Hashtable zipCache = new Hashtable();
	private static final Object ZIP_NOT_FOUND = new Object();
	// Indicates there is no dynamic content provider for a particular plugin
	private static final Object STATIC_DOCS_ONLY = ZIP_NOT_FOUND;
	// Map of document content providers by plug-in ID;
	private static Map contentProducers = new HashMap(2, 0.5f);
	static {
		Platform.getExtensionRegistry().addRegistryChangeListener(
				new IRegistryChangeListener() {
					/*
					 * (non-Javadoc)
					 * 
					 * @see org.eclipse.core.runtime.IRegistryChangeListener#registryChanged(org.eclipse.core.runtime.IRegistryChangeEvent)
					 */
					public void registryChanged(IRegistryChangeEvent event) {
						IExtensionDelta[] deltas = event.getExtensionDeltas(
								HelpPlugin.PLUGIN_ID, CONTENTPRODUCER_XP_NAME);
						for (int i = 0; i < deltas.length; i++) {
							IExtension extension = deltas[i].getExtension();
							String affectedPlugin = extension.getNamespace();
							// reset producer for the affected plugin,
							// it will be recreated on demand
							synchronized (contentProducers) {
								contentProducers.remove(affectedPlugin);
							}
						}
					}
				});
	}
	/**
	 * Obtains content proivider for a documentation plug-in, creates one if
	 * necessary.
	 * 
	 * @param pluginId
	 * @return ITopicContentProvider or null
	 */
	private static IHelpContentProducer getContentProducer(String pluginId) {
		synchronized (contentProducers) {
			Object producer = contentProducers.get(pluginId);
			if (producer == null) {
				// first time for the plug-in, so attempt to
				// find and instantiate provider
				producer = createContentProducer(pluginId);
				if (producer == null) {
					producer = STATIC_DOCS_ONLY;
				}
				contentProducers.put(pluginId, producer);
			}
			if (producer == STATIC_DOCS_ONLY) {
				return null;
			} else {
				return (IHelpContentProducer) producer;
			}
		}
	}
	/**
	 * Creates content proivider for a documentation plug-in
	 * 
	 * @param pluginId
	 * @return ITopicContentProvider or null
	 */
	private static IHelpContentProducer createContentProducer(String pluginId) {
		IExtensionPoint xp = Platform.getExtensionRegistry().getExtensionPoint(
				CONTENTPRODUCER_XP_FULLNAME);
		if (xp == null) {
			return null;
		}
		IExtension[] extensions = xp.getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			if (!extensions[i].getNamespace().equals(pluginId)) {
				continue;
			}
			IConfigurationElement[] elements = extensions[i]
					.getConfigurationElements();
			for (int j = 0; j < elements.length; j++) {
				if (!CONTENTPRODUCER_XP_NAME.equals(elements[j].getName())) {
					continue;
				}
				try {
					Object o = elements[j]
							.createExecutableExtension("producer"); //$NON-NLS-1$
					if (o instanceof IHelpContentProducer) {
						return (IHelpContentProducer) o;
					}
				} catch (CoreException ce) {
					HelpPlugin.logError(HelpResources.getString(
							"E044", pluginId), ce); //$NON-NLS-1$
				}
			}
		}
		return null;
	}
	/**
	 * Opens an input stream to a file contained in a plugin. This includes NL
	 * lookup.
	 */
	public static InputStream openFromProducer(Bundle pluginDesc, String file,
			String locale) {
		IHelpContentProducer producer = getContentProducer(pluginDesc
				.getSymbolicName());
		if (producer == null) {
			return null;
		}
		if (locale == null || locale.length() <= 0) {
			locale = Platform.getNL();
		}
		Locale l;
		if (locale.length() >= 5) {
			l = new Locale(locale.substring(0, 2), locale.substring(3, 5));
		} else if (locale.length() >= 2) {
			l = new Locale(locale.substring(0, 2), ""); //$NON-NLS-1$
		} else {
			l = Locale.getDefault();
		}
		return producer.getInputStream(pluginDesc.getSymbolicName(), file, l);
	}
	/**
	 * Opens an input stream to a file contained in a plugin. This includes NL
	 * lookup.
	 */
	public static InputStream openFromPlugin(String pluginId, String file,
			String locale) {
		Bundle bundle = Platform.getBundle(pluginId);
		if (bundle != null)
			return openFromPlugin(Platform.getBundle(pluginId), file, locale);
		else
			return null;
	}
	/**
	 * Opens an input stream to a file contained in a zip in a plugin. This
	 * includes NL lookup.
	 */
	public static InputStream openFromZip(Bundle pluginDesc, String zip,
			String file, String locale) {
		// First try the NL lookup
		InputStream is = doOpenFromZip(pluginDesc, "$nl$/" + zip, file, locale); //$NON-NLS-1$
		if (is == null)
			// Default location <plugin>/doc.zip
			is = doOpenFromZip(pluginDesc, zip, file, locale);
		return is;
	}
	/**
	 * Opens an input stream to a file contained in a plugin. This includes NL
	 * lookup.
	 */
	public static InputStream openFromPlugin(Bundle pluginDesc, String file,
			String locale) {
		InputStream is = doOpenFromPlugin(pluginDesc, "$nl$/" + file, locale); //$NON-NLS-1$
		if (is == null)
			// Default location
			is = doOpenFromPlugin(pluginDesc, file, locale);
		return is;
	}
	/**
	 * Opens an input stream to a file contained in doc.zip in a plugin
	 */
	private static InputStream doOpenFromZip(Bundle pluginDesc, String zip,
			String file, String locale) {
		String realZipURL = findZip(pluginDesc, zip, locale);
		if (realZipURL == null) {
			return null;
		}
		try {
			URL jurl = new URL("jar", "", realZipURL + "!/" + file); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			URLConnection jconnection = jurl.openConnection();
			jconnection.setDefaultUseCaches(false);
			jconnection.setUseCaches(false);
			return jconnection.getInputStream();
		} catch (IOException ioe) {
			return null;
		}
	}
	/**
	 * Opens an input stream to a file contained in a plugin
	 */
	private static InputStream doOpenFromPlugin(Bundle pluginDesc, String file,
			String locale) {
		IPath flatFilePath = new Path(file);
		Map override = new HashMap(1);
		override.put("$nl$", locale); //$NON-NLS-1$
		URL flatFileURL = Platform.find(pluginDesc, flatFilePath, override);
		if (flatFileURL != null)
			try {
				return flatFileURL.openStream();
			} catch (IOException e) {
				return null;
			}
		return null;
	}
	/**
	 * @param pluginDesc
	 * @param zip
	 *            zip file path as required by Plugin.find()
	 * @param locale
	 * @return String form of resolved URL of a zip or null
	 */
	private static String findZip(Bundle pluginDesc, String zip, String locale) {
		String pluginID = pluginDesc.getSymbolicName();
		// check cache
		Map cache = zipCache;
		Object cached = cache.get(pluginID + '/' + zip + '/' + locale);
		if (cached == null) {
			// not in cache find on filesystem
			IPath zipFilePath = new Path(zip);
			Map override = new HashMap(1);
			override.put("$nl$", locale); //$NON-NLS-1$
			try {
				URL zipFileURL = Platform.find(pluginDesc, zipFilePath,
						override); //PASCAL This will not activate the plugin
				if (zipFileURL != null) {
					URL realZipURL = Platform.asLocalURL(Platform
							.resolve(zipFileURL));
					cached = realZipURL.toExternalForm();
				} else {
					cached = ZIP_NOT_FOUND;
				}
			} catch (IOException ioe) {
				cached = ZIP_NOT_FOUND;
			}
			// cache it
			cache.put(pluginID + '/' + zip + '/' + locale, cached);
		}
		if (cached == ZIP_NOT_FOUND) {
			return null;
		}
		return (String) cached;
	}
	public static void clearZipCache() {
		zipCache = new Hashtable();
	}
}
