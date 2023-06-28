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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

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
		assertNotNull("Should have selected a word", selection);
		assertEquals("Unexpected selection", "world", document.get(selection.getOffset(), selection.getLength()));
		selection= doubleClickStrategy.findWord(document, document.getLength());
		assertNotNull("Should have selected a word", selection);
		assertEquals("Unexpected selection", "you", document.get(selection.getOffset(), selection.getLength()));
	}

	private static final class TestSpecificDefaultTextDoubleClickStrategy extends DefaultTextDoubleClickStrategy {

		@Override
		public IRegion findWord(IDocument document, int offset) { // make visible
			return super.findWord(document, offset);
		}
	}
}
