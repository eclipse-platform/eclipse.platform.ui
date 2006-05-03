/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.cheatsheet.composite;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;

import junit.framework.TestCase;

import org.eclipse.ui.internal.cheatsheets.CheatSheetPlugin;
import org.eclipse.ui.internal.cheatsheets.composite.parser.MarkupParser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class TestMarkupParser extends TestCase {
	
	private Document readString(String input) {
		StringReader reader = new StringReader(input); 
		InputSource source = new InputSource(reader);

	    DocumentBuilder documentBuilder = CheatSheetPlugin.getPlugin()
					.getDocumentBuilder();
		try {
			return documentBuilder.parse(source);
		} catch (SAXException e) {
			fail("SAX exception");
		} catch (IOException e) {
			fail("IOException");
		}
		return null;		
	}
	
	private String parse(String input) {
		return MarkupParser.parseAndTrimTextMarkup(readString(input).getFirstChild());
	}
	
	public void testParseEmptyString() {
		assertEquals("", parse("<root></root>"));
	}	

	public void testParseTags() {
		assertEquals("a<b>c</b>d<b>e</b>", parse("<root>a<b>c</b>d<b>e</b></root>"));
	}
	
	public void testParseNestedTags() {
		assertEquals("<p>a<b>c</b>d</p>", parse("<root><p>a<b>c</b>d</p></root>"));
	}

	public void testEscape() {
		assertEquals("a&lt;b&gt;c", parse("<root>a&lt;b&gt;c</root>"));
	}
	
	public void testEscapeAmpersand() {
		assertEquals("a&amp;c", parse("<root>a&amp;c</root>"));
	}
	
	public void testNoEscapeQuotes() {
		assertEquals("a'b'\"c\"", parse("<root>a'b'\"c\"</root>"));
	}

}
