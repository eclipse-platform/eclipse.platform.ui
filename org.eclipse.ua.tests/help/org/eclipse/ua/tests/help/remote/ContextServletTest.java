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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ContextServletTest extends TestCase {
	
	private int mode;

	protected void setUp() throws Exception {
		BaseHelpSystem.ensureWebappRunning();
		mode = BaseHelpSystem.getMode();
		BaseHelpSystem.setMode(BaseHelpSystem.MODE_INFOCENTER);
	}
	
	protected void tearDown() throws Exception {
		BaseHelpSystem.setMode(mode);
	}

	public void testRemoteContextFound() throws Exception {
		Element[] topics = getContextsFromServlet("org.eclipse.ua.tests.test_cheatsheets");
		assertEquals(1, topics.length);
		assertEquals("abcdefg", topics[0].getAttribute("label")); 
	}
	
	public void testRemoteContextFoundDe() throws Exception {
		Element[] topics = getContextsUsingLocale
		    ("org.eclipse.ua.tests.test_cheatsheets", "de");
		assertEquals(1, topics.length);
		assertEquals("German Context", topics[0].getAttribute("label")); 
	}
	
	public void testRemoteContextNotFound() throws Exception {
		try {
			getContextsFromServlet("org.eclipse.ua.tests.no_such_context");
			fail("No exception thrown");
		} catch (IOException e) {
            // IO exception caught as expected
		}
	}

	private Element[] getContextsFromServlet(String phrase)
			throws Exception {
		int port = WebappManager.getPort();
		URL url = new URL("http", "localhost", port, "/help/context?id=" + URLEncoder.encode(phrase, "UTF-8"));
		return makeServletCall(url);
	}
	
	private Element[] getContextsUsingLocale(String phrase, String locale)
			throws Exception {
		int port = WebappManager.getPort();
		URL url = new URL("http", "localhost", port, "/help/context?id="
				+ URLEncoder.encode(phrase, "UTF-8") + "&lang=" + locale);
		return makeServletCall(url);
	}

	private Element[] makeServletCall(URL url) throws IOException,
			ParserConfigurationException, FactoryConfigurationError,
			SAXException {
		InputStream is = url.openStream();
		InputSource inputSource = new InputSource(is);
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		documentBuilder.setEntityResolver(new LocalEntityResolver());
		Document document = documentBuilder.parse(inputSource);
		Node root = document.getFirstChild();
		is.close();
		assertEquals("context", root.getNodeName());
		NodeList children = root.getChildNodes();
		List<Node> topics = new ArrayList<Node>();
		int length = children.getLength();
		for (int i = 0; i < length; i++) {
			Node next = children.item(i);
			if ("topic".equals(next.getNodeName())) {
				topics.add(next);
			}
		}
		return topics.toArray(new Element[topics.size()]);
	}
	
}
