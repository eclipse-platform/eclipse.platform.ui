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
 * RGBColorImpl} / {@code CSSValueListImpl} wrappers and removes the last SAC
 * dependency ({@code LexicalUnit}).
 *
 * <p>
 * The variants still implement the W3C DOM-CSS interfaces so the property
 * handlers and converters that read values through {@link CSSValue} /
 * {@link CSSPrimitiveValue} keep working unchanged. Those consumers move to
 * pattern matching on the records in a later step, after which the W3C
 * interfaces can be dropped.
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
	 * A single CSS value. Provides the W3C boilerplate; concrete variants only
	 * implement {@link #getPrimitiveType()}, {@link #getCssText()} and whichever
	 * of {@link #getFloatValue(short)} / {@link #getStringValue()} /
	 * {@link #getRGBColorValue()} applies.
	 */
	public sealed interface CssPrimitive extends CssValue, CSSPrimitiveValue
			permits CssNumber, CssDimension, CssText, CssColor, CssOperator {

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

	/** {@code 34} or {@code 2.0} - a unitless number. */
	public record CssNumber(double value, boolean integer) implements CssPrimitive {
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

	/** {@code 26px}, {@code 30%}, {@code 75em} - a number with a unit. */
	public record CssDimension(double value, short primitiveType, String unit) implements CssPrimitive {
		@Override
		public short getPrimitiveType() {
			return primitiveType;
		}

		@Override
		public float getFloatValue(short unitType) {
			return (float) value;
		}

		@Override
		public String getCssText() {
			return (float) value + unit;
		}
	}

	/** {@code red}, {@code 'a string'}, {@code url(x)}, {@code inherit}. */
	public record CssText(short primitiveType, String value) implements CssPrimitive {
		@Override
		public short getPrimitiveType() {
			return primitiveType;
		}

		@Override
		public String getStringValue() {
			return value;
		}

		@Override
		public String getCssText() {
			return switch (primitiveType) {
			case CSS_URI -> "url(" + value + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			default -> value;
			};
		}
	}

	/** {@code rgb(...)} or a {@code #rgb} / {@code #rrggbb} colour. */
	public record CssColor(CssPrimitive red, CssPrimitive green, CssPrimitive blue)
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
	 * for parity with the former SAC behaviour where the comma was a list item.
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
