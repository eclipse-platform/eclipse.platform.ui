package org.eclipse.e4.ui.tests.css.core.parser;

import java.io.IOException;

import junit.framework.TestCase;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.tests.css.core.util.ParserTestUtil;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.SelectorList;

public class SelectorTest extends TestCase {
	private CSSEngine engine;

	protected void setUp() throws Exception {
		engine = ParserTestUtil.createEngine();
	}

	public void testSimpleSelector() throws Exception {
		SelectorList list = engine.parseSelectors("Type1");
		assertNotNull(list);
		assertEquals(1, list.getLength());
		assertEquals("Type1", list.item(0).toString());
	}

	public void testMultipleSelectors() throws Exception {
		SelectorList list = engine.parseSelectors("Type1, Type2");
		assertNotNull(list);
		assertEquals(2, list.getLength());
		assertEquals("Type1", list.item(0).toString());
		assertEquals("Type2", list.item(1).toString());
	}

	public void testClassSelector() throws Exception {
		SelectorList list = engine.parseSelectors(".Class1");
		assertNotNull(list);
		assertEquals(1, list.getLength());
		assertEquals("*[class=\"Class1\"]", list.item(0).toString());
	}

	public void testAttributeSelector() throws Exception {
		SelectorList list = engine.parseSelectors("*[class='Class1']");
		assertNotNull(list);
		assertEquals(1, list.getLength());
		assertEquals("*[class=\"Class1\"]", list.item(0).toString());
	}

	public void testErrorAttributeSelector() throws IOException {
		try {
			engine.parseSelectors("*[class='Class1'"); // missing ']'
			fail("Parser should have errored on missing bracket");
		} catch (CSSParseException e) {
			// ignore
		}
	}
}
