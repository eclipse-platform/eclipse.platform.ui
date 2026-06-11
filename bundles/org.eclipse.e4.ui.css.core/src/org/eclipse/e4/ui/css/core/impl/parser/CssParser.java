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
package org.eclipse.e4.ui.css.core.impl.parser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.ui.css.core.impl.dom.CSSImportRuleImpl;
import org.eclipse.e4.ui.css.core.impl.dom.CSSPropertyImpl;
import org.eclipse.e4.ui.css.core.impl.dom.CSSStyleDeclarationImpl;
import org.eclipse.e4.ui.css.core.impl.dom.CSSStyleRuleImpl;
import org.eclipse.e4.ui.css.core.impl.dom.CSSStyleSheetImpl;
import org.eclipse.e4.ui.css.core.impl.dom.CssRule;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssColor;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssDimension;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssList;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssNumber;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssNumeric;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssOperator;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssPrimitive;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssText;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssUnit;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssValue;
import org.eclipse.e4.ui.css.core.impl.engine.selector.Selectors;
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
import org.eclipse.e4.ui.css.core.impl.parser.CssTokenizer.Kind;
import org.eclipse.e4.ui.css.core.impl.parser.CssTokenizer.Token;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;

/**
 * Recursive-descent CSS parser for the Eclipse engine's supported subset. It
 * builds {@code CSSStyleSheetImpl} with {@code CSSStyleRuleImpl} / {@code
 * CSSImportRuleImpl} rules, {@code Selectors} selector trees, and
 * {@link org.eclipse.e4.ui.css.core.impl.dom.CssValues} value records.
 *
 * <p>
 * {@code @media}, {@code @font-face} and {@code @page} are parsed and discarded;
 * {@code !important} is recorded on the declaration; {@code @import} is kept as
 * an import rule.
 * </p>
 */
public final class CssParser {

	private final List<Token> tokens;
	private int index;

	private CssParser(List<Token> tokens) {
		this.tokens = tokens;
	}

	/** Parse a complete style sheet. */
	public static CSSStyleSheetImpl parseStyleSheet(String css) {
		return new CssParser(CssTokenizer.tokenize(css)).styleSheet();
	}

	/** Parse a standalone declaration list, e.g. {@code "color: red; margin: 0"}. */
	public static CSSStyleDeclaration parseStyleDeclaration(String style) {
		CssParser parser = new CssParser(CssTokenizer.tokenize(style));
		CSSStyleDeclarationImpl declaration = new CSSStyleDeclarationImpl(null);
		parser.declarations(declaration);
		return declaration;
	}

	/** Parse a single property value, e.g. {@code "1px 2px"} or {@code "#fff"}. */
	public static CSSValue parsePropertyValue(String value) {
		return new CssParser(CssTokenizer.tokenize(value)).value();
	}

	/** Parse a standalone selector group, e.g. for {@code CSSEngine.parseSelectors}. */
	public static Selectors.SelectorList parseSelectors(String selectors) {
		CssParser parser = new CssParser(CssTokenizer.tokenize(selectors));
		Selectors.SelectorList list = parser.selectorList();
		parser.skipWhitespace();
		if (parser.peek().kind != Kind.EOF) {
			throw parser.error("Unexpected trailing input in selector"); //$NON-NLS-1$
		}
		return list;
	}

	// ---------- style sheet ----------

	private CSSStyleSheetImpl styleSheet() {
		List<CssRule> rules = new ArrayList<>();

		skipWhitespace();
		while (peek().kind != Kind.EOF) {
			if (peek().kind == Kind.AT_KEYWORD) {
				atRule(rules);
			} else {
				styleRule(rules);
			}
			skipWhitespace();
		}
		return new CSSStyleSheetImpl(rules);
	}

	private void atRule(List<CssRule> rules) {
		Token at = advance(); // AT_KEYWORD
		String name = at.text.toLowerCase();
		if (name.equals("import")) { //$NON-NLS-1$
			skipWhitespace();
			Token target = peek();
			String href = null;
			if (target.kind == Kind.URI || target.kind == Kind.STRING) {
				href = target.text;
				advance();
			}
			discardUntilStatementEnd();
			if (href != null) {
				rules.add(new CSSImportRuleImpl(href));
			}
		} else {
			// @media / @font-face / @page / unknown: parse and discard.
			discardUntilStatementEnd();
		}
	}

	/** Skip the remainder of an at-rule: either a {@code ;} statement or a balanced {@code { ... }} block. */
	private void discardUntilStatementEnd() {
		int depth = 0;
		while (peek().kind != Kind.EOF) {
			Kind kind = peek().kind;
			if (kind == Kind.LBRACE) {
				depth++;
				advance();
			} else if (kind == Kind.RBRACE) {
				advance();
				if (--depth <= 0) {
					return;
				}
			} else if (kind == Kind.SEMICOLON && depth == 0) {
				advance();
				return;
			} else {
				advance();
			}
		}
		if (depth > 0) {
			throw error("Unterminated at-rule block"); //$NON-NLS-1$
		}
	}

	private void styleRule(List<CssRule> rules) {
		Selectors.SelectorList selectors = selectorList();
		expect(Kind.LBRACE);
		CSSStyleRuleImpl rule = new CSSStyleRuleImpl(selectors);
		CSSStyleDeclarationImpl declaration = new CSSStyleDeclarationImpl(rule);
		rule.setStyle(declaration);
		declarations(declaration);
		if (peek().kind == Kind.RBRACE) {
			advance();
		}
		rules.add(rule);
	}

	/** Parse declarations up to a closing brace or end of input; the brace is left in place. */
	private void declarations(CSSStyleDeclarationImpl declaration) {
		while (true) {
			skipWhitespace();
			Kind kind = peek().kind;
			if (kind == Kind.RBRACE || kind == Kind.EOF) {
				return;
			}
			if (kind == Kind.SEMICOLON) {
				advance(); // tolerate stray and leading semicolons
				continue;
			}
			declaration(declaration);
		}
	}

	private void declaration(CSSStyleDeclarationImpl declaration) {
		Token name = expect(Kind.IDENT);
		skipWhitespace();
		expect(Kind.COLON);
		CssValue value = value();

		boolean important = false;
		skipWhitespace();
		if (peek().kind == Kind.BANG) {
			advance();
			skipWhitespace();
			Token keyword = expect(Kind.IDENT);
			if (!keyword.text.equalsIgnoreCase("important")) { //$NON-NLS-1$
				throw error("Expected 'important' after '!'"); //$NON-NLS-1$
			}
			important = true;
			skipWhitespace();
		}
		if (peek().kind == Kind.SEMICOLON) {
			advance();
		}
		if (value != null) {
			declaration.addProperty(new CSSPropertyImpl(name.text, value, important));
		}
	}

	// ---------- values ----------

	private CssValue value() {
		List<CssValue> values = new ArrayList<>();
		while (true) {
			skipWhitespace();
			Kind kind = peek().kind;
			if (kind == Kind.SEMICOLON || kind == Kind.RBRACE || kind == Kind.BANG || kind == Kind.EOF) {
				break;
			}
			if (kind == Kind.COMMA) {
				advance();
				values.add(new CssOperator(",")); //$NON-NLS-1$
				continue;
			}
			values.add(primitive());
		}
		if (values.isEmpty()) {
			return null;
		}
		return values.size() == 1 ? values.get(0) : new CssList(values);
	}

	private CssPrimitive primitive() {
		Token token = peek();
		switch (token.kind) {
		case NUMBER:
			advance();
			return new CssNumber(token.number, token.integer);
		case DIMENSION:
			advance();
			return new CssDimension(token.number, dimensionUnit(token.unit), token.unit);
		case PERCENTAGE:
			advance();
			return new CssDimension(token.number, CssUnit.PERCENT);
		case IDENT:
			advance();
			return token.text.equalsIgnoreCase("inherit") //$NON-NLS-1$
					? new CssText(CssText.Kind.INHERIT, "inherit") //$NON-NLS-1$
					: new CssText(CssText.Kind.IDENT, token.text);
		case STRING:
			advance();
			return new CssText(CssText.Kind.STRING, token.text);
		case URI:
			advance();
			return new CssText(CssText.Kind.URI, token.text);
		case HASH:
			advance();
			return hexColor(token.text);
		case FUNCTION:
			return function(token);
		default:
			throw error("Unexpected token in value: " + token); //$NON-NLS-1$
		}
	}

	private CssColor function(Token token) {
		if (!token.text.equalsIgnoreCase("rgb")) { //$NON-NLS-1$
			throw error("Unsupported function in value: " + token.text + "()"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		advance(); // FUNCTION consumes the '('
		CssNumeric red = component();
		CssNumeric green = component();
		CssNumeric blue = component();
		skipWhitespace();
		expect(Kind.RPAREN);
		return new CssColor(red, green, blue);
	}

	/** One numeric component of an {@code rgb(...)} function, with the trailing comma skipped. */
	private CssNumeric component() {
		skipWhitespace();
		CssPrimitive value = primitive();
		if (!(value instanceof CssNumeric numeric)) {
			throw error("Expected a numeric component in rgb(): " + value.getCssText()); //$NON-NLS-1$
		}
		skipWhitespace();
		if (peek().kind == Kind.COMMA) {
			advance();
		}
		return numeric;
	}

	private CssColor hexColor(String hex) {
		int r;
		int g;
		int b;
		if (hex.length() == 3) {
			r = hexPair(hex.charAt(0), hex.charAt(0));
			g = hexPair(hex.charAt(1), hex.charAt(1));
			b = hexPair(hex.charAt(2), hex.charAt(2));
		} else if (hex.length() == 6) {
			r = hexPair(hex.charAt(0), hex.charAt(1));
			g = hexPair(hex.charAt(2), hex.charAt(3));
			b = hexPair(hex.charAt(4), hex.charAt(5));
		} else {
			throw error("Invalid hex colour #" + hex); //$NON-NLS-1$
		}
		return new CssColor(new CssNumber(r, true), new CssNumber(g, true), new CssNumber(b, true));
	}

	private int hexPair(char high, char low) {
		return Character.digit(high, 16) * 16 + Character.digit(low, 16);
	}

	private static CssUnit dimensionUnit(String unit) {
		return switch (unit.toLowerCase()) {
		case "px" -> CssUnit.PX; //$NON-NLS-1$
		case "em" -> CssUnit.EM; //$NON-NLS-1$
		case "ex" -> CssUnit.EX; //$NON-NLS-1$
		case "cm" -> CssUnit.CM; //$NON-NLS-1$
		case "mm" -> CssUnit.MM; //$NON-NLS-1$
		case "in" -> CssUnit.IN; //$NON-NLS-1$
		case "pt" -> CssUnit.PT; //$NON-NLS-1$
		case "pc" -> CssUnit.PC; //$NON-NLS-1$
		case "deg" -> CssUnit.DEG; //$NON-NLS-1$
		default -> CssUnit.OTHER;
		};
	}

	// ---------- selectors ----------

	private Selectors.SelectorList selectorList() {
		List<Selector> alternatives = new ArrayList<>();
		alternatives.add(complex());
		skipWhitespace();
		while (peek().kind == Kind.COMMA) {
			advance();
			alternatives.add(complex());
			skipWhitespace();
		}
		return new Selectors.SelectorList(alternatives);
	}

	private Selector complex() {
		skipWhitespace(); // leading space before a compound is not a combinator
		Selector left = compound();
		while (true) {
			boolean hadWhitespace = skipWhitespace();
			Kind kind = peek().kind;
			if (kind == Kind.GT) {
				advance();
				skipWhitespace();
				left = new Child(left, compound());
			} else if (kind == Kind.PLUS) {
				advance();
				skipWhitespace();
				left = new Adjacent(left, compound());
			} else if (hadWhitespace && startsCompound(kind)) {
				left = new Descendant(left, compound());
			} else {
				return left;
			}
		}
	}

	private static boolean startsCompound(Kind kind) {
		return kind == Kind.STAR || kind == Kind.IDENT || kind == Kind.DOT || kind == Kind.HASH
				|| kind == Kind.LBRACKET || kind == Kind.COLON;
	}

	private Selector compound() {
		Selector element;
		Kind first = peek().kind;
		if (first == Kind.STAR) {
			advance();
			element = new Universal();
		} else if (first == Kind.IDENT) {
			element = new ElementType(advance().text);
		} else {
			element = new Universal(); // implicit universal before a condition
		}

		List<Selector> conditions = new ArrayList<>();
		boolean reading = true;
		while (reading) {
			switch (peek().kind) {
			case DOT:
				advance();
				conditions.add(new ClassSelector(expect(Kind.IDENT).text));
				break;
			case HASH:
				conditions.add(new IdSelector(advance().text));
				break;
			case LBRACKET:
				conditions.add(attribute());
				break;
			case COLON:
				advance();
				if (peek().kind == Kind.COLON) {
					advance(); // pseudo-element ::, modelled as a pseudo-class
				}
				conditions.add(new PseudoClass(expect(Kind.IDENT).text));
				break;
			default:
				reading = false;
				break;
			}
		}

		if (conditions.isEmpty()) {
			return element;
		}
		Selector conditionTree = conditions.get(conditions.size() - 1);
		for (int i = conditions.size() - 2; i >= 0; i--) {
			conditionTree = new And(conditions.get(i), conditionTree);
		}
		// A universal element before conditions is dropped: '.foo' and '*[a]'
		// carry no element-type contribution.
		return element instanceof ElementType ? new And(element, conditionTree) : conditionTree;
	}

	private Selector attribute() {
		expect(Kind.LBRACKET);
		skipWhitespace();
		String name = expect(Kind.IDENT).text;
		skipWhitespace();
		Kind op = peek().kind;
		if (op == Kind.RBRACKET) {
			advance();
			return new AttributeSelector(name, null); // presence form [attr]
		}
		if (op != Kind.EQUALS && op != Kind.INCLUDE_MATCH && op != Kind.DASH_MATCH) {
			throw error("Unsupported attribute operator: " + peek()); //$NON-NLS-1$
		}
		advance();
		skipWhitespace();
		String value = attributeValue();
		skipWhitespace();
		expect(Kind.RBRACKET);
		return switch (op) {
		case INCLUDE_MATCH -> new AttributeIncludes(name, value);
		case DASH_MATCH -> new AttributeBeginHyphen(name, value);
		default -> new AttributeSelector(name, value);
		};
	}

	private String attributeValue() {
		Token token = peek();
		if (token.kind == Kind.STRING || token.kind == Kind.IDENT) {
			advance();
			return token.text;
		}
		throw error("Expected attribute value, found " + token); //$NON-NLS-1$
	}

	// ---------- token cursor ----------

	private Token peek() {
		return tokens.get(index);
	}

	private Token advance() {
		return tokens.get(index++);
	}

	private boolean skipWhitespace() {
		boolean skipped = false;
		while (peek().kind == Kind.WS) {
			advance();
			skipped = true;
		}
		return skipped;
	}

	private Token expect(Kind kind) {
		Token token = peek();
		if (token.kind != kind) {
			throw error("Expected " + kind + " but found " + token); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return advance();
	}

	private CssParseException error(String message) {
		Token token = peek();
		return new CssParseException(message, token.line, token.column);
	}
}
