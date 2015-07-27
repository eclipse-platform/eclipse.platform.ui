/*******************************************************************************
 * Copyright (c) 2008, 2015 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     Stefan Winkler <stefan@winklerweb.net> - Bug 459961
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.converters;

import org.eclipse.e4.ui.css.core.css2.CSS2ColorHelper;
import org.eclipse.e4.ui.css.core.dom.properties.converters.AbstractCSSValueConverter;
import org.eclipse.e4.ui.css.core.dom.properties.converters.ICSSValueConverter;
import org.eclipse.e4.ui.css.core.dom.properties.converters.ICSSValueConverterConfig;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTColorHelper;
import org.eclipse.swt.graphics.RGB;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.RGBColor;

/**
 * CSS Value converter to convert :
 * <ul>
 * <li>CSS Value to {@link RGB}</li>.
 * <li>{@link RGB} to String CSS Value</li>
 * </ul>
 *
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 *
 */
public class CSSValueSWTRGBConverterImpl extends AbstractCSSValueConverter {

	public static final ICSSValueConverter INSTANCE = new CSSValueSWTRGBConverterImpl();

	public CSSValueSWTRGBConverterImpl() {
		super(RGB.class);
	}

	@Override
	public Object convert(CSSValue value, CSSEngine engine, Object context)
			throws Exception {
		return CSSSWTColorHelper.getRGBA(value).rgb;
	}

	@Override
	public String convert(Object value, CSSEngine engine, Object context,
			ICSSValueConverterConfig config) throws Exception {
		RGB color = (RGB) value;
		RGBColor rgbColor = CSSSWTColorHelper.getRGBColor(color);
		return CSS2ColorHelper.getColorStringValue(rgbColor, config);
	}

}
