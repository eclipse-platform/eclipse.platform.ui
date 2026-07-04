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
package org.eclipse.e4.ui.tests.css.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.impl.engine.selector.RuleIndex;
import org.eclipse.e4.ui.css.core.impl.engine.selector.SelectorMatcher;
import org.eclipse.e4.ui.tests.css.core.util.ParserTestUtil;
import org.eclipse.e4.ui.tests.css.core.util.TestElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.css.CSSStyleDeclaration;

/**
 * Verifies that the rule index never filters out an alternative the matcher
 * would accept and that candidates keep cascade order.
 */
public class RuleIndexTest {

	private static final String SHEET = """
			Button { p1: v; }
			.warning { p2: v; }
			#go { p3: v; }
			Button.warning { p4: v; }
			Button#go.warning { p5: v; }
			Composite Button { p6: v; }
			Shell > .warning { p7: v; }
			Label + Button { p8: v; }
			* { p9: v; }
			[role] { p10: v; }
			[role='editor'] { p11: v; }
			[tags~='x'] { p12: v; }
			:selected { p13: v; }
			Button:selected { p14: v; }
			Button, .warning, #go, [role], Text { p15: v; }
			Shell Composite Button.warning { p16: v; }
			Composite > Button#go { p17: v; }
			.other.warning { p18: v; }
			""";

	private CSSEngine engine;
	private RuleIndex index;

	@BeforeEach
	void setUp() throws Exception {
		engine = ParserTestUtil.createEngine();
		index = RuleIndex.of(List.of(ParserTestUtil.parseCss(SHEET)));
	}

	private List<TestElement> buildElements() {
		List<TestElement> elements = new ArrayList<>();
		TestElement shell = new TestElement("Shell", engine);
		elements.add(shell);
		TestElement composite = new TestElement("Composite", shell, engine);
		elements.add(composite);

		TestElement button = new TestElement("Button", composite, engine);
		button.setClass("warning other");
		button.setId("go");
		button.setAttribute("role", "editor");
		button.setAttribute("tags", "x y");
		elements.add(button);

		TestElement label = new TestElement("Label", composite, engine);
		elements.add(label);
		TestElement adjacentButton = new TestElement("Button", composite, engine);
		elements.add(adjacentButton);

		TestElement duplicateClasses = new TestElement("Text", shell, engine);
		duplicateClasses.setClass("warning warning");
		elements.add(duplicateClasses);

		TestElement other = new TestElement("Text", shell, engine);
		other.setClass("other");
		other.setAttribute("role", "outline");
		elements.add(other);

		TestElement plain = new TestElement("Canvas", shell, engine);
		elements.add(plain);
		return elements;
	}

	@Test
	void testCandidatesCompleteAndOrdered() {
		for (TestElement element : buildElements()) {
			assertCandidatesComplete(element, null);
			assertCandidatesComplete(element, "selected");
		}
	}

	private void assertCandidatesComplete(TestElement element, String pseudo) {
		List<Integer> linearMatches = new ArrayList<>();
		for (RuleIndex.Candidate candidate : index.allCandidates()) {
			if (SelectorMatcher.matches(candidate.selector(), element, pseudo)) {
				linearMatches.add(candidate.order());
			}
		}
		List<Integer> indexedMatches = new ArrayList<>();
		int previousOrder = -1;
		for (RuleIndex.Candidate candidate : index.candidatesFor(element)) {
			assertTrue(candidate.order() > previousOrder,
					"candidates for " + element.getLocalName() + " not in cascade order");
			previousOrder = candidate.order();
			if (SelectorMatcher.matches(candidate.selector(), element, pseudo)) {
				indexedMatches.add(candidate.order());
			}
		}
		assertEquals(linearMatches, indexedMatches,
				"index dropped matching alternatives for " + element.getLocalName());
	}

	@Test
	void testCandidatesArePrunedForPlainElement() {
		TestElement plain = new TestElement("Canvas", engine);
		List<RuleIndex.Candidate> candidates = index.candidatesFor(plain);
		assertFalse(candidates.isEmpty());
		assertTrue(candidates.size() < index.allCandidates().size(),
				"expected the index to prune non-applicable rules");
	}

	@Test
	void testComputedStyleMergesAcrossBuckets() throws Exception {
		engine.parseStyleSheet(new StringReader(
				"Button { color: red; } .warning { color: blue; }"));
		TestElement button = new TestElement("Button", engine);
		button.setClass("warning");
		CSSStyleDeclaration style = engine.computeStyle(button, null);
		assertEquals("blue", style.getPropertyCSSValue("color").getCssText());
	}

	@Test
	void testComputedStyleKeepsDocumentOrderOnEqualSpecificity() throws Exception {
		engine.parseStyleSheet(new StringReader(
				".a { color: red; } .b { color: green; }"));
		TestElement element = new TestElement("Button", engine);
		element.setClass("a b");
		CSSStyleDeclaration style = engine.computeStyle(element, null);
		assertEquals("green", style.getPropertyCSSValue("color").getCssText());
	}
}
