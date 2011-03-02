/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.help.webapp.service;


import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.entityresolver.LocalEntityResolver;
import org.eclipse.help.internal.server.WebappManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class ExtensionServiceTest extends TestCase {

	private int mode;

	protected void setUp() throws Exception {
		BaseHelpSystem.ensureWebappRunning();
		mode = BaseHelpSystem.getMode();
		BaseHelpSystem.setMode(BaseHelpSystem.MODE_INFOCENTER);
	}
	
	protected void tearDown() throws Exception {
		BaseHelpSystem.setMode(mode);
	}

	public void testExtensionServiceContributionExactMatch1() throws Exception {
		Node root = getContentExtensions("en");
		Element[] UARoot = findContributionByContent(root, 
				"/org.eclipse.ua.tests/data/help/dynamic/shared/doc2.xml#element.1");
	    assertEquals(1, UARoot.length);
	}

	public void testExtensionServiceContributionExactMatch3() throws Exception {
		Node root = getContentExtensions("en");
		Element[] UARoot = findContributionByContent(root, 
				"/org.eclipse.ua.tests/data/help/dynamic/shared/doc2.xml#element.3");
	    assertEquals(1, UARoot.length);
	}
	
	public void testExtensionServiceContributionNoMatch() throws Exception {
		Node root = getContentExtensions("en");
		Element[] UARoot = findContributionByContent(root, 
				"/org.eclipse.ua.tests/data/help/dynamic/shared/doc2.xml#element.4");
	    assertEquals(0, UARoot.length);
	}
	
	public void testExtensionServiceContributionByPath() throws Exception {
		Node root = getContentExtensions("en");
		Element[] UARoot = findContributionByPath(root, 
				"/org.eclipse.ua.tests/data/help/dynamic/extension.xml#anchor.invalidcontribution");
		assertEquals(2, UARoot.length);
	}

	public void testExtensionServiceReplacementExactMatch() throws Exception {
		Node root = getContentExtensions("en");
		Element[] UARoot = findReplacementByContent(root, 
				"/org.eclipse.ua.tests/data/help/dynamic/shared/doc2.xml#element.1");
	    assertEquals(1, UARoot.length);
	}
	
	public void testExtensionServiceReplacementNoMatch() throws Exception {
		Node root = getContentExtensions("en");
		Element[] UARoot = findReplacementByContent(root, 
				"/org.eclipse.ua.tests/data/help/dynamic/shared/doc2.xml#element.3");
		assertEquals(0, UARoot.length);
	}
	
	public void testExtensionServiceReplacementByPath() throws Exception {
		Node root = getContentExtensions("en");
		Element[] UARoot = findReplacementByPath(root, 
				"/org.eclipse.ua.tests/data/help/dynamic/shared/doc1.xml#element.2");
		assertEquals(1, UARoot.length);
	}
	
	private Element[] findContributionByContent(Node root, String content) {
		return findChildren(root, "contribution", "content", content);
	}
	
	private Element[] findContributionByPath(Node root, String path) {
		return findChildren(root, "contribution", "path", path);
	}
	
	private Element[] findReplacementByContent(Node root, String content) {
		return findChildren(root, "replacement", "content", content);
	}
	
	private Element[] findReplacementByPath(Node root, String path) {
		return findChildren(root, "replacement", "path", path);
	}
	
	private Element[] findChildren(Node parent, String childKind, 
			String attributeName, String attributeValue) {
		NodeList contributions = parent.getChildNodes();
		List<Node> results = new ArrayList<Node>();
		for (int i = 0; i < contributions.getLength(); i++) {
			Node next = contributions.item(i);
			if (next instanceof Element) {
				Element nextElement = (Element)next;
				if ( childKind.equals(nextElement.getTagName()) && attributeValue.equals(nextElement.getAttribute(attributeName))) {
		
				    results.add(next);	
				}
			}
		}
		return results.toArray(new Element[results.size()]);
	}

	private Node getContentExtensions(String locale)
			throws Exception {
		int port = WebappManager.getPort();
		URL url = new URL("http", "localhost", port, "/help/vs/service/extension?lang=" + locale);
		InputStream is = url.openStream();
		InputSource inputSource = new InputSource(is);
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		documentBuilder.setEntityResolver(new LocalEntityResolver());
		Document document = documentBuilder.parse(inputSource);
		Node root = document.getFirstChild();
		is.close();
		assertEquals("contentExtensions", root.getNodeName());
		return root;
	}

	public void testExtensionFragmentServiceXMLSchema() 
			throws Exception {
		int port = WebappManager.getPort();
		URL url = new URL("http", "localhost", port, "/help/vs/service/extension?lang=en");
		URL schemaUrl = new URL("http", "localhost", port, "/help/test/schema/xml/extension.xsd");
		String schema = schemaUrl.toString();
		String uri = url.toString();
		String result = SchemaValidator.testXMLSchema(uri, schema);
		
		assertEquals("URL: \"" + uri + "\" is ", "valid", result);
	}

	public void testExtensionFragmentServiceJSONSchema() 
			throws Exception {
//		fail("Not yet implemented.");
	}

}
