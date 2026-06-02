/*******************************************************************************
 * Copyright (c) 2026 Lars Vogel and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.impl.engine.selector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.e4.ui.css.core.impl.engine.CSSEngineImpl;
import org.eclipse.e4.ui.css.core.impl.engine.selector.Selectors.Adjacent;
import org.eclipse.e4.ui.css.core.impl.engine.selector.Selectors.And;
import org.eclipse.e4.ui.css.core.impl.engine.selector.Selectors.AttributeBeginHyphen;
import org.eclipse.e4.ui.css.core.impl.engine.selector.Selectors.AttributeIncludes;
import org.eclipse.e4.ui.css.core.impl.engine.selector.Selectors.AttributeSelector;
import org.eclipse.e4.ui.css.core.impl.engine.selector.Selectors.Child;
import org.eclipse.e4.ui.css.core.impl.engine.selector.Selectors.ClassSelector;
import org.eclipse.e4.ui.css.core.impl.engine.selector.Selectors.Descendant;
import org.eclipse.e4.ui.css.core.impl.engine.selector.Selectors.ElementType;
import org.eclipse.e4.ui.css.core.impl.engine.selector.Selectors.IdSelector;
import org.eclipse.e4.ui.css.core.impl.engine.selector.Selectors.PseudoClass;
import org.eclipse.e4.ui.css.core.impl.engine.selector.Selectors.SelectorList;
import org.eclipse.e4.ui.css.core.impl.engine.selector.Selectors.Universal;
import org.eclipse.e4.ui.tests.css.core.util.TestElement;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SelectorMatcher}. The cases mirror those in the
 * Phase 1 {@code CSSEngineTest}, but go through the new internal selector
 * AST instead of SAC. When Phase 3 Step 1 wires the engine to use this
 * matcher, the SAC-based duplicate tests can be retired.
 */
class SelectorMatcherTest {

	private static final TestCSSEngine ENGINE = new TestCSSEngine();

	private static final class TestCSSEngine extends CSSEngineImpl {
		@Override
		public void reapply() {
		}
	}

	private static TestElement element(String tag, String cssClass, String id) {
		TestElement e = new TestElement(tag, ENGINE);
		if (cssClass != null) {
			e.setClass(cssClass);
		}
		if (id != null) {
			e.setId(id);
		}
		return e;
	}

	@Test
	void universalMatchesAnything() {
		assertTrue(SelectorMatcher.matches(new Universal(), element("Button", null, null), null));
		assertTrue(SelectorMatcher.matches(new Universal(), element("Label", "x", "y"), null));
	}

	@Test
	void typeSelectorIsCaseSensitive() {
		assertTrue(SelectorMatcher.matches(new ElementType("Button"), element("Button", null, null), null));
		assertFalse(SelectorMatcher.matches(new ElementType("Button"), element("button", null, null), null));
		assertFalse(SelectorMatcher.matches(new ElementType("Button"), element("Label", null, null), null));
	}

	@Test
	void classSelector() {
		assertTrue(SelectorMatcher.matches(new ClassSelector("foo"), element("Button", "foo", null), null));
		assertFalse(SelectorMatcher.matches(new ClassSelector("foo"), element("Button", "bar", null), null));
		assertFalse(SelectorMatcher.matches(new ClassSelector("foo"), element("Button", null, null), null));
	}

	@Test
	void classSelectorMatchesOneOfMultipleClasses() {
		assertTrue(SelectorMatcher.matches(new ClassSelector("foo"), element("Button", "foo bar", null), null));
		assertTrue(SelectorMatcher.matches(new ClassSelector("bar"), element("Button", "foo bar", null), null));
		assertFalse(SelectorMatcher.matches(new ClassSelector("baz"), element("Button", "foo bar", null), null));
	}

	@Test
	void idSelector() {
		assertTrue(SelectorMatcher.matches(new IdSelector("go"), element("Button", null, "go"), null));
		assertFalse(SelectorMatcher.matches(new IdSelector("go"), element("Button", null, "stop"), null));
		assertFalse(SelectorMatcher.matches(new IdSelector("go"), element("Button", null, null), null));
	}

	@Test
	void compoundSelector() {
		Selectors.Selector selector = new And(new And(new ElementType("Button"), new ClassSelector("primary")),
				new IdSelector("go"));
		assertTrue(SelectorMatcher.matches(selector, element("Button", "primary", "go"), null));
		assertFalse(SelectorMatcher.matches(selector, element("Label", "primary", "go"), null));
		assertFalse(SelectorMatcher.matches(selector, element("Button", "secondary", "go"), null));
		assertFalse(SelectorMatcher.matches(selector, element("Button", "primary", "stop"), null));
	}

	@Test
	void descendantCombinator() {
		Selectors.Selector selector = new Descendant(new ElementType("Composite"), new ElementType("Button"));
		TestElement composite = element("Composite", null, null);
		TestElement intermediate = new TestElement("Group", composite, ENGINE);
		TestElement button = new TestElement("Button", intermediate, ENGINE);
		assertTrue(SelectorMatcher.matches(selector, button, null));

		TestElement orphan = element("Button", null, null);
		assertFalse(SelectorMatcher.matches(selector, orphan, null));
	}

	@Test
	void childCombinator() {
		Selectors.Selector selector = new Child(new ElementType("Composite"), new ElementType("Button"));
		TestElement composite = element("Composite", null, null);
		TestElement direct = new TestElement("Button", composite, ENGINE);
		assertTrue(SelectorMatcher.matches(selector, direct, null));

		TestElement intermediate = new TestElement("Group", composite, ENGINE);
		TestElement grandchild = new TestElement("Button", intermediate, ENGINE);
		assertFalse(SelectorMatcher.matches(selector, grandchild, null));
	}

	@Test
	void attributePresentMatchesEvenWithEmptyValue() {
		AttributeSelector selector = new AttributeSelector("style", null);
		TestElement withAttr = element("Button", null, null);
		withAttr.setAttribute("style", "SWT.PUSH");
		assertTrue(SelectorMatcher.matches(selector, withAttr, null));
		assertFalse(SelectorMatcher.matches(selector, element("Button", null, null), null));
	}

	@Test
	void attributeIncludesIsWordBoundaryMatch() {
		AttributeIncludes selector = new AttributeIncludes("style", "SWT.CHECK");
		TestElement match = element("Button", null, null);
		match.setAttribute("style", "SWT.CHECK SWT.BORDER");
		assertTrue(SelectorMatcher.matches(selector, match, null));

		TestElement substring = element("Button", null, null);
		substring.setAttribute("style", "SWT.CHECK_DELAYED");
		// 'CHECK' is a substring of 'CHECK_DELAYED' but not a whitespace-separated word.
		assertFalse(SelectorMatcher.matches(new AttributeIncludes("style", "CHECK"), substring, null));
	}

	@Test
	void attributeBeginHyphen() {
		AttributeBeginHyphen selector = new AttributeBeginHyphen("lang", "en");
		TestElement exact = element("p", null, null);
		exact.setAttribute("lang", "en");
		TestElement prefixed = element("p", null, null);
		prefixed.setAttribute("lang", "en-US");
		TestElement other = element("p", null, null);
		other.setAttribute("lang", "fr");
		assertTrue(SelectorMatcher.matches(selector, exact, null));
		assertTrue(SelectorMatcher.matches(selector, prefixed, null));
		assertFalse(SelectorMatcher.matches(selector, other, null));
	}

	@Test
	void pseudoClassMatchesViaIsPseudoInstanceOf() {
		PseudoClass selector = new PseudoClass("selected");
		TestElement on = new TestElement("Button", ENGINE) {
			@Override
			public boolean isPseudoInstanceOf(String s) {
				return "selected".equals(s);
			}
		};
		TestElement off = element("Button", null, null);
		assertTrue(SelectorMatcher.matches(selector, on, null));
		assertFalse(SelectorMatcher.matches(selector, off, null));
	}

	@Test
	void selectorListMatchesAnyAlternative() {
		SelectorList list = new SelectorList(List.of(new ClassSelector("a"), new ClassSelector("b")));
		assertTrue(SelectorMatcher.matches(list, element("Button", "a", null), null));
		assertTrue(SelectorMatcher.matches(list, element("Button", "b", null), null));
		assertFalse(SelectorMatcher.matches(list, element("Button", "c", null), null));
	}

	@Test
	void specificityMatchesCss21() {
		assertEquals(0, new Universal().specificity());
		assertEquals(1, new ElementType("Button").specificity());
		assertEquals(10, new ClassSelector("foo").specificity());
		assertEquals(100, new IdSelector("go").specificity());
		assertEquals(11, new And(new ElementType("Button"), new ClassSelector("primary")).specificity());
		assertEquals(111, new And(new And(new ElementType("Button"), new ClassSelector("primary")),
				new IdSelector("go")).specificity());
	}

	@Test
	void specificityOfSelectorListIsMaxOverAlternatives() {
		// "Button, .foo, #go": max specificity is the id (100).
		SelectorList list = new SelectorList(
				List.of(new ElementType("Button"), new ClassSelector("foo"), new IdSelector("go")));
		assertEquals(100, list.specificity());
	}

	@Test
	void adjacentSiblingCombinatorRequiresSiblingSupport() {
		// TestElement's ElementAdapter base returns null from getPreviousSibling,
		// so adjacent matching cannot succeed against it. Locks in that the
		// matcher returns false rather than throwing on elements without
		// sibling support.
		Adjacent selector = new Adjacent(new ElementType("Label"), new ElementType("Button"));
		TestElement parent = element("Composite", null, null);
		new TestElement("Label", parent, ENGINE);
		TestElement second = new TestElement("Button", parent, ENGINE);
		assertFalse(SelectorMatcher.matches(selector, second, null));
	}
}
