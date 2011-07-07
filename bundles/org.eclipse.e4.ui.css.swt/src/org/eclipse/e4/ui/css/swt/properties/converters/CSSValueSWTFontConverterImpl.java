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
package org.eclipse.e4.ui.css.swt.properties.converters;

import org.eclipse.e4.ui.css.core.dom.properties.converters.ICSSValueConverter;
import org.eclipse.e4.ui.css.core.dom.properties.converters.ICSSValueConverterConfig;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTFontHelper;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.css.CSSValue;

public class CSSValueSWTFontConverterImpl extends
		CSSValueSWTFontDataConverterImpl {

	public static final ICSSValueConverter INSTANCE = new CSSValueSWTFontConverterImpl();

	public CSSValueSWTFontConverterImpl() {
		super(Font.class);
	}

	public Object convert(CSSValue value, CSSEngine engine, Object context)
			throws Exception {
		FontData fontData = (FontData) super.convert(value, engine, context);
		if (fontData != null) {
			Display display = super.getDisplay(context);
			return new Font(display, fontData);
		}
		return null;
	}

	public String convert(Object value, CSSEngine engine, Object context,
			ICSSValueConverterConfig config) throws Exception {
		Font font = (Font) value;
		return super.convert(CSSSWTFontHelper.getFirstFontData(font), engine,
				context, config);
	}
}
