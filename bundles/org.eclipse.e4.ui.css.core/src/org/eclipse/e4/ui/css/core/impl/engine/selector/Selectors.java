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

import java.util.List;

/**
 * Internal CSS selector AST.
 *
 * <p>
 * The engine historically exposed W3C SAC selectors
 * ({@code org.w3c.css.sac.Selector} and friends) and matched against them
 * through a hierarchy of vendored Batik wrapper classes under
 * {@code impl/sac/*}. This package replaces both with a small set of records
 * that the engine owns end to end. The W3C SAC types stay only as long as
 * the parser still emits them; a translator turns the SAC selector tree
 * produced by the Batik SAC parser into one of these records before it
 * reaches the engine matcher.
 * </p>
 *
 * <p>
 * Specificity follows CSS 2.1: 100 per id, 10 per class / attribute /
 * pseudo-class, 1 per element, 0 for the universal selector. Combinators
 * sum the specificity of their operands; selector lists report the maximum
 * specificity over their alternatives.
 * </p>
 */
public final class Selectors {

	private Selectors() {
		// constants only
	}

	/** A parsed CSS selector. Sealed; pattern-match in the matcher. */
	public sealed interface Selector
			permits Universal, ElementType, ClassSelector, IdSelector, AttributeSelector,
			AttributeIncludes, AttributeBeginHyphen, PseudoClass, And, Descendant, Child, Adjacent, SelectorList {

		/** CSS specificity contribution of this selector. */
		int specificity();

		/** Best-effort textual reproduction of the selector. */
		String text();
	}

	/** {@code *} — matches any element. */
	public record Universal() implements Selector {
		@Override
		public int specificity() {
			return 0;
		}

		@Override
		public String text() {
			return "*"; //$NON-NLS-1$
		}

		@Override
		public String toString() {
			return text();
		}
	}

	/** {@code Button} — matches by local element name. */
	public record ElementType(String localName) implements Selector {
		@Override
		public int specificity() {
			return 1;
		}

		@Override
		public String text() {
			return localName;
		}

		@Override
		public String toString() {
			return text();
		}
	}

	/** {@code .foo} — matches by CSS class. */
	public record ClassSelector(String className) implements Selector {
		@Override
		public int specificity() {
			return 10;
		}

		@Override
		public String text() {
			return "." + className; //$NON-NLS-1$
		}

		@Override
		public String toString() {
			return text();
		}
	}

	/** {@code #foo} — matches by CSS id. */
	public record IdSelector(String id) implements Selector {
		@Override
		public int specificity() {
			return 100;
		}

		@Override
		public String text() {
			return "#" + id; //$NON-NLS-1$
		}

		@Override
		public String toString() {
			return text();
		}
	}

	/**
	 * {@code [attr]} or {@code [attr='value']}. {@code value} is {@code null}
	 * for the presence form and the empty string for {@code [attr='']}.
	 */
	public record AttributeSelector(String name, String value) implements Selector {
		@Override
		public int specificity() {
			return 10;
		}

		@Override
		public String text() {
			return value == null ? "[" + name + "]" : "[" + name + "='" + value + "']"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}

		@Override
		public String toString() {
			return text();
		}
	}

	/** {@code [attr~='value']} — matches when {@code attr} contains {@code value} as a whitespace-separated word. */
	public record AttributeIncludes(String name, String value) implements Selector {
		@Override
		public int specificity() {
			return 10;
		}

		@Override
		public String text() {
			return "[" + name + "~='" + value + "']"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		@Override
		public String toString() {
			return text();
		}
	}

	/** {@code [attr|='value']} — matches when {@code attr} equals {@code value} or starts with {@code value-}. */
	public record AttributeBeginHyphen(String name, String value) implements Selector {
		@Override
		public int specificity() {
			return 10;
		}

		@Override
		public String text() {
			return "[" + name + "|='" + value + "']"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		@Override
		public String toString() {
			return text();
		}
	}

	/**
	 * {@code :name} — matches when the element answers true to
	 * {@code isPseudoInstanceOf(name)} on its CSS-stylable element wrapper.
	 */
	public record PseudoClass(String name) implements Selector {
		@Override
		public int specificity() {
			return 10;
		}

		@Override
		public String text() {
			return ":" + name; //$NON-NLS-1$
		}

		@Override
		public String toString() {
			return text();
		}
	}

	/**
	 * Compound selector: every operand must match the same element. Built
	 * by the translator for forms like {@code Button.primary#go} or
	 * {@code [a][b]} where multiple simple selectors apply to one element.
	 */
	public record And(Selector left, Selector right) implements Selector {
		@Override
		public int specificity() {
			return left.specificity() + right.specificity();
		}

		@Override
		public String text() {
			return left.text() + right.text();
		}

		@Override
		public String toString() {
			return text();
		}
	}

	/** {@code ancestor descendant} — descendant combinator. */
	public record Descendant(Selector ancestor, Selector descendant) implements Selector {
		@Override
		public int specificity() {
			return ancestor.specificity() + descendant.specificity();
		}

		@Override
		public String text() {
			return ancestor.text() + " " + descendant.text(); //$NON-NLS-1$
		}

		@Override
		public String toString() {
			return text();
		}
	}

	/** {@code parent > child} — child combinator. */
	public record Child(Selector parent, Selector child) implements Selector {
		@Override
		public int specificity() {
			return parent.specificity() + child.specificity();
		}

		@Override
		public String text() {
			return parent.text() + " > " + child.text(); //$NON-NLS-1$
		}

		@Override
		public String toString() {
			return text();
		}
	}

	/** {@code first + second} — direct adjacent sibling combinator. */
	public record Adjacent(Selector first, Selector second) implements Selector {
		@Override
		public int specificity() {
			return first.specificity() + second.specificity();
		}

		@Override
		public String text() {
			return first.text() + " + " + second.text(); //$NON-NLS-1$
		}

		@Override
		public String toString() {
			return text();
		}
	}

	/**
	 * {@code a, b} — selector list. Specificity reports the maximum over the
	 * alternatives so cascade ordering can match a list against an element by
	 * iterating its alternatives.
	 *
	 * <p>
	 * A regular final class rather than a record because the cascade reads
	 * {@link #specificity()} once per matched alternative and we want it
	 * precomputed; record components cannot host derived state.
	 * </p>
	 */
	public static final class SelectorList implements Selector {

		private final List<Selector> alternatives;
		private final int specificity;

		public SelectorList(List<Selector> alternatives) {
			this.alternatives = List.copyOf(alternatives);
			int max = 0;
			for (Selector alternative : this.alternatives) {
				int s = alternative.specificity();
				if (s > max) {
					max = s;
				}
			}
			this.specificity = max;
		}

		public List<Selector> alternatives() {
			return alternatives;
		}

		/** Number of alternatives in the list. SAC-style accessor for callers iterating the list. */
		public int getLength() {
			return alternatives.size();
		}

		/** {@code i}-th alternative. SAC-style accessor for callers iterating the list. */
		public Selector item(int i) {
			return alternatives.get(i);
		}

		@Override
		public int specificity() {
			return specificity;
		}

		@Override
		public String text() {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < alternatives.size(); i++) {
				if (i > 0) {
					sb.append(", "); //$NON-NLS-1$
				}
				sb.append(alternatives.get(i).text());
			}
			return sb.toString();
		}

		@Override
		public boolean equals(Object o) {
			return o instanceof SelectorList other && alternatives.equals(other.alternatives);
		}

		@Override
		public int hashCode() {
			return alternatives.hashCode();
		}

		@Override
		public String toString() {
			return text();
		}
	}
}
