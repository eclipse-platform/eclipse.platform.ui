/*******************************************************************************
 * Copyright (c) 2000, 2025 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Genady Beryozkin, me@genady.org - initial API and implementation
 *     Fabio Zadrozny <fabiofz at gmail dot com> - [typing] HippieCompleteAction is slow  ( Alt+/ ) - https://bugs.eclipse.org/bugs/show_bug.cgi?id=270385
 *******************************************************************************/
package org.eclipse.ui.workbench.texteditor.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.eclipse.core.runtime.AssertionFailedException;

import org.eclipse.text.tests.Accessor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import org.eclipse.ui.internal.texteditor.HippieCompletionEngine;

/**
 * Tests for the Hippie completion action of the text editor.
 *
 * @author Genady Beryozkin, me@genady.org
 */
public class HippieCompletionTest {

	IDocument[] documents;
	private HippieCompletionEngine fEngine;

	@BeforeEach
	public void setUp() throws Exception {
		documents= new IDocument[5];
		documents[0]= new Document("""
				package ui.TestPackage;

				/**
				 * This is a testing class that tests the hippie completion engine.
				 * it has a simple main with a print method
				 */
				public class TestClass1 {

				    public static void main(String[] args) {
				        System.out.println("I will be printing Hello world!");
				    }
				}""");
		documents[1]= new Document("This is a simple text file\n" +
				"with some testssome test that is also used in the completion engine tests");

		documents[2]= new Document("""
				<?xml version="1.0" encoding="UTF-8"?>
				<plugin
				   id="org.eclipse.ui.workbench.texteditor.tests"
				   name="%Plugin.name"
				   version="3.1.0"
				   provider-name="%Plugin.providerName">

				   <runtime>
				      <library name="workbenchtexteditortests.jar">
				         <export name="*"/>
				      </library>
				   </runtime>
				  \s
				   <requires>
				      <import plugin="org.eclipse.core.runtime.compatibility"/>
				      <import plugin="org.junit"/>
				      <import plugin="org.eclipse.text.tests"/>
				      <import plugin="org.eclipse.jface.text"/>
				      <import plugin="org.eclipse.ui.workbench.texteditor"/>
				      <import plugin="org.eclipse.ui"/>
				   </requires>
				  \s
				</plugin>
				""");

		documents[3]= new Document("""
				###############################################################################
				# Copyright (c) 2000, 2004 IBM Corporation and others.
				\\n\
				# This program and the accompanying materials\s
				# are made available under the terms of the Eclipse Public License 2.0
				# which accompanies this distribution, and is available at
				# https://www.eclipse.org/legal/epl-2.0/\
				#\s
				# SPDX-License-Identifier: EPL-2.0
				#\s
				# Contributors:
				#     IBM Corporation - initial API and implementation
				###############################################################################
				bin.includes= plugin.xml,\\
				               plugin.properties,\\
				               test.xml,\\
				               about.html,\\
				               *.jar

				src.includes= about.html
				              \s
				source.workbenchtexteditortests.jar= src/
				""");
		documents[4]= new Document("/**\n" +
				" * This class tests the hippie completion functionality.\n" +
				" * \u05D4\u05DE\u05D7\u05DC\u05E7\u05D4 \u05D4\u05D6\u05D5 \u05D1\u05D5\u05D3\u05E7\u05EA \u05D0\u05EA \u05DE\u05E0\u05D2\u05E0\u05D5\u05DF \u05D4\u05D4\u05E9\u05DC\u05DE\u05D5\u05EA\n" +
				" * This is an arabic word - \u0647\u0630\u0627 \u0643\u0644\u0645\u0629 \u0639\u0631\u0628\u064A\u0651\u0629\n" +
				" * Eclipse is the best IDE - Eclipse \u044D\u0442\u043E \u0441\u0430\u043C\u044B\u0439 \u043B\u0443\u0447\u0448\u0438\u0439 IDE.\n" +
				" */\n" +
				"\n" +
				"public class HippieTester2 {\n" +
				"\n" +
				"\tpublic static void main(String[] args) {\n" +
					"\t\tchar \u05DE\u05D7= '9';\n" +   // hebrew text
				"\t\tString $arabic\u20ACDigits= \"\u0661\u0662\u0663\u0664\u0665\u0666" + // Euro symbol in variable name, arabic digits from 1 to 6
									"\u2021\u0667\u0668\u0669\u0660\";\n" + // double dagger, arabic digits 7-0
				"\t\tString $arabic\u20AAWord= \"\u0628\u064E\u0627\u0628\u0650\";\n" + // shekel (israeli) currency symbol + arabic word
				"\t\tString \u0628\u0627\u0628= \"\u044D\u0442\";\n" + // arabic var, russian string
				"\t\tint \u20A31= 3;\n" + // frank currency symbol
				"\t\tint \u00A3\u0661\u0662\u0663= \u20A31 + \u05DE\u05D7;\n" + // pound, arabic digits 1-3, partial hebrew word
				"\t\tint a\u0300\u0301b= 18;\n" + // combining diactritical marks
				"\t\t}\n" +
				"\t\t\n" +
				"\tpublic void \u05D4\u05D4\u05E9(int \u0441\u0430\u043C) {\n" + // hebrew word prexif, russian word prefix
				"\t\tString \u043B\u0443\u0447\u0448= \"\u05D1\u05D5\u05D3\u05E7\";\n" +
				"\t\tchar \u20AA129;\n" + // shekel (israeli) currency
				"\t}\n" +
				"}");

		fEngine= new HippieCompletionEngine();
	}

	@Test
	public void testSearchBackwards1() throws BadLocationException {
		List<String> list= fEngine.getCompletionsBackwards(documents[0],
				"pri", documents[0].get().indexOf("println") + 10);
		assertEquals(2, list.size());
		assertEquals("ntln", list.get(0));
		assertEquals("nt", list.get(1));

		list= fEngine.getCompletionsBackwards(documents[0],
				"pri", documents[0].getLength());
		assertEquals(3, list.size());
		assertEquals("nting", list.get(0));
		assertEquals("ntln", list.get(1));
		assertEquals("nt", list.get(2));

		list= fEngine.getCompletionsBackwards(documents[0],
				"pri", documents[0].get().indexOf("println") + 1);
		assertEquals(1, list.size());
		assertEquals("nt", list.get(0));

		list= fEngine.getCompletionsBackwards(documents[0],
				"pa", 2);
		assertEquals(0, list.size());

	}

	@Test
	public void testSearchBackwards2() throws BadLocationException {
		List<String> list= fEngine.getCompletionsBackwards(documents[2],
				"plugi", documents[2].getLength());
		assertEquals(8, list.size());
		list= fEngine.makeUnique(list);
		assertEquals(1, list.size());
		assertEquals("n", list.get(0));

		list= fEngine.getCompletionsBackwards(documents[2],
				"plugin", documents[2].getLength());
		assertEquals(0, list.size()); // empty completions discarded

	}

	@Test
	public void testSearchBackwards3() throws BadLocationException {
		List<String> list= fEngine.getCompletionsBackwards(documents[1],
				"test", documents[1].getLength());
		assertEquals(2, list.size(), "Number of backwards suggestions does not match");
		list= fEngine.getCompletionsBackwards(documents[1],
				"tests", documents[1].getLength());
		assertEquals(1, list.size(), "Number of backwards suggestions does not match");

		list= fEngine.getCompletionsBackwards(documents[1],
				"test", documents[1].getLength() - 1);
		assertEquals(1, list.size(), "Number of backwards suggestions does not match");
	}

	@Test
	public void testSearch() throws BadLocationException {
		ArrayList<IDocument> docsList= new ArrayList<>(Arrays.asList(this.documents));
		List<String> result= createSuggestions("te", docsList);
		assertEquals(15, result.size(), "Number of completions does not match");
		result= fEngine.makeUnique(result);
		assertEquals(7, result.size(), "Number of completions does not match");

		result= createSuggestions("Plug", docsList);
		assertEquals(2, result.size(), "Number of completions does not match");

		result= createSuggestions("p", docsList);
		assertEquals(23, result.size(), "Number of completions does not match");
		result= fEngine.makeUnique(result);
		assertEquals(10, result.size(), "Number of completions does not match");
		assertEquals("ackage", result.get(0), "Incorrect completion");
		assertEquals("rint", result.get(1), "Incorrect completion");
		assertEquals("ublic", result.get(2), "Incorrect completion");
		assertEquals("rintln", result.get(3), "Incorrect completion");
		assertEquals("rinting", result.get(4), "Incorrect completion");
		assertEquals("lugin", result.get(5), "Incorrect completion");
		assertEquals("rovider", result.get(6), "Incorrect completion");
		assertEquals("roviderName", result.get(7), "Incorrect completion");
		assertEquals("rogram", result.get(8), "Incorrect completion");
		assertEquals("roperties", result.get(9), "Incorrect completion");
	}

	@Test
	public void testSearch2() throws BadLocationException {
		ArrayList<IDocument> docsList= new ArrayList<>(Arrays.asList(this.documents));
		List<String> result= createSuggestions("printe", docsList);
		assertEquals(0, result.size(), "Number of completions does not match");

		result= createSuggestions("s", docsList);
		assertEquals(8, result.size(), "Number of completions does not match");

		result= createSuggestions("pack", documents[0]);
		assertEquals(1, result.size(), "Number of completions does not match");
	}

	@Test
	public void testForwardSearch() throws BadLocationException {
		List<String> result= fEngine.getCompletionsForward(documents[0],
				"cl", documents[0].get().indexOf("cl"), true);
		assertEquals(2, result.size());

		result= fEngine.getCompletionsForward(documents[0],
				"cl", documents[0].get().indexOf("cl") + 1, true);
		assertEquals(1, result.size());

		result= fEngine.getCompletionsForward(documents[1],
				"Thi", 0, true);
		assertEquals(1, result.size());

		result= fEngine.getCompletionsForward(documents[1],
				"Thi", 1, true);
		assertEquals(0, result.size());
	}

	@Test
	public void testForwardSearchInternational() throws BadLocationException {
		List<String> result= fEngine.getCompletionsForward(documents[4],
				"$", documents[4].get().indexOf('$'), true);
		assertEquals(2, result.size());
		assertEquals("arabic\u20AAWord", result.get(0));
		assertEquals("arabic\u20ACDigits", result.get(1));

		result= fEngine.getCompletionsForward(documents[4],
				"$", documents[4].get().indexOf('$'), false);
		assertEquals(2, result.size());
		assertEquals("arabic\u20ACDigits", result.get(0));
		assertEquals("arabic\u20AAWord", result.get(1));

		result= fEngine.getCompletionsForward(documents[4],
				"$", documents[4].get().indexOf('$') + 1, true);
		assertEquals(1, result.size());
		assertEquals("arabic\u20AAWord", result.get(0));
	}

	@Test
	public void testPrefix() throws BadLocationException {
		String prefix= fEngine.getPrefixString(documents[0],
				documents[0].get().indexOf("testing") + 3);
		assertEquals("tes", prefix);

		prefix= fEngine.getPrefixString(documents[0],
				documents[0].get().indexOf("public") + 4);
		assertEquals("publ", prefix);

		prefix= fEngine.getPrefixString(documents[0],
				documents[0].get().indexOf("println") + 7);
		assertEquals("println", prefix);

		prefix= fEngine.getPrefixString(documents[0],
				documents[0].get().indexOf("println") + 8);
		assertEquals(null, prefix);

		prefix= fEngine.getPrefixString(documents[1], 3);
		assertEquals("Thi", prefix);

		prefix= fEngine.getPrefixString(documents[1], 0);
		assertEquals(null, prefix);

		prefix= fEngine.getPrefixString(documents[1], documents[1].getLength());
		assertEquals("tests", prefix);

		prefix= fEngine.getPrefixString(documents[3],
				documents[3].get().indexOf("Copyright") - 2);
		assertEquals(null, prefix);

		prefix= fEngine.getPrefixString(documents[4],
				documents[4].get().indexOf("IDE") + 2);
		assertEquals("ID", prefix);

		prefix= fEngine.getPrefixString(documents[4],
				documents[4].get().indexOf("$arabic\u20ACDigits") + 8);
		assertEquals("$arabic\u20AC", prefix);

		prefix= fEngine.getPrefixString(documents[4],
				documents[4].get().indexOf("$arabic\u20AAWord") + 8);
		assertEquals("$arabic\u20AA", prefix);

		prefix= fEngine.getPrefixString(documents[4],
				documents[4].get().indexOf("\u00A3\u0661\u0662\u0663") + 3);
		assertEquals("\u00A3\u0661\u0662", prefix);

		prefix= fEngine.getPrefixString(documents[4],
				documents[4].get().indexOf("a\u0300\u0301b") + 3);
		assertEquals("a\u0300\u0301", prefix);

		prefix= fEngine.getPrefixString(documents[4],
				documents[4].get().indexOf("\u0667\u0668\u0669\u0660") + 2);
		assertEquals("\u0667\u0668", prefix);
	}

	@Test
	public void testInternational() throws BadLocationException {
		IDocument intlDoc= documents[4];

		List<String> result= createSuggestions("\u05D4", intlDoc); // hebrew letter heh
		assertEquals(4, result.size(), "Number of completions does not match");
		assertEquals("\u05DE\u05D7\u05DC\u05E7\u05D4", result.get(0));
		assertEquals("\u05D6\u05D5", result.get(1));
		assertEquals("\u05D4\u05E9\u05DC\u05DE\u05D5\u05EA", result.get(2));
		assertEquals("\u05D4\u05E9", result.get(3));

		result= createSuggestions("\u0661", intlDoc); // arabic digit "1"
		assertEquals(1, result.size(), "Number of completions does not match");
		assertEquals("\u0662\u0663\u0664\u0665\u0666", result.get(0));

		result= createSuggestions("\u0628\u064E", intlDoc); // arabic letter bah and fatha
		assertEquals(1, result.size(), "Number of completions does not match");
		assertEquals("\u0627\u0628\u0650", result.get(0));
		result= createSuggestions("\u0628", intlDoc); // arabic letter bah
		assertEquals(2, result.size(), "Number of completions does not match");
		assertEquals("\u064E\u0627\u0628\u0650", result.get(0));
		assertEquals("\u0627\u0628", result.get(1));

		result= createSuggestions("$ara", intlDoc);
		assertEquals(2, result.size(), "Number of completions does not match");
		assertEquals("bic\u20ACDigits", result.get(0));
		assertEquals("bic\u20AAWord", result.get(1));

		result= createSuggestions("\u0441\u0430", intlDoc); // russian letters "s" and "a"
		assertEquals(2, result.size(), "Number of completions does not match");
		assertEquals("\u043C\u044B\u0439", result.get(0));
		assertEquals("\u043C", result.get(1));

		result= createSuggestions("\u05D1\u05D5", intlDoc); // hebrew letters bet and vav
		assertEquals(2, result.size(), "Number of completions does not match");
		assertEquals("\u05D3\u05E7\u05EA", result.get(0));
		assertEquals("\u05D3\u05E7", result.get(1));

		result= createSuggestions("a", intlDoc);
		assertEquals(4, result.size(), "Number of completions does not match");
		assertEquals("n", result.get(0));
		assertEquals("rabic", result.get(1));
		assertEquals("rgs", result.get(2));
		assertEquals("\u0300\u0301b", result.get(3));

		result= createSuggestions("\u20AA", intlDoc);  // israeli currency (shekel)
		assertEquals(1, result.size(), "Number of completions does not match");
		assertEquals("129", result.get(0));

		result= createSuggestions("\u20A3", intlDoc);  // french currency (frank)
		assertEquals(2, result.size(), "Number of completions does not match");
		assertEquals("1", result.get(0));
		assertEquals("1", result.get(1));

		result= createSuggestions("\u044D", intlDoc);  // russial letter "hard e"
		assertEquals(2, result.size(), "Number of completions does not match");
		assertEquals("\u0442\u043E", result.get(0));
		assertEquals("\u0442", result.get(1));

		result= createSuggestions("\u00A3", intlDoc);  // pound currency sign
		assertEquals(1, result.size(), "Number of completions does not match");
		assertEquals("\u0661\u0662\u0663", result.get(0));

		result= createSuggestions("\u00A5", intlDoc);  // yen currency sign
		assertEquals(0, result.size(), "Number of completions does not match");
	}

	@Test
	public void testInternationalBackwards() throws BadLocationException {
		IDocument intlDoc= documents[4];
		List<String> list= fEngine.getCompletionsBackwards(intlDoc,
				"\u043B\u0443", intlDoc.get().indexOf("129"));
		assertEquals(2, list.size());
		assertEquals("\u0447\u0448", list.get(0));
		assertEquals("\u0447\u0448\u0438\u0439", list.get(1));

		list= fEngine.getCompletionsBackwards(intlDoc,
				"\u05DE", intlDoc.get().lastIndexOf('+'));
		assertEquals(2, list.size());
		assertEquals("\u05D7", list.get(0));
		assertEquals("\u05E0\u05D2\u05E0\u05D5\u05DF", list.get(1));

		list= fEngine.getCompletionsBackwards(intlDoc,
				"\u0667", intlDoc.get().indexOf("\u2021\u0667") + 1);
		assertEquals(0, list.size());

		list= fEngine.getCompletionsBackwards(intlDoc,
				"\u0628", intlDoc.get().lastIndexOf("\u0628"));
		assertEquals(1, list.size());
		assertEquals("\u064E\u0627\u0628\u0650", list.get(0));

	}

	private Accessor createAccessor(Iterator<String> suggestions, int startOffset) {
		return new Accessor("org.eclipse.ui.texteditor.HippieCompleteAction$CompletionState",
				getClass().getClassLoader(), new Class[] { Iterator.class, int.class }, new
				Object[] { suggestions, Integer.valueOf(startOffset) });
	}

	private String next(Accessor state) {
		return (String) state.invoke("next");
	}


	/*
	 * Getting completions lazily
	 */
	@Test
	public void testCompletionState() throws Exception {
		ArrayList<String> list= new ArrayList<>();
		Accessor state= null;

		try {
			state= createAccessor(list.iterator(), 0);
			fail("Having no items is not valid (at least the empty completion must be there)");
		} catch (AssertionFailedException ex) {
		}

		list.add("");
		state= createAccessor(list.iterator(), 0);
		assertTrue(state.getBoolean("hasOnly1EmptySuggestion"));
		for (int i= 0; i < 3; i++) {
			assertEquals("", next(state));
		}

		list.add("");
		state= createAccessor(list.iterator(), 0);
		assertTrue(state.getBoolean("hasOnly1EmptySuggestion"));
		for (int i= 0; i < 3; i++) {
			assertEquals("", next(state));
		}


		//only empty and aaaa
		list.add(0, "aaaa");
		state= createAccessor(list.iterator(), 0);
		assertFalse(state.getBoolean("hasOnly1EmptySuggestion"));

		for (int i= 0; i < 3; i++) {
			assertEquals("aaaa", next(state));
			assertEquals("", next(state));
		}

		//empty, aaaa and bbbb
		list.add(1, "bbbb");
		state= createAccessor(list.iterator(), 0);
		assertFalse(state.getBoolean("hasOnly1EmptySuggestion"));

		for (int i= 0; i < 3; i++) {
			assertEquals("aaaa", next(state));
			assertEquals("bbbb", next(state));
			assertEquals("", next(state));
		}


		//empty, aaaa and 2 from 'bbbb' (should make unique)
		list.add(2, "bbbb");
		state= createAccessor(list.iterator(), 0);
		assertFalse(state.getBoolean("hasOnly1EmptySuggestion"));

		for (int i= 0; i < 3; i++) {
			assertEquals("aaaa", next(state));
			assertEquals("bbbb", next(state));
			assertEquals("", next(state));
		}
	}

	/*
	 * Getting completions lazily
	 */
	@Test
	public void testIteration() throws Exception {
		//Check only with current document
		IDocument openDocument= new Document ("" +
				"bar\n" +
				"bar1\n" +
				"bar2\n" +
				"");

		Iterator<String> suggestions= fEngine.getMultipleDocumentsIterator(openDocument, new ArrayList<>(), "bar", 3);
		assertEquals("1", suggestions.next());
		assertEquals("2", suggestions.next());
		assertEquals("", suggestions.next());
		assertFalse(suggestions.hasNext());


		//Check with 2 documents
		List<IDocument> otherDocuments= new ArrayList<>();
		otherDocuments.add(new Document ("" +
				"bar3\n" +
				"bar4\n" +
				""));

		suggestions= fEngine.getMultipleDocumentsIterator(openDocument, otherDocuments, "bar", 3);
		assertEquals("1", suggestions.next());
		assertEquals("2", suggestions.next());
		assertEquals("3", suggestions.next());
		assertEquals("4", suggestions.next());
		assertEquals("", suggestions.next());
		assertFalse(suggestions.hasNext());


		//Check with duplicates (duplicates are gotten at this level -- they're removed later -- at the CompletionState)
		suggestions= fEngine.getMultipleDocumentsIterator(openDocument, otherDocuments, "bar", 3);
		otherDocuments.add(new Document());
		otherDocuments.add(new Document("" +
				"bar3\n" +
				"bar4\n" +
				""));
		assertEquals("1", suggestions.next());
		assertEquals("2", suggestions.next());
		assertEquals("3", suggestions.next());
		assertEquals("4", suggestions.next());
		assertEquals("3", suggestions.next());
		assertEquals("4", suggestions.next());
		assertEquals("", suggestions.next());
		assertFalse(suggestions.hasNext());

		//Check with current document with only backward matches
		openDocument= new Document("" +
				"bar0 bar1 bar" +
				"");

		suggestions= fEngine.getMultipleDocumentsIterator(openDocument, new ArrayList<>(), "bar", openDocument.getLength());
		assertEquals("1", suggestions.next());
		assertEquals("0", suggestions.next());
		assertEquals("", suggestions.next());
		assertFalse(suggestions.hasNext());

	}

	private List<String> createSuggestions(String prefix, IDocument doc) throws BadLocationException {
		return createSuggestions(prefix, Arrays.asList(new IDocument[]{doc}));
	}

	private List<String> createSuggestions(String prefix, List<IDocument> docsList) throws BadLocationException {
		ArrayList<String> results= new ArrayList<>();
		for (IDocument doc : docsList) {
			results.addAll(fEngine.getCompletionsForward(doc, prefix, 0, false));
		}
		return results;
	}

}