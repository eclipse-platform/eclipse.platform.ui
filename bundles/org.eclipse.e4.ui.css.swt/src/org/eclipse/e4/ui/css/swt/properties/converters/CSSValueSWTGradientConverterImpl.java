/*******************************************************************************
 * Copyright (c) 2008, 2009 Angelo Zerr and others.
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

import org.eclipse.swt.widgets.Display;

import java.util.List;
import org.eclipse.e4.ui.css.core.dom.properties.Gradient;
import org.eclipse.e4.ui.css.core.dom.properties.converters.AbstractCSSValueConverter;
import org.eclipse.e4.ui.css.core.dom.properties.converters.ICSSValueConverter;
import org.eclipse.e4.ui.css.core.dom.properties.converters.ICSSValueConverterConfig;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTColorHelper;
import org.eclipse.swt.graphics.Color;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

/**
 * CSS Value converter to convert :
 * <ul>
 * <li>CSS Value to {@link Gradient}</li>.
 * <li>{@link Gradient} to String CSS Value</li>
 * </ul>
 */
public class CSSValueSWTGradientConverterImpl extends AbstractCSSValueConverter {

	public static final ICSSValueConverter INSTANCE = new CSSValueSWTGradientConverterImpl();

	public CSSValueSWTGradientConverterImpl() {
		super(Gradient.class);
	}

	@Override
	public Object convert(CSSValue value, CSSEngine engine, Object context) throws Exception {
		if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
			Display display = (context instanceof Display) ? (Display) context : null;
			Gradient grad = CSSSWTColorHelper.getGradient((CSSValueList) value, display);
			List values = grad.getValues();
			for (int i = 0; i < values.size(); i++) {
				//Ensure all the colors are already converted and in the registry
				//TODO see bug #278077	
				CSSPrimitiveValue prim = (CSSPrimitiveValue) values.get(i);
				engine.convert(prim, Color.class, context);
			}
			return grad;
		}

		return null;
	}

	@Override
	public String convert(Object value, CSSEngine engine, Object context,
			ICSSValueConverterConfig config) throws Exception {
		return null;
	}

}
