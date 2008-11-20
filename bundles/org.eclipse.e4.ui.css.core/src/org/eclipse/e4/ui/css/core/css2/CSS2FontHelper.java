/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * 
 */
public class CSS2FontHelper {

	/**
	 * Return CSS2 font-family. Escape font <code>family</code> with " if
	 * need.
	 * 
	 * @param family
	 * @return
	 */
	public static String getFontFamily(String family) {
		if (family == null)
			return null;
		if (family.indexOf(" ") != -1 || family.startsWith("@"))
			family = "\"" + family + "\"";
		return family;
	}

	/**
	 * Return CSS2 font-size of int <code>size</code>.
	 * 
	 * @param size
	 * @return
	 */
	public static String getFontSize(int size) {
		return size + "";
	}

	/**
	 * Return CSS2 font-style.
	 * 
	 * @param isItalic
	 * @return
	 */
	public static String getFontStyle(boolean isItalic) {
		if (isItalic)
			return "italic";
		return "normal";
	}

	/**
	 * Return CSS2 font-weight.
	 * 
	 * @param isBold
	 * @return
	 */
	public static String getFontWeight(boolean isBold) {
		if (isBold)
			return "bold";
		return "normal";
	}

	/**
	 * Return the CSS Font Property name (font-style, font-weight, font-size,
	 * font-family) switch the {@link CSSPrimitiveValue} <code>value</code>.
	 * 
	 * @param value
	 * @return
	 */
	public static String getCSSFontPropertyName(CSSPrimitiveValue value) {
		short type = value.getPrimitiveType();
		switch (type) {
		case CSSPrimitiveValue.CSS_STRING:
		case CSSPrimitiveValue.CSS_IDENT:
			String s = value.getStringValue();
			if (/* "normal".equals(s) || */"italic".equals(s)
					|| "oblique".equals(s)) {
				return "font-style";
			}
			if ("normal".equals(s) || "bold".equals(s) || "bolder".equals(s)) {
				return "font-weight";
			}
			return "font-family";
		case CSSPrimitiveValue.CSS_PT:
		case CSSPrimitiveValue.CSS_NUMBER:
		case CSSPrimitiveValue.CSS_PX:
			return "font-size";
		}
		return null;
	}
}
