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

public class IndexServletTest extends TestCase {
	
	private int mode;

	protected void setUp() throws Exception {
		BaseHelpSystem.ensureWebappRunning();
		mode = BaseHelpSystem.getMode();
		BaseHelpSystem.setMode(BaseHelpSystem.MODE_INFOCENTER);
	}
	
	protected void tearDown() throws Exception {
		BaseHelpSystem.setMode(mode);
	}

	public void testIndexServletContainsSimpleWord() throws Exception {
		Node root = getIndexContributions("en");
		Element[] UARoot = findEntryInAllContributions(root, "xyz");
	    assertEquals(1, UARoot.length);
	}
	
	public void testIndexServletContainsWordWithAccent() throws Exception {
		Node root = getIndexContributions("en");
		Element[] UARoot = findEntryInAllContributions(root, "\u00E1mbito");
	    assertEquals(1, UARoot.length);
	}

	public void testIndexServletContainsWordWithGt() throws Exception {
		Node root = getIndexContributions("en");
		Element[] UARoot = findEntryInAllContributions(root, "character >");
	    assertEquals(1, UARoot.length);
	}

	public void testIndexServletContainsWordWithLt() throws Exception {
		Node root = getIndexContributions("en");
		Element[] UARoot = findEntryInAllContributions(root, "character <");
	    assertEquals(1, UARoot.length);
	}
	public void testIndexServletContainsWordWithAmp() throws Exception {
		Node root = getIndexContributions("en");
		Element[] UARoot = findEntryInAllContributions(root, "character &");
	    assertEquals(1, UARoot.length);
	}
	public void testIndexServletContainsWordWithQuot() throws Exception {
		Node root = getIndexContributions("en");
		Element[] UARoot = findEntryInAllContributions(root, "character \"");
	    assertEquals(1, UARoot.length);
	}
	public void testIndexServletContainsWordWithApostrophe() throws Exception {
		Node root = getIndexContributions("en");
		Element[] UARoot = findEntryInAllContributions(root, "character '");
	    assertEquals(1, UARoot.length);
	}

	public void testDeWordNotInEnIndex() throws Exception {
		Node root = getIndexContributions("en");
		Element[] UARoot = findEntryInAllContributions(root, "munich");
	    assertEquals(0, UARoot.length);
	}
	
	public void testWordInDeIndex() throws Exception {
		Node root = getIndexContributions("de");
		Element[] UARoot = findEntryInAllContributions(root, "munich");
	    assertEquals(1, UARoot.length);
	}
	
	public void testWordNotInDeIndex() throws Exception {
		Node root = getIndexContributions("de");
		Element[] UARoot = findEntryInAllContributions(root, "xyz");
	    assertEquals(0, UARoot.length);
	}
	
	private Element[] findEntryInAllContributions(Node parent, String keyword) {
		NodeList contributions = parent.getChildNodes();
		List<Node> results = new ArrayList<Node>();
		for (int i = 0; i < contributions.getLength(); i++) {
			Node next = contributions.item(i);
			if (next instanceof Element)  {
				Element nextElement = (Element)next;
				if ("indexContribution".equals(nextElement.getTagName())) {
					findEntryInIndexContribution(nextElement, keyword, results);
			    }
			}
		}
		return results.toArray(new Element[results.size()]);
	} 
	
	private void findEntryInIndexContribution(Element parent, String keyword,
			List<Node> results) {
		NodeList indexes = parent.getChildNodes();
		for (int i = 0; i < indexes.getLength(); i++) {
			Node next = indexes.item(i);
			if (next instanceof Element) {
				Element nextElement = (Element) next;
				if ("index".equals(nextElement.getTagName())) {
					findMatchingChildEntry(nextElement, keyword, results);
				}
			}
		}
	}

	private void findMatchingChildEntry(Element parent, String keyword,
			List<Node> results) {
		NodeList topLevelEntries = parent.getChildNodes();
		for (int i = 0; i < topLevelEntries.getLength(); i++) {
			Node next = topLevelEntries.item(i);
			if (next instanceof Element) {
				Element nextElement = (Element) next;
				if ("entry".equals(nextElement.getTagName())
						&& keyword.equals(nextElement
								.getAttribute("keyword"))) {

					results.add(next);
				}
			}
		}
	}

	private Node getIndexContributions( String locale)
			throws Exception {
		int port = WebappManager.getPort();
		URL url = new URL("http", "localhost", port, "/help/index?lang=" + locale);
		InputStream is = url.openStream();
		InputSource inputSource = new InputSource(is);
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		documentBuilder.setEntityResolver(new LocalEntityResolver());
		Document document = documentBuilder.parse(inputSource);
		Node root = document.getFirstChild();
		is.close();
		assertEquals("indexContributions", root.getNodeName());
		return root;
	}
	
}
