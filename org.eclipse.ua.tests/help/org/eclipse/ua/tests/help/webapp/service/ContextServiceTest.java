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

import java.net.URL;
import java.net.URLEncoder;

import org.eclipse.help.internal.server.WebappManager;
import org.eclipse.ua.tests.help.remote.ContextServletTest;
import org.w3c.dom.Element;

public class ContextServiceTest extends ContextServletTest {

	protected Element[] getContextsFromServlet(String phrase)
			throws Exception {
		int port = WebappManager.getPort();
		URL url = new URL("http", "localhost", port, "/help/vs/service/context?id=" 
				+ URLEncoder.encode(phrase, "UTF-8"));
		return makeServletCall(url);
	}
	
	protected Element[] getContextsUsingLocale(String phrase, String locale)
			throws Exception {
		int port = WebappManager.getPort();
		URL url = new URL("http", "localhost", port, "/help/vs/service/context?id="
				+ URLEncoder.encode(phrase, "UTF-8") + "&lang=" + locale);
		return makeServletCall(url);
	}

	public void testContextServiceXMLSchema() 
			throws Exception {
		int port = WebappManager.getPort();
		URL url = new URL("http", "localhost", port, "/help/vs/service/context?id=org.eclipse.ua.tests.test_cheatsheets&lang=en");
		URL schemaUrl = new URL("http", "localhost", port, "/help/test/schema/xml/context.xsd");
		String schema = schemaUrl.toString();
		String uri = url.toString();
		String result = SchemaValidator.testXMLSchema(uri, schema);
		
		assertEquals("URL: \"" + uri + "\" is ", "valid", result);
	}

	public void testContextServiceJSONSchema() 
			throws Exception {
//		fail("Not yet implemented.");
	}

}
