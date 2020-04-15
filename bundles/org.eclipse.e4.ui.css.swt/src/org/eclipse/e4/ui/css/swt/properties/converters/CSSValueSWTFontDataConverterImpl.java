/*******************************************************************************
 * Copyright (c) 2008, 2014 Angelo Zerr and others.
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
 *     Remy Chi Jian Suen <remy.suen@gmail.com>
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.converters;

import org.eclipse.e4.ui.css.core.dom.properties.converters.AbstractCSSValueConverter;
import org.eclipse.e4.ui.css.core.dom.properties.converters.ICSSValueConverter;
import org.eclipse.e4.ui.css.core.dom.properties.converters.ICSSValueConverterConfig;
import org.eclipse.e4.ui.css.core.dom.properties.css2.CSS2FontProperties;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTFontHelper;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.css.CSSValue;

/**
 * CSS Value converter to convert :
 * <ul>
 * <li>CSS Value to {@link FontData}</li>
 * <li>{@link FontData} to String CSS Value</li>
 * </ul>
 */
public class CSSValueSWTFontDataConverterImpl extends AbstractCSSValueConverter {

	public static final ICSSValueConverter INSTANCE = new CSSValueSWTFontDataConverterImpl();

	public CSSValueSWTFontDataConverterImpl(Object toType) {
		super(toType);
	}

	public CSSValueSWTFontDataConverterImpl() {
		super(FontData.class);
	}

	@Override
	public Object convert(CSSValue value, CSSEngine engine, Object context)
			throws Exception {
		FontData fontData = null;
		if (context != null) {
			if (context instanceof Display) {
				Display display = (Display) context;
				Font font = display.getSystemFont();
				fontData = CSSSWTFontHelper.getFirstFontData(font);
			}
			if (context instanceof Control) {
				Control control = (Control) context;
				Font font = control.getFont();
				fontData = CSSSWTFontHelper.getFirstFontData(font);
			}
			if (context instanceof CTabItem) {
				CTabItem item = (CTabItem) context;
				Font font = item.getFont();
				fontData = CSSSWTFontHelper.getFirstFontData(font);
			}
			if (context instanceof Font) {
				Font font = (Font)context;
				fontData = CSSSWTFontHelper.getFirstFontData(font);
			}
		}
		if (fontData != null) {
			if (value instanceof CSS2FontProperties) {
				return CSSSWTFontHelper.getFontData((CSS2FontProperties) value,
						fontData);
			}
		}
		return null;
	}

	@Override
	public String convert(Object value, CSSEngine engine, Object context,
			ICSSValueConverterConfig config) throws Exception {
		FontData fontData = (FontData) value;
		String property = (context instanceof String) ? (String) context : "";
		switch (property) {
		case "font-family":
			return CSSSWTFontHelper.getFontFamily(fontData);
		case "font-size":
			return CSSSWTFontHelper.getFontSize(fontData);
		case "font-style":
			return CSSSWTFontHelper.getFontStyle(fontData);
		case "font-weight":
			return CSSSWTFontHelper.getFontWeight(fontData);
		case "font":
			return CSSSWTFontHelper.getFontComposite(fontData);
		default:
			return null;
		}
	}

	protected Display getDisplay(Object context) {
		if (context instanceof Display) {
			return (Display) context;
		}
		if (context instanceof Control) {
			return ((Control) context).getDisplay();
		}
		return null;
	}
}
