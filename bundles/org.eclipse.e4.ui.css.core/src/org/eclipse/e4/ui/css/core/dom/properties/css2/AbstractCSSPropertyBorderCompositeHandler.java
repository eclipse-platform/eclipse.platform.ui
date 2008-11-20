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
package org.eclipse.e4.ui.css.core.dom.properties.css2;

import org.eclipse.e4.ui.css.core.css2.CSS2ColorHelper;
import org.eclipse.e4.ui.css.core.dom.properties.AbstractCSSPropertyCompositeHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

/**
 * Abstract class which dispatch border CSS Property defined to call the
 * applyCSSProperty methods CSS Properties border-color, border-style,
 * border-width.
 * 
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 * 
 */
public abstract class AbstractCSSPropertyBorderCompositeHandler extends
		AbstractCSSPropertyCompositeHandler {

	private static final String[] BORDER_CSSPROPERTIES = { "border-width",
			"border-style", "border-color" };

	public void applyCSSProperty(Object element, CSSValue value, String pseudo,
			CSSEngine engine) throws Exception {
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			CSSPrimitiveValue primitiveValue = (CSSPrimitiveValue) value;
			short type = primitiveValue.getPrimitiveType();
			switch (type) {
			case CSSPrimitiveValue.CSS_IDENT:
				if (CSS2ColorHelper
						.isColorName(primitiveValue.getStringValue())) {
					engine.applyCSSProperty(element, "border-color", value,
							pseudo);
				} else {
					engine.applyCSSProperty(element, "border-style", value,
							pseudo);
				}
				break;
			case CSSPrimitiveValue.CSS_RGBCOLOR:
				engine.applyCSSProperty(element, "border-color", value, pseudo);
				break;
			case CSSPrimitiveValue.CSS_PT:
			case CSSPrimitiveValue.CSS_NUMBER:
			case CSSPrimitiveValue.CSS_PX:
				engine.applyCSSProperty(element, "border-width", value, pseudo);
				break;
			}
		}
	}

	public boolean isCSSPropertyComposite(String property) {
		return "border".equals(property);
	}

	public String[] getCSSPropertiesNames(String property) {
		if ("border".equals(property))
			return BORDER_CSSPROPERTIES;
		return null;
	}

}
