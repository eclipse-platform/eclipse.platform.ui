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

package org.eclipse.ant.ui.internal.editor.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.xerces.parsers.SAXParser;
import org.eclipse.ant.tests.ui.testplugin.AbstractAntUITest;
import org.eclipse.ant.ui.internal.editor.EnclosingTargetSearchingHandler;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Tests the parsing using the EnclosingTargetSearchingHandler.
 * 
 */
public class EnclosingTargetSearchingHandlerTest extends AbstractAntUITest {

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
		return new TestSuite(EnclosingTargetSearchingHandlerTest.class);
    }
	    
    /**
     * Tests parsing an XML file with the use of our EnclosingTargetSearchingHandler.
     */
    public void testParsingOfBuildFileWithoutTargetElement() throws SAXException, IOException, ParserConfigurationException {
        SAXParser parser = getSAXParser();
		File file= getBuildFile("test1.xml");
        EnclosingTargetSearchingHandler handler = new EnclosingTargetSearchingHandler(file.getParentFile(), 4, 8);
        InputStream stream = getClass().getResourceAsStream("test1.xml");
		parse(stream, parser, handler, file);
		
        Element element = handler.getParentElement(false);
        assertNull(element);
        element = handler.getParentElement(true);
        assertNull(element);

		handler = new EnclosingTargetSearchingHandler(file.getParentFile(), 2, 0);
        stream = new FileInputStream(file);
		parse(stream, parser, handler, file);
        element = handler.getParentElement(false);
        assertNull(element);
        element = handler.getParentElement(true);
        assertNull(element);
        stream.close();

        handler = new EnclosingTargetSearchingHandler(file.getParentFile(), 0, 0);
        stream = new FileInputStream(file);
		parse(stream, parser, handler, file);
		stream.close();
		
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

		File file= getBuildFile("russianbuild.xml");
        EnclosingTargetSearchingHandler handler = new EnclosingTargetSearchingHandler(file.getParentFile(), 5, 5);
        InputStream stream = new FileInputStream(file);
		parse(stream, parser, handler, file);
        Element element = handler.getParentElement(false);
        assertNotNull(element);
		assertEquals("target", element.getTagName());
        element = handler.getParentElement(true);
        assertNotNull(element);
		assertEquals("target", element.getTagName());
		assertEquals("init", element.getAttribute("name"));
		stream.close();
		
        handler = new EnclosingTargetSearchingHandler(file.getParentFile(), 7, 0);
        stream = new FileInputStream(file);
		parse(stream, parser, handler, file);
        element = handler.getParentElement(false);
        assertNull(element);
        element = handler.getParentElement(true);
        assertNull(element);
        stream.close();

        handler = new EnclosingTargetSearchingHandler(file.getParentFile(), 0, 0);
        stream =  new FileInputStream(file);
		parse(stream, parser, handler, file);
        element = handler.getParentElement(false);
        assertNull(element);
        element = handler.getParentElement(true);
        assertNull(element);
        stream.close();
    }
    
    /**
     * Tests parsing an XML file with the use of our EnclosingTargetSearchingHandler.
     */
    public void testParsingOfEmptyBuildFile() throws SAXException, ParserConfigurationException, IOException {
		File file= getBuildFile("projectOnly.xml");
        EnclosingTargetSearchingHandler handler = new EnclosingTargetSearchingHandler(file.getParentFile(), 0, 0);
        InputStream stream= new FileInputStream(file);
        parse(stream, getSAXParser(), handler, file);
        Element element = handler.getParentElement(false);
        assertNull(element);
        element = handler.getParentElement(true);
        assertNull(element);
        stream.close();
    }
}