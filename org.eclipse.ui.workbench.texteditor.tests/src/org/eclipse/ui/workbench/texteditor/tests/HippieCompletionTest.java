/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Genady Beryozkin, me@genady.org - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.workbench.texteditor.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import org.eclipse.ui.internal.texteditor.HippieCompletionEngine;

/**
 * Tests for the Hippie completion action of the text editor
 * 
 * @author Genady Beryozkin, me@genady.org
 */
public class HippieCompletionTest extends TestCase {
	
	IDocument[] documents;
	private HippieCompletionEngine fEngine;
	
	public HippieCompletionTest(String name) {
		super(name);
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		documents = new IDocument[4];
		documents[0] = new Document("package ui.TestPackage;\n" + 
				"\n" + 
				"/**\n" + 
				" * This is a testing class that tests the hippie completion engine.\n" +
				" * it has a simple main with a print method\n" + 
				" */\n" + 
				"public class TestClass1 {\n" + 
				"\n" + 
				"    public static void main(String[] args) {\n" + 
				"        System.out.println(\"I will be printing Hello world!\");\n" + 
				"    }\n" + 
				"}");
		documents[1] = new Document("This is a simple text file\n" + 
				"with some testssome test that is also used in the completion engine tests");
		
		documents[2] = new Document("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<plugin\n" + 
				"   id=\"org.eclipse.ui.workbench.texteditor.tests\"\n" + 
				"   name=\"%Plugin.name\"\n" + 
				"   version=\"3.1.0\"\n" + 
				"   provider-name=\"%Plugin.providerName\">\n" + 
				"\n" + 
				"   <runtime>\n" + 
				"      <library name=\"workbenchtexteditortests.jar\">\n" + 
				"         <export name=\"*\"/>\n" + 
				"      </library>\n" + 
				"   </runtime>\n" + 
				"   \n" + 
				"   <requires>\n" + 
				"      <import plugin=\"org.eclipse.core.runtime.compatibility\"/>\n" + 
				"      <import plugin=\"org.junit\"/>\n" + 
				"      <import plugin=\"org.eclipse.text.tests\"/>\n" + 
				"      <import plugin=\"org.eclipse.jface.text\"/>\n" + 
				"      <import plugin=\"org.eclipse.ui.workbench.texteditor\"/>\n" + 
				"      <import plugin=\"org.eclipse.ui\"/>\n" + 
				"   </requires>\n" + 
				"   \n" + 
				"</plugin>\n" + 
				"");
		
		documents[3] = new Document("###############################################################################\n" + 
				"# Copyright (c) 2000, 2004 IBM Corporation and others.\n" + 
				"# All rights reserved. This program and the accompanying materials \n" + 
				"# are made available under the terms of the Eclipse Public License v1.0\n" + 
				"# which accompanies this distribution, and is available at\n" + 
				"# http://www.eclipse.org/legal/epl-v10.html\n" + 
				"# \n" + 
				"# Contributors:\n" + 
				"#     IBM Corporation - initial API and implementation\n" + 
				"###############################################################################\n" + 
				"bin.includes = plugin.xml,\\\n" + 
				"               plugin.properties,\\\n" + 
				"               test.xml,\\\n" + 
				"               about.html,\\\n" + 
				"               *.jar\n" + 
				"\n" + 
				"src.includes = about.html\n" + 
				"               \n" + 
				"source.workbenchtexteditortests.jar = src/\n" + 
				"");
		
		fEngine= new HippieCompletionEngine();
	}
	
	public void testSearchBackwards1() {
		try {
			List list = fEngine.getCompletionsBackwards(documents[0], 
					"pri", documents[0].get().indexOf("println") + 10);
			assertEquals(list.size(), 2);
			assertEquals(list.get(0), "ntln");
			assertEquals(list.get(1), "nt");
			
			list = fEngine.getCompletionsBackwards(documents[0], 
					"pri", documents[0].getLength());
			assertEquals(list.size(), 3);
			assertEquals(list.get(0), "nting");
			assertEquals(list.get(1), "ntln");
			assertEquals(list.get(2), "nt");
			
			list = fEngine.getCompletionsBackwards(documents[0], 
					"pri", documents[0].get().indexOf("println") + 1);
			assertEquals(list.size(), 2);
			assertEquals(list.get(0), "ntln");
            assertEquals(list.get(1), "nt");
			
		} catch (BadLocationException e) {
			assertTrue("Got out of document bounds", false);
		}
	}
	
	public void testSearchBackwards2() {
		try {
			List list = fEngine.getCompletionsBackwards(documents[2], 
					"plugi", documents[2].getLength());
			assertEquals(8, list.size());
			list = fEngine.makeUnique(list);
			assertEquals(1, list.size());
			assertEquals("n", list.get(0));
			
			list = fEngine.getCompletionsBackwards(documents[2], 
					"plugin", documents[2].getLength());
			assertEquals(0, list.size()); // empty completions discarded
			
		} catch (BadLocationException e) {
			assertTrue("Got out of document bounds", false);
		}
	}
	
    public void testSearchBackwards3() {
        try {
            List list = fEngine.getCompletionsBackwards(documents[1], 
                    "test", documents[1].getLength());
            assertEquals("Number of backwards suggestions does not match", 2, list.size());
            list = fEngine.getCompletionsBackwards(documents[1], 
                    "tests", documents[1].getLength());
            assertEquals("Number of backwards suggestions does not match", 1, list.size());

            list = fEngine.getCompletionsBackwards(documents[1], 
                    "test", documents[1].getLength() - 1);
            assertEquals("Number of backwards suggestions does not match", 2, list.size());
        } catch (BadLocationException e) {
            assertTrue("Got out of document bounds", false);
        }
    }
	
	
	public void testSearch() {
		ArrayList docsList = new ArrayList(Arrays.asList(this.documents));
		List result = createSuggestions("te", docsList);
		assertEquals("Number of completions does not match", 14, result.size());
		result = fEngine.makeUnique(result);
		assertEquals("Number of completions does not match", 7, result.size());
		
		result = createSuggestions("Plug", docsList);
		assertEquals("Number of completions does not match", 2, result.size());
		
		result = createSuggestions("p", docsList);
		assertEquals("Number of completions does not match", 20, result.size());
		result = fEngine.makeUnique(result);
		assertEquals("Number of completions does not match", 10, result.size());
		assertEquals("Incorrect completion", "ackage", result.get(0));
		assertEquals("Incorrect completion", "rint", result.get(1));
		assertEquals("Incorrect completion", "ublic", result.get(2));
		assertEquals("Incorrect completion", "rintln", result.get(3));
		assertEquals("Incorrect completion", "rinting", result.get(4));
		assertEquals("Incorrect completion", "lugin", result.get(5));
		assertEquals("Incorrect completion", "rovider", result.get(6));
		assertEquals("Incorrect completion", "roviderName", result.get(7));
		assertEquals("Incorrect completion", "rogram", result.get(8));
		assertEquals("Incorrect completion", "roperties", result.get(9));
	}
	
	public void testSearch2() {
		ArrayList docsList = new ArrayList(Arrays.asList(this.documents));
		List result = createSuggestions("printe", docsList);
		assertEquals("Number of completions does not match", 0, result.size());
		
		result = createSuggestions("s", docsList);
		assertEquals("Number of completions does not match", 7, result.size());
	}
	
	public static Test suite() {
		return new TestSuite(HippieCompletionTest.class); 
	}
	
	private List createSuggestions(String prefix, ArrayList docsList) {
		ArrayList results = new ArrayList();
		for (Iterator i = docsList.iterator(); i.hasNext();) {
			IDocument doc = (IDocument) i.next();
			try {
				results.addAll(fEngine.getCompletions(doc, prefix));
			} catch (BadLocationException e) {
				assertTrue("No exception should be thrown here", false);
			}
		}
		return results;
	}
	
}
