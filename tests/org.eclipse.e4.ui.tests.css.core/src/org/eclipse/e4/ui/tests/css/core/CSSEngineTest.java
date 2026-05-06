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
 *     Alain Le Guennec <Alain.LeGuennec@esterel-technologies.com> - Bug 458334
 *     Lars Sadau <lars@sadau-online.de> - Bug 487994
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.impl.engine.CSSEngineImpl;
import org.eclipse.e4.ui.tests.css.core.util.TestElement;
import org.junit.jupiter.api.Test;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SelectorList;
import org.w3c.dom.Element;

class CSSEngineTest {

	private static class TestCSSEngine extends CSSEngineImpl {
		@Override
		public void reapply() {
		}
	}

	private static TestElement createElement(CSSEngine engine, String tag, String cssClass, String id) {
		TestElement e = new TestElement(tag, engine);
		if (cssClass != null) {
			e.setClass(cssClass);
		}
		if (id != null) {
			e.setId(id);
		}
		return e;
	}

	private static Selector parse(CSSEngine engine, String selector) throws Exception {
		return engine.parseSelectors(selector).item(0);
	}

	@Test
	void testSelectorMatch() throws Exception {
		TestCSSEngine engine = new TestCSSEngine();
		SelectorList list = engine.parseSelectors("Date");
		engine.setElementProvider((element, engine1) -> new TestElement(element.getClass().getSimpleName(),
				engine1));
		assertFalse(engine.matches(list.item(0), new Object(), null));
		assertTrue(engine.matches(list.item(0), new Date(), null));
	}

	@Test
	void testSelectorMatchOneOf() throws Exception {
		TestCSSEngine engine = engineWhichProducesElementsWithAttributeA();
		Selector selector = engine.parseSelectors("E[a~='B']").item(0);
		assertTrue(engine.matches(selector, "B AB", null));
		assertTrue(engine.matches(selector, "BC B", null));
		assertFalse(engine.matches(selector, "ABC", null));
		assertTrue(engine.matches(selector, "B", null));
	}

	@Test
	void testSelectorAttributeIs() throws Exception {
		TestCSSEngine engine = engineWhichProducesElementsWithAttributeA();
		Selector selector = engine.parseSelectors("E[a='B']").item(0);
		assertFalse(engine.matches(selector, "ABC", null));
		assertTrue(engine.matches(selector, "B", null));
	}

	@Test
	void testSelectorAttributeIs_EmptySting() throws Exception {
		TestCSSEngine engine = engineWhichProducesElementsWithAttributeA();
		Selector selector = engine.parseSelectors("E[a='']").item(0);
		assertFalse(engine.matches(selector, "ABC", null));
		assertTrue(engine.matches(selector, "", null));
	}

	@Test
	void testSelectorAttributeIs_NotPresent() throws Exception {
		TestCSSEngine engine = engineWhichProducesElementsWithAttributeA();
		Selector selector = engine.parseSelectors("E[b='']").item(0);
		assertFalse(engine.matches(selector, "ABC", null));
		assertFalse(engine.matches(selector, "", null));
	}

	// --- Phase 1: selector matching contract tests ---

	@Test
	void testTypeSelector() throws Exception {
		TestCSSEngine engine = new TestCSSEngine();
		Selector selector = parse(engine, "Button");
		assertTrue(engine.matches(selector, createElement(engine, "Button", null, null), null));
		assertFalse(engine.matches(selector, createElement(engine, "Label", null, null), null));
	}

	@Test
	void testClassSelector() throws Exception {
		TestCSSEngine engine = new TestCSSEngine();
		Selector selector = parse(engine, ".foo");
		assertTrue(engine.matches(selector, createElement(engine, "Button", "foo", null), null));
		assertFalse(engine.matches(selector, createElement(engine, "Button", "bar", null), null));
		assertFalse(engine.matches(selector, createElement(engine, "Button", null, null), null));
	}

	@Test
	void testIdSelector() throws Exception {
		TestCSSEngine engine = new TestCSSEngine();
		Selector selector = parse(engine, "#bar");
		assertTrue(engine.matches(selector, createElement(engine, "Button", null, "bar"), null));
		assertFalse(engine.matches(selector, createElement(engine, "Button", null, "baz"), null));
		assertFalse(engine.matches(selector, createElement(engine, "Button", null, null), null));
	}

	@Test
	void testCompoundSelector() throws Exception {
		TestCSSEngine engine = new TestCSSEngine();
		Selector selector = parse(engine, "Button.primary#go");
		assertTrue(engine.matches(selector, createElement(engine, "Button", "primary", "go"), null));
		assertFalse(engine.matches(selector, createElement(engine, "Button", "primary", "stop"), null));
		assertFalse(engine.matches(selector, createElement(engine, "Button", "secondary", "go"), null));
		assertFalse(engine.matches(selector, createElement(engine, "Label", "primary", "go"), null));
	}

	@Test
	void testDescendantCombinator() throws Exception {
		TestCSSEngine engine = new TestCSSEngine();
		Selector selector = parse(engine, "Composite Button");
		TestElement composite = new TestElement("Composite", engine);
		TestElement intermediate = new TestElement("Group", composite, engine);
		TestElement button = new TestElement("Button", intermediate, engine);
		assertTrue(engine.matches(selector, button, null));

		TestElement composite2 = new TestElement("Composite", engine);
		TestElement directButton = new TestElement("Button", composite2, engine);
		assertTrue(engine.matches(selector, directButton, null));

		TestElement orphan = new TestElement("Button", engine);
		assertFalse(engine.matches(selector, orphan, null));
	}

	@Test
	void testChildCombinator() throws Exception {
		TestCSSEngine engine = new TestCSSEngine();
		Selector selector = parse(engine, "Composite > Button");
		TestElement composite = new TestElement("Composite", engine);
		TestElement directButton = new TestElement("Button", composite, engine);
		assertTrue(engine.matches(selector, directButton, null));

		TestElement composite2 = new TestElement("Composite", engine);
		TestElement intermediate = new TestElement("Group", composite2, engine);
		TestElement grandchildButton = new TestElement("Button", intermediate, engine);
		assertFalse(engine.matches(selector, grandchildButton, null));
	}

	@Test
	void testAttributePresent() throws Exception {
		TestCSSEngine engine = new TestCSSEngine();
		Selector selector = parse(engine, "Button[style]");
		TestElement withAttr = createElement(engine, "Button", null, null);
		withAttr.setAttribute("style", "SWT.PUSH");
		assertTrue(engine.matches(selector, withAttr, null));

		TestElement withoutAttr = createElement(engine, "Button", null, null);
		assertFalse(engine.matches(selector, withoutAttr, null));
	}

	@Test
	void testAttributeWordMatchIncludes() throws Exception {
		TestCSSEngine engine = new TestCSSEngine();
		Selector selector = parse(engine, "Button[style~='SWT.CHECK']");
		TestElement match = createElement(engine, "Button", null, null);
		match.setAttribute("style", "SWT.CHECK SWT.BORDER");
		assertTrue(engine.matches(selector, match, null));

		TestElement single = createElement(engine, "Button", null, null);
		single.setAttribute("style", "SWT.CHECK");
		assertTrue(engine.matches(selector, single, null));

		TestElement noMatch = createElement(engine, "Button", null, null);
		noMatch.setAttribute("style", "SWT.PUSH");
		assertFalse(engine.matches(selector, noMatch, null));
	}

	@Test
	void testAttributeWordMatchNoSubstring() throws Exception {
		TestCSSEngine engine = new TestCSSEngine();
		Selector selector = parse(engine, "Button[style~='CHECK']");
		TestElement element = createElement(engine, "Button", null, null);
		element.setAttribute("style", "SWT.CHECK");
		// Word boundary: 'CHECK' is not a whitespace-separated word in 'SWT.CHECK'.
		assertFalse(engine.matches(selector, element, null));
	}

	@Test
	void testPseudoClassSelected() throws Exception {
		TestCSSEngine engine = new TestCSSEngine();
		Selector selector = parse(engine, "Button:selected");
		TestElement selected = new TestElement("Button", engine) {
			@Override
			public boolean isPseudoInstanceOf(String s) {
				return "selected".equalsIgnoreCase(s);
			}
		};
		TestElement unselected = new TestElement("Button", engine);
		assertTrue(engine.matches(selector, selected, null));
		assertFalse(engine.matches(selector, unselected, null));
	}

	@Test
	void testPseudoClassDisabled() throws Exception {
		TestCSSEngine engine = new TestCSSEngine();
		Selector selector = parse(engine, "Button:disabled");
		TestElement disabled = new TestElement("Button", engine) {
			@Override
			public boolean isPseudoInstanceOf(String s) {
				return "disabled".equalsIgnoreCase(s);
			}
		};
		TestElement enabled = new TestElement("Button", engine);
		assertTrue(engine.matches(selector, disabled, null));
		assertFalse(engine.matches(selector, enabled, null));
	}

	@Test
	void testNegativeMatch() throws Exception {
		TestCSSEngine engine = new TestCSSEngine();
		Selector typeSelector = parse(engine, "Button");
		Selector classSelector = parse(engine, ".primary");
		Selector idSelector = parse(engine, "#go");
		TestElement element = createElement(engine, "Label", "secondary", "stop");
		assertFalse(engine.matches(typeSelector, element, null));
		assertFalse(engine.matches(classSelector, element, null));
		assertFalse(engine.matches(idSelector, element, null));
	}

	@Test
	void testSelectorListMatch() throws Exception {
		TestCSSEngine engine = new TestCSSEngine();
		SelectorList list = engine.parseSelectors(".a, .b");
		TestElement a = createElement(engine, "Button", "a", null);
		TestElement b = createElement(engine, "Button", "b", null);
		TestElement c = createElement(engine, "Button", "c", null);
		assertTrue(matchesAny(engine, list, a));
		assertTrue(matchesAny(engine, list, b));
		assertFalse(matchesAny(engine, list, c));
	}

	@Test
	void testTagNameCaseSensitivity() throws Exception {
		// Locks in current case-sensitive matching of the SAC engine. If the
		// parser swap moves to case-insensitive (closer to HTML semantics), this
		// test must be updated together with that change.
		TestCSSEngine engine = new TestCSSEngine();
		Selector capital = parse(engine, "Button");
		Selector lower = parse(engine, "button");
		TestElement capitalElement = createElement(engine, "Button", null, null);
		TestElement lowerElement = createElement(engine, "button", null, null);
		assertTrue(engine.matches(capital, capitalElement, null));
		assertTrue(engine.matches(lower, lowerElement, null));
		assertFalse(engine.matches(capital, lowerElement, null));
		assertFalse(engine.matches(lower, capitalElement, null));
	}

	private static boolean matchesAny(CSSEngine engine, SelectorList list, Element element) {
		for (int i = 0; i < list.getLength(); i++) {
			if (engine.matches(list.item(i), element, null)) {
				return true;
			}
		}
		return false;
	}

	private TestCSSEngine engineWhichProducesElementsWithAttributeA() {
		TestCSSEngine engine = new TestCSSEngine();
		engine.setElementProvider((element, aEngine) -> {
			Element e = new TestElement("E", aEngine);
			e.setAttribute("a", element.toString());
			return e;
		});
		return engine;
	}

}
