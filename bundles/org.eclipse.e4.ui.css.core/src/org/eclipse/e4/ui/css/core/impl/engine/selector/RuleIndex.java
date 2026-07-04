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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.e4.ui.css.core.dom.CSSStylableElement;
import org.eclipse.e4.ui.css.core.impl.dom.CSSStyleRuleImpl;
import org.eclipse.e4.ui.css.core.impl.dom.CSSStyleSheetImpl;
import org.eclipse.e4.ui.css.core.impl.dom.CssRule;
import org.eclipse.e4.ui.css.core.impl.engine.selector.Selectors.Selector;
import org.w3c.dom.Element;

/**
 * Buckets style rules by the rightmost simple selector of each selector
 * alternative, so the cascade only runs the matcher against rules that can
 * possibly match a given element.
 */
public final class RuleIndex {

	/** One selector alternative of a style rule, with its cascade position. */
	public record Candidate(Selector selector, CSSStyleRuleImpl rule, int order) {
	}

	private static final Comparator<Candidate> BY_ORDER = Comparator.comparingInt(Candidate::order);

	private final Map<String, List<Candidate>> byId = new HashMap<>();
	private final Map<String, List<Candidate>> byClass = new HashMap<>();
	private final Map<String, List<Candidate>> byTag = new HashMap<>();
	private final List<Candidate> remainder = new ArrayList<>();
	private final List<Candidate> all = new ArrayList<>();

	private RuleIndex() {
	}

	/** Indexes every style rule of the given stylesheets in cascade order. */
	public static RuleIndex of(List<CSSStyleSheetImpl> styleSheets) {
		RuleIndex index = new RuleIndex();
		for (CSSStyleSheetImpl styleSheet : styleSheets) {
			for (CssRule rule : styleSheet.getRules()) {
				if (rule instanceof CSSStyleRuleImpl styleRule) {
					for (Selector alternative : styleRule.getSelectorList().alternatives()) {
						index.add(new Candidate(alternative, styleRule, index.all.size()));
					}
				}
			}
		}
		return index;
	}

	private void add(Candidate candidate) {
		all.add(candidate);
		Selector key = rightmostKey(candidate.selector());
		if (key instanceof Selectors.IdSelector id) {
			byId.computeIfAbsent(id.id(), k -> new ArrayList<>()).add(candidate);
		} else if (key instanceof Selectors.ClassSelector cls) {
			byClass.computeIfAbsent(cls.className(), k -> new ArrayList<>()).add(candidate);
		} else if (key instanceof Selectors.ElementType type) {
			byTag.computeIfAbsent(type.localName(), k -> new ArrayList<>()).add(candidate);
		} else {
			remainder.add(candidate);
		}
	}

	/**
	 * The most selective simple selector the rightmost compound requires of
	 * the subject element: id over class over element type. {@code null}
	 * when the compound only constrains the element by attributes or pseudo
	 * classes, or not at all; such alternatives go into the remainder bucket
	 * consulted for every element.
	 */
	private static Selector rightmostKey(Selector selector) {
		if (selector instanceof Selectors.Descendant d) {
			return rightmostKey(d.descendant());
		}
		if (selector instanceof Selectors.Child c) {
			return rightmostKey(c.child());
		}
		if (selector instanceof Selectors.Adjacent a) {
			return rightmostKey(a.second());
		}
		if (selector instanceof Selectors.And and) {
			Selector left = rightmostKey(and.left());
			Selector right = rightmostKey(and.right());
			return rank(left) >= rank(right) ? left : right;
		}
		if (selector instanceof Selectors.IdSelector || selector instanceof Selectors.ClassSelector) {
			return selector;
		}
		if (selector instanceof Selectors.ElementType type && type.localName() != null) {
			return selector;
		}
		return null;
	}

	private static int rank(Selector key) {
		if (key instanceof Selectors.IdSelector) {
			return 3;
		}
		if (key instanceof Selectors.ClassSelector) {
			return 2;
		}
		if (key instanceof Selectors.ElementType) {
			return 1;
		}
		return 0;
	}

	/**
	 * The candidates whose bucket key the element satisfies, in cascade
	 * order. A superset of the actually matching alternatives; callers still
	 * run the matcher on each candidate.
	 */
	public List<Candidate> candidatesFor(Element element) {
		List<Candidate> result = new ArrayList<>(remainder);
		String elementName = element.getPrefix() == null ? element.getNodeName() : element.getLocalName();
		addBucket(byTag, elementName, result);
		if (element instanceof CSSStylableElement stylable) {
			addClassBuckets(stylable.getCSSClass(), result);
			addBucket(byId, stylable.getCSSId(), result);
		}
		result.sort(BY_ORDER);
		return result;
	}

	/** Every indexed candidate, in cascade order. */
	public List<Candidate> allCandidates() {
		return Collections.unmodifiableList(all);
	}

	private static void addBucket(Map<String, List<Candidate>> buckets, String key, List<Candidate> result) {
		if (key == null || buckets.isEmpty()) {
			return;
		}
		List<Candidate> bucket = buckets.get(key);
		if (bucket != null) {
			result.addAll(bucket);
		}
	}

	private void addClassBuckets(String cssClass, List<Candidate> result) {
		if (cssClass == null || byClass.isEmpty()) {
			return;
		}
		// Same whitespace-token semantics as the matcher's class comparison;
		// a duplicate word must not add its bucket twice.
		List<String> words = null;
		int length = cssClass.length();
		int i = 0;
		while (i < length) {
			while (i < length && Character.isWhitespace(cssClass.charAt(i))) {
				i++;
			}
			int start = i;
			while (i < length && !Character.isWhitespace(cssClass.charAt(i))) {
				i++;
			}
			if (i > start) {
				String word = cssClass.substring(start, i);
				if (words == null) {
					words = new ArrayList<>(2);
				}
				if (!words.contains(word)) {
					words.add(word);
					addBucket(byClass, word, result);
				}
			}
		}
	}
}
