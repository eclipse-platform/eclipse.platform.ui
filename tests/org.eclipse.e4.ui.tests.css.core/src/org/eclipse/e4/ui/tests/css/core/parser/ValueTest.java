/*******************************************************************************
 * Copyright (c) 2009, 2014 IBM Corporation and others.
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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.impl.dom.Measure;
import org.eclipse.e4.ui.tests.css.core.util.ParserTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

public class ValueTest {

	private CSSEngine engine;

	@BeforeEach
	public void setUp() {
		engine = ParserTestUtil.createEngine();
	}

	@Test
	void testFloat() throws Exception {
		CSSValue value = engine.parsePropertyValue("2.0");
		assertTrue(value instanceof Measure);
		assertEquals( "2.0", value.getCssText() );
	}

	@Test
	void testInt() throws Exception {
		CSSValue value = engine.parsePropertyValue("34");
		assertTrue(value instanceof Measure);
		assertEquals(CSSPrimitiveValue.CSS_NUMBER, ((Measure) value).getPrimitiveType());
		assertEquals( "34", value.getCssText() );
	}

	@Test
	void testIdentifier() throws Exception {
		CSSValue value = engine.parsePropertyValue("SomeWord");
		assertTrue(value instanceof Measure);
		assertEquals(CSSPrimitiveValue.CSS_IDENT, ((Measure) value).getPrimitiveType());
		assertEquals( "SomeWord", value.getCssText() );
	}

	@Test
	void testPercent() throws Exception {
		CSSValue value = engine.parsePropertyValue("30%");
		assertTrue(value instanceof Measure);
		assertEquals(CSSPrimitiveValue.CSS_PERCENTAGE, ((Measure) value).getPrimitiveType());
		assertEquals( "30.0%", value.getCssText() );
	}

	@Test
	void testPixel() throws Exception {
		CSSValue value = engine.parsePropertyValue("26px");
		assertTrue(value instanceof Measure);
		assertEquals(CSSPrimitiveValue.CSS_PX, ((Measure) value).getPrimitiveType());
		assertEquals( "26.0px", value.getCssText() );
	}

	@Test
	void testInch() throws Exception {
		CSSValue value = engine.parsePropertyValue("88in");
		assertTrue(value instanceof Measure);
		assertEquals(CSSPrimitiveValue.CSS_IN, ((Measure) value).getPrimitiveType());
		assertEquals( "88.0in", value.getCssText() );
	}

	@Test
	void testEm() throws Exception {
		CSSValue value = engine.parsePropertyValue("75em");
		assertTrue(value instanceof Measure);
		assertEquals(CSSPrimitiveValue.CSS_EMS, ((Measure) value).getPrimitiveType());
		assertEquals( "75.0em", value.getCssText() );
	}

	@Test
	void testURI() throws Exception {
		CSSValue value = engine.parsePropertyValue("url(./somepath/picture.gif)");
		assertTrue(value instanceof Measure);
		assertEquals(CSSPrimitiveValue.CSS_URI, ((Measure) value).getPrimitiveType());
		assertEquals("url(./somepath/picture.gif)", value.getCssText());
	}

	@Test
	void testList() throws Exception {
		CSSValue value = engine.parsePropertyValue("34 34 34");
		assertTrue(value instanceof CSSValueList);
		assertEquals(CSSValue.CSS_VALUE_LIST, ((CSSValueList) value).getCssValueType());
		assertEquals(3, ((CSSValueList) value).getLength());
		assertTrue(((CSSValueList) value).item(0) instanceof Measure);
		assertEquals(CSSPrimitiveValue.CSS_NUMBER,
				((Measure) ((CSSValueList) value).item(0)).getPrimitiveType());
		assertTrue(((CSSValueList) value).item(1) instanceof Measure);
		assertEquals(CSSPrimitiveValue.CSS_NUMBER,
				((Measure) ((CSSValueList) value).item(1)).getPrimitiveType());
		assertTrue(((CSSValueList) value).item(2) instanceof Measure);
		assertEquals(CSSPrimitiveValue.CSS_NUMBER,
				((Measure) ((CSSValueList) value).item(2)).getPrimitiveType());
		assertEquals("34 34 34", value.getCssText());
	}

	@Test
	void testCommaSeparatedList() throws Exception {
		CSSValue value = engine.parsePropertyValue("34, 34, 34");
		assertTrue(value instanceof CSSValueList);
		CSSValueList list = (CSSValueList) value;
		assertEquals(CSSValue.CSS_VALUE_LIST, list.getCssValueType());
		assertEquals(5, list.getLength());
		// FIXME: see comments in bug 278139
		for (int i = 0; i < list.getLength(); i++) {
			assertTrue(list.item(i) instanceof Measure);
		}
		assertEquals(CSSPrimitiveValue.CSS_NUMBER,
				((Measure) list.item(0)).getPrimitiveType());
		assertEquals(CSSPrimitiveValue.CSS_CUSTOM,
				((Measure) list.item(1)).getPrimitiveType());
		assertEquals(CSSPrimitiveValue.CSS_NUMBER,
				((Measure) list.item(2)).getPrimitiveType());
		assertEquals(CSSPrimitiveValue.CSS_CUSTOM,
				((Measure) list.item(3)).getPrimitiveType());
		assertEquals(CSSPrimitiveValue.CSS_NUMBER,
				((Measure) list.item(4)).getPrimitiveType());
		// use String#matches() as there may be white-space differences
		assertTrue(value.getCssText().matches("34\\s*,\\s*34\\s*,\\s*34"));
	}

}
