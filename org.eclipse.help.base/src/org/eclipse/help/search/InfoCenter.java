/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.search;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.ArrayList;

import javax.xml.parsers.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.IHelpResource;
import org.eclipse.help.internal.base.*;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * This implementation of <code>ISearchEngine</code> interface performs search
 * by running a query on the remote InfoCenter and presenting the results
 * locally. Instances of this engine type are required to supply the URL to the
 * InfoCenter.
 * 
 * <p>
 * This class is made public in order to be instantiated and parametrized
 * directly in the extentsions. Clients are required to supply the required URL
 * as a parameter <code>url</code>.
 * 
 * <p>
 * This class is not expected to be subclassed or otherwise accessed
 * programmatically.
 * 
 * @since 3.1
 */

public final class InfoCenter implements ISearchEngine {
	private Hashtable tocs;

	public static class Scope implements ISearchScope {
		String url;

		boolean searchSelected;

		String[] tocs;

		public Scope(String url, boolean searchSelected, String[] tocs) {
			this.url = url;
			this.searchSelected = searchSelected;
			this.tocs = tocs;
		}
	}

	class InfoCenterResult implements ISearchEngineResult {
		private IHelpResource category;

		private Element node;

		private String baseURL;

		public InfoCenterResult(String baseURL, Element node) {
			this.baseURL = baseURL;
			this.node = node;
			createCategory(node);
		}

		private void createCategory(Element node) {
			final String href = node.getAttribute("toc"); //$NON-NLS-1$
			final String label = node.getAttribute("toclabel"); //$NON-NLS-1$
			if (href != null && label != null) {
				category = (IHelpResource) tocs.get(href);
				if (category == null) {
					category = new IHelpResource() {
						public String getLabel() {
							return label;
						}

						public String getHref() {
							return href;
						}
					};
					tocs.put(href, category);
				}
			}
		}

		public String getLabel() {
			return node.getAttribute("label"); //$NON-NLS-1$
		}

		public String getDescription() {
			return null;
		}

		public IHelpResource getCategory() {
			return category;
		}

		public String getHref() {
			return node.getAttribute("href"); //$NON-NLS-1$
		}

		public float getScore() {
			String value = node.getAttribute("score");
			if (value != null)
				return Float.parseFloat(value);
			return (float) 0.0;
		}

		public boolean getForceExternalWindow() {
			return false;
		}

		public String toAbsoluteHref(String href, boolean frames) {
			String url = baseURL;
			if (!url.endsWith("/")) //$NON-NLS-1$
				url = url + "/"; //$NON-NLS-1$
			if (frames) {
				return url + "topic" + href; //$NON-NLS-1$
			} else
				return url + "topic" + href + "&noframes=true"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * The default constructor.
	 */
	public InfoCenter() {
		tocs = new Hashtable();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ISearchEngine#run(String, ISearchScope,
	 *      ISearchEngineResultCollector, IProgressMonitor)
	 */
	public void run(String query, ISearchScope scope,
			ISearchEngineResultCollector collector, IProgressMonitor monitor)
			throws CoreException {
		URL url = createURL(query, (Scope) scope);
		if (url == null)
			return;
		InputStream is = null;
		tocs.clear();
		try {
			URLConnection connection = url.openConnection();
			is = connection.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "utf-8"));//$NON-NLS-1$
			load(((Scope) scope).url, reader, collector, monitor);
			reader.close();
		} catch (FileNotFoundException e) {
			reportError(
					HelpBaseResources.getString("InfoCenter.fileNotFound"), e, collector); //$NON-NLS-1$
		} catch (IOException e) {
			reportError(
					HelpBaseResources.getString("InfoCenter.io"), e, collector); //$NON-NLS-1$
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private void reportError(String message, IOException e,
			ISearchEngineResultCollector collector) {
		Status status = new Status(IStatus.ERROR, HelpBasePlugin.PLUGIN_ID,
				IStatus.OK, message, e);
		collector.error(status);
	}

	private void load(String baseURL, Reader r,
			ISearchEngineResultCollector collector, IProgressMonitor monitor) {
		Document document = null;
		try {
			DocumentBuilder parser = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			// parser.setProcessNamespace(true);
			document = parser.parse(new InputSource(r));

			// Strip out any comments first
			Node root = document.getFirstChild();
			while (root.getNodeType() == Node.COMMENT_NODE) {
				document.removeChild(root);
				root = document.getFirstChild();
			}
			load(baseURL, document, (Element) root, collector, monitor);
		} catch (ParserConfigurationException e) {
			// ignore
		} catch (IOException e) {
			// ignore
		} catch (SAXException e) {
			// ignore
		}
	}

	private void load(String baseURL, Document doc, Element root,
			ISearchEngineResultCollector collector, IProgressMonitor monitor) {
		ArrayList list = new ArrayList();
		NodeList topics = root.getElementsByTagName("topic"); //$NON-NLS-1$
		ISearchEngineResult[] results = new ISearchEngineResult[topics
				.getLength()];
		for (int i = 0; i < topics.getLength(); i++) {
			Element el = (Element) topics.item(i);
			results[i] = new InfoCenterResult(baseURL, el);
		}
		collector.add(results);
	}

	private URL createURL(String query, Scope scope) {
		StringBuffer buf = new StringBuffer();
		buf.append(scope.url);
		if (!scope.url.endsWith("/")) //$NON-NLS-1$
			buf.append("/search?searchWord="); //$NON-NLS-1$
		else
			buf.append("search?searchWord="); //$NON-NLS-1$
		try {
			buf.append(URLEncoder.encode(query, "UTF-8")); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			buf.append(query);
		}
		buf.append("&locale="); //$NON-NLS-1$
		buf.append(Platform.getNL());
		if (scope.searchSelected && scope.tocs != null) {
			buf.append("&scopedSearch=true"); //$NON-NLS-1$
			for (int i = 0; i < scope.tocs.length; i++) {
				String toc;
				try {
					toc = URLEncoder.encode(scope.tocs[i], "UTF-8"); //$NON-NLS-1$
				} catch (UnsupportedEncodingException e) {
					toc = scope.tocs[i];
				}
				buf.append("&scope="); //$NON-NLS-1$
				buf.append(toc);
			}
		}
		try {
			return new URL(buf.toString());
		} catch (MalformedURLException e) {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ISearchEngine#cancel()
	 */

	public void cancel() {
	}
}