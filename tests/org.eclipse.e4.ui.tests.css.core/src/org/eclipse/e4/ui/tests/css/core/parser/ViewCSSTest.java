/*******************************************************************************
 * Copyright (c) 2008, 2026 Angelo Zerr and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     IBM Corporation - ongoing development
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.core.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.eclipse.e4.ui.css.core.impl.engine.CSSEngineImpl;
import org.eclipse.e4.ui.css.swt.engine.CSSSWTEngineImpl;
import org.eclipse.e4.ui.tests.css.core.util.TestElement;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.css.CSSStyleDeclaration;

/**
 * Tests the computed-style cascade ({@code CSSEngine.computeStyle}).
 */
public class ViewCSSTest {

	private Display display;
	private CSSSWTEngineImpl engine;

	@BeforeEach
	public void setUp() {
		display = Display.getDefault();
		engine = new CSSSWTEngineImpl(display);
	}

	@Test
	void testGetComputedStyle() throws Exception {
		// Two rules with the same specificity, the second rule should take
		// precedence because of its position in the stylesheet
		String css = """
			Label { color: black; }\
			Button { color: blue; font-weight: bold; }
			Button { color: green; }
			""";
		parseStyleSheet(css);

		TestElement shell = new TestElement("Shell", engine);
		CSSStyleDeclaration shellStyle = engine.computeStyle(shell, null);
		assertNull(shellStyle);

		TestElement label = new TestElement("Label", engine);
		CSSStyleDeclaration labelStyle = engine.computeStyle(label, null);
		assertNotNull(labelStyle);
		assertEquals(1, labelStyle.getLength());

		TestElement button = new TestElement("Button", engine);
		CSSStyleDeclaration buttonStyle = engine.computeStyle(button, null);
		assertNotNull(buttonStyle);
		assertEquals(2, buttonStyle.getLength());
	}

	@Test
	void testBug419482_order1() throws Exception {
		String css = "Shell > * > * { color: red; }\n" + "Button { color: blue; }\n";
		parseStyleSheet(css);

		final TestElement shell = new TestElement("Shell", engine);
		final TestElement composite = new TestElement("Composite", shell, engine);
		final TestElement button = new TestElement("Button", composite, engine);

		CSSStyleDeclaration buttonStyle = engine.computeStyle(button, null);
		assertNotNull(buttonStyle);
		assertEquals(1, buttonStyle.getLength());
		assertEquals("color: blue;", buttonStyle.getCssText());
	}

	@Test
	void testBug419482_order2() throws Exception {
		String css = "Button { color: blue; }\n" + "Shell > * > * { color: red; }\n";
		parseStyleSheet(css);

		final TestElement shell = new TestElement("Shell", engine);
		final TestElement composite = new TestElement("Composite", shell, engine);
		final TestElement button = new TestElement("Button", composite, engine);

		CSSStyleDeclaration buttonStyle = engine.computeStyle(button, null);
		assertNotNull(buttonStyle);
		assertEquals(1, buttonStyle.getLength());
		assertEquals("color: red;", buttonStyle.getCssText());
	}

	@Test
	void testBug419482_higherSpecificity() throws Exception {
		String css = "Shell > * > Button { color: blue; }\n" + "Shell > * > * { color: red; }\n";
		parseStyleSheet(css);

		final TestElement shell = new TestElement("Shell", engine);
		final TestElement composite = new TestElement("Composite", shell, engine);
		final TestElement button = new TestElement("Button", composite, engine);

		CSSStyleDeclaration buttonStyle = engine.computeStyle(button, null);
		assertNotNull(buttonStyle);
		assertEquals(1, buttonStyle.getLength());
		assertEquals("color: blue;", buttonStyle.getCssText());
	}

	@SuppressWarnings("unchecked")
	@Test
	void testRuleCaching() throws Exception {
		String css = "Shell > * > * { color: red; }\n" + "Button { color: blue; }\n";
		parseStyleSheet(css);

		Field combinedRulesField = CSSEngineImpl.class.getDeclaredField("combinedRules");
		combinedRulesField.setAccessible(true);
		// before the first computeStyle() call the cache is empty
		assertNull(combinedRulesField.get(engine));

		final TestElement shell = new TestElement("Shell", engine);
		final TestElement composite = new TestElement("Composite", shell, engine);
		final TestElement button = new TestElement("Button", composite, engine);
		CSSStyleDeclaration buttonStyle = engine.computeStyle(button, null);
		assertNotNull(buttonStyle);

		// now the cache is filled
		assertNotNull(combinedRulesField.get(engine));

		// deeper inspection: check what private method getCombinedRules returns
		Method getCombinedRulesMethod = CSSEngineImpl.class.getDeclaredMethod("getCombinedRules");
		getCombinedRulesMethod.setAccessible(true);
		List<Object> cssRules = (List<Object>) getCombinedRulesMethod.invoke(engine);

		// check caching: a 2nd call retrieves the cached list
		assertSame(cssRules, getCombinedRulesMethod.invoke(engine));

		// add a new stylesheet => flush cache
		css = "Shell > * > * { color: blue; }\n" + "Label { color: green; }\n";
		parseStyleSheet(css);
		assertNull(combinedRulesField.get(engine));

		List<Object> cssRules2 = (List<Object>) getCombinedRulesMethod.invoke(engine);
		assertNotSame(cssRules, cssRules2);
		// stylesheet added => more rules
		assertTrue(cssRules2.size() > cssRules.size());
	}

	private void parseStyleSheet(String css) throws IOException {
		engine.parseStyleSheet(new StringReader(css));
	}
}
