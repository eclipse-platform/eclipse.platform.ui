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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.entityresolver.LocalEntityResolver;
import org.eclipse.help.internal.server.WebappManager;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class IndexFragmentServiceTest extends TestCase {

	private int mode;

	protected void setUp() throws Exception {
		BaseHelpSystem.ensureWebappRunning();
		mode = BaseHelpSystem.getMode();
		BaseHelpSystem.setMode(BaseHelpSystem.MODE_INFOCENTER);
	}
	
	protected void tearDown() throws Exception {
		BaseHelpSystem.setMode(mode);
	}

	public void testIndexServletEn() throws Exception {
		getTreeData("en");
	}

	public void testIndexServletDe() throws Exception {
		getTreeData("de");
	}

	private void getTreeData(String locale)
			throws Exception {
		int port = WebappManager.getPort();
		URL url = new URL("http", "localhost", port, "/help/vs/service/indexfragment?lang=" + locale);
		InputStream is = url.openStream();
		InputSource inputSource = new InputSource(is);
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		documentBuilder.setEntityResolver(new LocalEntityResolver());
		Document document = documentBuilder.parse(inputSource);
		Node root = document.getFirstChild();
		is.close();
		assertEquals("tree_data", root.getNodeName());
	}

	public void testIndexFragmentServiceXMLSchema() 
			throws Exception {
		int port = WebappManager.getPort();
		URL url = new URL("http", "localhost", port, "/help/vs/service/indexfragment?lang=en");
		URL schemaUrl = new URL("http", "localhost", port, "/help/test/schema/xml/indexfragment.xsd");
		String schema = schemaUrl.toString();
		String uri = url.toString();
		String result = SchemaValidator.testXMLSchema(uri, schema);
		
		assertEquals("URL: \"" + uri + "\" is ", "valid", result);
	}

	public void testIndexFragmentServiceJSONSchema() 
			throws Exception {
//		fail("Not yet implemented.");
	}

}
