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

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.ant.tests.ui.testplugin.AbstractAntUITest;
import org.eclipse.ant.ui.internal.editor.outline.AntModel;
import org.eclipse.ant.ui.internal.editor.xml.IAntEditorConstants;
import org.eclipse.ant.ui.internal.editor.xml.XmlElement;
import org.xml.sax.SAXException;

/**
 * Tests the correct creation of the outline for an xml file.
 * 
 */
public class AntEditorContentOutlineTests extends AbstractAntUITest {
    
    public AntEditorContentOutlineTests(String name) {
        super(name);
    }

    /**
     * Tests parsing an XML file with the use of our OutlinePreparingHandler.
     */
    public void testOutlinePreparingHandler() throws SAXException, ParserConfigurationException, IOException {
		AntModel model= getAntModel("test2.xml");
        
		XmlElement[] roots = model.getRootElements();
		assertNotNull(roots);
		XmlElement rootElement= roots[0];
   
		assertNotNull(rootElement);
        
        assertEquals("bla", rootElement.getName());
        assertEquals(1, rootElement.getStartingRow());
        assertEquals(1, rootElement.getStartingColumn());
        assertEquals(9, rootElement.getEndingRow());
        assertEquals(7, rootElement.getEndingColumn());
        List childNodes = rootElement.getChildNodes();
        assertEquals(2, childNodes.size());

        XmlElement childElement = (XmlElement)childNodes.get(0);
        assertEquals("blub", childElement.getName());
        assertEquals(2, childElement.getStartingRow());
        assertEquals(3, childElement.getStartingColumn());
        assertEquals(2, childElement.getEndingRow());
        assertEquals(16, childElement.getEndingColumn());

        childElement = (XmlElement)childNodes.get(1);
        assertEquals("klick", childElement.getName());
        assertEquals(3, childElement.getStartingRow());
        assertEquals(3, childElement.getStartingColumn());
        assertEquals(8, childElement.getEndingRow());
        assertEquals(11, childElement.getEndingColumn());

		childNodes = childElement.getChildNodes();
        assertEquals(4, childNodes.size());
		
		childElement = (XmlElement)childNodes.get(0);
        assertEquals("gurgel", childElement.getName());
        assertEquals(4, childElement.getStartingRow());
        assertEquals(5, childElement.getStartingColumn());
        assertEquals(4, childElement.getEndingRow());
        assertEquals(22, childElement.getEndingColumn());

		childElement = (XmlElement)childNodes.get(1);
        assertEquals("hal", childElement.getName());
        assertEquals(5, childElement.getStartingRow());
        assertEquals(5, childElement.getStartingColumn());
        assertEquals(5, childElement.getEndingRow());
        assertEquals(16, childElement.getEndingColumn());

		childElement = (XmlElement)childNodes.get(2);
        assertEquals("klack", childElement.getName());
        assertEquals(6, childElement.getStartingRow());
        assertEquals(5, childElement.getStartingColumn());
        assertEquals(6, childElement.getEndingRow());
        assertEquals(13, childElement.getEndingColumn());

		childElement = (XmlElement)childNodes.get(3);
        assertEquals("humpf", childElement.getName());
        assertEquals(7, childElement.getStartingRow());
        assertEquals(5, childElement.getStartingColumn());
        assertEquals(7, childElement.getEndingRow());
        assertEquals(13, childElement.getEndingColumn());
    }

    /**
     * Tests the creation of the XmlElement, that includes parsing a file
     * and determining the correct location of the tags.
     */
    public void testCreationOfOutlineTree() {
		AntModel model= getAntModel("buildtest1.xml");
        
		XmlElement[] roots = model.getRootElements();
		assertNotNull(roots);
		XmlElement rootProject= roots[0];
       
        assertNotNull(rootProject);
        
        // Get the content as string
        String wholeDocumentString = getCurrentDocument().get();

        
        // <project>
        assertEquals(2, rootProject.getStartingRow());
        assertEquals(1, rootProject.getStartingColumn());
        int offset = wholeDocumentString.indexOf("<project");
          
	    assertEquals(offset, rootProject.getOffset());
        
        List children = rootProject.getChildNodes();
		
		// <property name="propD">
		XmlElement element = (XmlElement)children.get(0);
        assertEquals(3, element.getStartingRow());
        assertEquals(2, element.getStartingColumn()); // with tab in file
        assertEquals(3, element.getEndingRow());
        assertEquals(40, element.getEndingColumn());  // with tab in file
        offset = wholeDocumentString.indexOf("<property");
       
        assertEquals(offset, element.getOffset());
// (T)
        int length = "<property name=\"propD\" value=\"valD\" />".length();
        assertEquals(length, element.getLength());

		
		// <property file="buildtest1.properties">
		element = (XmlElement)children.get(1);
        assertEquals(4, element.getStartingRow());
        assertEquals(5, element.getStartingColumn()); // no tab
        assertEquals(4, element.getEndingRow());
        assertEquals(46, element.getEndingColumn());
		
		// <property name="propV">
		element = (XmlElement)children.get(2);
        assertEquals(5, element.getStartingRow());
        assertEquals(5, element.getStartingColumn());
        assertEquals(5, element.getEndingRow());
        assertEquals(43, element.getEndingColumn());
		
		// <target name="main">
		element = (XmlElement)children.get(3);
        assertEquals(6, element.getStartingRow());
        assertEquals(5, element.getStartingColumn());
        assertEquals(8, element.getEndingRow());
        assertEquals(14, element.getEndingColumn());
		
		// <property name="property_in_target">
		element = (XmlElement)element.getChildNodes().get(0);
        assertEquals(7, element.getStartingRow());
        assertEquals(9, element.getStartingColumn());
        assertEquals(7, element.getEndingRow());
        assertEquals(58, element.getEndingColumn());
        offset = wholeDocumentString.indexOf("<property name=\"property_in_target\"");
          
        assertEquals(offset, element.getOffset());
		
        assertEquals(9, rootProject.getEndingRow());
        assertEquals(11, rootProject.getEndingColumn());
                
    }

    /**
     * Tests the creation of the XmlElement, that includes parsing a non-valid 
     * file.
     */
    public void testParsingOfNonValidFile() {
		AntModel model= getAntModel("buildtest2.xml");
        
		XmlElement[] roots = model.getRootElements();
		assertNotNull(roots);
		XmlElement root= roots[0];
   		assertNotNull(root);
   		
        // Get the content as string
        String wholeDocumentString = getCurrentDocument().get();

        List children = root.getChildNodes();

		// <target name="main">
		XmlElement element = (XmlElement)children.get(2);
        assertEquals(5, element.getStartingRow());
        assertEquals(2, element.getStartingColumn()); // with tab in file
        assertEquals(5, element.getEndingRow());
        assertEquals(43, element.getEndingColumn());
        int offset = wholeDocumentString.indexOf("<target name=\"main\"");
         
	    assertEquals(offset, element.getOffset()); // ???
    }

	
	/**
	 * Tests whether the outline can handle a build file with only the <project></project> tags.
	 */
	public void testWithProjectOnlyBuildFile() {
		AntModel model= getAntModel("projectOnly.xml");
		XmlElement[] roots = model.getRootElements();
		assertNotNull(roots);
		XmlElement root= roots[0];
		assertNotNull(root);
	}
	
	/**
	 * Tests whether the outline can handle an empty build file.
	 */
	public void testWithEmptyBuildFile() {
		AntModel model= getAntModel("empty.xml");
		XmlElement[] roots = model.getRootElements();
		assertNotNull(roots);
		assertTrue(roots.length == 0);
	}		

	
	/**
	 * Some testing of getting the right location of tags.
	 */
	public void testAdvancedTaskLocationing() {
		AntModel model= getAntModel("outline_select_test_build.xml");
        
        XmlElement[] roots = model.getRootElements();
        assertNotNull(roots);
        XmlElement rootProject= roots[0];
        // Get the content as string
        String wholeDocumentString = getCurrentDocument().get();
        
        // <project>
        assertNotNull(rootProject);
        assertEquals(2, rootProject.getStartingRow());
        assertEquals(1, rootProject.getStartingColumn());
        int offset = wholeDocumentString.indexOf("<project");
        
	    assertEquals(offset, rootProject.getOffset());
        
		// <target name="properties">
        XmlElement element = (XmlElement)rootProject.getChildNodes().get(1);
        assertNotNull(element);
        assertEquals("properties", element.getAttributeNamed(IAntEditorConstants.ATTR_NAME).getValue());
        assertEquals(16, element.getStartingRow());
        assertEquals(2, element.getStartingColumn());
        offset = wholeDocumentString.indexOf("<target name=\"properties\"");
      
	    assertEquals(offset, element.getOffset());
	}


    public static Test suite() {
		return new TestSuite(AntEditorContentOutlineTests.class);
    }
}
