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
import java.util.ArrayList;

import javax.xml.parsers.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.IHelpResource;
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
 * as a parameter <code>url</code>. This class is not expected to be
 * subclassed or otherwise accessed programmatically.
 * 
 * @since 3.1
 */

public final class InfoCenter implements ISearchEngine {
	public static class Scope implements ISearchScope {
		String url;

		boolean searchSelected;

		String[] tocs;

		public Scope(String url, boolean searchSelected, String [] tocs) {
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
			this.node = node;
			this.baseURL = baseURL;
			createCategory();
		}

		private void createCategory() {
			category = new IHelpResource() {
				public String getHref() {
					return node.getAttribute("toc");
				}

				public String getLabel() {
					return node.getAttribute("toclabel");
				}
			};
		}

		public String getLabel() {
			return node.getAttribute("label");
		}

		public String getDescription() {
			return node.getAttribute("description");
		}

		public IHelpResource getCategory() {
			return category;
		}

		public String getHref() {
			return node.getAttribute("href");
		}

		public float getScore() {
			String value = node.getAttribute("score");
			return Float.parseFloat(value);
		}

		public boolean getForceExternalWindow() {
			return false;
		}

		public String toAbsoluteHref(String href, boolean frames) {
			String url = baseURL;
			if (!url.endsWith("/"))
				url = url+"/";
			if (frames) {
				return url+"topic"+href;
			}
			else
				return url+"topic"+href + "&noframes=true";
		}
	}

	/**
	 * The default constructor.
	 */
	public InfoCenter() {
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
		try {
			URLConnection connection = url.openConnection();
			is = connection.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "utf-8"));//$NON-NLS-1$
			load(((Scope)scope).url, reader, collector, monitor);
			reader.close();
		} catch (FileNotFoundException e) {
			reportError("File not found", e, collector);
		} catch (IOException e) {
			reportError("I/O exception during search", e, collector);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	private void reportError(String message, IOException e, ISearchEngineResultCollector collector) {
		Status status = new Status(IStatus.ERROR, HelpBasePlugin.PLUGIN_ID,
				IStatus.OK, message, e);
		collector.error(status);
	}

	private void load(String baseURL, Reader r, ISearchEngineResultCollector collector,
			IProgressMonitor monitor) {
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
		NodeList topics = root.getElementsByTagName("topic");
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
		if (!scope.url.endsWith("/"))
			buf.append("/search?searchWord=");
		else
			buf.append("search?searchWord=");
		try {
			buf.append(URLEncoder.encode(query, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			buf.append(query);
		}
		buf.append("&locale=");
		buf.append(Platform.getNL());
		if (scope.searchSelected && scope.tocs!=null) {
			buf.append("&scopedSearch=true");
			for (int i=0; i<scope.tocs.length; i++) {
				String toc;
				try {
					toc = URLEncoder.encode(scope.tocs[i], "UTF-8");
				}
				catch (UnsupportedEncodingException e) {
					toc = scope.tocs[i];
				}
				buf.append("&scope=");
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