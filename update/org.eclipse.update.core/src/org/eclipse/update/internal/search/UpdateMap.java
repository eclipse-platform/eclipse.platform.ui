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
package org.eclipse.update.internal.search;

import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.search.*;

/**
 * 
 * This class opens connection to the update map resource
 * and parses the file to load update URL mappings.
 * These mappings are used to redirect new updates search
 * when the redirection pattern matches.
 */

public class UpdateMap {
	private static class MapSite implements IUpdateSiteAdapter {
		private URL url;
		public MapSite(URL url) {
			this.url = url;
		}
		public String getLabel() {
			return url.toString();
		}
		public URL getURL() {
			return url;
		}
	}

	private static class UpdateMapEntry {
		private IUpdateSiteAdapter site;
		private String pattern;

		public UpdateMapEntry(String pattern, URL url) {
			this.pattern = pattern;
			this.site = new MapSite(url);
		}
		public IUpdateSiteAdapter getSite() {
			return site;
		}
		public boolean matches(String id) {
			return id.startsWith(pattern);
		}
	}

	private ArrayList entries;
	private IUpdateSiteAdapter defaultSite;
	private boolean loaded = false;

	public UpdateMap() {
		entries = new ArrayList();
	}

	public void load(URL mapFile, IProgressMonitor monitor)
		throws CoreException {
		InputStream mapStream = null;
		try {
			Response response = UpdateCore.getPlugin().get(mapFile);
			UpdateManagerUtils.checkConnectionResult(response, mapFile);
			mapStream = response.getInputStream(monitor);
			// the stream can be null if the user cancels the connection
			if (mapStream == null)
				return;
			Properties mappings = new Properties();
			mappings.load(mapStream);
			processMappings(mappings);
			loaded = true;
		} catch (IOException e) {
			throw Utilities.newCoreException(
				Policy.bind(
					"SiteURLFactory.UnableToAccessSiteStream",
					mapFile == null ? "" : mapFile.toExternalForm()),
				ISite.SITE_ACCESS_EXCEPTION,
				e);
		} finally {
			if (mapStream != null) {
				try {
					mapStream.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public boolean isLoaded() {
		return loaded;
	}

	private void processMappings(Properties mappings) {
		if (entries.isEmpty() == false)
			entries.clear();
		for (Enumeration enum = mappings.keys(); enum.hasMoreElements();) {
			String key = (String) enum.nextElement();
			if (key.startsWith("urlMap.")) {
				String pattern = key.substring(7);
				String value = (String) mappings.get(key);
				if (value != null && value.length() > 0) {
					String decodedValue = URLDecoder.decode(value);
					try {
						URL url = new URL(decodedValue);
						addEntry(pattern, url);
					} catch (MalformedURLException e) {
					}
				}
			}
		}
	}

	private void addEntry(String pattern, URL url) {
		if (pattern.equalsIgnoreCase("*"))
			defaultSite = new MapSite(url);
		else
			entries.add(new UpdateMapEntry(pattern, url));
	}
	/*
	 * Given the feature ID, returns the mapped update URL if
	 * found in the mappings. This URL will be used INSTEAD of
	 * the update URL encoded in the feature itself during the
	 * new update search.
	 */
	public IUpdateSiteAdapter getMappedSite(String id) {
		for (int i = 0; i < entries.size(); i++) {
			UpdateMapEntry entry = (UpdateMapEntry) entries.get(i);
			if (entry.matches(id))
				return entry.getSite();
		}
		return defaultSite;
	}
}