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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.ui.externaltools.internal.ant.editor.AntEditorSaxDefaultHandler;
import org.eclipse.ui.externaltools.internal.ant.editor.support.TestTextCompletionProcessor;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * Tests everything about code completion and code assistance.
 * 
 * @version 29.11.2002
 * @author Alf Schiefelbein
 */
public class CodeCompletionTest extends TestCase {

    /**
     * Constructor for CodeCompletionTest.
     * @param arg0
     */
    public CodeCompletionTest(String arg0) {
        super(arg0);
    }

    
    /**
     * Tests the code completion for attributes of tasks.
     */
    public void testAttributeProposals() throws IOException {
        TestTextCompletionProcessor tempProcessor = new TestTextCompletionProcessor();

        ICompletionProposal[] tempProposals = tempProcessor.getAttributeProposals("contains", "ca");
        assertEquals(1, tempProposals.length);
        assertEquals("casesensitive - (true | false | on | off | yes | no)", tempProposals[0].getDisplayString());

        tempProposals = tempProcessor.getAttributeProposals("move", "");
        assertEquals(14, tempProposals.length);
        ICompletionProposal tempProposal = tempProposals[0];
        String tempString = tempProposal.getDisplayString();
        assertTrue(tempString.equals("id") 
        || tempString.equals("flatten - (true | false | on | off | yes | no)")
        || tempString.equals("encoding")
        || tempString.equals("tofile")
        || tempString.equals("todir")
        || tempString.equals("file")
        || tempString.equals("verbose - (true | false | on | off | yes | no)")
        || tempString.equals("includeemptydirs")
        || tempString.equals("overwrite - (true | false | on | off | yes | no)")
        || tempString.equals("taskname")
        || tempString.equals("failonerror - (true | false | on | off | yes | no)")
        || tempString.equals("description")
        || tempString.equals("preservelastmodified - (true | false | on | off | yes | no)")
        || tempString.equals("filtering - (true | false | on | off | yes | no)"));

        tempProposals = tempProcessor.getAttributeProposals("move", "to");
        assertEquals(2, tempProposals.length);

        tempProposals = tempProcessor.getAttributeProposals("reference", "idl");
        assertEquals(0, tempProposals.length);

        tempProposals = tempProcessor.getAttributeProposals("reference", "id");
        assertEquals(1, tempProposals.length);
        assertEquals("id", tempProposals[0].getDisplayString());

        tempProposals = tempProcessor.getAttributeProposals("reference", "i");
        assertEquals(1, tempProposals.length);
        assertEquals("id", tempProposals[0].getDisplayString());

        tempProposals = tempProcessor.getAttributeProposals("project", "de");
        assertEquals(1, tempProposals.length);
        
        // assertEquals("default - #REQUIRED", tempProposals[0].getDisplayString());

    }
    

    /**
     * Test the code completion for properties.
     */
    public void testPropertyProposals1() {
		TestTextCompletionProcessor tempProcessor = new TestTextCompletionProcessor();

        String tempDocumentText = "<project default=\"test\"><property name=\"prop1\" value=\"val1\" />\n";
        tempDocumentText += "<property name=\"prop2\" value=\"val2\" />\n";
        tempDocumentText += "<property name=\"alf\" value=\"horst\" />\n";
        tempDocumentText += "<test name=\"$";
		tempProcessor.setLineNumber(4);
		tempProcessor.setColumnNumber(14);
        int tempCursorPos = tempDocumentText.length();
        ICompletionProposal[] tempProposals = tempProcessor.getPropertyProposals(tempDocumentText, "", tempCursorPos);
        assertTrue(tempProposals.length >= 3);
        assertContains("alf", tempProposals);
        tempDocumentText = "<project default=\"test\"><property name=\"prop1\" value=\"val1\" />\n";
        tempDocumentText += "<property name=\"prop2\" value=\"val2\" />\n";
        tempDocumentText += "<property name=\"alf\" value=\"horst\" />\n";
        tempDocumentText += "<test name=\"$a}";
		tempProcessor.setLineNumber(4);
		tempProcessor.setColumnNumber(15);
        tempCursorPos = tempDocumentText.length()-1;
        tempProposals = tempProcessor.getPropertyProposals(tempDocumentText, "a", tempCursorPos);
        assertTrue(tempProposals.length >= 1);
        assertContains("alf", tempProposals);

    }
 
 	/**
 	 * Asserts that <code>aProposalDisplayString</code> is in one of the 
 	 * completion proposals.
 	 */
    private void assertContains(String aProposalDisplayString, ICompletionProposal[] aProposalArray) {
        boolean found = false;
        for (int i = 0; i < aProposalArray.length; i++) {
            ICompletionProposal tempProposal = aProposalArray[i];
            String tempDisplayString = tempProposal.getDisplayString();
            if(aProposalDisplayString.equals(tempDisplayString)) {
                found = true;
                break;
            }
        }
        assertEquals(true, found);
    }        
    
	/**
	 * Tests the property proposals in for the case that they are defined in
	 * a seperate property file.
	 */
    public void testBuildWithProperties() {
        Project tempProject = new Project();
        tempProject.init();
        URL tempURL = getClass().getResource("buildtest1.xml");
        assertNotNull(tempURL);
        File tempFile = new File(tempURL.getPath());
        assertTrue(tempFile.exists());
        
        tempProject.setUserProperty("ant.file", tempFile.getAbsolutePath());

        ProjectHelper.configureProject(tempProject, tempFile);  // File will be parsed here
        Hashtable tempTable = tempProject.getProperties();
        assertEquals("valD", tempTable.get("propD"));
        assertEquals("val1", tempTable.get("prop1"));
        assertEquals("val2", tempTable.get("prop2"));
        assertEquals("valV", tempTable.get("propV"));
        // assertEquals("val", tempTable.get("property_in_target"));  // (T) is known and should be fixed

        // test with not valid (complete) build file
        tempProject = new Project();
        tempProject.init();
        tempURL = getClass().getResource("buildtest2.xml");
        assertNotNull(tempURL);
        tempFile = new File(tempURL.getPath());
		assertTrue(tempFile.exists());
        tempProject.setUserProperty("ant.file", tempFile.getAbsolutePath());
        try {
            org.eclipse.ui.externaltools.internal.ant.editor.utils.ProjectHelper.configureProject(tempProject, tempFile);  // File will be parsed here
        }
        catch(BuildException e) {
            // ignore a build exception on purpose 
        }    
        tempTable = tempProject.getProperties();
        assertEquals("valD", tempTable.get("propD"));
        assertEquals("val1", tempTable.get("prop1"));
        assertEquals("val2", tempTable.get("prop2"));

        // test with not valid whole document string
        tempProject = new Project();
        tempProject.init();
        tempURL = getClass().getResource("buildtest2.xml");
        assertNotNull(tempURL);
        String tempPath = tempURL.getPath();
        tempPath = tempPath.substring(0, tempPath.lastIndexOf('/')+1) + "someNonExisting.xml";
        tempFile = new File(tempPath);
        tempProject.setUserProperty("ant.file", tempFile.getAbsolutePath());
        StringBuffer tempStrBuf = new StringBuffer();
        tempStrBuf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        tempStrBuf.append("<project name=\"testproject\" basedir=\".\" default=\"main\">");
        tempStrBuf.append("<property name=\"propA\" value=\"valA\" />\n");
		tempStrBuf.append("<property file=\"buildtest1.properties\" />\n");
        tempStrBuf.append("<target name=\"main\" depends=\"properties\">\n");
        try {
            org.eclipse.ui.externaltools.internal.ant.editor.utils.ProjectHelper.configureProject(tempProject, tempFile, tempStrBuf.toString());  // File will be parsed here
        }
        catch (BuildException e) {
            //ignore a build exception on purpose
			//as the document does not start and end within the same entity
        }    
        tempTable = tempProject.getProperties();
        assertEquals("valA", tempTable.get("propA"));
        assertEquals("val2", tempTable.get("prop2"));
    }


	/**
	 * Tests the property proposals for the case that they are defined in
	 * a dependent targets.
	 */
    public void testPropertyProposalDefinedInDependendTargets() throws FileNotFoundException {
        TestTextCompletionProcessor tempProcessor = new TestTextCompletionProcessor();

        URL tempURL = getClass().getResource("dependencytest.xml");
        assertNotNull(tempURL);
        File tempFile = new File(tempURL.getPath());
        assertTrue(tempFile.exists());
        tempProcessor.setEditedFile(tempFile);
		String tempDocumentText = getFileContentAsString(tempFile);

		tempProcessor.setLineNumber(35);
		tempProcessor.setColumnNumber(41);
		int tempCursorPosition = tempDocumentText.lastIndexOf("${");
		assertTrue(tempCursorPosition != -1);
        ICompletionProposal[] tempProposals = tempProcessor.getPropertyProposals(tempDocumentText, "", tempCursorPosition+2);
		assertContains("init_prop", tempProposals);
		assertContains("main_prop", tempProposals);
		assertContains("prop_prop", tempProposals);
		assertContains("do_not_compile", tempProposals);
		assertContains("adit_prop", tempProposals);
		assertContains("compile_prop", tempProposals);
    }

    
    /**
     * Tests the code completion for tasks having parent tasks.
     */
    public void testTaskProposals() throws IOException, ParserConfigurationException {
		TestTextCompletionProcessor tempProcessor = new TestTextCompletionProcessor();

        DocumentBuilder tempDocBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document tempDocument = tempDocBuilder.newDocument();

        ICompletionProposal[] tempProposals = tempProcessor.getTaskProposals("         <", tempDocument.createElement("rename"), "");
        assertEquals(0, tempProposals.length);

        tempProposals = tempProcessor.getTaskProposals("       <cl", createTestPropertyElement(tempDocument), "cl");
        assertEquals(1, tempProposals.length);
        ICompletionProposal tempProposal = tempProposals[0];
        assertEquals("classpath", tempProposal.getDisplayString());
        
        //case insensitivity
		tempProposals = tempProcessor.getTaskProposals("       <CL", createTestPropertyElement(tempDocument), "cl");
	   	assertEquals(1, tempProposals.length);
	   	tempProposal = tempProposals[0];
	   	assertEquals("classpath", tempProposal.getDisplayString());

        tempProposals = tempProcessor.getTaskProposals("       <cl", tempDocument.createElement("property"), "cl");
        assertEquals(1, tempProposals.length);
        tempProposal = tempProposals[0];
        assertEquals("classpath", tempProposal.getDisplayString());

        tempProposals = tempProcessor.getTaskProposals("       <pr", tempDocument.createElement("property"), "");
        assertEquals(1, tempProposals.length);
        tempProposal = tempProposals[0];
        assertEquals("classpath", tempProposal.getDisplayString());

        tempProposals = tempProcessor.getTaskProposals("       <pr", createTestProjectElement(tempDocument), "pr");
        assertEquals(1, tempProposals.length); // is choice and already used with classpath
        tempProposal = tempProposals[0];
        assertEquals("property", tempProposal.getDisplayString());
        
        tempProposals = tempProcessor.getTaskProposals("       <fi", createTestProjectElement(tempDocument), "fi");
        assertEquals(5, tempProposals.length); // is choice and already used with classpath
        
        tempProposals = tempProcessor.getTaskProposals("          ", tempDocument.createElement("project"), "");
        assertEquals(22, tempProposals.length);

        tempProposals = tempProcessor.getTaskProposals("          ", null, "");
        assertEquals(1, tempProposals.length);
        tempProposal = tempProposals[0];
        assertEquals("project", tempProposal.getDisplayString());

        tempProposals = tempProcessor.getTaskProposals("            jl", null, "jl");
        assertEquals(0, tempProposals.length);

        tempProposals = tempProcessor.getTaskProposals("             ", tempDocument.createElement("projexxx"), "");
        assertEquals(0, tempProposals.length);

        tempProposals = tempProcessor.getTaskProposals("              ", tempDocument.createElement("filelist"), "");
        assertEquals(0, tempProposals.length);

        // "<project><target><mk"
        String tempString = "<project><target><mk";
        tempProposals = tempProcessor.getTaskProposals("             <mk", tempProcessor.findParentElement(tempString, 0, 20), "mk");
        assertEquals(1, tempProposals.length);
        tempProposal = tempProposals[0];
        assertEquals("mkdir", tempProposal.getDisplayString());
        
        tempProposals = tempProcessor.getTaskProposals("", null, "");
        assertEquals(1, tempProposals.length);
        tempProposal = tempProposals[0];
        assertEquals("project", tempProposal.getDisplayString());

        tempProposals = tempProcessor.getTaskProposals("    \n<project></project>", null, "");
        assertEquals(1, tempProposals.length);

    }

    /**
     * Tests the algorithm for finding a child as used by the processor.
     */
    public void testFindChildElement() throws ParserConfigurationException {
        
        // Create the test data
        DocumentBuilder tempDocBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document tempDocument = tempDocBuilder.newDocument();
        Element tempParentElement = tempDocument.createElement("parent");
        Attr tempAttribute = tempDocument.createAttribute("att1");
        tempParentElement.setAttributeNode(tempAttribute);
        Comment tempComment = tempDocument.createComment("lakjjflsakdfj");
        tempParentElement.appendChild(tempComment);
        Element tempChildElement = tempDocument.createElement("child");
        tempParentElement.appendChild(tempChildElement);
        tempChildElement = tempDocument.createElement("secondchild");
        tempParentElement.appendChild(tempChildElement);
        
        // Create the processor
        TestTextCompletionProcessor tempProcessor = new TestTextCompletionProcessor();
        
        // Test it!
        tempChildElement = tempProcessor.findChildElementNamedOf(tempParentElement, "jkl");
        assertNull(tempChildElement);
        tempChildElement = tempProcessor.findChildElementNamedOf(tempParentElement, "secondchild");
        assertNotNull(tempChildElement);
        assertEquals("secondchild", tempChildElement.getTagName());
    }
    
    
    protected Element createTestProjectElement(Document aDocument) throws ParserConfigurationException {
        Element tempParentElement = aDocument.createElement("project");
        Element tempChildElement = aDocument.createElement("property");
        tempParentElement.appendChild(tempChildElement);
        Element tempClasspathElement = aDocument.createElement("classpath");
        tempChildElement.appendChild(tempClasspathElement);
        
        return tempParentElement;
    }

    protected Element createTestPropertyElement(Document aDocument) {
        Element tempChildElement = aDocument.createElement("property");
        Element tempClasspathElement = aDocument.createElement("classpath");
        tempChildElement.appendChild(tempClasspathElement);

        return tempChildElement;
    }
    
    protected Element createTestTargetElement(Document aDocument) {
        Element tempChildElement = aDocument.createElement("property");
        Element tempClasspathElement = aDocument.createElement("classpath");
        tempChildElement.appendChild(tempClasspathElement);

        return tempChildElement;
    }
    
    
    /**
     * Tests how the processor determines the proposal mode.
     */
    public void testDeterminingProposalMode() throws IOException {
        TestTextCompletionProcessor tempProcessor = new TestTextCompletionProcessor();

        // Modes:
        // 0 None
        // 1 Task Proposal
        // 2 Attribute Proposal
        // 3 Task Closing Proposal
        // 4 Attribute Value Proposal
        // 5 Property Proposal
        
        int tempMode = tempProcessor.determineProposalMode("<project><property ta", 21, "ta");
        assertEquals(2, tempMode);
        tempMode = tempProcessor.determineProposalMode("<project><property ", 19, "");
        assertEquals(2, tempMode);
        // (T) has to be implemented still
        tempMode = tempProcessor.determineProposalMode("<project><property   ", 21, "");
        assertEquals(2, tempMode);
        tempMode = tempProcessor.determineProposalMode("<project><prop", 14, "prop");
        assertEquals(1, tempMode);
        tempMode = tempProcessor.determineProposalMode("<project><prop bla", 18, "bla");
        assertEquals(0, tempMode); // task not known
        tempMode = tempProcessor.determineProposalMode("<project> hjk", 13, "");
        assertEquals(0, tempMode);
        tempMode = tempProcessor.determineProposalMode("<project> hjk<", 14, "");
        assertEquals(1, tempMode); // allow this case though it is not valid with Ant
        tempMode = tempProcessor.determineProposalMode("<project>", 9, "");
        assertEquals(1, tempMode);
        tempMode = tempProcessor.determineProposalMode("<project> ", 10, "");
        assertEquals(1, tempMode);
        tempMode = tempProcessor.determineProposalMode("<project></", 11, "");
        assertEquals(3, tempMode);
        tempMode = tempProcessor.determineProposalMode("<project>< </project>", 10, "");
        assertEquals(1, tempMode);
        tempMode = tempProcessor.determineProposalMode("<property id=\"hu\" ", 18, "");
        assertEquals(2, tempMode);
        tempMode = tempProcessor.determineProposalMode("<property id=\"hu\" \r\n ", 21, "");
        assertEquals(2, tempMode);
        tempMode = tempProcessor.determineProposalMode("<property\n", 10, "");
        assertEquals(2, tempMode);
        tempMode = tempProcessor.determineProposalMode("<property id=\"\" \r\n ", 14, "");
        assertEquals(4, tempMode);
        tempMode = tempProcessor.determineProposalMode("<target name=\"main\"><zip><size></size></zip></", 46, "");
        assertEquals(3, tempMode);
        tempMode = tempProcessor.determineProposalMode("<project><target name=\"$\"", 24, "");
        assertEquals(5, tempMode);
        tempMode = tempProcessor.determineProposalMode("<project><target name=\"${\"", 25, "");
        assertEquals(5, tempMode);
        tempMode = tempProcessor.determineProposalMode("<project><target name=\"${ja.bl\"", 30, "ja.bl");
        assertEquals(5, tempMode);
        tempMode = tempProcessor.determineProposalMode("", 0, "");
        assertEquals(1, tempMode);
    }


    /**
     * Tests how the prefix will be determined.
     */
    public void testDeterminingPrefix() {
        TestTextCompletionProcessor tempProcessor = new TestTextCompletionProcessor();

        // cursor after ${
        String tempPrefix = tempProcessor.getPrefixFromDocument("<project><target name=\"${}\"", 25);
        assertEquals("", tempPrefix);

        // cursor after $
        tempPrefix = tempProcessor.getPrefixFromDocument("<project><target name=\"${\"", 24);
        assertEquals("", tempPrefix);

        // cursor after ${ja.
        tempPrefix = tempProcessor.getPrefixFromDocument("<project><target name=\"${ja.\"", 28);
        assertEquals("ja.", tempPrefix);
        
        // cursor after <
		tempPrefix = tempProcessor.getPrefixFromDocument("<project><", 10);
		assertEquals("", tempPrefix);
		
		tempPrefix = tempProcessor.getPrefixFromDocument("<project name= \"test\"><tar", 26);
		assertEquals("tar", tempPrefix);
    }    


    /**
     * Tests parsing an XML file with the use of our AntEditorSaxDefaultHandler.
     */
    public void testXMLParsingWithAntEditorDefaultHandler() throws SAXException, ParserConfigurationException, IOException {
        SAXParser tempParser = SAXParserFactory.newInstance().newSAXParser();

		URL url= getClass().getResource("test1.xml");
		File file= new File(url.getFile());
        AntEditorSaxDefaultHandler tempHandler = new AntEditorSaxDefaultHandler(file.getParentFile(), 4, 8);
        InputStream tempStream = getClass().getResourceAsStream("test1.xml");
        try {
            tempParser.parse(tempStream, tempHandler);
        } catch(SAXParseException e) {
        }
        Element tempElement = tempHandler.getParentElement(true);
        assertNotNull(tempElement);
        assertEquals("klick", tempElement.getTagName());
        NodeList tempChildNodes = tempElement.getChildNodes();
        assertEquals(1, tempChildNodes.getLength());
        assertEquals("gurgel", ((Element)tempChildNodes.item(0)).getTagName());

		url= getClass().getResource("test2.xml");
		file= new File(url.getFile());
        tempHandler = new AntEditorSaxDefaultHandler(file.getParentFile(), 4, 8);
        tempStream = getClass().getResourceAsStream("test2.xml");
        tempParser.parse(tempStream, tempHandler);
        tempElement = tempHandler.getParentElement(false);
        assertNotNull(tempElement);
        assertEquals("klick", tempElement.getTagName());
        tempChildNodes = tempElement.getChildNodes();
        assertEquals(4, tempChildNodes.getLength());
        assertEquals("gurgel", ((Element)tempChildNodes.item(0)).getTagName());
        assertEquals("hal", ((Element)tempChildNodes.item(1)).getTagName());
        assertEquals("klack", ((Element)tempChildNodes.item(2)).getTagName());
        assertEquals("humpf", ((Element)tempChildNodes.item(3)).getTagName());

		url= getClass().getResource("test3.xml");
		file= new File(url.getFile());
        tempHandler = new AntEditorSaxDefaultHandler(file.getParentFile(), 3, 1);
        tempStream = getClass().getResourceAsStream("test3.xml");
        try {
            tempParser.parse(tempStream, tempHandler);
        } catch(SAXParseException e) {
        }
        tempElement = tempHandler.getParentElement(true);
        assertNotNull(tempElement);
        assertEquals("bla", tempElement.getTagName());

		url= getClass().getResource("test4.xml");
		file= new File(url.getFile());
        tempHandler = new AntEditorSaxDefaultHandler(file.getParentFile(), 0, 46);
        tempStream = getClass().getResourceAsStream("test4.xml");
        try {
            tempParser.parse(tempStream, tempHandler);
        } catch(SAXParseException e) {
        }
        tempElement = tempHandler.getParentElement(true);
        assertNotNull(tempElement);
        assertEquals("target", tempElement.getTagName());
    }
    
    
	/**
	 * Returns the content of the specified file as <code>String</code>.
	 */
	private String getFileContentAsString(File aFile) throws FileNotFoundException {
        InputStream tempStream = new FileInputStream(aFile);
        InputStreamReader tempReader = new InputStreamReader(tempStream);
        BufferedReader tempBufferedReader = new BufferedReader(tempReader);

        String tempResult = "";
        try {
            String tempLine;
            tempLine = tempBufferedReader.readLine();
        
            while(tempLine != null) {
                tempResult += "\n";
                tempResult += tempLine;
                tempLine = tempBufferedReader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
		return tempResult;
	}


    public static Test suite() {
        return new TestSuite(CodeCompletionTest.class);
    }
}
