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

package org.eclipse.ant.tests.ui.editor;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.ant.internal.ui.editor.outline.AntModel;
import org.eclipse.ant.internal.ui.editor.xml.IAntEditorConstants;
import org.eclipse.ant.internal.ui.editor.xml.XmlElement;
import org.eclipse.ant.tests.ui.testplugin.AbstractAntUITest;
import org.eclipse.jface.text.BadLocationException;

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
    public void testOutlinePreparingHandler() throws BadLocationException{
		AntModel model= getAntModel("test2.xml");
        
		XmlElement[] roots = model.getRootElements();
		assertNotNull(roots);
		XmlElement rootElement= roots[0];
   
		assertNotNull(rootElement);
        
        assertEquals("bla", rootElement.getName());
        assertEquals(1, getStartingRow(rootElement));
        assertEquals(1, getStartingColumn(rootElement));
        assertEquals(9, getEndingRow(rootElement));
        assertEquals(7, getEndingColumn(rootElement));
        List childNodes = rootElement.getChildNodes();
        assertEquals(2, childNodes.size());

        XmlElement childElement = (XmlElement)childNodes.get(0);
        assertEquals("blub", childElement.getName());
        assertEquals(2, getStartingRow(childElement));
        assertEquals(3, getStartingColumn(childElement));
        assertEquals(2, getEndingRow(childElement));
        assertEquals(9, getEndingColumn(childElement));

        childElement = (XmlElement)childNodes.get(1);
        assertEquals("klick", childElement.getName());
        assertEquals(3, getStartingRow(childElement));
        assertEquals(3, getStartingColumn(childElement));
        assertEquals(8, getEndingRow(childElement));
        assertEquals(11, getEndingColumn(childElement));

		childNodes = childElement.getChildNodes();
        assertEquals(4, childNodes.size());
		
		childElement = (XmlElement)childNodes.get(0);
        assertEquals("gurgel", childElement.getName());
        assertEquals(4, getStartingRow(childElement));
        assertEquals(5, getStartingColumn(childElement));
        assertEquals(4, getEndingRow(childElement));
        assertEquals(13, getEndingColumn(childElement));

		childElement = (XmlElement)childNodes.get(1);
        assertEquals("hal", childElement.getName());
        assertEquals(5, getStartingRow(childElement));
        assertEquals(5, getStartingColumn(childElement));
        assertEquals(5, getEndingRow(childElement));
        assertEquals(10, getEndingColumn(childElement));

		childElement = (XmlElement)childNodes.get(2);
        assertEquals("klack", childElement.getName());
        assertEquals(6, getStartingRow(childElement));
        assertEquals(5, getStartingColumn(childElement));
        assertEquals(6, getEndingRow(childElement));
        assertEquals(13, getEndingColumn(childElement));

		childElement = (XmlElement)childNodes.get(3);
        assertEquals("humpf", childElement.getName());
        assertEquals(7, getStartingRow(childElement));
        assertEquals(5, getStartingColumn(childElement));
        assertEquals(7, getEndingRow(childElement));
        assertEquals(13, getEndingColumn(childElement));
    }

    /**
     * Tests the creation of the XmlElement, that includes parsing a file
     * and determining the correct location of the tags.
     */
    public void testCreationOfOutlineTree() throws BadLocationException {
		AntModel model= getAntModel("buildtest1.xml");
        
		XmlElement[] roots = model.getRootElements();
		assertNotNull(roots);
		XmlElement rootProject= roots[0];
       
        assertNotNull(rootProject);
        
        // Get the content as string
        String wholeDocumentString = getCurrentDocument().get();
        
        // <project>
        assertEquals(2, getStartingRow(rootProject));
        assertEquals(1, getStartingColumn(rootProject));
        int offset = wholeDocumentString.indexOf("<project");
	    assertEquals(offset, rootProject.getOffset());
        
        List children = rootProject.getChildNodes();
		
		// <property name="propD">
		XmlElement element = (XmlElement)children.get(0);
		assertEquals(3, getStartingRow(element));
		assertEquals(2, getStartingColumn(element)); // with tab in file
		assertEquals(3, getEndingRow(element));
		assertEquals(40, getEndingColumn(element));  // with tab in file

        offset = wholeDocumentString.indexOf("<property");
        assertEquals(offset, element.getOffset());
        int length = "<property name=\"propD\" value=\"valD\" />".length();
        assertEquals(length, element.getLength());
		
		// <property file="buildtest1.properties">
		element = (XmlElement)children.get(1);
		assertEquals(4, getStartingRow(element));
		assertEquals(5, getStartingColumn(element)); // no tab
		assertEquals(4, getEndingRow(element));
		assertEquals(46, getEndingColumn(element));

		// <property name="propV">
		element = (XmlElement)children.get(2);
		assertEquals(5, getStartingRow(element));
		assertEquals(5, getStartingColumn(element));
		assertEquals(5, getEndingRow(element));
		assertEquals(43, getEndingColumn(element));
		
		// <target name="main">
		element = (XmlElement)children.get(3);
		assertEquals(6, getStartingRow(element));
		assertEquals(5, getStartingColumn(element));
		assertEquals(8, getEndingRow(element));
		assertEquals(14, getEndingColumn(element));
		
		// <property name="property_in_target">
		element = (XmlElement)element.getChildNodes().get(0);
		assertEquals(7, getStartingRow(element));
		assertEquals(9, getStartingColumn(element));
		assertEquals(7, getEndingRow(element));
		assertEquals(58, getEndingColumn(element));
        offset = wholeDocumentString.indexOf("<property name=\"property_in_target\"");
        assertEquals(offset, element.getOffset());
		
        assertEquals(9, getEndingRow(rootProject));
        assertEquals(11, getEndingColumn(rootProject));
    }
    
    private int getColumn(int offset, int line) throws BadLocationException {
    	return offset - getCurrentDocument().getLineOffset(line - 1) + 1;
    }
    
    private int getStartingRow(XmlElement element) throws BadLocationException {
    	return getCurrentDocument().getLineOfOffset(element.getOffset()) + 1;
    }
    
    private int getEndingRow(XmlElement element) throws BadLocationException {
    	return getCurrentDocument().getLineOfOffset(element.getOffset() + element.getLength()) + 1;
    }
    
    private int getStartingColumn(XmlElement element) throws BadLocationException {
    	return getColumn(element.getOffset(), getStartingRow(element));
    }
    
    private int getEndingColumn(XmlElement element) throws BadLocationException {
    	return getColumn(element.getOffset() + element.getLength(), getEndingRow(element));
    }

    /**
     * Tests the creation of the XmlElement, that includes parsing a non-valid 
     * file.
     */
    public void testParsingOfNonValidFile() throws BadLocationException {
		AntModel model= getAntModel("buildtest2.xml");
        
		XmlElement[] roots = model.getRootElements();
		assertNotNull(roots);
		XmlElement root= roots[0];
   		assertNotNull(root);

        List children = root.getChildNodes();

		// <target name="main">
		XmlElement element = (XmlElement)children.get(2);
		assertEquals(5, getStartingRow(element));
		assertEquals(2, getStartingColumn(element)); // with tab in file
		assertEquals(5, getEndingRow(element));
		//assertEquals(43, getEndingColumn(element));
        int offset = getCurrentDocument().get().indexOf("<target name=\"main\"");
        assertEquals(offset, element.getOffset()); 
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
	public void testAdvancedTaskLocation() throws BadLocationException {
		AntModel model= getAntModel("outline_select_test_build.xml");
        
        XmlElement[] roots = model.getRootElements();
        assertNotNull(roots);
        XmlElement rootProject= roots[0];
        // Get the content as string
        String wholeDocumentString = getCurrentDocument().get();
        
        // <project>
        assertNotNull(rootProject);
        assertEquals(2, getStartingRow(rootProject));
        assertEquals(1, getStartingColumn(rootProject));
        int offset = wholeDocumentString.indexOf("<project");
        
	    assertEquals(offset, rootProject.getOffset());
        
		// <target name="properties">
        XmlElement element = (XmlElement)rootProject.getChildNodes().get(1);
        assertNotNull(element);
        assertEquals("properties", element.getAttributeNamed(IAntEditorConstants.ATTR_NAME).getValue());
        assertEquals(16, getStartingRow(element));
        assertEquals(2, getStartingColumn(element));
        offset = wholeDocumentString.indexOf("<target name=\"properties\"");
      
	    assertEquals(offset, element.getOffset());
	}

    public static Test suite() {
		return new TestSuite(AntEditorContentOutlineTests.class);
    }
}