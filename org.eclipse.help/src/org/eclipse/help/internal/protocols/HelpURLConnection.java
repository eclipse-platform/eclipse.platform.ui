/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.protocols;
import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.core.boot.*;
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.appserver.*;
import org.eclipse.help.internal.util.*;
/**
 * URLConnection to help documents in plug-ins
 */
public class HelpURLConnection extends URLConnection {
	private final static String LANG = "lang";
	// document caching - disabled if running in dev mode
	protected static boolean cachingEnabled = true;
	static {
		String[] args = Platform.getCommandLineArgs();
		for (int i = 0; i < args.length; i++) {
			if ("-dev".equals(args[i])) {
				cachingEnabled = false;
				break;
			}
		}
	}

	protected String pluginAndFile; // plugin/file
	protected String query; // after ?
	protected HashMap arguments;
	protected IPluginDescriptor plugin;
	// file in a plug-in
	protected String file;
	protected String locale;
	/**
	 * Constructor for HelpURLConnection
	 */
	public HelpURLConnection(URL url) {
		super(url);

		String urlFile = url.getFile();

		// Strip off the leading "/" and the query
		if (urlFile.startsWith("/"))
			urlFile = urlFile.substring(1);

		int indx = urlFile.indexOf("?");
		if (indx != -1) {
			query = urlFile.substring(indx + 1);
			urlFile = urlFile.substring(0, indx);
		}
		this.pluginAndFile = urlFile;
		parseQuery();

		setDefaultUseCaches(isCacheable());
		if (HelpPlugin.DEBUG_PROTOCOLS) {
			System.out.println("HelpURLConnection: url=" + url);
		}
	}

	/**
	 * @see URLConnection#connect()
	 */
	public void connect() throws IOException {
	}
	/**
	 * see URLConnection#getInputStream();
	 *Note: this method can throw IOException, but should never return null
	 */
	public InputStream getInputStream() throws IOException {
		// must override parent implementation, since it does nothing.
		IPluginDescriptor plugin = getPlugin();
		if (plugin == null) {
			throw new IOException("Resource not found.");
		}
		if (plugin
			.equals(
				AppserverPlugin.getDefault().getContributingServerPlugin())) {
			// Do not return documents from app server implementation plug-in
			throw new IOException("Resource not found.");
		}
		if (getFile() == null || "".equals(getFile())) {
			throw new IOException("Resource not found.");
		}

		// first try using content provider
		// then find the file inside nl tree in doc.zip,
		// and then, in the file system
		InputStream inputStream=ResourceLocator.openFromProducer(
			plugin,
			query == null ? getFile() : getFile() + "?" + query,
			getLocale());

		if (inputStream == null) {
			inputStream =
				ResourceLocator.openFromZip(
					plugin,
					"doc.zip",
					getFile(),
					getLocale());
		}
		if (inputStream == null) {
			inputStream =
				ResourceLocator.openFromPlugin(plugin, getFile(), getLocale());
		}
		if (inputStream == null) {
			throw new IOException("Resource not found.");
		}
		return inputStream;
	}

	public long getExpiration() {
		return isCacheable() ? new Date().getTime() + 10000 : 0;
	}
	/**
	 * NOTE: need to add support for multi-valued parameters (like filtering)
	 * Multiple values are added as vectors
	 */
	protected void parseQuery() {
		if (query != null && !"".equals(query)) {
			if (arguments == null) {
				arguments = new HashMap(5);
			}

			StringTokenizer stok = new StringTokenizer(query, "&");
			while (stok.hasMoreTokens()) {
				String aQuery = stok.nextToken();
				int equalsPosition = aQuery.indexOf("=");
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
	}

	public String getContentType() {
		// Check if the file is hypertext or plain text 
		String file = pluginAndFile.toLowerCase(Locale.US);
		if (file.endsWith(".html") || file.endsWith(".htm"))
			return "text/html";
		else if (file.endsWith(".css"))
			return "text/css";
		else if (file.endsWith(".gif"))
			return "image/gif";
		else if (file.endsWith(".jpg"))
			return "image/jpeg";
		else if (file.endsWith(".pdf"))
			return "application/pdf";
		else if (file.endsWith(".xml"))
			return "application/xml";
		else if (file.endsWith(".xsl"))
			return "application/xsl";
		return "text/plain";
	}
	/**
	 * 
	 */
	public Vector getMultiValue(String name) {
		if (arguments != null) {
			Object value = arguments.get(name);
			if (value instanceof Vector)
				return (Vector) value;
			else
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
				locale = BootLoader.getNL();
			}
		}
		return locale;
	}

	protected String getFile() {
		if (file == null) {
			// Strip the plugin id
			int start = pluginAndFile.indexOf("/") + 1;
			// Strip query string or anchor bookmark
			int end = pluginAndFile.indexOf("?");
			if (end == -1)
				end = pluginAndFile.indexOf("#");
			if (end == -1)
				end = pluginAndFile.length();
			file = pluginAndFile.substring(start, end);
			file = URLCoder.decode(file);
		}
		return file;
	}
	protected IPluginDescriptor getPlugin() {
		if (plugin == null) {
			// Assume the url is pluginID/path_to_topic.html
			int i = pluginAndFile.indexOf('/');
			String pluginId = i == -1 ? "" : pluginAndFile.substring(0, i);
			pluginId = URLCoder.decode(pluginId);
			plugin = Platform.getPluginRegistry().getPluginDescriptor(pluginId);
		}
		return plugin;
	}
	public boolean isCacheable() {
		if (getValue("resultof") != null)
			return false;
		else
			return cachingEnabled;
	}
	public String toString() {
		return pluginAndFile;
	}

}
