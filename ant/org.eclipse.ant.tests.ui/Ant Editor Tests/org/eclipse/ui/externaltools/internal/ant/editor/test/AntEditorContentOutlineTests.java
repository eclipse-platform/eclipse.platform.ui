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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ant.ui.internal.editor.outline.AntModel;
import org.eclipse.ant.ui.internal.editor.outline.XMLCore;
import org.eclipse.ui.externaltools.internal.ant.editor.support.TestLocationProvider;
import org.eclipse.ui.externaltools.internal.ant.editor.support.TestUtils;
import org.eclipse.ant.ui.internal.editor.xml.IAntEditorConstants;
import org.eclipse.ant.ui.internal.editor.xml.XmlElement;
import org.xml.sax.SAXException;

/**
 * Tests the correct creation of the outline for an xml file.
 * 
 * @author Alf Schiefelbein
 */
public class AntEditorContentOutlineTests extends TestCase {
    
    private IDocument currentDocument;
    
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
		XmlElement project= roots[0];
       
        assertNotNull(project);
        
        // Get the content as string
        String wholeDocumentString = currentDocument.get();

        
        // <project>
        assertEquals(2, project.getStartingRow());
        assertEquals(1, project.getStartingColumn());
        int tempOffset = wholeDocumentString.indexOf("<project");
          
	    assertEquals(tempOffset, project.getOffset());
        
        List children = project.getChildNodes();
		
		// <property name="propD">
		XmlElement tempEle = (XmlElement)children.get(0);
        assertEquals(3, tempEle.getStartingRow());
        assertEquals(2, tempEle.getStartingColumn()); // with tab in file
        assertEquals(3, tempEle.getEndingRow());
        assertEquals(40, tempEle.getEndingColumn());  // with tab in file
        tempOffset = wholeDocumentString.indexOf("<property");
       
        assertEquals(tempOffset, tempEle.getOffset());
// (T)
        int tempLength = "<property name=\"propD\" value=\"valD\" />".length();
        assertEquals(tempLength, tempEle.getLength());

		
		// <property file="buildtest1.properties">
		tempEle = (XmlElement)children.get(1);
        assertEquals(4, tempEle.getStartingRow());
        assertEquals(5, tempEle.getStartingColumn()); // no tab
        assertEquals(4, tempEle.getEndingRow());
        assertEquals(46, tempEle.getEndingColumn());
		
		// <property name="propV">
		tempEle = (XmlElement)children.get(2);
        assertEquals(5, tempEle.getStartingRow());
        assertEquals(5, tempEle.getStartingColumn());
        assertEquals(5, tempEle.getEndingRow());
        assertEquals(43, tempEle.getEndingColumn());
		
		// <target name="main">
		tempEle = (XmlElement)children.get(3);
        assertEquals(6, tempEle.getStartingRow());
        assertEquals(5, tempEle.getStartingColumn());
        assertEquals(8, tempEle.getEndingRow());
        assertEquals(14, tempEle.getEndingColumn());
		
		// <property name="property_in_target">
		tempEle = (XmlElement)tempEle.getChildNodes().get(0);
        assertEquals(7, tempEle.getStartingRow());
        assertEquals(9, tempEle.getStartingColumn());
        assertEquals(7, tempEle.getEndingRow());
        assertEquals(58, tempEle.getEndingColumn());
        tempOffset = wholeDocumentString.indexOf("<property name=\"property_in_target\"");
          
        assertEquals(tempOffset, tempEle.getOffset());
		
        assertEquals(9, project.getEndingRow());
        assertEquals(11, project.getEndingColumn());
                
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
        String wholeDocumentString = currentDocument.get();

        List children = root.getChildNodes();

		// <target name="main">
		XmlElement tempEle = (XmlElement)children.get(2);
        assertEquals(5, tempEle.getStartingRow());
        assertEquals(2, tempEle.getStartingColumn()); // with tab in file
        assertEquals(5, tempEle.getEndingRow());
        assertEquals(43, tempEle.getEndingColumn());
        int tempOffset = wholeDocumentString.indexOf("<target name=\"main\"");
         
	    assertEquals(tempOffset, tempEle.getOffset()); // ???
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
        XmlElement project= roots[0];
        // Get the content as string
        String wholeDocumentString = currentDocument.get();
        
        // <project>
        assertNotNull(project);
        assertEquals(2, project.getStartingRow());
        assertEquals(1, project.getStartingColumn());
        int tempOffset = wholeDocumentString.indexOf("<project");
        
	    assertEquals(tempOffset, project.getOffset());
        
		// <target name="properties">
        XmlElement tempEle = (XmlElement)project.getChildNodes().get(1);
        assertNotNull(tempEle);
        assertEquals("properties", tempEle.getAttributeNamed(IAntEditorConstants.ATTR_NAME).getValue());
        assertEquals(16, tempEle.getStartingRow());
        assertEquals(2, tempEle.getStartingColumn());
        tempOffset = wholeDocumentString.indexOf("<target name=\"properties\"");
      
	    assertEquals(tempOffset, tempEle.getOffset());
	}


    public static Test suite() {
        TestSuite suite = new TestSuite("AntEditorContentOutlineTest");
        suite.addTest(new AntEditorContentOutlineTests("testOutlinePreparingHandler"));
        suite.addTest(new AntEditorContentOutlineTests("testCreationOfOutlineTree"));
        suite.addTest(new AntEditorContentOutlineTests("testParsingOfNonValidFile"));
        suite.addTest(new AntEditorContentOutlineTests("testAdvancedTaskLocationing"));
        suite.addTest(new AntEditorContentOutlineTests("testWithEmptyBuildFile"));
        return suite;
    }
    
    private IDocument getDocument(String fileName) {
		InputStream in = getClass().getResourceAsStream(fileName);
		String initialContent= TestUtils.getStreamContentAsString(in);
		return new Document(initialContent);
    }
    
	private AntModel getAntModel(String fileName) {
		currentDocument= getDocument(fileName);
		AntModel model= new AntModel(XMLCore.getDefault(), currentDocument, null, new TestLocationProvider(fileName));
		model.reconcile();
		return model;
	}

}
