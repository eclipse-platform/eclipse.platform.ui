/*******************************************************************************
 * Copyright (c) 2014, 2015 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

	@Override
	protected void applyCSSProperty(Control control, String property, CSSValue value, String pseudo, CSSEngine engine)
			throws Exception {

		if (!(control instanceof Section) || property == null
				|| value.getCssValueType() != CSSValue.CSS_PRIMITIVE_VALUE) {
			return;
		}

		Color newColor = (Color) engine.convert(value, Color.class, control.getDisplay());
		Section section = (Section) control;

		switch (property.toLowerCase()) {
		case BACKGROUND_COLOR_GRADIENT_TITLEBAR_PROPERTY:
			section.setTitleBarGradientBackground(newColor);
			break;
		case BACKGROUND_COLOR_TITLEBAR_PROPERTY:
			section.setTitleBarBackground(newColor);
			break;
		case BORDER_COLOR_TITLEBAR_PROPERTY:
			section.setTitleBarBorderColor(newColor);
			break;
		case CSSPropertyExpandableCompositeHandler.TITLE_BAR_FOREGROUND:
			section.setTitleBarForeground(newColor);
			break;
		case CSSPropertyFormHandler.TB_TOGGLE:
			section.setToggleColor(newColor);
			break;
		case CSSPropertyFormHandler.TB_TOGGLE_HOVER:
			section.setActiveToggleColor(newColor);
			break;
		default:
			break;
		}

	}

	@Override
	protected String retrieveCSSProperty(Control control, String property, String pseudo, CSSEngine engine)
			throws Exception {

		return null;
	}

}
