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
package org.eclipse.update.internal.search;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.xml.parsers.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.search.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * 
 * This class opens connection to the update map resource
 * and parses the file to load update URL mappings.
 * These mappings are used to redirect new updates search
 * when the redirection pattern matches.
 */

public class UpdatePolicy {
	private static final String TAG_POLICY = "update-policy"; //$NON-NLS-1$
	private static final String TAG_URL_MAP = "url-map"; //$NON-NLS-1$
	private static final String ATT_URL = "url"; //$NON-NLS-1$
	private static final String ATT_PATTERN = "pattern"; //$NON-NLS-1$

	private static final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

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
		public String getPattern() {
			return pattern;
		}
	}

	private ArrayList entries;
	private IUpdateSiteAdapter defaultSite;
	private boolean loaded = false;
	private boolean fallbackAllowed = true;

	public UpdatePolicy() {
		entries = new ArrayList();
	}

	public void load(URL mapFile, IProgressMonitor monitor)
		throws CoreException {
		InputStream policyStream = null;
		try {
			Response response = UpdateCore.getPlugin().get(mapFile);
			UpdateManagerUtils.checkConnectionResult(response, mapFile);
			policyStream = response.getInputStream(monitor);
			// the stream can be null if the user cancels the connection
			if (policyStream == null)
				return;
			
			documentBuilderFactory.setNamespaceAware(true);
			DocumentBuilder parser = documentBuilderFactory.newDocumentBuilder();
			Document doc = parser.parse(new InputSource(policyStream));

			processUpdatePolicy(doc);
			loaded = true;
		} catch (IOException e) {
			throw Utilities.newCoreException(
				Policy.bind(
					"SiteURLFactory.UnableToAccessSiteStream", //$NON-NLS-1$
					mapFile == null ? "" : mapFile.toExternalForm()), //$NON-NLS-1$
				ISite.SITE_ACCESS_EXCEPTION,
				e);
		} catch (SAXException e) {
			throw Utilities.newCoreException(
				Policy.bind("UpdatePolicy.parsePolicy"), //$NON-NLS-1$
				0,
				e);

		} catch(ParserConfigurationException e) {
			throw Utilities.newCoreException(
				Policy.bind("UpdatePolicy.parsePolicy"), //$NON-NLS-1$
				0,
				e);
		} finally {
			if (policyStream != null) {
				try {
					policyStream.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public boolean isLoaded() {
		return loaded;
	}
	
	/*
	 * Given the feature ID, returns the mapped update URL if
	 * found in the mappings. This URL will be used INSTEAD of
	 * the update URL encoded in the feature itself during the
	 * new update search.
	 * <p>In case of multiple matches (e.g. org.eclipse and org.eclipse.platform)
	 * the URL for the longer pattern will be picked (i.e. org.eclipse.platform).
	 */
	public IUpdateSiteAdapter getMappedSite(String id) {
		UpdateMapEntry lastEntry = null;
		for (int i = 0; i < entries.size(); i++) {
			UpdateMapEntry entry = (UpdateMapEntry) entries.get(i);
			if (entry.matches(id)) {
				if (lastEntry == null)
					lastEntry = entry;
				else {
					// Choose the match with longer pattern.
					// For example, if two matches are found:
					// 'org.eclipse' and 'org.eclipse.platform',
					// pick 'org.eclipse.platform'.
					String pattern = entry.getPattern();
					String lastPattern = lastEntry.getPattern();
					if (pattern.length() > lastPattern.length())
						lastEntry = entry;
				}
			}
		}
		if (lastEntry != null)
			return lastEntry.getSite();
		else
			return defaultSite;
	}

	public boolean isFallbackAllowed() {
		return fallbackAllowed;
	}
	
	private void reset() {
		if (entries.isEmpty() == false)
			entries.clear();
	}

	private void processUpdatePolicy(Document document) throws CoreException {
		Node root = document.getDocumentElement();
		reset();
		
		if (root.getNodeName().equals(TAG_POLICY)==false)
			throwCoreException("'"+TAG_POLICY+Policy.bind("UpdatePolicy.policyExpected"), null); //$NON-NLS-1$ //$NON-NLS-2$
				
		NodeList nodes = root.getChildNodes();
		
		for (int i=0; i<nodes.getLength(); i++) {
			Node child = nodes.item(i);
			if (child.getNodeType() != Node.ELEMENT_NODE)
				continue;
			String tag = child.getNodeName();
			if (tag.equals(TAG_URL_MAP))
				processMapNode(child);
		}
	}
	private void processMapNode(Node node) throws CoreException {
		String pattern = getAttribute(node, ATT_PATTERN);
		String urlName = getAttribute(node, ATT_URL);
		
		assertNotNull(ATT_PATTERN, pattern);
		assertNotNull(ATT_URL, urlName);
		
		// empty url means feature is not updateable
		if (urlName.trim().length() == 0) {
			addEntry(pattern, null);
			return;
		}

		try {
			String decodedValue = URLDecoder.decode(urlName, "UTF-8"); //$NON-NLS-1$
			URL url = new URL(decodedValue);
			addEntry(pattern, url);
		} catch (MalformedURLException e) {
			throwCoreException(Policy.bind("UpdatePolicy.invalidURL")+urlName, null); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
		}
	}
	
	private void assertNotNull(String name, String value) throws CoreException {
		if (value==null)
			throwCoreException(name+Policy.bind("UpdatePolicy.nameNoNull"), null); //$NON-NLS-1$
	}
	
	private String getAttribute(Node node, String name) {
		NamedNodeMap attMap = node.getAttributes();
		Node att = attMap.getNamedItem(name);
		if (att==null) return null;
		return att.getNodeValue();
	}

	private void addEntry(String pattern, URL url) {
		if (pattern.equalsIgnoreCase("*")) //$NON-NLS-1$
			defaultSite = new MapSite(url);
		else
			entries.add(new UpdateMapEntry(pattern, url));
	}
	
	private void throwCoreException(String message, Throwable e) throws CoreException {
		String fullMessage = Policy.bind("UpdatePolicy.UpdatePolicy")+message; //$NON-NLS-1$
		throw Utilities.newCoreException(fullMessage, 0, e);
	}
}
