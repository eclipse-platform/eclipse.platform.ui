/*******************************************************************************
 * Copyright (c) 2026 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;

import org.junit.jupiter.api.Test;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import org.eclipse.ltk.internal.core.refactoring.XmlProcessorFactoryLtk;

/**
 * The parsers must accept more attributes per element than the JAXP default.
 */
public class XmlProcessorFactoryLtkTest {

	private static final int ATTRIBUTES= 1000;

	private static String manyAttributes() {
		StringBuilder xml= new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?><session version=\"1.0\"><refactoring");
		for (int i= 1; i <= ATTRIBUTES; i++) {
			xml.append(" element").append(i).append("=\"/project/file").append(i).append("\"");
		}
		return xml.append("/></session>").toString();
	}

	@Test
	public void documentBuilderAcceptsManyAttributes() throws Exception {
		Document document= XmlProcessorFactoryLtk.createDocumentBuilderWithErrorOnDOCTYPE().parse(new InputSource(new StringReader(manyAttributes())));
		assertEquals(ATTRIBUTES, document.getDocumentElement().getFirstChild().getAttributes().getLength());
	}

	@Test
	public void documentBuilderFactoryAcceptsManyAttributes() throws Exception {
		Document document= XmlProcessorFactoryLtk.createDocumentBuilderFactoryWithErrorOnDOCTYPE().newDocumentBuilder().parse(new InputSource(new StringReader(manyAttributes())));
		assertEquals(ATTRIBUTES, document.getDocumentElement().getFirstChild().getAttributes().getLength());
	}

	@Test
	public void saxParserAcceptsManyAttributes() throws Exception {
		int[] count= new int[1];
		DefaultHandler handler= new DefaultHandler() {
			@Override
			public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes attributes) {
				if ("refactoring".equals(qName)) {
					count[0]= attributes.getLength();
				}
			}
		};
		XmlProcessorFactoryLtk.createSAXParserWithErrorOnDOCTYPE().parse(new InputSource(new StringReader(manyAttributes())), handler);
		assertEquals(ATTRIBUTES, count[0]);
		XmlProcessorFactoryLtk.withoutElementAttributeLimit(XmlProcessorFactoryLtk.createSAXFactoryWithErrorOnDOCTYPE().newSAXParser()).parse(new InputSource(new StringReader(manyAttributes())), handler);
		assertEquals(ATTRIBUTES, count[0]);
	}
}
