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

import org.eclipse.help.internal.entityresolver.LocalEntityResolver;
import org.eclipse.help.internal.server.WebappManager;
import org.eclipse.ua.tests.help.remote.IndexServletTest;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class IndexServiceTest extends IndexServletTest {

	protected Node getIndexContributions( String locale)
			throws Exception {
		int port = WebappManager.getPort();
		URL url = new URL("http", "localhost", port, "/help/vs/service/index?lang=" + locale);
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

	public void testIndexServiceXMLSchema() 
			throws Exception {
		int port = WebappManager.getPort();
		URL url = new URL("http", "localhost", port, "/help/vs/service/index?lang=en");
		URL schemaUrl = new URL("http", "localhost", port, "/help/test/schema/xml/index.xsd");
		String schema = schemaUrl.toString();
		String uri = url.toString();
		String result = SchemaValidator.testXMLSchema(uri, schema);
		
		assertEquals("URL: \"" + uri + "\" is ", "valid", result);
	}

	public void testIndexServiceJSONSchema() 
			throws Exception {
//		fail("Not yet implemented.");
	}

}
