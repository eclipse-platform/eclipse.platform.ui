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

/**
 * Splits CSS source into a flat token stream for {@link CssParser}. Scoped to
 * the CSS subset the Eclipse engine uses:
 * type / class / id / attribute / pseudo selectors, the child, descendant and
 * adjacent combinators, declarations with length / percentage / number / colour
 * / identifier / string / {@code url()} / {@code rgb()} values, {@code !important},
 * and the {@code @import} / {@code @media} / {@code @font-face} at-rules.
 */
final class CssTokenizer {

	enum Kind {
		IDENT, FUNCTION, AT_KEYWORD, HASH, STRING, NUMBER, PERCENTAGE, DIMENSION, URI,
		COLON, SEMICOLON, COMMA, LBRACE, RBRACE, LBRACKET, RBRACKET, RPAREN,
		DOT, STAR, GT, PLUS, TILDE, BAR, EQUALS, INCLUDE_MATCH, DASH_MATCH, BANG,
		WS, EOF
	}

	static final class Token {
		final Kind kind;
		final String text;
		final double number;
		final boolean integer;
		final String unit;
		final int line;
		final int column;

		Token(Kind kind, String text, double number, boolean integer, String unit, int line, int column) {
			this.kind = kind;
			this.text = text;
			this.number = number;
			this.integer = integer;
			this.unit = unit;
			this.line = line;
			this.column = column;
		}

		@Override
		public String toString() {
			return kind + (text != null ? "(" + text + ")" : ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}

	private final String input;
	private int pos;
	private int line = 1;
	private int column = 1;

	private CssTokenizer(String input) {
		this.input = input;
	}

	static List<Token> tokenize(String input) {
		return new CssTokenizer(input).run();
	}

	private List<Token> run() {
		List<Token> tokens = new ArrayList<>();
		Token token;
		do {
			token = next();
			tokens.add(token);
		} while (token.kind != Kind.EOF);
		return tokens;
	}

	private Token next() {
		skipComments();
		if (pos >= input.length()) {
			return token(Kind.EOF, null);
		}
		int startLine = line;
		int startColumn = column;
		char c = input.charAt(pos);

		if (isWhitespace(c)) {
			while (pos < input.length() && isWhitespace(input.charAt(pos))) {
				advance();
			}
			return new Token(Kind.WS, " ", 0, false, null, startLine, startColumn); //$NON-NLS-1$
		}

		switch (c) {
		case '{': advance(); return new Token(Kind.LBRACE, "{", 0, false, null, startLine, startColumn); //$NON-NLS-1$
		case '}': advance(); return new Token(Kind.RBRACE, "}", 0, false, null, startLine, startColumn); //$NON-NLS-1$
		case '[': advance(); return new Token(Kind.LBRACKET, "[", 0, false, null, startLine, startColumn); //$NON-NLS-1$
		case ']': advance(); return new Token(Kind.RBRACKET, "]", 0, false, null, startLine, startColumn); //$NON-NLS-1$
		case ')': advance(); return new Token(Kind.RPAREN, ")", 0, false, null, startLine, startColumn); //$NON-NLS-1$
		case ':': advance(); return new Token(Kind.COLON, ":", 0, false, null, startLine, startColumn); //$NON-NLS-1$
		case ';': advance(); return new Token(Kind.SEMICOLON, ";", 0, false, null, startLine, startColumn); //$NON-NLS-1$
		case ',': advance(); return new Token(Kind.COMMA, ",", 0, false, null, startLine, startColumn); //$NON-NLS-1$
		case '>': advance(); return new Token(Kind.GT, ">", 0, false, null, startLine, startColumn); //$NON-NLS-1$
		case '+': if (!startsNumber()) { advance(); return new Token(Kind.PLUS, "+", 0, false, null, startLine, startColumn); } break; //$NON-NLS-1$
		case '*': advance(); return new Token(Kind.STAR, "*", 0, false, null, startLine, startColumn); //$NON-NLS-1$
		case '.': if (!startsNumber()) { advance(); return new Token(Kind.DOT, ".", 0, false, null, startLine, startColumn); } break; //$NON-NLS-1$
		case '!': advance(); return new Token(Kind.BANG, "!", 0, false, null, startLine, startColumn); //$NON-NLS-1$
		case '=': advance(); return new Token(Kind.EQUALS, "=", 0, false, null, startLine, startColumn); //$NON-NLS-1$
		case '~':
			advance();
			if (peek() == '=') {
				advance();
				return new Token(Kind.INCLUDE_MATCH, "~=", 0, false, null, startLine, startColumn); //$NON-NLS-1$
			}
			return new Token(Kind.TILDE, "~", 0, false, null, startLine, startColumn); //$NON-NLS-1$
		case '|':
			advance();
			if (peek() == '=') {
				advance();
				return new Token(Kind.DASH_MATCH, "|=", 0, false, null, startLine, startColumn); //$NON-NLS-1$
			}
			return new Token(Kind.BAR, "|", 0, false, null, startLine, startColumn); //$NON-NLS-1$
		case '"':
		case '\'':
			return readString(c, startLine, startColumn);
		case '#':
			advance();
			String hashName = readName();
			return new Token(Kind.HASH, hashName, 0, false, null, startLine, startColumn);
		case '@':
			advance();
			String atName = readName();
			return new Token(Kind.AT_KEYWORD, atName, 0, false, null, startLine, startColumn);
		default:
			break;
		}

		if (startsNumber()) {
			return readNumber(startLine, startColumn);
		}
		if (isNameStart(c)) {
			return readIdentLike(startLine, startColumn);
		}
		// Unknown character: surface it so the parser can decide to skip or fail.
		advance();
		return new Token(Kind.IDENT, String.valueOf(c), 0, false, null, startLine, startColumn);
	}

	private Token readString(char quote, int startLine, int startColumn) {
		advance(); // opening quote
		StringBuilder sb = new StringBuilder();
		while (pos < input.length()) {
			char c = input.charAt(pos);
			if (c == quote) {
				advance();
				break;
			}
			if (c == '\\' && pos + 1 < input.length()) {
				advance();
				sb.append(input.charAt(pos));
				advance();
				continue;
			}
			sb.append(c);
			advance();
		}
		return new Token(Kind.STRING, sb.toString(), 0, false, null, startLine, startColumn);
	}

	private Token readIdentLike(int startLine, int startColumn) {
		String name = readName();
		if (peek() == '(') {
			advance(); // consume '('
			if (name.equalsIgnoreCase("url")) { //$NON-NLS-1$
				return readUri(startLine, startColumn);
			}
			return new Token(Kind.FUNCTION, name, 0, false, null, startLine, startColumn);
		}
		return new Token(Kind.IDENT, name, 0, false, null, startLine, startColumn);
	}

	private Token readUri(int startLine, int startColumn) {
		while (pos < input.length() && isWhitespace(input.charAt(pos))) {
			advance();
		}
		String value;
		char c = peek();
		if (c == '"' || c == '\'') {
			Token string = readString(c, startLine, startColumn);
			value = string.text;
		} else {
			StringBuilder sb = new StringBuilder();
			while (pos < input.length() && input.charAt(pos) != ')' && !isWhitespace(input.charAt(pos))) {
				sb.append(input.charAt(pos));
				advance();
			}
			value = sb.toString();
		}
		while (pos < input.length() && isWhitespace(input.charAt(pos))) {
			advance();
		}
		if (peek() == ')') {
			advance();
		}
		return new Token(Kind.URI, value, 0, false, null, startLine, startColumn);
	}

	private Token readNumber(int startLine, int startColumn) {
		int start = pos;
		boolean real = false;
		if (peek() == '+' || peek() == '-') {
			advance();
		}
		while (Character.isDigit(peek())) {
			advance();
		}
		if (peek() == '.') {
			real = true;
			advance();
			while (Character.isDigit(peek())) {
				advance();
			}
		}
		String numberText = input.substring(start, pos);
		double value = Double.parseDouble(numberText);

		if (peek() == '%') {
			advance();
			return new Token(Kind.PERCENTAGE, numberText, value, false, "%", startLine, startColumn); //$NON-NLS-1$
		}
		if (isNameStart(peek())) {
			String unit = readName();
			return new Token(Kind.DIMENSION, numberText, value, !real, unit, startLine, startColumn);
		}
		return new Token(Kind.NUMBER, numberText, value, !real, null, startLine, startColumn);
	}

	private String readName() {
		int start = pos;
		while (pos < input.length() && isNamePart(input.charAt(pos))) {
			advance();
		}
		return input.substring(start, pos);
	}

	private void skipComments() {
		while (pos + 1 < input.length() && input.charAt(pos) == '/' && input.charAt(pos + 1) == '*') {
			advance();
			advance();
			while (pos + 1 < input.length() && !(input.charAt(pos) == '*' && input.charAt(pos + 1) == '/')) {
				advance();
			}
			if (pos + 1 < input.length()) {
				advance();
				advance();
			} else {
				pos = input.length();
			}
		}
	}

	private boolean startsNumber() {
		char c = peek();
		if (Character.isDigit(c)) {
			return true;
		}
		if (c == '.') {
			return Character.isDigit(charAt(pos + 1));
		}
		if (c == '+' || c == '-') {
			char d = charAt(pos + 1);
			return Character.isDigit(d) || (d == '.' && Character.isDigit(charAt(pos + 2)));
		}
		return false;
	}

	private char peek() {
		return charAt(pos);
	}

	private char charAt(int index) {
		return index < input.length() ? input.charAt(index) : '\0';
	}

	private void advance() {
		if (input.charAt(pos) == '\n') {
			line++;
			column = 1;
		} else {
			column++;
		}
		pos++;
	}

	private Token token(Kind kind, String text) {
		return new Token(kind, text, 0, false, null, line, column);
	}

	private static boolean isWhitespace(char c) {
		return c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == '\f';
	}

	private static boolean isNameStart(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_' || c == '-' || c >= 0x80;
	}

	private static boolean isNamePart(char c) {
		return isNameStart(c) || (c >= '0' && c <= '9');
	}
}
