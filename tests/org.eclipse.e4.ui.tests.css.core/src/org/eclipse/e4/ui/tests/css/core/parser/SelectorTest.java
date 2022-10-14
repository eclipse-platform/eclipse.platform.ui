/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
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
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.tests.css.core.util.ParserTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.SelectorList;

public class SelectorTest {
	private CSSEngine engine;

	@BeforeEach
	public void setUp() throws Exception {
		engine = ParserTestUtil.createEngine();
	}

	@Test
	void testSimpleSelector() throws Exception {
		SelectorList list = engine.parseSelectors("Type1");
		assertNotNull(list);
		assertEquals(1, list.getLength());
		assertEquals("Type1", list.item(0).toString());
	}

	@Test
	void testMultipleSelectors() throws Exception {
		SelectorList list = engine.parseSelectors("Type1, Type2");
		assertNotNull(list);
		assertEquals(2, list.getLength());
		assertEquals("Type1", list.item(0).toString());
		assertEquals("Type2", list.item(1).toString());
	}

	@Test
	void testClassSelector() throws Exception {
		SelectorList list = engine.parseSelectors(".Class1");
		assertNotNull(list);
		assertEquals(1, list.getLength());
		assertEquals("*[class=\"Class1\"]", list.item(0).toString());
	}

	@Test
	void testAttributeSelector() throws Exception {
		SelectorList list = engine.parseSelectors("*[class='Class1']");
		assertNotNull(list);
		assertEquals(1, list.getLength());
		assertEquals("*[class=\"Class1\"]", list.item(0).toString());
	}

	@Test
	void testErrorAttributeSelector() throws IOException {
		try {
			engine.parseSelectors("*[class='Class1'"); // missing ']'
			fail("Parser should have errored on missing bracket");
		} catch (CSSParseException e) {
			// ignore
		}
	}
}
