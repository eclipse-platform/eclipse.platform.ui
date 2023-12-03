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

import org.w3c.dom.css.CSSPrimitiveValue;

/**
 * CSS2 Font Helper.
 *
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 */
public class CSS2FontHelper {

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
	 * Return the CSS Font Property name (font-style, font-weight, font-size,
	 * font-family) switch the {@link CSSPrimitiveValue} <code>value</code>.
	 */
	public static String getCSSFontPropertyName(CSSPrimitiveValue value) {
		short type = value.getPrimitiveType();
		switch (type) {
		case CSSPrimitiveValue.CSS_STRING:
		case CSSPrimitiveValue.CSS_IDENT:
			switch (value.getStringValue()) {
			case "italic":
			case "oblique":
				return "font-style";
			case "normal":
			case "bold":
			case "bolder":
				return "font-weight";
			default:
				return "font-family";
			}
		case CSSPrimitiveValue.CSS_PT:
		case CSSPrimitiveValue.CSS_NUMBER:
		case CSSPrimitiveValue.CSS_PX:
			return "font-size";
		}
		return null;
	}
}
