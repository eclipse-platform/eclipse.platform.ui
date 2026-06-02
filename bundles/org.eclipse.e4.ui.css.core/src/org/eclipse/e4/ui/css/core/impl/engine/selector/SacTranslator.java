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
import java.util.List;
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
import org.eclipse.e4.ui.css.core.impl.engine.selector.Selectors.Universal;
import org.w3c.css.sac.AttributeCondition;
import org.w3c.css.sac.CombinatorCondition;
import org.w3c.css.sac.Condition;
import org.w3c.css.sac.ConditionalSelector;
import org.w3c.css.sac.DescendantSelector;
import org.w3c.css.sac.ElementSelector;
import org.w3c.css.sac.SelectorList;
import org.w3c.css.sac.SiblingSelector;
import org.w3c.css.sac.SimpleSelector;

/**
 * Converts a SAC selector tree (as produced by the Batik parser) into the
 * engine's internal {@link Selectors} AST.
 *
 * <p>
 * The translator is the single boundary between the SAC parser output and the
 * rest of the engine. Once a stylesheet has been parsed, only the internal
 * AST flows through {@code CSSEngine.matches}, the rule list, and
 * {@link SelectorMatcher}. SAC types do not cross this boundary.
 * </p>
 *
 * <p>
 * Specificity is preserved exactly: the internal records compute it the same
 * way the legacy SAC wrappers did (100 per id, 10 per class / attribute /
 * pseudo-class, 1 per element, 0 for {@code *}). Combinators sum operands.
 * </p>
 */
public final class SacTranslator {

	private SacTranslator() {
		// statics only
	}

	/** Translate an entire {@link SelectorList} into the internal form. */
	public static Selectors.SelectorList translate(SelectorList sacList) {
		List<Selector> alternatives = new ArrayList<>(sacList.getLength());
		for (int i = 0; i < sacList.getLength(); i++) {
			alternatives.add(translate(sacList.item(i)));
		}
		return new Selectors.SelectorList(alternatives);
	}

	/** Translate a single SAC {@link org.w3c.css.sac.Selector}. */
	public static Selector translate(org.w3c.css.sac.Selector sac) {
		return switch (sac.getSelectorType()) {
		case org.w3c.css.sac.Selector.SAC_ELEMENT_NODE_SELECTOR -> translateElement((ElementSelector) sac);
		case org.w3c.css.sac.Selector.SAC_PSEUDO_ELEMENT_SELECTOR -> translatePseudoElement((ElementSelector) sac);
		case org.w3c.css.sac.Selector.SAC_CONDITIONAL_SELECTOR -> translateConditional((ConditionalSelector) sac);
		case org.w3c.css.sac.Selector.SAC_DESCENDANT_SELECTOR -> {
			DescendantSelector d = (DescendantSelector) sac;
			yield new Descendant(translate(d.getAncestorSelector()), translateSimple(d.getSimpleSelector()));
		}
		case org.w3c.css.sac.Selector.SAC_CHILD_SELECTOR -> {
			DescendantSelector c = (DescendantSelector) sac;
			yield new Child(translate(c.getAncestorSelector()), translateSimple(c.getSimpleSelector()));
		}
		case org.w3c.css.sac.Selector.SAC_DIRECT_ADJACENT_SELECTOR -> {
			SiblingSelector s = (SiblingSelector) sac;
			yield new Adjacent(translate(s.getSelector()), translateSimple(s.getSiblingSelector()));
		}
		default -> throw new IllegalArgumentException(
				"Unsupported SAC selector type: " + sac.getSelectorType() + " (" + sac + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		};
	}

	private static Selector translateSimple(SimpleSelector simple) {
		return translate(simple);
	}

	private static Selector translateElement(ElementSelector sel) {
		String name = sel.getLocalName();
		return name == null ? new Universal() : new ElementType(name);
	}

	private static Selector translatePseudoElement(ElementSelector sel) {
		// Pseudo-element form (::first-line). The engine has never matched
		// these; treat as the element it appears on (if any) or universal.
		// In practice the parser only emits this when a stylesheet uses :: ,
		// which the supported subset does not.
		String name = sel.getLocalName();
		return name == null ? new Universal() : new PseudoClass(name);
	}

	private static Selector translateConditional(ConditionalSelector sel) {
		Selector left = translate(sel.getSimpleSelector());
		Selector right = translateCondition(sel.getCondition());
		if (left instanceof Universal) {
			return right;
		}
		return new And(left, right);
	}

	private static Selector translateCondition(Condition condition) {
		return switch (condition.getConditionType()) {
		case Condition.SAC_CLASS_CONDITION -> new ClassSelector(((AttributeCondition) condition).getValue());
		case Condition.SAC_ID_CONDITION -> new IdSelector(((AttributeCondition) condition).getValue());
		case Condition.SAC_PSEUDO_CLASS_CONDITION -> new PseudoClass(((AttributeCondition) condition).getValue());
		case Condition.SAC_LANG_CONDITION -> {
			// Modeled as a presence-form attribute selector keyed on lang;
			// nothing in the supported subset uses :lang(), but the parser
			// can still emit it.
			AttributeCondition lang = (AttributeCondition) condition;
			yield new AttributeSelector("lang", lang.getValue()); //$NON-NLS-1$
		}
		case Condition.SAC_ATTRIBUTE_CONDITION -> {
			AttributeCondition attr = (AttributeCondition) condition;
			// Batik's stock Parser always calls createAttributeCondition with
			// specified=false, regardless of whether the source was [attr] or
			// [attr='value']. Distinguish the two by whether a value was
			// supplied: null means the presence form [attr].
			yield new AttributeSelector(attr.getLocalName(), attr.getValue());
		}
		case Condition.SAC_ONE_OF_ATTRIBUTE_CONDITION -> {
			AttributeCondition attr = (AttributeCondition) condition;
			yield new AttributeIncludes(attr.getLocalName(), attr.getValue());
		}
		case Condition.SAC_BEGIN_HYPHEN_ATTRIBUTE_CONDITION -> {
			AttributeCondition attr = (AttributeCondition) condition;
			yield new AttributeBeginHyphen(attr.getLocalName(), attr.getValue());
		}
		case Condition.SAC_AND_CONDITION -> {
			CombinatorCondition combo = (CombinatorCondition) condition;
			yield new And(translateCondition(combo.getFirstCondition()),
					translateCondition(combo.getSecondCondition()));
		}
		default -> throw new IllegalArgumentException(
				"Unsupported SAC condition type: " + condition.getConditionType() + " (" + condition + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		};
	}
}
