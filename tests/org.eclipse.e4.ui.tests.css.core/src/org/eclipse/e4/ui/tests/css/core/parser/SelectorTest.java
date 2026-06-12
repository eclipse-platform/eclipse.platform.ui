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
