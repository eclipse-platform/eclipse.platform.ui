/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.help.other;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.eclipse.help.internal.entityresolver.LocalEntityResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class XHTMLEntityTest extends TestCase {
	private final String XHTML1 = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
	"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
	"<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\n" +
	"<head></head><body>";
	private final String XHTML2 = "</body></html>";

	public void checkResolution(String text, int expected) throws Exception {
		DocumentBuilder documentBuilder;	
		documentBuilder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
		documentBuilder.setEntityResolver(new LocalEntityResolver());
		String xmlSource = XHTML1 + text + XHTML2;
		InputStream is = new ByteArrayInputStream(xmlSource.getBytes());
		Document doc = documentBuilder.parse(is);
		assertEquals(2, doc.getChildNodes().getLength());
		Node bodyNode = doc.getElementsByTagName("body").item(0);
		Node textNode = bodyNode.getChildNodes().item(0);
		assertNotNull(textNode);
		String value = textNode.getNodeValue();
		assertEquals(1, value.length());
		assertEquals(expected, value.charAt(0));
	}

	public void testResolveAmpersand() throws Exception {
		checkResolution("&amp;", 38);
	}

	public void testResolveNonBlockingSpace() throws Exception {
		checkResolution("&nbsp;", 160);
	}

	public void testResolveGt() throws Exception {
		checkResolution("&gt;", 62);
	}

	public void testResolveLeftArrow() throws Exception {
		checkResolution("&larr;", 8592);
	}

	public void testResolveaacute() throws Exception {
		checkResolution("&aacute;", 225);
	}

	public void testResolveCopy() throws Exception {
		checkResolution("&copy;", 169);
	}
	
	public void testResolveTilde() throws Exception {
		checkResolution("&tilde;", 732);
	}

	public void testResolveAacute() throws Exception {
		checkResolution("&Aacute;", 193);
	}
		
	public void testResolveAlpha() throws Exception {
		checkResolution("&Alpha;", 913);
	}

}
