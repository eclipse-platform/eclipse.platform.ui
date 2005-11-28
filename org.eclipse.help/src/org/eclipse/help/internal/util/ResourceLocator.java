/***************************************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/
package org.eclipse.help.internal.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.IHelpContentProducer;
import org.eclipse.help.internal.HelpPlugin;
import org.osgi.framework.Bundle;

public class ResourceLocator {

	public static final String CONTENTPRODUCER_XP_NAME = "contentProducer"; //$NON-NLS-1$
	public static final String BINDING = "binding"; //$NON-NLS-1$

	public static final String CONTENTPRODUCER_XP_FULLNAME = HelpPlugin.PLUGIN_ID
			+ "." + CONTENTPRODUCER_XP_NAME; //$NON-NLS-1$

	private static Hashtable zipCache = new Hashtable();

	private static final Object ZIP_NOT_FOUND = new Object();

	// Indicates there is no dynamic content provider for a particular plugin
	private static final Object STATIC_DOCS_ONLY = ZIP_NOT_FOUND;

	// Map of document content providers by plug-in ID;
	private static Map contentProducers = new HashMap(2, 0.5f);

	static class ProducerDescriptor {

		IHelpContentProducer producer;
		IConfigurationElement config;

		public ProducerDescriptor(IConfigurationElement config) {
			this.config = config;
		}

		public boolean matches(String refId) {
			IExtension ex = config.getDeclaringExtension();
			String id = ex.getUniqueIdentifier();
			return id != null && id.equals(refId);
		}
	}
	static {
		Platform.getExtensionRegistry().addRegistryChangeListener(new IRegistryChangeListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.IRegistryChangeListener#registryChanged(org.eclipse.core.runtime.IRegistryChangeEvent)
			 */
			public void registryChanged(IRegistryChangeEvent event) {
				IExtensionDelta[] deltas = event.getExtensionDeltas(HelpPlugin.PLUGIN_ID,
						CONTENTPRODUCER_XP_NAME);
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
	 * Obtains content proivider for a documentation plug-in, creates one if necessary.
	 * 
	 * @param pluginId
	 * @return ITopicContentProvider or null
	 */
	private static IHelpContentProducer getContentProducer(String pluginId) {
		synchronized (contentProducers) {
			Object descriptor = contentProducers.get(pluginId);
			if (descriptor == null) {
				// first time for the plug-in, so attempt to
				// find and instantiate provider
				descriptor = createContentProducer(pluginId);
				if (descriptor == null) {
					descriptor = STATIC_DOCS_ONLY;
				}
				contentProducers.put(pluginId, descriptor);
			}
			if (descriptor == STATIC_DOCS_ONLY) {
				return null;
			}
			return ((ProducerDescriptor) descriptor).producer;
		}
	}

	/**
	 * Creates content proivider for a documentation plug-in
	 * 
	 * @param pluginId
	 * @return ITopicContentProvider or null
	 */
	private static ProducerDescriptor createContentProducer(String pluginId) {
		IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(
				CONTENTPRODUCER_XP_FULLNAME);
		if (elements.length == 0) {
			return null;
		}

		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			if (!elements[i].getNamespace().equals(pluginId)) {
				continue;
			}
			if (BINDING.equals(element.getName())) {
				//producer binding - locate the descriptor
				// with the matching reference Id
				String refId = element.getAttribute("producerId");
				if (refId != null) {
					return findContentProducer(refId);
				}
			} else if (CONTENTPRODUCER_XP_NAME.equals(element.getName())) {
				try {
					Object o = element.createExecutableExtension("producer"); //$NON-NLS-1$
					if (o instanceof IHelpContentProducer) {
						ProducerDescriptor ad = new ProducerDescriptor(element);
						ad.producer = (IHelpContentProducer)o;
						return ad;
					}
				} catch (CoreException ce) {
					HelpPlugin
							.logError(
									"Exception occurred creating help content producer for plug-in " + pluginId + ".", ce); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
		return null;
	}

	private static ProducerDescriptor findContentProducer(String refId) {
		for (Iterator iter = contentProducers.values().iterator(); iter.hasNext();) {
			Object obj = iter.next();
			if (obj instanceof ProducerDescriptor) {
				ProducerDescriptor desc = (ProducerDescriptor) obj;
				if (desc.matches(refId))
					return desc;
			}
		}
		return null;
	}

	/**
	 * Opens an input stream to a file contained in a plugin. This includes NL lookup.
	 */
	public static InputStream openFromProducer(Bundle pluginDesc, String file, String locale) {
		IHelpContentProducer producer = getContentProducer(pluginDesc.getSymbolicName());
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
	 * Opens an input stream to a file contained in a plugin. This includes includes OS, WS and NL
	 * lookup.
	 * 
	 * @param pluginId
	 *            the plugin id of the plugin that contains the file you are trying to find
	 * @param file
	 *            the relative path of the file to find
	 * @param locale
	 *            the locale used as an override or <code>null</code> to use the default locale
	 * 
	 * @return an InputStream to the file or <code>null</code> if the file wasn't found
	 */
	public static InputStream openFromPlugin(String pluginId, String file, String locale) {
		Bundle bundle = Platform.getBundle(pluginId);
		if (bundle != null)
			return openFromPlugin(bundle, file, locale);
		return null;
	}

	/**
	 * Opens an input stream to a file contained in a zip in a plugin. This includes OS, WS and NL
	 * lookup.
	 * 
	 * @param pluginDesc
	 *            the plugin description of the plugin that contains the file you are trying to find
	 * @param file
	 *            the relative path of the file to find
	 * @param locale
	 *            the locale used as an override or <code>null</code> to use the default locale
	 * 
	 * @return an InputStream to the file or <code>null</code> if the file wasn't found
	 */
	public static InputStream openFromZip(Bundle pluginDesc, String zip, String file, String locale) {

		String pluginID = pluginDesc.getSymbolicName();
		Map cache = zipCache;
		ArrayList pathPrefix = getPathPrefix(locale);

		for (int i = 0; i < pathPrefix.size(); i++) {

			// finds the zip file by either using a cached location, or
			// calling Platform.find - the result is cached for future use.
			Object cached = cache.get(pluginID + '/' + pathPrefix.get(i) + zip);
			if (cached == null) {
				try {
					URL url = Platform.find(pluginDesc, new Path(pathPrefix.get(i) + zip));
					if (url != null) {
						URL realZipURL = Platform.asLocalURL(Platform.resolve(url));
						cached = realZipURL.toExternalForm();
					} else {
						cached = ZIP_NOT_FOUND;
					}
				} catch (IOException ioe) {
					cached = ZIP_NOT_FOUND;
				}
				// cache it
				cache.put(pluginID + '/' + pathPrefix.get(i) + zip, cached);
			}

			if (cached == ZIP_NOT_FOUND || cached.toString().startsWith("jar:")) //$NON-NLS-1$
				continue;

			// cached should be a zip file that is actually on the filesystem
			// now check if the file is in this zip
			try {
				URL jurl = new URL("jar", "", (String) cached + "!/" + file); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				URLConnection jconnection = jurl.openConnection();
				jconnection.setDefaultUseCaches(false);
				jconnection.setUseCaches(false);
				return jconnection.getInputStream();
			} catch (IOException ioe) {
				// a file not found exception is an io exception
				continue;
			}

		} // end for loop

		// we didn't find the file in any zip
		return null;
	}

	/**
	 * Opens an input stream to a file contained in a plugin. This includes includes OS, WS and NL
	 * lookup.
	 * 
	 * @param pluginDesc
	 *            the plugin description of the plugin that contains the file you are trying to find
	 * @param file
	 *            the relative path of the file to find
	 * @param locale
	 *            the locale used as an override or <code>null</code> to use the default locale
	 * 
	 * @return an InputStream to the file or <code>null</code> if the file wasn't found
	 */
	public static InputStream openFromPlugin(Bundle pluginDesc, String file, String locale) {

		ArrayList pathPrefix = getPathPrefix(locale);
		URL flatFileURL = find(pluginDesc, new Path(file), pathPrefix);
		if (flatFileURL != null)
			try {
				return flatFileURL.openStream();
			} catch (IOException e) {
				return null;
			}
		return null;
	}



	/*
	 * Search the ws, os then nl for a resource. Platform.find can't be used directly with $nl$,
	 * $os$ or $ws$ becuase the root directory will be searched too early.
	 */
	public static URL find(Bundle pluginDesc, IPath flatFilePath, ArrayList pathPrefix) {

		// try to find the actual file.
		for (int i = 0; i < pathPrefix.size(); i++) {
			URL url = Platform.find(pluginDesc, new Path((String) pathPrefix.get(i) + flatFilePath));
			if (url != null)
				return url;
		}
		return null;
	}

	public static void clearZipCache() {
		zipCache = new Hashtable();
	}

	/*
	 * Gets an ArrayList that has the path prefixes to search.
	 * 
	 * @param locale the locale used as an override or <code>null</code> to use the default locale
	 * @return an ArrayList that has path prefixes that need to be search. The returned ArrayList
	 * will have an entry for the root of the plugin.
	 */
	public static ArrayList getPathPrefix(String locale) {
		ArrayList pathPrefix = new ArrayList(5);
		// TODO add override for ws and os similar to how it's done with locale
		// now
		String ws = Platform.getWS();
		String os = Platform.getOS();
		if (locale == null)
			locale = Platform.getNL();

		if (ws != null)
			pathPrefix.add("ws/" + ws + '/'); //$NON-NLS-1$

		if (os != null && !os.equals("OS_UNKNOWN")) //$NON-NLS-1$
			pathPrefix.add("os/" + os + '/'); //$NON-NLS-1$

		if (locale != null && locale.length() >= 5)
			pathPrefix.add("nl/" + locale.substring(0, 2) + '/' + locale.substring(3, 5) + '/'); //$NON-NLS-1$

		if (locale != null && locale.length() >= 2)
			pathPrefix.add("nl/" + locale.substring(0, 2) + '/'); //$NON-NLS-1$

		// the plugin root
		pathPrefix.add(""); //$NON-NLS-1$

		return pathPrefix;
	}

	/**
	 * Finds all topics under specified directory (recursively). This includes includes OS, WS and
	 * NL lookup.
	 * 
	 * @param pluginDesc
	 *            the plugin description of the plugin that contains the file you are trying to find
	 * @param directory
	 *            the relative path of the directory
	 * @param locale
	 *            the locale used as an override or <code>null</code> to use the default locale
	 * 
	 * @return an InputStream to the file or <code>null</code> if the file wasn't found
	 */
	public static Set findTopicPaths(Bundle pluginDesc, String directory, String locale) {
		Set ret = new HashSet();
		findTopicPaths(pluginDesc, directory, locale, ret);
		return ret;
	}

	/**
	 * @param pluginDesc
	 * @param directory
	 * @param locale
	 * @param paths
	 */
	private static void findTopicPaths(Bundle pluginDesc, String directory, String locale, Set paths) {
		if (directory.endsWith("/")) //$NON-NLS-1$
			directory = directory.substring(0, directory.length() - 1);
		ArrayList pathPrefix = getPathPrefix(locale);
		for (int i = 0; i < pathPrefix.size(); i++) {
			String path = pathPrefix.get(i) + directory;
			if (path.length() == 0)
				path = "/"; //$NON-NLS-1$
			Enumeration entries = pluginDesc.getEntryPaths(path);
			if (entries != null) {
				while (entries.hasMoreElements()) {
					String topicPath = (String) entries.nextElement();
					if (topicPath.endsWith("/")) { //$NON-NLS-1$
						findTopicPaths(pluginDesc, topicPath, locale, paths);
					} else {
						paths.add(topicPath);
					}
				}
			}
		}
	}
}
