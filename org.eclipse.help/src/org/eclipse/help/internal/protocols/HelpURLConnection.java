/***************************************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/
package org.eclipse.help.internal.protocols;

import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.util.*;
import org.osgi.framework.*;

/**
 * URLConnection to help documents in plug-ins
 */
public class HelpURLConnection extends URLConnection {

	private final static String LANG = "lang"; //$NON-NLS-1$
	private final static String PRODUCT_PLUGIN = "PRODUCT_PLUGIN"; //$NON-NLS-1$
	// document caching - disabled if running in dev mode
	protected static boolean cachingEnabled = true;
	static {
		String[] args = Platform.getCommandLineArgs();
		for (int i = 0; i < args.length; i++) {
			if ("-dev".equals(args[i])) { //$NON-NLS-1$
				cachingEnabled = false;
				break;
			}
		}
	}

	protected String pluginAndFile; // plugin/file
	protected String query; // after ?
	protected HashMap arguments;
	protected Bundle plugin;
	// file in a plug-in
	protected String file;
	protected String locale;
	private static String appserverImplPluginId;

	/**
	 * Constructor for HelpURLConnection
	 */
	public HelpURLConnection(URL url) {
		super(url);

		String urlFile = url.getFile();

		// Strip off the leading "/" and the query
		if (urlFile.startsWith("/")) //$NON-NLS-1$
			urlFile = urlFile.substring(1);

		int indx = urlFile.indexOf("?"); //$NON-NLS-1$
		if (indx != -1) {
			query = urlFile.substring(indx + 1);
			urlFile = urlFile.substring(0, indx);
		}
		this.pluginAndFile = urlFile;
		parseQuery();

		setDefaultUseCaches(isCacheable());
		if (HelpPlugin.DEBUG_PROTOCOLS) {
			System.out.println("HelpURLConnection: url=" + url); //$NON-NLS-1$
		}
	}

	/**
	 * @see URLConnection#connect()
	 */
	public void connect() throws IOException {
	}

	/**
	 * see URLConnection#getInputStream(); Note: this method can throw IOException, but should never
	 * return null
	 */
	public InputStream getInputStream() throws IOException {
		// must override parent implementation, since it does nothing.
		Bundle plugin = getPlugin();
		if (plugin == null) {
			throw new IOException("Resource not found."); //$NON-NLS-1$
		}

		if (plugin.getSymbolicName().equals(getAppserverImplPluginId())) {
			// Do not return documents from app server implementation plug-in
			throw new IOException("Resource not found."); //$NON-NLS-1$
		}

		if (getFile() == null || "".equals(getFile())) { //$NON-NLS-1$
			throw new IOException("Resource not found."); //$NON-NLS-1$
		}

		// first try using content provider, then try to find the file
		// inside doc.zip, and finally try the file system
		InputStream inputStream = ResourceLocator.openFromProducer(plugin, query == null ? getFile()
				: getFile() + "?" + query, //$NON-NLS-1$
				getLocale());

		if (inputStream == null) {
			inputStream = ResourceLocator.openFromZip(plugin, "doc.zip", //$NON-NLS-1$
					getFile(), getLocale());
		}
		if (inputStream == null) {
			inputStream = ResourceLocator.openFromPlugin(plugin, getFile(), getLocale());
		}
		if (inputStream == null) {
			throw new IOException("Resource not found."); //$NON-NLS-1$
		}
		return inputStream;
	}

	public long getExpiration() {
		return isCacheable() ? new Date().getTime() + 10000 : 0;
	}

	public static void parseQuery(String query, HashMap arguments) {
		StringTokenizer stok = new StringTokenizer(query, "&"); //$NON-NLS-1$
		while (stok.hasMoreTokens()) {
			String aQuery = stok.nextToken();
			int equalsPosition = aQuery.indexOf("="); //$NON-NLS-1$
			if (equalsPosition > -1) { // well formed name/value pair
				String arg = aQuery.substring(0, equalsPosition);
				String val = aQuery.substring(equalsPosition + 1);
				Object existing = arguments.get(arg);
				if (existing == null)
					arguments.put(arg, val);
				else if (existing instanceof Vector) {
					((Vector) existing).add(val);
					arguments.put(arg, existing);
				} else {
					Vector v = new Vector(2);
					v.add(existing);
					v.add(val);
					arguments.put(arg, v);
				}
			}
		}
	}

	/**
	 * NOTE: need to add support for multi-valued parameters (like filtering) Multiple values are
	 * added as vectors
	 */
	protected void parseQuery() {
		if (query != null && !"".equals(query)) { //$NON-NLS-1$
			if (arguments == null) {
				arguments = new HashMap(5);
			}
			parseQuery(query, arguments);
		}
	}

	public String getContentType() {
		// Check if the file is hypertext or plain text
		String file = pluginAndFile.toLowerCase(Locale.US);
		if (file.endsWith(".html") || file.endsWith(".htm")) //$NON-NLS-1$ //$NON-NLS-2$
			return "text/html"; //$NON-NLS-1$
		else if (file.endsWith(".css")) //$NON-NLS-1$
			return "text/css"; //$NON-NLS-1$
		else if (file.endsWith(".gif")) //$NON-NLS-1$
			return "image/gif"; //$NON-NLS-1$
		else if (file.endsWith(".jpg")) //$NON-NLS-1$
			return "image/jpeg"; //$NON-NLS-1$
		else if (file.endsWith(".pdf")) //$NON-NLS-1$
			return "application/pdf"; //$NON-NLS-1$
		else if (file.endsWith(".xml")) //$NON-NLS-1$
			return "application/xml"; //$NON-NLS-1$
		else if (file.endsWith(".xsl")) //$NON-NLS-1$
			return "application/xsl"; //$NON-NLS-1$
		return "text/plain"; //$NON-NLS-1$
	}

	/**
	 * 
	 */
	public Vector getMultiValue(String name) {
		if (arguments != null) {
			Object value = arguments.get(name);
			if (value instanceof Vector)
				return (Vector) value;
			return null;
		}
		return null;
	}

	/**
	 * 
	 */
	public String getValue(String name) {
		if (arguments == null)
			return null;
		Object value = arguments.get(name);
		String stringValue = null;
		if (value instanceof String)
			stringValue = (String) value;
		else if (value instanceof Vector)
			stringValue = (String) ((Vector) value).firstElement();
		else
			return null;
		try {
			return URLCoder.decode(stringValue);
		} catch (Exception e) {
			return null;
		}

	}

	/**
	 * Returns the locale specified by client.
	 */
	protected String getLocale() {
		if (locale == null) {
			locale = getValue(LANG);
			if (locale == null) {
				locale = Platform.getNL();
			}
		}
		return locale;
	}

	protected String getFile() {
		if (file == null) {
			// Strip the plugin id
			int start = pluginAndFile.indexOf("/") + 1; //$NON-NLS-1$
			// Strip query string or anchor bookmark
			int end = pluginAndFile.indexOf("?"); //$NON-NLS-1$
			if (end == -1)
				end = pluginAndFile.indexOf("#"); //$NON-NLS-1$
			if (end == -1)
				end = pluginAndFile.length();
			file = pluginAndFile.substring(start, end);
			file = URLCoder.decode(file);
		}
		return file;
	}

	protected Bundle getPlugin() {
		if (plugin == null) {
			// Assume the url is pluginID/path_to_topic.html
			int i = pluginAndFile.indexOf('/');
			String pluginId = i == -1 ? "" : pluginAndFile.substring(0, i); //$NON-NLS-1$
			pluginId = URLCoder.decode(pluginId);
			if (PRODUCT_PLUGIN.equals(pluginId)) {
				IProduct product = Platform.getProduct();
				if (product != null) {
					plugin = product.getDefiningBundle();
					return plugin;
				}
			}
			plugin = Platform.getBundle(pluginId);
		}
		return plugin;
	}

	public boolean isCacheable() {
		if (getValue("resultof") != null) //$NON-NLS-1$
			return false;
		return cachingEnabled;
	}

	public String toString() {
		return pluginAndFile;
	}

	/**
	 * Obtains ID of plugin that contributes appserver implementation. *
	 * 
	 * @return plug-in ID or null
	 */
	private static String getAppserverImplPluginId() {
		if (appserverImplPluginId == null) {

			// This part mimics AppserverPlugin.createWebappServer()

			// get the app server extension from the system plugin registry
			IExtensionRegistry pluginRegistry = Platform.getExtensionRegistry();
			IExtensionPoint point = pluginRegistry.getExtensionPoint("org.eclipse.help.appserver.server"); //$NON-NLS-1$
			if (point != null) {
				IExtension[] extensions = point.getExtensions();
				if (extensions.length != 0) {
					// We need to pick up the non-default configuration
					IConfigurationElement[] elements = extensions[0].getConfigurationElements();
					if (elements.length == 0)
						return null;
					IConfigurationElement serverElement = null;
					for (int i = 0; i < elements.length; i++) {
						String defaultValue = elements[i].getAttribute("default"); //$NON-NLS-1$
						if (defaultValue == null || defaultValue.equals("false")) { //$NON-NLS-1$
							serverElement = elements[i];
							break;
						}
					}
					// if all the servers are default, then pick the first one
					if (serverElement == null) {
						serverElement = elements[0];
					}
					//

					appserverImplPluginId = serverElement.getNamespace();

				}
			}
		}
		return appserverImplPluginId;
	}

}
