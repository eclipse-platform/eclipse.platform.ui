/*******************************************************************************
 * Copyright (c) 2014, 2015 vogella GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 422702
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 430370
 *******************************************************************************/
package org.eclipse.ui.internal.forms.css.properties.css2;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.Section;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyTitleFormsHandler extends AbstractCSSPropertySWTHandler {

	private static final String BACKGROUND_COLOR_GRADIENT_TITLEBAR_PROPERTY = "background-color-gradient-titlebar"; //$NON-NLS-1$
	private static final String BACKGROUND_COLOR_TITLEBAR_PROPERTY = "background-color-titlebar"; //$NON-NLS-1$
	private static final String BORDER_COLOR_TITLEBAR_PROPERTY = "border-color-titlebar"; //$NON-NLS-1$

	protected void applyCSSProperty(Control control, String property, CSSValue value, String pseudo, CSSEngine engine)
			throws Exception {

		if (BACKGROUND_COLOR_GRADIENT_TITLEBAR_PROPERTY.equalsIgnoreCase(property)) {
			if (control instanceof Section) {
				if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
					Color newColor = (Color) engine.convert(value, Color.class, control.getDisplay());
					((Section) control).setTitleBarGradientBackground(newColor);
				} else if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {

				}
			}
		} else if (BACKGROUND_COLOR_TITLEBAR_PROPERTY.equalsIgnoreCase(property)) {
			if (control instanceof Section) {
				if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
					Color newColor = (Color) engine.convert(value, Color.class, control.getDisplay());
					((Section) control).setTitleBarBackground(newColor);
				} else if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {

				}
			}
		} else if (BORDER_COLOR_TITLEBAR_PROPERTY.equalsIgnoreCase(property)) {
			if (control instanceof Section) {
				if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
					Color newColor = (Color) engine.convert(value, Color.class, control.getDisplay());
					((Section) control).setTitleBarBorderColor(newColor);
				} else if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {

				}
			}
		}
	}

	protected String retrieveCSSProperty(Control control, String property, String pseudo, CSSEngine engine)
			throws Exception {

		return null;
	}

}
