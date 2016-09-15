/*******************************************************************************
 * Copyright (c) 2008, 2014 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.converters;

import org.eclipse.e4.ui.css.core.css2.CSS2ColorHelper;
import org.eclipse.e4.ui.css.core.dom.properties.converters.AbstractCSSValueConverter;
import org.eclipse.e4.ui.css.core.dom.properties.converters.ICSSValueConverter;
import org.eclipse.e4.ui.css.core.dom.properties.converters.ICSSValueConverterConfig;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.exceptions.DOMExceptionImpl;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTColorHelper;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.RGBColor;

/**
 * CSS Value converter to convert :
 * <ul>
 * <li>CSS Value to {@link Color}</li>.
 * <li>{@link Color} to String CSS Value</li>
 * </ul>
 */
public class CSSValueSWTColorConverterImpl extends AbstractCSSValueConverter {

	public static final ICSSValueConverter INSTANCE = new CSSValueSWTColorConverterImpl();

	public CSSValueSWTColorConverterImpl() {
		super(Color.class);
	}

	@Override
	public Color convert(CSSValue value, CSSEngine engine, Object context)
			throws DOMException {
		Display display = (Display) context;
		Color color = CSSSWTColorHelper.getSWTColor(value, display);
		if (color == null) {
			throw new DOMExceptionImpl(DOMException.INVALID_ACCESS_ERR,
					DOMExceptionImpl.RGBCOLOR_ERROR);
		}

		return color;
	}

	@Override
	public String convert(Object value, CSSEngine engine, Object context,
			ICSSValueConverterConfig config) throws Exception {
		Color color = (Color) value;
		RGBColor rgbColor = CSSSWTColorHelper.getRGBColor(color);
		return CSS2ColorHelper.getColorStringValue(rgbColor, config);
	}
}
