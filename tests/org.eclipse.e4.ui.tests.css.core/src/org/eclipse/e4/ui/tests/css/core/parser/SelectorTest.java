/*******************************************************************************
 * Copyright (c) 2013, 2026 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 430468
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.core.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.impl.engine.selector.Selectors;
import org.eclipse.e4.ui.css.core.impl.parser.CssParseException;
import org.eclipse.e4.ui.tests.css.core.util.ParserTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SelectorTest {
	private CSSEngine engine;

	@BeforeEach
	public void setUp() throws Exception {
		engine = ParserTestUtil.createEngine();
	}

	@Test
	void testSimpleSelector() throws Exception {
		Selectors.SelectorList list = engine.parseSelectors("Type1");
		assertNotNull(list);
		assertEquals(1, list.getLength());
		assertEquals("Type1", list.item(0).text());
	}

	@Test
	void testNotSelector() throws Exception {
		Selectors.SelectorList list = engine.parseSelectors("Composite:not(.Toolbar)");
		assertEquals(1, list.getLength());
		assertEquals("Composite:not(.Toolbar)", list.item(0).text());
	}

	@Test
	void testNotSelectorAcceptsEverySimpleSelector() throws Exception {
		assertEquals("Button:not(*)", engine.parseSelectors("Button:not(*)").item(0).text());
		assertEquals("Button:not(Label)", engine.parseSelectors("Button:not(Label)").item(0).text());
		assertEquals("Button:not(.warning)", engine.parseSelectors("Button:not(.warning)").item(0).text());
		assertEquals("Button:not(#go)", engine.parseSelectors("Button:not(#go)").item(0).text());
		assertEquals("Button:not([role='editor'])", engine.parseSelectors("Button:not([role='editor'])").item(0).text());
	}

	@Test
	void testNotSelectorSpecificity() throws Exception {
		// CSS3: :not() adds nothing, its argument counts normally.
		assertEquals(engine.parseSelectors("Button.warning").item(0).specificity(),
				engine.parseSelectors("Button:not(.warning)").item(0).specificity());
		assertEquals(engine.parseSelectors("Button").item(0).specificity(),
				engine.parseSelectors("Button:not(*)").item(0).specificity());
	}

	@Test
	void testNotSelectorRejectsUnsupportedArguments() {
		// CSS3 allows a pseudo-class here; the engine does not, because
		// negating one would invert the static-pseudo-instance carve-out.
		assertThrows(CssParseException.class, () -> engine.parseSelectors("Button:not(:selected)"));
		assertThrows(CssParseException.class, () -> engine.parseSelectors("Button:not()"));
		assertThrows(CssParseException.class, () -> engine.parseSelectors("Button:not(.a"));
		// No combinators and no nesting inside :not().
		assertThrows(CssParseException.class, () -> engine.parseSelectors("Button:not(Composite Label)"));
		assertThrows(CssParseException.class, () -> engine.parseSelectors("Button:not(:not(.a))"));
	}

	@Test
	void testMultipleSelectors() throws Exception {
		Selectors.SelectorList list = engine.parseSelectors("Type1, Type2");
		assertNotNull(list);
		assertEquals(2, list.getLength());
		assertEquals("Type1", list.item(0).text());
		assertEquals("Type2", list.item(1).text());
	}

	@Test
	void testClassSelector() throws Exception {
		Selectors.SelectorList list = engine.parseSelectors(".Class1");
		assertNotNull(list);
		assertEquals(1, list.getLength());
		assertEquals(".Class1", list.item(0).text());
	}

	@Test
	void testAttributeSelector() throws Exception {
		Selectors.SelectorList list = engine.parseSelectors("*[class='Class1']");
		assertNotNull(list);
		assertEquals(1, list.getLength());
		// The Universal selector ('*') is folded away since the AttributeSelector
		// alone carries the full match condition.
		assertEquals("[class='Class1']", list.item(0).text());
	}

	@Test
	void testErrorAttributeSelector() {
		assertThrows(CssParseException.class, () -> engine.parseSelectors("*[class='Class1'")); // missing ']'
	}
}
