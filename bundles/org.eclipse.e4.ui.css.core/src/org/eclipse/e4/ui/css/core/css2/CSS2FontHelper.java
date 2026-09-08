/*******************************************************************************
 * Copyright (c) 2008, 2013 Angelo Zerr and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.css2;

import java.util.Locale;
import java.util.OptionalInt;

import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssNumeric;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssPrimitive;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssText;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssUnit;

/**
 * CSS2 Font Helper.
 *
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 */
public class CSS2FontHelper {

	/** CSS font-weight of a regular face. */
	public static final int FONT_WEIGHT_NORMAL = 400;

	/** CSS font-weight of a bold face. */
	public static final int FONT_WEIGHT_BOLD = 700;

	private static final int MIN_FONT_WEIGHT = 1;

	private static final int MAX_FONT_WEIGHT = 1000;

	/**
	 * Return CSS2 font-family. Escape font <code>family</code> with " if need.
	 */
	public static String getFontFamily(String family) {
		if (family == null) {
			return null;
		}
		if (family.indexOf(' ') != -1 || family.startsWith("@")) {
			family = "\"" + family + "\"";
		}
		return family;
	}

	/**
	 * Return CSS2 font-size of int <code>size</code>.
	 */
	public static String getFontSize(int size) {
		return Integer.toString(size);
	}

	/**
	 * Return CSS2 font-style.
	 */
	public static String getFontStyle(boolean isItalic) {
		if (isItalic) {
			return "italic";
		}
		return "normal";
	}

	/**
	 * Return CSS2 font-weight.
	 */
	public static String getFontWeight(boolean isBold) {
		if (isBold) {
			return "bold";
		}
		return "normal";
	}

	/**
	 * Numeric font-weight of <code>value</code>, empty if it is not a weight.
	 * Relative keywords are resolved against <code>inheritedWeight</code>.
	 */
	public static OptionalInt getFontWeight(CssPrimitive value, int inheritedWeight) {
		if (value instanceof CssNumeric numeric && numeric.unit() == CssUnit.NUMBER) {
			int weight = (int) Math.round(numeric.value());
			return weight >= MIN_FONT_WEIGHT && weight <= MAX_FONT_WEIGHT ? OptionalInt.of(weight)
					: OptionalInt.empty();
		}
		if (value instanceof CssText text) {
			return switch (text.value().toLowerCase(Locale.ENGLISH)) {
			case "normal" -> OptionalInt.of(FONT_WEIGHT_NORMAL);
			case "bold" -> OptionalInt.of(FONT_WEIGHT_BOLD);
			case "bolder" -> OptionalInt.of(bolder(inheritedWeight));
			case "lighter" -> OptionalInt.of(lighter(inheritedWeight));
			default -> OptionalInt.empty();
			};
		}
		return OptionalInt.empty();
	}

	// CSS Fonts 4: relative weights step between the anchors 100/400/700/900
	private static int bolder(int inheritedWeight) {
		if (inheritedWeight < 350) {
			return 400;
		}
		if (inheritedWeight < 550) {
			return 700;
		}
		return inheritedWeight < 900 ? 900 : inheritedWeight;
	}

	private static int lighter(int inheritedWeight) {
		if (inheritedWeight < 100) {
			return inheritedWeight;
		}
		if (inheritedWeight < 550) {
			return 100;
		}
		return inheritedWeight < 750 ? 400 : 700;
	}

	/**
	 * Return the CSS Font Property name (font-style, font-weight, font-size,
	 * font-family) for the given <code>value</code>, <code>null</code> if the value
	 * belongs to none of them.
	 */
	public static String getCSSFontPropertyName(CssPrimitive value) {
		if (value instanceof CssText text && (text.kind() == CssText.Kind.STRING || text.kind() == CssText.Kind.IDENT)) {
			switch (text.value()) {
			case "italic":
			case "oblique":
				return "font-style";
			case "normal":
			case "bold":
			case "bolder":
			case "lighter":
				return "font-weight";
			default:
				return "font-family";
			}
		}
		if (value instanceof CssNumeric numeric) {
			// In the shorthand a bare 100..900 is a weight, not a size
			if (numeric.unit() == CssUnit.NUMBER && isWeightStep(numeric.value())) {
				return "font-weight";
			}
			if (numeric.unit() == CssUnit.PT || numeric.unit() == CssUnit.NUMBER || numeric.unit() == CssUnit.PX
					|| numeric.unit() == CssUnit.EM || numeric.unit() == CssUnit.PERCENT) {
				return "font-size";
			}
		}
		return null;
	}

	private static boolean isWeightStep(double value) {
		return value >= 100 && value <= 900 && value % 100 == 0;
	}
}
