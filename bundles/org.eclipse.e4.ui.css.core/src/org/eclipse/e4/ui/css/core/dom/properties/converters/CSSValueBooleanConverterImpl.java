/*******************************************************************************
 * Copyright (c) 2008, 2014 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.dom.properties.converters;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

/**
 * CSS Value converter to convert :
 * <ul>
 * <li>CSS Value to {@link Boolean}</li>.
 * <li>{@link Boolean} to String CSS Value</li>
 * </ul>
 *
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 *
 */
public class CSSValueBooleanConverterImpl extends AbstractCSSValueConverter {

	public static final ICSSValueConverter INSTANCE = new CSSValueBooleanConverterImpl();

	public CSSValueBooleanConverterImpl() {
		super(Boolean.class);
	}

	@Override
	public Object convert(CSSValue value, CSSEngine engine, Object context)
			throws Exception {
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			CSSPrimitiveValue primitiveValue = (CSSPrimitiveValue) value;
			if ("true".equals(primitiveValue.getStringValue()))
				return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	@Override
	public String convert(Object value, CSSEngine engine, Object context,
			ICSSValueConverterConfig config) throws Exception {
		if (value instanceof Boolean) {
			Boolean b = (Boolean) value;
			if (b.booleanValue())
				return "true";
		}
		return "false";
	}
}
