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

import org.eclipse.e4.ui.css.core.dom.CSSStylableElement;
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
import org.eclipse.e4.ui.css.core.impl.engine.selector.Selectors.Selector;
import org.eclipse.e4.ui.css.core.impl.engine.selector.Selectors.SelectorList;
import org.eclipse.e4.ui.css.core.impl.engine.selector.Selectors.Universal;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Matches a {@link Selector} against an {@link Element}.
 *
 * <p>
 * Every method is static; the matcher carries no state. Callers pass the
 * element being tested plus an optional pseudo-element string (the same
 * argument the SAC engine carried) so that pseudo-class matching can defer
 * to the existing {@link CSSStylableElement#isPseudoInstanceOf} contract.
 * </p>
 *
 * <p>
 * Tag-name comparison is case sensitive, matching the existing SAC matcher
 * (Phase 1 test {@code testTagNameCaseSensitivity} in {@code CSSEngineTest}
 * locks this in). Pseudo-class semantics also follow the existing engine:
 * the static-pseudo-instance carve-out from
 * {@code CSSPseudoClassConditionImpl} is preserved so cascade behaviour
 * does not shift.
 * </p>
 */
public final class SelectorMatcher {

	private SelectorMatcher() {
		// statics only
	}

	/**
	 * @return {@code true} if {@code selector} matches {@code element} for
	 *         the given pseudo state.
	 */
	public static boolean matches(Selector selector, Element element, String pseudoElement) {
		return matches(selector, element, pseudoElement, null, 0);
	}

	/**
	 * @return {@code true} if {@code selector} matches {@code element} for
	 *         the given pseudo state, using a pre-computed ancestor hierarchy array if available.
	 */
	public static boolean matches(Selector selector, Element element, String pseudoElement, Element[] hierarchy, int hierarchyIndex) {
		if (element == null) {
			return false;
		}
		if (selector instanceof Universal) {
			return true;
		}
		if (selector instanceof ElementType type) {
			return matchesElementType(type, element);
		}
		if (selector instanceof ClassSelector cls) {
			return matchesClass(cls, element);
		}
		if (selector instanceof IdSelector id) {
			return matchesId(id, element);
		}
		if (selector instanceof AttributeSelector attr) {
			return matchesAttribute(attr, element);
		}
		if (selector instanceof AttributeIncludes inc) {
			return matchesAttributeIncludes(inc, element);
		}
		if (selector instanceof AttributeBeginHyphen beg) {
			return matchesAttributeBeginHyphen(beg, element);
		}
		if (selector instanceof PseudoClass pc) {
			return matchesPseudoClass(pc, element, pseudoElement);
		}
		if (selector instanceof And and) {
			return matches(and.left(), element, pseudoElement, hierarchy, hierarchyIndex)
				&& matches(and.right(), element, pseudoElement, hierarchy, hierarchyIndex);
		}
		if (selector instanceof Descendant d) {
			return matchesDescendant(d, element, pseudoElement, hierarchy, hierarchyIndex);
		}
		if (selector instanceof Child c) {
			return matchesChild(c, element, pseudoElement, hierarchy, hierarchyIndex);
		}
		if (selector instanceof Adjacent a) {
			return matchesAdjacent(a, element, pseudoElement, hierarchy, hierarchyIndex);
		}
		if (selector instanceof SelectorList list) {
			return matchesAny(list, element, pseudoElement, hierarchy, hierarchyIndex);
		}
		throw new IllegalStateException("Unknown selector kind: " + selector.getClass()); //$NON-NLS-1$
	}

	private static boolean matchesElementType(ElementType type, Element element) {
		String localName = type.localName();
		if (localName == null) {
			return true;
		}
		String elementName = element.getPrefix() == null ? element.getNodeName() : element.getLocalName();
		return localName.equals(elementName);
	}

	private static boolean matchesClass(ClassSelector cls, Element element) {
		if (!(element instanceof CSSStylableElement stylable)) {
			return false;
		}
		String elementClass = stylable.getCSSClass();
		if (elementClass == null) {
			return false;
		}
		// CSS class attribute can be a whitespace-separated list of classes.
		// Walk the string manually to avoid the regex compile + array allocation
		// String.split forces on every match evaluation.
		return containsWord(elementClass, cls.className());
	}

	private static boolean matchesId(IdSelector id, Element element) {
		if (!(element instanceof CSSStylableElement stylable)) {
			return false;
		}
		return id.id().equals(stylable.getCSSId());
	}

	private static boolean matchesAttribute(AttributeSelector attr, Element element) {
		String name = attr.name();
		if (!element.hasAttribute(name)) {
			return false;
		}
		String required = attr.value();
		if (required == null) {
			// presence form: [attr]
			return true;
		}
		return required.equals(element.getAttribute(name));
	}

	private static boolean matchesAttributeIncludes(AttributeIncludes inc, Element element) {
		String actual = element.getAttribute(inc.name());
		if (actual == null) {
			return false;
		}
		return containsWord(actual, inc.value());
	}

	/**
	 * Returns {@code true} if {@code haystack} contains {@code word} as a
	 * whitespace-separated token. Equivalent to splitting on
	 * {@code \s+} and checking for an exact token match, but without the
	 * regex compile and array allocation each call.
	 */
	private static boolean containsWord(String haystack, String word) {
		if (word == null || word.isEmpty()) {
			return false;
		}
		int wordLength = word.length();
		int length = haystack.length();
		int i = 0;
		while (i < length) {
			while (i < length && Character.isWhitespace(haystack.charAt(i))) {
				i++;
			}
			int start = i;
			while (i < length && !Character.isWhitespace(haystack.charAt(i))) {
				i++;
			}
			if (i - start == wordLength && haystack.regionMatches(start, word, 0, wordLength)) {
				return true;
			}
		}
		return false;
	}

	private static boolean matchesAttributeBeginHyphen(AttributeBeginHyphen beg, Element element) {
		String actual = element.getAttribute(beg.name());
		if (actual == null) {
			return false;
		}
		String value = beg.value();
		return actual.equals(value) || actual.startsWith(value + "-");
	}

	private static boolean matchesPseudoClass(PseudoClass pseudo, Element element, String pseudoElement) {
		String name = pseudo.name();
		// If the caller is iterating a static-pseudo cascade, only match the
		// pseudo argument on the way down.
		if (pseudoElement != null && !pseudoElement.equals(name)) {
			return false;
		}
		if (!(element instanceof CSSStylableElement stylable)) {
			return false;
		}
		if (!stylable.isPseudoInstanceOf(name)) {
			return false;
		}
		if (pseudoElement == null) {
			// Same carve-out as CSSPseudoClassConditionImpl: when no pseudo
			// element argument is supplied, pseudos that the element
			// publishes only as static instances do not match the regular
			// cascade. They get applied separately via the default style
			// declaration map.
			return !stylable.isStaticPseudoInstance(name);
		}
		return true;
	}

	private static boolean matchesDescendant(Descendant d, Element element, String pseudoElement, Element[] hierarchy, int hierarchyIndex) {
		if (!matches(d.descendant(), element, pseudoElement, hierarchy, hierarchyIndex)) {
			return false;
		}
		if (hierarchy != null) {
			for (int i = hierarchyIndex + 1; i < hierarchy.length; i++) {
				if (matches(d.ancestor(), hierarchy[i], null, hierarchy, i)) {
					return true;
				}
			}
			return false;
		} else {
			Node parent = element.getParentNode();
			while (parent instanceof Element parentElement) {
				if (matches(d.ancestor(), parentElement, null, null, 0)) {
					return true;
				}
				parent = parentElement.getParentNode();
			}
			return false;
		}
	}

	private static boolean matchesChild(Child c, Element element, String pseudoElement, Element[] hierarchy, int hierarchyIndex) {
		if (!matches(c.child(), element, pseudoElement, hierarchy, hierarchyIndex)) {
			return false;
		}
		if (hierarchy != null) {
			int parentIdx = hierarchyIndex + 1;
			if (parentIdx < hierarchy.length) {
				return matches(c.parent(), hierarchy[parentIdx], null, hierarchy, parentIdx);
			}
			return false;
		} else {
			Node parent = element.getParentNode();
			return parent instanceof Element parentElement && matches(c.parent(), parentElement, null, null, 0);
		}
	}

	private static boolean matchesAdjacent(Adjacent a, Element element, String pseudoElement, Element[] hierarchy, int hierarchyIndex) {
		if (!matches(a.second(), element, pseudoElement, hierarchy, hierarchyIndex)) {
			return false;
		}
		Node previous = element.getPreviousSibling();
		while (previous != null && !(previous instanceof Element)) {
			previous = previous.getPreviousSibling();
		}
		return previous instanceof Element previousElement && matches(a.first(), previousElement, null, null, 0);
	}

	private static boolean matchesAny(SelectorList list, Element element, String pseudoElement, Element[] hierarchy, int hierarchyIndex) {
		for (Selector alternative : list.alternatives()) {
			if (matches(alternative, element, pseudoElement, hierarchy, hierarchyIndex)) {
				return true;
			}
		}
		return false;
	}
}
