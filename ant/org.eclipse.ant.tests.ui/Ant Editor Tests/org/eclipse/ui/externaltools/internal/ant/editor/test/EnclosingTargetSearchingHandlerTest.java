/*******************************************************************************
 * Copyright (c) 2002, 2003 GEBIT Gesellschaft fuer EDV-Beratung
 * und Informatik-Technologien mbH, 
 * Berlin, Duesseldorf, Frankfurt (Germany) and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     GEBIT Gesellschaft fuer EDV-Beratung und Informatik-Technologien mbH - initial implementation
 * 	   IBM Corporation - additional tests
 *******************************************************************************/

package org.eclipse.ui.externaltools.internal.ant.editor.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.xerces.parsers.SAXParser;
import org.eclipse.ant.ui.internal.editor.EnclosingTargetSearchingHandler;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Tests the parsing using the EnclosingTargetSearchingHandler.
 * 
 * @version 19.11.2002
 * @author Alf Schiefelbein
 */
public class EnclosingTargetSearchingHandlerTest extends TestCase {

    /**
     * Constructor for EnclosingTargetSearchingHandlerTest.
     * @param name
     */
    public EnclosingTargetSearchingHandlerTest(String name) {
        super(name);
    }


	/**
	 * The test suite.
	 */
    public static Test suite() {
        TestSuite suite = new TestSuite("EnclosingTargetSearchingHandlerTest");
        
        suite.addTest(new EnclosingTargetSearchingHandlerTest("testParsingOfBuildFileWithTargetElement"));
        suite.addTest(new EnclosingTargetSearchingHandlerTest("testParsingOfBuildFileWithoutTargetElement"));
        suite.addTest(new EnclosingTargetSearchingHandlerTest("testParsingOfEmptyBuildFile"));
        return suite;
    }
	
    
    /**
     * Tests parsing an XML file with the use of our EnclosingTargetSearchingHandler.
     */
    public void testParsingOfBuildFileWithoutTargetElement() throws SAXException, IOException, ParserConfigurationException {
        SAXParser parser = getSAXParser();
		URL url= getClass().getResource("test1.xml");
		File file= new File(url.getFile());
        EnclosingTargetSearchingHandler handler = new EnclosingTargetSearchingHandler(file.getParentFile(), 4, 8);
        InputStream stream = getClass().getResourceAsStream("test1.xml");
		parse(stream, parser, handler, file);
		
        Element element = handler.getParentElement(false);
        assertNull(element);
        element = handler.getParentElement(true);
        assertNull(element);

		handler = new EnclosingTargetSearchingHandler(file.getParentFile(), 2, 0);
        stream = getClass().getResourceAsStream("test1.xml");
		parse(stream, parser, handler, file);
        element = handler.getParentElement(false);
        assertNull(element);
        element = handler.getParentElement(true);
        assertNull(element);

        handler = new EnclosingTargetSearchingHandler(file.getParentFile(), 0, 0);
        stream = getClass().getResourceAsStream("test1.xml");
		parse(stream, parser, handler, file);
		
        element = handler.getParentElement(false);
        assertNull(element);
        element = handler.getParentElement(true);
        assertNull(element);
    }
    
    /**
     * Tests parsing an XML file with the use of our EnclosingTargetSearchingHandler.
     */
    public void testParsingOfBuildFileWithTargetElement() throws SAXException, ParserConfigurationException, IOException {
        SAXParser parser = getSAXParser();
		URL url= getClass().getResource("russianbuild.xml");
		File file= new File(url.getFile());
        EnclosingTargetSearchingHandler handler = new EnclosingTargetSearchingHandler(file.getParentFile(), 5, 5);
        InputStream stream = getClass().getResourceAsStream("russianbuild.xml");
		parse(stream, parser, handler, file);
        Element element = handler.getParentElement(false);
        assertNotNull(element);
		assertEquals("target", element.getTagName());
        element = handler.getParentElement(true);
        assertNotNull(element);
		assertEquals("target", element.getTagName());
		assertEquals("init", element.getAttribute("name"));

        handler = new EnclosingTargetSearchingHandler(file.getParentFile(), 7, 0);
        stream = getClass().getResourceAsStream("russianbuild.xml");
		parse(stream, parser, handler, file);
        element = handler.getParentElement(false);
        assertNull(element);
        element = handler.getParentElement(true);
        assertNull(element);

        handler = new EnclosingTargetSearchingHandler(file.getParentFile(), 0, 0);
        stream = getClass().getResourceAsStream("russianbuild.xml");
		parse(stream, parser, handler, file);
        element = handler.getParentElement(false);
        assertNull(element);
        element = handler.getParentElement(true);
        assertNull(element);
    }
    
    /**
     * Tests parsing an XML file with the use of our EnclosingTargetSearchingHandler.
     */
    public void testParsingOfEmptyBuildFile() throws SAXException, ParserConfigurationException, IOException {
		URL url= getClass().getResource("projectOnly.xml");
		File file= new File(url.getFile());
        EnclosingTargetSearchingHandler handler = new EnclosingTargetSearchingHandler(file.getParentFile(), 0, 0);
        InputStream stream = getClass().getResourceAsStream("projectOnly.xml");
        parse(stream, getSAXParser(), handler, file);
        Element element = handler.getParentElement(false);
        assertNull(element);
        element = handler.getParentElement(true);
        assertNull(element);
    }
    
	private SAXParser getSAXParser() throws SAXException {
		SAXParser parser = parser = new SAXParser();
		parser.setFeature("http://xml.org/sax/features/namespaces", false); //$NON-NLS-1$
		return parser;
	}
	
	private void parse(InputStream stream, SAXParser parser, EnclosingTargetSearchingHandler handler, File editedFile) {
		InputSource inputSource= new InputSource(stream);
		if (editedFile != null) {
			//needed for resolving relative external entities
			inputSource.setSystemId(editedFile.getAbsolutePath());
		}
	
		parser.setContentHandler(handler);
		parser.setDTDHandler(handler);
		parser.setEntityResolver(handler);
		parser.setErrorHandler(handler);
		try {
			parser.parse(inputSource);
		} catch (SAXException e) {
		} catch (IOException e) {
		}
	}
}