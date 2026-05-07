/*******************************************************************************
 * Copyright (c) 2020, 2021 SAP SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     SAP SE - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import org.eclipse.jface.text.DefaultTextDoubleClickStrategy;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

public class DefaultTextDoubleClickStrategyTest {

	@Test
	public void testUnderscoreHandling() throws Exception {
		String content= "foo_bar foo__bar foo_1  foo1_bar foo_bar__baz___1 __aaaa a_aa___a   _asdf_  _____1";
		IDocument document= new Document(content);
		TestSpecificDefaultTextDoubleClickStrategy doubleClickStrategy= new TestSpecificDefaultTextDoubleClickStrategy();

		for (String word : content.split(" ")) {
			int offsetWordStart= content.indexOf(word);
			for (int offset= offsetWordStart; offset < offsetWordStart + word.length(); offset++) {
				IRegion selection= doubleClickStrategy.findWord(document, offset);
				String actualWord= document.get(selection.getOffset(), selection.getLength());

				assertEquals(word, actualWord);
			}
		}
	}

	@Test
	public void testClickAtLineEnd() throws Exception {
		String content= "Hello world\nhow are you";
		IDocument document= new Document(content);
		TestSpecificDefaultTextDoubleClickStrategy doubleClickStrategy= new TestSpecificDefaultTextDoubleClickStrategy();
		IRegion selection= doubleClickStrategy.findWord(document, 11);
		assertNotNull(selection, "Should have selected a word");
		assertEquals("world", document.get(selection.getOffset(), selection.getLength()), "Unexpected selection");
		selection= doubleClickStrategy.findWord(document, document.getLength());
		assertNotNull(selection, "Should have selected a word");
		assertEquals("you", document.get(selection.getOffset(), selection.getLength()), "Unexpected selection");
	}

	@Test
	public void testClickJustPastIdentifierSelectsThatIdentifier() throws Exception {
		String content= "foo bar baz";
		IDocument document= new Document(content);
		TestSpecificDefaultTextDoubleClickStrategy strategy= new TestSpecificDefaultTextDoubleClickStrategy();
		// Click at offset 3: the space right after "foo".
		IRegion selection= strategy.findWord(document, 3);
		assertNotNull(selection);
		assertEquals("foo", document.get(selection.getOffset(), selection.getLength()));
	}

	@Test
	public void testClickAtIdentifierStartSelectsWholeIdentifier() throws Exception {
		String content= "foo __aaaa bar";
		IDocument document= new Document(content);
		TestSpecificDefaultTextDoubleClickStrategy strategy= new TestSpecificDefaultTextDoubleClickStrategy();
		// Click at offset 4: the first '_' starting "__aaaa".
		IRegion selection= strategy.findWord(document, 4);
		assertNotNull(selection);
		assertEquals("__aaaa", document.get(selection.getOffset(), selection.getLength()));
	}

	@Test
	public void testIdentifierAtLineStartAndEnd() throws Exception {
		String content= "_foo___\nbar_baz";
		IDocument document= new Document(content);
		TestSpecificDefaultTextDoubleClickStrategy strategy= new TestSpecificDefaultTextDoubleClickStrategy();
		// First line: every offset 0..7 should yield "_foo___".
		for (int offset= 0; offset <= 7; offset++) {
			IRegion selection= strategy.findWord(document, offset);
			assertNotNull(selection, "no selection at offset " + offset);
			assertEquals("_foo___", document.get(selection.getOffset(), selection.getLength()),
					"unexpected selection at offset " + offset);
		}
		// Second line.
		IRegion selection= strategy.findWord(document, 11);
		assertNotNull(selection);
		assertEquals("bar_baz", document.get(selection.getOffset(), selection.getLength()));
	}

	@Test
	public void testSingleLineDocument() throws Exception {
		String content= "abc";
		IDocument document= new Document(content);
		TestSpecificDefaultTextDoubleClickStrategy strategy= new TestSpecificDefaultTextDoubleClickStrategy();
		IRegion selection= strategy.findWord(document, 0);
		assertNotNull(selection);
		assertEquals("abc", document.get(selection.getOffset(), selection.getLength()));
		selection= strategy.findWord(document, document.getLength());
		assertNotNull(selection);
		assertEquals("abc", document.get(selection.getOffset(), selection.getLength()));
	}

	@Test
	public void testIdentifierSurroundedByPunctuation() throws Exception {
		String content= "(foo_bar);";
		IDocument document= new Document(content);
		TestSpecificDefaultTextDoubleClickStrategy strategy= new TestSpecificDefaultTextDoubleClickStrategy();
		// Click in the middle of the identifier.
		IRegion selection= strategy.findWord(document, 4);
		assertNotNull(selection);
		assertEquals("foo_bar", document.get(selection.getOffset(), selection.getLength()));
	}

	@Test
	public void testCjkWordSelection() throws Exception {
		// Japanese text without spaces. The word break iterator segments it into a
		// Hiragana run ("こんにちは") followed by a Kanji run
		// ("世界"). This segmentation is locale-independent, so double-click
		// selects the script run the click lands in rather than the whole line.
		String content= "こんにちは世界";
		IDocument document= new Document(content);
		TestSpecificDefaultTextDoubleClickStrategy strategy= new TestSpecificDefaultTextDoubleClickStrategy();
		// Click inside the Hiragana run.
		IRegion selection= strategy.findWord(document, 2);
		assertNotNull(selection);
		assertEquals("こんにちは", document.get(selection.getOffset(), selection.getLength()));
		// Click inside the Kanji run.
		selection= strategy.findWord(document, 6);
		assertNotNull(selection);
		assertEquals("世界", document.get(selection.getOffset(), selection.getLength()));
	}

	@Test
	public void testCjkTokenBetweenSpaces() throws Exception {
		String content= "foo 我是 bar";
		IDocument document= new Document(content);
		TestSpecificDefaultTextDoubleClickStrategy strategy= new TestSpecificDefaultTextDoubleClickStrategy();
		// Click inside the CJK token.
		IRegion selection= strategy.findWord(document, 5);
		assertNotNull(selection);
		assertEquals("我是", document.get(selection.getOffset(), selection.getLength()));
	}

	@Test
	public void testThaiTokenBetweenSpaces() throws Exception {
		// Dictionary-based segmentation of a contiguous Thai run only happens under a
		// Thai locale, so this test delimits the token with spaces to stay
		// locale-independent: double-click selects the whole Thai token.
		String content= "foo ไทย bar";
		IDocument document= new Document(content);
		TestSpecificDefaultTextDoubleClickStrategy strategy= new TestSpecificDefaultTextDoubleClickStrategy();
		IRegion selection= strategy.findWord(document, 5);
		assertNotNull(selection);
		assertEquals("ไทย", document.get(selection.getOffset(), selection.getLength()));
	}

	private static final class TestSpecificDefaultTextDoubleClickStrategy extends DefaultTextDoubleClickStrategy {

		@Override
		public IRegion findWord(IDocument document, int offset) { // make visible
			return super.findWord(document, offset);
		}
	}
}
