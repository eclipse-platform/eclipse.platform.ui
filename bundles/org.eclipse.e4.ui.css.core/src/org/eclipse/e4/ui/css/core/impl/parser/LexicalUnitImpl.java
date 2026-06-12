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

import org.w3c.css.sac.LexicalUnit;

/**
 * A hand-built {@link LexicalUnit} produced by the internal CSS value parser.
 *
 * <p>
 * The existing DOM-CSS value classes ({@code Measure}, {@code RGBColorImpl},
 * {@code CSSValueListImpl}) and {@code CSSValueFactory} are written against the
 * SAC {@link LexicalUnit} interface. Rather than rewrite that value model now,
 * the new parser emits these units so {@code CSSValueFactory.newValue} keeps
 * producing exactly the same value objects the Batik path produced. The value
 * model itself is replaced later (Phase 4); the SAC interface is the only SAC
 * type that survives until then.
 * </p>
 */
final class LexicalUnitImpl implements LexicalUnit {

	private final short type;
	private int integerValue;
	private float floatValue;
	private String text;
	private LexicalUnit parameters;
	private LexicalUnit next;
	private LexicalUnit previous;

	private LexicalUnitImpl(short type) {
		this.type = type;
	}

	static LexicalUnitImpl integer(int value) {
		LexicalUnitImpl unit = new LexicalUnitImpl(SAC_INTEGER);
		unit.integerValue = value;
		unit.floatValue = value;
		return unit;
	}

	static LexicalUnitImpl real(float value) {
		LexicalUnitImpl unit = new LexicalUnitImpl(SAC_REAL);
		unit.floatValue = value;
		return unit;
	}

	static LexicalUnitImpl dimension(short type, float value, String unitText) {
		LexicalUnitImpl unit = new LexicalUnitImpl(type);
		unit.floatValue = value;
		unit.text = unitText;
		return unit;
	}

	static LexicalUnitImpl ident(String value) {
		LexicalUnitImpl unit = new LexicalUnitImpl(SAC_IDENT);
		unit.text = value;
		return unit;
	}

	static LexicalUnitImpl string(String value) {
		LexicalUnitImpl unit = new LexicalUnitImpl(SAC_STRING_VALUE);
		unit.text = value;
		return unit;
	}

	static LexicalUnitImpl uri(String value) {
		LexicalUnitImpl unit = new LexicalUnitImpl(SAC_URI);
		unit.text = value;
		return unit;
	}

	static LexicalUnitImpl inherit() {
		return new LexicalUnitImpl(SAC_INHERIT);
	}

	static LexicalUnitImpl comma() {
		return new LexicalUnitImpl(SAC_OPERATOR_COMMA);
	}

	static LexicalUnitImpl rgbColor(LexicalUnit parameters) {
		LexicalUnitImpl unit = new LexicalUnitImpl(SAC_RGBCOLOR);
		unit.text = "rgb"; //$NON-NLS-1$
		unit.parameters = parameters;
		return unit;
	}

	/** Append {@code unit} after this one and return it for chaining. */
	LexicalUnitImpl append(LexicalUnitImpl unit) {
		this.next = unit;
		unit.previous = this;
		return unit;
	}

	@Override
	public short getLexicalUnitType() {
		return type;
	}

	@Override
	public LexicalUnit getNextLexicalUnit() {
		return next;
	}

	@Override
	public LexicalUnit getPreviousLexicalUnit() {
		return previous;
	}

	@Override
	public int getIntegerValue() {
		return integerValue;
	}

	@Override
	public float getFloatValue() {
		return floatValue;
	}

	@Override
	public String getDimensionUnitText() {
		return text == null ? "" : text; //$NON-NLS-1$
	}

	@Override
	public String getFunctionName() {
		return text;
	}

	@Override
	public LexicalUnit getParameters() {
		return parameters;
	}

	@Override
	public String getStringValue() {
		return text;
	}

	@Override
	public LexicalUnit getSubValues() {
		return null;
	}
}
