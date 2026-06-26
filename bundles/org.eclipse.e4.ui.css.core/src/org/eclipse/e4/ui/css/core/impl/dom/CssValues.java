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
package org.eclipse.e4.ui.css.core.impl.dom;

import java.util.List;

import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;
import org.w3c.dom.css.Counter;
import org.w3c.dom.css.RGBColor;
import org.w3c.dom.css.Rect;

/**
 * The internal CSS value model produced by the parser. A small sealed hierarchy
 * of immutable values that replaces the former {@code Measure} / {@code
 * RGBColorImpl} / {@code CSSValueListImpl} wrappers.
 *
 * <p>
 * Consumers pattern-match on the record variants ({@link CssNumber},
 * {@link CssDimension}, {@link CssText}, {@link CssColor}, {@link CssList})
 * and read their components. The variants still implement the W3C DOM-CSS
 * interfaces as a transitional bridge; the bridge goes away once the
 * computed-style cascade is internal as well.
 * </p>
 */
public final class CssValues {

	private CssValues() {
		// constants only
	}

	/** A parsed CSS value: either a primitive or a whitespace/comma separated list. */
	public sealed interface CssValue extends CSSValue permits CssPrimitive, CssList {
	}

	/**
	 * A single CSS value. Provides the W3C bridge boilerplate; read the values
	 * through the record components, not through the W3C accessors.
	 */
	public sealed interface CssPrimitive extends CssValue, CSSPrimitiveValue
			permits CssNumeric, CssText, CssColor, CssOperator {

		@Override
		default short getCssValueType() {
			return CSS_PRIMITIVE_VALUE;
		}

		@Override
		default float getFloatValue(short unitType) throws DOMException {
			throw invalidAccess();
		}

		@Override
		default String getStringValue() throws DOMException {
			throw invalidAccess();
		}

		@Override
		default RGBColor getRGBColorValue() throws DOMException {
			throw invalidAccess();
		}

		@Override
		default Counter getCounterValue() throws DOMException {
			throw invalidAccess();
		}

		@Override
		default Rect getRectValue() throws DOMException {
			throw invalidAccess();
		}

		@Override
		default void setCssText(String cssText) throws DOMException {
			throw readOnly();
		}

		@Override
		default void setFloatValue(short unitType, float floatValue) throws DOMException {
			throw readOnly();
		}

		@Override
		default void setStringValue(short stringType, String stringValue) throws DOMException {
			throw readOnly();
		}
	}

	/** Unit of a {@link CssNumeric} value. {@link #NUMBER} marks a unitless number. */
	public enum CssUnit {
		NUMBER(""), PX("px"), EM("em"), EX("ex"), CM("cm"), MM("mm"), IN("in"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
		PT("pt"), PC("pc"), DEG("deg"), PERCENT("%"), OTHER(""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

		private final String text;

		CssUnit(String text) {
			this.text = text;
		}

		/** Canonical unit text, e.g. {@code "px"}; empty for {@link #NUMBER} and {@link #OTHER}. */
		public String text() {
			return text;
		}
	}

	/** A numeric value: a unitless {@link CssNumber} or a {@link CssDimension} with a unit. */
	public sealed interface CssNumeric extends CssPrimitive permits CssNumber, CssDimension {

		/** The numeric magnitude, without unit conversion. */
		double value();

		/** The unit; {@link CssUnit#NUMBER} for a unitless number. */
		CssUnit unit();
	}

	/** {@code 34} or {@code 2.0} - a unitless number. */
	public record CssNumber(double value, boolean integer) implements CssNumeric {
		@Override
		public CssUnit unit() {
			return CssUnit.NUMBER;
		}

		@Override
		public short getPrimitiveType() {
			return CSS_NUMBER;
		}

		@Override
		public float getFloatValue(short unitType) {
			return (float) value;
		}

		@Override
		public String getCssText() {
			return integer ? Integer.toString((int) value) : Float.toString((float) value);
		}
	}

	/**
	 * {@code 26px}, {@code 30%}, {@code 75em} - a number with a unit.
	 * {@code unitText} keeps the source spelling for units the {@link CssUnit}
	 * enum does not model ({@link CssUnit#OTHER}).
	 */
	public record CssDimension(double value, CssUnit unit, String unitText) implements CssNumeric {

		public CssDimension(double value, CssUnit unit) {
			this(value, unit, unit.text());
		}

		@Override
		public short getPrimitiveType() {
			return switch (unit) {
			case NUMBER -> CSS_NUMBER;
			case PX -> CSS_PX;
			case EM -> CSS_EMS;
			case EX -> CSS_EXS;
			case CM -> CSS_CM;
			case MM -> CSS_MM;
			case IN -> CSS_IN;
			case PT -> CSS_PT;
			case PC -> CSS_PC;
			case DEG -> CSS_DEG;
			case PERCENT -> CSS_PERCENTAGE;
			case OTHER -> CSS_DIMENSION;
			};
		}

		@Override
		public float getFloatValue(short unitType) {
			return (float) value;
		}

		@Override
		public String getCssText() {
			return (float) value + unitText;
		}
	}

	/** {@code red}, {@code 'a string'}, {@code url(x)}, {@code inherit}. */
	public record CssText(Kind kind, String value) implements CssPrimitive {

		/** What textual form the value had in the source. */
		public enum Kind {
			IDENT, STRING, URI, INHERIT
		}

		@Override
		public short getPrimitiveType() {
			return switch (kind) {
			case IDENT -> CSS_IDENT;
			case STRING -> CSS_STRING;
			case URI -> CSS_URI;
			case INHERIT -> CSS_INHERIT;
			};
		}

		@Override
		public String getStringValue() {
			return value;
		}

		@Override
		public String getCssText() {
			return switch (kind) {
			case URI -> "url(" + value + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			default -> value;
			};
		}
	}

	/** {@code rgb(...)} or a {@code #rgb} / {@code #rrggbb} colour. */
	public record CssColor(CssNumeric red, CssNumeric green, CssNumeric blue)
			implements CssPrimitive, RGBColor {
		@Override
		public short getPrimitiveType() {
			return CSS_RGBCOLOR;
		}

		@Override
		public RGBColor getRGBColorValue() {
			return this;
		}

		@Override
		public CSSPrimitiveValue getRed() {
			return red;
		}

		@Override
		public CSSPrimitiveValue getGreen() {
			return green;
		}

		@Override
		public CSSPrimitiveValue getBlue() {
			return blue;
		}

		@Override
		public String getCssText() {
			return "rgb(" + red.getCssText() + ", " + green.getCssText() + ", " + blue.getCssText() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
	}

	/**
	 * A separator (currently only {@code ,}) carried inside a value list, kept
	 * for parity with the historical behaviour where the comma was a list item.
	 */
	public record CssOperator(String text) implements CssPrimitive {
		@Override
		public short getPrimitiveType() {
			return CSS_CUSTOM;
		}

		@Override
		public String getCssText() {
			return text;
		}
	}

	/** A whitespace or comma separated sequence of values, e.g. {@code 1px 2px 3px}. */
	public record CssList(List<CssValue> values) implements CssValue, CSSValueList {

		public CssList {
			values = List.copyOf(values);
		}

		@Override
		public short getCssValueType() {
			return CSS_VALUE_LIST;
		}

		@Override
		public int getLength() {
			return values.size();
		}

		@Override
		public CSSValue item(int index) {
			return values.get(index);
		}

		@Override
		public String getCssText() {
			StringBuilder sb = new StringBuilder();
			for (CssValue value : values) {
				sb.append(value.getCssText()).append(' ');
			}
			return sb.toString().trim();
		}

		@Override
		public void setCssText(String cssText) throws DOMException {
			throw readOnly();
		}
	}

	private static DOMException invalidAccess() {
		return new DOMException(DOMException.INVALID_ACCESS_ERR, "Value does not support this access"); //$NON-NLS-1$
	}

	private static DOMException readOnly() {
		return new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "CSS value is read-only"); //$NON-NLS-1$
	}
}
