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

public class TocFragmentServiceTest extends TestCase {

	private int mode;

	protected void setUp() throws Exception {
		BaseHelpSystem.ensureWebappRunning();
		mode = BaseHelpSystem.getMode();
		BaseHelpSystem.setMode(BaseHelpSystem.MODE_INFOCENTER);
	}
	
	protected void tearDown() throws Exception {
		BaseHelpSystem.setMode(mode);
	}

	public void testTocFragmentServiceContainsUAToc() throws Exception {
		int port = WebappManager.getPort();
		URL url = new URL("http", "localhost", port, 
				"/help/vs/service/tocfragment?lang=en");
		Node root = getTreeData(url);
		Element[] UARoot = findNodeById(root, 
				"/org.eclipse.ua.tests/data/help/toc/root.xml");
	    assertEquals(1, UARoot.length);
	}

	public void testTocFragmentServiceContainsFilteredToc() throws Exception {
		int port = WebappManager.getPort();
		URL url = new URL("http", "localhost", port, 
				"/help/vs/service/tocfragment?lang=en&toc=/org.eclipse.ua.tests/data/help/toc/root.xml&path=2");
		Node root = getTreeData(url);
		Element[] UARoot = findNodeById(root, 
				"/org.eclipse.ua.tests/data/help/toc/root.xml");
		assertEquals(1, UARoot.length);
		Element[] filterNode = findNodeById(UARoot[0], "2");
		assertEquals(1, filterNode.length);
		Element[] results = findChildren(filterNode[0], "node", "href", 
				"../topic/org.eclipse.ua.tests/data/help/toc/filteredToc/simple_page.html");
		assertEquals(24, results.length);
		
		results = findChildren(filterNode[0], "node", "href", 
		"../topic/org.eclipse.ua.tests/data/help/toc/filteredToc/helpInstalled.html");
		assertEquals(1, results.length);
	}
	
	/*
	 * These tests are still failing - see Bug 338732
	 * 
	 * 
	public void testReadEnToc() throws Exception {
		int port = WebappManager.getPort();
		URL url = new URL("http", "localhost", port, 
				"/help/vs/service/tocfragment?lang=en&toc=/org.eclipse.ua.tests/data/help/toc/root.xml&path=7");
		Node root = getTreeData(url);
		Element[] UARoot = findNodeById(root, 
				"/org.eclipse.ua.tests/data/help/toc/root.xml");
		assertEquals(1, UARoot.length);
		Element[] searchNode = findNodeById(UARoot[0], "7");
		assertEquals(1, searchNode.length);
		Element[] topicEn = findChildren(searchNode[0], "node", "href", 
				"../topic/org.eclipse.ua.tests/data/help/search/test_en.html");
		assertEquals(1, topicEn.length);
		Element[] topicDe = findChildren(searchNode[0], "node", "href", 
				"../topic/org.eclipse.ua.tests/data/help/search/test_de.html");
		assertEquals(0, topicDe.length);
	}	
	
	public void testReadDeToc() throws Exception {
		int port = WebappManager.getPort();
		URL url = new URL("http", "localhost", port, 
				"/help/vs/service/tocfragment?lang=de&toc=/org.eclipse.ua.tests/data/help/toc/root.xml&path=7");
		Node root = getTreeData(url);
		Element[] UARoot = findNodeById(root, 
				"/org.eclipse.ua.tests/data/help/toc/root.xml");
		assertEquals(1, UARoot.length);
		Element[] searchNode = findNodeById(UARoot[0], "7");
		Element[] topicEn = findChildren(searchNode[0], "node", "href", 
				"../topic/org.eclipse.ua.tests/data/help/search/test_en.html");
		assertEquals(0, topicEn.length);
		Element[] topicDe = findChildren(searchNode[0], "node", "href", 
				"../topic/org.eclipse.ua.tests/data/help/search/test_de.html");
		assertEquals(1, topicDe.length);
	}
	
	*/
	
	private Element[] findNodeById(Node root, String id) {
		return findChildren(root, "node", "id", id);
	}
	
	private Element[] findChildren(Node parent, String childKind, 
			String attributeName, String attributeValue) {
		NodeList nodes = parent.getChildNodes();
		List<Node> results = new ArrayList<Node>();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node next = nodes.item(i);
			if (next instanceof Element) {
				Element nextElement = (Element)next;
				if ( childKind.equals(nextElement.getTagName()) 
						&& attributeValue.equals(nextElement.getAttribute(attributeName))) {
		
				    results.add(next);	
				}
			}
		}
		return (Element[]) results.toArray(new Element[results.size()]);
	}
	 

	private Node getTreeData(URL url)
			throws Exception {
		InputStream is = url.openStream();
		InputSource inputSource = new InputSource(is);
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		documentBuilder.setEntityResolver(new LocalEntityResolver());
		Document document = documentBuilder.parse(inputSource);
		Node root = document.getFirstChild();
		is.close();
		assertEquals("tree_data", root.getNodeName());
		return root;
	}

	public void testTocFragmentServiceXMLSchema() 
			throws Exception {
		int port = WebappManager.getPort();
		URL url = new URL("http", "localhost", port, "/help/vs/service/tocfragment?lang=en");
		URL schemaUrl = new URL("http", "localhost", port, "/help/test/schema/xml/tocfragment.xsd");
		String schema = schemaUrl.toString();
		String uri = url.toString();
		String result = SchemaValidator.testXMLSchema(uri, schema);
		
		assertEquals("URL: \"" + uri + "\" is ", "valid", result);
	}
	
	public void testTocFragmentServiceJSONSchema() 
			throws Exception {
//		fail("Not yet implemented.");
	}

}
