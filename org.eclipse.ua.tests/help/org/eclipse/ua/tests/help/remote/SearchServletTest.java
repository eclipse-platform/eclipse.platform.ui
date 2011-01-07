/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.help.remote;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.entityresolver.LocalEntityResolver;
import org.eclipse.help.internal.server.WebappManager;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SearchServletTest extends TestCase {
	
	private int mode;

	protected void setUp() throws Exception {
		BaseHelpSystem.ensureWebappRunning();
		mode = BaseHelpSystem.getMode();
		BaseHelpSystem.setMode(BaseHelpSystem.MODE_INFOCENTER);
	}
	
	protected void tearDown() throws Exception {
		BaseHelpSystem.setMode(mode);
	}
	
	public void testRemoteSearchNotFound() throws Exception {
		Node[] hits = getSearchHitsFromServlet("duernfryehd");
		assertEquals(0, hits.length);
	}

	public void testRemoteSearchFound() throws Exception {
		Node[] hits = getSearchHitsFromServlet("jehcyqpfjs");
		assertEquals(1, hits.length);
	}

	public void testRemoteSearchOrFound() throws Exception {
		Node[] hits = getSearchHitsFromServlet("jehcyqpfjs OR duernfryehd");
		assertEquals(1, hits.length);
	}

	public void testRemoteSearchAndFound() throws Exception {
		Node[] hits = getSearchHitsFromServlet("jehcyqpfjs AND vkrhjewiwh");
		assertEquals(1, hits.length);
	}

	public void testRemoteSearchAndNotFound() throws Exception {
		Node[] hits = getSearchHitsFromServlet("jehcyqpfjs AND duernfryehd");
		assertEquals(0, hits.length);
	}
	
	public void testRemoteSearchExactMatchFound() throws Exception {
		Node[] hits = getSearchHitsFromServlet("\"jehcyqpfjs vkrhjewiwh\"");
		assertEquals(1, hits.length);
	}

	public void testRemoteSearchExactMatchNotFound() throws Exception {
		Node[] hits = getSearchHitsFromServlet("\"vkrhjewiwh jehcyqpfjs\"");
		assertEquals(0, hits.length);
	}

	public void testRemoteSearchWordNotInDefaultLocale() throws Exception {
		Node[] hits = getSearchHitsFromServlet("deuejwuid");
		assertEquals(0, hits.length);
	}

	public void testRemoteSearchUsingDeLocale() throws Exception {
		Node[] hits = getSearchHitsUsingLocale("deuejwuid", "de");
		assertEquals(1, hits.length);
	}
	
	public void testRemoteSearchUsingEnLocale() throws Exception {
		Node[] hits = getSearchHitsUsingLocale("deuejwuid", "en");
		assertEquals(0, hits.length);
	}

	private Node[] getSearchHitsFromServlet(String phrase)
			throws Exception {
		int port = WebappManager.getPort();
		URL url = new URL("http", "localhost", port, "/help/search?phrase=" + URLEncoder.encode(phrase, "UTF-8"));
		return makeServletCall(url);
	}
	
	private Node[] getSearchHitsUsingLocale(String phrase, String locale)
			throws Exception {
		int port = WebappManager.getPort();
		URL url = new URL("http", "localhost", port, "/help/search?phrase="
				+ URLEncoder.encode(phrase, "UTF-8") + "&lang=" + locale);
		return makeServletCall(url);
	}

	private Node[] makeServletCall(URL url) throws IOException,
			ParserConfigurationException, FactoryConfigurationError,
			SAXException {
		InputStream is = url.openStream();
		InputSource inputSource = new InputSource(is);
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		documentBuilder.setEntityResolver(new LocalEntityResolver());
		Document document = documentBuilder.parse(inputSource);
		Node root = document.getFirstChild();
		is.close();
		assertEquals("searchHits", root.getNodeName());
		NodeList children = root.getChildNodes();
		List<Node> hits = new ArrayList<Node>();
		int length = children.getLength();
		for (int i = 0; i < length; i++) {
			Node next = children.item(i);
			if ("hit".equals(next.getNodeName())) {
				hits.add(next);
			}
		}
		return hits.toArray(new Node[hits.size()]);
	}
	
}
